package app.repository;

import app.model.Activation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivationRepository extends MongoRepository<Activation, Long> {
    List<Activation> findAllByStepAndTimestampLessThan(int step, long threeHoursAgo);

    List<Activation> findAllByTimestampLessThan(long timeAgo);
}