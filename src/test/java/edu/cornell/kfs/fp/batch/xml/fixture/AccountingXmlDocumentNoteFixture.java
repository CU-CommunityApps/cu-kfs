package edu.cornell.kfs.fp.batch.xml.fixture;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentNote;

public enum AccountingXmlDocumentNoteFixture {
    TEST_NOTE1(null);

    public final String description;

    private AccountingXmlDocumentNoteFixture(String description) {
        this.description = description;
    }

    public AccountingXmlDocumentNote toDocumentNotePojo() {
        AccountingXmlDocumentNote note = new AccountingXmlDocumentNote();
        note.setDescription(description);
        return note;
    }

}
