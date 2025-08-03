package app.service;

import app.data.UserActionData;
import app.model.UserAction;
import app.repository.UserActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserActionService {
    private final UserActionRepository repo;

    public void addUserAction(Long chatId, UserActionData userActionData) {
        repo.save(new UserAction(chatId, userActionData));
    }

}