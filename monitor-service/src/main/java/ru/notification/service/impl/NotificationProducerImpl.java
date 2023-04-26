package ru.notification.service.impl;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.notification.service.NotificationProducer;

import static ru.notification.model.RabbitQueue.YOUTUBE_NOTIFICATION_MESSAGE;

@Service
public class NotificationProducerImpl implements NotificationProducer {
    private final RabbitTemplate rabbitTemplate;

    public NotificationProducerImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    @Override
    public void produceNotification(SendMessage sendMessage) {
        rabbitTemplate.convertAndSend(YOUTUBE_NOTIFICATION_MESSAGE, sendMessage);
    }
}
