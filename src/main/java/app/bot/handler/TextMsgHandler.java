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
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
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
    private final UtmVisitService utmVisitService;

    public TextMsgHandler(AppConfig appConfig,
                          CheckSubscribeToChannel subscribe,
                          UserService userService,
                          ReferralService referralService,
                          ActivationService activationService,
                          @Lazy MessagingService msg,
                          BuildAutoMessageService autoMessageService,
                          WelcomeMessageService welcome,
                          StateManager stateManager, UtmVisitService utmVisitService
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
        this.utmVisitService = utmVisitService;
    }


    public void updateHandler(Update update) {
        String text = update.getMessage().getText() == null ? "" : update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        int msgId = update.getMessage().getMessageId();

        log.info("msgId: {}, chatId: {}, text: {}", msgId, chatId, text);
        if (chatId.equals(appConfig.getLogChat())) return;

        if (text.equals("/trigger")) {
            userService.subscribeChecking();
            return;
        }
        if (text.equals("/trigger2")) {
            msg.processMessage(Messages.areYouOk(chatId));
            return;
        }

        boolean ue = userService.existsById(chatId);
        if (subscribe.hasNotSubscription(update, chatId, -1, false)) return;

        if (text.equals("/start")) {
            if (!ue) {
                userService.saveUser(update, chatId, 0L);
            }

            Map<String, String> m = referralService.getUsrLevel(chatId);
            boolean pc = subscribe.checkUserPartner(chatId, appConfig.getBotPrivateChannel());
            msg.processMessage(Messages.mainMenu(chatId, -1, pc, m));
            return;
        }

        if (text.contains("/start ")) {
            try {
                Long ref = ExtractReferralIdFromStartCommand.extract(text);
                if (ref < 1000 && ref > 0) {
                    utmVisitService.save(ref, chatId);
                    return;
                }

                if (!ue) {
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
                msg.processMessage(Messages.adminPanel(chatId, -1));
            }
            return;
        }

        if (update.hasMessage() && update.getMessage().hasContact()) {
            msg.processMessage(Messages.adminMsgHelp(update, appConfig.getLogChat()));
            msg.processMessage(new ForwardMessage(String.valueOf(appConfig.getLogChat()), String.valueOf(chatId), msgId));
            activationService.deleteByUserId(chatId);
        }

        if (stateManager.statusIs(chatId, "edit_welcome_message")) {
            autoMessageService.getOrAwaitScheduleMessage(chatId, update.getMessage());
            msg.processMessage(Messages.welcomeMessageSaved(chatId));
            return;
        }

        if (stateManager.statusIs(chatId, "add_utm")) {
            long newUtmId = userService.saveNewUtmUser(text);
            msg.processMessage(Messages.utmSaved(chatId, newUtmId));
            return;
        }
    }

    public void newMembers(Update update, List<User> newChatMembers) {
        Long chatId = update.getMessage().getChatId();
        int msgId = update.getMessage().getMessageId();

        User u = newChatMembers.getFirst();
        Object wm = autoMessageService.getAutoMsg(chatId, update, u);
        int welcomeMessageId = msg.processMessageReturnMsgId(wm);
        welcome.save(chatId, welcomeMessageId);
        msg.processMessage(new DeleteMessage(String.valueOf(chatId), msgId));
    }
}