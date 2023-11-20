package edu.cornell.kfs.concur.exception;

public class ConcurWebserviceException extends RuntimeException {
    private static final long serialVersionUID = 8777327265631798933L;
    
    public ConcurWebserviceException(String message) {
        super(message);
    }
    
    public ConcurWebserviceException(String message, Throwable cause) {
        super(message, cause);
    }
}
