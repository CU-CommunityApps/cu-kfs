package edu.cornell.kfs.fp.batch.service.impl;

import java.util.function.Supplier;

import org.kuali.kfs.fp.service.FiscalYearFunctionControlService;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.sys.businessobject.AccountingLine;

import edu.cornell.kfs.fp.batch.util.BudgetAdjustmentDocumentGeneratorUtils;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;
import edu.cornell.kfs.fp.document.CuBudgetAdjustmentDocument;

public class CuBudgetAdjustmentDocumentGenerator extends AccountingDocumentGeneratorBase<CuBudgetAdjustmentDocument> {

    protected FiscalYearFunctionControlService fiscalYearFunctionControlService;

    public CuBudgetAdjustmentDocumentGenerator() {
        super();
    }

    public CuBudgetAdjustmentDocumentGenerator(
            Supplier<Note> emptyNoteGenerator, Supplier<AdHocRoutePerson> emptyAdHocRoutePersonGenerator) {
        super(emptyNoteGenerator, emptyAdHocRoutePersonGenerator);
    }

    @Override
    public Class<? extends CuBudgetAdjustmentDocument> getDocumentClass() {
        return CuBudgetAdjustmentDocument.class;
    }

    @Override
    protected <A extends AccountingLine> A buildAccountingLine(
            Class<A> accountingLineClass, String documentNumber, AccountingXmlDocumentAccountingLine xmlLine) {
        A accountingLine = super.buildAccountingLine(accountingLineClass, documentNumber, xmlLine);
        return BudgetAdjustmentDocumentGeneratorUtils.buildAccountingLine(accountingLine, xmlLine);
    }

    @Override
    protected void populateCustomAccountingDocumentData(
            CuBudgetAdjustmentDocument document, AccountingXmlDocumentEntry documentEntry) {
        super.populateCustomAccountingDocumentData(document, documentEntry);
        BudgetAdjustmentDocumentGeneratorUtils.validateAndSetFiscalYear(document, documentEntry, fiscalYearFunctionControlService, 
                "Budget Adjustments are not allowed for fiscal year: ");
    }

    public void setFiscalYearFunctionControlService(FiscalYearFunctionControlService fiscalYearFunctionControlService) {
        this.fiscalYearFunctionControlService = fiscalYearFunctionControlService;
    }

}
