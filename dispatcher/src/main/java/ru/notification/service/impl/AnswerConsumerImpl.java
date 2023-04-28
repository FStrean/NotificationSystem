package ru.notification.service.impl;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.notification.controller.UpdateProcessor;
import ru.notification.service.AnswerConsumer;

import static ru.notification.model.RabbitQueue.ANSWER_MESSAGE;
import static ru.notification.model.RabbitQueue.YOUTUBE_NOTIFICATION_MESSAGE;

@Service
public class AnswerConsumerImpl implements AnswerConsumer {
    private final UpdateProcessor updateProcessor;
    public AnswerConsumerImpl(UpdateProcessor updateProcessor) {
        this.updateProcessor = updateProcessor;
    }

    @Override
    @RabbitListener(queues = {ANSWER_MESSAGE, YOUTUBE_NOTIFICATION_MESSAGE})
    public void consume(SendMessage sendMessage) {
        updateProcessor.setView(sendMessage);
    }
}
