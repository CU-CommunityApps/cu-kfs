package edu.cornell.kfs.module.bc.batch.service;

/**
 * A service that contains methods to populate the CSF Tracker table (LD_CSF_TRACKER_T).
 */
public interface PopulateCSFTrackerService {

    /**
     * Populates/Updates the CSF Tracker table based on data in the input file.
     * 
     * @param fileName the input file containing the data to be loaded in the CSF tracker
     * table
     * @return true if successful, false otherwise
     */
    public boolean populateCSFTracker(String fileName);

}
