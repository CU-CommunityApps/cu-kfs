package edu.cornell.kfs.concur.batch.service;

import java.util.List;

import org.kuali.kfs.sys.batch.FlatFileDataHandler;

public interface ConcurSaeCreateRequestedCashAdvanceFileService {

    List<String> getUnprocessedSaeFiles();

    boolean processFile(String standardAccountingExtractFullyQualifiedFileName);
}
