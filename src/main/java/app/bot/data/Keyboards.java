package app.bot.data;

import app.bot.telegramdata.TelegramData;
import app.model.Partner;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

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
        return TelegramData.createInlineKeyboardColumn(
                new String[]{"\uD83C\uDF81 Мои баллы", "\uD83D\uDC65 Пригласить друзей", "\uD83D\uDECD Потратить баллы"},
                new String[]{"my_bolls", "share", "spend_bolls"}
        );
    }

    public static InlineKeyboardMarkup award(long b) {
        return TelegramData.createInlineKeyboardColumn(
                new String[]{
                        "\uD83C\uDF81 Забрать награду {b}/100".replace("{b}", String.valueOf(b)),
                        "⏪ Назад"
                },
                new String[]{
                        "award_" + (b < 100 ? "no" : "yes"),
                        "main_menu"
                }
        );
    }

    public static InlineKeyboardMarkup areYouOk() {
        return TelegramData.createInlineKeyboardLine(
                new String[]{"✅ Всё работает\uD83E\uDD1D", "Нужна помощь", "⌛ Пока не приступал(а)"},
                new String[]{"areYouOk?yes", "areYouOk?help", "areYouOk?wait"}
        );
    }

    public static ReplyKeyboard contactShare() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();

        KeyboardButton contactButton = new KeyboardButton("Поделиться контактом");
        contactButton.setRequestContact(true);
        row1.add(contactButton);
        keyboard.add(row1);
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }
}
