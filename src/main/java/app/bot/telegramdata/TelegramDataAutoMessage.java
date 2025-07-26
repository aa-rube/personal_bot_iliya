package app.bot.telegramdata;

import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import java.util.List;

public class TelegramDataAutoMessage {

    public static Object getSendMessage(Long chatId, String text, List<MessageEntity> entities) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);
        msg.setEntities(entities);
        msg.setReplyMarkup(null);
        msg.enableHtml(false);
        msg.setParseMode(null);
        return msg;
    }

    public static Object getSendVideo(Long chatId, String text, List<MessageEntity> entities, InlineKeyboardMarkup markup, String fileId) {
        SendVideo msg = new SendVideo();
        msg.setChatId(chatId);
        msg.setParseMode(null);
        msg.setCaptionEntities(entities);
        msg.setCaption(text);
        msg.setReplyMarkup(markup);
        msg.setVideo(new InputFile(fileId));
        return msg;
    }

    public static Object getSendVoice(Long chatId, String text, List<MessageEntity> entities, InlineKeyboardMarkup markup, String fileId) {
        SendVoice msg = new SendVoice();
        msg.setChatId(chatId);
        msg.setCaption(text);
        msg.setParseMode(null);
        msg.setCaptionEntities(entities);
        msg.setReplyMarkup(markup);
        msg.setVoice(new InputFile(fileId));
        return msg;
    }

    public static Object getSendVideoNote(Long chatId, InlineKeyboardMarkup markup, String fileId) {
        SendVideoNote msg = new SendVideoNote();
        msg.setChatId(chatId);
        msg.setReplyMarkup(markup);
        msg.setVideoNote(new InputFile(fileId));
        return msg;
    }

    public static Object getSendDocument(Long chatId, String text, List<MessageEntity> entities, InlineKeyboardMarkup markup, String fileId) {
        SendDocument msg = new SendDocument();
        msg.setChatId(chatId);
        msg.setCaption(text);
        msg.setParseMode(null);
        msg.setCaptionEntities(entities);
        msg.setReplyMarkup(markup);
        msg.setDocument(new InputFile(fileId));
        return msg;
    }

    public static Object getSendAnimation(Long chatId, String text, List<MessageEntity> entities, InlineKeyboardMarkup markup, String fileId) {
        SendAnimation msg = new SendAnimation();
        msg.setChatId(chatId);
        msg.setCaption(text);
        msg.setParseMode(null);
        msg.setCaptionEntities(entities);
        msg.setReplyMarkup(markup);
        msg.setAnimation(new InputFile(fileId));
        return msg;
    }

    public static Object getSendPhoto(Long chatId, String text, List<MessageEntity> entities, InlineKeyboardMarkup markup, String fileId) {
        SendPhoto msg = new SendPhoto();
        msg.setChatId(chatId);
        msg.setCaption(text);
        msg.setParseMode(null);
        msg.setCaptionEntities(entities);
        msg.setReplyMarkup(markup);
        msg.setPhoto(new InputFile(fileId));
        return msg;
    }

    public static Object getSendAudio(Long chatId, String text, List<MessageEntity> entities, InlineKeyboardMarkup markup, String fileId) {
        SendAudio msg = new SendAudio();
        msg.setChatId(chatId);
        msg.setCaption(text);
        msg.setParseMode(null);
        msg.setCaptionEntities(entities);
        msg.setReplyMarkup(markup);
        msg.setAudio(new InputFile(fileId));
        return msg;
    }
}