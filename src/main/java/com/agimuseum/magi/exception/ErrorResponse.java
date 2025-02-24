package com.agimuseum.magi.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {
    private String message;
    private String details;
    private String timestamp;
}