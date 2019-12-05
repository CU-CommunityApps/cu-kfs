package edu.cornell.kfs.concur.batch.service;

import java.io.IOException;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.report.ConcurSaeRequestedCashAdvanceBatchReportData;

public interface ConcurCreateCashAdvancePdpFeedFileService {

    boolean createPdpFeedFileForValidatedDetailFileLines(ConcurStandardAccountingExtractFile validatedRequestExtractFile, ConcurSaeRequestedCashAdvanceBatchReportData reportData);

    void createDoneFileForPdpFile(String concurCashAdvancePdpFeedFileName) throws IOException;

}
