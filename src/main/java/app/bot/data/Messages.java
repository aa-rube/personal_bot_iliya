package app.bot.data;

import app.bot.telegramdata.TelegramData;
import app.model.Partner;
import app.util.LinkWrapper;
import app.util.UpdateNameExtractor;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

public class Messages {

    public static Object subscribeMsg(Update update, Long chatId, int msgId, Map<Partner, Boolean> results) {


        if (msgId == -1) {
            String s = "Привет, " + UpdateNameExtractor.extractFullName(update) + "! Чтобы попасть в закрытый чат <b>«С GPT-на-Ты: Клуб Доверия к Нейросетям»</b>" +
                    " с пошаговыми инструкциями по установке ChatGPT, сначала подпишитесь на мой канал  " +
                    LinkWrapper.wrapTextInLink("Mr.SuperNew", "https://t.me/+DBb3T3wGHd8xMzgy") +
                    ". После подписки нажмите «Проверить подписку»  и доступ откроется автоматически.";
            return TelegramData.getSendMessage(chatId, s, Keyboards.subscribe(results));
        } else {
            String s = "Похоже, вы ещё не подписаны. Перейдите по ссылке, подпишитесь и попробуйте снова \uD83D\uDE42";
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
        String s = "Баланс: <b>" + userData.getOrDefault("b", "0") + "</b> баллов." +
                "\n\nУровень: " + userData.getOrDefault("l", "0") +
                "\nПриглашённых напрямую: " + userData.getOrDefault("l1", "0") +
                "\nПриглашённых 2-го уровня: " + userData.getOrDefault("l2", "0");
        return TelegramData.getEditMessage(chatId, s, Keyboards.mainKb(),msgId);
    }

    public static Object share(Long chatId, int msgId) {
        String s = "Твоя жизнь уже стала проще и эффективнее с нейросетями? \n" +
                "\n" +
                "Поделись этим с окружением  и получай бонусы!\n" +
                "• Отправь друзьям персональную ссылку ниже.\n" +
                "• За каждого, кто запустит бота, подпишется на канал и нажмёт «Проверить подписку», начислим 10 баллов.\n" +
                "• За каждого друга твоего друга — ещё 3 балла.\n" +
                "• Накопи 100 баллов и получи 1-часовую личную консультацию по нейросетям.\n" +
                "\n" +
                "Баллы сохраняются, а призовой каталог будет расширяться. Делитесь ссылкой и собирайте их заранее!\uD83D\uDD17 " +
                String.format("https://t.me/UstanovkaChatGPTbot?start=%d", chatId);
        return TelegramData.getEditMessage(chatId, s, Keyboards.mainKb(), msgId);
    }

    public static Object spendBolls(Long chatId, int msgId, Map<String, String> m) {
        String s = """
                Доступные награды:
                • 1-часовая личная консультация — 100 баллов (reward 1)⚡\s
                
                Каталог призов будет расширяться. Копите баллы, чтобы первыми получать новые возможности!""";

        long b = Long.parseLong(m.getOrDefault("b", "0"));
        return TelegramData.getEditMessage(chatId, s, b >= 100 ? Keyboards.award() : Keyboards.mainKb(), msgId);
    }

    public static Object newUser(Long chatId) {
        String s = "Новый подтверждённый пользователь";
        return TelegramData.getSendMessage(chatId, s, null);
    }

    public static Object overInviteLimitForAdmin(Long chatId) {
        String s = "⚠️ " + chatId + " превысил лимит приглашений на сегодня.";
        return TelegramData.getSendMessage(chatId, s, null);
    }

    public static Object overInviteLimitForUser(Long chatId) {
        String s = "⚠️ Вы превысили лимит приглашений на сегодня. Баллы временно не начисляются. Если это ошибка — напишите админу.";
        return TelegramData.getSendMessage(chatId, s, null);
    }

    public static Object requestAward(Long chatId, int msgId) {
        String s = "✅ Заявка на «1-часовую консультацию» принята! Мы свяжемся с вами, чтобы выбрать удобное время.";
        return TelegramData.getEditMessage(chatId, s, null, msgId);
    }

    public static Object uniqueLink(Long chatId) {
        String s = "Отлично! 🎉Вот ваш пропуск в " +
                LinkWrapper.wrapTextInLink("закрытый клуб", "https://t.me/+R_7xy_8KZ244Y2Qx") +
                "Внутри ждут подробные инструкции и поддержка сообщества. Если возникнут вопросы - задавайте их в группе 'Общий чат'.";
        return TelegramData.getSendMessage(chatId, s, Keyboards.mainKb());
    }

    public static Object adminNotificationAward(Long chatId, Long userId, int msgId) {
        String s = "✅ Получена заявка на «1-часовую консультацию»\n\nПользователь: " + userId + "\n\n";
        return TelegramData.getEditMessage(chatId, s, null, msgId);
    }


    public static Object cheers(Long ref, Map<String, String> m) {
        String s = "\uD83C\uDF89 Поздравляем! Вы достигли уровня {level_emoji} {level_name}. Продолжайте делиться — следующий ранг уже близко.";
        return TelegramData.getSendMessage(ref, s, null);
    }
}