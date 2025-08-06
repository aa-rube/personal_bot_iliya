package app.bot.api;

import app.bot.telegramdata.TelegramData;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Инлайн-календарь для выбора диапазона дат (from/to).
 *
 * Протокол callback data (все начинаются с "ft:"):
 *  - "ft:"                    — старт рендера (инициализация)
 *  - "ft:d:YYYY-MM-DD"        — выбор дня
 *  - "ft:m:YYYY-MM"           — перейти к месяцу
 *  - "ft:nav:prev|next"       — переключить месяц (относительно текущего видимого)
 *  - "ft:today"               — перейти к месяцу сегодняшнего дня
 *  - "ft:clear"               — очистить выбор
 *  - "ft:ok"                  — завершить и вернуть диапазон (если обе даты выбраны)
 *  - "ft:cancel"              — отменить (ничего не возвращать)
 */
@Service
public class DateRangePickerService {

    private final MessagingService msg; // Внедрённый сервис из условия

    /** Таймзона, в которой считаем «сегодня» и отображаем даты */
    private final ZoneId zoneId = ZoneId.systemDefault();

    /** Состояния по chatId+messageId */
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    private static final DateTimeFormatter YM_FMT  = DateTimeFormatter.ofPattern("LLLL yyyy", new Locale("ru"));
    private static final DateTimeFormatter YMD_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter D_FMT   = DateTimeFormatter.ofPattern("d");

    public DateRangePickerService(@Lazy MessagingService msg) {
        this.msg = msg;
    }

    /** Единственная публичная точка входа */
    public Optional<Map<String, LocalDate>> handle(Long chatId, Integer msgId, String data) {
        if (data == null || !data.startsWith("ft:")) return Optional.empty();

        String key = sessionKey(chatId, msgId);
        Session s = sessions.computeIfAbsent(key, k -> Session.create(zoneId));

        // Разбор callback data
        String[] parts = data.split(":", 4);
        String action = parts.length >= 2 ? parts[1] : "";

        switch (action) {
            case "" -> { // "ft:" — старт/перерисовка
                render(chatId, msgId, s);
                return Optional.empty();
            }
            case "d" -> { // выбор дня
                if (parts.length >= 3) {
                    LocalDate picked = LocalDate.parse(parts[2], YMD_FMT);
                    onPickDate(s, picked);
                    // При выборе дня ещё раз перерисовываем
                    render(chatId, msgId, s);
                }
                return Optional.empty();
            }
            case "m" -> { // перейти к месяцу
                if (parts.length >= 3) {
                    YearMonth ym = YearMonth.parse(parts[2]);
                    s.view = ym;
                    render(chatId, msgId, s);
                }
                return Optional.empty();
            }
            case "nav" -> { // prev/next
                if (parts.length >= 3) {
                    String dir = parts[2];
                    s.view = "prev".equals(dir) ? s.view.minusMonths(1) : s.view.plusMonths(1);
                    render(chatId, msgId, s);
                }
                return Optional.empty();
            }
            case "today" -> {
                s.view = YearMonth.from(LocalDate.now(zoneId));
                render(chatId, msgId, s);
                return Optional.empty();
            }
            case "clear" -> {
                s.from = null;
                s.to = null;
                s.stage = Stage.PICK_FROM;
                render(chatId, msgId, s);
                return Optional.empty();
            }
            case "cancel" -> {
                sessions.remove(key);
                // Можно очистить сообщение/написать «Отменено», но по условию — просто редактируем.
                msg.process(TelegramData.getEditMessage(
                        chatId,
                        "Выбор даты отменён.",
                        null,
                        msgId
                ));
                return Optional.empty();
            }
            case "ok" -> {
                if (s.from != null && s.to != null) {
                    Map<String, LocalDate> result = Map.of("f", s.from, "t", s.to);
                    sessions.remove(key);
                    msg.process(TelegramData.getEditMessage(
                            chatId,
                            "Диапазон выбран: с " + s.from + " по " + s.to,
                            null,
                            msgId
                    ));
                    return Optional.of(result);
                } else {
                    // Если не обе выбраны — подсказка и перерисовка
                    render(chatId, msgId, s, "Нужно выбрать обе даты: сначала начало, затем конец.");
                    return Optional.empty();
                }
            }
            default -> {
                // NOP для неизвестных команд
                return Optional.empty();
            }
        }
    }

    /** Вспомогательный метод для старта из контроллера/хэндлера */
    public Optional<Map<String, LocalDate>> start(Long chatId, Integer msgId) {
        return handle(chatId, msgId, "ft:");
    }

    /* ==================== ВНУТРЕННЯЯ ЛОГИКА ==================== */

    private void onPickDate(Session s, LocalDate picked) {
        if (s.from == null) {
            s.from = picked;
            s.to = null;
            s.stage = Stage.PICK_TO;
            s.view = YearMonth.from(picked);
            return;
        }
        if (s.to == null) {
            // Если picked раньше from — меняем местами
            if (picked.isBefore(s.from)) {
                s.to = s.from;
                s.from = picked;
            } else {
                s.to = picked;
            }
            s.stage = Stage.READY;
            s.view = YearMonth.from(picked);
            return;
        }
        // Если обе даты уже были — начинаем заново с новой from
        s.from = picked;
        s.to = null;
        s.stage = Stage.PICK_TO;
        s.view = YearMonth.from(picked);
    }

