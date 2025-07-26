package app.bot.data;

import app.bot.telegramdata.TelegramData;
import app.model.Partner;
import app.util.LinkWrapper;
import app.util.UpdateNameExtractor;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

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
        return msgId < 0 ? TelegramData.getSendMessage(chatId, text, Keyboards.mainKb())
                : TelegramData.getEditMessage(chatId, text, Keyboards.mainKb(), msgId);
    }

    public static Object spendBolls(Long chatId, int msgId, Map<String, String> m) {
        String text = """
                Доступные награды:
                • 1-часовая личная консультация — 100 баллов ⚡
                
                Каталог призов будет расширяться. Копите баллы, чтобы первыми получать новые возможности!
                """;
        long b = Long.parseLong(m.getOrDefault("b", "0"));
        return TelegramData.getEditMessage(chatId, text, Keyboards.award(b), msgId);
    }

    public static Object newUser(Update update, Long chatId, Long ref, int count) {
        String text = """
        Новый приглашенный пользователь:
        userName: {un}
        chatId: {id}
        
        {rid} пригласил всего (за 24 часа) {count}
        """     .replace("{un}", UpdateNameExtractor.usernameAndFullName(update))
                .replace("{id}", String.valueOf(chatId))
                .replace("{rid}", String.valueOf(ref))
                .replace("{count}", String.valueOf(count));
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

    public static Object uniqueLink(Long chatId, int msgId) {
        String text = """
                Отлично! 🎉 Вот ваш пропуск в {link}
                Внутри ждут подробные инструкции и поддержка сообщества.
                Если возникнут вопросы — задавайте их в группе «Общий чат».
                """
                .replace("{link}", LinkWrapper.wrapTextInLink("закрытый клуб", "https://t.me/+R_7xy_8KZ244Y2Qx"));
        return msgId < 0 ? TelegramData.getSendMessage(chatId, text, Keyboards.mainKb())
                : TelegramData.getEditMessage(chatId, text, Keyboards.mainKb(), msgId);
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

    public static Object areYouOk(Long userId) {
        String s = "Получилось ли установить ChatGPT по нашим инструкциям?\n\nВыберите вариант:";
        return TelegramData.getSendMessage(userId, s, Keyboards.areYouOk());
    }

    public static Object yes(Long chatId, int msgId) {
        String s = "Спасибо за ваш ответ. Рады были помочь!";
        return TelegramData.getEditMessage(chatId, s, null, msgId);
    }

    public static Object userMsgHelp(Long chatId) {
        String s = "Можем связаться с вами в самое ближайшее время! Поделитесь Вашим контактом, пожалуйста!";
        return TelegramData.getSendMessage(chatId, s, Keyboards.contactShare());
    }

    public static Object adminMsgHelp(Update update, Long logChat) {
        String s = "Пользователь {uid} запросил помощи"
                .replace("{uid}", UpdateNameExtractor.usernameAndFullName(update))
                .replace("{cid}", String.valueOf(logChat));
        return TelegramData.getSendMessage(logChat, s, null);
    }

    public static Object welcomeMessage(Update update, User user, Long chatId) {
        String msg = """
                Привет! 👋 [username] Добро пожаловать в [title]!
                Здесь ты найдёшь всё, что нужно для комфортной работы с ChatGPT, установки приложения и оплаты подписки.
                ___
                🔗 Полезные темы:
                1. Введение — Как использовать ChatGPT для бизнеса, учебы и личных задач?
                2. Общий чат — Задавай вопросы и получай ответы
                3. Эфиры — Записи и анонсы эфиров
                4. Android: установка приложения
                5. iPhone: установка приложения
                6. Оплата ChatGPT
                ___
                Если у тебя возникнут вопросы, пиши в общий чат — здесь всегда помогут!
                Хорошего общения и продуктивного использования ChatGPT! 🚀
                
                P.S. это сообщение будет удалено через 5 мин.
                """
                .replaceAll("\\[username]", UpdateNameExtractor.userExtractName(user))
                .replaceAll("\\[title]", UpdateNameExtractor.extractGroupTitleName(update)
                );

        return TelegramData.getSendMessage(chatId, msg, null);
    }

    public static Object leftUser(Long chatId) {
        String s = """
                Я заметил, что вы отписались от telegram-канала Mr.SuperNew.
                
                Чтобы сохранить доступ к закрытому чату «C GPT на ТЫ», подпишитесь, пожалуйста, обратно и нажмите «Проверить подписку».
                
                Если вы не восстановите подписку нв течение 48 часов, доступ к закрытому чату по нейросетям будет приостановлен
                """;
        return new SendMessage(String.valueOf(chatId), s);
    }

    public static Object kickUserFromChat(Long chatId, Long userId) {
        BanChatMember banRequest = new BanChatMember();
        banRequest.setChatId(chatId.toString());
        banRequest.setUserId(userId);
        return banRequest;
    }

    public static Object popAward(String callBackQueryId) {
        String s = "У вас не достаточно баллов.";
        return TelegramData.getPopupMessage(callBackQueryId, s, false);
    }
}
