package app.service;

import app.bot.api.MessagingService;
import app.model.AutoMessage;
import app.data.MediaType;
import app.repository.AutoMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.*;

@Service
public class BuildAutoMessageService {

    private static final Logger log = LoggerFactory.getLogger(BuildAutoMessageService.class);

    public final String TEXT_EXCEPTION =
            "Сообщение без вложений не может быть длиннее 4096 символов\n\n!Гипер-ссылки не учитываются в длине сообщения.";

    private final AutoMessageRepository repo;
    private final MessagingService msg;

    public BuildAutoMessageService(@Lazy MessagingService msg,
                                   AutoMessageRepository repo) {
        this.msg = msg;
        this.repo = repo;
    }

    private void save(AutoMessage am) {
        repo.save(am);
    }

    public void getOrAwaitScheduleMessage(Long chatId, Message message) {
        log.debug("Incoming message msgId={}, chatId={}, mediaGroupId={}",
                message.getMessageId(), message.getChatId(), message.getMediaGroupId());

        // CASE 1: Text message
        if (message.hasText() && message.getMediaGroupId() == null) {
            if (message.getText().length() > 4096) {
                msg.process(new SendMessage(String.valueOf(chatId), TEXT_EXCEPTION));
                return;
            }

            try {
                AutoMessage am = new AutoMessage(
                        message.getText(),
                        message.getEntities(),
                        null
                );
                save(am);
            } catch (Exception e) {
                msg.process(new SendMessage(String.valueOf(chatId), e.getMessage()));
            }
            return;
        }

        // CASE 2: Single media
        if (message.getMediaGroupId() == null) {
            try {

                Map<MediaType, List<String>> mediaMap = extractSingleMedia(message);
                if (!mediaMap.isEmpty()) {
                    AutoMessage am = new AutoMessage(
                            message.getCaption(),
                            message.getCaptionEntities(),
                            mediaMap
                    );

                    save(am);
                }
            } catch (Exception e) {
                msg.process(new SendMessage(String.valueOf(chatId), e.getMessage()));
            }
        }
    }

    private Map<MediaType, List<String>> extractSingleMedia(Message message) {
        Map<MediaType, List<String>> map = new HashMap<>();
        try {
            if (message.hasPhoto()) {
                map.computeIfAbsent(MediaType.IMG, k -> new ArrayList<>())
                        .add(message.getPhoto().getLast().getFileId());
            }
            if (message.hasVideo()) {
                map.computeIfAbsent(MediaType.VIDEO, k -> new ArrayList<>())
                        .add(message.getVideo().getFileId());
            }
            if (message.hasVoice()) {
                map.computeIfAbsent(MediaType.VOICE, k -> new ArrayList<>())
                        .add(message.getVoice().getFileId());
            }
            if (message.hasVideoNote()) {
                map.computeIfAbsent(MediaType.VIDEO_NOTE, k -> new ArrayList<>())
                        .add(message.getVideoNote().getFileId());
            }
            if (message.hasAudio()) {
                map.computeIfAbsent(MediaType.AUDIO, k -> new ArrayList<>())
                        .add(message.getAudio().getFileId());
            }
            if (message.hasDocument()) {
                map.computeIfAbsent(MediaType.DOC, k -> new ArrayList<>())
                        .add(message.getDocument().getFileId());
            }
            if (message.hasAnimation()) {
                map.computeIfAbsent(MediaType.ANIMATION, k -> new ArrayList<>())
                        .add(message.getAnimation().getFileId());
            }
        } catch (Exception e) {
            log.error("Error extracting media: {}", e.getMessage());
        }
        return map;
    }

    public Object getAutoMsg(Long sendInChatId, Update update, User u, int threadId) {
        try {
            Optional<AutoMessage> oam = repo.findById(123456789L);
            return oam.map(autoMessage -> autoMessage.getMessages(sendInChatId, update, u, threadId)).orElse(null);
        } catch (Exception e) {
            log.error("Error finding message: {}", e.getMessage());
            return Optional.empty();
        }
    }
}