package it.SimoSW.util.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limits failed login attempts in the current application instance.
 */
public final class LoginRateLimiter {

    private static final int MAX_FAILURES_PER_ACCOUNT = 5;
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(15);

    private final Map<String, AttemptState> accountAttempts = new ConcurrentHashMap<>();
    private final Clock clock;

    public LoginRateLimiter() {
        this(Clock.systemUTC());
    }

    LoginRateLimiter(Clock clock) {
        this.clock = clock;
    }

    public boolean isAllowed(String username) {
        Instant now = clock.instant();
        return !isBlocked(accountAttempts, accountKey(username), MAX_FAILURES_PER_ACCOUNT, now);
    }

    public void recordFailure(String username) {
        Instant now = clock.instant();
        recordFailure(accountAttempts, accountKey(username), MAX_FAILURES_PER_ACCOUNT, now);
    }

    public void reset(String username) {
        accountAttempts.remove(accountKey(username));
    }

    private static boolean isBlocked(Map<String, AttemptState> attempts, String key, int maxFailures, Instant now) {
        AttemptState state = attempts.get(key);
        if (state == null) {
            return false;
        }
        synchronized (state) {
            if (now.isBefore(state.blockedUntil)) {
                return true;
            }
            if (!state.blockedUntil.equals(Instant.EPOCH)
                    || !now.isBefore(state.windowStartedAt.plus(BLOCK_DURATION))) {
                attempts.remove(key, state);
            }
            return false;
        }
    }

    private static void recordFailure(Map<String, AttemptState> attempts, String key, int maxFailures, Instant now) {
        attempts.compute(key, (ignored, state) -> {
            AttemptState updated = state == null ? new AttemptState(now) : state;
            synchronized (updated) {
                if (!now.isBefore(updated.blockedUntil)
                        && !now.isBefore(updated.windowStartedAt.plus(BLOCK_DURATION))) {
                    updated.failures = 0;
                    updated.windowStartedAt = now;
                    updated.blockedUntil = Instant.EPOCH;
                }
                updated.failures += 1;
                if (updated.failures >= maxFailures) {
                    updated.blockedUntil = now.plus(BLOCK_DURATION);
                }
            }
            return updated;
        });
    }

    private static String accountKey(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    private static final class AttemptState {
        private int failures;
        private Instant windowStartedAt;
        private Instant blockedUntil = Instant.EPOCH;

        private AttemptState(Instant windowStartedAt) {
            this.windowStartedAt = windowStartedAt;
        }
    }
}
