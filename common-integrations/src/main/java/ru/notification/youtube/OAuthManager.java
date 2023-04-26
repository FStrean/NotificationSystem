package ru.notification.youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.notification.dao.YouTubeCredentialsDAO;
import ru.notification.dao.YouTubeNonceDAO;
import ru.notification.entity.AppUser;
import ru.notification.entity.YouTubeCredentials;
import ru.notification.entity.YouTubeNonce;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
public class OAuthManager {
    @Value("${youtube-redirect-url}")
    private String youTubeRedirectUrl;
    private final GoogleAuthorizationCodeFlow flow;

    private final YouTubeNonceDAO youTubeNonceDAO;
    private final YouTubeCredentialsDAO youTubeCredentialsDAO;

    public OAuthManager(GoogleAuthorizationCodeFlow flow, YouTubeNonceDAO youTubeNonceDAO, YouTubeCredentialsDAO youTubeCredentialsDAO) {
        this.flow = flow;
        this.youTubeNonceDAO = youTubeNonceDAO;
        this.youTubeCredentialsDAO = youTubeCredentialsDAO;
    }

    public String getAuthorizeLink(AppUser appUser) {
        YouTubeNonce transientYouTubeNonce = generateYouTubeNonce(appUser);
        return flow.newAuthorizationUrl()
                .setRedirectUri(youTubeRedirectUrl)
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .setState(transientYouTubeNonce.getNonceValue().toString()).build();
    }

    private YouTubeNonce generateYouTubeNonce(AppUser appUser) {
        YouTubeNonce transientYouTubeNonce = YouTubeNonce.builder()
                .appUser(appUser)
                .build();
        return youTubeNonceDAO.save(transientYouTubeNonce);
    }

    @Transactional
    public Optional<Pair<AppUser, Credential>> createAndStoreCredential(String code, String state) {
        TokenResponse response;
        Credential credential;
        try {
            response = flow.newTokenRequest(code).setRedirectUri(youTubeRedirectUrl).execute();
            credential = flow.createAndStoreCredential(response, "userId");
        } catch (IOException e) {
            return Optional.empty();
        }

        AppUser appUser = getAppUserFromState(state);
        if(appUser == null) {
            return Optional.empty();
        }

        YouTubeCredentials transientYouTubeCredentials = YouTubeCredentials.builder()
                .accessToken(credential.getAccessToken())
                .refreshToken(credential.getRefreshToken())
                .appUser(appUser)
                .build();
        youTubeCredentialsDAO.save(transientYouTubeCredentials);

        return Optional.of(Pair.of(appUser, credential));
    }

    private AppUser getAppUserFromState(String state) {
        YouTubeNonce youTubeNonce = youTubeNonceDAO.findByNonceValue(UUID.fromString(state))
                .orElse(null);
        if(youTubeNonce == null) {
            return null;
        }
        AppUser appUser = youTubeNonce.getAppUser();
        youTubeNonceDAO.delete(youTubeNonce);
        return appUser;
    }
}
