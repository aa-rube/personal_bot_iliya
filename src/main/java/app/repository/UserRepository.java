package app.repository;

import app.model.User;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, Long> {
    @Query("{ 'active' : true, 'kickUserFromChat' : false, 'lastSubscribeChecked' : { $lt: ?0 } }")
    List<User> findActiveUsersForSubscriptionCheck(long lastCheckedBefore);

    @Query("{ 'active' : false, 'kickUserFromChat' : false, 'lastSubscribeChecked' : { $lt: ?0 } }")
    List<User> findInactiveUsersForKickCheck(long lastCheckedBefore);

    Optional<User> findFirstByChatIdLessThanOrderByChatIdDesc(Long upperExclusive);
    List<User> findByChatIdBetweenOrderByChatIdAsc(Long lowerInclusive, Long upperInclusive);
}