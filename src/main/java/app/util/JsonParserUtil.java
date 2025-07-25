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


    public static void main(String[] args) {
        System.out.println(toJson("Update(updateId=744371656, message=Message(messageId=29, messageThreadId=null, from=User(id=6714443394, firstName=javaDev, isBot=false, lastName=null, userName=sdelautgbota, languageCode=ru, canJoinGroups=null, canReadAllGroupMessages=null, supportInlineQueries=null, isPremium=null, addedToAttachmentMenu=null), date=1753460781, chat=Chat(id=-1002317608626, type=supergroup, title=С GPT-на-Ты: Клуб Доверия к Нейросетям, firstName=null, lastName=null, userName=null, photo=null, description=null, inviteLink=null, pinnedMessage=null, stickerSetName=null, canSetStickerSet=null, permissions=null, slowModeDelay=null, bio=null, linkedChatId=null, location=null, messageAutoDeleteTime=null, hasPrivateForwards=null, HasProtectedContent=null, joinToSendMessages=null, joinByRequest=null, hasRestrictedVoiceAndVideoMessages=null, isForum=true, activeUsernames=null, emojiStatusCustomEmojiId=null, hasAggressiveAntiSpamEnabled=null, hasHiddenMembers=null, emojiStatusExpirationDate=null, availableReactions=null, accentColorId=null, backgroundCustomEmojiId=null, profileAccentColorId=null, profileBackgroundCustomEmojiId=null, hasVisibleHistory=null, unrestrictBoostCount=null, customEmojiStickerSetName=null), forwardFrom=null, forwardFromChat=null, forwardDate=null, text=null, entities=null, captionEntities=null, audio=null, document=null, photo=null, sticker=null, video=null, contact=null, location=null, venue=null, animation=null, pinnedMessage=null, newChatMembers=[], leftChatMember=User(id=6714443394, firstName=javaDev, isBot=false, lastName=null, userName=sdelautgbota, languageCode=ru, canJoinGroups=null, canReadAllGroupMessages=null, supportInlineQueries=null, isPremium=null, addedToAttachmentMenu=null), newChatTitle=null, newChatPhoto=null, deleteChatPhoto=null, groupchatCreated=null, replyToMessage=null, voice=null, caption=null, superGroupCreated=null, channelChatCreated=null, migrateToChatId=null, migrateFromChatId=null, editDate=null, game=null, forwardFromMessageId=null, invoice=null, successfulPayment=null, videoNote=null, authorSignature=null, forwardSignature=null, mediaGroupId=null, connectedWebsite=null, passportData=null, forwardSenderName=null, poll=null, replyMarkup=null, dice=null, viaBot=null, senderChat=null, proximityAlertTriggered=null, messageAutoDeleteTimerChanged=null, isAutomaticForward=null, hasProtectedContent=true, webAppData=null, videoChatStarted=null, videoChatEnded=null, videoChatParticipantsInvited=null, videoChatScheduled=null, isTopicMessage=null, forumTopicCreated=null, forumTopicClosed=null, forumTopicReopened=null, forumTopicEdited=null, generalForumTopicHidden=null, generalForumTopicUnhidden=null, writeAccessAllowed=null, hasMediaSpoiler=null, userShared=null, chatShared=null, story=null, externalReplyInfo=null, forwardOrigin=null, linkPreviewOptions=null, quote=null, usersShared=null, giveawayCreated=null, giveaway=null, giveawayWinners=null, giveawayCompleted=null, replyToStory=null, boostAdded=null, senderBoostCount=null), inlineQuery=null, chosenInlineQuery=null, callbackQuery=null, editedMessage=null, channelPost=null, editedChannelPost=null, shippingQuery=null, preCheckoutQuery=null, poll=null, pollAnswer=null, myChatMember=null, chatMember=null, chatJoinRequest=null, messageReaction=null, messageReactionCount=null, chatBoost=null, removedChatBoost=null)"));
    }

}
