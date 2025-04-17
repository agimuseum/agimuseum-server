package com.agimuseum.magi.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agimuseum.magi.dto.LocationDTO;
import com.agimuseum.magi.dto.LocationDetailDTO;
import com.agimuseum.magi.dto.ParkingAreaDTO;
import com.agimuseum.magi.dto.StopDTO;
import com.agimuseum.magi.dto.StopLocationDTO;
import com.agimuseum.magi.exception.ResourceNotFoundException;
import com.agimuseum.magi.model.Location;
import com.agimuseum.magi.model.LocationDetail;
import com.agimuseum.magi.model.ParkingArea;
import com.agimuseum.magi.model.Photo;
import com.agimuseum.magi.model.PhotoType;
import com.agimuseum.magi.model.Stop;
import com.agimuseum.magi.repository.LocationDetailRepository;
import com.agimuseum.magi.repository.LocationRepository;
import com.agimuseum.magi.repository.ParkingAreaRepository;
import com.agimuseum.magi.repository.PhotoRepository;
import com.agimuseum.magi.repository.StopRepository;
import com.agimuseum.magi.service.LocationService;
import com.agimuseum.magi.service.S3StorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final LocationDetailRepository locationDetailRepository;
    private final ParkingAreaRepository parkingAreaRepository;
    private final StopRepository stopRepository;
    private final PhotoRepository photoRepository;
    private final S3StorageService s3StorageService;

    @Override
    public List<LocationDTO> getAllLocations() {
        log.debug("Fetching all locations");
        List<Location> locations = locationRepository.findAll();
        return locations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LocationDTO getLocationById(Integer id) {
        log.debug("Fetching location with ID: {}", id);
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));
        return convertToDTO(location);
    }


    /**
     * Get a list of photos for a location
     * Modified to return all photos without filtering by approval status
     */
    @Override
    public List<String> getLocationPhotos(Integer locationId) {
        log.debug("Fetching photos for location ID: {}", locationId);
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + locationId));

        // Get all photos for this location
        List<Photo> photos = photoRepository.findByLocationIdOrderByUploadedAtDesc(locationId);
        return photos.stream()
                .map(Photo::getUrl)
                .collect(Collectors.toList());
    }

    /**
     * Get a list of photos for a stop
     * Modified to return all photos without filtering by approval status
     */
    @Override
    public List<String> getStopPhotos(Integer stopId) {
        log.debug("Fetching photos for stop ID: {}", stopId);
        Stop stop = stopRepository.findById(stopId)
                .orElseThrow(() -> new ResourceNotFoundException("Stop not found with id: " + stopId));

        // Get all photos for this stop
        List<Photo> photos = photoRepository.findByStopIdOrderByUploadedAtDesc(stopId);
        return photos.stream()
                .map(Photo::getUrl)
                .collect(Collectors.toList());
    }

    /**
     * Convert a Location entity to a LocationDTO
     * Modified to include all photos without filtering by approval status
     */
    private LocationDTO convertToDTO(Location location) {
        LocationDTO dto = new LocationDTO();
        dto.setId(location.getId());
        dto.setName(location.getName());
        dto.setSummary(location.getSummary());
        dto.setWeblink(location.getWeblink());

        // Set location details
        LocationDetail detail = locationDetailRepository.findByLocationId(location.getId());
        if (detail != null) {
            LocationDetailDTO detailDTO = new LocationDetailDTO();
            detailDTO.setAddress(detail.getAddress());
            detailDTO.setLatitude(detail.getLatitude());
            detailDTO.setLongitude(detail.getLongitude());
            detailDTO.setGeoFenceRadius(detail.getGeoFenceRadius());

            // Set parking areas
            List<ParkingArea> parkingAreas = parkingAreaRepository.findByLocationId(location.getId());
            List<ParkingAreaDTO> parkingAreaDTOs = parkingAreas.stream()
                    .map(pa -> new ParkingAreaDTO(pa.getName(), pa.getLatitude(), pa.getLongitude()))
                    .collect(Collectors.toList());
            detailDTO.setNearbyParkingAreas(parkingAreaDTOs);

            dto.setLocation(detailDTO);
        }

        // Set all photos - no longer filtering by approval status
        List<Photo> photos = photoRepository.findByLocationIdOrderByUploadedAtDesc(location.getId());
        dto.setPhotos(photos.stream()
                .map(Photo::getUrl)
                .collect(Collectors.toList()));

        // Set stops
        List<Stop> stops = stopRepository.findByLocationId(location.getId());
        List<StopDTO> stopDTOs = stops.stream()
                .map(stop -> {
                    StopDTO stopDTO = new StopDTO();
                    stopDTO.setId(stop.getId());
                    stopDTO.setName(stop.getName());
                    stopDTO.setSummary(stop.getSummary());
                    stopDTO.setWeblink(stop.getWeblink());

                    StopLocationDTO stopLocationDTO = new StopLocationDTO();
                    stopLocationDTO.setLatitude(stop.getLatitude());
                    stopLocationDTO.setLongitude(stop.getLongitude());
                    stopLocationDTO.setGeoFenceRadius(stop.getGeoFenceRadius());
                    stopDTO.setLocation(stopLocationDTO);

                    // Set all photos for the stop - no longer filtering by approval status
                    List<Photo> stopPhotos = photoRepository.findByStopIdOrderByUploadedAtDesc(stop.getId());
                    stopDTO.setPhotos(stopPhotos.stream()
                            .map(Photo::getUrl)
                            .collect(Collectors.toList()));

                    return stopDTO;
                })
                .collect(Collectors.toList());
        dto.setStops(stopDTOs);

        return dto;
    }




    /**
     * Create a new location
     */
    @Override
    @Transactional
    public Location createLocation(Location location, LocationDetail locationDetail) {
        log.debug("Creating new location: {}", location.getName());

        // Save the location first to get an ID
        Location savedLocation = locationRepository.save(location);

        // Set the location reference in the detail and save it
        locationDetail.setLocation(savedLocation);
        locationDetailRepository.save(locationDetail);

        log.info("Created new location with ID: {}", savedLocation.getId());
        return savedLocation;
    }

    /**
     * Update an existing location
     */
    @Override
    @Transactional
    public Location updateLocation(Integer id, Location location, LocationDetail locationDetail) {
        log.debug("Updating location with ID: {}", id);

        // Check if location exists
        Location existingLocation = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));

        // Update location fields
        existingLocation.setName(location.getName());
        existingLocation.setSummary(location.getSummary());
        existingLocation.setWeblink(location.getWeblink());

        // Save updated location
        Location updatedLocation = locationRepository.save(existingLocation);

        // Update location detail
        LocationDetail existingDetail = locationDetailRepository.findByLocationId(id);
        if (existingDetail != null) {
            existingDetail.setAddress(locationDetail.getAddress());
            existingDetail.setLatitude(locationDetail.getLatitude());
            existingDetail.setLongitude(locationDetail.getLongitude());
            existingDetail.setGeoFenceRadius(locationDetail.getGeoFenceRadius());
            locationDetailRepository.save(existingDetail);
        } else {
            // Create new detail if it doesn't exist
            locationDetail.setLocation(updatedLocation);
            locationDetailRepository.save(locationDetail);
        }

        log.info("Updated location with ID: {}", id);
        return updatedLocation;
    }

    /**
     * Delete a location
     */
    @Override
    @Transactional
    public void deleteLocation(Integer id) {
        log.debug("Deleting location with ID: {}", id);

        // Check if location exists
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));

        // Delete associated photos from S3
        List<Photo> photos = photoRepository.findByLocationId(id);
        for (Photo photo : photos) {
            try {
                if (photo.getS3Key() != null) {
                    s3StorageService.deleteFile(photo.getS3Key());
                }
            } catch (Exception e) {
                log.error("Failed to delete photo from S3: {}", e.getMessage());
            }
        }

        // Delete the location (cascades to details, parking areas, stops, and visits)
        locationRepository.delete(location);

        log.info("Deleted location with ID: {}", id);
    }

    /**
     * Add a stop to a location
     */
    @Override
    @Transactional
    public Stop addStopToLocation(Integer locationId, Stop stop) {
        log.debug("Adding stop to location ID: {}", locationId);

        // Check if location exists
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + locationId));

        // Set the location and save the stop
        stop.setLocation(location);
        Stop savedStop = stopRepository.save(stop);

        log.info("Added stop with ID: {} to location ID: {}", savedStop.getId(), locationId);
        return savedStop;
    }

    /**
     * Update a stop
     */
    @Override
    @Transactional
    public Stop updateStop(Integer stopId, Stop stop) {
        log.debug("Updating stop with ID: {}", stopId);

        // Check if stop exists
        Stop existingStop = stopRepository.findById(stopId)
                .orElseThrow(() -> new ResourceNotFoundException("Stop not found with id: " + stopId));

        // Update stop fields
        existingStop.setName(stop.getName());
        existingStop.setSummary(stop.getSummary());
        existingStop.setWeblink(stop.getWeblink());
        existingStop.setLatitude(stop.getLatitude());
        existingStop.setLongitude(stop.getLongitude());
        existingStop.setGeoFenceRadius(stop.getGeoFenceRadius());

        // Save updated stop
        Stop updatedStop = stopRepository.save(existingStop);

        log.info("Updated stop with ID: {}", stopId);
        return updatedStop;
    }

    /**
     * Delete a stop
     */
    @Override
    @Transactional
    public void deleteStop(Integer stopId) {
        log.debug("Deleting stop with ID: {}", stopId);

        // Check if stop exists
        Stop stop = stopRepository.findById(stopId)
                .orElseThrow(() -> new ResourceNotFoundException("Stop not found with id: " + stopId));

        // Delete associated photos from S3
        List<Photo> photos = photoRepository.findByStopId(stopId);
        for (Photo photo : photos) {
            try {
                if (photo.getS3Key() != null) {
                    s3StorageService.deleteFile(photo.getS3Key());
                }
            } catch (Exception e) {
                log.error("Failed to delete photo from S3: {}", e.getMessage());
            }
        }

        // Delete the stop (cascades to visits)
        stopRepository.delete(stop);

        log.info("Deleted stop with ID: {}", stopId);
    }

    /**
     * Find locations near a given geographic coordinate
     */
    @Override
    public List<LocationDTO> findNearbyLocations(Double latitude, Double longitude, Double radiusInMeters) {
        log.debug("Finding locations near lat: {}, long: {}, radius: {} meters", latitude, longitude, radiusInMeters);

        // This is a simplified implementation - for a real app, you would use
        // a spatial query with a database that supports geographic calculations

        // Get all locations
        List<Location> allLocations = locationRepository.findAll();
        List<Location> nearbyLocations = new ArrayList<>();

        // Filter by distance
        for (Location location : allLocations) {
            LocationDetail detail = locationDetailRepository.findByLocationId(location.getId());
            if (detail != null && detail.getLatitude() != null && detail.getLongitude() != null) {
                double distance = calculateDistance(
                        latitude, longitude,
                        detail.getLatitude(), detail.getLongitude());

                // Convert to meters and check if within radius
                if (distance * 1000 <= radiusInMeters) {
                    nearbyLocations.add(location);
                }
            }
        }

        // Convert to DTOs
        return nearbyLocations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Calculate distance between two points using the Haversine formula
     * @return Distance in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // Earth radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    /**
     * Search for locations by name or description
     */
    @Override
    public List<LocationDTO> searchLocations(String query) {
        log.debug("Searching locations with query: {}", query);

        if (query == null || query.trim().isEmpty()) {
            return getAllLocations();
        }

        String searchTerm = "%" + query.toLowerCase() + "%";

        // Get all locations (in a real app, you would use a database query with LIKE)
        List<Location> allLocations = locationRepository.findAll();
        List<Location> matchingLocations = allLocations.stream()
                .filter(location ->
                        (location.getName() != null && location.getName().toLowerCase().contains(query.toLowerCase())) ||
                                (location.getSummary() != null && location.getSummary().toLowerCase().contains(query.toLowerCase()))
                )
                .collect(Collectors.toList());

        // Convert to DTOs
        return matchingLocations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}