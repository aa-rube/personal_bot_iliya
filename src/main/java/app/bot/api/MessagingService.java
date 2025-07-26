package app.bot.api;

import app.bot.polling.UpdatePolling;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.*;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@RequiredArgsConstructor
public class MessagingService {

    private static final Logger log = LoggerFactory.getLogger(MessagingService.class);

    private final UpdatePolling bot;

    public void processMessage(Object msg) {
        log.info("Обрабатываем сообщение типа: {}", msg.getClass().getSimpleName());
        try {
            switch (msg) {
                case SendMessage sendMessage -> bot.executeAsync(sendMessage);
                case SendPhoto sendPhoto -> bot.executeAsync(sendPhoto);
                case EditMessageText editMessageText -> bot.executeAsync(editMessageText);
                case DeleteMessage deleteMessage -> bot.executeAsync(deleteMessage);
                case EditMessageReplyMarkup editMessageReplyMarkup -> bot.executeAsync(editMessageReplyMarkup);
                case SendMediaGroup sendMediaGroup -> bot.execute(sendMediaGroup);
                case EditMessageCaption editMessageCaption -> bot.executeAsync(editMessageCaption);
                case ForwardMessage forwardMessage -> bot.executeAsync(forwardMessage);
                case AnswerCallbackQuery answerCallbackQuery -> bot.executeAsync(answerCallbackQuery);
                case SendVideo sendVideo -> bot.executeAsync(sendVideo);
                case SendAudio sendAudio -> bot.executeAsync(sendAudio);
                case EditMessageMedia editMessageMedia -> bot.executeAsync(editMessageMedia);
                case BanChatMember ban -> bot.executeAsync(ban);
                default -> {
                }
            }

        } catch (Exception exception) {
            log.error("Ошибка при обработке сообщения типа {}: {}", msg.getClass().getSimpleName(), exception.getMessage(), exception);
        }
    }

    public ChatMember getChatMember(GetChatMember member) {
        log.info("Получаем информацию о участнике чата: {}", member.getChatId());
        try {
            return bot.execute(member);
        } catch (TelegramApiException telegramApiException) {
            log.error("Ошибка при получении участника чата {}: {}", member.getChatId(), telegramApiException.getMessage());
            return null;
        }
    }

    public int processMessageReturnMsgId(Object msg) {
        log.info("Обрабатываем сообщение с возвратом ID типа: {}", msg.getClass().getSimpleName());
        try {
            switch (msg) {
                case SendMessage sendMessage -> {
                    return bot.executeAsync(sendMessage).get().getMessageId();
                }
                case SendPhoto sendPhoto -> {
                    return bot.executeAsync(sendPhoto).get().getMessageId();
                }
                case ForwardMessage forwardMessage -> {
                    bot.executeAsync(forwardMessage);
                    return forwardMessage.getMessageId();
                }
                default -> {
                }
            }

        } catch (Exception exception) {
            log.error("Ошибка при обработке сообщения с возвратом ID типа {}: {}", msg.getClass().getSimpleName(), exception.getMessage(), exception);
        }
        return -1;
    }
}