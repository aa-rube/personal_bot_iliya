package app.bot.handler;

import app.bot.api.CheckSubscribeToChannel;
import app.bot.api.MessagingService;
import app.bot.data.Messages;
import app.config.AppConfig;
import app.model.Activation;
import app.service.ActivationService;
import app.service.ReferralService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class CallBackDataHandler {

    private final AppConfig appConfig;
    private final MessagingService msg;
    private final ReferralService referralService;
    private final CheckSubscribeToChannel subscribe;
    private final ActivationService activationService;

    public CallBackDataHandler(@Lazy MessagingService msg,
                               AppConfig appConfig,
                               CheckSubscribeToChannel subscribe,
                               ReferralService referralService,
                               ActivationService activationService
    ) {
        this.appConfig = appConfig;
        this.msg = msg;
        this.subscribe = subscribe;
        this.referralService = referralService;
        this.activationService = activationService;
    }

    public void updateHandler(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String data = update.getCallbackQuery().getData();
        int msgId = update.getCallbackQuery().getMessage().getMessageId();

        log.info("msgId: {}, chatId: {}, data: {}", msgId, chatId, data);

        if (!data.equals("subscribe_chek")) {
            if (subscribe.hasNotSubscription(msg, update, chatId, msgId, false)) return;
        }

        switch (data) {
            case "subscribe_chek" -> {
                if (!subscribe.hasNotSubscription(msg, update, chatId, msgId, true)) {
                    activationService.save(new Activation(chatId, System.currentTimeMillis(), 0));
                    return;
                }
            }
            case "my_bolls" -> {
                Map<String, String> m = referralService.getUsrLevel(chatId);
                msg.processMessage(Messages.myBolls(chatId, msgId, m));
                return;
            }
            case "share" -> {
                msg.processMessage(Messages.share(chatId, msgId));
                activationService.save(new Activation(chatId, System.currentTimeMillis(), 1));
                return;
            }
            case "spend_bolls" -> {
                Map<String, String> m = new HashMap<>();
                msg.processMessage(Messages.spendBolls(chatId, msgId, m));
                return;
            }
            case "award" -> {
                msg.processMessage(Messages.requestAward(chatId, msgId));
                msg.processMessage(Messages.adminNotificationAward(appConfig.getLogChat(), chatId, msgId));
                return;
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
                    msg.processMessage(Messages.mainMenu(chatId, msgId));
                    return;
                }
            }
        }
    }
}