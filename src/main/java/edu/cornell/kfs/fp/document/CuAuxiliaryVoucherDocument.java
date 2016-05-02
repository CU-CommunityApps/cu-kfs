package edu.cornell.kfs.fp.document;

import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.fp.document.AuxiliaryVoucherDocument;
import org.kuali.kfs.gl.service.SufficientFundsService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterConstants.COMPONENT;
import org.kuali.kfs.coreservice.framework.parameter.ParameterConstants.NAMESPACE;

@NAMESPACE(namespace = KFSConstants.CoreModuleNamespaces.FINANCIAL)
@COMPONENT(component = "AuxiliaryVoucher")
@SuppressWarnings("deprecation")
public class CuAuxiliaryVoucherDocument extends AuxiliaryVoucherDocument {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean customizeOffsetGeneralLedgerPendingEntry(GeneralLedgerPendingEntrySourceDetail postable, 
            GeneralLedgerPendingEntry explicitEntry, GeneralLedgerPendingEntry offsetEntry) {
        // set the document type to that of a Distrib. Of Income and Expense if it's a recode
        if (isRecodeType()) {
            offsetEntry.setFinancialDocumentTypeCode(KFSConstants.FinancialDocumentTypeCodes.DISTRIBUTION_OF_INCOME_AND_EXPENSE);

            // set the posting period
            java.sql.Date today = SpringContext.getBean(DateTimeService.class).getCurrentSqlDateMidnight();
            offsetEntry.setUniversityFiscalPeriodCode(SpringContext.getBean(AccountingPeriodService.class).getByDate(today).getUniversityFiscalPeriodCode());
            offsetEntry.setUniversityFiscalYear(SpringContext.getBean(AccountingPeriodService.class).getByDate(today).getUniversityFiscalYear());
        }

        // now set the offset entry to the specific offset object code for the AV generated offset fund balance; only if it's an
        // accrual or adjustment
        if (isAccrualType() || isAdjustmentType()) {
            String glpeOffsetObjectCode = SpringContext.getBean(ParameterService.class)
                    .getParameterValueAsString(this.getClass(), getGeneralLedgerPendingEntryOffsetObjectCode());
            offsetEntry.setFinancialObjectCode(glpeOffsetObjectCode);

            // set the posting period
            offsetEntry.setUniversityFiscalPeriodCode(getPostingPeriodCode());
            offsetEntry.setUniversityFiscalYear(getPostingYear()); 
        }

        // set the reversal date to null
        offsetEntry.setFinancialDocumentReversalDate(null);

        // although they are offsets, we need to set the offset indicator to false
        offsetEntry.setTransactionEntryOffsetIndicator(false);

        //KFSMI-798 - refreshNonUpdatableReferences() used instead of refresh(), 
        //GeneralLedgerPendingEntry does not have any updatable references
        offsetEntry.refreshNonUpdateableReferences();
        offsetEntry.setAcctSufficientFundsFinObjCd(SpringContext.getBean(SufficientFundsService.class)
                .getSufficientFundsObjectCode(offsetEntry.getFinancialObject(), offsetEntry.getAccount().getAccountSufficientFundsCode()));

        return true;
    }
    
    @Override
    protected boolean processOffsetGeneralLedgerPendingEntryForRecodes(GeneralLedgerPendingEntrySequenceHelper sequenceHelper, 
            AccountingLine accountingLineCopy, GeneralLedgerPendingEntry explicitEntry, GeneralLedgerPendingEntry offsetEntry) {

        GeneralLedgerPendingEntry explicitEntryDeepCopy = new GeneralLedgerPendingEntry(explicitEntry);

        // set the posting period to current, because DI GLPEs for recodes should post to the current period
        java.sql.Date today = SpringContext.getBean(DateTimeService.class).getCurrentSqlDateMidnight();

        explicitEntryDeepCopy.setUniversityFiscalYear(SpringContext.getBean(AccountingPeriodService.class).getByDate(today)
                .getUniversityFiscalYear()); 

        return super.processOffsetGeneralLedgerPendingEntryForRecodes(sequenceHelper, accountingLineCopy, explicitEntryDeepCopy, offsetEntry);
    }
}
