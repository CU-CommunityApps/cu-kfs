package edu.cornell.kfs.concur.batch.service.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractCashAdvanceService;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;

public class ConcurStandardAccountingExtractCashAdvanceServiceImpl implements ConcurStandardAccountingExtractCashAdvanceService {
    
	private static final Logger LOG = LogManager.getLogger(ConcurStandardAccountingExtractCashAdvanceServiceImpl.class);

    @Override
    public boolean isAtmCashAdvanceLine(ConcurStandardAccountingExtractDetailLine line) {
        return isCashAdvanceToBeAppliedToReimbursement(line) && StringUtils.equalsIgnoreCase(
                ConcurConstants.CASH_ADVANCE_PAYMENT_CODE_NAME_UNIVERSITY_BILLED_OR_PAID, line.getCashAdvancePaymentCodeName());
    }
    
    @Override
    public boolean isAtmFeeDebitLine(ConcurStandardAccountingExtractDetailLine line) {
        return StringUtils.equalsIgnoreCase(ConcurConstants.EXPENSE_TYPE_ATM_FEE, line.getExpenseType())
                && StringUtils.equalsIgnoreCase(ConcurConstants.DEBIT, line.getJournalDebitCredit());
    }
    
    @Override
    public boolean isAtmFeeCreditLine(ConcurStandardAccountingExtractDetailLine line) {
        return isCashAdvanceToBeAppliedToReimbursement(line)
                && StringUtils.equalsIgnoreCase(ConcurConstants.EXPENSE_TYPE_ATM_FEE, line.getExpenseType())
                && StringUtils.equalsIgnoreCase(ConcurConstants.CREDIT, line.getJournalDebitCredit());
    }
    
    @Override
    public boolean isAtmCashAdvanceLineWithUnusedAmount(ConcurStandardAccountingExtractDetailLine line) {
        return isAtmCashAdvanceLine(line)
                && StringUtils.equalsIgnoreCase(ConcurConstants.PAYMENT_CODE_PSEUDO, line.getPaymentCode());
    }
    
    @Override 
    public ConcurAccountInfo findAccountingInfoForCashAdvanceLine(ConcurStandardAccountingExtractDetailLine cashAdvanceLine, 
            List<ConcurStandardAccountingExtractDetailLine> saeLines) {
        LOG.debug("findAccountingInfoForCashAdvanceLine, entering");
        ConcurAccountInfo info = new ConcurAccountInfo();
        String cashAdvanceReportEntryId = cashAdvanceLine.getReportEntryId();
        if (LOG.isDebugEnabled()) {
            LOG.debug("findAccountingInfoForCashAdvanceLine, cashAdvanceReportEntryId: " + cashAdvanceReportEntryId);
        }
        for (ConcurStandardAccountingExtractDetailLine line : saeLines) {
            if (StringUtils.equalsIgnoreCase(line.getReportEntryId(), cashAdvanceReportEntryId) && !isCashAdvanceToBeAppliedToReimbursement(line)) {
                info.setChart(line.getChartOfAccountsCode());
                info.setAccountNumber(line.getAccountNumber());
                info.setSubAccountNumber(line.getSubAccountNumber());
                info.setObjectCode(line.getJournalAccountCode());
                info.setSubObjectCode(line.getSubObjectCode());
                info.setProjectCode(line.getProjectCode());
                info.setOrgRefId(line.getOrgRefId());
                if (LOG.isDebugEnabled()) {
                    LOG.debug("findAccountingInfoForCashAdvanceLine, returning Concur account info: " + info.toString());
                }
                break;
            }
        }
        return info;
    }
    
    private boolean cashAdvanceKeyExists(ConcurStandardAccountingExtractDetailLine line) {
        return StringUtils.isNotBlank(line.getCashAdvanceKey());
    }
    
    @Override
    public boolean isPreTripCashAdvanceRequestLine(ConcurStandardAccountingExtractDetailLine saeLine) {
        if (StringUtils.isNotEmpty(saeLine.getCashAdvancePaymentCodeName()) 
                && StringUtils.equalsIgnoreCase(saeLine.getCashAdvancePaymentCodeName(), ConcurConstants.SAE_REQUESTED_CASH_ADVANCE_INDICATOR)) {
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public boolean isPreTripCashAdvanceIssuedByCashAdmin(ConcurStandardAccountingExtractDetailLine saeLine) {
        if (StringUtils.isNotBlank(saeLine.getCashAdvanceTransactionType())
                && StringUtils.equalsIgnoreCase(saeLine.getCashAdvanceTransactionType(), ConcurConstants.SAE_REQUESTED_CASH_ADVANCE_APPROVED_BY_CONCUR_ADMIN)) {
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public boolean isCashAdvanceToBeAppliedToReimbursement(ConcurStandardAccountingExtractDetailLine saeLine) {
        if ( (StringUtils.isNotBlank(saeLine.getCashAdvanceTransactionType())
                && StringUtils.equalsIgnoreCase(saeLine.getCashAdvanceTransactionType(), ConcurConstants.SAE_CASH_ADVANCE_BEING_APPLIED_TO_TRIP_REIMBURSEMENT))
              && cashAdvanceKeyExists(saeLine) ) {
            return true;
        } else {
            return false;
        }
    }

}
