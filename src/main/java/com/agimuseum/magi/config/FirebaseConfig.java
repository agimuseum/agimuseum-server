package com.agimuseum.magi.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
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

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setStorageBucket(storageBucket)
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase application has been initialized successfully");
            }
        } catch (IOException e) {
            log.error("Error initializing Firebase application", e);
            throw new RuntimeException("Firebase initialization failed", e);
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

            Storage storage = StorageOptions.newBuilder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build()
                    .getService();

            log.info("Storage bean created successfully");
            return storage;
        } catch (IOException e) {
            log.error("Error creating Storage bean", e);
            throw new RuntimeException("Failed to create Storage bean", e);
        }
    }
}