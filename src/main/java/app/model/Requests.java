package app.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "requests")
public class Requests {

    @Id
    private Long requestId;
    private Long bolls;
    private Long chatId;
    private Long rewardId;
    private Long timestamps;
    private RequestsStatus status;

    public Requests(Long userId) {
        this.requestId = System.currentTimeMillis();
        this.timestamps = Instant.now().toEpochMilli();
        this.rewardId = 1L;
        this.chatId = userId;
        this.bolls = 100L;
        this.status = RequestsStatus.NEW;
    }
}

enum RequestsStatus {
    NEW, PROCESSING, DONE
}