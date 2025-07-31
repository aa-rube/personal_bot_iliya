package app.repository;

import app.model.Requests;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RequestsRepository extends MongoRepository<Requests, Long> {

    @Aggregation(pipeline = {
            "{ '$match': { 'chatId': ?0 } }",
            "{ '$group':  { '_id': null, total: { '$sum': '$bolls' } } }",
            "{ '$project': { '_id': 0, total: 1 } }"
    })
    Long sumBollsByChatId(Long chatId);
}