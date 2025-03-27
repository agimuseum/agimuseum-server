package com.agimuseum.magi.controller;

import com.agimuseum.magi.util.FirebaseCredentialsHelper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.StorageClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug/firebase-auth")
@Slf4j
@RequiredArgsConstructor
public class FirebaseAuthController {

    private final Storage storage;

    @Value("${firebase.config.path}")
    private String firebaseConfigPath;

    @Value("${firebase.storage.bucket}")
    private String storageBucket;

    @GetMapping("/diagnostics")
    public ResponseEntity<Map<String, Object>> runFirebaseAuthDiagnostics() {
        Map<String, Object> results = new HashMap<>();
        results.put("timestamp", System.currentTimeMillis());

        try {
            // 1. Check if Firebase is initialized
            boolean firebaseInitialized = !FirebaseApp.getApps().isEmpty();
            results.put("firebaseInitialized", firebaseInitialized);

            if (firebaseInitialized) {
                FirebaseApp app = FirebaseApp.getInstance();
                results.put("appName", app.getName());
                results.put("projectId", app.getOptions().getProjectId());
                results.put("storageBucket", app.getOptions().getStorageBucket());
            }

            // 2. Test Firebase credentials
            GoogleCredentials firebaseCredentials = null;
            try {
                firebaseCredentials = FirebaseCredentialsHelper.getFirebaseCredentials(firebaseConfigPath);
                boolean credentialsValid = FirebaseCredentialsHelper.validateCredentials(firebaseCredentials);
                results.put("firebaseCredentialsValid", credentialsValid);
            } catch (Exception e) {
                log.error("Error validating Firebase credentials", e);
                results.put("firebaseCredentialsValid", false);
                results.put("firebaseCredentialsError", e.getMessage());
            }

            // 3. Test Storage credentials
            GoogleCredentials storageCredentials = null;
            try {
                storageCredentials = FirebaseCredentialsHelper.getStorageCredentials(firebaseConfigPath);
                boolean credentialsValid = FirebaseCredentialsHelper.validateCredentials(storageCredentials);
                results.put("storageCredentialsValid", credentialsValid);
            } catch (Exception e) {
                log.error("Error validating Storage credentials", e);
                results.put("storageCredentialsValid", false);
                results.put("storageCredentialsError", e.getMessage());
            }

            // 4. Test Storage access
            try {
                Bucket bucket = StorageClient.getInstance().bucket();
                results.put("bucketName", bucket.getName());
                results.put("bucketAccessible", true);
            } catch (Exception e) {
                log.error("Error accessing Firebase Storage bucket", e);
                results.put("bucketAccessible", false);
                results.put("bucketAccessError", e.getMessage());
            }

            // 5. Test direct Storage API access
            try {
                com.google.cloud.storage.Bucket gcsBucket = storage.get(storageBucket);
                results.put("gcsBucketAccessible", gcsBucket != null);
                if (gcsBucket != null) {
                    results.put("gcsBucketName", gcsBucket.getName());
                }
            } catch (Exception e) {
                log.error("Error accessing GCS bucket directly", e);
                results.put("gcsBucketAccessible", false);
                results.put("gcsBucketError", e.getMessage());
            }

            results.put("status", "completed");
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            log.error("Error performing Firebase diagnostics", e);
            results.put("status", "error");
            results.put("error", e.getMessage());
            return ResponseEntity.status(500).body(results);
        }
    }
}