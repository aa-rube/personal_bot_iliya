package app.service;

import app.bot.api.MessagingService;
import app.data.Messages;
import app.model.Activation;
import app.repository.ActivationRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivationService {

    private final ActivationRepository activationRepository;
    private final MessagingService msg;

    public ActivationService(@Lazy MessagingService msg,
                             ActivationRepository activationRepository) {
        this.activationRepository = activationRepository;
        this.msg = msg;
    }

    public void save(Activation activation) {
        activationRepository.save(activation);
    }

    @Scheduled(cron = "0 * * * * *") // каждую минуту
    public void sendNotification() {

        long timeAgo = System.currentTimeMillis() - (3 * 60 * 60 * 1000);
        List<Activation> outdated = activationRepository.findAllByStepAndTimestampLessThan(0, timeAgo);
        outdated.forEach(a -> {
            a.stepByStep(0);
            msg.processMessage(Messages.areYouOk(a.getUserId()));
            save(a);
        });

        timeAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
        outdated = activationRepository.findAllByStepAndTimestampLessThan(1, timeAgo);
        outdated.forEach(a -> {
            a.stepByStep(0);
            msg.processMessage(Messages.share(a.getUserId(), -1));
            save(a);
        });

        timeAgo = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000);
        outdated = activationRepository.findAllByStepAndTimestampLessThan(2, timeAgo);
        outdated.forEach(a -> {
            a.stepByStep(0);
            msg.processMessage(Messages.share(a.getUserId(), -1));
            save(a);
        });

        timeAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
        outdated = activationRepository.findAllByStepAndTimestampLessThan(3, timeAgo);
        outdated.forEach(a -> {
            a.stepByStep(0);
            msg.processMessage(Messages.share(a.getUserId(), -1));
            save(a);
        });

        timeAgo = System.currentTimeMillis() - (14 * 24 * 60 * 60 * 1000);
        outdated = activationRepository.findAllByStepAndTimestampLessThan(4, timeAgo);
        outdated.forEach(a -> {
            a.stepByStep(0);
            msg.processMessage(Messages.share(a.getUserId(), -1));
            deleteByUserId(a.getUserId());
        });
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