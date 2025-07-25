package app.service;

import app.bot.api.MessagingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
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

    private final StringRedisTemplate redis;
    private final TaskScheduler taskScheduler;
    private final MessagingService msg;

    public WelcomeMessageService(@Lazy MessagingService msg,
                                 TaskScheduler taskScheduler,
                                 StringRedisTemplate redis) {
        this.msg = msg;
        this.redis = redis;
        this.taskScheduler = taskScheduler;
    }

    public void saveAndSchedule(Long groupId, Integer msgId) {
        log.info("Scheduling welcome message deletion: groupId={}, msgId={}", groupId, msgId);
        String key = String.format(KEY_TEMPLATE, groupId, msgId);
        redis.opsForValue().set(key, String.valueOf(msgId), TTL);
        long executeAt = System.currentTimeMillis() + DELAY.toMillis();
        Boolean added = redis.opsForZSet().add(QUEUE_ZSET, key, executeAt);
        log.info("ZADD {} {} {} => {}", QUEUE_ZSET, executeAt, key, added);
    }

    @PostConstruct
    void init() {
        log.info("Initializing WelcomeMessageService with TTL={}, DELAY={}, POLL_PERIOD={}", TTL, DELAY, POLL_PERIOD);
        processDueTasks(); // добираем хвосты после рестарта
        taskScheduler.scheduleAtFixedRate(this::processDueTasks, POLL_PERIOD);
        log.info("WelcomeMessageService initialized and scheduled");
    }

    private void processDueTasks() {
        long now = System.currentTimeMillis();
        Set<String> due = redis.opsForZSet().rangeByScore(QUEUE_ZSET, 0, now);
        if (due == null || due.isEmpty()) {
            log.trace("No due tasks found in queue");
            return;
        }

        log.info("Processing {} due tasks from queue", due.size());
        for (String obj : due) {
            String key = obj;
            ParsedKey k = parseKey(key);
            if (k == null) {
                log.warn("Invalid key format in queue, removing: {}", key);
                redis.opsForZSet().remove(QUEUE_ZSET, obj);
                continue;
            }
            
            log.info("Processing deletion for groupId={}, msgId={}", k.groupId, k.msgId);
            try {
                msg.processMessage(new DeleteMessage(String.valueOf(k.groupId), k.msgId));
                log.info("Successfully deleted welcome message: groupId={}, msgId={}", k.groupId, k.msgId);
            } catch (Exception e) {
                log.error("Failed to delete welcome message: groupId={}, msgId={}, error={}", k.groupId, k.msgId, e.getMessage(), e);
            }

            // Убираем из очереди
            redis.opsForZSet().remove(QUEUE_ZSET, obj);
            // основное значение можно удалить вручную
            redis.delete(key);
            log.info("Cleaned up queue and key for: {}", key);
        }
        log.info("Completed processing {} due tasks", due.size());
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