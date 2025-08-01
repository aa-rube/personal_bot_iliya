package app.bot.polling;

import app.config.AppConfig;
import app.bot.handler.CallBackDataHandler;
import app.bot.handler.TextMsgHandler;
import app.bot.telegramdata.TelegramData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
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
        log.debug("update: {}", update);

        if (update.hasMessage() && update.getMessage().hasContact()) {
            try {
                execute(new ForwardMessage(
                        String.valueOf(appConfig.getLogChat()),
                        String.valueOf(update.getMessage().getChatId()),
                        update.getMessage().getMessageId()));
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        if (update.hasMessage() && update.getMessage().getLeftChatMember() != null) {
            try {
                execute(new DeleteMessage(String.valueOf(update.getMessage().getChatId()),
                        update.getMessage().getMessageId()));
                return;
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }

        //ожидаем пользователя в приватном канале
        if (update.hasMessage()
                && update.getMessage().getNewChatMembers() != null
                && !update.getMessage().getNewChatMembers().isEmpty()) {
            try {
                new Thread(() -> textMsgHandler.newMembers(update, update.getMessage().getNewChatMembers())).start();
                return;
            } catch (Exception e) {
                log.error("New member: {}", e.getMessage());
            }
        }

        if (update.hasCallbackQuery()) {
            callBackData(update);
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            try {
                new Thread(() -> textMsgHandler.updateHandler(update)).start();
            } catch (Exception e) {
                log.error("Text: {}", e.getMessage());
            }
        }
    }

    private void callBackData(Update update) {
        try {
            new Thread(() -> callBackDataHandler.updateHandler(update)).start();

            if (!update.getCallbackQuery().getData().contains("award_no")) {

                new Thread(() -> {
                    try {
                        executeAsync(TelegramData.getCallbackQueryAnswer(update));
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }).start();

            }
        } catch (Exception e) {
            log.error("CallBackData: {}", e.getMessage());
        }
    }
}