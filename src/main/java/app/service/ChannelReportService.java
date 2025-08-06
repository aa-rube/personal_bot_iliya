package app.service;

import app.model.UserAction;
import app.repository.UserActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChannelReportService {

    private final UserActionRepository actionRepo;
    private final GoogleSheetsService  sheetsService;

    /**
     * Строит отчёт по событиям каналов и отдаёт ссылку на Google Sheets.
     *
     * @param from  дата "с" (включительно)
     * @param to    дата "по" (включительно)
     */
    public String export(LocalDate from, LocalDate to) throws IOException {

        /* 1. Получаем события за период */
        LocalDateTime fromDt = from.atStartOfDay();          // 00:00 from-дня
        LocalDateTime toDt   = to.plusDays(1).atStartOfDay(); // 00:00 следующего дня (=> inclusive)
        List<UserAction> events = actionRepo.findByLocalDateTimeBetween(fromDt, toDt);

        /* 2. Считаем уникальные userId по каждому типу */
        Set<Long> publicJoined        = new HashSet<>();
        Set<Long> publicLeft          = new HashSet<>();
        Set<Long> privateJoined       = new HashSet<>();
        Set<Long> privateLeft         = new HashSet<>();
        Set<Long> starters            = new HashSet<>();

        for (UserAction ev : events) {
            switch (ev.getUserActionData()) {
                case USER_SAVED -> publicJoined.add(ev.getUserId());           // подписка на Mr.SuperNew
                case LEFT_PUBLIC_CHANNEL -> publicLeft.add(ev.getUserId());    // отписка от Mr.SuperNew
                case JOIN_PRIVATE_CHANNEL -> privateJoined.add(ev.getUserId());// подписка на «С GPT-на-Ты»
                case LEFT_PRIVATE_CHANNEL -> privateLeft.add(ev.getUserId());  // отписка от «С GPT-на-Ты»
                case USER_HAD_START_SUCCESS,
                     USER_HAD_REFERRAL_START -> starters.add(ev.getUserId());  // запуск бота
            }
        }

        /* 3. Запустили бота, но не подписались никуда */
        Set<Long> noSubscription = new HashSet<>(starters);
        noSubscription.removeAll(publicJoined);
        noSubscription.removeAll(privateJoined);

        /* 4. Готовим данные для Sheet */
        List<String> headers = List.of("Метрика", "Количество");

        List<List<Object>> rows = List.of(
                List.of("Подписались на публичный канал (Mr.SuperNew)", publicJoined.size()),
                List.of("Отписались от публичного канала (Mr.SuperNew)", publicLeft.size()),
                List.of("Подписались на приватный канал (С GPT-на-Ты: Клуб Доверия к Нейросетям)", privateJoined.size()),
                List.of("Отписались от приватного канала (С GPT-на-Ты: Клуб Доверия к Нейросетям)", privateLeft.size()),
                List.of("Запустили бота, но не подписались", noSubscription.size())
        );

        /* 5. Создаём одно-листовой отчёт */
        String reportName = "Отчёт каналов " +
                from.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " – " +
                to.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        return sheetsService.createIndividualReport(reportName, headers, rows);
    }
}
