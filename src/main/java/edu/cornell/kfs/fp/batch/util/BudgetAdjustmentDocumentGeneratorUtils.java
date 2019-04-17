package edu.cornell.kfs.fp.batch.util;

import java.util.List;
import java.util.function.Consumer;

import org.kuali.kfs.fp.businessobject.BudgetAdjustmentAccountingLine;
import org.kuali.kfs.fp.businessobject.FiscalYearFunctionControl;
import org.kuali.kfs.fp.document.BudgetAdjustmentDocument;
import org.kuali.kfs.fp.service.FiscalYearFunctionControlService;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.businessobject.AccountingLine;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;

public final class BudgetAdjustmentDocumentGeneratorUtils {
    
    private BudgetAdjustmentDocumentGeneratorUtils() {
        
    }
    
    public static <A extends AccountingLine> A buildAccountingLine(A accountingLine, AccountingXmlDocumentAccountingLine xmlLine) {
        BudgetAdjustmentAccountingLine baLine = (BudgetAdjustmentAccountingLine) accountingLine;
        
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
    
    public static <N extends Number> void setAmountIfNotNull(Consumer<N> amountSetter, N amount) {
        if (ObjectUtils.isNotNull(amount)) {
            amountSetter.accept(amount);
        }
    }
    
    public static void validateAndSetFiscalYear(BudgetAdjustmentDocument document, AccountingXmlDocumentEntry documentEntry, 
            FiscalYearFunctionControlService fiscalYearFunctionControlService, String fiscalYearNotAllowedValidationExceptionMessage) {
        Integer fiscalYear = documentEntry.getPostingFiscalYear();
        if (ObjectUtils.isNull(fiscalYear)) {
            throw new ValidationException("Fiscal year cannot be null");
        }
        
        @SuppressWarnings("unchecked")
        List<FiscalYearFunctionControl> allowedYears = fiscalYearFunctionControlService.getBudgetAdjustmentAllowedYears();
        boolean fiscalYearAllowed = allowedYears.stream()
                .anyMatch((fyFunctionControl) -> fiscalYear.equals(fyFunctionControl.getUniversityFiscalYear()));
        if (!fiscalYearAllowed) {
            throw new ValidationException(fiscalYearNotAllowedValidationExceptionMessage + fiscalYear);
        }
        
        document.setPostingYear(fiscalYear);
    }

}
