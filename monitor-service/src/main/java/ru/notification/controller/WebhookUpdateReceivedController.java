package ru.notification.controller;

import lombok.extern.log4j.Log4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.notification.dao.YouTubeChannelDAO;
import ru.notification.dto.YouTubeVideoParams;
import ru.notification.entity.YouTubeChannel;
import ru.notification.service.NotificationProducer;

@Log4j
@RestController
@RequestMapping("/monitor/update")
public class WebhookUpdateReceivedController {
    private final YouTubeChannelDAO youTubeChannelDAO;
    private final NotificationProducer notificationProducer;

    public WebhookUpdateReceivedController(YouTubeChannelDAO youTubeChannelDAO, NotificationProducer notificationProducer) {
        this.youTubeChannelDAO = youTubeChannelDAO;
        this.notificationProducer = notificationProducer;
    }

    //TODO если канал не был найден, значит по какой-то причине, когда от него отписались все пользователи, но мы не отписались от вебхука
    @PostMapping("/youtube")
    public ResponseEntity<?> onYouTubeUpdateReceived(@RequestBody YouTubeVideoParams youTubeVideoParams) {
        YouTubeChannel channel = youTubeChannelDAO.findByYoutubeChannelId(youTubeVideoParams.getChannelId()).orElse(null);
        if(channel != null && (channel.getLastVideoId() == null || !channel.getLastVideoId().equals(youTubeVideoParams.getVideoId()))) {
            channel.setLastVideoId(youTubeVideoParams.getVideoId());
            notificationProducer.produceNotification(youTubeChannelDAO.save(channel));
            return ResponseEntity.ok().build();
        }
        log.debug("Some error");
        return ResponseEntity.internalServerError().build();
    }
}
