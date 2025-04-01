package com.agimuseum.magi.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service for managing Firebase connection and handling retries
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FirebaseConnectionManager {

    private final Storage storage;

    @Value("${firebase.storage.bucket}")
    private String storageBucket;

    @Value("${firebase.config.path}")
    private String firebaseConfigPath;

    private final AtomicBoolean connectionVerified = new AtomicBoolean(false);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * List of scopes required for Firebase operations
     */
    private static final List<String> FIREBASE_SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/cloud-platform",
            "https://www.googleapis.com/auth/firebase.database",
            "https://www.googleapis.com/auth/firebase.messaging",
            "https://www.googleapis.com/auth/firebase.storage",
            "https://www.googleapis.com/auth/datastore"
    );

    /**
     * Verifies Firebase connection and schedules retries if needed
     */
    public void verifyConnection() {
        // Skip if already verified
        if (connectionVerified.get()) {
            return;
        }

        try {
            // Test Firebase Storage connection
            Bucket bucket = StorageClient.getInstance().bucket();
            log.info("Successfully connected to Firebase Storage bucket: {}", bucket.getName());
            connectionVerified.set(true);
        } catch (Exception e) {
            log.error("Error connecting to Firebase Storage on startup: {}", e.getMessage());

            // Schedule retry
            scheduler.schedule(this::retryFirebaseInitialization, 1, TimeUnit.MINUTES);
        }
    }

    /**
     * Retry Firebase initialization
     */
    private void retryFirebaseInitialization() {
        if (connectionVerified.get()) {
            return;
        }

        log.info("Retrying Firebase initialization...");

        try {
            // Get all current Firebase apps
            List<FirebaseApp> apps = FirebaseApp.getApps();

            // Delete any existing apps
            for (FirebaseApp app : apps) {
                app.delete();
            }

            // Read service account file
            String serviceAccountJson = getCleanedServiceAccountJson();
            log.info("Successfully loaded Firebase configuration JSON for retry");

            // Create credentials from the cleaned JSON string with explicit scopes
            InputStream serviceAccountStream = new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8));
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream)
                    .createScoped(FIREBASE_SCOPES);

            // Create options with explicit project ID
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setStorageBucket(storageBucket)
                    .setProjectId("agimuseum") // Add explicit project ID
                    .setDatabaseUrl("https://agimuseum-default-rtdb.firebaseio.com")
                    .build();

            // Initialize the app
            FirebaseApp app = FirebaseApp.initializeApp(options);
            log.info("Firebase application has been reinitialized successfully: {}", app.getName());

            // Test connection
            Bucket bucket = StorageClient.getInstance().bucket();
            log.info("Successfully connected to Firebase Storage bucket after retry: {}", bucket.getName());

            connectionVerified.set(true);
        } catch (Exception e) {
            log.error("Firebase reinitialization failed: {}", e.getMessage());

            // Schedule another retry
            scheduler.schedule(this::retryFirebaseInitialization, 5, TimeUnit.MINUTES);
        }
    }

    /**
     * Reads the service account JSON file and cleans it to ensure proper formatting
     */
    private String getCleanedServiceAccountJson() throws IOException {
        // Try using ClassPathResource
        Resource resource = new ClassPathResource(firebaseConfigPath);
        InputStream serviceAccount = resource.getInputStream();

        // Read the file content as a string
        byte[] bytes = FileCopyUtils.copyToByteArray(serviceAccount);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}