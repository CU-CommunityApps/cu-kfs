package edu.cornell.kfs.sys.exception;

public class ManyFIPSCountriesForNameException extends RuntimeException {

    private static final long serialVersionUID = 1416660134593684583L;

    public ManyFIPSCountriesForNameException(String message) {
        super(message);
    }

    public ManyFIPSCountriesForNameException(String message, Throwable cause) {
        super(message, cause);
    }

}
