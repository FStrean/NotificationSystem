package ru.notification.youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SubscriptionListResponse;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.notification.dao.AppUserDAO;
import ru.notification.dao.YouTubeChannelDAO;
import ru.notification.entity.AppUser;

import ru.notification.entity.YouTubeChannel;
import ru.notification.entity.enums.UserState;
import ru.notification.youtube.utils.ChannelUtils;

import java.io.IOException;
import java.util.HashSet;

@Service
@Log4j
public class ChannelManager {
    private final ChannelUtils channelUtils;
    private final YouTubeChannelDAO youtubeChannelDAO;
    private final AppUserDAO appUserDAO;
    private final NetHttpTransport httpTransport;
    private final JsonFactory jsonFactory;
    private final String applicationName;

    public ChannelManager(ChannelUtils channelUtils, YouTubeChannelDAO youtubeChannelDAO, AppUserDAO appUserDAO, NetHttpTransport httpTransport, JsonFactory jsonFactory, String applicationName) {
        this.channelUtils = channelUtils;
        this.youtubeChannelDAO = youtubeChannelDAO;
        this.appUserDAO = appUserDAO;
        this.httpTransport = httpTransport;
        this.jsonFactory = jsonFactory;
        this.applicationName = applicationName;
    }

    private YouTube getService(Credential credential) {
        return new YouTube.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(applicationName)
                .build();
    }

    @Transactional
    public boolean addSubscriptionsToNotifyFromChannel(AppUser appUser, Credential credential) {
        YouTube youtubeService = getService(credential);
        YouTube.Subscriptions.List request;
        try {
            request = youtubeService.subscriptions().list("snippet");
        } catch (IOException e) {
            return false;
        }
        request.setMine(true);
        request.setMaxResults(50L);
        boolean isAllSuccessful = true;

        String nextPageToken = "";
        do {
            request.setPageToken(nextPageToken);
            SubscriptionListResponse response;
            try {
                response = request.execute();
            } catch (IOException e) {
                isAllSuccessful = false;
                continue;
            }

            for(var subscription : response.getItems()) {
                try {
                    addSubscriptionToNotify(appUser, subscription.getSnippet().getResourceId().getChannelId(), subscription.getSnippet().getTitle());
                } catch (IOException e) {
                    isAllSuccessful = false;
                    log.error(e);
                }
            }

            nextPageToken = response.getNextPageToken();
        } while (nextPageToken != null);

        return isAllSuccessful;
    }

    @Transactional
    public String addSubscriptionToNotifyFromUrl(AppUser appUser, String url) throws IOException {
        String channelId;
        try {
            channelId = channelUtils.getChannelIdFromUrl(url).orElse(null);
        } catch (IOException e) {
            return "Некорректная ссылка!";
        }
        if(channelId == null) {
            return "Ссылка ведёт не на YouTube канал!";
        }
        return addSubscriptionToNotify(appUser, channelId);
    }

    private String addSubscriptionToNotify(AppUser appUser, String channelId) throws IOException {
        String channelName = channelUtils.getChannelName(channelId).orElse(null);
        if(channelName == null) {
            log.error("Can't get channel name with that channel id: " + channelId);
            return "Произошла ошибка на стороне сервера, пожалуйста свяжитесь с поддержкой!";
        }
        return addSubscriptionToNotify(appUser, channelId, channelName);
    }
    private String addSubscriptionToNotify(AppUser appUser, String channelId, String channelName) throws IOException {
        YouTubeChannel persistentYoutubeChannel = findOrSaveYouTubeChannel(channelId, channelName);

        if(persistentYoutubeChannel.getAppUsers() == null) {
            persistentYoutubeChannel.setAppUsers(new HashSet<>());
        }
        persistentYoutubeChannel.getAppUsers().add(appUser);
        youtubeChannelDAO.save(persistentYoutubeChannel);

        appUser.setState(UserState.BASIC_STATE);
        appUserDAO.save(appUser);

        return "Успех! Теперь вы будете получать уведомления об этом канале!";
    }

    private YouTubeChannel findOrSaveYouTubeChannel(String channelId, String channelName) throws IOException {
        var optional = youtubeChannelDAO.findByYoutubeChannelId(channelId);
        if(optional.isEmpty()) {
            var latestVideoId = channelUtils.getLastVideoId(channelId).orElse(null);

            YouTubeChannel transientYoutubeChannel = YouTubeChannel.builder()
                    .channelName(channelName)
                    .youtubeChannelId(channelId)
                    .lastVideoId(latestVideoId)
                    .build();

            return youtubeChannelDAO.save(transientYoutubeChannel);
        }

        return optional.get();
    }
}
