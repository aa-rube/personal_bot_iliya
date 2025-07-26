package app.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class StateManager {
    public final ConcurrentHashMap<Long, String> editWelcomeMessage = new ConcurrentHashMap<>();
}