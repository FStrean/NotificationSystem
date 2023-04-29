package ru.notification.controller;

import lombok.extern.log4j.Log4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.notification.dao.YouTubeChannelDAO;
import ru.notification.dto.YouTubeVideoParams;
import ru.notification.entity.YouTubeChannel;
import ru.notification.service.NotificationProducer;
import ru.notification.youtube.ChannelManager;

@Log4j
@RestController
@RequestMapping("/monitor/update")
public class WebhookUpdateReceivedController {
    private final YouTubeChannelDAO youTubeChannelDAO;
    private final NotificationProducer notificationProducer;
    private final ChannelManager channelManager;

    public WebhookUpdateReceivedController(YouTubeChannelDAO youTubeChannelDAO, NotificationProducer notificationProducer, ChannelManager channelManager) {
        this.youTubeChannelDAO = youTubeChannelDAO;
        this.notificationProducer = notificationProducer;
        this.channelManager = channelManager;
    }

    @PostMapping("/youtube")
    public ResponseEntity<?> onYouTubeUpdateReceived(@RequestBody YouTubeVideoParams youTubeVideoParams) {
        YouTubeChannel channel = youTubeChannelDAO.findByYoutubeChannelId(youTubeVideoParams.getChannelId()).orElse(null);
        if(channel == null) {
            log.error("monitor-service: Channel with id " + youTubeVideoParams.getChannelId() + " was not found");
            channelManager.unSubscribeFromPubSubHubbub(youTubeVideoParams.getChannelId());
        } else if (channel.getLastVideoId() == null || !channel.getLastVideoId().equals(youTubeVideoParams.getVideoId())) {
            channel.setLastVideoId(youTubeVideoParams.getVideoId());
            notificationProducer.produceNotification(youTubeChannelDAO.save(channel));
        }

        return ResponseEntity.ok().build();
    }
}
