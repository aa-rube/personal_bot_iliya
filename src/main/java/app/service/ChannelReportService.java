package app.service;

import app.data.UserActionData;
import app.model.UserAction;
import app.repository.UserActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChannelReportService {

    private final UserActionRepository actionRepo;
    private final GoogleSheetsService  sheetsService;

    // Начальные/реферальные старты
    private static final Set<UserActionData> START_EVENTS = Set.of(
            UserActionData.USER_HAD_START_SUCCESS,
            UserActionData.USER_HAD_REFERRAL_START
    );

    // События подписки (если есть — пользователь не считается "никуда не подписавшимся")
    private static final Set<UserActionData> SUBSCRIBE_EVENTS = Set.of(
            UserActionData.USER_SAVED,           // сохранён после подписки на публичный канал
            UserActionData.JOIN_PRIVATE_CHANNEL  // подписался на приватный канал
    );

    /**
     * Явные системные / планировочные события, которые **не** считаем "активностью пользователя".
     * Если хотите исключить/включить события — отредактируйте этот набор.
     */
    private static final Set<UserActionData> IGNORED_EVENTS = Set.of(
            UserActionData.GET_SCHEDULE_MSG_IS_ARE_YOU_OK_3H,
            UserActionData.GET_SCHEDULE_MSG_IS_ARE_YOU_OK_24H,
            UserActionData.GET_SCHEDULE_MSG_IS_SHARE_WITH_FRIENDS_72H_7D_14D,
            UserActionData.GET_SCHEDULE_COMPLETE
    );

    /**
     * Политика: "прочая активность" = любое событие, которое не в START_EVENTS и не в IGNORED_EVENTS.
     * Это включает меню, шаринги, запросы награды, USER_SAVED и т.д.
     */

    public String export(LocalDate from, LocalDate to) throws IOException {

        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt   = to.plusDays(1).atStartOfDay(); // exclusive upper bound

        // 1) все события внутри периода
        List<UserAction> eventsInPeriod = actionRepo.findByLocalDateTimeBetween(fromDt, toDt);

        // 2) считаем простые метрики по событиям в периоде (подписки/отписки) — эти метрики берём по событиям внутри периода
        Set<Long> publicJoined        = new HashSet<>();
        Set<Long> publicLeft          = new HashSet<>();
        Set<Long> privateJoined       = new HashSet<>();
        Set<Long> privateLeft         = new HashSet<>();

        for (UserAction ev : eventsInPeriod) {
            if (ev.getUserActionData() == null || ev.getUserId() == null) continue;
            switch (ev.getUserActionData()) {
                case USER_SAVED -> publicJoined.add(ev.getUserId());
                case LEFT_PUBLIC_CHANNEL -> publicLeft.add(ev.getUserId());
                case JOIN_PRIVATE_CHANNEL -> privateJoined.add(ev.getUserId());
                case LEFT_PRIVATE_CHANNEL -> privateLeft.add(ev.getUserId());
                default -> { /* остальные считаем ниже */ }
            }
        }

        // 3) сгруппируем события в период по пользователю — это даст нам кандидатов со стартом
        Map<Long, List<UserAction>> eventsByUserInPeriod = eventsInPeriod.stream()
                .filter(e -> e.getUserId() != null)
                .collect(Collectors.groupingBy(UserAction::getUserId));

        // 4) находим всех кандидатов, у которых в периоде есть старт-эвент
        Set<Long> startCandidates = eventsByUserInPeriod.entrySet().stream()
                .filter(e -> e.getValue().stream()
                        .anyMatch(ev -> START_EVENTS.contains(ev.getUserActionData())))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        // 5) окончательный набор startersOnly — те, у кого есть старт в периоде
        //    и при этом нет "прочей активности" ни в периоде, ни в истории до fromDt,
        //    и при этом нет событий подписки (в истории или в периоде)
        Set<Long> startersOnly = new HashSet<>();

        for (Long userId : startCandidates) {

            // события в период (мы уже их имеем)
            List<UserAction> inPeriod = eventsByUserInPeriod.getOrDefault(userId, List.of());

            // события до периода начала (история)
            List<UserAction> before = actionRepo.findByUserIdAndLocalDateTimeBefore(userId, fromDt);

            // объединяем для проверок
            List<UserAction> combined = new ArrayList<>(before.size() + inPeriod.size());
            combined.addAll(before);
            combined.addAll(inPeriod);

            // 5a) если есть событие подписки (USER_SAVED или JOIN_PRIVATE_CHANNEL) — дисквалифицируем
            boolean hasSubscriptionEver = combined.stream()
                    .anyMatch(ev -> SUBSCRIBE_EVENTS.contains(ev.getUserActionData()));
            if (hasSubscriptionEver) continue; // не считаем, т.к. где-то подписался

            // 5b) проверка "прочей активности": ищем события, которые не старт и не игнорируемые
            boolean hasOtherActivity = combined.stream()
                    .anyMatch(ev -> {
                        UserActionData d = ev.getUserActionData();
                        if (d == null) return false;
                        if (START_EVENTS.contains(d)) return false;      // старт — ок
                        if (IGNORED_EVENTS.contains(d)) return false;    // системное — ок
                        return true; // любое другое — активность
                    });

            if (hasOtherActivity) continue; // дисквалифицируем

            // Всё чисто — учитываем
            startersOnly.add(userId);
        }

        // 6) итоговая метрика: стартовали, не подписались (глобально) и без прочей активности
        // (тут проверяем глобально: если у пользователя есть подписки в любой точке истории — мы уже отфильтровали выше)
        Set<Long> noSubscription = new HashSet<>(startersOnly);

        // 7) собираем результат для Google Sheets
        List<String> headers = List.of("Метрика", "Количество");

        List<List<Object>> rows = List.of(
                List.of("Подписались на публичный канал (Mr.SuperNew)", publicJoined.size()),
                List.of("Отписались от публичного канала (Mr.SuperNew)", publicLeft.size()),
                List.of("Подписались на приватный канал (С GPT-на-Ты: Клуб Доверия к Нейросетям)", privateJoined.size()),
                List.of("Отписались от приватного канала (С GPT-на-Ты: Клуб Доверия к Нейросетям)", privateLeft.size()),
                List.of("Запустили бота, но не подписались (без прочей активности)", noSubscription.size())
        );

        String reportName = "Отчёт каналов " +
                from.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " – " +
                to.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        return sheetsService.createIndividualReport(reportName, headers, rows);
    }
}
