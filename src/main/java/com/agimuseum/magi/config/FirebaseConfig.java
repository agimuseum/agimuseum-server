package com.agimuseum.magi.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.storage.bucket}")
    private String storageBucket;

    @Value("${firebase.config.path}")
    private String firebaseConfigPath;

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

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                log.info("Initializing Firebase application with config path: {}", firebaseConfigPath);

                // Get service account as a cleaned String to avoid any issues with line breaks or encoding
                String serviceAccountJson = getCleanedServiceAccountJson();
                log.info("Successfully loaded Firebase configuration JSON");

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

                FirebaseApp.initializeApp(options);
                log.info("Firebase application has been initialized successfully");
            }
        } catch (IOException e) {
            log.error("Error initializing Firebase application", e);
            throw new RuntimeException("Firebase initialization failed: " + e.getMessage(), e);
        }
    }

    @Bean
    public Storage storage() {
        try {
            log.info("Creating Storage bean");

            // Get service account as a cleaned String to avoid any issues with line breaks or encoding
            String serviceAccountJson = getCleanedServiceAccountJson();
            log.info("Successfully loaded Firebase configuration JSON for Storage");

            // Create credentials from the cleaned JSON string with explicit scopes
            InputStream serviceAccountStream = new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8));
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream)
                    .createScoped(STORAGE_SCOPES);

            Storage storage = StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .setProjectId("agimuseum") // Add explicit project ID
                    .build()
                    .getService();

            log.info("Storage bean created successfully");
            return storage;
        } catch (IOException e) {
            log.error("Error creating Storage bean", e);
            throw new RuntimeException("Failed to create Storage bean: " + e.getMessage(), e);
        }
    }

    /**
     * Reads the service account JSON file and cleans it to ensure proper formatting.
     * This helps avoid issues with line breaks or encoding that can cause JWT signature problems.
     */
    private String getCleanedServiceAccountJson() throws IOException {
        // Try using ClassPathResource first
        Resource resource = new ClassPathResource(firebaseConfigPath);
        InputStream serviceAccount;

        if (resource.exists()) {
            log.info("Loading Firebase configuration from classpath");
            serviceAccount = resource.getInputStream();
        } else {
            // Try as a file path
            log.info("Loading Firebase configuration from file system");
            serviceAccount = new FileInputStream(firebaseConfigPath);
        }

        // Read the file content as a string
        byte[] bytes = FileCopyUtils.copyToByteArray(serviceAccount);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}