package ru.notification.youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SubscriptionListResponse;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
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
    @Value("${youtube.monitor.uri}")
    private String youtubeMonitorUri;
    @Value("${youtube.pubsubhubbub.uri}")
    private String pubSubHubbubUri;

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

    private MultiValueMap<String, String> getPubSubHubbubBody(String channelId) {
        MultiValueMap<String, String> body= new LinkedMultiValueMap<>();
        body.add("hub.callback", youtubeMonitorUri);
        body.add("hub.topic", "https://www.youtube.com/xml/feeds/videos.xml?channel_id=" + channelId);
        body.add("hub.verify", "async");

        return body;
    }

    private void sendPubSubHubbubRequest(MultiValueMap<String, String> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(pubSubHubbubUri, request , String.class);
    }

    public void subscribeToPubSubHubbub(String channelId) {
        MultiValueMap<String, String> body= getPubSubHubbubBody(channelId);
        body.add("hub.mode", "subscribe");

        sendPubSubHubbubRequest(body);
    }

    public void unSubscribeFromPubSubHubbub(String channelId) {
        MultiValueMap<String, String> body= getPubSubHubbubBody(channelId);
        body.add("hub.mode", "unsubscribe");

        sendPubSubHubbubRequest(body);
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
        Pair<String, String> channelIdAndName;
        try {
            channelIdAndName = channelUtils.getChannelIdAndNameFromUrl(url).orElse(null);
        } catch (IllegalArgumentException e) {
            return "Некорректная ссылка!";
        }
        if(channelIdAndName == null) {
            return "Ссылка ведёт не на YouTube канал!";
        }
        return addSubscriptionToNotify(appUser, channelIdAndName.getFirst(), channelIdAndName.getSecond());
    }

    private String addSubscriptionToNotify(AppUser appUser, String channelId, String channelName) throws IOException {
        YouTubeChannel persistentYoutubeChannel = findOrSaveYouTubeChannel(channelId, channelName);

        if(persistentYoutubeChannel.getAppUsers() == null) {
            persistentYoutubeChannel.setAppUsers(new HashSet<>());
        }
        persistentYoutubeChannel.getAppUsers().add(appUser);
        subscribeToPubSubHubbub(youtubeChannelDAO.save(persistentYoutubeChannel).getYoutubeChannelId());

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
