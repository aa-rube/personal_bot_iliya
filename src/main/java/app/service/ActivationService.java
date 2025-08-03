package app.service;

import app.model.Activation;
import app.repository.ActivationRepository;
import org.springframework.stereotype.Service;

@Service
public class ActivationService {

    private final ActivationRepository activationRepository;

    public ActivationService(ActivationRepository activationRepository) {
        this.activationRepository = activationRepository;
    }

    public void save(Activation activation) {
        activationRepository.save(activation);
    }

    public Activation getActivation(Long chatId) {
        return activationRepository.findById(chatId).orElse(null);
    }

    public void deleteByUserId(Long userId) {
        activationRepository.deleteById(userId);
    }
}