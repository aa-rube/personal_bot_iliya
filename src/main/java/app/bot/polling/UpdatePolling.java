package app.bot.polling;

import app.config.BotConfig;
import app.bot.handler.CallBackDataHandler;
import app.bot.handler.TextMsgHandler;
import app.bot.telegramdata.TelegramData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Controller
@RequiredArgsConstructor
public class UpdatePolling extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final TextMsgHandler textMsgHandler;
    private final CallBackDataHandler callBackDataHandler;

    @Override
    public String getBotUsername() {
        return botConfig.getUsername();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasCallbackQuery()) {
            callBackData(update);
        }

        else if (update.hasMessage() && update.getMessage().hasText()) {
            new Thread(() -> textMsgHandler.updateHandler(update)).start();
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