package edu.cornell.kfs.concur.service;

import org.kuali.kfs.sys.batch.InitiateDirectory;

public interface ConcurRequestExtractFileService extends InitiateDirectory {

    boolean requestExtractHeaderRowValidatesToFileContents(String fileName);

    void performRejectedRequestExtractFileTasks(String fileName);

    void performAcceptedRequestExtractFileTasks(String fileName);

    void processRequestExtractFile(String fileName);

}
