package app.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Configuration
public class GoogleSheetsConfig {

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private final AppConfig app;   // весь доступ через бин с already-cached byte[]

    public GoogleSheetsConfig(AppConfig appConfig) {
        this.app = appConfig;
    }

    /** Singleton-бин Sheets (scope = spreadsheets.readonly / read-write) */
    @Bean
    public Sheets googleSheetsService() throws IOException, GeneralSecurityException {
        try (InputStream in = app.getCredentialsStream()) {
            GoogleCredential cred = GoogleCredential.fromStream(in)
                    .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

            return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    cred)
                    .setApplicationName(app.getApplicationName())
                    .build();
        }
    }

    /** Singleton-бин Drive, созданный из тех же credentials */
    @Bean
    public Drive driveClient() throws IOException, GeneralSecurityException {
        GoogleCredentials creds;
        try (InputStream in = app.getCredentialsStream()) {
            creds = ServiceAccountCredentials.fromStream(in)
                    .createScoped(List.of(DriveScopes.DRIVE));
        }

        HttpRequestInitializer rqInit = new HttpCredentialsAdapter(creds);

        return new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                rqInit)
                .setApplicationName(app.getApplicationName())
                .build();
    }
}
