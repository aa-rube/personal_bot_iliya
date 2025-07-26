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
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

//    @Scheduled(fixedDelay = 600000) // 10 минут в миллисекундах
//    public void subscribeChecking() {
//        if (isRunning) {
//            log.warn("Проверка подписок уже выполняется, пропускаем");
//            return;
//        }
//
//        isRunning = true;
//        try {
//            log.info("Начинаем проверку подписок пользователей");
//            this.partnerList = this.partnerList.isEmpty() ? partners.findAll() : this.partnerList;
//            log.info("Загружено {} партнеров для проверки", partnerList.size());
//
//            List<User> users = userRepository.findAll();
//            log.info("Найдено {} пользователей для проверки (3 часа)", users.size());
//            users.forEach(user -> {
//                if (user.isActive() && !user.isKickUserFromChat()) {
//                    log.info("Проверяем пользователя: {}", user.getChatId());
//                    boolean hasSubscribe = !checkSubscribeToChannel.check(msg, user.getChatId(), partnerList).containsValue(false);
//                    if (!hasSubscribe) {
//                        log.warn("Пользователь {} неактивен, отправляем уведомление", user.getChatId());
//                        msg.processMessage(Messages.leftUser(user.getChatId()));
//                    }
//
//                    user.setActive(hasSubscribe);
//                    user.setLastSubscribeChecked(System.currentTimeMillis());
//                    userRepository.save(user);
//                    Sleep.sleepSafely(3000);
//                }
//            });
//
//
//            long timeAgo = System.currentTimeMillis() - (48 * 60 * 60 * 1000);
//            log.info("Найдено {} пользователей для исключения (48 часов)", users.size());
//            users.forEach(user -> {
//                if (!user.isActive() && user.getLastSubscribeChecked() < timeAgo) {
//
//                    log.info("Проверяем пользователя для исключения: {}", user.getChatId());
//                    boolean hasSubscribe = !checkSubscribeToChannel.check(msg, user.getChatId(), partnerList).containsValue(false);
//                    if (!hasSubscribe) {
//                        log.warn("Исключаем неактивного пользователя: {}", user.getChatId());
//                        msg.processMessage(Messages.kickUserFromChat(user.getChatId(), -1002317608626L));
//                    }
//
//                    user.setKickUserFromChat(true);
//                    user.setLastSubscribeChecked(System.currentTimeMillis() + (1825L * 60L * 60L * 1000L));
//                    userRepository.save(user);
//                    Sleep.sleepSafely(3000);
//                }
//            });
//
//            log.info("Проверка подписок завершена");
//        } finally {
//            isRunning = false;
//        }
//    }

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

            // Проверка активных пользователей (каждые 3 часа)
            processActiveUsers(now - TimeUnit.HOURS.toMillis(3), now);

            // Исключение неактивных (каждые 48 часов)
            processInactiveUsers(now - TimeUnit.DAYS.toMillis(2), now);

            log.info("Проверка подписок завершена");
        } finally {
            isRunning = false;
        }
    }

    private void processActiveUsers(long threeHoursAgo, long now) {
        List<User> activeUsers = userRepository.findActiveUsersForSubscriptionCheck(threeHoursAgo);
        log.info("Найдено {} активных пользователей для проверки", activeUsers.size());

        for (User user : activeUsers) {
            log.info("Проверяем пользователя: {}", user.getChatId());
            boolean isSubscribed = checkSubscription(user.getChatId());

            if (!isSubscribed) {
                log.warn("Пользователь {} неактивен", user.getChatId());
                msg.processMessage(Messages.leftUser(user.getChatId()));
            }

            updateUserStatus(user, isSubscribed, now);
            Sleep.sleepSafely(3000);
        }
    }

    private void processInactiveUsers(long fortyEightHoursAgo, long now) {
        List<User> inactiveUsers = userRepository.findInactiveUsersForKickCheck(fortyEightHoursAgo);
        log.info("Найдено {} неактивных пользователей для исключения", inactiveUsers.size());

        for (User user : inactiveUsers) {
            log.info("Проверка для исключения: {}", user.getChatId());
            boolean isSubscribed = checkSubscription(user.getChatId());

            if (!isSubscribed) {
                log.warn("Исключаем пользователя: {}", user.getChatId());
                msg.processMessage(Messages.kickUserFromChat(user.getChatId(), -1002317608626L));
                user.setKickUserFromChat(true);
                user.setLastSubscribeChecked(now + TimeUnit.DAYS.toMillis(76)); // 76 дней
            } else {
                user.setActive(true); // Возвращаем в активные
                user.setLastSubscribeChecked(now);
            }

            userRepository.save(user);
            Sleep.sleepSafely(3000);
        }
    }

    private boolean checkSubscription(Long chatId) {
        Map<Partner, Boolean> results = checkSubscribeToChannel.check(msg, chatId, partnerList);
        return !results.containsValue(false);
    }

    private void updateUserStatus(User user, boolean isActive, long timestamp) {
        user.setActive(isActive);
        user.setLastSubscribeChecked(timestamp);
        userRepository.save(user);
    }

}