package edu.cornell.kfs.concur.batch.service.impl;

import java.util.List;

import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractFile;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractRequestDetailFileLine;
import edu.cornell.kfs.concur.batch.service.ConcurRequestExtractFileValidationService;

import org.kuali.rice.core.api.util.type.KualiDecimal;

public class ConcurRequestExtractFileValidationServiceImpl implements ConcurRequestExtractFileValidationService {

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurRequestExtractFileValidationServiceImpl.class);

    public boolean fileRowCountMatchesHeaderRowCount(ConcurRequestExtractFile requestExtractFile) {
        int countEntryDetailLines = 0;
        List<ConcurRequestExtractRequestDetailFileLine> requestDetailLines = requestExtractFile.getRequestDetails();
        for (ConcurRequestExtractRequestDetailFileLine detailLine : requestDetailLines) {
            countEntryDetailLines = countEntryDetailLines + detailLine.getRequestEntryDetails().size();
        }
        if ((requestDetailLines.size() + countEntryDetailLines) == requestExtractFile.getRecordCount()) {
            return true;
        }
        else {
            LOG.error("Header row count validation failed. Header record count was =" +requestExtractFile.getRecordCount()+"= while file line count was =" +(requestDetailLines.size() + countEntryDetailLines)+ "=");
            return false;
        }
    }

    public boolean fileApprovedAmountsMatchHeaderApprovedAmount(ConcurRequestExtractFile requestExtractFile) {
        double detailLinesApprovedAmountSum = 0;
        List<ConcurRequestExtractRequestDetailFileLine> requestDetailLines = requestExtractFile.getRequestDetails();
        for (ConcurRequestExtractRequestDetailFileLine detailLine : requestDetailLines) {
            detailLinesApprovedAmountSum = detailLinesApprovedAmountSum + detailLine.getRequestAmount().doubleValue();
        }
        KualiDecimal detailLinesAmountSumKualiDecimal = new KualiDecimal(detailLinesApprovedAmountSum);
        if (detailLinesAmountSumKualiDecimal.doubleValue() == requestExtractFile.getTotalApprovedAmount().doubleValue()) {
            return true;
        }
        else {
            LOG.error("Header amount validation failed. Header amount was =" +requestExtractFile.getTotalApprovedAmount().toString()+"= while file calculated amount was =" +detailLinesAmountSumKualiDecimal.toString()+ "=");
            return false;
        }
    }

}
