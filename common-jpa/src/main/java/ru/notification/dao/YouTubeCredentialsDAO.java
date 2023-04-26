package ru.notification.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.notification.entity.YouTubeCredentials;

public interface YouTubeCredentialsDAO extends JpaRepository<YouTubeCredentials, Long> {
}
