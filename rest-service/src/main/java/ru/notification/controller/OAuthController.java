package ru.notification.controller;

import com.google.api.client.auth.oauth2.Credential;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.notification.entity.AppUser;
import ru.notification.youtube.ChannelManager;
import ru.notification.youtube.OAuthManager;

@RestController
@RequestMapping("/oauth")
public class OAuthController {
    private final OAuthManager oAuthManager;
    private final ChannelManager channelManager;

    public OAuthController(OAuthManager oAuthManager, ChannelManager channelManager) {
        this.oAuthManager = oAuthManager;
        this.channelManager = channelManager;
    }

    @GetMapping("/youtube/callback")
    public ResponseEntity<?> authenticateYouTubeUser(@RequestParam String code,
                                                     @RequestParam String state) {
        Pair<AppUser, Credential> pair = oAuthManager.createAndStoreCredential(code, state).orElse(null);
        if(pair != null && channelManager.addSubscriptionsToNotifyFromChannel(pair.getFirst(), pair.getSecond())) {
            return ResponseEntity.ok().build();//Все каналы добавлены без проблем
        }
        return ResponseEntity.badRequest().build(); //Один или больше каналов не было добавлено
    }
}
