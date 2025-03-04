package com.agimuseum.magi.controller;

import com.agimuseum.magi.dto.PhotoDTO;
import com.agimuseum.magi.model.Photo;
import com.agimuseum.magi.service.PhotoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/photos")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @PostMapping("/locations/{locationId}")
    public ResponseEntity<PhotoDTO> uploadLocationPhoto(
            @PathVariable Integer locationId,
            @RequestParam("file") MultipartFile file) throws IOException {

        Photo photo = photoService.uploadLocationPhoto(locationId, file);
        return new ResponseEntity<>(convertToDTO(photo), HttpStatus.CREATED);
    }

    @PostMapping("/stops/{stopId}")
    public ResponseEntity<PhotoDTO> uploadStopPhoto(
            @PathVariable Integer stopId,
            @RequestParam("file") MultipartFile file) throws IOException {

        Photo photo = photoService.uploadStopPhoto(stopId, file);
        return new ResponseEntity<>(convertToDTO(photo), HttpStatus.CREATED);
    }

    @DeleteMapping("/{photoId}")
    public ResponseEntity<Void> deletePhoto(@PathVariable Integer photoId) {
        photoService.deletePhoto(photoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-photos/locations/{locationId}")
    public ResponseEntity<List<PhotoDTO>> getMyLocationPhotos(@PathVariable Integer locationId) {
        List<Photo> photos = photoService.getUserPhotosForLocation(locationId);
        List<PhotoDTO> photoDTOs = photos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(photoDTOs);
    }

    @GetMapping("/my-photos/stops/{stopId}")
    public ResponseEntity<List<PhotoDTO>> getMyStopPhotos(@PathVariable Integer stopId) {
        List<Photo> photos = photoService.getUserPhotosForStop(stopId);
        List<PhotoDTO> photoDTOs = photos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(photoDTOs);
    }

    private PhotoDTO convertToDTO(Photo photo) {
        return PhotoDTO.builder()
                .id(photo.getId())
                .fileName(photo.getFileName())
                .contentType(photo.getContentType())
                .url(photo.getUrl())
                .locationId(photo.getLocationId())
                .locationName(photo.getLocationName())
                .stopId(photo.getStopId())
                .stopName(photo.getStopName())
                .uploaderName(photo.getUser().getFirstname() + " " + photo.getUser().getLastname())
                .uploadedAt(photo.getUploadedAt())
                .build();
    }
}