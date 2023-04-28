package ru.notification.youtube.utils;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import lombok.extern.log4j.Log4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.data.util.Pair;
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
    //TODO (В данный момент это никак не сделать) не самый надежный метод получения id и возможно не самый быстрый,
    // так как приходится загружать полностью html код страницы, было бы неплохо переделать
    public Optional<Pair<String, String>> getChannelIdAndNameFromUrl(String url) throws IOException {
        Document document = Jsoup.connect(url).get();
        Element metaTagForId = document.select("meta[itemprop=channelId]").first();
        Element metaTagForName = document.select("meta[itemprop=name]").first();

        if (metaTagForId == null || metaTagForName == null) {
            return Optional.empty();
        }

        return Optional.of(Pair.of(metaTagForId.attr("content"), metaTagForName.attr("content")));
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
//    public Optional<String> getLastVideoId(String channelId) throws IOException {
//        var playListId = getUploadPlaylistId(channelId);
//        PlaylistItemListResponse playlistItemListResponse = apiEntry.playlistItems()
//                .list("contentDetails")
//                .setPlaylistId(playListId)
//                .setMaxResults(1L)
//                .execute();
//        List<PlaylistItem> playlistItems = playlistItemListResponse.getItems();
//        if (playlistItems.isEmpty()) {
//            return Optional.empty();
//        }
//        PlaylistItem playlistItem = playlistItems.get(0);
//
//        return Optional.of(playlistItem.getContentDetails().getVideoId());
//    }

    //Gets videos with parsing YouTube videos page
    public Optional<String> getLastVideoId(String channelId) throws IOException {
        String channelUrl = "https://www.youtube.com/channel/" + channelId + "/videos";
        Document document;
        document = Jsoup.connect(channelUrl).get();
        String pageSource = document.outerHtml();
        String videoIdTag = "\"videoId\":";
        int index = pageSource.indexOf(videoIdTag);
        if (index != -1) {
            int endIndex = pageSource.indexOf("\"", index + videoIdTag.length() + 1);
            if (endIndex != -1) {
                String videoId = pageSource.substring(index + videoIdTag.length() + 1, endIndex);
                return Optional.of(videoId);
            }
        }
        return Optional.empty();
    }
}
