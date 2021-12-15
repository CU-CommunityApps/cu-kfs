package edu.cornell.kfs.sys.exception;

/**
 * Helper exception class for handling errors related to Favorite Account setup.
 * The error message will represent the key that should be used for adding the
 * error to the message map.
 */
public class FavoriteAccountException extends RuntimeException {
    private static final long serialVersionUID = -4394212638693566094L;

    public FavoriteAccountException(String message) {
        super(message);
    }

    public FavoriteAccountException(String message, Throwable cause) {
        super(message, cause);
    }

}
