package app.util;

import org.telegram.telegrambots.meta.api.objects.MessageEntity;

import java.util.Comparator;
import java.util.List;

public class MessageEntityUtil {
    // HTML-теги для форматирования
    private static final String BOLD_START = "<b>";
    private static final String BOLD_END = "</b>";
    private static final String ITALIC_START = "<i>";
    private static final String ITALIC_END = "</i>";
    private static final String UNDERLINE_START = "<u>";
    private static final String UNDERLINE_END = "</u>";
    private static final String STRIKE_START = "<s>";
    private static final String STRIKE_END = "</s>";
    private static final String SPOILER_START = "<tg-spoiler>";
    private static final String SPOILER_END = "</tg-spoiler>";
    private static final String CODE_START = "<code>";
    private static final String CODE_END = "</code>";
    private static final String PRE_START = "<pre>";
    private static final String PRE_END = "</pre>";

    /**
     * Преобразует исходный текст с учётом списка MessageEntity в HTML-разметку.
     * Возвращает готовую строку, которую можно отправлять как HTML.
     */
    public static String messageEntityToHtml(String text, List<MessageEntity> entities) {
        if (text == null) return null;
        if (entities == null || entities.isEmpty()) return text;

        // Чтобы при вставке тегов не ломать последующие индексы,
        // сортируем сущности по УБЫВАНИЮ offset
        // (сначала обрабатываем те, что начинаются дальше в тексте).
        entities.sort(Comparator.comparingInt(MessageEntity::getOffset).reversed());

        StringBuilder sb = new StringBuilder(text);

        for (MessageEntity entity : entities) {
            int start = entity.getOffset();
            int length = entity.getLength();
            int end = start + length;

            if (start < 0 || end > sb.length() || start >= end) {
                // Некорректные границы - пропустим
                continue;
            }

            String type = entity.getType();
            String openTag = "";
            String closeTag = "";

            // Подбираем HTML-теги исходя из типа сущности
            switch (type) {
                // --- Форматирующие сущности ---
                case "bold":
                    openTag = BOLD_START; closeTag = BOLD_END;
                    break;
                case "italic":
                    openTag = ITALIC_START; closeTag = ITALIC_END;
                    break;
                case "underline":
                    openTag = UNDERLINE_START; closeTag = UNDERLINE_END;
                    break;
                case "strikethrough":
                    openTag = STRIKE_START; closeTag = STRIKE_END;
                    break;
                case "spoiler":
                    // Telegram поддерживает <tg-spoiler>
                    openTag = SPOILER_START; closeTag = SPOILER_END;
                    break;
                case "code":
                    openTag = CODE_START; closeTag = CODE_END;
                    break;
                case "pre":
                    openTag = PRE_START; closeTag = PRE_END;
                    break;

                // --- Ссылки и упоминания ---
                case "text_link":
                    // entity.getUrl() содержит ссылку
                    if (entity.getUrl() != null && !entity.getUrl().isEmpty()) {
                        openTag = "<a href=\"" + escapeHtml(entity.getUrl()) + "\">";
                        closeTag = "</a>";
                    }
                    break;
                case "text_mention":
                    // entity.getUser() содержит User
                    // Делаем ссылку вида tg://user?id=USER_ID
                    if (entity.getUser() != null && entity.getUser().getId() != null) {
                        openTag = "<a href=\"tg://user?id=" + entity.getUser().getId() + "\">";
                        closeTag = "</a>";
                    }
                    break;

                // --- Не форматирующие, но часто хотят превратить в ссылки ---
                case "mention":        // Например, @username
                case "bot_command":    // Например, /start
                case "hashtag":        // Например, #tag
                case "cashtag":        // Например, $USD
                case "url":            // Ссылка
                case "email":
                case "phone_number":
                    // При желании можно оставить без форматирования.
                    // Или, например, превратить в ссылку:
                    String subText = sb.substring(start, end);

                    // Для "url" - просто <a href="url">url</a>
                    // Для "email" - <a href="mailto:...">...</a>
                    // Для "phone_number" - <a href="tel:...">...</a>
                    // Для остальных - можно или просто выделить <i>...</i>, или сделать поиск чего-то ещё.

                    // Пример (упрощенный): всегда делаем ссылку <a href="...">...</a> для url/email/phone_number
                    // а для mention/hashtag/bot_command/cashtag просто оставляем как есть или делаем <i>...<i>.
                    if (type.equals("url")) {
                        openTag = "<a href=\"" + escapeHtml(subText) + "\">";
                        closeTag = "</a>";
                    } else if (type.equals("email")) {
                        openTag = "<a href=\"mailto:" + escapeHtml(subText) + "\">";
                        closeTag = "</a>";
                    } else if (type.equals("phone_number")) {
                        openTag = "<a href=\"tel:" + escapeHtml(subText) + "\">";
                        closeTag = "</a>";
                    } else {
                        // Можно просто наклонный шрифт или оставить пусто
                        // openTag = "<i>"; closeTag = "</i>";
                    }
                    break;
                default:
                    // Неизвестный тип или не требуется обработка
                    break;
            }

            // Если получили непустые теги, вставляем их в строку
            if (!openTag.isEmpty() || !closeTag.isEmpty()) {
                sb.insert(start, openTag);
                // Так как мы уже вставили openTag слева,
                // конец текста сдвинулся на openTag.length()
                sb.insert(end + openTag.length(), closeTag);
            }
        }

        return sb.toString();
    }

    /**
     * Простейшее экранирование спецсимволов для использования внутри атрибутов HTML.
     * Можно расширять/изменять при необходимости.
     */
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
