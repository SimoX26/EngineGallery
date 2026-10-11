package it.SimoSW.util.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginRateLimiterTest {

    @Test
    void blocksAnAccountAfterFiveFailedAttempts() {
        LoginRateLimiter limiter = new LoginRateLimiter(fixedClock());

        for (int attempt = 0; attempt < 5; attempt++) {
            assertTrue(limiter.isAllowed("operatore"));
            limiter.recordFailure("operatore");
        }

        assertFalse(limiter.isAllowed("operatore"));
    }

    @Test
    void successfulLoginResetsFailedAttemptCounter() {
        LoginRateLimiter limiter = new LoginRateLimiter(fixedClock());

        for (int attempt = 0; attempt < 4; attempt++) {
            limiter.recordFailure("operatore");
        }
        limiter.reset("operatore");

        assertTrue(limiter.isAllowed("operatore"));
    }

    private static Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-10-12T00:00:00Z"), ZoneOffset.UTC);
    }
}
