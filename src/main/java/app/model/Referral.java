package app.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "ref")
public class Referral {

    @Id
    private Long userId;

    private Long referrerId;

    private int depth;//=1/2/

    private Long timestamps;

    public Referral(Long newUser, Long ref) {
        this.userId = newUser;
        this.referrerId = ref;
        this.depth = 1;
        this.timestamps = System.currentTimeMillis();
    }
}
