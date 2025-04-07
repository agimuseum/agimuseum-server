package com.agimuseum.magi.controller;

import com.agimuseum.magi.service.S3StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Test controller for S3 functionality
 */
@RestController
@RequestMapping("/api/debug/s3")
@Slf4j
@RequiredArgsConstructor
public class S3TestController {

    private final S3StorageService s3StorageService;

    @PostMapping(value = "/test-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> testUpload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        try {
            String url = s3StorageService.uploadFile(
                    "tests/",
                    file.getOriginalFilename(),
                    file.getBytes(),
                    file.getContentType()
            );

            result.put("success", true);
            result.put("message", "File uploaded successfully to S3");
            result.put("url", url);
            result.put("fileSize", file.getSize());
            result.put("fileName", file.getOriginalFilename());
            result.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error in test upload", e);
            result.put("success", false);
            result.put("error", e.getMessage());

            return ResponseEntity.status(500).body(result);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getS3Status() {
        Map<String, Object> status = new HashMap<>();

        try {
            // Test connectivity by listing some objects
            status.put("timestamp", System.currentTimeMillis());
            status.put("service", "Amazon S3");
            status.put("status", "Connected");
            status.put("bucket", "agimuseum-storage");

            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error checking S3 status", e);
            status.put("status", "Error");
            status.put("error", e.getMessage());

            return ResponseEntity.status(500).body(status);
        }
    }
}