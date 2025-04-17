package com.agimuseum.magi.controller;

import com.agimuseum.magi.dto.PhotoDTO;
import com.agimuseum.magi.exception.ResourceNotFoundException;
import com.agimuseum.magi.model.Photo;
import com.agimuseum.magi.model.PhotoType;
import com.agimuseum.magi.repository.PhotoRepository;
import com.agimuseum.magi.service.PhotoService;
import com.agimuseum.magi.util.ApiErrorUtil;
import com.agimuseum.magi.util.PhotoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin controller for managing photos
 */
@RestController
@RequestMapping("/api/admin/photos")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminPhotoController {

    private final PhotoRepository photoRepository;
    private final PhotoMapper photoMapper;
    private final PhotoService photoService;

    /**
     * Get all photos pending approval
     */
    @GetMapping("/pending")
    public ResponseEntity<List<PhotoDTO>> getPendingPhotos() {
        log.info("Admin fetching photos pending approval");

        List<Photo> pendingPhotos = photoRepository.findByApprovedFalseOrderByUploadedAtAsc();

        return ResponseEntity.ok(pendingPhotos.stream()
                .map(photoMapper::toDTO)
                .collect(Collectors.toList()));
    }

    /**
     * Get all stock photos pending approval
     */
    @GetMapping("/pending/stock")
    public ResponseEntity<List<PhotoDTO>> getPendingStockPhotos() {
        log.info("Admin fetching stock photos pending approval");

        List<Photo> pendingPhotos = photoRepository
                .findByPhotoTypeAndApprovedFalseOrderByUploadedAtAsc(PhotoType.STOCK);

        return ResponseEntity.ok(pendingPhotos.stream()
                .map(photoMapper::toDTO)
                .collect(Collectors.toList()));
    }

    /**
     * Approve a photo
     */
    @PostMapping("/{photoId}/approve")
    public ResponseEntity<?> approvePhoto(@PathVariable Integer photoId) {
        log.info("Admin approving photo with ID: {}", photoId);

        try {
            Photo photo = photoRepository.findById(photoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + photoId));

            photo.setApproved(true);
            Photo updatedPhoto = photoRepository.save(photo);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Photo approved successfully");
            response.put("photo", photoMapper.toDTO(updatedPhoto));

            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ApiErrorUtil.createErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error approving photo", e);
            return ApiErrorUtil.createErrorResponse("Failed to approve photo", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Reject a photo (delete it)
     */
    @DeleteMapping("/{photoId}/reject")
    public ResponseEntity<?> rejectPhoto(@PathVariable Integer photoId) {
        log.info("Admin rejecting photo with ID: {}", photoId);

        try {
            Photo photo = photoRepository.findById(photoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + photoId));

            // Delete the photo
            photoService.deletePhoto(photoId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Photo rejected and deleted successfully");

            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ApiErrorUtil.createErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error rejecting photo", e);
            return ApiErrorUtil.createErrorResponse("Failed to reject photo", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Bulk approve photos
     */
    @PostMapping("/approve")
    public ResponseEntity<?> bulkApprovePhotos(@RequestBody List<Integer> photoIds) {
        log.info("Admin bulk approving {} photos", photoIds.size());

        try {
            int approvedCount = 0;

            for (Integer photoId : photoIds) {
                try {
                    Photo photo = photoRepository.findById(photoId).orElse(null);
                    if (photo != null) {
                        photo.setApproved(true);
                        photoRepository.save(photo);
                        approvedCount++;
                    }
                } catch (Exception e) {
                    log.warn("Error approving photo with ID {}: {}", photoId, e.getMessage());
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", approvedCount + " photos approved successfully");
            response.put("totalRequested", photoIds.size());
            response.put("approvedCount", approvedCount);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during bulk approval", e);
            return ApiErrorUtil.createErrorResponse("Failed to process bulk approval", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Change a photo type (e.g., from STOCK to VISIT_PROOF or vice versa)
     */
    @PostMapping("/{photoId}/change-type")
    public ResponseEntity<?> changePhotoType(
            @PathVariable Integer photoId,
            @RequestParam PhotoType newType) {

        log.info("Admin changing photo type for ID: {} to {}", photoId, newType);

        try {
            Photo photo = photoRepository.findById(photoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + photoId));

            // Update the photo type
            photo.setPhotoType(newType);

            // If changing to VISIT_PROOF, automatically approve
            if (PhotoType.VISIT_PROOF.equals(newType)) {
                photo.setApproved(true);
            }

            Photo updatedPhoto = photoRepository.save(photo);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Photo type changed successfully");
            response.put("photo", photoMapper.toDTO(updatedPhoto));

            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ApiErrorUtil.createErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error changing photo type", e);
            return ApiErrorUtil.createErrorResponse("Failed to change photo type", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}