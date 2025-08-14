package app.bot.handler;

import app.bot.api.MessagingService;
import app.data.Messages;
import app.config.AppConfig;
import app.bot.api.CheckSubscribeToChannel;
import app.data.UserActionData;
import app.service.*;
import app.util.ExtractReferralIdFromStartCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
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
    private final BuildAutoMessageService autoMessageService;
    private final WelcomeMessageService welcome;
    private final StateManager stateManager;
    private final UtmVisitService utmVisitService;
    private final UserActionService userActionService;

    public TextMsgHandler(AppConfig appConfig,
                          CheckSubscribeToChannel subscribe,
                          UserService userService,
                          ReferralService referralService,
                          @Lazy MessagingService msg,
                          BuildAutoMessageService autoMessageService,
                          WelcomeMessageService welcome,
                          StateManager stateManager,
                          UtmVisitService utmVisitService,
                          UserActionService userActionService
    ) {
        this.msg = msg;
        this.appConfig = appConfig;
        this.subscribe = subscribe;
        this.userService = userService;
        this.referralService = referralService;
        this.autoMessageService = autoMessageService;
        this.welcome = welcome;
        this.stateManager = stateManager;
        this.utmVisitService = utmVisitService;
        this.userActionService = userActionService;
    }


    public void updateHandler(Update update) {
        String text = update.getMessage().getText() == null ? "" : update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        int msgId = update.getMessage().getMessageId();

        if (chatId < 0) return;

        log.info("msgId: {}, chatId: {}, text: {}", msgId, chatId, text);
        if (chatId.equals(appConfig.getLogChat())) return;

        if (text.equals("/trigger")) {
            userService.subscribeChecking();
            return;
        }
        if (text.equals("/trigger2")) {
            msg.process(Messages.areYouOk(chatId));
            return;
        }

        boolean ue = userService.existsById(chatId);
        if (!text.startsWith("/start ") && !ue) {
            userService.saveUser(update, chatId, 0L);
            userActionService.addUserAction(chatId, UserActionData.USER_SAVED);
        }

        if (!text.contains("/start ")) {
            if (subscribe.hasNotSubscription(update, chatId, -1, false)) return;
        }

        if (text.equals("/start")) {
            Map<String, String> m = referralService.getUsrLevel(chatId);
            boolean pc = subscribe.checkUserPartner(chatId, appConfig.getBotPrivateChannel());
            msg.process(Messages.mainMenu(chatId, -1, pc, m));
            userActionService.addUserAction(chatId, UserActionData.USER_HAD_START_SUCCESS);
            return;
        }

        if (text.contains("/start ")) {
            userActionService.addUserAction(chatId, UserActionData.USER_HAD_REFERRAL_START);
            try {
                Long ref = ExtractReferralIdFromStartCommand.extract(text);
                if (ref < 1000 && ref >= 0) {
                    utmVisitService.save(ref, chatId);
                    subscribe.hasNotSubscription(update, chatId, -1, true);
                    return;
                }

                if (!ue) {
                    int c = referralService.updateRefUserWithCount(chatId, ref);

                    userService.saveUser(update, chatId, ref);
                    userActionService.addUserAction(chatId, UserActionData.USER_SAVED);

                    if (c > 100) {
                        msg.process(Messages.overInviteLimitForAdmin(appConfig.getLogChat()));
                        msg.process(Messages.overInviteLimitForUser(ref));
                    } else {
                        msg.process(Messages.newUser(update, appConfig.getLogChat(), ref, c));

                        Map<String, String> m = referralService.getUsrLevel(chatId);
                        long count = Long.parseLong(m.getOrDefault("b", "0"));
                        if (count != 0 && count % 10 == 0) {
                            msg.process(Messages.cheers(ref, m));
                        }
                    }

                    userActionService.addUserAction(chatId, UserActionData.THE_USER_GET_NEW_REFERRAL);
                }

                subscribe.hasNotSubscription(update, chatId, -1, false);
                return;
            } catch (Exception e) {
                log.error("new referral exception: {}", e.getMessage());
            }
        }

        if (text.equals("/admin")) {
            if (chatId.equals(7833048230L) || chatId.equals(6134218314L)) {
                msg.process(Messages.adminPanel(chatId, -1));
                userActionService.addUserAction(chatId, UserActionData.OPEN_ADMIN_PANEL_SUCCESS);
                return;
            }

            userActionService.addUserAction(chatId, UserActionData.OPEN_ADMIN_PANEL_FAIL);
            return;
        }

        if (stateManager.statusIs(chatId, "edit_welcome_message")) {
            autoMessageService.getOrAwaitScheduleMessage(chatId, update.getMessage());
            msg.process(Messages.welcomeMessageSaved(chatId));
            userActionService.addUserAction(chatId, UserActionData.SAVED_NEW_WELCOME_MESSAGE);
            return;
        }

        if (stateManager.statusIs(chatId, "add_utm")) {
            long newUtmId = userService.saveNewUtmUser(text);
            msg.process(Messages.utmSaved(chatId, newUtmId));

            userActionService.addUserAction(chatId, UserActionData.SAVE_NEW_UTM);
        }
    }

    public void newMembers(Update update, List<User> newChatMembers) {
        Long chatId = update.getMessage().getChatId();
        int msgId = update.getMessage().getMessageId();
        msg.process(new DeleteMessage(String.valueOf(chatId), msgId));

        User u = newChatMembers.getFirst();
        int threadId = 89;
        int welcomeMessageId = msg.processMessageReturnMsgId(autoMessageService.getAutoMsg(chatId, update, u, threadId));
        welcome.save(chatId, welcomeMessageId);

        userService.userNeedToBeChecked(chatId);
        userActionService.addUserAction(chatId, UserActionData.JOIN_PRIVATE_CHANNEL);
    }

    public void leftMember(Long chatId) {
        userService.userLeftPublicChannel(chatId);
        userActionService.addUserAction(chatId, UserActionData.LEFT_PUBLIC_CHANNEL);
    }
}