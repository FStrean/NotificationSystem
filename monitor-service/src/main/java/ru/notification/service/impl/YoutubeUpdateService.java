package ru.notification.service.impl;

import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.notification.dao.YouTubeChannelDAO;
import ru.notification.entity.YouTubeChannel;
import ru.notification.service.NotificationProducer;
import ru.notification.service.UpdateService;
import ru.notification.youtube.YouTubeNotificationManager;

import java.util.List;


@Service
@Log4j
public class YoutubeUpdateService implements UpdateService {
    private final YouTubeNotificationManager youtubeNotificationManager;
    private final NotificationProducer notificationProducer;
    private final YouTubeChannelDAO youTubeChannelDAO;
    @Value("${youtube.monitor.uri}")
    private String youtubeMonitorUri;
    @Value("${pubsubhubbub.uri}")
    private String pubSubHubbubUri;

    public YoutubeUpdateService(YouTubeNotificationManager youtubeNotificationManager, NotificationProducer notificationProducer, YouTubeChannelDAO youTubeChannelDAO) {
        this.youtubeNotificationManager = youtubeNotificationManager;
        this.notificationProducer = notificationProducer;
        this.youTubeChannelDAO = youTubeChannelDAO;
    }

    @Scheduled(fixedDelayString = "${pubsubhubbub.youtube.resub.interval}")
    public void updateYouTubePubSubHubbubSubscriptions() {
        youTubeChannelDAO.findAll().forEach(channel -> {
            MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
            map.add("hub.callback", youtubeMonitorUri);
            map.add("hub.topic", "https://www.youtube.com/xml/feeds/videos.xml?channel_id=" + channel.getYoutubeChannelId());
            map.add("hub.verify", "async");
            map.add("hub.mode", "subscribe");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            restTemplate.postForEntity(pubSubHubbubUri, request , String.class);
        });
    }

    @Override
    //@Scheduled(fixedRateString = "${youtube.update.interval}")
    @Transactional
    public void update() {
        List<YouTubeChannel> updates = youtubeNotificationManager.fetchNotifications();
        updates.forEach(notificationProducer::produceNotification);
    }
}
