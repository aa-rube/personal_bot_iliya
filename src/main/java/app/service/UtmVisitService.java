package app.service;

import app.model.UtmVisit;
import app.repository.UtmVisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UtmVisitService {

    private final UtmVisitRepository repo;

    private String uuid() {
        while (true) {
            String uuid = java.util.UUID.randomUUID().toString();
            if (!repo.existsById(uuid)) {
                return uuid;
            }
        }
    }

    public void save(Long utmId, Long userId) {
        if (repo.existsByUserId(userId)) return;
        repo.save(new UtmVisit(uuid(), utmId, userId));

    }
}