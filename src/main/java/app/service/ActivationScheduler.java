package app.service;

import app.bot.api.MessagingService;
import app.data.Messages;
import app.data.UserActionData;
import app.model.Activation;
import app.repository.ActivationRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;

import java.util.List;

@Service
public class ActivationScheduler {

    private final ActivationRepository repo;
    private final MessagingService msg;
    private final ReferralService referral;
    private final UserActionService userActionService;


    public ActivationScheduler(ActivationRepository repo,
                               @Lazy MessagingService msg,
                               ReferralService referral,
                               UserActionService userActionService
    ) {
        this.repo = repo;
        this.msg = msg;
        this.referral = referral;
        this.userActionService = userActionService;
    }

    @Scheduled(cron = "0 * * * * *")            // раз в минуту
    public void sendNotifications() {

        long now = System.currentTimeMillis();
        List<Activation> due = repo.findByNextSendAtLessThanEqual(now);
        due.forEach(this::process);
    }

    private void process(Activation a) {
        Activation.Step step = Activation.Step.byCode(a.getStep());

        switch (step) {
            case FIRST -> {            // +3 ч
                msg.process(Messages.areYouOk(a.getUserId()));
                userActionService.addUserAction(a.getUserId(), UserActionData.GET_SCHEDULE_MSG_IS_ARE_YOU_OK_3H);
            }

            case SECOND -> {         // +24 ч
                msg.process(Messages.areYouOk(a.getUserId()));
                a.setStep(step.next().code);           // продвинуть вручную, если нужен иной поток
                userActionService.addUserAction(a.getUserId(), UserActionData.GET_SCHEDULE_MSG_IS_ARE_YOU_OK_24H);
            }

            case THIRD, FOURTH, FIFTH -> {  // +72 ч, +7 дней, +14 дней
                var lvl = referral.getUsrLevel(a.getUserId());
                int pin = msg.processMessageReturnMsgId(Messages.share(a.getUserId(), lvl));
                msg.process(new PinChatMessage(String.valueOf(a.getUserId()), pin));
                userActionService.addUserAction(a.getUserId(), UserActionData.GET_SCHEDULE_MSG_IS_ARE_YOU_OK_73H_7D_14D);
            }

            default -> { /* DONE – ничего не делаем */ }
        }

        // перевести на следующий шаг и сохранить
        a.advance();
        repo.save(a);

        // если сценарий завершён – чистим запись
        if (a.getStep() == Activation.Step.DONE.code) {
            repo.deleteById(a.getUserId());
            userActionService.addUserAction(a.getUserId(), UserActionData.GET_SCHEDULE_COMPLETE);
        }
    }
}
