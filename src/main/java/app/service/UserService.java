package app.service;

import app.bot.api.CheckSubscribeToChannel;
import app.bot.api.MessagingService;
import app.config.AppConfig;
import app.data.Messages;
import app.data.UserActionData;
import app.model.Partner;
import app.model.User;
import app.repository.PartnersRepository;
import app.repository.UserRepository;
import app.util.Sleep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final AppConfig appConfig;
    private final UserRepository repo;
    private final MessagingService msg;
    private final PartnersRepository partners;
    private final CheckSubscribeToChannel checkSubscribeToChannel;
    private final UserActionService userActionService;


    public UserService(AppConfig appConfig,
                       PartnersRepository partners,
                       @Lazy MessagingService msg,
                       UserRepository userRepository,
                       CheckSubscribeToChannel checkSubscribeToChannel,
                       UserActionService userActionService

    ) {
        this.appConfig = appConfig;
        this.partners = partners;
        this.msg = msg;
        this.repo = userRepository;
        this.checkSubscribeToChannel = checkSubscribeToChannel;
        this.userActionService = userActionService;
    }

    private volatile boolean isRunning = false;

    public void saveUser(Update update, Long chatId, Long ref) {
        repo.save(new User(update, chatId, ref));
    }

    public long saveNewUtmUser(String text) {
        Optional<User> optionalUser = repo.findFirstByChatIdLessThanOrderByChatIdDesc(1000L);
        long id = 0L;

        if (optionalUser.isPresent()) {
             id = optionalUser.get().getChatId() + 1;
        }
        return repo.save(new User(id, text)).getChatId();
    }

    public List<User> findAllUtmUsers() {
        return repo.findByChatIdBetweenOrderByChatIdAsc(-1L, 1000L);
    }

    public boolean existsById(Long chatId) {
        return repo.existsById(chatId);
    }

    @Scheduled(fixedDelay = 600000)
    public void subscribeChecking() {
        if (isRunning) {
            log.warn("Проверка подписок уже выполняется, пропускаем");
            return;
        }

        isRunning = true;
        try {
            log.info("Начинаем проверку подписок");

            List<Partner> partnerList = partners.findAll();
            List<User> users = repo.findAll();

            log.info("Загружено {} партнеров", partnerList.size());
            log.info("Загружено {} пользователей", users.size());

            final long now = System.currentTimeMillis();
            final long threeHoursAgo = now - TimeUnit.HOURS.toMillis(3);
            final long fortyEightHoursAgo = now - TimeUnit.DAYS.toMillis(2);

            processActiveUsers(partnerList, users, threeHoursAgo, now);
            processInactiveUsers(partnerList, users, fortyEightHoursAgo, now);

            log.info("Проверка подписок завершена");
        } finally {
            isRunning = false;
        }
    }

    private void processActiveUsers(List<Partner> partnerList, List<User> users, long threeHoursAgo, long now) {
        for (User user : users) {
            if (user.isActive() && threeHoursAgo > user.getLastSubscribeChecked()) {

                log.info("Проверяем пользователя: {}", user.getChatId());
                Map<Partner, Boolean> result = checkSubscription(user.getChatId(), partnerList);
                boolean notActive = result != null;

                if (notActive) {
                    log.warn("Пользователь {} неактивен", user.getChatId());
                    msg.process(Messages.leftUser(user.getChatId(), result));
                    userActionService.addUserAction(user.getChatId(), UserActionData.LEFT_PUBLICK_CHANNEL);
                }

                user.setActive(false);
                user.setLastSubscribeChecked(now);
                repo.save(user);

                Sleep.sleepSafely(3000);
            }
        }
    }

    private void processInactiveUsers(List<Partner> partnerList, List<User> users, long fortyEightHoursAgo, long now) {
        for (User user : users) {

            if (!user.isKickUserFromChat() && !user.isActive() && fortyEightHoursAgo > user.getLastSubscribeChecked()) {

                log.info("Проверка для исключения: {}", user.getChatId());
                Map<Partner, Boolean> result = checkSubscription(user.getChatId(), partnerList);
                boolean notActive = result != null;

                if (notActive) {
                    log.warn("Исключаем пользователя: {}", user.getChatId());
                    msg.process(Messages.kickUserFromChat(user.getChatId(), appConfig.getBotPrivateChannel()));
                    user.setKickUserFromChat(true);
                    user.setLastSubscribeChecked(now + TimeUnit.DAYS.toMillis(100000));
                    Sleep.sleepSafely(3000);

                    userActionService.addUserAction(user.getChatId(), UserActionData.REMOVE_PRIVATE_CHANNEL_48H);
                } else {
                    user.setActive(true); // Возвращаем в активные
                    user.setLastSubscribeChecked(now);

                    userActionService.addUserAction(user.getChatId(), UserActionData.RETURN_PUBLICK_CHANNEL_48H);
                }

                repo.save(user);
                Sleep.sleepSafely(3000);
            }
        }
    }

    private Map<Partner, Boolean> checkSubscription(Long chatId, List<Partner> partnerList) {
        Map<Partner, Boolean> results = checkSubscribeToChannel.checkList(chatId, partnerList);
        return !results.containsValue(false) ? null : results;
    }
}