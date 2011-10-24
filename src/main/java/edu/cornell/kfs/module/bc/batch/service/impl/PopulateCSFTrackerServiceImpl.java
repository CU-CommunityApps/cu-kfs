package edu.cornell.kfs.module.bc.batch.service.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;

import edu.cornell.kfs.module.bc.batch.service.PopulateCSFTrackerService;

/**
 * An implementation of a service that contains methods to populate the CSF Tracker table
 * (LD_CSF_TRACKER_T).
 */
public class PopulateCSFTrackerServiceImpl implements PopulateCSFTrackerService {

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PopulateCSFTrackerServiceImpl.class);

    protected BatchInputFileService batchInputFileService;
    protected BatchInputFileType csfTrackerFlatInputFileType;

    /**
     * @see edu.cornell.kfs.module.bc.batch.service.PopulateCSFTrackerService#populateCSFTracker(java.lang.String)
     */
    public boolean populateCSFTracker(String fileName) {

        FileInputStream fileContents = null;

        //read file contents
        try {
            fileContents = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            LOG.error("file to parse not found " + fileName, e);
            throw new RuntimeException(
                    "Cannot find the file requested to be parsed " + fileName
                            + " " + e.getMessage(), e);
        }

        Collection csfTackerEntries = null;
        // read csf tracker entries
        try {
            byte[] fileByteContent = IOUtils.toByteArray(fileContents);
            csfTackerEntries = (Collection) batchInputFileService.parse(
                    csfTrackerFlatInputFileType, fileByteContent);
        } catch (IOException e) {
            LOG.error("error while getting file bytes:  " + e.getMessage(), e);
            throw new RuntimeException(
                    "Error encountered while attempting to get file bytes: "
                            + e.getMessage(), e);
        }

        // if no entries read log
        if (csfTackerEntries == null || csfTackerEntries.isEmpty()) {
            LOG.warn("No entries in the PS Job extract input file " + fileName);
        }

        // load entries in CSF tracker
        validateAndloadEntriesInCSFTracker(csfTackerEntries);

        // log the number of entries loaded
        LOG.info("Total entries loaded: " + Integer.toString(csfTackerEntries.size()));
        return true;

    }

    /**
     * Validates the entries to be loaded in the CSF Tracker table.
     * 
     * @param csfTackerEntries the entries to be loaded into the CSF Tracker table.
     */
    private void validateAndloadEntriesInCSFTracker(Collection csfTackerEntries) {
        // validate entry

        // if not valid log it

        // if valid update CSF tracker table

    }

    public BatchInputFileService getBatchInputFileService() {
        return batchInputFileService;
    }

    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    public BatchInputFileType getCsfTrackerFlatInputFileType() {
        return csfTrackerFlatInputFileType;
    }

    public void setCsfTrackerFlatInputFileType(BatchInputFileType csfTrackerFlatInputFileType) {
        this.csfTrackerFlatInputFileType = csfTrackerFlatInputFileType;
    }

}
