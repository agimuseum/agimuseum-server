package com.agimuseum.magi.model;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

/**
 * Entity listener to enforce hotel validation rules before saving to the database
 */
public class UserEntityListener {

    @PrePersist
    @PreUpdate
    public void validateUser(User user) {
        // Set null for hotel-related fields when not staying in hotel
        if (!user.isNightInHotel()) {
            user.setHotelName(null);
            user.setNumberOfNights(null);
            user.setNumberOfRooms(null);
        }

        // Ensure numberOfPeople has a default value
        if (user.getNumberOfPeople() == null) {
            user.setNumberOfPeople(1); // Default to 1 person
        }
    }
}