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

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public S3StorageService(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    /**
     * Upload a file to S3
     * @param directory Directory within bucket (e.g., "locations/1/")
     * @param fileName Original file name
     * @param data File data bytes
     * @param contentType MIME type of the file
     * @return Complete URL to the uploaded file
     */
    public String uploadFile(String directory, String fileName, byte[] data, String contentType) {
        try {
            // Generate a unique file name to avoid collisions
            String uniqueFileName = generateUniqueFileName(fileName);
            String key = directory + uniqueFileName;

            // Set metadata including content type
            Map<String, String> metadata = Map.of(
                    "Content-Type", contentType,
                    "Original-Filename", fileName
            );

            // Create the PutObjectRequest
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .metadata(metadata)
                    .build();

            // Upload the file
            s3Client.putObject(request, RequestBody.fromBytes(data));
            log.info("Successfully uploaded file to S3: {}", key);

            // Generate a URL to the file
            return generateFileUrl(key);
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
     * Download a file from S3
     * @param key File key (path) in S3
     * @return File data as bytes
     */
    public byte[] downloadFile(String key) throws IOException {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            return s3Client.getObject(request).readAllBytes();
        } catch (S3Exception e) {
            log.error("S3 Error downloading file from S3: {} - Status Code: {}, Error Code: {}",
                    e.getMessage(), e.statusCode(), e.awsErrorDetails().errorCode(), e);
            throw new IOException("Failed to download file from S3: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("Error downloading file from S3", e);
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
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(request);
            log.info("Successfully deleted file from S3: {}", key);
            return true;
        } catch (S3Exception e) {
            log.error("S3 Error deleting file from S3: {} - Status Code: {}, Error Code: {}",
                    e.getMessage(), e.statusCode(), e.awsErrorDetails().errorCode(), e);
            return false;
        } catch (Exception e) {
            log.error("Error deleting file from S3", e);
            return false;
        }
    }

    /**
     * Generate a presigned URL for temporary access to a private object
     * @param key File key (path) in S3
     * @param expirationMinutes How long the URL should be valid
     * @return Presigned URL
     */
    public String generatePresignedUrl(String key, int expirationMinutes) {
        try {
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expirationMinutes))
                    .getObjectRequest(GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build())
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception e) {
            log.error("Error generating presigned URL for key: {}", key, e);
            // Return a fallback URL that will indicate it's a failure
            return "https://" + bucketName + ".s3.amazonaws.com/" + key +
                    "?error=failed_to_generate_presigned_url";
        }
    }

    /**
     * Generate a permanent URL for the file (for public buckets or requires presigned URL for private)
     */
    protected String generateFileUrl(String key) {
        // For public objects: direct S3 URL
        // return "https://" + bucketName + ".s3.amazonaws.com/" + key;

        // For private objects: generate 24-hour presigned URL
        return generatePresignedUrl(key, 24 * 60); // 24 hours
    }

    /**
     * Extract S3 key from URL
     */
    public String extractKeyFromUrl(String url) {
        try {
            if (url != null && url.contains(".s3.amazonaws.com/")) {
                int startIndex = url.indexOf(".s3.amazonaws.com/") + ".s3.amazonaws.com/".length();
                int endIndex = url.indexOf("?", startIndex);
                if (endIndex == -1) {
                    return url.substring(startIndex);
                } else {
                    return url.substring(startIndex, endIndex);
                }
            }
        } catch (Exception e) {
            log.error("Error extracting key from URL: {}", url, e);
        }
        return null;
    }

    /**
     * Generate a unique filename to avoid collisions in S3
     */
    protected String generateUniqueFileName(String originalFilename) {
        String extension = "";
        if (originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}