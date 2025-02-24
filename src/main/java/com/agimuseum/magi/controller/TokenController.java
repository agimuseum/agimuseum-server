package com.agimuseum.magi.controller;

import com.agimuseum.magi.dto.TokenResponse;
import com.agimuseum.magi.model.*;
import com.agimuseum.magi.repository.RefreshTokenRepository;
import com.agimuseum.magi.repository.UserRepository;
import com.agimuseum.magi.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/api/auth")
public class TokenController {
    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody TokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (refreshToken.getExpiryDate().before(new Date())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired");
        }

        User user = refreshToken.getUser();
        TokenResponse newTokens = tokenService.createTokenPair(user);

        return ResponseEntity.ok(newTokens);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        tokenService.invalidateToken(request.getAccessToken());
        return ResponseEntity.ok().build();
    }
}
