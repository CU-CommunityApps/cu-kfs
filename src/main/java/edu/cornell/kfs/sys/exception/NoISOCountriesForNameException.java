package edu.cornell.kfs.sys.exception;

public class NoISOCountriesForNameException extends RuntimeException {

    private static final long serialVersionUID = 3016822838833689683L;

    public NoISOCountriesForNameException(String message) {
        super(message);
    }

    public NoISOCountriesForNameException(String message, Throwable cause) {
        super(message, cause);
    }

}
