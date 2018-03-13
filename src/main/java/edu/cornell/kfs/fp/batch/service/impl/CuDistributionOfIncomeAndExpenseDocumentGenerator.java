package edu.cornell.kfs.fp.batch.service.impl;

import java.util.function.Supplier;

import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;

import edu.cornell.kfs.fp.document.CuDistributionOfIncomeAndExpenseDocument;

public class CuDistributionOfIncomeAndExpenseDocumentGenerator
        extends AccountingDocumentGeneratorBase<CuDistributionOfIncomeAndExpenseDocument> {

    public CuDistributionOfIncomeAndExpenseDocumentGenerator() {
        super();
    }

    public CuDistributionOfIncomeAndExpenseDocumentGenerator(
            Supplier<Note> emptyNoteGenerator, Supplier<AdHocRoutePerson> emptyAdHocRoutePersonGenerator) {
        super(emptyNoteGenerator, emptyAdHocRoutePersonGenerator);
    }

    @Override
    public Class<? extends CuDistributionOfIncomeAndExpenseDocument> getDocumentClass() {
        return CuDistributionOfIncomeAndExpenseDocument.class;
    }

}
