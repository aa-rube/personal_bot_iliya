package app.repository;

import app.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserRepository extends MongoRepository<User, Long> {
    public List<User> findAllByIsKickUserFromChatAndIsActiveAndLastSubscribeCheckedLessThan(boolean kickUserFromChat,
                                                                                        boolean isActive,
                                                                                        Long lastSubscribeChecked);
}