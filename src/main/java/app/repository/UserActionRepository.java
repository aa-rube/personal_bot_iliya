package app.repository;

import app.data.UserActionData;
import app.model.UserAction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface UserActionRepository extends MongoRepository<UserAction, String> {

    // все события в промежутке (используется для быстрой выборки за период)
    List<UserAction> findByLocalDateTimeBetween(LocalDateTime from, LocalDateTime to);

    // все события пользователя (используем для проверки истории до периода)
    List<UserAction> findByUserId(Long userId);

    // все события пользователя до даты (эффективно)
    List<UserAction> findByUserIdAndLocalDateTimeBefore(Long userId, LocalDateTime before);

    // опционально: все события пользователя в промежутке
    List<UserAction> findByUserIdAndLocalDateTimeBetween(Long userId, LocalDateTime from, LocalDateTime to);

    // опционально: найти у пользователя конкретные типы событий
    List<UserAction> findByUserIdAndUserActionDataIn(Long userId, Collection<UserActionData> actions);
}