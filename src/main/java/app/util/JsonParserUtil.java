package app.util;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonParserUtil {

    /**
     * Принимает строку вида:
     *  Update(updateId=744371647, message=Message(...), inlineQuery=null, ...)
     * и возвращает JSONObject.
     * Зависимость: org.json:json
     */
    public static JSONObject toJson(String dump) {
        return new Parser(dump).parseRoot();
    }

    /* -------------------- ниже — простой рекурсивный парсер -------------------- */

    private static final class Parser {
        private final char[] src;
        private int p = 0;

        Parser(String s) {
            this.src = s.toCharArray();
        }

        JSONObject parseRoot() {
            skipWs();
            // корень всегда ClassName(...). Имя класса нам не нужно — пропускаем.
            readIdentifier();           // e.g. "Update"
            expect('(');
            JSONObject obj = parseObjectBody();
            skipWs();
            return obj;
        }

        private JSONObject parseObjectBody() {
            JSONObject o = new JSONObject();
            while (true) {
                skipWs();
                if (peek() == ')') { // ')' или '}' (но у нас только ')')
                    p++;
                    break;
                }
                String key = readIdentifier();
                expect('=');
                Object val = parseValue();
                o.put(key, val);

                skipWs();
                if (peek() == ',') {
                    p++;
                    continue;
                }
                // может сразу идти закрывающая скобка
                if (peek() == ')') continue;
            }
            return o;
        }

        private Object parseValue() {
            skipWs();
            char c = peek();

            // массив
            if (c == '[') {
                p++; // skip '['
                JSONArray arr = new JSONArray();
                while (true) {
                    skipWs();
                    if (peek() == ']') { p++; break; }
                    Object v = parseValue();
                    arr.put(v);
                    skipWs();
                    if (peek() == ',') { p++; continue; }
                    if (peek() == ']') continue;
                }
                return arr;
            }

            // Вложенный объект ClassName(...)? Или обычная строка, начинающаяся с буквы?
            if (isIdentifierStart(c)) {
                int mark = p;
                String ident = readIdentifier();
                skipWs();
                if (peek() == '(') {
                    p++; // объект
                    return parseObjectBody();
                } else {
                    // НЕ объект → это значение. Может быть одно слово (supergroup),
                    // а может быть фраза с пробелами. Откатываемся и читаем целиком.
                    p = mark;
                    String raw = readBareString();
                    return castScalar(raw);
                }
            }

            // null / true / false / number / строка без пробелов
            if (c == 'n') { // null
                if (matchAhead("null")) { p += 4; return JSONObject.NULL; }
            }
            if (c == 't') { if (matchAhead("true"))  { p += 4; return true;  } }
            if (c == 'f') { if (matchAhead("false")) { p += 5; return false; } }

            // число
            if (c == '-' || Character.isDigit(c)) {
                String num = readNumber();
                return castNumber(num);
            }

            // на всякий случай — всё остальное читаем до разделителя как строку
            return readBareString();
        }

        /* -------------------- helpers -------------------- */

        private Object castScalar(String token) {
            if ("null".equals(token))  return JSONObject.NULL;
            if ("true".equals(token))  return true;
            if ("false".equals(token)) return false;
            // число?
            if (token.matches("-?\\d+"))        return Long.parseLong(token);
            if (token.matches("-?\\d+\\.\\d+")) return Double.parseDouble(token);
            return token; // строка
        }

        private Number castNumber(String s) {
            if (s.contains(".")) return Double.parseDouble(s);
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                return Double.parseDouble(s); // fallback
            }
        }

        private String readNumber() {
            int start = p;
            if (peek() == '-') p++;
            while (!eof() && Character.isDigit(peek())) p++;
            if (!eof() && peek() == '.') {
                do p++;
                while (!eof() && Character.isDigit(peek()));
            }
            return new String(src, start, p - start);
        }

        private String readBareString() {
            int start = p;
            while (!eof()) {
                char ch = peek();
                if (ch == ',' || ch == ')' || ch == ']' ) break;
                p++;
            }
            return new String(src, start, p - start).trim();
        }

        private String readIdentifier() {
            int start = p;
            if (!isIdentifierStart(peek()))
                throw new IllegalStateException("Identifier expected at pos " + p);

            do p++;
            while (!eof() && isIdentifierPart(peek()));
            return new String(src, start, p - start);
        }

        private boolean isIdentifierStart(char c) {
            return Character.isLetter(c) || c == '_' || c == '$';
        }

        private boolean isIdentifierPart(char c) {
            return Character.isLetterOrDigit(c) || c == '_' || c == '$';
        }

        private void expect(char ch) {
            skipWs();
            if (peek() != ch)
                throw new IllegalStateException("Expected '" + ch + "' at pos " + p + ", got '" + peek() + "'");
            p++;
        }

        private void skipWs() {
            while (!eof() && Character.isWhitespace(src[p])) p++;
        }

        private char peek() {
            return eof() ? '\0' : src[p];
        }

        private boolean matchAhead(String word) {
            if (p + word.length() > src.length) return false;
            for (int i = 0; i < word.length(); i++) {
                if (src[p + i] != word.charAt(i)) return false;
            }
            return true;
        }

        private boolean eof() {
            return p >= src.length;
        }
    }

}
