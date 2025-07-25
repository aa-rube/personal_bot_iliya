package app.bot.handler;

import app.bot.api.MessagingService;
import app.bot.data.Messages;
import app.config.AppConfig;
import app.bot.api.CheckSubscribeToChannel;
import app.service.ActivationService;
import app.service.ReferralService;
import app.service.UserService;
import app.service.WelcomeMessageService;
import app.util.ExtractReferralIdFromStartCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TextMsgHandler {

    private final AppConfig appConfig;
    private final MessagingService msg;
    private final UserService userService;
    private final ReferralService referralService;
    private final CheckSubscribeToChannel subscribe;
    private final ActivationService activationService;
    private final WelcomeMessageService welcome;

    public TextMsgHandler(AppConfig appConfig,
                          CheckSubscribeToChannel subscribe,
                          UserService userService,
                          ReferralService referralService,
                          ActivationService activationService,
                          @Lazy MessagingService msg,
                          WelcomeMessageService welcome
    ) {
        this.msg = msg;
        this.appConfig = appConfig;
        this.subscribe = subscribe;
        this.userService = userService;
        this.referralService = referralService;
        this.activationService = activationService;
        this.welcome = welcome;
    }


    public void updateHandler(Update update) {
        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        int msgId = update.getMessage().getMessageId();

        log.info("msgId: {}, chatId: {}, text: {}", msgId, chatId, text);
        if (chatId.equals(appConfig.getLogChat())) return;

        boolean ue = userService.existsById(chatId);
        if (subscribe.hasNotSubscription(msg, update, chatId, -1, false)) return;

        if (text.equals("/start")) {
            if (!ue) {
                userService.saveUser(update, chatId, 0L);
            } else {
                msg.processMessage(Messages.mainMenu(chatId, -1));
            }
            return;
        }

        if (text.contains("/start ")) {

            if (!ue) {
                Long ref = ExtractReferralIdFromStartCommand.extract(text);
                int c = referralService.updateRefUserWithCount(chatId, ref);

                userService.saveUser(update, chatId, ref);

                if (c > 100) {
                    msg.processMessage(Messages.overInviteLimitForAdmin(appConfig.getLogChat()));
                    msg.processMessage(Messages.overInviteLimitForUser(ref));
                } else {
                    msg.processMessage(Messages.newUser(appConfig.getLogChat()));

                    Map<String, String> m = referralService.getUsrLevel(chatId);
                    long count = Long.parseLong(m.getOrDefault("b", "0"));
                    if (count != 0 && count % 10 == 0) {
                        msg.processMessage(Messages.cheers(ref, m));
                    }
                }
            }
            return;
        }

        if (update.hasMessage() && update.getMessage().hasContact()) {
            msg.processMessage(Messages.adminMsgHelp(update, appConfig.getLogChat()));
            msg.processMessage(new ForwardMessage(String.valueOf(appConfig.getLogChat()), String.valueOf(chatId), msgId));
            activationService.deleteByUserId(chatId);
        }
    }

    public void newMembers(Update update, List<User> newChatMembers) {
        Long chatId = update.getMessage().getChatId();
        User u = newChatMembers.getFirst();
        int welcomeMessageId = msg.processMessageReturnMsgId(Messages.welcomeMessage(update, u, chatId));
        welcome.save(chatId, welcomeMessageId);
    }
}