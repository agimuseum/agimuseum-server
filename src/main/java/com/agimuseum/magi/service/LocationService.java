package com.agimuseum.magi.service;

import java.util.List;

import com.agimuseum.magi.dto.LocationDTO;
import com.agimuseum.magi.model.Location;
import com.agimuseum.magi.model.LocationDetail;
import com.agimuseum.magi.model.Stop;

/**
 * Service interface for location-related operations
 */
public interface LocationService {

    /**
     * Get all locations
     * @return List of all locations as DTOs
     */
    List<LocationDTO> getAllLocations();

    /**
     * Get a location by ID
     * @param id The location ID
     * @return The location as a DTO
     */
    LocationDTO getLocationById(Integer id);

    /**
     * Get a list of stock photos for a location (not visit proof photos)
     * @param locationId The location ID
     * @return List of photo URLs
     */
    List<String> getLocationPhotos(Integer locationId);

    /**
     * Get a list of stock photos for a stop (not visit proof photos)
     * @param stopId The stop ID
     * @return List of photo URLs
     */
    List<String> getStopPhotos(Integer stopId);

    /**
     * Create a new location
     * @param location The location to create
     * @param locationDetail The location details
     * @return The created location
     */
    Location createLocation(Location location, LocationDetail locationDetail);

    /**
     * Update an existing location
     * @param id The location ID
     * @param location The updated location data
     * @param locationDetail The updated location details
     * @return The updated location
     */
    Location updateLocation(Integer id, Location location, LocationDetail locationDetail);

    /**
     * Delete a location
     * @param id The location ID to delete
     */
    void deleteLocation(Integer id);

    /**
     * Add a stop to a location
     * @param locationId The location ID
     * @param stop The stop to add
     * @return The created stop
     */
    Stop addStopToLocation(Integer locationId, Stop stop);

    /**
     * Update a stop
     * @param stopId The stop ID
     * @param stop The updated stop data
     * @return The updated stop
     */
    Stop updateStop(Integer stopId, Stop stop);

    /**
     * Delete a stop
     * @param stopId The stop ID to delete
     */
    void deleteStop(Integer stopId);

    /**
     * Find locations near a given geographic coordinate
     * @param latitude The latitude coordinate
     * @param longitude The longitude coordinate
     * @param radiusInMeters The search radius in meters
     * @return List of locations within the specified radius
     */
    List<LocationDTO> findNearbyLocations(Double latitude, Double longitude, Double radiusInMeters);

    /**
     * Search for locations by name or description
     * @param query The search query
     * @return List of matching locations
     */
    List<LocationDTO> searchLocations(String query);
}