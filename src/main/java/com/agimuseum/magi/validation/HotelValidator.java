package com.agimuseum.magi.validation;

import com.agimuseum.magi.dto.RegisterRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class HotelValidator implements ConstraintValidator<ConditionalHotelValidation, RegisterRequest> {

    @Override
    public void initialize(ConditionalHotelValidation constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(RegisterRequest request, ConstraintValidatorContext context) {
        if (!request.isNightInHotel()) {
            // If not staying in a hotel, no hotel information is required
            return true;
        }

        // If staying in a hotel, validate required fields
        context.disableDefaultConstraintViolation();

        boolean isValid = true;

        if (request.getHotelName() == null || request.getHotelName().trim().isEmpty()) {
            context.buildConstraintViolationWithTemplate("Hotel name is required when staying overnight")
                    .addPropertyNode("hotelName")
                    .addConstraintViolation();
            isValid = false;
        }

        if (request.getNumberOfNights() == null || request.getNumberOfNights() < 1) {
            context.buildConstraintViolationWithTemplate("Number of nights must be at least 1 when staying overnight")
                    .addPropertyNode("numberOfNights")
                    .addConstraintViolation();
            isValid = false;
        }

        if (request.getNumberOfRooms() == null || request.getNumberOfRooms() < 1) {
            context.buildConstraintViolationWithTemplate("Number of rooms must be at least 1 when staying overnight")
                    .addPropertyNode("numberOfRooms")
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }
}