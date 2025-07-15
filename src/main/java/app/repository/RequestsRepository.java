package app.repository;

import app.model.Requests;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RequestsRepository extends MongoRepository<Requests, Long> {
}