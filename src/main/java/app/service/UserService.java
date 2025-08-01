package app.service;

import app.bot.api.CheckSubscribeToChannel;
import app.bot.api.MessagingService;
import app.config.AppConfig;
import app.data.Messages;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final AppConfig appConfig;
    private final MessagingService msg;
    private final PartnersRepository partners;
    private final UserRepository repo;
    private final CheckSubscribeToChannel checkSubscribeToChannel;

    private List<Partner> partnerList = new ArrayList<>();
    private volatile boolean isRunning = false;

    public UserService(AppConfig appConfig,
                       PartnersRepository partners,
                       @Lazy MessagingService msg,
                       UserRepository userRepository,
                       CheckSubscribeToChannel checkSubscribeToChannel) {
        this.appConfig = appConfig;
        this.partners = partners;
        this.msg = msg;
        this.repo = userRepository;
        this.checkSubscribeToChannel = checkSubscribeToChannel;
    }

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
            this.partnerList = this.partnerList.isEmpty() ? partners.findAll() : this.partnerList;
            log.info("Загружено {} партнеров", partnerList.size());

            final long now = System.currentTimeMillis();

            List<User> users = repo.findAll().
                    stream().filter(User::isKickUserFromChat).toList();

            // Проверка активных пользователей (каждые 3 часа)
            processActiveUsers(users, now - TimeUnit.HOURS.toMillis(3), now);

            // Исключение неактивных (каждые 48 часов)
            processInactiveUsers(users, now - TimeUnit.DAYS.toMillis(2), now);

            log.info("Проверка подписок завершена");
        } finally {
            isRunning = false;
        }
    }

    private void processActiveUsers(List<User> users, long threeHoursAgo, long now) {
        for (User user : users) {
            if ((now - threeHoursAgo) < user.getLastSubscribeChecked()) continue;

            log.info("Проверяем пользователя: {}", user.getChatId());
            Map<Partner, Boolean> result = checkSubscription(user.getChatId());
            boolean notActive = result != null;

            if (notActive) {
                log.warn("Пользователь {} неактивен", user.getChatId());
                msg.processMessage(Messages.leftUser(user.getChatId(), result));
            }

            updateUserStatus(user, !notActive, now);
            Sleep.sleepSafely(3000);
        }
    }

    private void processInactiveUsers(List<User> users, long fortyEightHoursAgo, long now) {
        for (User user : users) {
            if ((now - fortyEightHoursAgo) < user.getLastSubscribeChecked()) continue;

            log.info("Проверка для исключения: {}", user.getChatId());
            Map<Partner, Boolean> result = checkSubscription(user.getChatId());
            boolean notActive = result != null;

            if (notActive) {
                log.warn("Исключаем пользователя: {}", user.getChatId());
                msg.processMessage(Messages.kickUserFromChat(user.getChatId(), appConfig.getBotPrivateChannel()));
                user.setKickUserFromChat(true);
                user.setLastSubscribeChecked(now + TimeUnit.DAYS.toMillis(100000));
                Sleep.sleepSafely(3000);
            } else {
                user.setActive(true); // Возвращаем в активные
                user.setLastSubscribeChecked(now);
            }

            repo.save(user);
            Sleep.sleepSafely(3000);
        }
    }

    private Map<Partner, Boolean> checkSubscription(Long chatId) {
        Map<Partner, Boolean> results = checkSubscribeToChannel.checkList(chatId, partnerList);
        return !results.containsValue(false) ? null : results;
    }

    private void updateUserStatus(User user, boolean isActive, long timestamp) {
        user.setActive(isActive);
        user.setLastSubscribeChecked(timestamp);
        repo.save(user);
    }
}