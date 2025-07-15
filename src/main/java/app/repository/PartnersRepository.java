package app.repository;

import app.model.Partner;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PartnersRepository extends MongoRepository<Partner, Long> {
}