    private void render(Long chatId, Integer msgId, Session s) {
        render(chatId, msgId, s, null);
    }

    private void render(Long chatId, Integer msgId, Session s, String hint) {
        String title = "Выбор диапазона дат\n" +
                "С: " + (s.from == null ? "—" : s.from) + "   " +
                "По: " + (s.to == null ? "—" : s.to) + "\n" +
                "Шаги: выберите начало → затем конец. Повторный выбор — начать заново.";

        if (hint != null && !hint.isBlank()) {
            title += "\n\n" + hint;
        }

        InlineKeyboardMarkup kb = buildKeyboard(s);
        msg.process(TelegramData.getEditMessage(
                chatId,
                title,
                kb,
                msgId
        ));
    }

    private InlineKeyboardMarkup buildKeyboard(Session s) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Header: Prev | Month Year | Next
        rows.add(Arrays.asList(
                btn("‹", "ft:nav:prev"),
                btn(capitalize(s.view.format(YM_FMT)), "ft:m:" + s.view),
                btn("›", "ft:nav:next")
        ));

        // Weekdays (locale-aware, starting Monday)
        rows.add(weekdayHeader());

        // Days grid
        rows.addAll(buildDaysGrid(s));

        // Footer actions
        List<InlineKeyboardButton> footer1 = new ArrayList<>();
        footer1.add(btn("Сегодня", "ft:today"));
        footer1.add(btn("Очистить", "ft:clear"));
        rows.add(footer1);

        List<InlineKeyboardButton> footer2 = new ArrayList<>();
        footer2.add(btn("Отмена", "ft:cancel"));
        footer2.add(btn(s.from != null && s.to != null ? "OK" : "OK ▫", "ft:ok"));
        rows.add(footer2);

        InlineKeyboardMarkup m = new InlineKeyboardMarkup();
        m.setKeyboard(rows);
        return m;
    }

    private List<List<InlineKeyboardButton>> buildDaysGrid(Session s) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        YearMonth ym = s.view;
        LocalDate first = ym.atDay(1);
        LocalDate last = ym.atEndOfMonth();

        // Определяем, с какого дня недели начинается сетка (локально)
        WeekFields wf = WeekFields.of(Locale.getDefault());
        int firstDayOfWeek = wf.getFirstDayOfWeek().getValue(); // 1..7
        int firstCol = (first.getDayOfWeek().getValue() - firstDayOfWeek + 7) % 7;

        // Сдвиг до начала сетки (понедельник по РФ-дефолту)
        LocalDate cursor = first.minusDays(firstCol);

        // 6 строк по 7 дней — всегда полностью заполняем сетку
        for (int r = 0; r < 6; r++) {
            List<InlineKeyboardButton> row = new ArrayList<>(7);
            for (int c = 0; c < 7; c++) {
                YearMonth curYm = YearMonth.from(cursor);
                boolean inMonth = curYm.equals(ym);
                String label = inMonth ? cursor.format(D_FMT) : " ";
                String data  = inMonth ? ("ft:d:" + cursor.format(YMD_FMT)) : "ft:"; // пустой клик

                // Визуальные метки диапазона
                if (inMonth) {
                    label = decorateLabel(label, cursor, s.from, s.to);
                }

                row.add(btn(label, data));
                cursor = cursor.plusDays(1);
            }
            rows.add(row);
        }
        return rows;
    }

    private String decorateLabel(String label, LocalDate day, LocalDate from, LocalDate to) {
        if (from != null && day.isEqual(from) && (to == null || !day.isEqual(to))) {
            return "⟦" + label + "⟧"; // начало
        }
        if (to != null && day.isEqual(to) && (from == null || !day.isEqual(from))) {
            return "⟨" + label + "⟩"; // конец
        }
        if (from != null && to != null && (day.isAfter(from) && day.isBefore(to))) {
            return "·" + label + "·"; // внутри диапазона
        }
        if (from != null && to != null && day.isEqual(from) && day.isEqual(to)) {
            return "[" + label + "]"; // один и тот же день
        }
        return label;
    }

    private List<InlineKeyboardButton> weekdayHeader() {
        // Пн..Вс
        List<String> names = Arrays.asList("Пн","Вт","Ср","Чт","Пт","Сб","Вс");
        return names.stream()
                .map(n -> btn(n, "ft:")) // клики по заголовкам ни на что не влияют
                .collect(Collectors.toList());
    }

    private InlineKeyboardButton btn(String text, String data) {
        InlineKeyboardButton b = new InlineKeyboardButton();
        b.setText(text);
        b.setCallbackData(data);
        return b;
    }

    private String sessionKey(Long chatId, Integer msgId) {
        return chatId + ":" + msgId;
    }

    private String capitalize(String s) {
        if (s == null || s.isBlank()) return s;
        return s.substring(0,1).toUpperCase(Locale.getDefault()) + s.substring(1);
    }

    /* ==================== МОДЕЛИ ==================== */

    enum Stage { PICK_FROM, PICK_TO, READY }

    static class Session {
        LocalDate from;
        LocalDate to;
        YearMonth view;
        Stage stage;
        ZoneId zone;

        static Session create(ZoneId zone) {
            Session s = new Session();
            s.zone = zone;
            s.from = null;
            s.to = null;
            s.stage = Stage.PICK_FROM;
            s.view = YearMonth.from(LocalDate.now(zone));
            return s;
        }
    }
}
