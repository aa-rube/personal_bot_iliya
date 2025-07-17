package app.data;

import app.config.AppConfig;
import app.bot.telegramdata.TelegramData;
import app.bot.api.MessagingService;
import app.util.ReferralCodeCipher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class ReferralMessage {

    private final MessagingService msgService;
    private final AppConfig appConfig;

    public ReferralMessage(@Lazy MessagingService msgService,
                           AppConfig appConfig) {
        this.msgService = msgService;
        this.appConfig = appConfig;
    }

    public void sendReferralMsg(Long chatId) {
        String invite = "Поделись этой ссылкой с другом:\n\n" + appConfig.getUsername() + "?start="
                + ReferralCodeCipher.encrypt(String.valueOf(chatId)) + "</code>";

        msgService.processMessage(TelegramData.getSendMessageHtmlParseMode(chatId, invite, null));
    }

    public void startMessage(Long chatId, boolean hasSubscribe) {
        msgService.processMessage(TelegramData.getSendMessage(
                chatId,
                "Рад приветствовать тебя мой друг! 3 дня премиум уже твои!"
                        + (hasSubscribe ? "" :"\n\n⬇️Подпишись, что бы активировать!⬇️"),
                null));
    }

    public void referralComplete(Long newUserId, String newUserName, Long oldUserId) {
        msgService.processMessage(TelegramData.getSendMessage(newUserId,
                "Поздравляю! 3 дня премиум подписки начислено на ваш аккаунт!", null));

        msgService.processMessage(TelegramData.getSendMessage(oldUserId,
                "Поздравляю! Ваш друг " + newUserName + " выполнил условия получения VIP статуса! Вы получаете 3 дня премиум!",
                null));
    }

    public void bonusMessage(Long chatId, long referredFriends) {
        String bonusText = String.format("Content.BONUS.getStr(%s)", referredFriends);
        msgService.processMessage(TelegramData.getSendMessage(
                chatId,
                bonusText,
                TelegramData.createInlineKeyboardColumn(
                        new String[]{"\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC67 Пригласи друга"},
                        new String[]{"REFERRAL"})
                ));
    }
}