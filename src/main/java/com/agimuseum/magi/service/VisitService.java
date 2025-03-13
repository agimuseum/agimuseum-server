package com.agimuseum.magi.service;

import com.agimuseum.magi.dto.*;
import com.agimuseum.magi.exception.ResourceNotFoundException;
import com.agimuseum.magi.model.*;
import com.agimuseum.magi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VisitService {

    private final LocationRepository locationRepository;
    private final StopRepository stopRepository;
    private final UserRepository userRepository;
    private final LocationVisitRepository locationVisitRepository;
    private final StopVisitRepository stopVisitRepository;
    private final LocationService locationService;

    /**
     * Get current authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
    }

    /**
     * Mark a location as visited by the current user
     */
    @Transactional
    public LocationVisitDTO markLocationVisited(Integer locationId) {
        User user = getCurrentUser();
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + locationId));

        // Check if already visited
        if (locationVisitRepository.existsByUserAndLocation(user, location)) {
            // Already marked as visited, just return the data
            LocationVisit visit = locationVisitRepository.findByUserAndLocation(user, location)
                    .orElseThrow(() -> new IllegalStateException("Visit record not found"));

            return LocationVisitDTO.builder()
                    .locationId(location.getId())
                    .locationName(location.getName())
                    .visitedAt(visit.getVisitedAt())
                    .build();
        }

        // Create new visit record
        LocationVisit newVisit = LocationVisit.builder()
                .user(user)
                .location(location)
                .visitedAt(new Date())
                .build();

        locationVisitRepository.save(newVisit);

        return LocationVisitDTO.builder()
                .locationId(location.getId())
                .locationName(location.getName())
                .visitedAt(newVisit.getVisitedAt())
                .build();
    }

    /**
     * Unmark a location as visited by the current user
     */
    @Transactional
    public void unmarkLocationVisited(Integer locationId) {
        User user = getCurrentUser();
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + locationId));

        locationVisitRepository.findByUserAndLocation(user, location)
                .ifPresent(locationVisitRepository::delete);

        // Also remove any visits to stops in this location
        List<Stop> stops = stopRepository.findByLocationId(locationId);
        for (Stop stop : stops) {
            stopVisitRepository.findByUserAndStop(user, stop)
                    .ifPresent(stopVisitRepository::delete);
        }
    }

    /**
     * Mark a stop as visited by the current user
     */
    @Transactional
    public StopVisitDTO markStopVisited(Integer stopId) {
        User user = getCurrentUser();
        Stop stop = stopRepository.findById(stopId)
                .orElseThrow(() -> new ResourceNotFoundException("Stop not found with id: " + stopId));

        // Check if already visited
        if (stopVisitRepository.existsByUserAndStop(user, stop)) {
            // Already marked as visited, just return the data
            StopVisit visit = stopVisitRepository.findByUserAndStop(user, stop)
                    .orElseThrow(() -> new IllegalStateException("Visit record not found"));

            return StopVisitDTO.builder()
                    .stopId(stop.getId())
                    .stopName(stop.getName())
                    .locationId(stop.getLocation().getId())
                    .locationName(stop.getLocation().getName())
                    .visitedAt(visit.getVisitedAt())
                    .build();
        }

        // Create new visit record
        StopVisit newVisit = StopVisit.builder()
                .user(user)
                .stop(stop)
                .visitedAt(new Date())
                .build();

        stopVisitRepository.save(newVisit);

        // Check if all stops in this location are visited
        Location location = stop.getLocation();
        List<Stop> locationStops = stopRepository.findByLocationId(location.getId());
        boolean allStopsVisited = true;

        for (Stop s : locationStops) {
            if (!stopVisitRepository.existsByUserAndStop(user, s)) {
                allStopsVisited = false;
                break;
            }
        }

        // If all stops are visited, mark the location as visited too
        if (allStopsVisited && !locationVisitRepository.existsByUserAndLocation(user, location)) {
            LocationVisit locationVisit = LocationVisit.builder()
                    .user(user)
                    .location(location)
                    .visitedAt(new Date())
                    .build();

            locationVisitRepository.save(locationVisit);
        }

        return StopVisitDTO.builder()
                .stopId(stop.getId())
                .stopName(stop.getName())
                .locationId(location.getId())
                .locationName(location.getName())
                .visitedAt(newVisit.getVisitedAt())
                .build();
    }

    /**
     * Unmark a stop as visited by the current user
     */
    @Transactional
    public void unmarkStopVisited(Integer stopId) {
        User user = getCurrentUser();
        Stop stop = stopRepository.findById(stopId)
                .orElseThrow(() -> new ResourceNotFoundException("Stop not found with id: " + stopId));

        stopVisitRepository.findByUserAndStop(user, stop)
                .ifPresent(stopVisitRepository::delete);
    }

    /**
     * Get visited locations for the current user
     */
    public List<LocationVisitDTO> getVisitedLocations() {
        User user = getCurrentUser();
        return locationVisitRepository.findByUser(user).stream()
                .map(visit -> LocationVisitDTO.builder()
                        .locationId(visit.getLocation().getId())
                        .locationName(visit.getLocation().getName())
                        .visitedAt(visit.getVisitedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Get visited stops for the current user
     */
    public List<StopVisitDTO> getVisitedStops() {
        User user = getCurrentUser();
        return stopVisitRepository.findByUser(user).stream()
                .map(visit -> StopVisitDTO.builder()
                        .stopId(visit.getStop().getId())
                        .stopName(visit.getStop().getName())
                        .locationId(visit.getStop().getLocation().getId())
                        .locationName(visit.getStop().getLocation().getName())
                        .visitedAt(visit.getVisitedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Get visit summary for the current user
     */
    public VisitSummaryDTO getVisitSummary() {
        User user = getCurrentUser();

        long visitedLocations = locationVisitRepository.countVisitedLocationsByUser(user);
        long unvisitedLocations = locationVisitRepository.countUnvisitedLocationsByUser(user);
        long visitedStops = stopVisitRepository.countVisitedStopsByUser(user);
        long unvisitedStops = stopVisitRepository.countUnvisitedStopsByUser(user);

        return VisitSummaryDTO.builder()
                .totalVisitedLocations(visitedLocations)
                .totalUnvisitedLocations(unvisitedLocations)
                .totalVisitedStops(visitedStops)
                .totalUnvisitedStops(unvisitedStops)
                .build();
    }

    /**
     * Get location details with visit status
     */
    public LocationVisitDetailsDTO getLocationWithVisitStatus(Integer locationId) {
        User user = getCurrentUser();
        LocationDTO locationDTO = locationService.getLocationById(locationId);

        // Check if location is visited
        boolean locationVisited = locationVisitRepository.existsByUserAndLocation(
                user,
                locationRepository.findById(locationId)
                        .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + locationId))
        );

        Date locationVisitedAt = null;
        if (locationVisited) {
            locationVisitedAt = locationVisitRepository.findByUserAndLocation(
                    user,
                    locationRepository.findById(locationId).orElseThrow()
            ).get().getVisitedAt();
        }

        // Check visit status for each stop
        List<StopVisitDetailsDTO> stopDTOs = locationDTO.getStops().stream()
                .map(stopDTO -> {
                    Stop stop = stopRepository.findById(stopDTO.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Stop not found with id: " + stopDTO.getId()));

                    boolean stopVisited = stopVisitRepository.existsByUserAndStop(user, stop);
                    Date stopVisitedAt = null;

                    if (stopVisited) {
                        stopVisitedAt = stopVisitRepository.findByUserAndStop(user, stop).get().getVisitedAt();
                    }

                    return StopVisitDetailsDTO.builder()
                            .id(stopDTO.getId())
                            .name(stopDTO.getName())
                            .summary(stopDTO.getSummary())
                            .weblink(stopDTO.getWeblink())
                            .location(stopDTO.getLocation())
                            .photos(stopDTO.getPhotos())
                            .visited(stopVisited)
                            .visitedAt(stopVisitedAt)
                            .build();
                })
                .collect(Collectors.toList());

        return LocationVisitDetailsDTO.builder()
                .id(locationDTO.getId())
                .name(locationDTO.getName())
                .summary(locationDTO.getSummary())
                .location(locationDTO.getLocation())
                .weblink(locationDTO.getWeblink())
                .photos(locationDTO.getPhotos())
                .stops(stopDTOs)
                .visited(locationVisited)
                .visitedAt(locationVisitedAt)
                .build();
    }

    /**
     * Get all locations with visit status
     */
    public List<LocationVisitDetailsDTO> getAllLocationsWithVisitStatus() {
        List<LocationDTO> locations = locationService.getAllLocations();

        return locations.stream()
                .map(location -> getLocationWithVisitStatus(location.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Get reward progress for current user
     */
    public RewardProgressDTO getRewardProgress() {
        User user = getCurrentUser();

        long totalVisitedLocations = locationVisitRepository.countVisitedLocationsByUser(user);
        long totalLocations = locationRepository.count();
        long locationsToGo = Math.max(0, 10 - totalVisitedLocations); // Assuming 10 is target for reward

        int progressPercentage = (int) (totalVisitedLocations * 100 / Math.max(10, totalLocations));
        boolean rewardEligible = totalVisitedLocations >= 10;

        return RewardProgressDTO.builder()
                .totalVisitedLocations((int) totalVisitedLocations)
                .totalLocations((int) totalLocations)
                .locationsToGo((int) locationsToGo)
                .progressPercentage(progressPercentage)
                .rewardEligible(rewardEligible)
                .build();
    }
}