package app.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Getter
@Configuration
public class AppConfig {

    private final String spreadsheetId;
    private final String applicationName;
    private final String  credentialsPath;
    private final byte[]  credentialsBytes;


    private final String username;
    private final String token;
    private final Long logChat;
    private final String secretChatLink;
    private final Long botPrivateChannel;

    public AppConfig(
            @Value("${google.sheets.spreadsheet.id}") String spreadsheetId,
            @Value("${google.sheets.credentials.path}") String credentialsPath,
            @Value("${google.sheets.application.name}") String applicationName,

            @Value("${bot.username}") String username,
            @Value("${bot.token}") String token,
            @Value("${bot.log.chat}") Long logChat,
            @Value("${bot.secret.chat.link}") String secretChatLink,
            @Value("${bot.private.channel}") Long botPrivateChannel

    ) throws IOException {
        this.spreadsheetId   = spreadsheetId;
        this.credentialsPath = credentialsPath;
        this.applicationName = applicationName;
        this.credentialsBytes = Files.readAllBytes(Path.of(credentialsPath));


        this.username = username;
        this.token = token;
        this.logChat = logChat;
        this.secretChatLink = secretChatLink;
        this.botPrivateChannel = botPrivateChannel;
    }

    /** отдаём новый stream при каждом запросе */
    public InputStream getCredentialsStream() {
        return new ByteArrayInputStream(credentialsBytes);
    }
}