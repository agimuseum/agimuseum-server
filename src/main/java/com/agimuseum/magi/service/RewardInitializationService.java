package com.agimuseum.magi.service;

import com.agimuseum.magi.model.Reward;
import com.agimuseum.magi.repository.RewardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Service to initialize some example rewards when the application starts
 * This can be removed in production if not needed
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RewardInitializationService {

    private final RewardRepository rewardRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeRewards() {
        // Only create sample rewards if there are none in the database
        if (rewardRepository.count() == 0) {
            log.info("Initializing sample rewards...");
            createSampleRewards();
            log.info("Sample rewards created successfully");
        } else {
            log.info("Rewards already exist in the database, skipping initialization");
        }
    }

    private void createSampleRewards() {
        // Create expiration dates
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 3); // 3 months from now
        Date threeMonthsFromNow = calendar.getTime();

        calendar.add(Calendar.MONTH, 3); // 6 months from now
        Date sixMonthsFromNow = calendar.getTime();

        calendar.add(Calendar.MONTH, 6); // 12 months from now
        Date oneYearFromNow = calendar.getTime();

        // Create sample rewards
        List<Reward> sampleRewards = List.of(
                // Bronze reward - requires 3 locations
                Reward.builder()
                        .name("Bronze Explorer Badge")
                        .description("Visit 3 locations to earn this bronze badge, which gives you a 10% discount at the AGI Museum gift shop.")
                        .code("BRONZE10")
                        .expirationDate(threeMonthsFromNow)
                        .requiredLocations(3)
                        .requiresPhotoVerification(false)
                        .active(true)
                        .build(),

                // Silver reward - requires 5 locations with photo verification
                Reward.builder()
                        .name("Silver Explorer Badge")
                        .description("Visit 5 locations with photo verification to earn this silver badge, which gives you a 15% discount at the AGI Museum gift shop and café.")
                        .code("SILVER15")
                        .expirationDate(sixMonthsFromNow)
                        .requiredLocations(5)
                        .requiresPhotoVerification(true)
                        .active(true)
                        .build(),

                // Gold reward - requires 10 locations with photo verification
                Reward.builder()
                        .name("Gold Explorer Badge")
                        .description("Visit all 10 major locations with photo verification to earn this prestigious gold badge, which gives you a 20% discount at the AGI Museum gift shop and café, plus a free guided tour for you and a guest.")
                        .code("GOLD20")
                        .expirationDate(oneYearFromNow)
                        .requiredLocations(10)
                        .requiresPhotoVerification(true)
                        .active(true)
                        .build(),

                // Special event reward
                Reward.builder()
                        .name("Summer Explorer 2025")
                        .description("Limited-time summer special! Visit any 3 locations during summer 2025 to receive a commemorative pin and a free drink at the museum café.")
                        .code("SUMMER25")
                        .expirationDate(getSummerEndDate())
                        .requiredLocations(3)
                        .requiresPhotoVerification(false)
                        .active(true)
                        .build()
        );

        // Save all rewards
        rewardRepository.saveAll(sampleRewards);
    }

    private Date getSummerEndDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, Calendar.SEPTEMBER, 21); // End of summer 2025
        return calendar.getTime();
    }
}