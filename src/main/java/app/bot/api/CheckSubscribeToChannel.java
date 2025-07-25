package app.bot.api;

import app.bot.data.Messages;
import app.model.Partner;
import app.repository.PartnersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CheckSubscribeToChannel {

    private final PartnersRepository partners;

    public CheckSubscribeToChannel(PartnersRepository partners) {
        this.partners = partners;
    }

    public boolean hasNotSubscription(MessagingService msg, Update update, Long chatId, int msgId, boolean subscribeChek) {
        List<Partner> partnersList = partners.findAll();
        Map<Partner, Boolean> results = new HashMap<>();

        for (Partner partner : partnersList) {
            String status;

            try {
                status = msg.getChatMember(new GetChatMember(String.valueOf(partner.getPartnerTelegramChatId()), chatId)).getStatus();
            } catch (Exception e) {
                log.error("The status of the user was not received! ChatId: {} Partner: {}, Exception: {}", chatId, partner.getName(), e.getMessage());
                break;
            }

            if (status != null
                    && !status.equals("null")
                    && (status.equals("member")
                    || status.equals("creator")
                    || status.equals("administrator")))
            {
                results.put(partner, true);
            } else {
                results.put(partner, false);
            }
        }

        if (!results.containsValue(false)) {

            if (subscribeChek) {
                msg.processMessage(Messages.uniqueLink(chatId, msgId));
            }

            return false;
        } else {
            msg.processMessage(Messages.subscribeMsg(update, chatId, msgId, results));
            return true;
        }
    }
}