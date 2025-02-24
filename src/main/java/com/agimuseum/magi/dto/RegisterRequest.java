package com.agimuseum.magi.dto;

import com.agimuseum.magi.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    private Role role = Role.USER; // Default role
}
