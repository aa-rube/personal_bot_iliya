package app.util;

public class LinkWrapper {

    /**
     * Оборачивает указанный фрагмент текста в HTML-ссылку.
     *
     * @param text текст, который нужно обернуть
     * @param url  URL для ссылки
     * @return строка с обернутым текстом в ссылку
     */
    public static String wrapTextInLink(String text, String url) {
        return "<a href='" + url + "'>" + text + "</a>";
    }
}
