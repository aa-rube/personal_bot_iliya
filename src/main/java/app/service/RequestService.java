package app.service;

import app.model.Requests;
import app.repository.RequestsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestsRepository repository;

    public void save(Long userId) {
        repository.save(new Requests(userId));
    }

    public Long getBallsSum(Long c) {
        try {
        return repository.sumBollsByChatId(c);
        } catch (Exception e){
            return 0L;
        }
    }
}