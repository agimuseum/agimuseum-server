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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.storage.bucket}")
    private String storageBucket;

    @Value("${firebase.config.path}")
    private String firebaseConfigPath;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                log.info("Initializing Firebase application with config path: {}", firebaseConfigPath);

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

                // Create credentials with explicit scopes
                GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount)
                        .createScoped(java.util.Arrays.asList(
                                "https://www.googleapis.com/auth/cloud-platform",
                                "https://www.googleapis.com/auth/firebase.database",
                                "https://www.googleapis.com/auth/firebase.messaging",
                                "https://www.googleapis.com/auth/firebase.storage",
                                "https://www.googleapis.com/auth/datastore"
                        ));

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

            // Try using ClassPathResource first
            Resource resource = new ClassPathResource(firebaseConfigPath);
            InputStream serviceAccount;

            if (resource.exists()) {
                log.info("Loading Firebase configuration from classpath for Storage");
                serviceAccount = resource.getInputStream();
            } else {
                // Try as a file path
                log.info("Loading Firebase configuration from file system for Storage");
                serviceAccount = new FileInputStream(firebaseConfigPath);
            }

            // Create credentials with explicit scopes
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount)
                    .createScoped(java.util.Arrays.asList(
                            "https://www.googleapis.com/auth/cloud-platform",
                            "https://www.googleapis.com/auth/devstorage.full_control",
                            "https://www.googleapis.com/auth/devstorage.read_write"
                    ));

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
}