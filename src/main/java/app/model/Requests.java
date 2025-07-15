package app.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "requests")
public class Requests {

    @Id
    private Long requestId;
    private Long chatId;
    private Long rewardId;
    private Long timestamps;
    private RequestsStatus status;
}

enum RequestsStatus {
    NEW, PROCESSING, DONE
}