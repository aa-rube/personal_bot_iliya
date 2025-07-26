package app.model;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class MediaGroupData {
    private final Integer msgId;
    private final Long chatId;
    private final String mediaGroupId;

    private String caption;
    private List<MessageEntity> captionEntity;
    private Map<MediaType, List<String>> media;

    private LocalDateTime lastUpdate;
    private boolean isFinalized;

    private JSONObject startJson;

    private String botUserName;

    public MediaGroupData(Integer msgId, Long chatId, String mediaGroupId, JSONObject json, String userBotName) {
        this.msgId = msgId;
        this.chatId = chatId;
        this.mediaGroupId = mediaGroupId;

        this.lastUpdate = LocalDateTime.now();
        this.isFinalized = false;

        this.captionEntity = new ArrayList<>();
        this.startJson = json;

        this.botUserName = userBotName;

        this.media = Map.of(
                MediaType.IMG, new ArrayList<>(),
                MediaType.VIDEO, new ArrayList<>(),
                MediaType.VIDEO_NOTE, new ArrayList<>(),
                MediaType.DOC, new ArrayList<>(),
                MediaType.ANIMATION, new ArrayList<>(),
                MediaType.AUDIO, new ArrayList<>(),
                MediaType.VOICE, new ArrayList<>()
        );
    }
}