package app.model;

import app.data.UserActionData;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "user_action")
public class UserAction {
    private String id;
    private Long userId;
    private Long timestamp;
    private Long utcTimestamp;
    private LocalDateTime localDateTime;
    private UserActionData userActionData;

    public UserAction(Long userId, UserActionData userActionData) {
        this.userId = userId;
        this.timestamp = System.currentTimeMillis();
        this.utcTimestamp = Instant.EPOCH.toEpochMilli();
        this.localDateTime = LocalDateTime.now();
        this.userActionData = userActionData;
    }
}