package edu.cornell.kfs.concur.batch.service;

import java.io.IOException;
import java.util.List;

import org.kuali.kfs.krad.exception.ValidationException;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.report.ConcurStandardAccountingExtractBatchReportData;

public interface ConcurStandardAccountingExtractService {

    ConcurStandardAccountingExtractFile parseStandardAccoutingExtractFile(String standardAccountingExtractFileName) throws ValidationException;
    
    List<String> buildListOfFullyQualifiedFileNamesToBeProcessed();

    String extractPdpFeedFromStandardAccountingExtract(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile, ConcurStandardAccountingExtractBatchReportData reportData);

    String extractCollectorFeedFromStandardAccountingExtract(
            ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile, ConcurStandardAccountingExtractBatchReportData reportData);
    
    void createDoneFileForPdpFile(String pdpFileName) throws IOException;

    void removeDoneFileForPdpFileQuietly(String pdpFileName);

    void createDoneFileForCollectorFile(String collectorFileName) throws IOException;

    void populateReportWithInformationOnSpecialCharacterRemoval(
            final ConcurStandardAccountingExtractBatchReportData reportData,
            final ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile);

}
