package com.agimuseum.magi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetBucketLocationRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Service to verify S3 connection at application startup and provide instructions
 * if there are any permission issues.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class S3Verifier {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.accessKey}")
    private String accessKey;

    /**
     * Verifies S3 connectivity and permissions at application startup
     */
    @EventListener(ApplicationReadyEvent.class)
    public void verifyS3Connection() {
        log.info("Verifying S3 connectivity for bucket: {}", bucketName);

        try {
            // Test if bucket exists
            s3Client.headBucket(HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build());

            // Try to get bucket location
            String location = s3Client.getBucketLocation(
                    GetBucketLocationRequest.builder()
                            .bucket(bucketName)
                            .build()
            ).locationConstraintAsString();

            log.info("S3 bucket verification successful - bucket exists in region: {}",
                    location.isEmpty() ? "us-east-1" : location);

        } catch (S3Exception e) {
            log.error("S3 error during verification: {} (Status code: {})", e.getMessage(), e.statusCode());
            log.error("AWS S3 Error Code: {}, AWS Request ID: {}", e.awsErrorDetails().errorCode(), e.requestId());

            if (e.statusCode() == 403) {
                log.error("Permission denied (403) - The application doesn't have sufficient permissions to access the bucket");
                log.error("Please check the following:");
                log.error("1. Verify the IAM policy for agimuseum-app user has s3:GetBucketLocation permission");
                log.error("2. Make sure the bucket policy allows the IAM user to access the bucket");
                log.error("3. Ensure the IAM credentials in application.properties are correct");
            } else if (e.statusCode() == 404) {
                log.error("Bucket not found (404) - The bucket '{}' doesn't exist", bucketName);
                log.error("Please create the bucket or update the bucket name in application.properties");
            }

        } catch (Exception e) {
            log.error("Unexpected error verifying S3 configuration: {}", e.getMessage(), e);
        }
    }
}