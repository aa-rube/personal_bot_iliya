package app.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.*;

public class UpdateNameExtractor {

    private static final Logger logger = LoggerFactory.getLogger(UpdateNameExtractor.class);
    private static final String DEFAULT_USERNAME = "no name";

    public static String extractUserName(Update update) {
        try {
            if (update == null) {
                return DEFAULT_USERNAME;
            }

            User user = null;

            // Проверяем тип Update
            if (update.hasMessage()) {
                Message message = update.getMessage();
                user = message != null ? message.getFrom() : null;
            } else if (update.hasCallbackQuery()) {
                CallbackQuery callbackQuery = update.getCallbackQuery();
                user = callbackQuery != null ? callbackQuery.getFrom() : null;
            }

            // Если не нашли пользователя
            if (user == null) {
                return DEFAULT_USERNAME;
            }

            // Извлекаем username с проверкой на пустоту
            return userExtractName(user);

        } catch (Exception e) {
            logger.error("Error extracting username: ", e);
            return DEFAULT_USERNAME;
        }
    }

    public static String userExtractName(User user) {
        String username = user.getUserName();
        return (username != null && !username.trim().isEmpty())
                ? "@" + username
                : DEFAULT_USERNAME;
    }

    /**
     * Возвращает "Имя" или "Имя, Фамилия" для любого Update:
     *  ‑ текстовое сообщение
     *  ‑ callback‑кнопка
     *  ‑ (опц.) inline‑query, edited‑message и др.
     */
    public static String extractFullName(Update update) {
        if (update == null) {
            return "";
        }

        // 1. Определяем отправителя
        User user = null;
        if (update.hasMessage() && update.getMessage().getFrom() != null) {
            user = update.getMessage().getFrom();
        } else if (update.hasCallbackQuery() && update.getCallbackQuery().getFrom() != null) {
            user = update.getCallbackQuery().getFrom();
        } else if (update.hasInlineQuery() && update.getInlineQuery().getFrom() != null) { // опционально
            user = update.getInlineQuery().getFrom();
        }

        if (user == null) {
            return "";
        }

        // 2. Склеиваем имя
        String first = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String last  = user.getLastName()  != null ? user.getLastName().trim()  : "";

        if (first.isEmpty() && last.isEmpty()) {
            return "";
        }
        return last.isEmpty() ? first : first + ", " + last;
    }

    public static String extractGroupTitleName(Update update) {
        if (update == null
                || update.getMessage() == null
                || update.getMessage().getChat() == null) return "";

        Chat chat = update.getMessage().getChat();
        String title = chat.getTitle();
        if (title == null || title.isEmpty()) return "";
        return title;
    }

}