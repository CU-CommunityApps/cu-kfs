package edu.cornell.kfs.sys.exception;

public class NoFIPSCountriesForNameException extends RuntimeException {

    private static final long serialVersionUID = 6385118814092116205L;

    public NoFIPSCountriesForNameException(String message) {
        super(message);
    }

    public NoFIPSCountriesForNameException(String message, Throwable cause) {
        super(message, cause);
    }

}
