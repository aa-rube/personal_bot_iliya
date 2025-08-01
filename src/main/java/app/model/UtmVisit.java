package app.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "utm_visit")
public class UtmVisit {

    @Id
    private String id;
    private Long utmId;
    private Long userId;
    private Long timestamp;
    private Long timestampUtc;
    private LocalDateTime localDateTime;

    public UtmVisit(String uuid, Long utmId, Long userId) {
        this.id = uuid;
        this.utmId = utmId;
        this.userId = userId;
        this.timestamp = System.currentTimeMillis();
        this.timestampUtc = Instant.EPOCH.toEpochMilli();
        this.localDateTime = LocalDateTime.now();
    }
}
