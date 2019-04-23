package edu.cornell.kfs.fp.batch.service.impl;

import java.util.function.Supplier;

import org.kuali.kfs.fp.document.YearEndTransferOfFundsDocument;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;

public class YearEndTransferOfFundsDocumentGenerator extends AccountingDocumentGeneratorBase<YearEndTransferOfFundsDocument> {

    public YearEndTransferOfFundsDocumentGenerator() {
        super();
    }

    public YearEndTransferOfFundsDocumentGenerator(Supplier<Note> emptyNoteGenerator, Supplier<AdHocRoutePerson> emptyAdHocRoutePersonGenerator) {
        super(emptyNoteGenerator, emptyAdHocRoutePersonGenerator);
    }

    @Override
    public Class<? extends YearEndTransferOfFundsDocument> getDocumentClass() {
        return YearEndTransferOfFundsDocument.class;
    }

}
