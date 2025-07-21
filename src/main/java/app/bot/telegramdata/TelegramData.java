package app.bot.telegramdata;

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TelegramData {
    public static Object getSendMessage(Long chatId, String text, InlineKeyboardMarkup markup) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);
        msg.enableHtml(true);
        msg.setParseMode(ParseMode.HTML);
        msg.setReplyMarkup(markup);
        return msg;
    }

    public static Object getSendPhoto(Long chatId, String text, InlineKeyboardMarkup markup, String fileId) {
        SendPhoto msg = new SendPhoto();
        msg.setChatId(chatId);
        msg.setCaption(text);
        msg.setParseMode(ParseMode.HTML);
        msg.setReplyMarkup(markup);
        msg.setPhoto(new InputFile(fileId));
        return msg;
    }

    public static Object getSendPhoto(Long chatId, String text, InlineKeyboardMarkup markup, java.io.File file) {
        SendPhoto msg = new SendPhoto();
        msg.setChatId(chatId);
        msg.setCaption(text);
        msg.setParseMode(ParseMode.HTML);
        msg.setReplyMarkup(markup);
        msg.setPhoto(new InputFile(file));
        return msg;
    }

    public static Object getSendSound(Long chatId, String caption, InlineKeyboardMarkup markup, InputFile file) {
        SendAudio msg = new SendAudio();
        msg.setChatId(chatId);
        msg.setCaption(caption);
        msg.setParseMode(ParseMode.HTML);
        msg.setReplyMarkup(markup);
        msg.setAudio(file);
        return msg;
    }


    public static Object getSendVideo(Long chatId, String caption, InlineKeyboardMarkup markup, InputFile file) {
        SendVideo msg = new SendVideo();
        msg.setChatId(chatId);
        msg.setCaption(caption);
        msg.setParseMode(ParseMode.HTML);
        msg.setReplyMarkup(markup);
        msg.setVideo(file);
        return msg;
    }

    public static Object getSendMediaGroupMsg(Long chatId, List<InputMedia> media) {
        SendMediaGroup msg = new SendMediaGroup();
        msg.setChatId(chatId);
        msg.setMedias(media);
        return msg;
    }

    public static Object getEditMessage(Long chatId, String text, InlineKeyboardMarkup markup, int msgId) {
        EditMessageText msg = new EditMessageText();
        msg.setChatId(chatId);
        msg.setMessageId(msgId);
        msg.setText(text);
        msg.enableHtml(true);
        msg.setParseMode(ParseMode.HTML);
        msg.setReplyMarkup(markup);
        return msg;
    }


    public static Object getDeleteMessage(Long chatId, int msgId) {
        DeleteMessage delete = new DeleteMessage();
        delete.setChatId(chatId);
        delete.setMessageId(msgId);
        return delete;
    }

    public static Object getEditMessageReplyMarkup(Long chatId, InlineKeyboardMarkup markup, int msgId, String inlineMsgId) {
        EditMessageReplyMarkup msg = new EditMessageReplyMarkup();

        msg.setChatId(chatId);
        msg.setMessageId(msgId);
        msg.setInlineMessageId(inlineMsgId);
        msg.setReplyMarkup(markup);
        return msg;
    }

    public static Object getPopupMessage(String callbackQueryId, String text, boolean alert) {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callbackQueryId);
        answer.setText(text);
        answer.setShowAlert(alert);
        return answer;
    }

    public static Object getEditMessageMedia(Long chatId, InputMedia media, InlineKeyboardMarkup markup, int msgId) {
        EditMessageMedia msg = new EditMessageMedia();
        msg.setChatId(chatId);
        msg.setMessageId(msgId);
        msg.setReplyMarkup(markup);
        msg.setMedia(media);
        return msg;
    }


    public static AnswerCallbackQuery getCallbackQueryAnswer(Update update) {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(update.getCallbackQuery().getId());
        return answer;
    }

    public static InlineKeyboardMarkup createInlineKeyboardLine(String[] buttonTexts, String[] callbackData) {
        InlineKeyboardMarkup inLineKeyBoard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardMatrix = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        for (int i = 0; i < buttonTexts.length; i++) {
            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText(buttonTexts[i]);
            btn.setCallbackData(callbackData[i]);
            row.add(btn);
        }

        keyboardMatrix.add(row);
        inLineKeyBoard.setKeyboard(keyboardMatrix);
        return inLineKeyBoard;
    }

    public static InlineKeyboardMarkup createInlineKeyboardColumn(String[] buttonTexts, String[] callbackData) {
        InlineKeyboardMarkup inLineKeyBoard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardMatrix = new ArrayList<>();

        for (int i = 0; i < buttonTexts.length; i++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText(buttonTexts[i]);

            String callBack = callbackData[i];

            if (callBack.contains("http")) {
                btn.setUrl(callBack);

            } else if(callBack.split(" ").length > 5) {
                btn.setSwitchInlineQueryCurrentChat(callBack);
            } else {
                btn.setCallbackData(callBack);
            }

            row.add(btn);
            keyboardMatrix.add(row);
        }

        inLineKeyBoard.setKeyboard(keyboardMatrix);
        return inLineKeyBoard;
    }

    public static InlineKeyboardMarkup createInlineKeyboardColumnThreeBtn(String[] buttonTexts, String[] callbackData) {
        InlineKeyboardMarkup inLineKeyBoard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardMatrix = new ArrayList<>();

        if (buttonTexts.length >= 2) {
            List<InlineKeyboardButton> firstRow = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                InlineKeyboardButton btn = new InlineKeyboardButton();
                btn.setText(buttonTexts[i]);
                btn.setCallbackData(callbackData[i]);
                firstRow.add(btn);
            }
            keyboardMatrix.add(firstRow);
        }

        if (buttonTexts.length > 2) {
            List<InlineKeyboardButton> secondRow = new ArrayList<>();
            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText(buttonTexts[2]);
            btn.setCallbackData(callbackData[2]);
            secondRow.add(btn);
            keyboardMatrix.add(secondRow);
        }

        inLineKeyBoard.setKeyboard(keyboardMatrix);
        return inLineKeyBoard;
    }
}
