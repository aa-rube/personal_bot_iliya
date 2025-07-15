package app.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

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
            String username = user.getUserName();
            return (username != null && !username.trim().isEmpty())
                    ? "@" + username
                    : DEFAULT_USERNAME;

        } catch (Exception e) {
            logger.error("Error extracting username: ", e);
            return DEFAULT_USERNAME;
        }
    }

    /**
     * Извлекает «полное имя» (FirstName + LastName) из Update,
     * складывая их через запятую, если оба присутствуют.
     *
     * @param update объект Telegram Update
     * @return строка вида "Имя", "Имя, Фамилия" или пустая, если данных нет
     */
    public static String extractFullName(Update update) {
        try {
            if (update == null || update.getMessage() == null || update.getMessage().getFrom() == null) {
                return "";
            }

            User user = update.getMessage().getFrom();
            String firstName = (user.getFirstName() != null) ? user.getFirstName().trim() : "";
            String lastName = (user.getLastName() != null) ? user.getLastName().trim() : "";

            StringBuilder sb = new StringBuilder();
            if (!firstName.isEmpty()) {
                sb.append(firstName);
            }
            if (!lastName.isEmpty()) {
                // Если и имя, и фамилия не пустые, разделяем запятой и пробелом
                if (!firstName.isEmpty()) {
                    sb.append(", ");
                }
                sb.append(lastName);
            }
            return sb.toString();
        } catch (Exception e) {
            logger.warn("extractFullName: Не удалось извлечь полное имя", e);
            return "";
        }
    }
}