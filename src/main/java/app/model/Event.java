package app.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "event")
public class Event {
    @Id
    private Long chatId;
    private EventType event;
    private String values;
    private Long timestamps;
}

enum EventType{
    EVENT
}