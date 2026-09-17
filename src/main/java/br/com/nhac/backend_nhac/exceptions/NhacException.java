package br.com.nhac.backend_nhac.exceptions;

import org.springframework.http.HttpStatus;
import java.util.Collections;
import java.util.Map;

public abstract class NhacException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Map<String, Object> details;

    public NhacException(String message, ErrorCode errorCode, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details != null ? details : Collections.emptyMap();
    }

    public NhacException(String message, ErrorCode errorCode) {
        this(message, errorCode, Collections.emptyMap());
    }

    public NhacException(String message, Throwable cause, ErrorCode errorCode, Map<String, Object> details) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = details != null ? details : Collections.emptyMap();
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public abstract HttpStatus getHttpStatus();
}
