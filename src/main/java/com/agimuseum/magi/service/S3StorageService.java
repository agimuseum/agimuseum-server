package com.agimuseum.magi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class S3StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final UrlShortenerService urlShortenerService;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.region}")
    private String awsRegion;

    public S3StorageService(
            S3Client s3Client,
            S3Presigner s3Presigner,
            UrlShortenerService urlShortenerService) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.urlShortenerService = urlShortenerService;
    }

    /**
     * Upload a file to S3
     * @param directory Directory within bucket (e.g., "locations/1/")
     * @param fileName Original file name
     * @param data File data bytes
     * @param contentType MIME type of the file
     * @return Complete URL to the uploaded file (possibly shortened for DB storage)
     */
    public String uploadFile(String directory, String fileName, byte[] data, String contentType) {
        try {
            log.debug("Starting S3 upload: directory={}, fileName={}, contentType={}, size={} bytes",
                    directory, fileName, contentType, data.length);

            // Generate a unique file name to avoid collisions
            String uniqueFileName = generateUniqueFileName(fileName);
            String key = directory + uniqueFileName;
            log.debug("Generated S3 key: {}", key);

            // Set metadata including content type
            Map<String, String> metadata = Map.of(
                    "Content-Type", contentType,
                    "Original-Filename", fileName
            );

            // Create the PutObjectRequest WITHOUT the ACL
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .metadata(metadata)
                    // Remove the ACL line
                    // .acl(ObjectCannedACL.PUBLIC_READ) // Don't use ACL - rely on bucket policy instead
                    .build();

            // Upload the file
            s3Client.putObject(request, RequestBody.fromBytes(data));
            log.debug("Successfully uploaded file to S3: {}", key);

            // Generate a direct URL to the file (no expiration)
            String url = generateDirectUrl(key);
            log.debug("Generated direct URL for uploaded file: {}", url);

            // If URL is too long, shorten it for database storage
            String finalUrl = urlShortenerService.shortenIfNeeded(url);
            log.debug("Final URL (possibly shortened): {}", finalUrl);

            return finalUrl;
        } catch (S3Exception e) {
            log.error("S3 Error uploading file to S3: {} - Status Code: {}, Error Code: {}",
                    e.getMessage(), e.statusCode(), e.awsErrorDetails().errorCode(), e);
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error uploading file to S3", e);
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }

    /**
     * Generate a direct URL for the file (no expiration)
     */
    protected String generateDirectUrl(String key) {
        // Generate direct S3 URL
        String directUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName, awsRegion, key);
        log.debug("Generated direct URL: {}", directUrl);
        return directUrl;
    }

    /**
     * Download a file from S3
     * @param key File key (path) in S3
     * @return File data as bytes
     */
    public byte[] downloadFile(String key) throws IOException {
        try {
            log.debug("Downloading file from S3: {}", key);

            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            byte[] data = s3Client.getObject(request).readAllBytes();
            log.debug("Successfully downloaded file from S3: {} ({} bytes)", key, data.length);

            return data;
        } catch (S3Exception e) {
            log.error("S3 Error downloading file from S3: {} - Status Code: {}, Error Code: {}",
                    e.getMessage(), e.statusCode(), e.awsErrorDetails().errorCode(), e);
            throw new IOException("Failed to download file from S3: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("Error downloading file from S3: {}", key, e);
            throw new IOException("Failed to download file from S3: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a file from S3
     * @param key File key (path) in S3
     * @return true if successful
     */
    public boolean deleteFile(String key) {
        try {
            if (key == null || key.trim().isEmpty()) {
                log.warn("Cannot delete file with null or empty key");
                return false;
            }

            log.debug("Deleting file from S3: {}", key);

            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(request);
            log.debug("Successfully deleted file from S3: {}", key);
            return true;
        } catch (S3Exception e) {
            log.error("S3 Error deleting file from S3: {} - Status Code: {}, Error Code: {}",
                    e.getMessage(), e.statusCode(), e.awsErrorDetails().errorCode(), e);
            return false;
        } catch (Exception e) {
            log.error("Error deleting file from S3: {}", key, e);
            return false;
        }
    }

    /**
     * Generate a presigned URL for temporary access to a private object
     * Only use this when temporary access is specifically needed
     */
    public String generatePresignedUrl(String key, int expirationMinutes) {
        try {
            log.debug("Generating presigned URL for key: {} with expiration: {} minutes", key, expirationMinutes);

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expirationMinutes))
                    .getObjectRequest(GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build())
                    .build();

            String url = s3Presigner.presignGetObject(presignRequest).url().toString();
            log.debug("Generated presigned URL: {}", url);

            // If the URL is too long for DB storage, shorten it
            String shortenedUrl = urlShortenerService.shortenIfNeeded(url);
            log.debug("Final presigned URL (possibly shortened): {}", shortenedUrl);

            return shortenedUrl;
        } catch (Exception e) {
            log.error("Error generating presigned URL for key: {}", key, e);
            // Return a fallback URL that will indicate it's a failure
            return "https://" + bucketName + ".s3.amazonaws.com/" + key +
                    "?error=failed_to_generate_presigned_url";
        }
    }

    /**
     * Generate a permanent URL for the file
     * Updated to use direct URLs instead of presigned URLs
     */
    protected String generateFileUrl(String key) {
        log.debug("Generating permanent URL for key: {}", key);
        return generateDirectUrl(key);
    }

    /**
     * Extract S3 key from URL
     */
    public String extractKeyFromUrl(String url) {
        if (url == null) {
            log.warn("Cannot extract key from null URL");
            return null;
        }

        try {
            log.debug("Extracting S3 key from URL: {}", url);

            // The URL appears to be in this format:
            // https://agimuseum-storage.s3.us-east-2.amazonaws.com/stock/locations/2/bc44f0cb-a501-4d16-ab58-3df985ed33a9.png?...

            if (url.contains(".amazonaws.com/")) {
                int startIndex = url.indexOf(".amazonaws.com/") + ".amazonaws.com/".length();
                int endIndex = url.indexOf("?", startIndex);

                if (endIndex == -1) {
                    return url.substring(startIndex);
                } else {
                    return url.substring(startIndex, endIndex);
                }
            }

            return null;
        } catch (Exception e) {
            log.error("Error extracting key from URL: {}", url, e);
            return null;
        }
    }

    /**
     * Generate a unique filename to avoid collisions in S3
     */
    protected String generateUniqueFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueName = UUID.randomUUID().toString() + extension;
        log.debug("Generated unique filename: {} from original: {}", uniqueName, originalFilename);
        return uniqueName;
    }
}