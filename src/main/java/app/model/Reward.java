package app.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "rewards")
public class Reward {

    @Id
    private Long rewardId;

    private String name;

    private Integer costPoints;

    private String description;

    private boolean active;
}
