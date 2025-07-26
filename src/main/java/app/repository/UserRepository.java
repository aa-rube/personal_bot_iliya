package app.repository;

import app.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface UserRepository extends MongoRepository<User, Long> {

    @Query("{ 'isKickUserFromChat': ?0, 'isActive': ?1, 'lastSubscribeChecked': { $lt: ?2 } }")
    List<User> findAllByKickActiveBefore(boolean kickUserFromChat,
                                         boolean isActive,
                                         long lastSubscribeChecked);

    // Если чаще нужно именно TRUE/TRUE:
    @Query("{ 'isKickUserFromChat': true, 'isActive': true, 'lastSubscribeChecked': { $lt: ?0 } }")
    List<User> findAllKickedActiveBefore(long lastSubscribeChecked);
}
