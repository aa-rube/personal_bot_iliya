package app.bot.handler;

import app.bot.api.CheckSubscribeToChannel;
import app.bot.api.DateRangePickerService;
import app.bot.api.MessagingService;
import app.bot.telegramdata.TelegramData;
import app.data.Messages;
import app.config.AppConfig;
import app.data.UserActionData;
import app.model.Activation;
import app.model.User;
import app.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class CallBackDataHandler {

    private final AppConfig appConfig;
    private final MessagingService msg;
    private final ReferralService referralService;
    private final CheckSubscribeToChannel subscribe;
    private final ActivationService activationService;
    private final BuildAutoMessageService autoMessageService;
    private final RequestService requestService;
    private final StateManager stateManager;
    private final UserService userService;
    private final UserActionService userActionService;
    private final ChannelReportService channelReportService;
    private final DateRangePickerService calendar;

    public CallBackDataHandler(@Lazy MessagingService msg,
                               AppConfig appConfig,
                               CheckSubscribeToChannel subscribe,
                               ReferralService referralService,
                               ActivationService activationService,
                               BuildAutoMessageService autoMessageService,
                               RequestService requestService,
                               StateManager stateManager,
                               UserService userService,
                               UserActionService userActionService,
                               ChannelReportService channelReportService,
                               DateRangePickerService calendar
    ) {
        this.appConfig = appConfig;
        this.msg = msg;
        this.subscribe = subscribe;
        this.referralService = referralService;
        this.activationService = activationService;
        this.autoMessageService = autoMessageService;
        this.requestService = requestService;
        this.stateManager = stateManager;
        this.userService = userService;
        this.userActionService = userActionService;
        this.channelReportService = channelReportService;
        this.calendar = calendar;
    }

    public void updateHandler(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String data = update.getCallbackQuery().getData();
        int msgId = update.getCallbackQuery().getMessage().getMessageId();

        log.info("msgId: {}, chatId: {}, data: {}", msgId, chatId, data);

        if (!data.startsWith("ft:")) {
            stateManager.remove(chatId);
        }

        if (!data.equals("subscribe_chek")) {
            if (subscribe.hasNotSubscription(update, chatId, msgId, false)) return;
        }

        switch (data) {
            case "subscribe_chek" -> {
                userActionService.addUserAction(chatId, UserActionData.INITIAL_SUBSCRIBE_CHECK);

                if (!subscribe.hasNotSubscription(update, chatId, msgId, true)) {
                    activationService.save(new Activation(chatId, Activation.Step.SECOND));
                    userActionService.addUserAction(chatId, UserActionData.CHECK_SUBSCRIBE_SUCCESS);
                }
                userActionService.addUserAction(chatId, UserActionData.CHECK_SUBSCRIBE_FAIL);
                return;
            }

            case "main_menu" -> {
                Map<String, String> m = referralService.getUsrLevel(chatId);
                boolean pc = subscribe.checkUserPartner(chatId, appConfig.getBotPrivateChannel());
                msg.process(Messages.mainMenu(chatId, msgId, pc, m));
                userActionService.addUserAction(chatId, UserActionData.OPEN_MAIN_MENU);
                return;
            }

            case "admin_menu" -> {
                msg.process(Messages.adminPanel(chatId, msgId));
                userActionService.addUserAction(chatId, UserActionData.OPEN_ADMIN_PANEL_SUCCESS_BY_CALLBACK);
                return;
            }

            case "my_bolls", "my_bolls_" -> {
                Map<String, String> m = referralService.getUsrLevel(chatId);
                msgId = data.contains("s_") ? -1 : msgId;
                msg.process(Messages.myBolls(chatId, msgId, m));
                userActionService.addUserAction(chatId, UserActionData.OPEN_MY_BOLLS);
                return;
            }

            case "share", "share_" -> {
                Map<String, String> m = referralService.getUsrLevel(chatId);

                int i = msg.processMessageReturnMsgId(Messages.share(chatId, m));
                msg.process(new PinChatMessage(String.valueOf(chatId), i));

                activationService.save(new Activation(chatId, Activation.Step.FIRST));
                userActionService.addUserAction(chatId, UserActionData.OPEN_SHARE);
                return;
            }

            case "spend_bolls", "spend_bolls_" -> {
                Map<String, String> m = referralService.getUsrLevel(chatId);
                msgId = data.contains("s_") ? -1 : msgId;
                msg.process(Messages.spendBolls(chatId, msgId, m));

                userActionService.addUserAction(chatId, UserActionData.OPEN_SPEND_BOLLS_MENU);
                return;
            }

            case "award_yes", "award_yes_" -> {
                msgId = data.contains("s_") ? -1 : msgId;
                msg.process(Messages.requestAward(chatId, msgId));

                msg.process(Messages.adminNotificationAward(appConfig.getLogChat(), chatId, msgId));
                requestService.save(chatId);

                userActionService.addUserAction(chatId, UserActionData.REQUEST_AWARD_YES);
                return;
            }

            case "award_no", "award_no_" -> {
                Map<String, String> m = referralService.getUsrLevel(chatId);
                msg.process(Messages.popAward(chatId,msgId, m));

                userActionService.addUserAction(chatId, UserActionData.REQUEST_AWARD_NO);
            }

            case "watch_welcome_msg" -> {
                int threadId = -1;
                Object o = autoMessageService.getAutoMsg(chatId, null, null, threadId);
                o = o == null ? Messages.emptyWelcome(chatId) : o;
                msg.process(o);

                userActionService.addUserAction(chatId, UserActionData.WATCHING_WELCOME_MESSAGE);
                return;
            }

            case "start_welcome_msg" -> {
                msg.process(Messages.startEditWelcomeMessage(chatId, msgId));

                userActionService.addUserAction(chatId, UserActionData.START_EDIT_WELCOME_MESSAGE);
                return;
            }

            case "edit_welcome_msg" -> {
                msg.process(Messages.inputNewTextForWelcomeMsg(chatId));
                stateManager.setStatus(chatId, "edit_welcome_message");
                userActionService.addUserAction(chatId, UserActionData.EDITING_WELCOME_MESSAGE);
                return;
            }

            case "start_utm" -> {
                msg.process(Messages.startEditUtm(chatId, msgId));

                userActionService.addUserAction(chatId, UserActionData.START_EDIT_UTM);
                return;
            }

            case "add_utm" -> {
                msg.process(Messages.addUtm(chatId, msgId));
                stateManager.setStatus(chatId, data);

                userActionService.addUserAction(chatId, UserActionData.ADDING_NEW_UTM_START);
                return;
            }

            case "list_utm" -> {
                String botUserName = appConfig.getUsername().split("@")[1];
                List<User> users = userService.findAllUtmUsers();

                msg.process(Messages.listUtm(chatId, botUserName, users));
                msg.process(Messages.startEditUtm(chatId, -1));

                userActionService.addUserAction(chatId, UserActionData.WATCHING_UTM_LIST);
                return;
            }

            case "reports" -> {
                msg.process(Messages.starReport(chatId, msgId));
                return;
            }

            case "sub_unsub" -> {
                stateManager.setStatus(chatId, data);
                calendar.start(chatId, msgId);
                return;
            }

            case "ft:ok" -> {
                Optional<Map<String, LocalDate>> ftOptional = calendar.handle(chatId, msgId, data);

                if (ftOptional.isPresent()) {
                    if (stateManager.statusIs(chatId, "sub_unsub")) {
                        Map<String, LocalDate> ftMap = ftOptional.get();

                        LocalDate f = ftMap.get("f");
                        LocalDate t = ftMap.get("t");
                        try {
                            String link = channelReportService.export(f, t);
                            msg.process(TelegramData.getEditMessage(chatId, link, null, msgId));

                        } catch (Exception e) {
                            log.error("Report {} create exception: {}", stateManager.getStatus(chatId), e.getMessage());
                        }
                        return;
                    }

                    if (stateManager.statusIs(chatId, "example")) {
                        return;
                    }
                }

                return;
            }
        }

        if (data.startsWith("areYouOk?")) {
            String command = data.split("\\?")[1];

            switch (command) {
                case "yes" -> {
                    msg.process(Messages.yes(chatId, msgId));
                    Activation a = activationService.getActivation(chatId);
                    a.setStep(1);

                    userActionService.addUserAction(chatId, UserActionData.ARE_YOU_OK_YES);
                }

                case "help" -> {
                    msg.process(Messages.userMsgHelp(chatId));
                    msg.process(Messages.adminMsgHelp(update, appConfig.getLogChat()));

                    userActionService.addUserAction(chatId, UserActionData.ARE_YOU_OK_HELP);
                }

                case "wait" -> {
                    Map<String, String> m = referralService.getUsrLevel(chatId);
                    boolean pc = subscribe.checkUserPartner(chatId, appConfig.getBotPrivateChannel());
                    msg.process(Messages.mainMenu(chatId, msgId, pc, m));

                    userActionService.addUserAction(chatId, UserActionData.ARE_YOU_OK_WAIT);
                }
            }
        }

        if (data.startsWith("ft:")) {
            calendar.handle(chatId, msgId, data);
        }
    }
}