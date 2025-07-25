package app.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "activation")
public class Activation {
    @Id
    private Long userId;

    private Long timestamp;

    private Integer step;

    public int stepByStep(int currentStep) {
        if (currentStep != step) return -1;

        int s = step;
        this.step = nextStep(s);
        this.timestamp = System.currentTimeMillis();
        return s;
    }

    public Activation(Long userId, Long timestamp, Integer step) {
        this.userId = userId;
        this.timestamp = timestamp;
        this.step = step;
    }

    private int nextStep(int s) {
        switch (s) {
            case 0 -> {//первое уведомление через 3 часа
                return 1;
            }
            case 1 -> {//второе уведомление через 24 часа
                return 2;
            }
            case 2 -> {//третье уведомление через 72 часа
                return 3;
            }
            default -> {
                return 4;
            }
        }
    }
}
