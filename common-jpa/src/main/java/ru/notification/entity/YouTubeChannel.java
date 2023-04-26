package ru.notification.entity;

import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(exclude = {"id", "appUsers"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "youtube_channel")
public class YouTubeChannel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String youtubeChannelId;
    private String channelName;
    private String lastVideoId;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "youtube_channel_user_data",
            joinColumns = @JoinColumn(name = "youtube_channel_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "app_user_id", referencedColumnName = "id"))
    private Set<AppUser> appUsers = new HashSet<>();
}