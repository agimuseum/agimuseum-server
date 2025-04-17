package com.agimuseum.magi.controller;

import com.agimuseum.magi.dto.LocationDTO;
import com.agimuseum.magi.dto.RewardProgressDTO;
import com.agimuseum.magi.dto.StopDTO;
import com.agimuseum.magi.service.LocationService;
import com.agimuseum.magi.service.VisitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Public API controller that provides endpoints for third-party applications
 * to access public data about locations, stops, and reward progress.
 * These endpoints do not require authentication.
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@Slf4j
public class PublicApiController {

    private final LocationService locationService;
    private final VisitService visitService;

    /**
     * Get all locations with basic information
     */
    @GetMapping("/locations")
    public ResponseEntity<List<LocationDTO>> getAllLocations() {
        log.info("Public API request for all locations");
        return ResponseEntity.ok(locationService.getAllLocations());
    }

    /**
     * Get application statistics (total locations, stops, etc.)
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("Public API request for application statistics");

        List<LocationDTO> locations = locationService.getAllLocations();

        long totalLocations = locations.size();
        long totalStops = locations.stream()
                .flatMap(location -> location.getStops().stream())
                .count();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalLocations", totalLocations);
        statistics.put("totalStops", totalStops);

        // Add more statistics as needed

        return ResponseEntity.ok(statistics);
    }

    /**
     * Get reward progress information (general, not user-specific)
     */
    @GetMapping("/rewards/requirements")
    public ResponseEntity<Map<String, Object>> getRewardRequirements() {
        log.info("Public API request for reward requirements");

        Map<String, Object> requirements = new HashMap<>();

        // Bronze reward requirements
        Map<String, Object> bronzeRequirements = new HashMap<>();
        bronzeRequirements.put("name", "Bronze Explorer Badge");
        bronzeRequirements.put("requiredLocations", 3);
        bronzeRequirements.put("requiresPhotoVerification", false);
        bronzeRequirements.put("benefit", "10% discount at the AGI Museum gift shop");

        // Silver reward requirements
        Map<String, Object> silverRequirements = new HashMap<>();
        silverRequirements.put("name", "Silver Explorer Badge");
        silverRequirements.put("requiredLocations", 5);
        silverRequirements.put("requiresPhotoVerification", true);
        silverRequirements.put("benefit", "15% discount at the AGI Museum gift shop and café");

        // Gold reward requirements
        Map<String, Object> goldRequirements = new HashMap<>();
        goldRequirements.put("name", "Gold Explorer Badge");
        goldRequirements.put("requiredLocations", 10);
        goldRequirements.put("requiresPhotoVerification", true);
        goldRequirements.put("benefit", "20% discount at the AGI Museum gift shop and café, plus a free guided tour");

        requirements.put("bronze", bronzeRequirements);
        requirements.put("silver", silverRequirements);
        requirements.put("gold", goldRequirements);

        return ResponseEntity.ok(requirements);
    }

    /**
     * Get information about the app and museum
     */
    @GetMapping("/about")
    public ResponseEntity<Map<String, Object>> getAboutInfo() {
        log.info("Public API request for about information");

        Map<String, Object> aboutInfo = new HashMap<>();
        aboutInfo.put("appName", "MAGI History Trail App");
        aboutInfo.put("version", "1.0.0");
        aboutInfo.put("museum", "American G.I. Museum");
        aboutInfo.put("description", "Explore historical locations and earn rewards while learning about military history.");
        aboutInfo.put("website", "https://agimuseum.org");
        aboutInfo.put("contact", "info@agimuseum.org");

        return ResponseEntity.ok(aboutInfo);
    }
}