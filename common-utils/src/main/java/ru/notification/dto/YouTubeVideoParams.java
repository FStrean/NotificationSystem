package ru.notification.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class YouTubeVideoParams {
    private String channelId;
    private String videoId;
}
