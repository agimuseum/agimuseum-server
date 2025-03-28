package com.agimuseum.magi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.agimuseum.magi.dto.LocationDTO;
import com.agimuseum.magi.dto.LocationDetailDTO;
import com.agimuseum.magi.dto.ParkingAreaDTO;
import com.agimuseum.magi.dto.StopDTO;
import com.agimuseum.magi.dto.StopLocationDTO;
import com.agimuseum.magi.model.Location;
import com.agimuseum.magi.model.LocationDetail;
import com.agimuseum.magi.model.ParkingArea;
import com.agimuseum.magi.model.Photo;
import com.agimuseum.magi.model.Stop;
import com.agimuseum.magi.repository.LocationDetailRepository;
import com.agimuseum.magi.repository.LocationRepository;
import com.agimuseum.magi.repository.ParkingAreaRepository;
import com.agimuseum.magi.repository.PhotoRepository;
import com.agimuseum.magi.repository.StopRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final LocationDetailRepository locationDetailRepository;
    private final ParkingAreaRepository parkingAreaRepository;
    private final StopRepository stopRepository;
    private final PhotoRepository photoRepository;

    public List<LocationDTO> getAllLocations() {
        List<Location> locations = locationRepository.findAll();
        return locations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public LocationDTO getLocationById(Integer id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + id));
        return convertToDTO(location);
    }

    /**
     * Convert a Location entity to a LocationDTO, with enhanced photo handling
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

        // Set photos - use sorted photos by upload date to get newest first
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

                    // Set stop photos - use sorted photos by upload date to get newest first
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
     * Get a list of photos for a location
     */
    public List<String> getLocationPhotos(Integer locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + locationId));

        List<Photo> photos = photoRepository.findByLocationIdOrderByUploadedAtDesc(locationId);
        return photos.stream()
                .map(Photo::getUrl)
                .collect(Collectors.toList());
    }

    /**
     * Get a list of photos for a stop
     */
    public List<String> getStopPhotos(Integer stopId) {
        Stop stop = stopRepository.findById(stopId)
                .orElseThrow(() -> new RuntimeException("Stop not found with id: " + stopId));

        List<Photo> photos = photoRepository.findByStopIdOrderByUploadedAtDesc(stopId);
        return photos.stream()
                .map(Photo::getUrl)
                .collect(Collectors.toList());
    }
}