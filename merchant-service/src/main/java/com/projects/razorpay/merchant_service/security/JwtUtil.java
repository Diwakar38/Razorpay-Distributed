package com.projects.razorpay.merchant_service.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.util.UUID;

@Component
@Getter
@Setter
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.secret-key}")
    public String secretKey;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String email, UUID merchantId, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(email)
                .issuedAt(Date.from(now))
                .expiration(java.util.Date.from(now.plusSeconds(60*100)))
                .claim("merchant_id", merchantId)
                .claim("role", role)
                .signWith(getSecretKey())
                .compact();
    }

}
