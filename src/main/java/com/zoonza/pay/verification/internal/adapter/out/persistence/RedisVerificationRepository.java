package com.zoonza.pay.verification.internal.adapter.out.persistence;

import com.zoonza.pay.verification.internal.domain.Verification;
import com.zoonza.pay.verification.internal.domain.VerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;

@Repository
@RequiredArgsConstructor
public class RedisVerificationRepository implements VerificationRepository {
    private static final String KEY_PREFIX = "verification:";
    private static final Duration EXPIRATION_GRACE_PERIOD = Duration.ofMinutes(10);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void save(Verification verification) {
        String value = objectMapper
                .writeValueAsString(verification);

        Duration timeToLive = Duration
                .between(Instant.now(), verification.expiresAt())
                .plus(EXPIRATION_GRACE_PERIOD);

        redisTemplate.opsForValue().set(KEY_PREFIX + verification.id(), value, timeToLive);
    }
}
