package app.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Duration;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "activation")
public class Activation {

    @Id
    private Long userId;

    /**
     * Текущий шаг сценария (см. enum Step)
     */
    private int step;

    /**
     * Время (epoch millis), когда нужно отправить следующее сообщение
     */
    private long nextSendAt;

    public Activation(Long userId, Step step) {
        this.userId = userId;
        this.step = Step.FIRST.code;
        this.nextSendAt = step.delayAfterStart().toMillis() + System.currentTimeMillis();
    }

    /**
     * Продвинуть сценарий и пересчитать nextSendAt
     */
    public void advance() {
        Step current = Step.byCode(step);
        Step next = current.next();

        if (next == Step.DONE) {      // сценарий завершён, дальше не шедулим
            this.step = Step.DONE.code;
            this.nextSendAt = Long.MAX_VALUE;
            return;
        }
        this.step = next.code;
        this.nextSendAt = Instant.now().plus(next.delayAfterStart()).toEpochMilli();
    }

    public enum Step {
        FIRST(0, Duration.ofHours(3)),
        SECOND(1, Duration.ofHours(24)),
        THIRD(2, Duration.ofHours(72)),
        FOURTH(3, Duration.ofDays(7)),
        FIFTH(4, Duration.ofDays(14)),
        DONE(5, Duration.ZERO);        // маркер финала

        public final int code;
        private final Duration delay;

        Step(int code, Duration delay) {
            this.code = code;
            this.delay = delay;
        }

        public Duration delayAfterStart() {
            return delay;
        }

        /**
         * Следующий шаг (или DONE)
         */
        public Step next() {
            return values()[Math.min(ordinal() + 1, DONE.ordinal())];
        }

        public static Step byCode(int code) {
            for (Step s : values()) if (s.code == code) return s;
            return DONE;
        }
    }
}
