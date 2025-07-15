package app.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "partners")
public class Partner {

    @Id
    private Long partnerTelegramChatId;
    private String name;
    private String inviteLink;
}