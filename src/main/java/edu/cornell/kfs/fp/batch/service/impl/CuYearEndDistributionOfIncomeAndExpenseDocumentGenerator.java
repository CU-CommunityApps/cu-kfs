package edu.cornell.kfs.fp.batch.service.impl;

import org.kuali.kfs.fp.document.YearEndDistributionOfIncomeAndExpenseDocument;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;

import java.util.function.Supplier;

public class CuYearEndDistributionOfIncomeAndExpenseDocumentGenerator
        extends AccountingDocumentGeneratorBase<YearEndDistributionOfIncomeAndExpenseDocument> {

    public CuYearEndDistributionOfIncomeAndExpenseDocumentGenerator() {
        super();
    }

    public CuYearEndDistributionOfIncomeAndExpenseDocumentGenerator(
            Supplier<Note> emptyNoteGenerator, Supplier<AdHocRoutePerson> emptyAdHocRoutePersonGenerator) {
        super(emptyNoteGenerator, emptyAdHocRoutePersonGenerator);
    }

    @Override
    public Class<? extends YearEndDistributionOfIncomeAndExpenseDocument> getDocumentClass() {
        return YearEndDistributionOfIncomeAndExpenseDocument.class;
    }

}
