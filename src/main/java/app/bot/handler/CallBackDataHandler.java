package app.bot.handler;

import app.bot.api.CheckSubscribeToChannel;
import app.bot.api.MessagingService;
import app.data.Messages;
import app.config.AppConfig;
import app.model.Activation;
import app.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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

    public CallBackDataHandler(@Lazy MessagingService msg,
                               AppConfig appConfig,
                               CheckSubscribeToChannel subscribe,
                               ReferralService referralService,
                               ActivationService activationService,
                               BuildAutoMessageService autoMessageService,
                               RequestService requestService,
                               StateManager stateManager, UserService userService
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
    }

    public void updateHandler(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String data = update.getCallbackQuery().getData();
        int msgId = update.getCallbackQuery().getMessage().getMessageId();

        log.info("msgId: {}, chatId: {}, data: {}", msgId, chatId, data);

        stateManager.remove(chatId);

        if (!data.equals("subscribe_chek")) {
            if (subscribe.hasNotSubscription(update, chatId, msgId, false)) return;
        }

        switch (data) {
            case "subscribe_chek" -> {
                if (!subscribe.hasNotSubscription(update, chatId, msgId, true)) {
                    activationService.save(new Activation(chatId, Activation.Step.SECOND));
                }
                return;
            }

            case "main_menu" -> {
                Map<String, String> m = referralService.getUsrLevel(chatId);
                boolean pc = subscribe.checkUserPartner(chatId, appConfig.getBotPrivateChannel());
                msg.process(Messages.mainMenu(chatId, msgId, pc, m));
                return;
            }

            case "admin_menu" -> {
                msg.process(Messages.adminPanel(chatId, msgId));
                return;
            }

            case "my_bolls", "my_bolls_" -> {
                Map<String, String> m = referralService.getUsrLevel(chatId);
                msgId = data.contains("s_") ? -1 : msgId;
                msg.process(Messages.myBolls(chatId, msgId, m));
                return;
            }

            case "share", "share_" -> {
                Map<String, String> m = referralService.getUsrLevel(chatId);

                int i = msg.processMessageReturnMsgId(Messages.share(chatId, -1, m));
                msg.process(new PinChatMessage(String.valueOf(chatId), i));

                activationService.save(new Activation(chatId, Activation.Step.FIRST));
                return;
            }

            case "spend_bolls", "spend_bolls_" -> {
                Map<String, String> m = referralService.getUsrLevel(chatId);
                msgId = data.contains("s_") ? -1 : msgId;
                msg.process(Messages.spendBolls(chatId, msgId, m));
                return;
            }

            case "award_yes", "award_yes_" -> {
                msgId = data.contains("s_") ? -1 : msgId;
                msg.process(Messages.requestAward(chatId, msgId));

                msg.process(Messages.adminNotificationAward(appConfig.getLogChat(), chatId, msgId));
                requestService.save(chatId);
                return;
            }

            case "award_no", "award_no_" -> {
                Map<String, String> m = referralService.getUsrLevel(chatId);
                msg.process(Messages.popAward(chatId,msgId, m));
            }

            case "watch_welcome_msg" -> {
                int threadId = -1;
                Object o = autoMessageService.getAutoMsg(chatId, null, null, threadId);
                o = o == null ? Messages.emptyWelcome(chatId) : o;
                msg.process(o);

                return;
            }

            case "start_welcome_msg" -> {
                msg.process(Messages.startEditWelcomeMessage(chatId, msgId));
            }

            case "edit_welcome_msg" -> {
                msg.process(Messages.inputNewTextForWelcomeMsg(chatId, msgId));
                stateManager.setStatus(chatId, "edit_welcome_message");
            }

            case "start_utm" -> {
                msg.process(Messages.startEditUtm(chatId, msgId));
                return;
            }

            case "add_utm" -> {
                msg.process(Messages.addUtm(chatId, msgId));
                stateManager.setStatus(chatId, data);
                return;
            }

            case "list_utm" -> {
                String bun = appConfig.getUsername().split("@")[1];
                AtomicInteger i = new AtomicInteger(1);

                StringBuffer b = new StringBuffer().append("Список UTM:\n");
                userService.findAllUtmUsers().forEach(u ->
                        b.append(i.getAndAdd(1)).append(". <code>")
                        .append("https://t.me/").append(bun).append("?start=")
                        .append(u.getChatId()).append("</code>, ").append(u.getFullName()).append("\n\n")
                );

                msg.process(Messages.listUtm(chatId, b));
                msg.process(Messages.startEditUtm(chatId, -1));
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
                }

                case "help" -> {
                    msg.process(Messages.userMsgHelp(chatId));
                    msg.process(Messages.adminMsgHelp(update, appConfig.getLogChat()));
                }

                case "wait" -> {
                    Map<String, String> m = referralService.getUsrLevel(chatId);
                    boolean pc = subscribe.checkUserPartner(chatId, appConfig.getBotPrivateChannel());
                    msg.process(Messages.mainMenu(chatId, msgId, pc, m));
                }
            }
        }

    }

}
