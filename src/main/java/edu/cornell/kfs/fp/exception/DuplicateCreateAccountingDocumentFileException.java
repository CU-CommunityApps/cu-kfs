package edu.cornell.kfs.fp.exception;

public class DuplicateCreateAccountingDocumentFileException extends Exception {

    private static final long serialVersionUID = 8930065901247977684L;

    public DuplicateCreateAccountingDocumentFileException() {
        super();
    }

    public DuplicateCreateAccountingDocumentFileException(String message) {
        super(message);
    }

}
