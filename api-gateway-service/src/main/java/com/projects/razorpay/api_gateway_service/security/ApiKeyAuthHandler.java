package com.projects.razorpay.api_gateway_service.security;

import com.projects.razorpay.api_gateway_service.client.ApiKeyLookupClient;
import com.projects.razorpay.common_lib.cache.ApiKeyCache;
import com.projects.razorpay.common_lib.cache.ApiKeyCacheEntry;
import com.projects.razorpay.common_lib.exceptions.RateLimitException;
import com.projects.razorpay.common_lib.ratelimit.RateLimitResult;
import com.projects.razorpay.common_lib.ratelimit.RateLimiter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyAuthHandler {

    private static final String BASIC_PREFIX = "Basic ";
    private static final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();

    private final RateLimiter rateLimiter;
    private final ApiKeyCache apiKeyCache;
    private final ApiKeyLookupClient apiKeyLookupClient;

    @Value("${app.rate-limit.use-case.api-key.request-per-minute:60}")
    private int requestsPerMinute;

    public Map<String, String> authenticate(String authHeader, HttpServletResponse response) {
        String[] credentials = decodeBasic(authHeader);

        if(credentials == null) {
            throw new GatewayAuthenticationException("Malformed API key header");
        }

        String keyId = credentials[0];
        String rawSecret = credentials[1];

        ApiKeyCacheEntry entry = apiKeyCache.get(keyId).orElseGet(() -> loadAndCache(keyId));

        if(entry == null || !entry.enabled() || !secretMatches(rawSecret, entry)) {
            throw new GatewayAuthenticationException("Invalid or missing API key");
        }

        RateLimitResult rateLimitResult = rateLimiter.check("apikey:" + keyId, requestsPerMinute, 60);
        if(!rateLimitResult.isAllowed()) {
            throw new RateLimitException("Too many requests", rateLimitResult.retryAfterSeconds());
        }
        response.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(rateLimitResult.remaining()));

        return Map.of(
                "X-Merchant-Id", entry.merchantId().toString(),
                "X-Environment", entry.environment().name(),
                "X-Key-Id", entry.keyId()
        );
    }

    private String[] decodeBasic(String authHeader) {
        try {
            String encoded = authHeader.substring(BASIC_PREFIX.length());
            String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
            int colon = decoded.indexOf(':');
            if(colon < 1) return null;
            return new String[]{decoded.substring(0,colon), decoded.substring(colon)};
        } catch (RuntimeException e) {
            return null;
        }
    }

    private boolean secretMatches(String rawSecret, ApiKeyCacheEntry entry) {
        if(BCRYPT.matches(rawSecret, entry.keySecretHash())) return true;

        return entry.isInGracePeriod()
                && entry.previousKeySecretHash() != null
                && BCRYPT.matches(rawSecret, entry.previousKeySecretHash());
    }

    private ApiKeyCacheEntry loadAndCache(String keyId) {
        try {
            ApiKeyCacheEntry entry = apiKeyLookupClient.findByKeyId(keyId);
            apiKeyCache.put(keyId,entry);
            return entry;
        } catch (Exception e) {
            log.warn("API key lookup failed keyId={}", keyId, e);
            return null;
        }
    }

}
