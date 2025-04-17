package com.agimuseum.magi.service.impl;

import com.agimuseum.magi.model.Location;
import com.agimuseum.magi.model.LocationDetail;
import com.agimuseum.magi.model.ParkingArea;
import com.agimuseum.magi.model.Stop;
import com.agimuseum.magi.repository.LocationDetailRepository;
import com.agimuseum.magi.repository.LocationRepository;
import com.agimuseum.magi.repository.ParkingAreaRepository;
import com.agimuseum.magi.repository.StopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for importing locations, stops, and parking areas from CSV files
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocationImportService {

    private final LocationRepository locationRepository;
    private final LocationDetailRepository locationDetailRepository;
    private final StopRepository stopRepository;
    private final ParkingAreaRepository parkingAreaRepository;

    /**
     * Import locations from a CSV file
     * Expected format: id,name,summary,weblink,address,latitude,longitude,geo_fence_radius
     */
    @Transactional
    public Map<String, Object> importLocationsFromCsv(MultipartFile file) throws IOException {
        log.info("Starting location import from CSV");

        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int importedCount = 0;
        int errorCount = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            // Skip header row
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                try {
                    String[] data = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

                    // Clean quoted values
                    for (int i = 0; i < data.length; i++) {
                        if (data[i].startsWith("\"") && data[i].endsWith("\"")) {
                            data[i] = data[i].substring(1, data[i].length() - 1);
                        }
                    }

                    if (data.length < 8) {
                        errors.add("Invalid data format in row: " + line);
                        errorCount++;
                        continue;
                    }

                    // Parse location data
                    Integer id = Integer.parseInt(data[0].trim());
                    String name = data[1].trim();
                    String summary = data[2].trim();
                    String weblink = data[3].trim();
                    String address = data[4].trim();
                    Double latitude = Double.parseDouble(data[5].trim());
                    Double longitude = Double.parseDouble(data[6].trim());
                    Integer geoFenceRadius = Integer.parseInt(data[7].trim());

                    // Create or update location
                    Location location = locationRepository.findById(id).orElse(new Location());
                    location.setId(id);
                    location.setName(name);
                    location.setSummary(summary);
                    location.setWeblink(weblink);

                    // Save location first to get ID
                    location = locationRepository.save(location);

                    // Create or update location detail
                    LocationDetail locationDetail = locationDetailRepository.findByLocationId(id);
                    if (locationDetail == null) {
                        locationDetail = new LocationDetail();
                        locationDetail.setLocation(location);
                    }

                    locationDetail.setAddress(address);
                    locationDetail.setLatitude(latitude);
                    locationDetail.setLongitude(longitude);
                    locationDetail.setGeoFenceRadius(geoFenceRadius);

                    locationDetailRepository.save(locationDetail);

                    importedCount++;
                } catch (Exception e) {
                    log.error("Error processing location row: " + line, e);
                    errors.add("Error processing row: " + line + " - " + e.getMessage());
                    errorCount++;
                }
            }
        }

        result.put("importedCount", importedCount);
        result.put("errorCount", errorCount);
        result.put("errors", errors);

        log.info("Completed location import. Imported: {}, Errors: {}", importedCount, errorCount);

        return result;
    }

    /**
     * Import stops from a CSV file
     * Expected format: id,location_id,name,summary,weblink,latitude,longitude,geo_fence_radius
     */
    @Transactional
    public Map<String, Object> importStopsFromCsv(MultipartFile file) throws IOException {
        log.info("Starting stops import from CSV");

        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int importedCount = 0;
        int errorCount = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            // Skip header row
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                try {
                    String[] data = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

                    // Clean quoted values
                    for (int i = 0; i < data.length; i++) {
                        if (data[i].startsWith("\"") && data[i].endsWith("\"")) {
                            data[i] = data[i].substring(1, data[i].length() - 1);
                        }
                    }

                    if (data.length < 8) {
                        errors.add("Invalid data format in row: " + line);
                        errorCount++;
                        continue;
                    }

                    // Parse stop data
                    Integer id = Integer.parseInt(data[0].trim());
                    Integer locationId = Integer.parseInt(data[1].trim());
                    String name = data[2].trim();
                    String summary = data[3].trim();
                    String weblink = data[4].trim();
                    Double latitude = Double.parseDouble(data[5].trim());
                    Double longitude = Double.parseDouble(data[6].trim());
                    Integer geoFenceRadius = Integer.parseInt(data[7].trim());

                    // Check if location exists
                    Location location = locationRepository.findById(locationId)
                            .orElseThrow(() -> new RuntimeException("Location not found with id: " + locationId));

                    // Create or update stop
                    Stop stop = stopRepository.findById(id).orElse(new Stop());
                    stop.setId(id);
                    stop.setLocation(location);
                    stop.setName(name);
                    stop.setSummary(summary);
                    stop.setWeblink(weblink);
                    stop.setLatitude(latitude);
                    stop.setLongitude(longitude);
                    stop.setGeoFenceRadius(geoFenceRadius);

                    stopRepository.save(stop);

                    importedCount++;
                } catch (Exception e) {
                    log.error("Error processing stop row: " + line, e);
                    errors.add("Error processing row: " + line + " - " + e.getMessage());
                    errorCount++;
                }
            }
        }

        result.put("importedCount", importedCount);
        result.put("errorCount", errorCount);
        result.put("errors", errors);

        log.info("Completed stops import. Imported: {}, Errors: {}", importedCount, errorCount);

        return result;
    }

    /**
     * Import parking areas from a CSV file
     * Expected format: id,location_id,name,latitude,longitude
     */
    @Transactional
    public Map<String, Object> importParkingAreasFromCsv(MultipartFile file) throws IOException {
        log.info("Starting parking areas import from CSV");

        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int importedCount = 0;
        int errorCount = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            // Skip header row
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                try {
                    String[] data = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

                    // Clean quoted values
                    for (int i = 0; i < data.length; i++) {
                        if (data[i].startsWith("\"") && data[i].endsWith("\"")) {
                            data[i] = data[i].substring(1, data[i].length() - 1);
                        }
                    }

                    if (data.length < 5) {
                        errors.add("Invalid data format in row: " + line);
                        errorCount++;
                        continue;
                    }

                    // Parse parking area data
                    Integer id = Integer.parseInt(data[0].trim());
                    Integer locationId = Integer.parseInt(data[1].trim());
                    String name = data[2].trim();
                    Double latitude = Double.parseDouble(data[3].trim());
                    Double longitude = Double.parseDouble(data[4].trim());

                    // Check if location exists
                    Location location = locationRepository.findById(locationId)
                            .orElseThrow(() -> new RuntimeException("Location not found with id: " + locationId));

                    // Create or update parking area
                    ParkingArea parkingArea = parkingAreaRepository.findById(id).orElse(new ParkingArea());
                    parkingArea.setId(id);
                    parkingArea.setLocation(location);
                    parkingArea.setName(name);
                    parkingArea.setLatitude(latitude);
                    parkingArea.setLongitude(longitude);

                    parkingAreaRepository.save(parkingArea);

                    importedCount++;
                } catch (Exception e) {
                    log.error("Error processing parking area row: " + line, e);
                    errors.add("Error processing row: " + line + " - " + e.getMessage());
                    errorCount++;
                }
            }
        }

        result.put("importedCount", importedCount);
        result.put("errorCount", errorCount);
        result.put("errors", errors);

        log.info("Completed parking areas import. Imported: {}, Errors: {}", importedCount, errorCount);

        return result;
    }
}