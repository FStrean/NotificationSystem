package ru.notification.service.impl;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.notification.service.NotificationProcessor;

import static ru.notification.model.RabbitQueue.YOUTUBE_NOTIFICATION_MESSAGE;

@Service
public class NotificationProcessorImpl implements NotificationProcessor {
    private final RabbitTemplate rabbitTemplate;

    public NotificationProcessorImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    @Override
    public void processNotification(SendMessage sendMessage) {
        rabbitTemplate.convertAndSend(YOUTUBE_NOTIFICATION_MESSAGE, sendMessage);
    }
}
