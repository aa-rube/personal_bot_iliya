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
    private final MessagingService msg;
    private final ReferralService referralService;

    public ActivationService(@Lazy MessagingService msg,
                             ActivationRepository activationRepository,
                             ReferralService referralService

    ) {
        this.activationRepository = activationRepository;
        this.msg = msg;
        this.referralService = referralService;
    }

    public void save(Activation activation) {
        activationRepository.save(activation);
    }

    public Activation getActivation(Long chatId) {
        return activationRepository.findById(chatId).orElse(null);
    }

    @Scheduled(cron = "0 * * * * *") // каждую минуту
    public void sendNotification() {

        long timeAgo = System.currentTimeMillis() - (3 * 60 * 60 * 1000);
        List<Activation> outdated = activationRepository.findAllByStepAndTimestampLessThan(0, timeAgo);
        outdated.forEach(a -> {
            int s = a.stepByStep(0);
            if (s == 0) {
                msg.processMessage(Messages.areYouOk(a.getUserId()));
                save(a);
            }
        });

        timeAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
        outdated = activationRepository.findAllByTimestampLessThan(timeAgo);
        outdated.forEach(a -> {
            int s = a.stepByStep(1);
            if (s == 0) {
                msg.processMessage(Messages.areYouOk(a.getUserId()));
                save(a);
            }

            if (s == 1) {
                Map<String, String> m = referralService.getUsrLevel(a.getUserId());
                int i = msg.processMessageReturnMsgId(Messages.share(a.getUserId(), -1, m));
                msg.processMessage(new PinChatMessage(String.valueOf(a.getUserId()), i));
                save(a);
            }
        });

        timeAgo = System.currentTimeMillis() - (48 * 60 * 60 * 1000);
        outdated = activationRepository.findAllByStepAndTimestampLessThan(1, timeAgo);
        outdated.forEach(a -> {
            int s = a.stepByStep(1);
            if (s == 0) {
                msg.processMessage(Messages.areYouOk(a.getUserId()));
                save(a);
            }
        });


        timeAgo = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000);
        outdated = activationRepository.findAllByStepAndTimestampLessThan(2, timeAgo);
        outdated.forEach(a -> {
            int s = a.stepByStep(2);
            if (s == 2) {
                Map<String, String> m = referralService.getUsrLevel(a.getUserId());
                int i = msg.processMessageReturnMsgId(Messages.share(a.getUserId(), -1, m));
                msg.processMessage(new PinChatMessage(String.valueOf(a.getUserId()), i));
                save(a);
            }
        });

        timeAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
        outdated = activationRepository.findAllByStepAndTimestampLessThan(3, timeAgo);
        outdated.forEach(a -> {
            int s = a.stepByStep(3);
            if (s == 3) {
                Map<String, String> m = referralService.getUsrLevel(a.getUserId());
                int i = msg.processMessageReturnMsgId(Messages.share(a.getUserId(), -1, m));
                msg.processMessage(new PinChatMessage(String.valueOf(a.getUserId()), i));
                save(a);
            }
        });

        timeAgo = System.currentTimeMillis() - (14 * 24 * 60 * 60 * 1000);
        outdated = activationRepository.findAllByStepAndTimestampLessThan(4, timeAgo);
        outdated.forEach(a -> {
            int s = a.stepByStep(4);
            if (s == 4) {
                Map<String, String> m = referralService.getUsrLevel(a.getUserId());
                int i = msg.processMessageReturnMsgId(Messages.share(a.getUserId(), -1, m));
                msg.processMessage(new PinChatMessage(String.valueOf(a.getUserId()), i));
            }
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