package app.bot.handler;

import app.bot.api.CheckSubscribeToChannel;
import app.bot.api.MessagingService;
import app.data.Messages;
import app.config.AppConfig;
import app.model.Activation;
import app.model.Partner;
import app.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

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


    public CallBackDataHandler(@Lazy MessagingService msg,
                               AppConfig appConfig,
                               CheckSubscribeToChannel subscribe,
                               ReferralService referralService,
                               ActivationService activationService,
                               BuildAutoMessageService autoMessageService,
                               RequestService requestService,
                               StateManager stateManager
    ) {
        this.appConfig = appConfig;
        this.msg = msg;
        this.subscribe = subscribe;
        this.referralService = referralService;
        this.activationService = activationService;
        this.autoMessageService = autoMessageService;
        this.requestService = requestService;
        this.stateManager = stateManager;
    }

    public void updateHandler(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String data = update.getCallbackQuery().getData();
        int msgId = update.getCallbackQuery().getMessage().getMessageId();

        log.info("msgId: {}, chatId: {}, data: {}", msgId, chatId, data);

        stateManager.editWelcomeMessage.remove(chatId);

        if (!data.equals("subscribe_chek")) {
            if (subscribe.hasNotSubscription(update, chatId, msgId, false)) return;
        }

        switch (data) {
            case "subscribe_chek" -> {
                if (!subscribe.hasNotSubscription(update, chatId, msgId, true)) {
                    activationService.save(new Activation(chatId, System.currentTimeMillis(), 0));
                }
                return;
            }
            case "main_menu" -> {
                Map<String, String> m = referralService.getUsrLevel(chatId);
                boolean pc = subscribe.checkUserPartner(chatId, -1002317608626L);
                msg.processMessage(Messages.mainMenu(chatId, msgId, pc, m));
                return;
            }
            case "my_bolls" -> {
                Map<String, String> m = referralService.getUsrLevel(chatId);
                msg.processMessage(Messages.myBolls(chatId, msgId, m));
                return;
            }
            case "share", "share_" -> {
                int i = msg.processMessageReturnMsgId(Messages.share(chatId, -1));
                msg.processMessage(new PinChatMessage(String.valueOf(chatId), i));

                activationService.save(new Activation(chatId, System.currentTimeMillis(), 1));
                return;
            }
            case "spend_bolls" -> {
                Map<String, String> m = referralService.getUsrLevel(chatId);
                msg.processMessage(Messages.spendBolls(chatId, msgId, m));
                return;
            }
            case "my_bolls_" -> {
                Map<String, String> m = referralService.getUsrLevel(chatId);
                msg.processMessage(Messages.myBolls(chatId, -1, m));
                return;
            }
            case "spend_bolls_" -> {
                Map<String, String> m = referralService.getUsrLevel(chatId);
                msg.processMessage(Messages.spendBolls(chatId, -1, m));
                return;
            }
            case "award_yes" -> {
                msg.processMessage(Messages.requestAward(chatId, msgId));
                msg.processMessage(Messages.adminNotificationAward(appConfig.getLogChat(), chatId, msgId));
                requestService.save(chatId);
                return;
            }
            case "award_no" -> {
                Map<String, String> m = referralService.getUsrLevel(chatId);
                msg.processMessage(Messages.popAward(update.getCallbackQuery().getId(), m));
            }
            case "watch_welcome_msg" -> {
                if (chatId.equals(7833048230L) || chatId.equals(6134218314L)) {
                    Object o = autoMessageService.getAutoMsg(chatId, null, null);
                    o = o == null ? Messages.emptyWelcome(chatId) : o;
                    msg.processMessage(o);
                }

                return;
            }
            case "edit_welcome_msg" -> {
                if (chatId.equals(7833048230L) || chatId.equals(6134218314L)) {
                    msg.processMessage(new SendMessage(String.valueOf(chatId),
                            """
                                    Введите текст нового сообщение для приветствия.
                                    
                                    Можно использовать все типы форматирования телеграм кроме премиум emoji
                                    """
                    ));
                    stateManager.editWelcomeMessage.put(chatId, "edit_welcome_message");
                }
            }
        }

        if (data.startsWith("areYouOk?")) {
            String command = data.split("\\?")[1];
            switch (command) {
                case "yes" -> {
                    msg.processMessage(Messages.yes(chatId, msgId));
                    activationService.deleteByUserId(chatId);
                }

                case "help" -> {
                    msg.processMessage(Messages.userMsgHelp(chatId));
                }

                case "wait" -> {
                    Map<String, String> m = referralService.getUsrLevel(chatId);
                    boolean pc = subscribe.checkUserPartner(chatId, -1002317608626L);
                    msg.processMessage(Messages.mainMenu(chatId, msgId, pc, m));
                    return;
                }
            }
        }
    }
}
