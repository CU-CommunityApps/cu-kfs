package edu.cornell.kfs.concur.batch.service.impl;

import java.util.List;

import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractFile;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractRequestDetailFileLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractRequestEntryDetailFileLine;
import edu.cornell.kfs.concur.batch.service.ConcurRequestExtractFileValidationService;

import org.kuali.rice.core.api.util.type.KualiDecimal;

public class ConcurRequestExtractFileValidationServiceImpl implements ConcurRequestExtractFileValidationService {

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurRequestExtractFileValidationServiceImpl.class);

    public boolean fileRowCountMatchesHeaderRowCount(ConcurRequestExtractFile requestExtractFile) {
        int fileLineCount = getTotalRequestFileRowCount(requestExtractFile);
        if (fileLineCount == requestExtractFile.getRecordCount()) {
            return true;
        }
        else {
            LOG.error("Header row count validation failed. Header record count was =" + requestExtractFile.getRecordCount() + "= while file line count was =" + fileLineCount + "=");
            return false;
        }
    }

    public boolean fileApprovedAmountsMatchHeaderApprovedAmount(ConcurRequestExtractFile requestExtractFile) {
        KualiDecimal detailLinesAmountSumKualiDecimal = getTotalRequestFileRequestAmount(requestExtractFile);
        if (detailLinesAmountSumKualiDecimal.equals(requestExtractFile.getTotalApprovedAmount())) {
            return true;
        }
        else {
            LOG.error("Header amount validation failed. Header amount was =" + requestExtractFile.getTotalApprovedAmount().toString() + "= while file calculated amount was =" + detailLinesAmountSumKualiDecimal.toString() + "=");
            return false;
        }
    }

    private int getTotalRequestFileRowCount(ConcurRequestExtractFile requestExtractFile) {
        int rowCount = 0;
        if ( (requestExtractFile.getRequestDetails() == null) || (requestExtractFile.getRequestDetails().isEmpty()) ) {
            return rowCount;
        }
        else {
            List<ConcurRequestExtractRequestDetailFileLine> requestDetailLines = requestExtractFile.getRequestDetails();
            for (ConcurRequestExtractRequestDetailFileLine detailLine : requestDetailLines) {
                //adding 1 ensures detailLine is also included in rowCount
                rowCount = rowCount + computedTotalRequestEntryDetailLinesForRequestDetail(detailLine) + 1;
            }
            return rowCount;
        }
    }

    private int computedTotalRequestEntryDetailLinesForRequestDetail(ConcurRequestExtractRequestDetailFileLine requestDetailLine) {
        if ( (requestDetailLine.getRequestEntryDetails() == null) || (requestDetailLine.getRequestEntryDetails().isEmpty()) ) {
            return 0;
        }
        else {
            return requestDetailLine.getRequestEntryDetails().size();
        }
    }

    private KualiDecimal getTotalRequestFileRequestAmount(ConcurRequestExtractFile requestExtractFile) {
        KualiDecimal detailLinesApprovedAmountSum = KualiDecimal.ZERO;
        if ( (requestExtractFile.getRequestDetails() == null) || (requestExtractFile.getRequestDetails().isEmpty()) ) {
            return detailLinesApprovedAmountSum;
        }
        else {
            List<ConcurRequestExtractRequestDetailFileLine> requestDetailLines = requestExtractFile.getRequestDetails();
            for (ConcurRequestExtractRequestDetailFileLine detailLine : requestDetailLines) {
                detailLinesApprovedAmountSum = detailLinesApprovedAmountSum.add(detailLine.getRequestAmount());
            }
            return detailLinesApprovedAmountSum;
        }
    }

}
