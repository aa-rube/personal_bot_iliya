package app.repository;

import app.model.UserAction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface UserActionRepository extends MongoRepository<UserAction, String> {
    List<UserAction> findByLocalDateTimeBetween(LocalDateTime from, LocalDateTime to);
}
