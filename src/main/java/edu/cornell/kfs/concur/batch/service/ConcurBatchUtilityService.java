package edu.cornell.kfs.concur.batch.service;

import java.sql.Date;

import org.kuali.kfs.sys.batch.BatchInputFileType;

import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedFileBaseEntry;

public interface ConcurBatchUtilityService {
    
    String buildFullyQualifiedPdpOutputFileName(String paymentImportDirecotry, String pdpInputfileName);
    
    void createDoneFileForPdpFile(String fullyQualifiedPdpFileName);
    
    boolean createPdpFeedFile(PdpFeedFileBaseEntry pdpFeedFileBaseEntry, String fullyQualifiedPdpFileName);
    
    String formatDate(Date date);
    
    String formatPdpPayeeName(String lastName, String firstName, String middleInitial);
    
    /**
     * 
     * @param documentTypeCode
     * @param concurDocumentId This value will be the RequestId for request extract files and the ReportId for SAE files.
     * @return
     */
    String formatSourceDocumentNumber(String documentTypeCode, String concurDocumentId);
    
    String getConcurParamterValue(String parameterName);
    
    Object loadFile(String fullyQualifiedFileName, BatchInputFileType batchInputFileType);
    
    void removeDoneFiles(String requestExtractFullyQualifiedFileName);
    
}
