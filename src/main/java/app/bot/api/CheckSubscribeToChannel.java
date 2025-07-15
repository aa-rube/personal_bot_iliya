package app.bot.api;

import app.bot.telegramdata.TelegramData;
import app.model.Partner;
import app.repository.PartnersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.ArrayList;
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

    public boolean hasSubscription(MessagingService msg, Long chatId, int msgId) {
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
            return true;
        } else {
            if (msgId == -1) {
                msg.processMessage(TelegramData.getSendMessage(chatId, getStr(), getSubscribeButtons(results)));
            } else {
                msg.processMessage(TelegramData.getEditMessage(chatId, getStr(), getSubscribeButtons(results), msgId));
            }
            return false;
        }
    }

    private String getStr() {
        return "Подпишись на официальный канал";
    }

    public InlineKeyboardMarkup getSubscribeButtons(Map<Partner, Boolean> results) {
        List<String> t = new ArrayList<>();
        List<String> d = new ArrayList<>();

        for (Map.Entry<Partner, Boolean> entry : results.entrySet()) {
            if (!entry.getValue()) {
                t.add("Подписаться - ".concat(entry.getKey().getName()));
                d.add(entry.getKey().getInviteLink());
            }
        }

        t.add("✅ Проверить подписку");
        d.add("subscribe_chek");
        return TelegramData.createInlineKeyboardColumn(
                t.toArray(new String[0]),d.toArray(new String[0])
        );
    }
}