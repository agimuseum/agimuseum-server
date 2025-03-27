package com.agimuseum.magi.util;

import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class FirebaseCredentialsHelper {

    private static final List<String> FIREBASE_SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/cloud-platform",
            "https://www.googleapis.com/auth/firebase.database",
            "https://www.googleapis.com/auth/firebase.messaging",
            "https://www.googleapis.com/auth/firebase.storage",
            "https://www.googleapis.com/auth/datastore"
    );

    private static final List<String> STORAGE_SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/cloud-platform",
            "https://www.googleapis.com/auth/devstorage.full_control",
            "https://www.googleapis.com/auth/devstorage.read_write"
    );

    /**
     * Load and create Google credentials with Firebase scopes
     *
     * @param configPath Path to the Firebase service account file
     * @return GoogleCredentials with Firebase scopes
     * @throws IOException if the credentials file cannot be loaded
     */
    public static GoogleCredentials getFirebaseCredentials(String configPath) throws IOException {
        InputStream serviceAccount = getServiceAccountStream(configPath);
        return GoogleCredentials.fromStream(serviceAccount).createScoped(FIREBASE_SCOPES);
    }

    /**
     * Load and create Google credentials with Storage scopes
     *
     * @param configPath Path to the Firebase service account file
     * @return GoogleCredentials with Storage scopes
     * @throws IOException if the credentials file cannot be loaded
     */
    public static GoogleCredentials getStorageCredentials(String configPath) throws IOException {
        InputStream serviceAccount = getServiceAccountStream(configPath);
        return GoogleCredentials.fromStream(serviceAccount).createScoped(STORAGE_SCOPES);
    }

    /**
     * Get the service account input stream from either classpath or file system
     *
     * @param configPath Path to the service account file
     * @return InputStream for the service account file
     * @throws IOException if the file cannot be loaded
     */
    private static InputStream getServiceAccountStream(String configPath) throws IOException {
        // Try using ClassPathResource first
        Resource resource = new ClassPathResource(configPath);

        if (resource.exists()) {
            log.info("Loading Firebase configuration from classpath");
            return resource.getInputStream();
        } else {
            // Try as a file path
            log.info("Loading Firebase configuration from file system");
            return new FileInputStream(configPath);
        }
    }

    /**
     * Validates Firebase credentials by forcing a token refresh
     *
     * @param credentials GoogleCredentials to validate
     * @return true if the credentials are valid
     */
    public static boolean validateCredentials(GoogleCredentials credentials) {
        try {
            // Force a token refresh to test validity
            credentials.refreshIfExpired();
            return true;
        } catch (IOException e) {
            log.error("Failed to refresh credentials: {}", e.getMessage());
            return false;
        }
    }
}