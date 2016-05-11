package edu.cornell.kfs.pdp.batch.service;

/**
 * Interface for services that load ACH batch updates from input files
 * (such as .csv files from Workday).
 */
public interface PayeeACHAccountExtractService {
    boolean processACHBatchDetails();
}
