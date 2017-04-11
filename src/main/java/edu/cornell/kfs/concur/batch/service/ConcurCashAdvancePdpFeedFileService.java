package edu.cornell.kfs.concur.batch.service;

import java.io.IOException;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractFile;

public interface ConcurCashAdvancePdpFeedFileService {

    boolean createPdpFeedFileForValidatedDetailFileLines(ConcurRequestExtractFile validatedRequestExtractFile);

    void createDoneFileForPdpFile(String concurCashAdvancePdpFeedFileName) throws IOException;

}
