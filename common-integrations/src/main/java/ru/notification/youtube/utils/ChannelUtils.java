package ru.notification.youtube.utils;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import lombok.extern.log4j.Log4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@Log4j
public class ChannelUtils {
    private final YouTube apiEntry;

    public ChannelUtils(YouTube apiEntry) {
        this.apiEntry = apiEntry;
    }

    //TODO сделать проверку, чтобы проверялся хост, а то описанный здесь тег может быть и на других сайтах. В ChatGpt уже есть заготовка
    public Optional<String> getChannelIdFromUrl(String url) {
        Document document;
        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            return Optional.empty();
        }
        Element metaTag = document.select("meta[itemprop=channelId]").first();

        if (metaTag == null) {
            return Optional.empty();
        }

        return Optional.of(metaTag.attr("content"));
    }

    public Optional<String> getChannelName(String channelId) {
        ChannelListResponse channelListResponse;
        try {
            YouTube.Channels.List channelsListRequest = apiEntry.channels().list("snippet");
            channelsListRequest.setId(channelId);
            channelListResponse = channelsListRequest.execute();
        } catch (IOException e) {
            return Optional.empty();
        }
        List<Channel> channelList = channelListResponse.getItems();

        if (channelList.isEmpty()) {
            return Optional.empty();
        }

        ChannelSnippet channelSnippet = channelList.get(0).getSnippet();
        return Optional.of(channelSnippet.getTitle());
    }

    private String getUploadPlaylistId(String channelId) throws IOException {
        ChannelListResponse channelResponse = apiEntry.channels()
                    .list("contentDetails")
                    .setId(channelId)
                    .execute();
        List<Channel> channels = channelResponse.getItems();
        Channel channel = channels.get(0);
        ChannelContentDetails contentDetails = channel.getContentDetails();

        return contentDetails.getRelatedPlaylists().getUploads();
    }

    //Uses YouTube Data API to fetch last video
    public Optional<String> getLastVideoId(String channelId) {
        PlaylistItemListResponse playlistItemListResponse;
        try {
            var playListId = getUploadPlaylistId(channelId);
            playlistItemListResponse = apiEntry.playlistItems()
                    .list("contentDetails")
                    .setPlaylistId(playListId)
                    .setMaxResults(1L)
                    .execute();
        } catch (IOException e) {
            return Optional.empty();
        }
        List<PlaylistItem> playlistItems = playlistItemListResponse.getItems();
        if (playlistItems.isEmpty()) {
            return Optional.empty();
        }
        PlaylistItem playlistItem = playlistItems.get(0);

        return Optional.of(playlistItem.getContentDetails().getVideoId());
    }

    //Gets videos with parsing YouTube videos page
//    public Optional<String> getLastVideoId(String channelId) {
//        String channelUrl = "https://www.youtube.com/channel/" + channelId + "/videos";
//        Document document;
//        try {
//            document = Jsoup.connect(channelUrl).get();
//        } catch (IOException e) {
//            return Optional.empty();
//        }
//        String pageSource = document.outerHtml();
//        String videoIdTag = "\"videoId\":";
//        int index = pageSource.indexOf(videoIdTag);
//        if (index != -1) {
//            int endIndex = pageSource.indexOf("\"", index + videoIdTag.length() + 1);
//            if (endIndex != -1) {
//                String videoId = pageSource.substring(index + videoIdTag.length() + 1, endIndex);
//                return Optional.of(videoId);
//            }
//        }
//        return Optional.empty();
//    }
}
