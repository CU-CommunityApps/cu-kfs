package edu.cornell.kfs.fp.exception;

import edu.cornell.kfs.fp.businessobject.CreateAccountingDocumentFileEntry;

public class DuplicateCreateAccountingDocumentFileException extends Exception {

    private static final long serialVersionUID = 8930065901247977684L;

    private final CreateAccountingDocumentFileEntry existingEntry;

    public DuplicateCreateAccountingDocumentFileException(CreateAccountingDocumentFileEntry existingEntry) {
        super();
        this.existingEntry = existingEntry;
    }

    public DuplicateCreateAccountingDocumentFileException(CreateAccountingDocumentFileEntry existingEntry,
            String message) {
        super(message);
        this.existingEntry = existingEntry;
    }

    public CreateAccountingDocumentFileEntry getExistingEntry() {
        return existingEntry;
    }

}
