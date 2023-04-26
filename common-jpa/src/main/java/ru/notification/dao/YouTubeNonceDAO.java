package ru.notification.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.notification.entity.YouTubeNonce;

import java.util.Optional;
import java.util.UUID;

public interface YouTubeNonceDAO extends JpaRepository<YouTubeNonce, UUID> {
    Optional<YouTubeNonce> findByNonceValue(UUID value);
}
