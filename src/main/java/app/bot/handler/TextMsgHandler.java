package app.bot.handler;

import app.bot.api.MessagingService;
import app.bot.telegramdata.TelegramData;
import app.config.BotConfig;
import app.bot.api.CheckSubscribeToChannel;
import app.service.ReferralService;
import app.service.UserService;
import app.util.ExtractReferralIdFromStartCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;

@Slf4j
@Service
public class TextMsgHandler {

    private final BotConfig botConfig;
    private final CheckSubscribeToChannel subscribe;
    private final UserService userService;
    private final ReferralService referralService;
    private final MessagingService msg;

    public TextMsgHandler(BotConfig botConfig,
                          CheckSubscribeToChannel subscribe, UserService userService,
                          ReferralService referralService,
                          @Lazy MessagingService msg

    ) {
        this.botConfig = botConfig;
        this.subscribe = subscribe;
        this.userService = userService;
        this.referralService = referralService;
        this.msg = msg;
    }

    public void updateHandler(Update update) {
        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        int msgId = update.getMessage().getMessageId();

        log.info("msgId: {}, chatId: {}, text: {}, time: {}", msgId, chatId, text, LocalDateTime.now());
        if (chatId.equals(botConfig.getLogChat())) return;

        if (!subscribe.hasSubscription(msg, chatId, -1)) return;

        if (text.equals("/start")) {
            if (!userService.userDoesNotExists(chatId)) {
                userService.saveUser(update, chatId, 0L);
            }
        }

        if (text.contains("/start ")) {
            if (userService.userDoesNotExists(chatId)) {
                Long ref = ExtractReferralIdFromStartCommand.extract(text);
                referralService.updateRefUser(chatId, ref);
                userService.saveUser(update, chatId, ref);
            }
        }
        msg.processMessage(TelegramData.getSendMessage(
                chatId,
                "Главное меню",
                TelegramData.createInlineKeyboardColumn(
                        new String[]{"\uD83C\uDF81 Мои баллы", "\uD83D\uDC65 Пригласить друзей", "\uD83D\uDECD Потратить баллы"},
                        new String[]{"my_bolls", "share", "spend_bolls"}))
        );
    }

}