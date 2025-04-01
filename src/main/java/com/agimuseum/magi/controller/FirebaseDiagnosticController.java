package com.agimuseum.magi.controller;

import com.agimuseum.magi.util.FirebaseJsonRegenerator;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.firebase.FirebaseApp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for diagnosing and fixing Firebase-related issues
 */
@RestController
@RequestMapping("/api/debug/firebase-fix")
@RequiredArgsConstructor
@Slf4j
public class FirebaseDiagnosticController {

    private final Storage storage;
    private final FirebaseJsonRegenerator firebaseJsonRegenerator;

    @Value("${firebase.storage.bucket}")
    private String storageBucket;

    @Value("${firebase.config.path}")
    private String firebaseConfigPath;

    /**
     * Get Firebase configuration status and diagnostics
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getFirebaseStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("timestamp", System.currentTimeMillis());

        try {
            // Check if Firebase is initialized
            boolean firebaseInitialized = !FirebaseApp.getApps().isEmpty();
            status.put("firebaseInitialized", firebaseInitialized);

            if (firebaseInitialized) {
                FirebaseApp app = FirebaseApp.getInstance();
                status.put("appName", app.getName());
                status.put("projectId", app.getOptions().getProjectId());
                status.put("storageBucket", app.getOptions().getStorageBucket());
                status.put("databaseUrl", app.getOptions().getDatabaseUrl());
            }

            // Check if the service account file exists and is readable
            ClassPathResource resource = new ClassPathResource(firebaseConfigPath);
            boolean fileExists = resource.exists();
            status.put("configFileExists", fileExists);

            if (fileExists) {
                try {
                    String jsonContent = new String(FileCopyUtils.copyToByteArray(resource.getInputStream()), StandardCharsets.UTF_8);
                    status.put("configFileReadable", true);
                    status.put("configFileLength", jsonContent.length());

                    // Don't include the actual JSON for security reasons
                    status.put("configFileContainsPrivateKey", jsonContent.contains("private_key"));
                } catch (Exception e) {
                    status.put("configFileReadable", false);
                    status.put("configFileError", e.getMessage());
                }
            }

            // Test Storage connectivity with a quick read operation
            try {
                status.put("storageConnected", storage != null);

                // Check if we can access bucket info (read operation)
                com.google.cloud.storage.Bucket bucket = storage.get(storageBucket);
                if (bucket != null) {
                    status.put("bucketAccessible", true);
                    status.put("bucketName", bucket.getName());
                    status.put("bucketLocation", bucket.getLocation());
                } else {
                    status.put("bucketAccessible", false);
                    status.put("bucketError", "Bucket not found");
                }
            } catch (Exception e) {
                status.put("bucketAccessible", false);
                status.put("storageError", e.getMessage());
                log.error("Storage connectivity test failed", e);
            }

            status.put("status", "completed");
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            status.put("status", "error");
            status.put("error", e.getMessage());
            return ResponseEntity.status(500).body(status);
        }
    }

    /**
     * Test storage connectivity by uploading a small test file
     */
    @PostMapping("/test-connectivity")
    public ResponseEntity<Map<String, Object>> testConnectivity() {
        Map<String, Object> result = new HashMap<>();

        try {
            // Create test content
            String testContent = "Firebase Connectivity Test - " + System.currentTimeMillis();
            byte[] testBytes = testContent.getBytes(StandardCharsets.UTF_8);

            // Create a unique test path
            String testPath = "tests/connectivity-test-" + UUID.randomUUID().toString() + ".txt";

            // Prepare blob for upload
            BlobId blobId = BlobId.of(storageBucket, testPath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType("text/plain")
                    .build();

            // Attempt to upload
            log.info("Testing Firebase connectivity by uploading to: {}", testPath);
            Blob blob = storage.create(blobInfo, testBytes);

            // Get details
            String url = String.format("https://storage.googleapis.com/%s/%s", storageBucket, testPath);

            result.put("success", true);
            result.put("message", "Successfully uploaded test file to Firebase Storage");
            result.put("url", url);
            result.put("storageBucket", storageBucket);
            result.put("testPath", testPath);
            result.put("blobName", blob.getName());
            result.put("blobSize", blob.getSize());

            // Clean up - delete the test file
            boolean deleted = storage.delete(blobId);
            result.put("cleanupSuccess", deleted);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Firebase connectivity test failed", e);

            result.put("success", false);
            result.put("message", "Failed to connect to Firebase Storage");
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getName());

            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * Regenerate the Firebase service account JSON file
     */
    @PostMapping("/regenerate-json")
    public ResponseEntity<Map<String, Object>> regenerateJson() {
        Map<String, Object> result = new HashMap<>();

        try {
            firebaseJsonRegenerator.regenerateFirebaseJsonDirect();

            result.put("success", true);
            result.put("message", "Successfully regenerated Firebase service account JSON file");
            result.put("backupCreated", true);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to regenerate Firebase JSON", e);

            result.put("success", false);
            result.put("message", "Failed to regenerate Firebase service account JSON file");
            result.put("error", e.getMessage());

            return ResponseEntity.status(500).body(result);
        }
    }
}