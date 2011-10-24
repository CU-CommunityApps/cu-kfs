package edu.cornell.kfs.module.bc.batch;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.rice.kns.service.DateTimeService;

import edu.cornell.kfs.module.bc.batch.service.PopulateCSFTrackerService;

/**
 * 
 */
public class PopulateCSFTrackerStep extends AbstractStep {

    protected PopulateCSFTrackerService populateCSFTrackerService;
    protected BatchInputFileService batchInputFileService;
    protected DateTimeService dateTimeService;
    protected BatchInputFileType csfTrackerFlatInputFileType;

    /**
     * @see org.kuali.kfs.sys.batch.Step#execute(java.lang.String, java.util.Date)
     */
    public boolean execute(String jobName, Date jobRunDate) {

        //get all done files
        List<String> fileNamesToLoad = batchInputFileService
                .listInputFileNamesWithDoneFile(csfTrackerFlatInputFileType);

        boolean processSuccess = false;
        List<String> doneFiles = new ArrayList<String>();

        String fileToProcess = null;
        Date latestDate = null;

        for (String inputFileName : fileNamesToLoad) {
            // select only the latest file to be processed;
            Date date;
            try {
                date = extractDateFromFileName(inputFileName);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            if (latestDate == null) {
                latestDate = date;
                fileToProcess = inputFileName;
            }
            if (latestDate != null) {
                if (latestDate.compareTo(date) < 0) {
                    latestDate = date;
                    fileToProcess = inputFileName;
                }
            }

            // add all the .done files in the list of files to be removed in
            // case of success
            doneFiles.add(inputFileName);

        }

        // process the latest file
        if (fileToProcess != null) {
            processSuccess = populateCSFTrackerService
                    .populateCSFTracker(fileToProcess);
        }

        // if successfully processed remove all the .done files
        if (processSuccess) {
            removeDoneFiles(doneFiles);
        }

        return processSuccess;

    }

    /**
     * Extract the file date from the file name.
     * 
     * @param inputFileName
     * @return
     * @throws ParseException
     */
    private Date extractDateFromFileName(String inputFileName)
            throws ParseException {
        Date date = null;
        if (inputFileName != null) {

            //TODO change this to read from the right positions
            date = dateTimeService.convertToDate(inputFileName
                    .substring(61, 71));
        }
        return date;

    }

    /**
     * @see org.kuali.kfs.sys.batch.AbstractStep#getDateTimeService()
     */
    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    /**
     * @see org.kuali.kfs.sys.batch.AbstractStep#setDateTimeService(org.kuali.rice.kns.service.DateTimeService)
     */
    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    /**
     * Clears out associated .done files for the processed data files.
     */
    private void removeDoneFiles(List<String> dataFileNames) {

        for (String dataFileName : dataFileNames) {

            File doneFile = new File(StringUtils.substringBeforeLast(
                    dataFileName, ".") + ".done");
            if (doneFile.exists()) {
                doneFile.delete();
            }
        }
    }

    /**
     * Gets the populateCSFTrackerService.
     * 
     * @return populateCSFTrackerService
     */
    public PopulateCSFTrackerService getPopulateCSFTrackerService() {
        return populateCSFTrackerService;
    }

    /**
     * Sets the populateCSFTrackerService.
     * 
     * @param populateCSFTrackerService
     */
    public void setPopulateCSFTrackerService(PopulateCSFTrackerService populateCSFTrackerService) {
        this.populateCSFTrackerService = populateCSFTrackerService;
    }

    /**
     * Gets the batchInputFileService.
     * 
     * @return batchInputFileService
     */
    public BatchInputFileService getBatchInputFileService() {
        return batchInputFileService;
    }

    /**
     * Sets the batchInputFileService.
     * 
     * @param batchInputFileService
     */
    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    /**
     * Gets the csfTrackerFlatInputFileType.
     * 
     * @return csfTrackerFlatInputFileType
     */
    public BatchInputFileType getCsfTrackerFlatInputFileType() {
        return csfTrackerFlatInputFileType;
    }

    /**
     * Sets the csfTrackerFlatInputFileType.
     * 
     * @param csfTrackerFlatInputFileType
     */
    public void setCsfTrackerFlatInputFileType(BatchInputFileType csfTrackerFlatInputFileType) {
        this.csfTrackerFlatInputFileType = csfTrackerFlatInputFileType;
    }

}
