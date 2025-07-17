package app.bot.data;

import app.bot.telegramdata.TelegramData;
import app.model.Partner;

import java.util.Map;

public class Messages {

    public static Object subscribeMsg(Long chatId, int msgId, Map<Partner, Boolean> results) {
        String s = "Подпишись на официальный канал";

        if (msgId == -1) {
            return TelegramData.getSendMessage(chatId, s, Keyboards.subscribe(results));
        } else {
            return TelegramData.getEditMessage(chatId, s, Keyboards.subscribe(results), msgId);
        }
    }

    public static Object mainMenu(Long chatId, int msgId) {
        String s = "Главное меню";
        if (msgId < 0) {
            return TelegramData.getSendMessage(chatId, s, Keyboards.mainKb());
        } else {
            return TelegramData.getEditMessage(chatId, s, Keyboards.mainKb(), msgId);
        }
    }

    public static Object myBolls(Long chatId, int msgId, Map<String, String> userData) {
        return TelegramData.getEditMessage(
                chatId, String.format("%s\n\nБаллы:%s", userData.get("l"), userData.get("b")), Keyboards.mainKb(), msgId);
    }

    public static Object share(Long chatId, int msgId) {
        return TelegramData.getEditMessage(
                chatId,
                String.format("Поделись этой ссылкой с друзьями, что бы заработать баллы: \n\nhttps://t.me/UstanovkaChatGPTbot?start=%d", chatId),
                Keyboards.mainKb(), msgId);
    }

    public static Object spendBolls(Long chatId, int msgId, Map<String, String> m) {
        String s = "⚡ Скоро появятся новые награды. Копите баллы, чтобы получить первые!";
        return TelegramData.getEditMessage(chatId, s, Keyboards.mainKb(), msgId);
    }

    public static Object newUser(Long chatId) {
        String s = "Новый подтверждённый пользователь";
        return TelegramData.getSendMessage(chatId, s, null);
    }

    public static Object overInviteLimit(Long chatId) {
        String s = "Превысил лимит приглашений";
        return TelegramData.getSendMessage(chatId, s, null);
    }

    public static Object requestAward(Long chatId) {
        String s = "Запросил награду";
        return TelegramData.getSendMessage(chatId, s, null);
    }

    public static Object uniqueLink(Long chatId, int msgId) {
        String s = "Ваша уникальная ссылка на закрытый чат <a href=\"https://t.me/+R_7xy_8KZ244Y2Qx\">тут</a>\n";
        return TelegramData.getEditMessage(chatId, s, Keyboards.mainKb(), msgId);
    }
}