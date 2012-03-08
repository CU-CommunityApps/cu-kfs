package edu.cornell.kfs.module.bc.batch.service;

import java.io.File;

/**
 * A service that contains methods to populate the BC and SIP related tables with PS data.
 */
public interface PSBudgetFeedService {

    /**
     * Loads BC and SIP related tables with PS data.
     * 
     * @param fileName the file containing the data to be loaded
     * @param currentFileName the name of the last file that was successfully loaded in
     * the database
     * @param startFresh tells whether the existing data for BC should be wiped out and
     * the new data from PS loaded in.
     * @param existingErrorFile
     * @param newErrorFile
     * 
     * @return true if successful, false otherwise
     */
    public boolean loadBCDataFromPS(String fileName, String currentFileName, File existingErrorFile, File newErrorFile,
            boolean startFresh);

}
