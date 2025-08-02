package app.service;

import app.bot.api.MessagingService;
import app.data.Messages;
import app.model.Activation;
import app.repository.ActivationRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ActivationService {

    private final ActivationRepository activationRepository;

    public ActivationService(@Lazy MessagingService msg,
                             ActivationRepository activationRepository,
                             ReferralService referralService

    ) {
        this.activationRepository = activationRepository;
    }

    public void save(Activation activation) {
        activationRepository.save(activation);
    }

    public Activation getActivation(Long chatId) {
        return activationRepository.findById(chatId).orElse(null);
    }

    /**
     * Удаляет активацию пользователя
     *
     * @param userId ID пользователя
     */
    public void deleteByUserId(Long userId) {
        activationRepository.deleteById(userId);
    }
}