package edu.cornell.kfs.concur.batch.service;

import java.util.List;

import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractFile;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractRequestDetailFileLine;
import edu.cornell.kfs.concur.batch.report.ConcurRequestExtractBatchReportData;

public interface ConcurRequestExtractFileValidationService {

    boolean requestExtractHeaderRowValidatesToFileContents(ConcurRequestExtractFile requestExtractFile, ConcurRequestExtractBatchReportData reportData);

    void performRequestDetailLineValidation(ConcurRequestExtractRequestDetailFileLine detailFileLine, List<String> uniqueRequestIdsInFile);

}
