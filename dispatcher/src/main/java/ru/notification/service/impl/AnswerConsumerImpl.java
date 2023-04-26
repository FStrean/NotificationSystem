package ru.notification.service.impl;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.notification.controller.UpdateController;
import ru.notification.service.AnswerConsumer;

import static ru.notification.model.RabbitQueue.ANSWER_MESSAGE;
import static ru.notification.model.RabbitQueue.YOUTUBE_NOTIFICATION_MESSAGE;

@Service
public class AnswerConsumerImpl implements AnswerConsumer {
    private final UpdateController updateController;
    public AnswerConsumerImpl(UpdateController updateController) {
        this.updateController = updateController;
    }

    @Override
    @RabbitListener(queues = {ANSWER_MESSAGE, YOUTUBE_NOTIFICATION_MESSAGE})
    public void consume(SendMessage sendMessage) {
        updateController.setView(sendMessage);
    }
}
