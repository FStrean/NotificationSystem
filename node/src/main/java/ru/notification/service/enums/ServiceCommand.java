package ru.notification.service.enums;

import lombok.Getter;

@Getter
public enum ServiceCommand {
    HELP("/help", "показать все команды бота"),
    REGISTRATION("/registration", "регистрация пользователя"),
    CANCEL("/cancel", "отмена выполнения текущей команды"),
    START("/start", "активировать бота"),
    YOUTUBE_ALL("/youtube_all", "показать все каналы, о которых вы получаете уведомления"),
    YOUTUBE_ADD("/youtube_add", "добавить новый канал для получения уведомлений"),
    YOUTUBE_DELETE("/youtube_delete", "удалить канал для получения уведомлений"),
    YOUTUBE_AUTHORIZE("/youtube_authorize", "предоставить данные " +
            "о ваших подписках на youtube"),
    YOUTUBE_SUBSCRIPTIONS_UPDATE("/youtube_subscriptions_update", "автоматически обновлять данные " +
            "о каналах, о которых вы получаете уведомления, в соответствии " +
            "с вашими подписками на youtube"),
    YOUTUBE_SETTINGS ("/youtube_settings", "показать ваши настройки уведомлений на Youtube");

    private final String value;
    private final String description;
    ServiceCommand(String value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String toString() {
        return value;
    }

    public static ServiceCommand fromValue(String v) {
        for (ServiceCommand c: ServiceCommand.values()) {
            if(c.value.equals(v)) {
                return c;
            }
        }
        return null;
    }
}
