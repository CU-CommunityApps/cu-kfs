package edu.cornell.kfs.fp.batch.service.impl;

import java.util.function.Supplier;

import org.kuali.kfs.fp.document.YearEndBudgetAdjustmentDocument;
import org.kuali.kfs.fp.service.FiscalYearFunctionControlService;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.sys.businessobject.AccountingLine;

import edu.cornell.kfs.fp.batch.util.BudgetAdjustmentDocumentGeneratorUtils;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;

public class CuYearEndBudgetAdjustmentDocumentGenerator extends AccountingDocumentGeneratorBase<YearEndBudgetAdjustmentDocument>  {

    protected FiscalYearFunctionControlService fiscalYearFunctionControlService;

    public CuYearEndBudgetAdjustmentDocumentGenerator() {
        super();
    }

    public CuYearEndBudgetAdjustmentDocumentGenerator(
            Supplier<Note> emptyNoteGenerator, Supplier<AdHocRoutePerson> emptyAdHocRoutePersonGenerator) {
        super(emptyNoteGenerator, emptyAdHocRoutePersonGenerator);
    }

    @Override
    public Class<? extends YearEndBudgetAdjustmentDocument> getDocumentClass() {
        return YearEndBudgetAdjustmentDocument.class;
    }

    @Override
    protected <A extends AccountingLine> A buildAccountingLine(Class<A> accountingLineClass, String documentNumber, AccountingXmlDocumentAccountingLine xmlLine) {
        A accountingLine = super.buildAccountingLine(accountingLineClass, documentNumber, xmlLine);
        return BudgetAdjustmentDocumentGeneratorUtils.buildAccountingLine(accountingLine, xmlLine);
    }

    @Override
    protected void populateCustomAccountingDocumentData(
            YearEndBudgetAdjustmentDocument document, AccountingXmlDocumentEntry documentEntry) {
        super.populateCustomAccountingDocumentData(document, documentEntry);
        BudgetAdjustmentDocumentGeneratorUtils.validateAndSetFiscalYear(document, documentEntry, fiscalYearFunctionControlService, 
                "Year End Budget Adjustments are not allowed for fiscal year: ");
    }

    public void setFiscalYearFunctionControlService(FiscalYearFunctionControlService fiscalYearFunctionControlService) {
        this.fiscalYearFunctionControlService = fiscalYearFunctionControlService;
    }

}
