
package edu.cornell.kfs.sys.exception;

/**
 * CU Generic ISO-FIPS Country modification
 *
 * Helper exception class for handling when there is
 * no FIPS-to-ISO Country mapping found for a FIPS Country code.
 */
public class NoFIPStoISOMappingException extends RuntimeException {

    private static final long serialVersionUID = 6086214247572352502L;

    public NoFIPStoISOMappingException(String message) {
        super(message);
    }

    public NoFIPStoISOMappingException(String message, Throwable cause) {
        super(message, cause);
    }

}