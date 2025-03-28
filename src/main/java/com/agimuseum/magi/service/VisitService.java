package com.agimuseum.magi.service;

import com.agimuseum.magi.dto.*;
import com.agimuseum.magi.exception.ResourceNotFoundException;
import com.agimuseum.magi.model.*;
import com.agimuseum.magi.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for tracking and managing user visits to locations and stops
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VisitService {

    private final LocationRepository locationRepository;
    private final StopRepository stopRepository;
    private final UserRepository userRepository;
    private final LocationVisitRepository locationVisitRepository;
    private final StopVisitRepository stopVisitRepository;
    private final PhotoRepository photoRepository;
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
     * Mark a location as visited by the current user (manual method)
     */
    @Transactional
    public LocationVisitDTO markLocationVisited(Integer locationId) {
        return markLocationVisited(locationId, null, LocationVisit.VisitMethod.MANUAL);
    }

    /**
     * Mark a location as visited by the current user with a photo proof
     */
    @Transactional
    public LocationVisitDTO markLocationVisited(Integer locationId, Integer photoId, LocationVisit.VisitMethod visitMethod) {
        User user = getCurrentUser();
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + locationId));

        // Check if already visited
        Optional<LocationVisit> existingVisit = locationVisitRepository.findByUserAndLocation(user, location);

        if (existingVisit.isPresent()) {
            // Update existing visit if photo proof is provided and not already set
            LocationVisit visit = existingVisit.get();

            if (photoId != null && (visit.getPhotoId() == null || !visit.getHasPhotoProof())) {
                visit.setPhotoId(photoId);
                visit.setHasPhotoProof(true);
                visit.setVisitMethod(visitMethod);
                locationVisitRepository.save(visit);
                log.info("Updated existing location visit with photo proof for location: {}", locationId);
            }

            return createLocationVisitDTO(visit);
        }

        // Create new visit record
        LocationVisit newVisit = LocationVisit.builder()
                .user(user)
                .location(location)
                .visitedAt(new Date())
                .photoId(photoId)
                .hasPhotoProof(photoId != null)
                .visitMethod(visitMethod)
                .build();

        LocationVisit savedVisit = locationVisitRepository.save(newVisit);
        log.info("New location visit created for location: {}", locationId);

        return createLocationVisitDTO(savedVisit);
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

        log.info("Location visit removed for location: {}", locationId);
    }

    /**
     * Mark a stop as visited by the current user (manual method)
     */
    @Transactional
    public StopVisitDTO markStopVisited(Integer stopId) {
        return markStopVisited(stopId, null, StopVisit.VisitMethod.MANUAL);
    }

    /**
     * Mark a stop as visited by the current user with a photo proof
     */
    @Transactional
    public StopVisitDTO markStopVisited(Integer stopId, Integer photoId, StopVisit.VisitMethod visitMethod) {
        User user = getCurrentUser();
        Stop stop = stopRepository.findById(stopId)
                .orElseThrow(() -> new ResourceNotFoundException("Stop not found with id: " + stopId));

        // Check if already visited
        Optional<StopVisit> existingVisit = stopVisitRepository.findByUserAndStop(user, stop);

        if (existingVisit.isPresent()) {
            // Update existing visit if photo proof is provided and not already set
            StopVisit visit = existingVisit.get();

            if (photoId != null && (visit.getPhotoId() == null || !visit.getHasPhotoProof())) {
                visit.setPhotoId(photoId);
                visit.setHasPhotoProof(true);
                visit.setVisitMethod(visitMethod);
                stopVisitRepository.save(visit);
                log.info("Updated existing stop visit with photo proof for stop: {}", stopId);
            }

            return createStopVisitDTO(visit);
        }

        // Create new visit record
        StopVisit newVisit = StopVisit.builder()
                .user(user)
                .stop(stop)
                .visitedAt(new Date())
                .photoId(photoId)
                .hasPhotoProof(photoId != null)
                .visitMethod(visitMethod)
                .build();

        StopVisit savedVisit = stopVisitRepository.save(newVisit);
        log.info("New stop visit created for stop: {}", stopId);

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
                    .hasPhotoProof(false) // Don't set photo proof as it's derived from stops
                    .visitMethod(LocationVisit.VisitMethod.MANUAL)
                    .build();

            locationVisitRepository.save(locationVisit);
            log.info("Location visit automatically created because all stops visited for location: {}", location.getId());
        }

        return createStopVisitDTO(savedVisit);
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

        log.info("Stop visit removed for stop: {}", stopId);
    }

    /**
     * Get visited locations for the current user
     */
    public List<LocationVisitDTO> getVisitedLocations() {
        User user = getCurrentUser();
        return locationVisitRepository.findByUser(user).stream()
                .map(this::createLocationVisitDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get visited stops for the current user
     */
    public List<StopVisitDTO> getVisitedStops() {
        User user = getCurrentUser();
        return stopVisitRepository.findByUser(user).stream()
                .map(this::createStopVisitDTO)
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

        // Get counts for visits with photo proof
        long visitedLocationsWithPhoto = locationVisitRepository.countVisitedLocationsWithPhotoByUser(user);
        long visitedStopsWithPhoto = stopVisitRepository.countVisitedStopsWithPhotoByUser(user);

        return VisitSummaryDTO.builder()
                .totalVisitedLocations(visitedLocations)
                .totalUnvisitedLocations(unvisitedLocations)
                .totalVisitedStops(visitedStops)
                .totalUnvisitedStops(unvisitedStops)
                .totalVisitedLocationsWithPhotoProof(visitedLocationsWithPhoto)
                .totalVisitedStopsWithPhotoProof(visitedStopsWithPhoto)
                .build();
    }

    /**
     * Get location details with visit status
     */
    public LocationVisitDetailsDTO getLocationWithVisitStatus(Integer locationId) {
        User user = getCurrentUser();
        LocationDTO locationDTO = locationService.getLocationById(locationId);

        // Check if location is visited
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + locationId));

        Optional<LocationVisit> locationVisit = locationVisitRepository.findByUserAndLocation(user, location);

        boolean locationVisited = locationVisit.isPresent();
        Date locationVisitedAt = null;
        boolean hasPhotoProof = false;
        String visitMethod = null;
        String photoUrl = null;

        if (locationVisited) {
            LocationVisit visit = locationVisit.get();
            locationVisitedAt = visit.getVisitedAt();
            hasPhotoProof = visit.getHasPhotoProof() != null && visit.getHasPhotoProof();
            visitMethod = visit.getVisitMethod() != null ? visit.getVisitMethod().name() : "MANUAL";

            // If there's a photo ID, get the URL
            if (visit.getPhotoId() != null) {
                Optional<Photo> photo = photoRepository.findById(visit.getPhotoId());
                photoUrl = photo.map(Photo::getUrl).orElse(null);
            }

            // If there's no photo URL but we know a photo exists, find the most recent one
            if (photoUrl == null && hasPhotoProof) {
                Photo latestPhoto = photoRepository.findTopByUserIdAndLocationIdOrderByUploadedAtDesc(user.getId(), locationId);
                if (latestPhoto != null) {
                    photoUrl = latestPhoto.getUrl();
                }
            }
        }

        // Check visit status for each stop
        List<StopVisitDetailsDTO> stopDTOs = locationDTO.getStops().stream()
                .map(stopDTO -> {
                    Stop stop = stopRepository.findById(stopDTO.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Stop not found with id: " + stopDTO.getId()));

                    Optional<StopVisit> stopVisit = stopVisitRepository.findByUserAndStop(user, stop);
                    boolean stopVisited = stopVisit.isPresent();
                    Date stopVisitedAt = null;
                    boolean stopHasPhotoProof = false;
                    String stopVisitMethod = null;
                    String stopPhotoUrl = null;

                    if (stopVisited) {
                        StopVisit visit = stopVisit.get();
                        stopVisitedAt = visit.getVisitedAt();
                        stopHasPhotoProof = visit.getHasPhotoProof() != null && visit.getHasPhotoProof();
                        stopVisitMethod = visit.getVisitMethod() != null ? visit.getVisitMethod().name() : "MANUAL";

                        // If there's a photo ID, get the URL
                        if (visit.getPhotoId() != null) {
                            Optional<Photo> photo = photoRepository.findById(visit.getPhotoId());
                            stopPhotoUrl = photo.map(Photo::getUrl).orElse(null);
                        }

                        // If there's no photo URL but we know a photo exists, find the most recent one
                        if (stopPhotoUrl == null && stopHasPhotoProof) {
                            Photo latestPhoto = photoRepository.findTopByUserIdAndStopIdOrderByUploadedAtDesc(user.getId(), stop.getId());
                            if (latestPhoto != null) {
                                stopPhotoUrl = latestPhoto.getUrl();
                            }
                        }
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
                            .hasPhotoEvidence(stopHasPhotoProof)
                            .visitPhotoUrl(stopPhotoUrl)
                            .visitMethod(stopVisitMethod)
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
                .hasPhotoEvidence(hasPhotoProof)
                .visitPhotoUrl(photoUrl)
                .visitMethod(visitMethod)
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
        long totalVisitedLocationsWithPhoto = locationVisitRepository.countVisitedLocationsWithPhotoByUser(user);
        long totalLocations = locationRepository.count();
        long locationsToGo = Math.max(0, 10 - totalVisitedLocations); // Assuming 10 is target for reward

        int progressPercentage = (int) (totalVisitedLocations * 100 / Math.max(10, totalLocations));
        boolean rewardEligible = totalVisitedLocations >= 10;
        boolean photoVerificationEligible = totalVisitedLocationsWithPhoto >= 10;

        return RewardProgressDTO.builder()
                .totalVisitedLocations((int) totalVisitedLocations)
                .totalVisitedLocationsWithPhoto((int) totalVisitedLocationsWithPhoto)
                .totalLocations((int) totalLocations)
                .locationsToGo((int) locationsToGo)
                .progressPercentage(progressPercentage)
                .rewardEligible(rewardEligible)
                .photoVerificationEligible(photoVerificationEligible)
                .build();
    }

    /**
     * Get list of all locations that have been visited with photo proof
     */
    public List<LocationVisitDTO> getVisitedLocationsWithPhotoProof() {
        User user = getCurrentUser();
        List<LocationVisit> visits = locationVisitRepository.findVisitsWithPhotoByUser(user);
        return visits.stream()
                .map(this::createLocationVisitDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get list of all stops that have been visited with photo proof
     */
    public List<StopVisitDTO> getVisitedStopsWithPhotoProof() {
        User user = getCurrentUser();
        List<StopVisit> visits = stopVisitRepository.findVisitsWithPhotoByUser(user);
        return visits.stream()
                .map(this::createStopVisitDTO)
                .collect(Collectors.toList());
    }

    /**
     * Check if a user has visited a specific location
     */
    public boolean hasVisitedLocation(User user, Integer locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + locationId));
        return locationVisitRepository.existsByUserAndLocation(user, location);
    }

    /**
     * Check if a user has visited a specific stop
     */
    public boolean hasVisitedStop(User user, Integer stopId) {
        Stop stop = stopRepository.findById(stopId)
                .orElseThrow(() -> new ResourceNotFoundException("Stop not found with id: " + stopId));
        return stopVisitRepository.existsByUserAndStop(user, stop);
    }

    /**
     * Convert a LocationVisit entity to a LocationVisitDTO
     */
    private LocationVisitDTO createLocationVisitDTO(LocationVisit visit) {
        LocationVisitDTO.LocationVisitDTOBuilder builder = LocationVisitDTO.builder()
                .locationId(visit.getLocation().getId())
                .locationName(visit.getLocation().getName())
                .visitedAt(visit.getVisitedAt())
                .hasPhotoProof(visit.getHasPhotoProof() != null ? visit.getHasPhotoProof() : false)
                .visitMethod(visit.getVisitMethod() != null ? visit.getVisitMethod().name() : "MANUAL");

        // Add photo URL if there's a photo ID
        if (visit.getPhotoId() != null) {
            Optional<Photo> photo = photoRepository.findById(visit.getPhotoId());
            if (photo.isPresent()) {
                builder.photoUrl(photo.get().getUrl());
            }
        }

        return builder.build();
    }

    /**
     * Convert a StopVisit entity to a StopVisitDTO
     */
    private StopVisitDTO createStopVisitDTO(StopVisit visit) {
        StopVisitDTO.StopVisitDTOBuilder builder = StopVisitDTO.builder()
                .stopId(visit.getStop().getId())
                .stopName(visit.getStop().getName())
                .locationId(visit.getStop().getLocation().getId())
                .locationName(visit.getStop().getLocation().getName())
                .visitedAt(visit.getVisitedAt())
                .hasPhotoProof(visit.getHasPhotoProof() != null ? visit.getHasPhotoProof() : false)
                .visitMethod(visit.getVisitMethod() != null ? visit.getVisitMethod().name() : "MANUAL");

        // Add photo URL if there's a photo ID
        if (visit.getPhotoId() != null) {
            Optional<Photo> photo = photoRepository.findById(visit.getPhotoId());
            if (photo.isPresent()) {
                builder.photoUrl(photo.get().getUrl());
            }
        }

        return builder.build();
    }
}