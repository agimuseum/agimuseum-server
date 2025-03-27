package com.agimuseum.magi.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class FirebaseTestService {

    private final Storage storage;

    @Value("${firebase.storage.bucket}")
    private String storageBucket;

    /**
     * Tests Firebase Storage connectivity by uploading a small test file
     * @return Map containing test results and any diagnostics
     */
    public Map<String, Object> testFirebaseStorage() {
        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", System.currentTimeMillis());

        try {
            // Create test content
            String testContent = "Firebase Storage Test - " + System.currentTimeMillis();
            byte[] testBytes = testContent.getBytes(StandardCharsets.UTF_8);

            // Create test path
            String testPath = "tests/connectivity-test-" + System.currentTimeMillis() + ".txt";

            // Create blob
            BlobId blobId = BlobId.of(storageBucket, testPath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType("text/plain")
                    .build();

            // Attempt to upload
            log.info("Testing Firebase connectivity by uploading to: {}", testPath);
            Blob blob = storage.create(blobInfo, testBytes);

            // Get blob URL and details
            String url = "https://storage.googleapis.com/" + storageBucket + "/" + testPath;

            result.put("success", true);
            result.put("message", "Successfully uploaded test file to Firebase Storage");
            result.put("url", url);
            result.put("storageBucket", storageBucket);
            result.put("testPath", testPath);
            result.put("blobName", blob.getName());
            result.put("blobSize", blob.getSize());
            result.put("blobGeneration", blob.getGeneration());

            log.info("Firebase Storage connectivity test successful. URL: {}", url);

            // Clean up test file
            boolean deleted = storage.delete(blobId);
            result.put("cleanupSuccess", deleted);

            return result;
        } catch (Exception e) {
            log.error("Firebase Storage connectivity test failed", e);

            result.put("success", false);
            result.put("message", "Failed to connect to Firebase Storage");
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getName());
            result.put("storageBucket", storageBucket);

            return result;
        }
    }
}