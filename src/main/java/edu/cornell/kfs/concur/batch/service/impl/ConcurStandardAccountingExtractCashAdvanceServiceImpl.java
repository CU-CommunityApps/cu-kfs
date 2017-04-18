package edu.cornell.kfs.concur.batch.service.impl;

import java.util.List;

import org.codehaus.plexus.util.StringUtils;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractCashAdvanceService;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;

public class ConcurStandardAccountingExtractCashAdvanceServiceImpl implements ConcurStandardAccountingExtractCashAdvanceService {

    @Override
    public boolean isCashAdvanceLine(ConcurStandardAccountingExtractDetailLine line) {
        return StringUtils.isNotEmpty(line.getCashAdvanceCaKey());
    }
    
    @Override 
    public ConcurAccountInfo findAccountingInfoForCashAdvanceLine(ConcurStandardAccountingExtractDetailLine cashAdvanceLine, 
            List<ConcurStandardAccountingExtractDetailLine> saeLines) {
        ConcurAccountInfo info = new ConcurAccountInfo();
        String cashAdvanceReportEntryId = cashAdvanceLine.getReportEntryId();
        for (ConcurStandardAccountingExtractDetailLine line : saeLines) {
            if (StringUtils.equalsIgnoreCase(line.getReportEntryId(), cashAdvanceReportEntryId) && !isCashAdvanceLine(line)) {
                info.setAccountNumber(line.getAccountNumber());
                info.setChart(line.getChartOfAccountsCode());
                break;
            }
        }
        return info;
    }

}
