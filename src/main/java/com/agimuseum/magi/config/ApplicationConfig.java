package com.agimuseum.magi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import com.agimuseum.magi.model.PhotoType;
import com.agimuseum.magi.repository.PhotoRepository;

/**
 * Application configuration class
 */
@Configuration
@Slf4j
public class ApplicationConfig {

    @Autowired
    private Environment env;

    /**
     * Configure system on startup
     */
    @Bean
    public CommandLineRunner configureSystem(PhotoRepository photoRepository) {
        return args -> {
            log.info("Application starting up with the following configuration:");
            log.info("Photos auto-approval: Enabled");
            log.info("AWS S3 bucket: {}", env.getProperty("aws.s3.bucket"));
            log.info("AWS region: {}", env.getProperty("aws.region"));

            // Log configured photo types
            log.info("Configured photo types:");
            for (PhotoType type : PhotoType.values()) {
                log.info("  - {}: Auto-approved", type);
            }
        };
    }
}