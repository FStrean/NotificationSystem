package ru.notification.utils.xml.youtube;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class YouTubeXmlFeedHandler extends DefaultHandler {
    private static final String ENTRY = "entry";
    private static final String YT_VIDEO_ID = "yt:videoId";
    private static final String YT_CHANNEL_ID = "yt:channelId";

    private Feed feed;
    private StringBuilder elementValue;

    @Override
    public void characters(char[] ch, int start, int length) {
        if (elementValue == null) {
            elementValue = new StringBuilder();
        } else {
            elementValue.append(ch, start, length);
        }
    }

    @Override
    public void startDocument() {
        feed = new Feed();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attr) {
        switch (qName) {
            case ENTRY -> feed.setEntry(new Entry());
            case YT_VIDEO_ID, YT_CHANNEL_ID -> elementValue = new StringBuilder();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        switch (qName) {
            case YT_VIDEO_ID -> feed.getEntry().setYt_videoId(elementValue.toString());
            case YT_CHANNEL_ID -> feed.getEntry().setYt_channelId(elementValue.toString());
        }
    }

    public Feed getFeed() {
        return feed;
    }
}

