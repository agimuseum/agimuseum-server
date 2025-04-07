package com.agimuseum.magi.service;

import com.agimuseum.magi.model.Photo;
import com.agimuseum.magi.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service to help migrate existing photo records to use S3
 * This will handle migrating photos from external URLs to S3
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StorageMigrationService {

    private final PhotoRepository photoRepository;
    private final S3StorageService s3StorageService;
    private final RestTemplate restTemplate;

    /**
     * Migrates all photos to S3 by downloading them from their current URLs and uploading to S3
     * @return Migration statistics
     */
    public Map<String, Object> migratePhotosToS3() {
        List<Photo> photos = photoRepository.findAll();
        log.info("Starting migration of {} photos to S3", photos.size());

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        AtomicInteger skippedCount = new AtomicInteger(0);

        photos.forEach(photo -> {
            try {
                // Skip if already has S3 info
                if (photo.getS3Key() != null && !photo.getS3Key().isEmpty()) {
                    skippedCount.incrementAndGet();
                    return;
                }

                // Download from current URL
                byte[] imageData = restTemplate.getForObject(photo.getUrl(), byte[].class);
                if (imageData == null || imageData.length == 0) {
                    log.warn("Empty data received for photo ID: {}", photo.getId());
                    failCount.incrementAndGet();
                    return;
                }

                // Determine directory path
                String directory;
                if (photo.getStopId() != null) {
                    directory = "stops/" + photo.getStopId() + "/";
                } else {
                    directory = "locations/" + photo.getLocationId() + "/";
                }

                // Upload to S3
                String url = s3StorageService.uploadFile(
                        directory,
                        photo.getFileName(),
                        imageData,
                        photo.getContentType()
                );

                // Update photo record
                String s3Key = s3StorageService.extractKeyFromUrl(url);
                photo.setS3Key(s3Key);
                photo.setS3Bucket("agimuseum-storage");
                photo.setUrl(url);
                photo.setPhotoUrl(url);

                photoRepository.save(photo);
                successCount.incrementAndGet();

                log.info("Successfully migrated photo ID {} to S3", photo.getId());
            } catch (Exception e) {
                log.error("Failed to migrate photo ID {}: {}", photo.getId(), e.getMessage());
                failCount.incrementAndGet();
            }
        });

        Map<String, Object> result = new HashMap<>();
        result.put("totalPhotos", photos.size());
        result.put("migrated", successCount.get());
        result.put("failed", failCount.get());
        result.put("skipped", skippedCount.get());
        result.put("completedAt", new Date());

        log.info("Migration completed: {} succeeded, {} failed, {} skipped",
                successCount.get(), failCount.get(), skippedCount.get());

        return result;
    }

    @RestController
    @RequestMapping("/api/admin/migration")
    @PreAuthorize("hasRole('ADMIN')")
    public static class MigrationController {

        private final StorageMigrationService migrationService;

        public MigrationController(StorageMigrationService migrationService) {
            this.migrationService = migrationService;
        }

        @PostMapping("/photos-to-s3")
        public ResponseEntity<Map<String, Object>> migratePhotos() {
            // Run migration in a separate thread to avoid blocking the response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Migration started in background");
            response.put("timestamp", new Date());

            new Thread(() -> {
                try {
                    migrationService.migratePhotosToS3();
                } catch (Exception e) {
                    log.error("Migration thread error", e);
                }
            }).start();

            return ResponseEntity.accepted().body(response);
        }
    }
}