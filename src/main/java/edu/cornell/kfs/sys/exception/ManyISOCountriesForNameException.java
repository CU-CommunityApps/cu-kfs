package edu.cornell.kfs.sys.exception;

public class ManyISOCountriesForNameException extends RuntimeException {

    private static final long serialVersionUID = 9205429635922350334L;

    public ManyISOCountriesForNameException(String message) {
        super(message);
    }

    public ManyISOCountriesForNameException(String message, Throwable cause) {
        super(message, cause);
    }

}
