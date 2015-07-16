package edu.cornell.kfs.fp.batch.service;


public interface ProcurementCardSummaryFeedService {

    /**
     * Loads FP_PCARD_SUMMARY_T table with USBank pcard data.
     * 
     * @param fileName the file containing the data to be loaded
     * @param currentFileName the name of the last file that was successfully loaded in
     * the database
     * 
     * @return true if successful, false otherwise
     */
    public boolean loadPCardDataFromBatchFile(String fileName);

}
