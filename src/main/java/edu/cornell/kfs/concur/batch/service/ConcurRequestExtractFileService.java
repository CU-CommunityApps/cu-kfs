package edu.cornell.kfs.concur.batch.service;

import java.util.List;

import org.kuali.kfs.sys.batch.FlatFileDataHandler;

public interface ConcurRequestExtractFileService {

    List<String> getUnprocessedRequestExtractFiles();

    boolean processFile(String requestExtractFullyQualifiedFileName);
}
