package edu.cornell.kfs.fp.batch.service.impl;

import java.util.function.Supplier;

import org.apache.commons.collections.CollectionUtils;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.exception.ValidationException;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;
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

    @Override
    protected void performCustomValidation(
            CuDistributionOfIncomeAndExpenseDocument document, AccountingXmlDocumentEntry documentEntry) {
        super.performCustomValidation(document, documentEntry);
        if (CollectionUtils.isNotEmpty(documentEntry.getItems())) {
            throw new ValidationException("Item lines are not permitted on DI documents");
        }
    }

}
