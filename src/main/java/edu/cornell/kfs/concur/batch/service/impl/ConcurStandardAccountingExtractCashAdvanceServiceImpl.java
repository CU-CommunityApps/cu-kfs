package edu.cornell.kfs.concur.batch.service.impl;

import java.util.List;

import org.codehaus.plexus.util.StringUtils;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractCashAdvanceService;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;

public class ConcurStandardAccountingExtractCashAdvanceServiceImpl implements ConcurStandardAccountingExtractCashAdvanceService {
    
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurStandardAccountingExtractCashAdvanceServiceImpl.class);

    @Override
    public boolean isCashAdvanceLine(ConcurStandardAccountingExtractDetailLine line) {
        return StringUtils.isNotEmpty(line.getCashAdvanceCaKey());
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
                if (LOG.isDebugEnabled()) {
                    LOG.debug("findAccountingInfoForCashAdvanceLine, setting accountNumber to " + line.getAccountNumber() + " and chart to " + line.getChartOfAccountsCode());
                }
                info.setAccountNumber(line.getAccountNumber());
                info.setChart(line.getChartOfAccountsCode());
                break;
            }
        }
        return info;
    }

}
