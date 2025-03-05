package com.agimuseum.magi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDeletionRequest {
    private String confirmPassword;
    private String reason;

    // This class can be expanded with additional fields as needed
    // like confirmation tokens, feedback, etc.
}