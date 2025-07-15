package app.bot.handler;

import app.bot.api.CheckSubscribeToChannel;
import app.bot.api.MessagingService;
import app.bot.telegramdata.TelegramData;
import app.service.ReferralService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

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

        if (!subscribe.hasSubscription(msg, chatId, msgId)) return;

        if (data.equals("my_bolls")) {
            Map<String, String> m = referralService.getUsrLevel(chatId);
            msg.processMessage(TelegramData.getSendMessage(
                    chatId,
                    String.format("%s\n\nБаллы:%s", m.get("l"), m.get("b")),
                    TelegramData.createInlineKeyboardColumn(
                            new String[]{"\uD83C\uDF81 Мои баллы", "\uD83D\uDC65 Пригласить друзей", "\uD83D\uDECD Потратить баллы"},
                            new String[]{"my_bolls", "share", "spend_bolls"}))
            );

        }

    }

}