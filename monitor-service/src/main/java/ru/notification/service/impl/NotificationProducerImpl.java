package ru.notification.service.impl;

import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import ru.notification.entity.AppUser;
import ru.notification.entity.YouTubeChannel;
import ru.notification.service.NotificationProcessor;
import ru.notification.service.NotificationProducer;

import java.util.List;

@Log4j
@Service
public class NotificationProducerImpl implements NotificationProducer {
    private final NotificationProcessor notificationProcessor;

    public NotificationProducerImpl(NotificationProcessor notificationProcessor) {
        this.notificationProcessor = notificationProcessor;
    }

    @Override
    public void produceNotification(YouTubeChannel channel) {
        channel.getAppUsers().forEach(user -> {
            log.debug("Monitor: " + channel.getChannelName() + " update");
            sendUpdateMessage(channel, user);
        });
    }

    private void sendUpdateMessage(YouTubeChannel channel, AppUser user) {
        var sendMessage = createUpdateMessage(channel, user);
        notificationProcessor.processNotification(sendMessage);
    }

    private SendMessage createUpdateMessage(YouTubeChannel channel, AppUser user) {
        var messagePart1 = "На канале ";
        var channelName = channel.getChannelName();
        var messagePart2 = " вышло новое видео:\n";
        var url = "https://www.youtube.com/watch?v=" + channel.getLastVideoId();

        var message = messagePart1 + channelName + messagePart2 + url;

        var messageEntity = new MessageEntity();
        messageEntity.setType("text_link");
        messageEntity.setText(channelName);
        messageEntity.setUrl(url);
        messageEntity.setOffset(messagePart1.length());
        messageEntity.setLength(channelName.length());

        var sendMessage = new SendMessage();
        sendMessage.setEntities(List.of(messageEntity));
        sendMessage.setChatId(user.getChatId());
        sendMessage.setText(message);

        return sendMessage;
    }
}
