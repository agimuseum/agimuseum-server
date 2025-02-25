package com.agimuseum.magi.service;

import com.agimuseum.magi.dto.LoginRequest;
import com.agimuseum.magi.dto.RegisterRequest;
import com.agimuseum.magi.dto.TokenResponse;
import com.agimuseum.magi.exception.UserAlreadyExistsException;
import com.agimuseum.magi.model.User;
import com.agimuseum.magi.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AuthenticationService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthenticationService(UserRepository repository,
                                 PasswordEncoder passwordEncoder,
                                 TokenService tokenService,
                                 AuthenticationManager authenticationManager, JwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public TokenResponse register(RegisterRequest request) {
        if(repository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        User user = new User();
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setCreatedAt(new Date());
        user.setActive(true);
        user.setLastLogin(new Date());
        user.setZipCode(request.getZipCode());
        user.setVisiting(request.isVisiting());
        user.setNightInHotel(request.isNightInHotel());

        // Set hotel-related fields only if staying in hotel
        if (request.isNightInHotel()) {
            user.setHotelName(request.getHotelName());
            user.setNumberOfNights(request.getNumberOfNights());
            user.setNumberOfRooms(request.getNumberOfRooms());
        } else {
            // Set to null when not staying in a hotel
            user.setHotelName(null);
            user.setNumberOfNights(null);
            user.setNumberOfRooms(null);
        }

        // Ensure numberOfPeople is set (default to 1 if not provided)
        if (request.getNumberOfPeople() != null) {
            user.setNumberOfPeople(request.getNumberOfPeople());
        } else {
            user.setNumberOfPeople(1); // Default to 1 person
        }

        user = repository.save(user);

        // Generate both access and refresh tokens
        return tokenService.createTokenPair(user);
    }

    public TokenResponse authenticate(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = repository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Update last login time
        user.setLastLogin(new Date());
        repository.save(user);

        // Generate both access and refresh tokens
        return tokenService.createTokenPair(user);
    }
}