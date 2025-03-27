package com.agimuseum.magi.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.firebase.FirebaseApp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Service to verify Firebase and Storage are correctly initialized at application startup
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FirebaseStartupVerifier {

    private final Storage storage;

    @Value("${firebase.storage.bucket}")
    private String storageBucket;

    @Value("${firebase.config.path}")
    private String firebaseConfigPath;

    @EventListener(ApplicationReadyEvent.class)
    public void verifyFirebaseSetup() {
        log.info("Verifying Firebase and Storage setup...");

        try {
            // 1. Check if Firebase is initialized
            boolean firebaseInitialized = !FirebaseApp.getApps().isEmpty();
            log.info("Firebase initialized: {}", firebaseInitialized);

            if (firebaseInitialized) {
                FirebaseApp app = FirebaseApp.getInstance();
                log.info("Firebase app name: {}", app.getName());
                log.info("Firebase project ID: {}", app.getOptions().getProjectId());
                log.info("Firebase storage bucket: {}", app.getOptions().getStorageBucket());
            } else {
                log.error("Firebase is NOT initialized! This will cause runtime errors.");
            }

            // 2. Check credentials file
            try {
                Resource resource = new ClassPathResource(firebaseConfigPath);
                if (resource.exists()) {
                    log.info("Firebase config file exists in classpath at: {}", firebaseConfigPath);
                } else {
                    try {
                        InputStream stream = new FileInputStream(firebaseConfigPath);
                        stream.close();
                        log.info("Firebase config file exists in filesystem at: {}", firebaseConfigPath);
                    } catch (Exception e) {
                        log.error("Firebase config file not found in filesystem at: {}", firebaseConfigPath);
                    }
                }
            } catch (Exception e) {
                log.error("Error checking for Firebase config file: {}", e.getMessage());
            }

            // 3. Test Storage service
            try {
                // Just load bucket info to verify credentials work
                com.google.cloud.storage.Bucket bucket = storage.get(storageBucket);
                if (bucket != null) {
                    log.info("Successfully connected to storage bucket: {}", bucket.getName());
                } else {
                    log.error("Storage bucket doesn't exist: {}", storageBucket);
                }
            } catch (Exception e) {
                log.error("Failed to connect to Firebase Storage: {}", e.getMessage());
                // Log more details about the error
                e.printStackTrace();
            }

            log.info("Firebase verification completed");
        } catch (Exception e) {
            log.error("Error during Firebase verification", e);
        }
    }
}