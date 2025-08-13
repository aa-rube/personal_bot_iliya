package app.data;

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

    public static InlineKeyboardMarkup mainKb(long b, boolean invitePrivateChat) {
        return TelegramData.createInlineKeyboardColumn(
                List.of(
                        (invitePrivateChat ? "Вступайте в закрытый чат ⚡" : "skip"),
                        "\uD83C\uDF81 Мои баллы",
                        "\uD83D\uDC65 Пригласить друзей",
                        "\uD83D\uDECD Потратить баллы",
                        "\uD83D\uDCC5 Бесплатная {b}/100".replace("{b}", String.valueOf(b)),
                        "\uD83D\uDCAC Платная консультация"
                ),
                List.of(
                        (invitePrivateChat ? "https://t.me/+R_7xy_8KZ244Y2Qx" : "skip"),
                        "my_bolls",
                        "share",
                        "spend_bolls",
                        "award_" + (b < 100 ? "no" : "yes"),
                        "https://t.me/MoneyBaires"
                )
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
        return TelegramData.createInlineKeyboardColumn(
                new String[]{
                        "✅ Всё работает\uD83E\uDD1D",
                        "Нужна помощь",
                        "⌛ Пока не приступал(а)"
                },
                new String[]{
                        "areYouOk?yes",
                        "areYouOk?help",
                        "areYouOk?wait"
                }
        );
    }

    public static ReplyKeyboard contactShare() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();

        KeyboardButton contactButton = new KeyboardButton("Личная консультация");
        contactButton.setRequestContact(true);
        row1.add(contactButton);
        keyboard.add(row1);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        return keyboardMarkup;
    }

    public static InlineKeyboardMarkup mainKbNewMessage(Long b) {
        return TelegramData.createInlineKeyboardColumn(
                new String[]{
                        "\uD83C\uDF81 Мои баллы",
                        "\uD83D\uDC65 Пригласить друзей",
                        "\uD83D\uDECD Потратить баллы",
                        "\uD83D\uDCC5 Бесплатная {b}/100".replace("{b}", String.valueOf(b)),
                        "\uD83D\uDCAC Платная консультация"
                },
                new String[]{
                        "my_bolls_",
                        "share_",
                        "spend_bolls_",
                        "award_" + (b < 100 ? "no_" : "yes_"),
                        "https://t.me/MoneyBaires"
                }
        );
    }


    //admins keyboards
    public static InlineKeyboardMarkup adminPanel() {
        return TelegramData.createInlineKeyboardColumn(
                new String[]{
                        "UTM-метки",
                        "Приветственное сообщение",
                        "Отчеты",
                        "⏪ Назад"
                },

                new String[]{
                        "start_utm",
                        "start_welcome_msg",
                        "reports",
                        "main_menu"
                }
        );
    }

    public static InlineKeyboardMarkup editWelcomeMessage() {
        return TelegramData.createInlineKeyboardColumn(
                new String[]{
                        "Просмотреть сообщение",
                        "Редактировать сообщение",
                        "⏪ Назад"
                },
                new String[]{
                        "watch_welcome_msg",
                        "edit_welcome_msg",
                        "admin_menu"
                }
        );
    }

    public static InlineKeyboardMarkup welcomeMessageSaved() {
        return TelegramData.createInlineKeyboardColumn(
                new String[]{
                        "Просмотреть сообщение",
                        "⏪ Назад"},
                new String[]{
                        "watch_welcome_msg",
                        "start_welcome_msg"
                }
        );
    }

    public static InlineKeyboardMarkup cancelInputNewWelcomeText() {
        return TelegramData.createInlineKeyboardColumn(
                new String[]{
                        "⏪ Назад"
                },
                new String[]{
                        "start_welcome_msg"
                }
        );
    }

    public static InlineKeyboardMarkup startEditUtm() {
        return TelegramData.createInlineKeyboardColumn(
                new String[]{
                        "Список",
                        "Добавить UTM",
                        "⏪ Назад"
                },
                new String[]{
                        "list_utm",
                        "add_utm",
                        "admin_menu"
                }
        );
    }

    public static InlineKeyboardMarkup cancelAddNewUtm() {
        return TelegramData.createInlineKeyboardColumn(
                new String[]{
                        "⏪ Назад"
                },
                new String[]{
                        "start_utm"
                }
        );
    }

    public static InlineKeyboardMarkup utmSaved() {
        return TelegramData.createInlineKeyboardColumn(
                new String[]{
                        "Просмотреть список",
                        "⏪ Назад"},
                new String[]{
                        "list_utm",
                        "start_utm"
                }
        );
    }

    public static InlineKeyboardMarkup starReport() {
        return TelegramData.createInlineKeyboardColumn(
                new String[]{
                        "Отчет по подпискам",
                        "⏪ Назад"},

                new String[]{
                        "sub_unsub",
                        "admin_menu"
                }
        );
    }

    public static InlineKeyboardMarkup getSuccessReportResult(String data) {
        return TelegramData.createInlineKeyboardColumn(
                new String[]{
                        "Выбрать другие даты",
                        "⏪ Назад"},

                new String[]{
                        data,
                        "admin_menu"
                }
        );
    }
}