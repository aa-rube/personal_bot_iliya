package app.bot.polling;

import app.config.AppConfig;
import app.bot.handler.CallBackDataHandler;
import app.bot.handler.TextMsgHandler;
import app.bot.telegramdata.TelegramData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UpdatePolling extends TelegramLongPollingBot {

    private final AppConfig appConfig;
    private final TextMsgHandler textMsgHandler;
    private final CallBackDataHandler callBackDataHandler;

    @Override
    public String getBotUsername() {
        return appConfig.getUsername();
    }

    @Override
    public String getBotToken() {
        return appConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info("update: {}", update);

        if (update.hasCallbackQuery()) {
            callBackData(update);
        }

        else if (update.hasMessage() && update.getMessage().hasText()) {
            new Thread(() -> textMsgHandler.updateHandler(update)).start();
        }

        if (update.hasMessage()
                && update.getMessage().getNewChatMembers() != null
                && !update.getMessage().getNewChatMembers().isEmpty()) {
            textMsgHandler.newMembers(update, update.getMessage().getNewChatMembers());
        }
    }

    private void callBackData(Update update) {
        new Thread(() -> callBackDataHandler.updateHandler(update)).start();

        new Thread(() -> {
            try {
                executeAsync(TelegramData.getCallbackQueryAnswer(update));
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}