package br.com.nhac.backend_nhac.infra.email;

public class BrevoApiException extends RuntimeException {

    private final boolean retryable;
    private final int statusCode;

    public BrevoApiException(boolean retryable, int statusCode, String message) {
        super(message);
        this.retryable = retryable;
        this.statusCode = statusCode;
    }

    public BrevoApiException(boolean retryable, int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.retryable = retryable;
        this.statusCode = statusCode;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
