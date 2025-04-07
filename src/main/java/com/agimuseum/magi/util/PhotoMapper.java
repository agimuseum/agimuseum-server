package com.agimuseum.magi.util;

import com.agimuseum.magi.dto.PhotoDTO;
import com.agimuseum.magi.model.Photo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PhotoMapper {

    /**
     * Convert a Photo entity to a PhotoDTO
     * @param photo the Photo entity
     * @return the PhotoDTO
     */
    public PhotoDTO toDTO(Photo photo) {
        if (photo == null) {
            return null;
        }

        return PhotoDTO.builder()
                .id(photo.getId())
                .fileName(photo.getFileName())
                .contentType(photo.getContentType())
                .url(photo.getUrl())
                .locationId(photo.getLocationId())
                .locationName(photo.getLocationName())
                .stopId(photo.getStopId())
                .stopName(photo.getStopName())
                .uploaderName(photo.getUser() != null ?
                        photo.getUser().getFirstname() + " " + photo.getUser().getLastname() : "Unknown")
                .uploadedAt(photo.getUploadedAt())
                // Add S3-specific fields if needed
                .build();
    }

    /**
     * Convert a list of Photo entities to a list of PhotoDTOs
     * @param photos the list of Photo entities
     * @return the list of PhotoDTOs
     */
    public List<PhotoDTO> toDTOList(List<Photo> photos) {
        if (photos == null) {
            return List.of();
        }

        return photos.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}