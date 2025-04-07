package com.agimuseum.magi.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@Slf4j
public class S3Config {

    @Value("${aws.accessKey}")
    private String accessKey;

    @Value("${aws.secretKey}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    /**
     * Creates an S3Client with AWS credentials
     */
    @Bean
    public S3Client s3Client() {
        log.info("Configuring S3Client with AWS credentials for region: {}", region);
        try {
            // Validate credentials before creating the client
            if (accessKey == null || accessKey.trim().isEmpty() ||
                    secretKey == null || secretKey.trim().isEmpty()) {
                log.warn("AWS credentials are missing or empty. Using default credential provider chain.");
                return S3Client.builder()
                        .region(Region.of(region))
                        .build();
            }

            // Create credentials with the provided keys
            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
                    accessKey.trim(), secretKey.trim());

            // Build the client with the provided credentials
            return S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                    .build();
        } catch (Exception e) {
            log.error("Error creating S3Client: {}", e.getMessage(), e);
            log.warn("Falling back to default credential provider chain");

            // Fallback to default credential provider chain
            return S3Client.builder()
                    .region(Region.of(region))
                    .build();
        }
    }

    /**
     * Creates an S3Presigner with AWS credentials
     */
    @Bean
    public S3Presigner s3Presigner() {
        log.info("Configuring S3Presigner with AWS credentials for region: {}", region);
        try {
            // Validate credentials before creating the presigner
            if (accessKey == null || accessKey.trim().isEmpty() ||
                    secretKey == null || secretKey.trim().isEmpty()) {
                log.warn("AWS credentials are missing or empty. Using default credential provider chain.");
                return S3Presigner.builder()
                        .region(Region.of(region))
                        .build();
            }

            // Create credentials with the provided keys
            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
                    accessKey.trim(), secretKey.trim());

            // Build the presigner with the provided credentials
            return S3Presigner.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                    .build();
        } catch (Exception e) {
            log.error("Error creating S3Presigner: {}", e.getMessage(), e);
            log.warn("Falling back to default credential provider chain");

            // Fallback to default credential provider chain
            return S3Presigner.builder()
                    .region(Region.of(region))
                    .build();
        }
    }
}