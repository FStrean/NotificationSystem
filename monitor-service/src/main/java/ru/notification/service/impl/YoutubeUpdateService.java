package ru.notification.service.impl;

import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.notification.dao.YouTubeChannelDAO;
import ru.notification.entity.YouTubeChannel;
import ru.notification.service.NotificationProducer;
import ru.notification.service.UpdateService;
import ru.notification.youtube.ChannelManager;
import ru.notification.youtube.YouTubeNotificationManager;

import java.util.List;


@Service
@Log4j
public class YoutubeUpdateService implements UpdateService {
    private final YouTubeNotificationManager youtubeNotificationManager;
    private final NotificationProducer notificationProducer;
    private final YouTubeChannelDAO youTubeChannelDAO;
    private final ChannelManager channelManager;

    public YoutubeUpdateService(YouTubeNotificationManager youtubeNotificationManager, NotificationProducer notificationProducer, YouTubeChannelDAO youTubeChannelDAO, ChannelManager channelManager) {
        this.youtubeNotificationManager = youtubeNotificationManager;
        this.notificationProducer = notificationProducer;
        this.youTubeChannelDAO = youTubeChannelDAO;
        this.channelManager = channelManager;
    }

    @Scheduled(fixedDelayString = "${pubsubhubbub.youtube.resub.interval}")
    public void updateYouTubePubSubHubbubSubscriptions() {
        youTubeChannelDAO.findAll().forEach(channel -> channelManager.subscribeToPubSubHubbub(channel.getYoutubeChannelId()));
    }

    @Override
    //@Scheduled(fixedRateString = "${youtube.update.interval}")
    @Transactional
    public void update() {
        List<YouTubeChannel> updates = youtubeNotificationManager.fetchNotifications();
        updates.forEach(notificationProducer::produceNotification);
    }
}
