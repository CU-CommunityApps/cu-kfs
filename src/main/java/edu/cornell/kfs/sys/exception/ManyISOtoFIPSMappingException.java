package edu.cornell.kfs.sys.exception;

/**
 * CU Generic ISO-FIPS Country modification
 *
 * Helper exception class for handling when there is
 * more than one ISO-to-FIPS Country mapping found for an ISO Country code.
 */
public class ManyISOtoFIPSMappingException extends RuntimeException {

    private static final long serialVersionUID = -734993911777424160L;

    public ManyISOtoFIPSMappingException(String message) {
        super(message);
    }

    public ManyISOtoFIPSMappingException(String message, Throwable cause) {
        super(message, cause);
    }

}
