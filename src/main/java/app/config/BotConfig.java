package app.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class BotConfig {
    private final String username;
    private final String token;
    private final Long logChat;
    private final String secretChatLink;

    public BotConfig(@Value("${bot.username}") String username,
                     @Value("${bot.token}") String token,
                     @Value("${bot.log.chat}") Long logChat,
                     @Value("${bot.secret.chat.link}") String secretChatLink
    ) {
        this.username = username;
        this.token = token;
        this.logChat = logChat;
        this.secretChatLink = secretChatLink;
    }
}