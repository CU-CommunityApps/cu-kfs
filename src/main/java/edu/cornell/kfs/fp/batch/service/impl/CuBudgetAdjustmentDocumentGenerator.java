package edu.cornell.kfs.fp.batch.service.impl;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.kuali.kfs.fp.businessobject.BudgetAdjustmentAccountingLine;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;
import edu.cornell.kfs.fp.document.CuBudgetAdjustmentDocument;

public class CuBudgetAdjustmentDocumentGenerator extends AccountingDocumentGeneratorBase<CuBudgetAdjustmentDocument> {

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
        BudgetAdjustmentAccountingLine baLine = (BudgetAdjustmentAccountingLine) accountingLine;
        
        baLine.setAmount(KualiDecimal.ZERO);
        setAmountIfNotNull(baLine::setBaseBudgetAdjustmentAmount, xmlLine.getBaseAmount());
        setAmountIfNotNull(baLine::setCurrentBudgetAdjustmentAmount, xmlLine.getAmount());
        setAmountIfNotNull(baLine::setFinancialDocumentMonth1LineAmount, xmlLine.getMonth01Amount());
        setAmountIfNotNull(baLine::setFinancialDocumentMonth2LineAmount, xmlLine.getMonth02Amount());
        setAmountIfNotNull(baLine::setFinancialDocumentMonth3LineAmount, xmlLine.getMonth03Amount());
        setAmountIfNotNull(baLine::setFinancialDocumentMonth4LineAmount, xmlLine.getMonth04Amount());
        setAmountIfNotNull(baLine::setFinancialDocumentMonth5LineAmount, xmlLine.getMonth05Amount());
        setAmountIfNotNull(baLine::setFinancialDocumentMonth6LineAmount, xmlLine.getMonth06Amount());
        setAmountIfNotNull(baLine::setFinancialDocumentMonth7LineAmount, xmlLine.getMonth07Amount());
        setAmountIfNotNull(baLine::setFinancialDocumentMonth8LineAmount, xmlLine.getMonth08Amount());
        setAmountIfNotNull(baLine::setFinancialDocumentMonth9LineAmount, xmlLine.getMonth09Amount());
        setAmountIfNotNull(baLine::setFinancialDocumentMonth10LineAmount, xmlLine.getMonth10Amount());
        setAmountIfNotNull(baLine::setFinancialDocumentMonth11LineAmount, xmlLine.getMonth11Amount());
        setAmountIfNotNull(baLine::setFinancialDocumentMonth12LineAmount, xmlLine.getMonth12Amount());
        
        return accountingLine;
    }

    protected <N extends Number> void setAmountIfNotNull(Consumer<N> amountSetter, N amount) {
        if (ObjectUtils.isNotNull(amount)) {
            amountSetter.accept(amount);
        }
    }

    @Override
    protected void populateCustomAccountingDocumentData(
            CuBudgetAdjustmentDocument document, AccountingXmlDocumentEntry documentEntry) {
        super.populateCustomAccountingDocumentData(document, documentEntry);
        validateAndSetFiscalYear(document, documentEntry);
    }

    protected void validateAndSetFiscalYear(CuBudgetAdjustmentDocument document, AccountingXmlDocumentEntry documentEntry) {
        
    }

}
