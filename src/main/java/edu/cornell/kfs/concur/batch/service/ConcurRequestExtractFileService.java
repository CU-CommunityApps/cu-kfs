package edu.cornell.kfs.concur.batch.service;

import java.util.List;

import org.kuali.kfs.sys.batch.FlatFileDataHandler;

public interface ConcurRequestExtractFileService extends FlatFileDataHandler {

    List<String> getUnprocessedRequestExtractFiles();

    boolean processFile(String requestExtractFileName);

}
