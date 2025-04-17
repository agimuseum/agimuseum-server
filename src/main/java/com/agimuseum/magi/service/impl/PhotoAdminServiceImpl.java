package com.agimuseum.magi.service.impl;

import com.agimuseum.magi.exception.ResourceNotFoundException;
import com.agimuseum.magi.model.Photo;
import com.agimuseum.magi.model.PhotoType;
import com.agimuseum.magi.repository.PhotoRepository;
import com.agimuseum.magi.service.PhotoAdminService;
import com.agimuseum.magi.service.PhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the PhotoAdminService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoAdminServiceImpl implements PhotoAdminService {

    private final PhotoRepository photoRepository;
    private final PhotoService photoService;

    @Override
    @Transactional
    public Photo approvePhoto(Integer photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + photoId));

        photo.setApproved(true);
        return photoRepository.save(photo);
    }

    @Override
    @Transactional
    public void rejectPhoto(Integer photoId) throws IOException {
        // This will both delete from S3 and the database
        photoService.deletePhoto(photoId);
    }

    @Override
    @Transactional
    public Photo changePhotoType(Integer photoId, PhotoType newType) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + photoId));

        photo.setPhotoType(newType);

        // If changing to VISIT_PROOF, automatically approve
        if (PhotoType.VISIT_PROOF.equals(newType)) {
            photo.setApproved(true);
        }

        return photoRepository.save(photo);
    }

    @Override
    @Transactional
    public Map<String, Object> bulkApprovePhotos(List<Integer> photoIds) {
        int totalRequested = photoIds.size();
        int approvedCount = 0;
        int errorCount = 0;

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
                errorCount++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalRequested", totalRequested);
        result.put("approvedCount", approvedCount);
        result.put("errorCount", errorCount);
        result.put("success", errorCount == 0);

        return result;
    }

    @Override
    public List<Photo> getPendingPhotos() {
        return photoRepository.findByApprovedFalseOrderByUploadedAtAsc();
    }

    @Override
    public List<Photo> getPendingPhotosByType(PhotoType photoType) {
        return photoRepository.findByPhotoTypeAndApprovedFalseOrderByUploadedAtAsc(photoType);
    }

    @Override
    @Transactional
    public Photo addPhotoDescription(Integer photoId, String description) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + photoId));

        photo.setDescription(description);
        return photoRepository.save(photo);
    }
}