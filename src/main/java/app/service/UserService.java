package app.service;

import app.model.User;
import app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void saveUser(Update update, Long chatId, Long ref) {
        userRepository.save(new User(update, chatId, ref));
    }

    public boolean userDoesNotExists(Long chatId) {
        return !userRepository.existsById(chatId);
    }
}