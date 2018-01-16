package edu.cornell.kfs.fp.document;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.krad.bo.DocumentHeader;
import org.kuali.kfs.krad.document.Copyable;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocumentBase;
import org.kuali.kfs.sys.document.AmountTotaling;
import org.kuali.kfs.sys.document.Correctable;
import org.kuali.kfs.sys.document.service.DebitDeterminerService;
import org.kuali.rice.kew.framework.postprocessor.DocumentRouteStatusChange;

public class AccountFundsUpdateDocument extends AccountingDocumentBase implements Copyable, Correctable, AmountTotaling {

    private String reason;

    public AccountFundsUpdateDocument() {
        super();
    }

    public String getSourceAccountingLinesSectionTitle() {
        return KFSConstants.FROM;
    }

    public String getTargetAccountingLinesSectionTitle() {
        return KFSConstants.TO;
    }

    public boolean isDebit(GeneralLedgerPendingEntrySourceDetail postable) {
        AccountingLine accountingLine = (AccountingLine) postable;
        // only allow income or expense
        DebitDeterminerService isDebitUtils = SpringContext.getBean(DebitDeterminerService.class);
        if (!isDebitUtils.isIncome(accountingLine) && !isDebitUtils.isExpense(accountingLine)) {
            throw new IllegalStateException(isDebitUtils.getDebitCalculationIllegalStateExceptionMessage());
        }
        boolean isDebit = false;
        if (accountingLine.isSourceAccountingLine()) {
            isDebit = isDebitUtils.isDebitConsideringNothingPositiveOnly(this, accountingLine);
        } else if (accountingLine.isTargetAccountingLine()) {
            isDebit = !isDebitUtils.isDebitConsideringNothingPositiveOnly(this, accountingLine);
        } else {
            throw new IllegalStateException(isDebitUtils.getInvalidLineTypeIllegalArgumentExceptionMessage());
        }

        return isDebit;
    }

    @Override
    public boolean documentPerformsSufficientFundsCheck() {
        boolean oldValue = StringUtils.isBlank(this.getFinancialSystemDocumentHeader().getFinancialDocumentInErrorNumber());
        return true; //force sufficient funds check
    }

    public String getReason(){
        return reason;
    }

    public void setReason(String reason){
        this.reason = reason;
    }

    @Override
    public void doRouteStatusChange(DocumentRouteStatusChange statusChangeEvent) {
        super.doRouteStatusChange(statusChangeEvent);

        DocumentHeader documentHeader = getDocumentHeader();
        if(documentHeader.getWorkflowDocument().isFinal()){
            StringBuilder newDocumentDescription = new StringBuilder("Reason: ");
            newDocumentDescription.append(reason.substring(0, 10)).append("; ").append(documentHeader.getDocumentDescription());
            documentHeader.setDocumentDescription(newDocumentDescription.substring(0, KFSConstants.getMaxLengthOfDocumentDescription()));
        }
    }
}
