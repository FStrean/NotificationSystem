package ru.notification.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.notification.entity.YouTubeChannel;

import java.util.Optional;

public interface YouTubeChannelDAO extends JpaRepository<YouTubeChannel, Long> {
    Optional<YouTubeChannel> findByYoutubeChannelId(String channelId);
}
