package app.bot.handler;

import app.bot.api.MessagingService;
import app.bot.data.Messages;
import app.config.AppConfig;
import app.bot.api.CheckSubscribeToChannel;
import app.service.ReferralService;
import app.service.UserService;
import app.util.ExtractReferralIdFromStartCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

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

        if (subscribe.hasNotSubscription(msg, chatId, -1)) return;

        if (text.equals("/start")) {
            if (!userService.userDoesNotExists(chatId)) {
                userService.saveUser(update, chatId, 0L);
            }
        }

        if (text.contains("/start ")) {
            if (userService.userDoesNotExists(chatId)) {
                Long ref = ExtractReferralIdFromStartCommand.extract(text);
                int c = referralService.updateRefUserWithCount(chatId, ref);
                userService.saveUser(update, chatId, ref);

                if (c > 100) {
                    msg.processMessage(Messages.overInviteLimit(appConfig.getLogChat()));
                } else {
                    msg.processMessage(Messages.newUser(appConfig.getLogChat()));
                }
            }
        }

        msg.processMessage(Messages.mainMenu(chatId, -1));
    }

}