package edu.cornell.kfs.sys.web;

import java.io.Serializable;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Custom ResponseStatusException subclass that has an extra property for including additional info.
 */
public class CuResponseStatusException extends ResponseStatusException {

    private static final long serialVersionUID = 3492030910434509354L;

    private final Serializable statusInfo;

    public CuResponseStatusException(final int rawStatusCode, final String reason, final Serializable statusInfo,
            final Throwable cause) {
        super(rawStatusCode, reason, cause);
        verifyNonNullStatusInfo(statusInfo);
        this.statusInfo = statusInfo;
    }

    public CuResponseStatusException(final HttpStatus status, final Serializable statusInfo) {
        super(status);
        verifyNonNullStatusInfo(statusInfo);
        this.statusInfo = statusInfo;
    }

    public CuResponseStatusException(final HttpStatus status, final String reason, final Serializable statusInfo) {
        super(status, reason);
        verifyNonNullStatusInfo(statusInfo);
        this.statusInfo = statusInfo;
    }

    public CuResponseStatusException(final HttpStatus status, final String reason, final Serializable statusInfo,
            final Throwable cause) {
        super(status, reason, cause);
        verifyNonNullStatusInfo(statusInfo);
        this.statusInfo = statusInfo;
    }

    private static void verifyNonNullStatusInfo(final Serializable statusInfo) {
        Objects.requireNonNull(statusInfo, "statusInfo cannot be null");
    }

    public Serializable getStatusInfo() {
        return statusInfo;
    }

}
