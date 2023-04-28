package ru.notification.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface NotificationProcessor {
    void processNotification(SendMessage sendMessage);
}
