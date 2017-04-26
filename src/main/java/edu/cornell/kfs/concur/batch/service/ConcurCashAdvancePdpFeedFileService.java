package edu.cornell.kfs.concur.batch.service;

import java.io.IOException;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractFile;
import edu.cornell.kfs.concur.batch.report.ConcurRequestExtractBatchReportData;

public interface ConcurCashAdvancePdpFeedFileService {

    boolean createPdpFeedFileForValidatedDetailFileLines(ConcurRequestExtractFile validatedRequestExtractFile, ConcurRequestExtractBatchReportData reportData);

    void createDoneFileForPdpFile(String concurCashAdvancePdpFeedFileName) throws IOException;

}
