package edu.cornell.kfs.concur.batch.service.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractCashAdvanceService;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;

public class ConcurStandardAccountingExtractCashAdvanceServiceImpl implements ConcurStandardAccountingExtractCashAdvanceService {
    
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurStandardAccountingExtractCashAdvanceServiceImpl.class);

    @Override
    public boolean isCashAdvanceLine(ConcurStandardAccountingExtractDetailLine line) {
        return StringUtils.isNotBlank(line.getCashAdvanceKey());
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
