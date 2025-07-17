package app.bot.handler;

import app.bot.api.CheckSubscribeToChannel;
import app.bot.api.MessagingService;
import app.bot.data.Messages;
import app.service.ReferralService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class CallBackDataHandler {

    private final MessagingService msg;
    private final CheckSubscribeToChannel subscribe;
    private final ReferralService referralService;

    public CallBackDataHandler(@Lazy MessagingService msg,
                               CheckSubscribeToChannel subscribe,
                               ReferralService referralService
    ) {
        this.msg = msg;
        this.subscribe = subscribe;
        this.referralService = referralService;
    }

    public void updateHandler(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String data = update.getCallbackQuery().getData();
        int msgId = update.getCallbackQuery().getMessage().getMessageId();

        log.info("msgId: {}, chatId: {}, data: {}", msgId, chatId, data);

        if (!data.equals("subscribe_chek")) {
            if (subscribe.hasNotSubscription(msg, chatId, msgId, false)) return;
        }

        switch (data) {
            case "subscribe_chek" -> {
                if (subscribe.hasNotSubscription(msg, chatId, msgId, true)) return;
            }
            case "my_bolls" -> {
                Map<String, String> m = referralService.getUsrLevel(chatId);
                msg.processMessage(Messages.myBolls(chatId, msgId, m));
            }
            case "share" -> {
                msg.processMessage(Messages.share(chatId, msgId));
            }
            case "spend_bolls" -> {
                Map<String, String> m = new HashMap<>();//referralService.getUsrLevel(chatId);
                msg.processMessage(Messages.spendBolls(chatId, msgId, m));
            }
        }
    }
}