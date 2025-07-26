package app.service;

import app.bot.api.CheckSubscribeToChannel;
import app.bot.api.MessagingService;
import app.bot.data.Messages;
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

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final MessagingService msg;
    private final PartnersRepository partners;
    private final UserRepository userRepository;
    private final CheckSubscribeToChannel checkSubscribeToChannel;

    private List<Partner> partnerList = new ArrayList<>();
    private volatile boolean isRunning = false;

    public UserService(PartnersRepository partners,
                       @Lazy MessagingService msg,
                       UserRepository userRepository,
                       CheckSubscribeToChannel checkSubscribeToChannel) {
        this.partners = partners;
        this.msg = msg;
        this.userRepository = userRepository;
        this.checkSubscribeToChannel = checkSubscribeToChannel;
    }

    public void saveUser(Update update, Long chatId, Long ref) {
        userRepository.save(new User(update, chatId, ref));
    }

    public boolean existsById(Long chatId) {
        return userRepository.existsById(chatId);
    }

    @Scheduled(fixedDelay = 600000) // 10 минут в миллисекундах
    public void subscribeChecking() {
        if (isRunning) {
            log.warn("Проверка подписок уже выполняется, пропускаем");
            return;
        }

        isRunning = true;
        try {
            log.info("Начинаем проверку подписок пользователей");
            this.partnerList = this.partnerList.isEmpty() ? partners.findAll() : this.partnerList;
            log.info("Загружено {} партнеров для проверки", partnerList.size());

            long timeAgo = System.currentTimeMillis() - (3 * 60 * 60 * 1000);
            List<User> users = findUsers(timeAgo);

            log.info("Найдено {} пользователей для проверки (3 часа)", users.size());
            users.forEach(user -> {
                log.info("Проверяем пользователя: {}", user.getChatId());
                boolean hasSubscribe = !checkSubscribeToChannel.check(msg, user.getChatId(), partnerList).containsValue(false);
                if (!hasSubscribe) {
                    log.warn("Пользователь {} неактивен, отправляем уведомление", user.getChatId());
                    msg.processMessage(Messages.leftUser(user.getChatId()));
                }

                user.setActive(hasSubscribe);
                user.setLastSubscribeChecked(System.currentTimeMillis());
                userRepository.save(user);
                Sleep.sleepSafely(3000);
            });


            timeAgo = System.currentTimeMillis() - (48 * 60 * 60 * 1000);
            users = findUsers(timeAgo);
            log.info("Найдено {} пользователей для исключения (48 часов)", users.size());
            users.forEach(user -> {
                log.info("Проверяем пользователя для исключения: {}", user.getChatId());
                boolean hasSubscribe = !checkSubscribeToChannel.check(msg, user.getChatId(), partnerList).containsValue(false);
                if (!hasSubscribe) {
                    log.warn("Исключаем неактивного пользователя: {}", user.getChatId());
                    msg.processMessage(Messages.kickUserFromChat(user.getChatId(), -1002317608626L));
                }

                user.setKickUserFromChat(true);
                user.setLastSubscribeChecked(System.currentTimeMillis() + (1825L * 60L * 60L * 1000L));
                userRepository.save(user);
                Sleep.sleepSafely(3000);
            });

            log.info("Проверка подписок завершена");
        } finally {
            isRunning = false;
        }
    }

    private List<User> findUsers(long time) {
        return userRepository.findAllByKickActiveBefore(false, true, time);
    }
}