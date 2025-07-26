package app.repository;

import app.model.AutoMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AutoMessageRepository extends MongoRepository<AutoMessage, Long> {
}
