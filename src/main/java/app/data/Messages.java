package app.data;


import app.bot.telegramdata.TelegramData;
import app.model.Partner;
import app.util.LinkWrapper;
import app.util.UpdateNameExtractor;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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

    public static Object mainMenu(Long chatId, int msgId, boolean pc, Map<String, String> m) {
        long tc = Long.parseLong(m.getOrDefault("tc", "0"));
        long b = Long.parseLong(m.getOrDefault("b", "0"));

        String text = """
                {header}
                
                • 🎁 Мои баллы — узнать текущий баланс и уровень.
                
                • 👥 Пригласить друзей — получить персональную ссылку и заработать баллы.
                
                • 🛍 Потратить баллы — увидеть доступные призы.
                
                • 📅 Бесплатная консультация {b}/100 — оставить заявку, когда накопится 100 баллов.
                
                • 💬 Платная консультация — сразу написать автору.
                """
                .replace("{b}", String.valueOf(b));

        if (tc > 0 && pc) {
            text = text.replace("{header}",
                    "Вы уже в закрытом чате «C GPT на ТЫ» и участвуете в реферальной программе.");
        }

        if (pc && tc <= 0) {
            text = text.replace("{header}",
                    "Вы уже в закрытом чате «C GPT на ТЫ». Приглашайте друзей в наш чат!");
        }

        if (!pc && tc <= 0) {
            text = text.replace("{header}",
                    "Вступайте в закрытый чат "
                            + LinkWrapper.wrapTextInLink("«C GPT на ТЫ»", "https://t.me/+R_7xy_8KZ244Y2Qx")
                            + ", приглашайте друзей - получайте максимум возможностей!");
        }

        return msgId < 0 ? TelegramData.getSendMessage(chatId, text, Keyboards.mainKb(b)) :
                TelegramData.getEditMessage(chatId, text, Keyboards.mainKb(b), msgId);
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
        long b = Long.parseLong(userData.getOrDefault("b", "0"));
        return msgId < 0 ? TelegramData.getSendMessage(chatId, text, Keyboards.mainKb(b))
                : TelegramData.getEditMessage(chatId, text, Keyboards.mainKb(b), msgId);
    }

    public static Object share(Long chatId, int msgId, Map<String, String> m) {
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
        long b = Long.parseLong(m.getOrDefault("b", "0"));
        return TelegramData.getSendMessage(chatId, text, Keyboards.mainKbNewMessage(b));
    }

    public static Object spendBolls(Long chatId, int msgId, Map<String, String> m) {
        String text = """
                Доступные награды:
                • 1-часовая личная консультация — 100 баллов ⚡
                
                Каталог призов будет расширяться. Копите баллы, чтобы первыми получать новые возможности!
                """;
        long b = Long.parseLong(m.getOrDefault("b", "0"));
        return msgId < 0 ? TelegramData.getSendMessage(chatId, text, Keyboards.award(b)) :
                TelegramData.getEditMessage(chatId, text, Keyboards.award(b), msgId);
    }

    public static Object newUser(Update update, Long chatId, Long ref, int count) {
        String text = """
                Новый приглашенный пользователь:
                userName: {un}
                chatId: {id}
                
                Пользователь пригласил всего (за 24 часа) = {count} друзей
                """.replace("{un}", UpdateNameExtractor.usernameAndFullName(update))
                .replace("{id}", String.valueOf(ref))
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
        return msgId < 0 ? TelegramData.getSendMessage(chatId, text, null) :
                TelegramData.getEditMessage(chatId, text, null, msgId);
    }

    public static Object uniqueLink(Long chatId, int msgId, Map<String, String> m) {
        String text = """
                Отлично! 🎉 Вот ваш пропуск в {link}
                Внутри ждут подробные инструкции и поддержка сообщества.
                Если возникнут вопросы — задавайте их в группе «Общий чат».
                """
                .replace("{link}", LinkWrapper.wrapTextInLink("закрытый клуб", "https://t.me/+R_7xy_8KZ244Y2Qx"));
        long b = Long.parseLong(m.getOrDefault("b", "0"));
        return msgId < 0 ? TelegramData.getSendMessage(chatId, text, Keyboards.mainKb(b))
                : TelegramData.getEditMessage(chatId, text, Keyboards.mainKb(b), msgId);
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

    public static Object leftUser(Long chatId, Map<Partner, Boolean> partners) {
        String t = """
                ⚠️⚠️⚠️
                Я заметил, что вы отписались от telegram-канала Mr.SuperNew.
                
                Чтобы сохранить доступ к закрытому чату «C GPT на ТЫ», подпишитесь, пожалуйста, обратно и нажмите «Проверить подписку».
                
                Если вы не восстановите подписку нв течение 48 часов, доступ к закрытому чату по нейросетям будет приостановлен
                """;
        return TelegramData.getSendMessage(chatId, t, Keyboards.subscribe(partners));
    }

    public static Object kickUserFromChat(Long chatId, Long userId) {
        BanChatMember banRequest = new BanChatMember();
        banRequest.setChatId(chatId.toString());
        banRequest.setUserId(userId);
        return banRequest;
    }

    public static Object popAward(Long chatId, int msgId, Map<String, String> m) {
        String bb = m.getOrDefault("b", "0");
        String s = """
                Для бесплатной консультации нужно 100 баллов, у вас сейчас {b}.
                
                Можно продолжить приглашать друзей или выбрать платную консультацию по ссылке ниже
                """
                .replace("{b}", bb);
        return TelegramData.getEditMessage(chatId, s, Keyboards.mainKb(Long.parseLong(bb)), msgId);
    }

    public static Object emptyWelcome(Long chatId) {
        return new SendMessage(String.valueOf(chatId), "Еще не задано ни одного сообщения");
    }


    //admins message
    public static Object adminPanel(Long chatId, int msgId) {
        String s = "Меню администратора:";
        return msgId < 0 ? TelegramData.getSendMessage(chatId, s, Keyboards.adminPanel())
                : TelegramData.getEditMessage(chatId, s, Keyboards.adminPanel(), msgId);
    }

    public static Object startEditWelcomeMessage(Long chatId, int msgId) {
        String s = "Редактирование приветственного сообщения:";
        return TelegramData.getEditMessage(chatId, s, Keyboards.editWelcomeMessage(), msgId);
    }

    public static Object welcomeMessageSaved(Long chatId) {
        return TelegramData.getSendMessage(chatId, "Сообщение сохранено!", Keyboards.welcomeMessageSaved());
    }

    public static Object inputNewTextForWelcomeMsg(Long chatId, int msId) {
        return TelegramData.getSendMessage(chatId,
                "Введите текст нового сообщение для приветствия. \n\nМожно использовать все типы форматирования телеграм кроме премиум emoji",
                Keyboards.cancelInputNewWelcomeText());
    }

    public static Object startEditUtm(Long chatId, int msgId) {
        return msgId < 0 ?
                TelegramData.getSendMessage(chatId, "Редактирование UTM-меток", Keyboards.startEditUtm()) :
                TelegramData.getEditMessage(chatId, "Редактирование UTM-меток", Keyboards.startEditUtm(), msgId);
    }

    public static Object addUtm(Long chatId, int msgId) {
        return TelegramData.getEditMessage(chatId, "Введите описание для новой utm-метки",
                Keyboards.cancelAddNewUtm(), msgId);
    }

    public static Object utmSaved(Long chatId, long newId) {
        return TelegramData.getSendMessage(chatId,
                String.format("Новая utm %d сохранена!", newId),
                Keyboards.utmSaved());
    }

    public static Object listUtm(Long chatId, StringBuffer b) {
        return TelegramData.getSendMessage(chatId,
                b.toString(),
                null);
    }
}