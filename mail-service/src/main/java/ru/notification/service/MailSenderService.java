package ru.notification.service;

import ru.notification.dto.MailParams;

public interface MailSenderService {
    void send(MailParams mailParams);
}
