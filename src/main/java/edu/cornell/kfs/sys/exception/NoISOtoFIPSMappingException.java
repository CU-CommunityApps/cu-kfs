package edu.cornell.kfs.sys.exception;

/**
 * Helper exception class for handling when there is
 * no ISO-to-FIPS Country mapping found for ISO Country code.
 */
public class NoISOtoFIPSMappingException extends RuntimeException {

    private static final long serialVersionUID = 8868093486793799395L;

    public NoISOtoFIPSMappingException(String message) {
        super(message);
    }

    public NoISOtoFIPSMappingException(String message, Throwable cause) {
        super(message, cause);
    }

}
