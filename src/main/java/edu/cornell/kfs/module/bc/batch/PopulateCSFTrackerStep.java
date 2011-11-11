package edu.cornell.kfs.module.bc.batch;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.rice.kns.bo.Parameter;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.DateTimeService;
import org.kuali.rice.kns.service.ParameterService;

import edu.cornell.kfs.module.bc.CUBCParameterKeyConstants;
import edu.cornell.kfs.module.bc.batch.service.PopulateCSFTrackerService;

/**
 * 
 */
public class PopulateCSFTrackerStep extends AbstractStep {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PopulateCSFTrackerStep.class);

    // parameter constants
    private static final String RUN_FOR_NEW_YEAR_PARAMETER_NAMESPACE_CODE = "KFS-BC";
    private static final String RUN_FOR_NEW_YEAR_PARAMETER_NAMESPACE_STEP = "PopulateCSFTrackerStep";
    private static final String RUN_FOR_NEW_YEAR_PARAMETER_VALUE = "N";
    private static final String RUN_FOR_NEW_YEAR_PARAMETER_ALLOWED = "A";
    private static final String RUN_FOR_NEW_YEAR_PARAMETER_DESCRIPTION = "Tells the populateCSFTracker job if it should wipe out the entries in the CSF Tracker table";
    private static final String RUN_FOR_NEW_YEAR_PARAMETER_TYPE = "CONFG";
    private static final String RUN_FOR_NEW_YEAR_PARAMETER_APPLICATION_NAMESPACE_CODE = "KFS";

    protected BatchInputFileService batchInputFileService;
    protected BatchInputFileType csfTrackerFlatInputFileType;
    protected BusinessObjectService businessObjectService;
    protected DateTimeService dateTimeService;
    protected ParameterService parameterService;
    protected PopulateCSFTrackerService populateCSFTrackerService;

    /**
     * @see org.kuali.kfs.sys.batch.Step#execute(java.lang.String, java.util.Date)
     */
    public boolean execute(String jobName, Date jobRunDate) {

        //get all done files
        List<String> fileNamesToLoad = batchInputFileService
                .listInputFileNamesWithDoneFile(csfTrackerFlatInputFileType);

        boolean processSuccess = false;
        List<String> doneFiles = new ArrayList<String>();

        String fileToProcess = getMostCurrentFileName(fileNamesToLoad);

        // add all the .done files in the list of files to be removed in
        // case of success
        doneFiles.addAll(fileNamesToLoad);

        // get the current file name
        String currentFileName = null;
        List<String> currentFiles = listInputFileNamesWithCurrentFile(csfTrackerFlatInputFileType);

        if (currentFiles != null && currentFiles.size() > 0) {
            // there should only be one current file otherwise it's a mistake and we should just ignore them
            currentFileName = currentFiles.get(0);
        }

        // process the latest file
        if (fileToProcess != null) {
            processSuccess = populateCSFTrackerService
                    .populateCSFTracker(fileToProcess, currentFileName);
        }

        // if successfully processed remove all the .done files, remove .current files and create new current file, set RUN_CSF_TRACKER
        if (processSuccess) {
            removeDoneFiles(doneFiles);
            removeCurrentFiles(currentFiles);
            createNewCurrentFile(fileToProcess);
            setRunPopulateCSFTrackerForNewYearParameter();
        }

        return processSuccess;

    }

    /**
     * Gets a list with all the files with .current
     */
    public List<String> listInputFileNamesWithCurrentFile(BatchInputFileType batchInputFileType) {
        if (batchInputFileType == null) {
            LOG.error("an invalid(null) argument was given");
            throw new IllegalArgumentException("an invalid(null) argument was given");
        }

        File batchTypeDirectory = new File(batchInputFileType.getDirectoryPath());
        File[] currentFiles = batchTypeDirectory.listFiles(new CurrentFilenameFilter());

        List<String> batchInputFiles = new ArrayList<String>();
        for (int i = 0; i < currentFiles.length; i++) {
            File currentFile = currentFiles[i];
            File dataFile = new File(StringUtils.substringBeforeLast(currentFile.getPath(), ".") + "."
                    + batchInputFileType.getFileExtension());
            if (dataFile.exists()) {
                batchInputFiles.add(dataFile.getPath());
            }
        }

        return batchInputFiles;
    }

    /**
     * Retrieves files in a directory with the .current extension.
     */
    protected class CurrentFilenameFilter implements FilenameFilter {
        /**
         * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
         */
        public boolean accept(File dir, String name) {
            return name.endsWith(".current");
        }
    }

    /**
     * Extract the file date from the file name. The file name format is expected to be
     * bc_csftracker_pay624cu_yymmdd.data
     * 
     * @param inputFileName
     * @return the file creation date
     * @throws ParseException
     */
    private Date extractDateFromFileName(String inputFileName)
            throws ParseException {

        Date date = null;

        if (inputFileName != null) {
            String dateString = inputFileName.substring(inputFileName.lastIndexOf("_") + 1,
                    inputFileName.lastIndexOf("."));
            date = dateTimeService.convertToDate(dateString);
        }

        return date;

    }

    /**
     * Gets the most current file name from a list of dated file names.
     * 
     * @param fileNames
     * @return the most current file name
     */
    private String getMostCurrentFileName(List<String> fileNames) {
        String mostCurrentFileName = null;
        Date latestDate = null;

        if (fileNames != null) {

            for (String inputFileName : fileNames) {
                // select only the latest file to be processed;
                Date date;
                try {
                    date = extractDateFromFileName(inputFileName);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

                if (latestDate == null) {
                    latestDate = date;
                    mostCurrentFileName = inputFileName;
                }
                if (latestDate != null) {
                    if (latestDate.compareTo(date) < 0) {
                        latestDate = date;
                        mostCurrentFileName = inputFileName;
                    }
                }
            }
        }

        return mostCurrentFileName;
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
     * Clears out associated .current files for the processed data files.
     */
    private void removeCurrentFiles(List<String> dataFileNames) {

        for (String dataFileName : dataFileNames) {

            File currentFile = new File(StringUtils.substringBeforeLast(
                    dataFileName, ".") + ".current");
            if (currentFile.exists()) {
                currentFile.delete();
            }
        }
    }

    /**
     * Create a new .current file for the last successfully processed file.
     */
    private void createNewCurrentFile(String processedFile) {

        File currentFile = new File(StringUtils.substringBeforeLast(
                    processedFile, ".") + ".current");
        try {
            if (!currentFile.createNewFile()) {
                LOG.info("Error creating the .current file created");
            }
        } catch (IOException e) {
            LOG.error("Exception while createing the .current file " + e.getMessage());
        }
    }

    /**
     * This method sets a parameter that tells the step that it has already run and it
     * does not need to run again.
     */
    private void setRunPopulateCSFTrackerForNewYearParameter() {
        boolean runPopulateCSFTRackerForNewYear = parameterService.getIndicatorParameter(PopulateCSFTrackerStep.class,
                CUBCParameterKeyConstants.RUN_POPULATE_CSF_TRACKER_FOR_NEW_YEAR);

        Map<String, Object> keyMap = new HashMap<String, Object>();
        keyMap.put("parameterNamespaceCode", RUN_FOR_NEW_YEAR_PARAMETER_NAMESPACE_CODE);
        keyMap.put("parameterDetailTypeCode", RUN_FOR_NEW_YEAR_PARAMETER_NAMESPACE_STEP);
        keyMap.put("parameterName", CUBCParameterKeyConstants.RUN_POPULATE_CSF_TRACKER_FOR_NEW_YEAR);
        keyMap.put("parameterApplicationNamespaceCode", RUN_FOR_NEW_YEAR_PARAMETER_APPLICATION_NAMESPACE_CODE);

        if (runPopulateCSFTRackerForNewYear) {

            // first see if we can find an existing Parameter object with this key
            Parameter runIndicatorParameter = (Parameter) businessObjectService.findByPrimaryKey(Parameter.class,
                    keyMap);
            if (runIndicatorParameter == null) {
                runIndicatorParameter = new Parameter();
                runIndicatorParameter.setVersionNumber(new Long(1));
                runIndicatorParameter.setParameterNamespaceCode(RUN_FOR_NEW_YEAR_PARAMETER_NAMESPACE_CODE);
                runIndicatorParameter.setParameterDetailTypeCode(RUN_FOR_NEW_YEAR_PARAMETER_NAMESPACE_STEP);
                runIndicatorParameter.setParameterName(CUBCParameterKeyConstants.RUN_POPULATE_CSF_TRACKER_FOR_NEW_YEAR);
                runIndicatorParameter.setParameterDescription(RUN_FOR_NEW_YEAR_PARAMETER_DESCRIPTION);
                runIndicatorParameter.setParameterConstraintCode(RUN_FOR_NEW_YEAR_PARAMETER_ALLOWED);
                runIndicatorParameter.setParameterTypeCode(RUN_FOR_NEW_YEAR_PARAMETER_TYPE);
                runIndicatorParameter
                        .setParameterApplicationNamespaceCode(RUN_FOR_NEW_YEAR_PARAMETER_APPLICATION_NAMESPACE_CODE);
            }
            runIndicatorParameter.setParameterValue(RUN_FOR_NEW_YEAR_PARAMETER_VALUE);
            businessObjectService.save(runIndicatorParameter);
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

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
