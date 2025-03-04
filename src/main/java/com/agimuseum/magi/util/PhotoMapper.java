package com.agimuseum.magi.util;

import com.agimuseum.magi.dto.PhotoDTO;
import com.agimuseum.magi.model.Photo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PhotoMapper {

    public PhotoDTO toDTO(Photo photo) {
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

    public List<PhotoDTO> toDTOList(List<Photo> photos) {
        return photos.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}