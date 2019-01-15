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
    public boolean isCashAdvanceLine(ConcurStandardAccountingExtractDetailLine line) {
        return StringUtils.isNotBlank(line.getCashAdvanceKey());
    }
    
    @Override
    public boolean isAtmCashAdvanceLine(ConcurStandardAccountingExtractDetailLine line) {
        return isCashAdvanceLine(line) && StringUtils.equalsIgnoreCase(
                ConcurConstants.CASH_ADVANCE_PAYMENT_CODE_NAME_UNIVERSITY_BILLED_OR_PAID, line.getCashAdvancePaymentCodeName());
    }
    
    @Override
    public boolean isAtmFeeDebitLine(ConcurStandardAccountingExtractDetailLine line) {
        return StringUtils.equalsIgnoreCase(ConcurConstants.EXPENSE_TYPE_ATM_FEE, line.getExpenseType())
                && StringUtils.equalsIgnoreCase(ConcurConstants.DEBIT, line.getJournalDebitCredit());
    }
    
    @Override
    public boolean isAtmFeeCreditLine(ConcurStandardAccountingExtractDetailLine line) {
        return isCashAdvanceLine(line)
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
            if (StringUtils.equalsIgnoreCase(line.getReportEntryId(), cashAdvanceReportEntryId) && !isCashAdvanceLine(line)) {
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

}
