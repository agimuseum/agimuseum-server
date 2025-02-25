package com.agimuseum.magi.util;

import com.agimuseum.magi.dto.RegisterRequest;
import com.agimuseum.magi.dto.UserDTO;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Helper class for additional validation logic not covered by annotations
 */
@Component
public class ValidationHelper implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return RegisterRequest.class.isAssignableFrom(clazz) ||
                UserDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (target instanceof RegisterRequest) {
            validateRegisterRequest((RegisterRequest) target, errors);
        } else if (target instanceof UserDTO) {
            validateUserDTO((UserDTO) target, errors);
        }
    }

    private void validateRegisterRequest(RegisterRequest request, Errors errors) {
        validateHotelInformation(
                request.isNightInHotel(),
                request.getHotelName(),
                request.getNumberOfNights(),
                request.getNumberOfRooms(),
                errors
        );
    }

    private void validateUserDTO(UserDTO userDTO, Errors errors) {
        validateHotelInformation(
                userDTO.isNightInHotel(),
                userDTO.getHotelName(),
                userDTO.getNumberOfNights(),
                userDTO.getNumberOfRooms(),
                errors
        );
    }

    private void validateHotelInformation(
            boolean isNightInHotel,
            String hotelName,
            Integer numberOfNights,
            Integer numberOfRooms,
            Errors errors) {

        if (isNightInHotel) {
            // Validate hotel-related fields when staying in hotel
            if (hotelName == null || hotelName.trim().isEmpty()) {
                errors.rejectValue("hotelName", "hotel.name.required",
                        "Hotel name is required when staying overnight");
            }

            if (numberOfNights == null || numberOfNights < 1) {
                errors.rejectValue("numberOfNights", "hotel.nights.required",
                        "Number of nights must be at least 1 when staying overnight");
            }

            if (numberOfRooms == null || numberOfRooms < 1) {
                errors.rejectValue("numberOfRooms", "hotel.rooms.required",
                        "Number of rooms must be at least 1 when staying overnight");
            }
        }
    }
}