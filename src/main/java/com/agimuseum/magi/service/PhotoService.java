package com.agimuseum.magi.service;

import com.agimuseum.magi.exception.ResourceNotFoundException;
import com.agimuseum.magi.model.Location;
import com.agimuseum.magi.model.Photo;
import com.agimuseum.magi.model.Stop;
import com.agimuseum.magi.model.User;
import com.agimuseum.magi.repository.LocationRepository;
import com.agimuseum.magi.repository.PhotoRepository;
import com.agimuseum.magi.repository.StopRepository;
import com.agimuseum.magi.repository.UserRepository;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Objects;

@Service
public class PhotoService {

    private final Storage storage;
    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final StopRepository stopRepository;

    @Value("${firebase.storage.bucket}")
    private String storageBucket;

    public PhotoService(Storage storage,
                        PhotoRepository photoRepository,
                        UserRepository userRepository,
                        LocationRepository locationRepository,
                        StopRepository stopRepository) {
        this.storage = storage;
        this.photoRepository = photoRepository;
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.stopRepository = stopRepository;
    }

    public Photo uploadLocationPhoto(Integer locationId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        // Get current user
        User currentUser = getCurrentUser();

        // Get location
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + locationId));

        // Check if user has already uploaded 3 photos for this location
        long photoCount = photoRepository.countByUserAndLocationId(currentUser, locationId);
        if (photoCount >= 3) {
            throw new IllegalStateException("Maximum number of photos (3) already uploaded for this location");
        }

        // Generate safe filename
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename(), "Filename cannot be null");
        String fileName = generateFileName(file);
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        String firebasePath = "locations/" + locationId + "/" + fileName;

        BlobId blobId = BlobId.of(storageBucket, firebasePath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();

        Blob blob = storage.create(blobInfo, file.getBytes());
        String url = "https://storage.googleapis.com/" + storageBucket + "/" + firebasePath;

        // Create and save photo entity with explicit fileName set
        Photo photo = Photo.builder()
                .fileName(fileName)
                .contentType(contentType)
                .contentTypeField(contentType)
                .url(url)
                .photoUrl(url)
                .firebasePath(firebasePath)
                .referenceId(UUID.randomUUID().toString())
                .locationId(locationId)
                .locationName(location.getName())
                .user(currentUser)
                .uploadedAt(new Date())
                .build();

        return photoRepository.save(photo);
    }

    public Photo uploadStopPhoto(Integer stopId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        // Get current user
        User currentUser = getCurrentUser();

        // Get stop
        Stop stop = stopRepository.findById(stopId)
                .orElseThrow(() -> new ResourceNotFoundException("Stop not found with id: " + stopId));

        // Check if user has already uploaded 3 photos for this stop
        long photoCount = photoRepository.countByUserAndStopId(currentUser, stopId);
        if (photoCount >= 3) {
            throw new IllegalStateException("Maximum number of photos (3) already uploaded for this stop");
        }

        // Generate safe filename
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename(), "Filename cannot be null");
        String fileName = generateFileName(file);
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        String firebasePath = "stops/" + stopId + "/" + fileName;

        BlobId blobId = BlobId.of(storageBucket, firebasePath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();

        Blob blob = storage.create(blobInfo, file.getBytes());
        String url = "https://storage.googleapis.com/" + storageBucket + "/" + firebasePath;

        // Create and save photo entity
        Photo photo = Photo.builder()
                .fileName(fileName)
                .contentType(contentType)
                .contentTypeField(contentType)
                .url(url)
                .photoUrl(url)
                .firebasePath(firebasePath)
                .referenceId(UUID.randomUUID().toString())
                .locationId(stop.getLocation().getId())
                .locationName(stop.getLocation().getName())
                .stopId(stopId)
                .stopName(stop.getName())
                .user(currentUser)
                .uploadedAt(new Date())
                .build();

        return photoRepository.save(photo);
    }

    public void deletePhoto(Integer photoId) {
        // Get current user
        User currentUser = getCurrentUser();

        // Get photo
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + photoId));

        // Check if the photo belongs to the current user
        if (!photo.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("You don't have permission to delete this photo");
        }

        // Delete from Firebase
        BlobId blobId = BlobId.of(storageBucket, photo.getFirebasePath());
        boolean deleted = storage.delete(blobId);

        if (deleted) {
            // Delete from database
            photoRepository.delete(photo);
        } else {
            throw new IllegalStateException("Failed to delete photo from storage");
        }
    }

    public List<Photo> getUserPhotosForLocation(Integer locationId) {
        User currentUser = getCurrentUser();
        return photoRepository.findByUserAndLocationId(currentUser, locationId);
    }

    public List<Photo> getUserPhotosForStop(Integer stopId) {
        User currentUser = getCurrentUser();
        return photoRepository.findByUserAndStopId(currentUser, stopId);
    }

    public long getLocationPhotoCount(User user, Integer locationId) {
        return photoRepository.countByUserAndLocationId(user, locationId);
    }

    public long getStopPhotoCount(User user, Integer stopId) {
        return photoRepository.countByUserAndStopId(user, stopId);
    }

    public List<Photo> getAllLocationPhotos(Integer locationId) {
        // Verify location exists
        locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + locationId));
        return photoRepository.findByLocationId(locationId);
    }

    public List<Photo> getAllStopPhotos(Integer stopId) {
        // Verify stop exists
        stopRepository.findById(stopId)
                .orElseThrow(() -> new ResourceNotFoundException("Stop not found with id: " + stopId));
        return photoRepository.findByStopId(stopId);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
    }

    private String generateFileName(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}