package edu.cornell.kfs.concur.batch.service;

import java.util.List;

import org.kuali.kfs.sys.batch.FlatFileDataHandler;

public interface ConcurRequestExtractFileService extends FlatFileDataHandler {

    List<String> getUnprocessedRequestExtractFiles();

    boolean processFile(String requestExtractFileName);

    void performDoneFileTasks(String requestExtractFileName);

    void performRejectedRequestExtractFileTasks(String fileName);

    void performAcceptedRequestExtractFileTasks(String fileName);
}
