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
            String name = UpdateNameExtractor.extractFullName(update);
            String text = """
                    Привет, {name}!
                    Чтобы попасть в закрытый чат <b>«С GPT-на-Ты: Клуб Доверия к Нейросетям»</b>,
                    с пошаговыми инструкциями по установке ChatGPT, сначала подпишитесь на мой канал 
                    {link}. После подписки нажмите «Проверить подписку» и доступ откроется автоматически.
                    """
                    .replace("{name}", name)
                    .replace("{link}", LinkWrapper.wrapTextInLink("Mr.SuperNew", "https://t.me/+DBb3T3wGHd8xMzgy"));
            return TelegramData.getSendMessage(chatId, text, Keyboards.subscribe(results));
        } else {
            String text = """
                    Похоже, вы ещё не подписаны. Перейдите по ссылке, подпишитесь и попробуйте снова 🙂
                    """;
            return TelegramData.getEditMessage(chatId, text, Keyboards.subscribe(results), msgId);
        }
    }

    public static Object mainMenu(Long chatId, int msgId) {
        String text = "Главное меню";
        return msgId < 0 ? TelegramData.getSendMessage(chatId, text, Keyboards.mainKb())
                : TelegramData.getEditMessage(chatId, text, Keyboards.mainKb(), msgId);
    }

    public static Object myBolls(Long chatId, int msgId, Map<String, String> userData) {
        String text = """
                {l}
                
                Баланс: <b>{b}</b> баллов.
                Приглашённых напрямую: {l1}
                Приглашённых 2-го уровня: {l2}
                """
                .replace("{b}", userData.getOrDefault("b", "0"))
                .replace("{l}", userData.getOrDefault("l", "0"))
                .replace("{l1}", userData.getOrDefault("l1", "0"))
                .replace("{l2}", userData.getOrDefault("l2", "0"));
        return TelegramData.getEditMessage(chatId, text, Keyboards.mainKb(), msgId);
    }

    public static Object share(Long chatId, int msgId) {
        String link = "https://t.me/UstanovkaChatGPTbot?start=" + chatId;
        String text = """
                Твоя жизнь уже стала проще и эффективнее с нейросетями?

                Поделись этим с окружением и получай бонусы!
                • Отправь друзьям персональную ссылку ниже.
                • За каждого, кто запустит бота, подпишется на канал и нажмёт «Проверить подписку», начислим 10 баллов.
                • За каждого друга твоего друга — ещё 3 балла.
                • Накопи 100 баллов и получи 1-часовую личную консультацию по нейросетям.

                Баллы сохраняются, а призовой каталог будет расширяться. Делитесь ссылкой и собирайте их заранее! 🔗
                {link}
                """.replace("{link}", link);
        return TelegramData.getEditMessage(chatId, text, Keyboards.mainKb(), msgId);
    }

    public static Object spendBolls(Long chatId, int msgId, Map<String, String> m) {
        String text = """
                Доступные награды:
                • 1-часовая личная консультация — 100 баллов (reward 1) ⚡

                Каталог призов будет расширяться. Копите баллы, чтобы первыми получать новые возможности!
                """;
        long b = Long.parseLong(m.getOrDefault("b", "0"));
        return TelegramData.getEditMessage(chatId, text, b >= 100 ? Keyboards.award() : Keyboards.mainKb(), msgId);
    }

    public static Object newUser(Long chatId) {
        String text = "Новый подтверждённый пользователь";
        return TelegramData.getSendMessage(chatId, text, null);
    }

    public static Object overInviteLimitForAdmin(Long chatId) {
        String text = "⚠️ " + chatId + " превысил лимит приглашений на сегодня.";
        return TelegramData.getSendMessage(chatId, text, null);
    }

    public static Object overInviteLimitForUser(Long chatId) {
        String text = """
                ⚠️ Вы превысили лимит приглашений на сегодня.
                Баллы временно не начисляются. Если это ошибка — напишите админу.
                """;
        return TelegramData.getSendMessage(chatId, text, null);
    }

    public static Object requestAward(Long chatId, int msgId) {
        String text = """
                ✅ Заявка на «1-часовую консультацию» принята!
                Мы свяжемся с вами, чтобы выбрать удобное время.
                """;
        return TelegramData.getEditMessage(chatId, text, null, msgId);
    }

    public static Object uniqueLink(Long chatId) {
        String text = """
                Отлично! 🎉 Вот ваш пропуск в {link}
                Внутри ждут подробные инструкции и поддержка сообщества.
                Если возникнут вопросы — задавайте их в группе «Общий чат».
                """
                .replace("{link}", LinkWrapper.wrapTextInLink("закрытый клуб", "https://t.me/+R_7xy_8KZ244Y2Qx"));
        return TelegramData.getSendMessage(chatId, text, Keyboards.mainKb());
    }

    public static Object adminNotificationAward(Long chatId, Long userId, int msgId) {
        String text = """
                ✅ Получена заявка на «1-часовую консультацию»

                Пользователь: {uid}
                """.replace("{uid}", String.valueOf(userId));
        return TelegramData.getEditMessage(chatId, text, null, msgId);
    }

    public static Object cheers(Long ref, Map<String, String> m) {
        String text = """
                🎉 Поздравляем! Вы достигли уровня {level_name}.
                Продолжайте делиться — следующий ранг уже близко.
                """.replace("{level_name}", m.getOrDefault("l", ""));
        return TelegramData.getSendMessage(ref, text, null);
    }
}
