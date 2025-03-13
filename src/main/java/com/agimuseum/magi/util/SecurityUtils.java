package com.agimuseum.magi.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Enumeration;

@Component
@Slf4j
public class SecurityUtils {

    /**
     * Logs the details of a request for debugging purposes
     * @param request The HTTP request to log
     */
    public static void logRequestDetails(HttpServletRequest request) {
        try {
            log.debug("Request Details:");
            log.debug("URI: {}", request.getRequestURI());
            log.debug("Method: {}", request.getMethod());
            log.debug("Query String: {}", request.getQueryString());

            log.debug("Headers:");
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                // Don't log sensitive headers like Authorization
                if (!"authorization".equalsIgnoreCase(headerName)) {
                    log.debug("{}: {}", headerName, request.getHeader(headerName));
                } else {
                    log.debug("{}: [REDACTED]", headerName);
                }
            }

            // Log current authentication
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                log.debug("Authentication: {}", auth.getName());
                log.debug("Authenticated: {}", auth.isAuthenticated());
                log.debug("Authorities: {}", auth.getAuthorities());
            } else {
                log.debug("No authentication found in context");
            }
        } catch (Exception e) {
            log.error("Error logging request details", e);
        }
    }

    /**
     * Get current authentication information as a formatted string
     */
    public static String getCurrentAuthInfo() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                return "No authentication in context";
            }

            return String.format(
                    "Authentication: %s, Authenticated: %s, Authorities: %s",
                    auth.getName(),
                    auth.isAuthenticated(),
                    auth.getAuthorities()
            );
        } catch (Exception e) {
            log.error("Error getting auth info", e);
            return "Error retrieving authentication info";
        }
    }
}