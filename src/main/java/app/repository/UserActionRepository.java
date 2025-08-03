package app.repository;

import app.model.UserAction;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserActionRepository extends MongoRepository<UserAction, String> {
}
