package ru.notification.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"id"})
@AllArgsConstructor
@Entity
@Table(name = "youtube_credentials")
public class YouTubeCredentials {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String accessToken;
    private String refreshToken;
    @OneToOne
    private AppUser appUser;
}
