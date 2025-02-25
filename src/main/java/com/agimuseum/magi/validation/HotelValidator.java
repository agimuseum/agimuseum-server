package com.agimuseum.magi.validation;

import com.agimuseum.magi.dto.RegisterRequest;
import com.agimuseum.magi.dto.UserDTO;
import com.agimuseum.magi.model.User;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class HotelValidator implements ConstraintValidator<ConditionalHotelValidation, Object> {

    @Override
    public void initialize(ConditionalHotelValidation constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        boolean isNightInHotel;
        String hotelName;
        Integer numberOfNights;
        Integer numberOfRooms;

        // Extract the relevant fields based on object type
        if (object instanceof RegisterRequest) {
            RegisterRequest request = (RegisterRequest) object;
            isNightInHotel = request.isNightInHotel();
            hotelName = request.getHotelName();
            numberOfNights = request.getNumberOfNights();
            numberOfRooms = request.getNumberOfRooms();
        } else if (object instanceof UserDTO) {
            UserDTO userDTO = (UserDTO) object;
            isNightInHotel = userDTO.isNightInHotel();
            hotelName = userDTO.getHotelName();
            numberOfNights = userDTO.getNumberOfNights();
            numberOfRooms = userDTO.getNumberOfRooms();
        } else if (object instanceof User) {
            User user = (User) object;
            isNightInHotel = user.isNightInHotel();
            hotelName = user.getHotelName();
            numberOfNights = user.getNumberOfNights();
            numberOfRooms = user.getNumberOfRooms();
        } else {
            // Unsupported object type
            return true;
        }

        if (!isNightInHotel) {
            // If not staying in a hotel, no hotel information is required
            return true;
        }

        // If staying in a hotel, validate required fields
        context.disableDefaultConstraintViolation();

        boolean isValid = true;

        if (hotelName == null || hotelName.trim().isEmpty()) {
            context.buildConstraintViolationWithTemplate("Hotel name is required when staying overnight")
                    .addPropertyNode("hotelName")
                    .addConstraintViolation();
            isValid = false;
        }

        if (numberOfNights == null || numberOfNights < 1) {
            context.buildConstraintViolationWithTemplate("Number of nights must be at least 1 when staying overnight")
                    .addPropertyNode("numberOfNights")
                    .addConstraintViolation();
            isValid = false;
        }

        if (numberOfRooms == null || numberOfRooms < 1) {
            context.buildConstraintViolationWithTemplate("Number of rooms must be at least 1 when staying overnight")
                    .addPropertyNode("numberOfRooms")
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }
}