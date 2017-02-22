package edu.cornell.kfs.concur.service;

import org.kuali.kfs.sys.batch.InitiateDirectory;

public interface ConcurRequestExtractFileService extends InitiateDirectory {

    public boolean requestExtractHeaderRowValidatesToFileContents(String fileName);

    public void performRejectedRequestExtractFileTasks(String fileName);

    public void performAcceptedRequestExtractFileTasks(String fileName);

    public void processRequestExtractFile(String fileName);

}
