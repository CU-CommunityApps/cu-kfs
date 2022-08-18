package edu.cornell.kfs.sys.exception;

/**
 * CU Generic ISO-FIPS Country modification
 *
 * Helper exception class for handling when there is
 * more than one FIPS-to-ISO Country mapping found for a FIPS Country code.
 */
public class ManyFIPStoISOMappingException extends RuntimeException {

    private static final long serialVersionUID = -8127747939165197210L;

    public ManyFIPStoISOMappingException(String message) {
        super(message);
    }

    public ManyFIPStoISOMappingException(String message, Throwable cause) {
        super(message, cause);
    }

}
