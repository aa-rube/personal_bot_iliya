package app.repository;

import app.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface UserRepository extends MongoRepository<User, Long> {
    // Для проверки активных пользователей (интервал: 3 часа)
    @Query("{ 'active' : true, 'kickUserFromChat' : false, 'lastSubscribeChecked' : { $lt: ?0 } }")
    List<User> findActiveUsersForSubscriptionCheck(long lastCheckedBefore);

    // Для исключения неактивных (интервал: 48 часов)
    @Query("{ 'active' : false, 'kickUserFromChat' : false, 'lastSubscribeChecked' : { $lt: ?0 } }")
    List<User> findInactiveUsersForKickCheck(long lastCheckedBefore);
}