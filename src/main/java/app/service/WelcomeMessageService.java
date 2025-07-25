package app.service;

import app.bot.api.MessagingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;


import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class WelcomeMessageService {

    private static final String KEY_TEMPLATE = "welcome:groupid:%d:msgid:%d";          // value: msgId
    private static final String QUEUE_ZSET   = "welcome:delete-queue";                // member = key, score = executeAtMillis
    private static final Duration TTL   = Duration.ofMinutes(10);
    private static final Duration DELAY = Duration.ofMinutes(5);
    private static final Duration POLL_PERIOD = Duration.ofSeconds(10);
    private static final Pattern KEY_PATTERN = Pattern.compile("^welcome:groupid:(\\d+):msgid:(\\d+)$");

    private final RedisTemplate<String, Object> redisTemplate;
    private final TaskScheduler taskScheduler;
    private final MessagingService msg;

    public WelcomeMessageService(@Lazy MessagingService msg,
                                 TaskScheduler taskScheduler,
                                 RedisTemplate<String, Object> redisTemplate
    ) {
        this.msg = msg;
        this.redisTemplate = redisTemplate;
        this.taskScheduler = taskScheduler;
    }

    public void saveAndSchedule(Long groupId, Integer msgId) {
        String key = String.format(KEY_TEMPLATE, groupId, msgId);
        redisTemplate.opsForValue().set(key, msgId.longValue(), TTL);
        long executeAt = System.currentTimeMillis() + DELAY.toMillis();
        Boolean added = redisTemplate.opsForZSet().add(QUEUE_ZSET, key, executeAt);
        // debug log
        System.out.printf("Added to ZSET=%s, key=%s, score=%d, result=%s%n",
                QUEUE_ZSET, key, executeAt, added);
    }

    @PostConstruct
    void init() {
        processDueTasks(); // добираем хвосты после рестарта
        taskScheduler.scheduleAtFixedRate(this::processDueTasks, POLL_PERIOD);
    }

    private void processDueTasks() {
        long now = System.currentTimeMillis();
        Set<Object> due = redisTemplate.opsForZSet().rangeByScore(QUEUE_ZSET, 0, now);
        if (due == null || due.isEmpty()) return;

        for (Object obj : due) {
            String key = String.valueOf(obj);
            ParsedKey k = parseKey(key);
            if (k == null) {
                redisTemplate.opsForZSet().remove(QUEUE_ZSET, obj);
                continue;
            }
            try {
                msg.processMessage(new DeleteMessage(String.valueOf(k.groupId), k.msgId));
            } catch (Exception e) {
                System.err.printf("Failed to delete msg %d in %d: %s%n", k.msgId, k.groupId, e.getMessage());
            }

            // Убираем из очереди
            redisTemplate.opsForZSet().remove(QUEUE_ZSET, obj);
            // основное значение можно удалить вручную
            redisTemplate.delete(key);
        }
    }

    private ParsedKey parseKey(String key) {
        Matcher m = KEY_PATTERN.matcher(key);
        if (!m.matches()) return null;
        long g = Long.parseLong(m.group(1));
        int  mId = Integer.parseInt(m.group(2));
        return new ParsedKey(g, mId);
    }

    private record ParsedKey(long groupId, int msgId) {}
}