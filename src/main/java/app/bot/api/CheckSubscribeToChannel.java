package app.bot.api;

import app.data.Messages;
import app.data.UserActionData;
import app.model.Partner;
import app.repository.PartnersRepository;
import app.service.ReferralService;
import app.service.UserActionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
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
    private final ReferralService referralService;
    private final MessagingService msg;

    public CheckSubscribeToChannel(PartnersRepository partners,
                                   ReferralService referralService,
                                   @Lazy MessagingService msg) {
        this.partners = partners;
        this.referralService = referralService;
        this.msg = msg;
    }

    public boolean hasNotSubscription(Update update, Long chatId, int msgId, boolean subscribeChek) {
        if (chatId < 0) return true;

        Map<Partner, Boolean> results = checkList(chatId, partners.findAll());

        if (!results.containsValue(false)) {
            if (subscribeChek) {
                Map<String, String> m = referralService.getUsrLevel(chatId);
                msg.process(Messages.uniqueLink(chatId, msgId, m));
            }

            return false;
        } else {
            msg.process(Messages.subscribeMsg(update, chatId, msgId, results));
            return true;
        }
    }

    public Map<Partner, Boolean> checkList(Long chatId, List<Partner> partnersList) {
        Map<Partner, Boolean> results = new HashMap<>();
        for (Partner partner : partnersList) {

            boolean r = checkUserPartner(chatId, partner.getPartnerTelegramChatId());
            if (r) {
                results.put(partner, true);
            } else {
                results.put(partner, false);
            }
        }
        return results;
    }

    public boolean checkUserPartner(Long chatId, Long partner) {
        String status;
        try {
            status = msg.getChatMember(new GetChatMember(String.valueOf(partner), chatId)).getStatus();
        } catch (Exception e) {
            return false;
        }
        return status != null
                && !status.equals("null")
                && (status.equals("member")
                || status.equals("creator")
                || status.equals("administrator"));
    }
}