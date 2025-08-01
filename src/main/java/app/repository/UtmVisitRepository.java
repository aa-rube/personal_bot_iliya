package app.repository;

import app.model.UtmVisit;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UtmVisitRepository extends MongoRepository<UtmVisit, String> {
    boolean existsByUserId(Long userId);
}
