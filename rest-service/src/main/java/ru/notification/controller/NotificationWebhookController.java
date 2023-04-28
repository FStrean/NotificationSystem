package ru.notification.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.xml.sax.SAXException;
import ru.notification.dto.MailParams;
import ru.notification.dto.YouTubeVideoParams;
import ru.notification.utils.xml.youtube.Entry;
import ru.notification.utils.xml.youtube.Feed;
import ru.notification.utils.xml.youtube.YouTubeXmlFeedHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/notification")
public class NotificationWebhookController {
    @Value("${service.monitor.youtube.uri}")
    private String youTubeMonitorServiceUri;

    @ResponseBody
    @GetMapping("/youtube/callback")
    public String onSubscribe(@RequestParam Map<String, String> allRequestParams){
        return allRequestParams.get("hub.challenge");
    }
    @PostMapping(value = "/youtube/callback", consumes = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_ATOM_XML_VALUE, MediaType.APPLICATION_JSON_VALUE },
            produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_ATOM_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> onYoutubeVideoPublished(@RequestBody String update) throws ParserConfigurationException, SAXException, IOException {
        System.out.println(update);//TODO обрабатывать только запросы о новых видео(приходят уведомления об изменениях в видео)
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();

        InputStream stream = new ByteArrayInputStream(update.getBytes(StandardCharsets.UTF_8));

        YouTubeXmlFeedHandler youTubeXmlFeedHandler = new YouTubeXmlFeedHandler();
        saxParser.parse(stream, youTubeXmlFeedHandler);

        Feed feed = youTubeXmlFeedHandler.getFeed();
        Entry entry = feed.getEntry();

        String channelId = entry.getYt_channelId();
        String videoId = entry.getYt_videoId();

        var restTemplate = new RestTemplate();
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var youTubeVideoParams = YouTubeVideoParams.builder()
                .channelId(channelId)
                .videoId(videoId)
                .build();
        var request = new HttpEntity<>(youTubeVideoParams, headers);
        return restTemplate.exchange(youTubeMonitorServiceUri,
                HttpMethod.POST,
                request,
                String.class);
    }
}
