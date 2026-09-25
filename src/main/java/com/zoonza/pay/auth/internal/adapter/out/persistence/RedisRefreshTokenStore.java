package com.zoonza.pay.auth.internal.adapter.out.persistence;

import com.zoonza.pay.auth.internal.application.dto.RefreshToken;
import com.zoonza.pay.auth.internal.application.port.out.RefreshTokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RedisRefreshTokenStore implements RefreshTokenStore {
    private static final String KEY_PREFIX = "refresh_token:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(RefreshToken refreshToken) {
        Duration timeToLive = Duration.between(Instant.now(), refreshToken.expiresAt());

        redisTemplate.opsForValue().set(
                keyOf(refreshToken.value()),
                refreshToken.customerId().toString(),
                timeToLive
        );
    }

    @Override
    public Optional<Long> findCustomerIdAndDelete(String refreshTokenValue) {
        return Optional.ofNullable(redisTemplate.opsForValue().getAndDelete(keyOf(refreshTokenValue)))
                .map(Long::valueOf);
    }

    @Override
    public void delete(String refreshTokenValue) {
        redisTemplate.delete(keyOf(refreshTokenValue));
    }

    private String keyOf(String tokenValue) {
        return KEY_PREFIX + sha256(tokenValue);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
