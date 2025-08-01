package app.util;

import app.bot.telegramdata.TelegramData;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MailingGroupMediaMessageBuilder {

    /**
     * Constructs a media group message for a given chat.
     *
     * @param chatId   the ID of the chat where the message will be sent
     * @param caption  the caption to be added to the first media item
     * @param threadId the topic id
     * @return the constructed media group message object
     */
    public static Object getMediaGroupMessage(Long chatId, List<InputMedia> media,
                                              String caption, int threadId) {
        log.debug("Creating media group message for chatId: {}", chatId);

        if (media.isEmpty()) {
            log.warn("No media found in MediaGroupData for chatId: {}", chatId);
            return null;
        }

        try {
            InputMedia firstMedia = media.getFirst();
            firstMedia.setCaption(caption);
            log.info("Setting caption and parse mode for the first media item");
            Object message = TelegramData.getSendMediaGroupMsg(chatId, media, threadId);
            log.info("Media group message created successfully for chatId: {}", chatId);

            return message;
        } catch (Exception e) {
            log.error("Error while creating media group message for chatId: {}: {}", chatId, e.getMessage(), e);
            try {
                InputMedia firstMedia = media.getFirst();
                firstMedia.setCaption(caption);
                Object message = TelegramData.getSendMediaGroupMsg(chatId, media, threadId);
                log.info("Media group message created successfully after handling exception for chatId: {}", chatId);

                return message;
            } catch (Exception ex) {
                log.error("Failed to create media group message after retry for chatId: {}: {}", chatId, ex.getMessage(), ex);
                throw ex;
            }
        }
    }


    public static List<InputMedia> getInputMedia(
            List<String> img,
            List<String> vid,
            List<String> aud,
            List<String> voice,
            List<String> videoNote,
            List<String> files,
            List<String> gifs, int threadId) {

        log.debug("Converting MediaGroupData to InputMedia list");
        List<InputMedia> media = new ArrayList<>();

        if (!img.isEmpty()) {
            log.info("Processing {} photo(s)", img.size());
            for (String fileId : img) {
                InputMediaPhoto inputMedia = new InputMediaPhoto();
                inputMedia.setMedia(fileId);
                media.add(inputMedia);
                log.debug("Added photo media: {}", fileId);
            }
        } else {
            log.debug("No photos to add");
        }

        if (!vid.isEmpty()) {
            log.info("Processing {} video(s)", vid.size());
            for (String fileId : vid) {
                InputMediaVideo inputMedia = new InputMediaVideo();
                inputMedia.setMedia(fileId);
                media.add(inputMedia);
                log.debug("Added video media: {}", fileId);
            }
        } else {
            log.debug("No videos to add");
        }

        if (!aud.isEmpty()) {
            log.info("Processing {} audio(s)", aud.size());
            for (String fileId : aud) {
                InputMediaVideo inputMedia = new InputMediaVideo();
                inputMedia.setMedia(fileId);
                media.add(inputMedia);
                log.debug("Added audio media: {}", fileId);
            }
        } else {
            log.debug("No audio to add");
        }

        if (!voice.isEmpty()) {
            log.info("Processing {} voice(s)", aud.size());
            for (String fileId : voice) {
                InputMediaVideo inputMedia = new InputMediaVideo();
                inputMedia.setMedia(fileId);
                media.add(inputMedia);
                log.debug("Added voice media: {}", voice);
            }
        } else {
            log.debug("No voice to add");
        }

        if (!videoNote.isEmpty()) {
            log.info("Processing {} videoNote(s)", aud.size());
            for (String fileId : videoNote) {
                InputMediaVideo inputMedia = new InputMediaVideo();
                inputMedia.setMedia(fileId);
                media.add(inputMedia);
                log.debug("Added video Note media: {}", voice);
            }
        } else {
            log.debug("No videoNote to add");
        }

        if (!files.isEmpty()) {
            log.info("Processing {} files(s)", aud.size());
            for (String fileId : files) {
                InputMediaVideo inputMedia = new InputMediaVideo();
                inputMedia.setMedia(fileId);
                media.add(inputMedia);
                log.debug("Added files media: {}", voice);
            }
        } else {
            log.debug("No files to add");
        }

        if (!gifs.isEmpty()) {
            log.info("Processing {} files(s)", aud.size());
            for (String fileId : gifs) {
                InputMediaVideo inputMedia = new InputMediaVideo();
                inputMedia.setMedia(fileId);
                media.add(inputMedia);
                log.debug("Added gif media: {}", gifs);
            }
        } else {
            log.debug("No files to add");
        }

        log.info("Total media items added: {}", media.size());
        return media;
    }
}
