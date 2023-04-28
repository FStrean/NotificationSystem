package ru.notification.service;

import ru.notification.entity.YouTubeChannel;

public interface NotificationProducer {
    void produceNotification(YouTubeChannel channel);
}
