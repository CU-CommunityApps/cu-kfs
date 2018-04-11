package edu.cornell.kfs.fp.batch.service.impl;

import java.util.function.Supplier;

import org.kuali.kfs.fp.document.TransferOfFundsDocument;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;

public class TransferOfFundsDocumentGenerator extends AccountingDocumentGeneratorBase<TransferOfFundsDocument> {
    
    public TransferOfFundsDocumentGenerator() {
        super();
    }

    public TransferOfFundsDocumentGenerator(
            Supplier<Note> emptyNoteGenerator, Supplier<AdHocRoutePerson> emptyAdHocRoutePersonGenerator) {
        super(emptyNoteGenerator, emptyAdHocRoutePersonGenerator);
    }

    @Override
    public Class<? extends TransferOfFundsDocument> getDocumentClass() {
        return TransferOfFundsDocument.class;
    }

}
