package app.bot.handler;

import app.bot.api.MessagingService;
import app.data.Messages;
import app.config.AppConfig;
import app.bot.api.CheckSubscribeToChannel;
import app.service.*;
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
    private final BuildAutoMessageService autoMessageService;
    private final WelcomeMessageService welcome;
    private final StateManager stateManager;

    public TextMsgHandler(AppConfig appConfig,
                          CheckSubscribeToChannel subscribe,
                          UserService userService,
                          ReferralService referralService,
                          ActivationService activationService,
                          @Lazy MessagingService msg,
                          BuildAutoMessageService autoMessageService,
                          WelcomeMessageService welcome,
                          StateManager stateManager
    ) {
        this.msg = msg;
        this.appConfig = appConfig;
        this.subscribe = subscribe;
        this.userService = userService;
        this.referralService = referralService;
        this.activationService = activationService;
        this.autoMessageService = autoMessageService;
        this.welcome = welcome;
        this.stateManager = stateManager;
    }


    public void updateHandler(Update update) {
        String text = update.getMessage().getText() == null ? "" : update.getMessage().getText();
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
            try {
                if (!ue) {
                    Long ref = ExtractReferralIdFromStartCommand.extract(text);
                    int c = referralService.updateRefUserWithCount(chatId, ref);

                    userService.saveUser(update, chatId, ref);

                    if (c > 100) {
                        msg.processMessage(Messages.overInviteLimitForAdmin(appConfig.getLogChat()));
                        msg.processMessage(Messages.overInviteLimitForUser(ref));
                    } else {
                        msg.processMessage(Messages.newUser(update, appConfig.getLogChat(), ref, c));

                        Map<String, String> m = referralService.getUsrLevel(chatId);
                        long count = Long.parseLong(m.getOrDefault("b", "0"));
                        if (count != 0 && count % 10 == 0) {
                            msg.processMessage(Messages.cheers(ref, m));
                        }
                    }
                }
                return;
            } catch (Exception e) {
                log.error("new referral exception: {}", e.getMessage());
            }
        }

        if (text.equals("/admin")) {
            if (chatId.equals(7833048230L) || chatId.equals(6134218314L)) {
                msg.processMessage(Messages.adminPanel(chatId));
            }
            return;
        }

        if (stateManager.editWelcomeMessage.getOrDefault(chatId, "").equals("edit_welcome_message")) {
            autoMessageService.getOrAwaitScheduleMessage(chatId, update.getMessage());
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
        Object wm = autoMessageService.getAutoMsg(chatId, update, u);
        int welcomeMessageId = msg.processMessageReturnMsgId(wm);
        welcome.save(chatId, welcomeMessageId);
    }
}