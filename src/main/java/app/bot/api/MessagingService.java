package app.bot.api;

import app.bot.polling.UpdatePolling;
import app.bot.telegramdata.TelegramData;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.groupadministration.ApproveChatJoinRequest;
import org.telegram.telegrambots.meta.api.methods.groupadministration.CreateChatInviteLink;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.*;
import org.telegram.telegrambots.meta.api.objects.ChatInviteLink;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@RequiredArgsConstructor
public class MessagingService {

    private static final Logger log = LoggerFactory.getLogger(MessagingService.class);

    private final UpdatePolling updatePolling;

    public void processMessage(Object msg) {
        log.info("Обрабатываем сообщение типа: {}", msg.getClass().getSimpleName());
        try {
            switch (msg) {
                case SendMessage sendMessage -> updatePolling.executeAsync(sendMessage);
                case SendPhoto sendPhoto -> updatePolling.executeAsync(sendPhoto);
                case EditMessageText editMessageText -> updatePolling.executeAsync(editMessageText);
                case DeleteMessage deleteMessage -> updatePolling.executeAsync(deleteMessage);
                case EditMessageReplyMarkup editMessageReplyMarkup -> updatePolling.executeAsync(editMessageReplyMarkup);
                case SendMediaGroup sendMediaGroup -> updatePolling.execute(sendMediaGroup);
                case EditMessageCaption editMessageCaption -> updatePolling.executeAsync(editMessageCaption);
                case ForwardMessage forwardMessage -> updatePolling.executeAsync(forwardMessage);
                case AnswerCallbackQuery answerCallbackQuery -> updatePolling.executeAsync(answerCallbackQuery);
                case SendVideo sendVideo -> updatePolling.executeAsync(sendVideo);
                case SendAudio sendAudio -> updatePolling.executeAsync(sendAudio);
                case EditMessageMedia editMessageMedia -> updatePolling.executeAsync(editMessageMedia);
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
            return updatePolling.execute(member);
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
                    return updatePolling.executeAsync(sendMessage).get().getMessageId();
                }
                case SendPhoto sendPhoto -> {
                    return updatePolling.executeAsync(sendPhoto).get().getMessageId();
                }
                case ForwardMessage forwardMessage -> {
                    updatePolling.executeAsync(forwardMessage);
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