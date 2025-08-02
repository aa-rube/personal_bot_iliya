package app.service;

import app.bot.api.MessagingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;

@Slf4j
@Service
public class WelcomeMessageService {

    private static final String KEY_TEMPLATE = "welcome:%d:%d";
    private static final String SCAN_PATTERN = "welcome:*:*";

    private static final Duration TTL = Duration.ofMinutes(10);
    private static final Duration DELETE_AFTER = Duration.ofMinutes(5);

    private final StringRedisTemplate redis;
    private final MessagingService msg;

    public WelcomeMessageService(
            StringRedisTemplate redis,
            @Lazy MessagingService msg) {
        this.redis = redis;
        this.msg = msg;
    }

    public void save(long chatId, int msgId) {
        String key = key(chatId, msgId);
        String value = System.currentTimeMillis() + "|" + chatId + "|" + msgId;

        redis.opsForValue().set(key, value, TTL);
        log.debug("Saved welcome key={} val={}", key, value);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onAppReady() {
        log.info("WelcomeMessageService: first sweep after startup");
        sweep();
    }

    @Scheduled(fixedDelay = 6000 * 3) // 3 минут в миллисекундах
    public void scheduledSweep() {
        sweep();
    }

    private void sweep() {
        long now = System.currentTimeMillis();

        // 1) non-blocking SCAN
        try (var cursor = redis.executeWithStickyConnection(conn ->
                conn.scan(ScanOptions.scanOptions().match(SCAN_PATTERN).count(500).build()))) {

            while (cursor.hasNext()) {
                String key = new String(cursor.next(), StandardCharsets.UTF_8);
                handleKey(now, key);
            }
            return;                         // SCAN успех
        } catch (Exception e) {
            log.warn("Redis SCAN failed, fallback to KEYS: {}", e.toString());
        }

        // 2) fallback KEYS (мало welcome-ключей → ок)
        Set<String> keys = redis.keys(SCAN_PATTERN);
        if (keys.isEmpty()) return;
        for (String key : keys) handleKey(now, key);
    }

    private void handleKey(long now, String key) {
        String val = redis.opsForValue().get(key);
        if (val == null) return;

        String[] parts = val.split("\\|", 3);
        if (parts.length < 3) {
            redis.delete(key);
            log.warn("Bad format, key removed: {}", key);
            return;
        }

        long ts, chatId;
        int msgId;
        try {
            ts = Long.parseLong(parts[0]);
            chatId = Long.parseLong(parts[1]);
            msgId = Integer.parseInt(parts[2]);
        } catch (NumberFormatException ex) {
            redis.delete(key);
            log.warn("Parse error, key removed: {} → {}", key, val);
            return;
        }

        if (now - ts < DELETE_AFTER.toMillis()) return; // ещё рано

        try {
            msg.process(new DeleteMessage(String.valueOf(chatId), msgId));
            redis.delete(key);
            log.debug("Deleted welcome message chatId={}, msgId={}", chatId, msgId);
        } catch (Exception e) {
            // оставляем ключ → повторим позже
            log.error("TG delete failed chatId={}, msgId={}, retry later. Err={}", chatId, msgId, e.toString(), e);
        }
    }

    private static String key(long chatId, int msgId) {
        return String.format(KEY_TEMPLATE, chatId, msgId);
    }
}
