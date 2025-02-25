package com.agimuseum.magi.dto;

import com.agimuseum.magi.model.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "First name is required")
    private String firstname;

    @NotBlank(message = "Last name is required")
    private String lastname;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String username; // using as email

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Password must be 8 characters long and contain at least one number, one uppercase, one lowercase letter and one special character")
    private String password;

    @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "Invalid ZIP code format")
    private String zipCode;

    private boolean isVisiting;

    private boolean isNightInHotel;

    private String hotelName;

    @Min(value = 0, message = "Number of nights cannot be negative")
    private Integer numberOfNights;

    @Min(value = 0, message = "Number of rooms must be positive")
    private Integer numberOfRooms;

    @Min(value = 1, message = "Number of people must be at least 1")
    private Integer numberOfPeople;

    private Role role = Role.USER; // Default role
}