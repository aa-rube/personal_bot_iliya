package app.bot.handler;

import app.bot.api.MessagingService;
import app.bot.data.Messages;
import app.config.AppConfig;
import app.bot.api.CheckSubscribeToChannel;
import app.service.ReferralService;
import app.service.UserService;
import app.util.ExtractReferralIdFromStartCommand;
import lombok.Lombok;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

@Slf4j
@Service
public class TextMsgHandler {

    private final AppConfig appConfig;
    private final CheckSubscribeToChannel subscribe;
    private final UserService userService;
    private final ReferralService referralService;
    private final MessagingService msg;

    public TextMsgHandler(AppConfig appConfig,
                          CheckSubscribeToChannel subscribe,
                          UserService userService,
                          ReferralService referralService,
                          @Lazy MessagingService msg
    ) {
        this.appConfig = appConfig;
        this.subscribe = subscribe;
        this.userService = userService;
        this.referralService = referralService;
        this.msg = msg;
    }

    public void updateHandler(Update update) {
        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        int msgId = update.getMessage().getMessageId();

        log.info("msgId: {}, chatId: {}, text: {}", msgId, chatId, text);
        if (chatId.equals(appConfig.getLogChat())) return;

        boolean ue = userService.existsById(chatId);
        if (subscribe.hasNotSubscription(msg,update, chatId, -1, false)) return;

        if (text.equals("/start")) {
            if (!ue) {
                userService.saveUser(update, chatId, 0L);
                msg.processMessage(Messages.uniqueLink(chatId));
            }
            return;
        }

        if (text.contains("/start ")) {

            if (!ue) {
                Long ref = ExtractReferralIdFromStartCommand.extract(text);
                int c = referralService.updateRefUserWithCount(chatId, ref);

                userService.saveUser(update, chatId, ref);
                msg.processMessage(Messages.uniqueLink(chatId));

                if (c > 100) {
                    msg.processMessage(Messages.overInviteLimitForAdmin(appConfig.getLogChat()));
                    msg.processMessage(Messages.overInviteLimitForUser(ref));
                } else {
                    msg.processMessage(Messages.newUser(appConfig.getLogChat()));

                    Map<String, String> m = referralService.getUsrLevel(chatId);
                    long count = Long.parseLong(m.getOrDefault("b", "0"));
                    if (count != 0 && count % 10 == 0) {
                        msg.processMessage(Messages.cheers(ref, m));
                    }
                }
            }

            return;
        }

        msg.processMessage(Messages.mainMenu(chatId, -1));
    }
}