package app.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class AppConfig {
    private final String username;
    private final String token;
    private final Long logChat;
    private final String secretChatLink;
    private final Long botPrivateChannel;

    public AppConfig(@Value("${bot.username}") String username,
                     @Value("${bot.token}") String token,
                     @Value("${bot.log.chat}") Long logChat,
                     @Value("${bot.secret.chat.link}") String secretChatLink,
                     @Value("${bot.private.channel}") Long botPrivateChannel
    ) {
        this.username = username;
        this.token = token;
        this.logChat = logChat;
        this.secretChatLink = secretChatLink;
        this.botPrivateChannel = botPrivateChannel;
    }
}