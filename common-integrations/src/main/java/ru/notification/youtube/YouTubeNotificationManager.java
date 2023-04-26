package ru.notification.youtube;

import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import ru.notification.dao.YouTubeChannelDAO;
import ru.notification.entity.YouTubeChannel;
import ru.notification.youtube.utils.ChannelUtils;

import java.util.*;

@Service
@Log4j
public class YouTubeNotificationManager {
    private final ChannelUtils channelUtils;
    private final YouTubeChannelDAO youtubeChannelDAO;

    public YouTubeNotificationManager(ChannelUtils channelUtils, YouTubeChannelDAO youtubeChannelDAO) {
        this.channelUtils = channelUtils;
        this.youtubeChannelDAO = youtubeChannelDAO;
    }

    public List<YouTubeChannel> fetchNotifications() {
        List<YouTubeChannel> result = new ArrayList<>();
        youtubeChannelDAO.findAll().stream().toList().forEach(channel -> {
            if (isUpdated(channel)) {
                result.add(channel);
            }
        });

        return result;
    }

    private boolean isUpdated(YouTubeChannel youTubeChannel) {
        String channelId = youTubeChannel.getYoutubeChannelId();
        String latestVideoId = channelUtils.getLastVideoId(channelId).orElse(null);

        if (latestVideoId != null && !latestVideoId.equals(getStoredVideoId(youTubeChannel))) {
            storeVideoId(youTubeChannel, latestVideoId);
            return true;
        }

        return false;
    }
    private String getStoredVideoId(YouTubeChannel youTubeChannel) {
        return youTubeChannel.getLastVideoId();
    }

    private void storeVideoId(YouTubeChannel youTubeChannel, String videoId) {
        youTubeChannel.setLastVideoId(videoId);
        youtubeChannelDAO.save(youTubeChannel);
    }
}