package com.agimuseum.magi.service;

import com.agimuseum.magi.dto.TokenResponse;
import com.agimuseum.magi.model.BlacklistedToken;
import com.agimuseum.magi.model.RefreshToken;
import com.agimuseum.magi.model.User;
import com.agimuseum.magi.repository.BlacklistedTokenRepository;
import com.agimuseum.magi.repository.RefreshTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class TokenService {
    private static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000; // 7 days

    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public TokenService(BlacklistedTokenRepository blacklistedTokenRepository,
                        RefreshTokenRepository refreshTokenRepository,
                        JwtService jwtService) {
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    public TokenResponse createTokenPair(User user) {
        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = createRefreshToken(user);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .type("Bearer")
                .expiresIn(JwtService.ACCESS_TOKEN_DURATION)
                .build();
    }

    private RefreshToken createRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user); // Remove existing refresh tokens

        return refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY))
                .build());
    }

    public void invalidateToken(String token) {
        blacklistedTokenRepository.save(BlacklistedToken.builder()
                .token(token)
                .expiryDate(jwtService.extractExpiration(token))
                .build());
    }

    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokenRepository.existsByToken(token);
    }

    @Scheduled(cron = "0 0 0 * * *") // Daily at midnight
    public void cleanupExpiredTokens() {
        try {
            Date now = new Date();
            refreshTokenRepository.deleteByExpiryDateBefore(now);
            blacklistedTokenRepository.deleteByExpiryDateBefore(now);
            log.info("Successfully cleaned up expired tokens");
        } catch (Exception e) {
            log.error("Error during token cleanup: ", e);
        }
    }
}