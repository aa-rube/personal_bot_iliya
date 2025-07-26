package app.model;

import app.bot.telegramdata.TelegramDataAutoMessage;
import app.data.MediaType;
import app.util.MailingGroupMediaMessageBuilder;
import app.util.UpdateNameExtractor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "welcome_message")
public class AutoMessage {

    //admins data
    @Id
    private Long id;
    private String name;

    //use for telegram message
    private String text;
    private List<MessageEntity> textEntities;

    private String caption;
    private List<MessageEntity> captionEntities;

    private List<String> imgFilesId;
    private List<String> gifFilesId;
    private List<String> vidFilesId;
    private List<String> audioFilesId;
    private List<String> voiceFilesId;
    private List<String> videoNoteFilesId;
    private List<String> fileFilesId;

    public AutoMessage(String content, List<MessageEntity> entities, Map<MediaType, List<String>> files) {
        this.id = 123456789L;
        this.name = "welcome_message";

        if (files == null) {
            this.text = content.length() > 4096 ? content.substring(0, 4096) : content;
            this.textEntities = entities;

            this.imgFilesId = new ArrayList<>();
            this.vidFilesId = new ArrayList<>();
            this.audioFilesId = new ArrayList<>();
            this.voiceFilesId = new ArrayList<>();
            this.videoNoteFilesId = new ArrayList<>();
            this.fileFilesId = new ArrayList<>();
            this.gifFilesId = new ArrayList<>();
        } else {
            this.caption = content.length() > 1024 ? content.substring(0, 1024) : content;
            this.captionEntities = entities;

            this.imgFilesId = files.getOrDefault(MediaType.IMG, new ArrayList<>());
            this.vidFilesId = files.getOrDefault(MediaType.VIDEO, new ArrayList<>());
            this.audioFilesId = files.getOrDefault(MediaType.AUDIO, new ArrayList<>());
            this.voiceFilesId = files.getOrDefault(MediaType.VOICE, new ArrayList<>());
            this.videoNoteFilesId = files.getOrDefault(MediaType.VIDEO_NOTE, new ArrayList<>());
            this.fileFilesId = files.getOrDefault(MediaType.DOC, new ArrayList<>());
            this.gifFilesId = files.getOrDefault(MediaType.ANIMATION, new ArrayList<>());
        }
    }

    private String getContent() {
        return text == null ? caption : text;
    }

    public int mediaDataCount() {
        return imgFilesId.size() + vidFilesId.size() + audioFilesId.size() +
                voiceFilesId.size() + videoNoteFilesId.size() + fileFilesId.size();
    }

    public Object getMessages(Long chatId, Update update, User user) {
        String content = getContent()
                .replaceAll("\\[username]", user == null ? "test" : UpdateNameExtractor.userExtractName(user))
                .replaceAll("\\[title]", user == null ? "test" : UpdateNameExtractor.extractGroupTitleName(update));

        if (mediaDataCount() == 0) {
            return TelegramDataAutoMessage.getSendMessage(chatId, content, textEntities);
        }

        if (imgFilesId.size() > 1 ||
                vidFilesId.size() > 1 ||
                audioFilesId.size() > 1 ||
                voiceFilesId.size() > 1 ||
                videoNoteFilesId.size() > 1 ||
                fileFilesId.size() > 1
        ) {
            List<InputMedia> inputMedia = MailingGroupMediaMessageBuilder.getInputMedia(
                    imgFilesId,
                    vidFilesId,
                    audioFilesId,
                    voiceFilesId,
                    videoNoteFilesId,
                    fileFilesId,
                    gifFilesId
            );

            int batchSize = 10;
            List<InputMedia> inputMediaLessThanNine = inputMedia.subList(0, Math.min(batchSize, inputMedia.size()));
            return MailingGroupMediaMessageBuilder.getMediaGroupMessage(
                    chatId,
                    inputMediaLessThanNine,
                    content
            );
        }

        if (imgFilesId.size() == 1) {
            return TelegramDataAutoMessage.getSendPhoto(chatId, content, captionEntities, null, imgFilesId.getFirst());
        }

        if (vidFilesId.size() == 1) {
            return TelegramDataAutoMessage.getSendVideo(chatId, content, captionEntities, null, vidFilesId.getFirst());
        }

        if (audioFilesId.size() == 1) {
            return TelegramDataAutoMessage.getSendAudio(chatId, content, captionEntities, null, audioFilesId.getFirst());
        }

        if (voiceFilesId.size() == 1) {
            return TelegramDataAutoMessage.getSendVoice(chatId, content, captionEntities, null, voiceFilesId.getFirst());
        }

        if (videoNoteFilesId.size() == 1) {
            return TelegramDataAutoMessage.getSendVideoNote(chatId, null, videoNoteFilesId.getFirst());
        }

        if (fileFilesId.size() == 1) {
            return TelegramDataAutoMessage.getSendDocument(chatId, content, captionEntities, null, fileFilesId.getFirst());
        }

        if (gifFilesId.size() == 1) {
            return TelegramDataAutoMessage.getSendAnimation(chatId, content, captionEntities, null, gifFilesId.getFirst());
        }
        return null;
    }
}