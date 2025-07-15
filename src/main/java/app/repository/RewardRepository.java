package app.repository;

import app.model.Reward;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RewardRepository extends MongoRepository<Reward, Long> {
}