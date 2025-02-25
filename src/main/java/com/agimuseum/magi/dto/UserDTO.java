package com.agimuseum.magi.dto;

import com.agimuseum.magi.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Integer id;
    private String firstname;
    private String lastname;
    private String username;
    private String zipCode;
    private boolean isVisiting;
    private boolean isNightInHotel;
    private String hotelName;
    private Integer numberOfNights;
    private Integer numberOfRooms;
    private Integer numberOfPeople;
    private Role role;
    private Date createdAt;
    private boolean active;
    private Date lastLogin;
}