package app.bot.data;

import app.bot.telegramdata.TelegramData;
import app.model.Partner;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Keyboards {

    public static InlineKeyboardMarkup subscribe(Map<Partner, Boolean> results) {
        List<String> t = new ArrayList<>();
        List<String> d = new ArrayList<>();

        for (Map.Entry<Partner, Boolean> entry : results.entrySet()) {
            if (!entry.getValue()) {
                t.add("Подписаться - ".concat(entry.getKey().getName()));
                d.add(entry.getKey().getInviteLink());
            }
        }

        t.add("✅ Проверить подписку");
        d.add("subscribe_chek");

        return TelegramData.createInlineKeyboardColumn(
                t.toArray(new String[0]), d.toArray(new String[0])
        );
    }

    public static InlineKeyboardMarkup mainKb() {
        return TelegramData.createInlineKeyboardLine(
                new String[]{"\uD83C\uDF81 Мои баллы", "\uD83D\uDC65 Пригласить друзей", "\uD83D\uDECD Потратить баллы"},
                new String[]{"my_bolls", "share", "spend_bolls"}
        );
    }
}
