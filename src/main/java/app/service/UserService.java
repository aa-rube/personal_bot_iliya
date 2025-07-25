package app.service;

import app.bot.api.CheckSubscribeToChannel;
import app.bot.api.MessagingService;
import app.bot.data.Messages;
import app.model.Partner;
import app.model.User;
import app.repository.PartnersRepository;
import app.repository.UserRepository;
import app.util.Sleep;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final MessagingService msg;
    private final PartnersRepository partners;
    private final UserRepository userRepository;
    private final CheckSubscribeToChannel checkSubscribeToChannel;

    private List<Partner> partnerList = new ArrayList<>();

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
        this.partnerList = this.partnerList.isEmpty() ? partners.findAll() : this.partnerList;

        long timeAgo = System.currentTimeMillis() - (3 * 60 * 60 * 1000);
        List<User> users = findUsers(timeAgo);
        users.forEach(user -> {
            boolean isActive = !checkSubscribeToChannel.check(msg, user.getChatId(), partnerList).containsValue(false);
            if (!isActive) msg.processMessage(Messages.leftUser(user.getChatId()));

            user.setActive(isActive);
            user.setLastSubscribeChecked(System.currentTimeMillis());
            userRepository.save(user);
            Sleep.sleepSafely(3000);
        });



        timeAgo = System.currentTimeMillis() - (48 * 60 * 60 * 1000);
        users = findUsers(timeAgo);
        users.forEach(user -> {
            boolean isActive = !checkSubscribeToChannel.check(msg, user.getChatId(), partnerList).containsValue(false);
            if (!isActive) msg.processMessage(Messages.kickUserFromChat(user.getChatId(), -1002317608626L));

            user.setKickUserFromChat(true);
            user.setLastSubscribeChecked(System.currentTimeMillis() + (1825L * 60L * 60L * 1000L));
            userRepository.save(user);
            Sleep.sleepSafely(3000);
        });
    }

    private List<User> findUsers(long time) {
        return userRepository.findAllByIsKickUserFromChatAndIsActiveAndLastSubscribeCheckedLessThan(false, true, time);
    }
}