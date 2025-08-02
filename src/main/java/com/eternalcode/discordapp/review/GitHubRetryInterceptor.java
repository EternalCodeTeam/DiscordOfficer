package com.eternalcode.discordapp.review;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

class GitHubRetryInterceptor implements Interceptor {
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    @Override
    public @NotNull Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = null;
        IOException lastException = null;

        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                if (response != null) {
                    response.close();
                }

                response = chain.proceed(request);

                if (response.isSuccessful() || response.code() == 404 || response.code() == 403) {
                    return response;
                }

                if (response.code() >= 500 || response.code() == 429) {
                    if (attempt < MAX_RETRIES - 1) {
                        try {
                            Thread.sleep(RETRY_DELAY_MS * (attempt + 1));
                        }
                        catch (InterruptedException exception) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                        continue;
                    }
                }

                return response;
            }
            catch (IOException exception) {
                lastException = exception;
                if (attempt < MAX_RETRIES - 1) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * (attempt + 1));
                    }
                    catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        if (response != null) {
            return response;
        }

        throw lastException != null ? lastException : new IOException("Max retries exceeded");
    }
}
