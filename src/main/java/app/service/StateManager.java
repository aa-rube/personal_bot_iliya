package app.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class StateManager {
    public final ConcurrentHashMap<Long, String> stringState = new ConcurrentHashMap<>();

    public void setStatus(Long chatId, String s) {
        stringState.put(chatId, s);
    }

    public boolean statusIs(Long chatId, String s) {
        return stringState.getOrDefault(chatId, "").equals(s);
    }

    public void remove(Long chatId) {
        stringState.remove(chatId);
    }
}