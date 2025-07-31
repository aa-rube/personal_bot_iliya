package app.service;

import app.model.Requests;
import app.repository.RequestsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestsRepository repository;

    public void save(Long userId) {
        repository.save(new Requests(userId));
    }

    public Long getBallsSum(Long c) {
        try {
            Long r = repository.sumBollsByChatId(c);
            return Objects.requireNonNullElse(r, 0L);
        } catch (Exception e) {
            return 0L;
        }
    }
}