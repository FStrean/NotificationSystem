package ru.notification.youtube.configuration;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import ru.notification.youtube.OAuthManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Objects;

@Configuration
@PropertySource("classpath:common-integrations.properties")
public class YouTubeConfiguration {
    private static final String APPLICATION_NAME = "Notification";
    private static final String CLIENT_SECRETS_FILE = "/client_secret.json";
    private static final List<String> SCOPES = List.of("https://www.googleapis.com/auth/youtube.readonly");
    @Value("${youtube.api.key}")
    private String apiKey;

    @Bean
    public String getApplicationName() {
        return APPLICATION_NAME;
    }

    @Bean
    public JsonFactory getJsonFactory() {
        return JacksonFactory.getDefaultInstance();
    }

    @Bean
    public HttpTransport getHttpTransport() throws GeneralSecurityException, IOException {
        return GoogleNetHttpTransport.newTrustedTransport();
    }

    @Bean
    public YouTube getApiEntry(HttpTransport httpTransport, JsonFactory jsonFactory, String applicationName) {
        return new YouTube.Builder(httpTransport, jsonFactory, null)
                .setApplicationName(applicationName)
                .setYouTubeRequestInitializer(new YouTubeRequestInitializer(apiKey))
                .build();
    }

    @Bean
    public GoogleClientSecrets getClientSecrets() throws IOException {
        InputStream in = OAuthManager.class.getResourceAsStream(CLIENT_SECRETS_FILE);
        return GoogleClientSecrets.load(
                JacksonFactory.getDefaultInstance(), new InputStreamReader(Objects.requireNonNull(in)));
    }

    @Bean
    public GoogleAuthorizationCodeFlow getFlow(HttpTransport httpTransport, GoogleClientSecrets clientSecrets,
                                               JsonFactory jsonFactory) {
        return new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets, SCOPES)
                .build();
    }
}
