package app.bot.api;

import app.bot.polling.UpdatePolling;
import app.bot.telegramdata.TelegramData;
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
public class MessagingService {

    private final UpdatePolling updatePolling;

    @Autowired
    public MessagingService(UpdatePolling updatePolling) {
        this.updatePolling = updatePolling;
    }

    public void processMessage(Object msg) {
        try {
            if (msg instanceof SendMessage) {
                updatePolling.executeAsync((SendMessage) msg);
            } else if (msg instanceof SendPhoto) {
                updatePolling.executeAsync((SendPhoto) msg);
            } else if (msg instanceof EditMessageText) {
                updatePolling.executeAsync((EditMessageText) msg);
            } else if (msg instanceof DeleteMessage) {
                updatePolling.executeAsync((DeleteMessage) msg);
            } else if (msg instanceof EditMessageReplyMarkup) {
                updatePolling.executeAsync((EditMessageReplyMarkup) msg);
            } else if (msg instanceof SendMediaGroup) {
                updatePolling.execute((SendMediaGroup) msg);
            } else if (msg instanceof EditMessageCaption) {
                updatePolling.executeAsync((EditMessageCaption) msg);
            } else if (msg instanceof ForwardMessage) {
                updatePolling.executeAsync((ForwardMessage) msg);
            } else if (msg instanceof AnswerCallbackQuery) {
                updatePolling.executeAsync((AnswerCallbackQuery) msg);
            } else if (msg instanceof SendVideo) {
                updatePolling.executeAsync((SendVideo) msg);
            } else if (msg instanceof SendAudio) {
                updatePolling.executeAsync((SendAudio) msg);
            } else if (msg instanceof EditMessageMedia) {
                updatePolling.executeAsync((EditMessageMedia) msg);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public ChatMember getChatMember(GetChatMember member) {
        try {
            return updatePolling.execute(member);
        } catch (TelegramApiException telegramApiException) {
            return null;
        }
    }

    public ChatInviteLink createChatInviteLink(Long chatId, int memberLimit) {
        try {
            CreateChatInviteLink createChatInviteLink = new CreateChatInviteLink();
            createChatInviteLink.setChatId(chatId);
            createChatInviteLink.setMemberLimit(memberLimit);
            return updatePolling.execute(createChatInviteLink);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public int sendMsg(Object msg) {
        try {
            return updatePolling.execute((SendMessage) msg).getMessageId();
        } catch (TelegramApiException e) {
            return -1;
        }
    }

    public void handleJoinRequest(Long user, Long chat) {
        ApproveChatJoinRequest approveRequest = new ApproveChatJoinRequest();

        approveRequest.setUserId(user);
        approveRequest.setChatId(chat);

        try {
            updatePolling.execute(approveRequest);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }




    private static int findSplitIndex(String text, int chunkSize) {

        int newlineIndex = text.lastIndexOf("\n", chunkSize);
        if (newlineIndex != -1 && newlineIndex >= chunkSize * 0.7) {
            return newlineIndex + 1;
        }

        String sentenceEndings = ".!?;";
        for (int i = chunkSize - 1; i >= chunkSize * 0.8; i--) {
            if (sentenceEndings.indexOf(text.charAt(i)) != -1) {
                return i + 1;
            }
        }

        int spaceIndex = text.lastIndexOf(" ", chunkSize);
        if (spaceIndex != -1) {
            return spaceIndex + 1;
        }
        return chunkSize;
    }
}