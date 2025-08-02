package com.eternalcode.discordapp.review;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import okhttp3.Interceptor;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

class GitHubRateLimitInterceptor implements Interceptor {
    private static final Duration MIN_REQUEST_INTERVAL = Duration.ofMillis(100);
    private volatile Instant lastRequest = Instant.MIN;

    @Override
    public @NotNull Response intercept(@NotNull Chain chain) throws IOException {
        synchronized (this) {
            Instant now = Instant.now();
            Instant nextAllowed = lastRequest.plus(MIN_REQUEST_INTERVAL);

            if (now.isBefore(nextAllowed)) {
                try {
                    long sleepMs = Duration.between(now, nextAllowed).toMillis();
                    if (sleepMs > 0) {
                        Thread.sleep(sleepMs);
                    }
                }
                catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted while rate limiting", exception);
                }
            }

            lastRequest = Instant.now();
        }

        return chain.proceed(chain.request());
    }
}
