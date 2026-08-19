package com.example.reading.service;

import com.example.reading.dto.ApiDtos.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder jwtEncoder;

    public TokenResponse issue(Long userId) {
        Instant now = Instant.now();
        long expiresIn = 3600;

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("reading-api")
                .subject(userId.toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiresIn))
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();

        return new TokenResponse(token, "Bearer", expiresIn);
    }
}