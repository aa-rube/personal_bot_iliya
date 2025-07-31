package app.model;

import app.util.UpdateNameExtractor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;


@Getter
@Setter
@NoArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private Long chatId;
    private String userName;
    private String fullName;
    private Long referrerId;
    private int level;
    private int points;
    private LocalDate firstSeen;
    private LocalDate lastAction;

    private boolean isActive;
    private long lastSubscribeChecked;

    private boolean isKickUserFromChat;

    public User(Update update, Long chatId, Long referrerId) {
        this.chatId = chatId;
        this.userName = UpdateNameExtractor.extractUserName(update);
        this.fullName = UpdateNameExtractor.extractFullName(update);
        this.referrerId = referrerId;
        this.level = 0;
        this.points = 0;

        this.firstSeen = LocalDate.now();
        this.lastAction = LocalDate.now();

        this.lastSubscribeChecked = System.currentTimeMillis();
        this.isActive = true;
        this.isKickUserFromChat = false;
    }

    public User(long id, String text) {
        this.chatId = id;
        this.fullName = text;
    }
}
