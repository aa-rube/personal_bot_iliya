package app.bot.handler;

import app.bot.api.CheckSubscribeToChannel;
import app.bot.api.MessagingService;
import app.bot.data.Messages;
import app.config.AppConfig;
import app.service.ReferralService;
import app.service.UserService;
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
    private final UserService userService;

    public CallBackDataHandler(@Lazy MessagingService msg,
                               CheckSubscribeToChannel subscribe,
                               ReferralService referralService,
                               UserService userService
    ) {
        this.msg = msg;
        this.subscribe = subscribe;
        this.referralService = referralService;
        this.userService = userService;
    }

    public void updateHandler(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String data = update.getCallbackQuery().getData();
        int msgId = update.getCallbackQuery().getMessage().getMessageId();

        boolean ue = userService.existsById(chatId);
        if (subscribe.hasNotSubscription(msg, chatId, msgId, ue)) return;

        switch (data) {
            case "my_bolls" -> {
                Map<String, String> m = referralService.getUsrLevel(chatId);
                msg.processMessage(Messages.myBolls(chatId, msgId, m));
                return;
            }
            case "share" -> {
                msg.processMessage(Messages.share(chatId, msgId));
                return;
            }
            case "spend_bolls" -> {
                Map<String, String> m = new HashMap<>();//referralService.getUsrLevel(chatId);

                msg.processMessage(Messages.spendBolls(chatId, msgId, m));
                return;
            }
        }

    }

}