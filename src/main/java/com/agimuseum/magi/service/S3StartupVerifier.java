package com.agimuseum.magi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetBucketLocationRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Service to verify S3 settings at application startup
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class S3StartupVerifier {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.accessKey}")
    private String accessKey;

    @EventListener(ApplicationReadyEvent.class)
    public void verifyS3Setup() {
        log.info("Verifying Amazon S3 setup...");

        try {
            // Check for empty credentials
            if (accessKey == null || accessKey.trim().isEmpty() || accessKey.equals("AKIA2KXCAYUDYFP72HZU")) {
                log.warn("Using default AWS credentials. Please update with your own credentials in production.");
            }

            // Test bucket exists by getting its location
            s3Client.headBucket(HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build());

            String location = s3Client.getBucketLocation(
                    GetBucketLocationRequest.builder()
                            .bucket(bucketName)
                            .build()
            ).locationConstraintAsString();

            log.info("Successfully connected to S3 bucket: {} in region: {}",
                    bucketName, location.isEmpty() ? "us-east-1" : location);

            log.info("S3 bucket is correctly configured and accessible");

        } catch (S3Exception e) {
            log.error("Error connecting to S3 bucket: {} (Status code: {})", e.getMessage(), e.statusCode());
            log.error("AWS S3 Error Code: {}, AWS Request ID: {}", e.awsErrorDetails().errorCode(), e.requestId());
            log.error("Please ensure your AWS credentials and bucket configuration are correct");

            // Help with troubleshooting
            if (e.statusCode() == 400) {
                log.error("Bad Request (400) typically indicates an authentication issue or malformed request");
                log.error("Verify that your access key and secret key are correct and have S3 permissions");
            } else if (e.statusCode() == 403) {
                log.error("Forbidden (403) indicates your credentials don't have permission to access this bucket");
            } else if (e.statusCode() == 404) {
                log.error("Not Found (404) indicates the bucket '{}' doesn't exist", bucketName);
            }

            // Allow application to continue even with S3 errors
            log.warn("Application will continue but S3 operations may fail. Fix S3 configuration for full functionality.");

        } catch (Exception e) {
            log.error("Unexpected error verifying S3 setup: {}", e.getMessage(), e);
            log.error("Application will continue but S3 operations may fail. Fix S3 configuration for full functionality.");
        }
    }
}