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
import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.Job;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.coreservice.api.parameter.EvaluationOperator;
import org.kuali.rice.coreservice.api.parameter.Parameter;
import org.kuali.rice.coreservice.api.parameter.ParameterType;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.krad.service.BusinessObjectService;

import edu.cornell.kfs.module.bc.CUBCConstants;
import edu.cornell.kfs.module.bc.CUBCParameterKeyConstants;
import edu.cornell.kfs.module.bc.batch.service.PSBudgetFeedService;

/**
 * 
 */
public class PSBudgetFeedStep extends AbstractStep {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PSBudgetFeedStep.class);

    protected BatchInputFileService batchInputFileService;
    protected BatchInputFileType psBudgetFeedFlatInputFileType;
    protected BusinessObjectService businessObjectService;
    protected DateTimeService dateTimeService;
    protected ParameterService parameterService;
    protected PSBudgetFeedService psBudgetFeedService;

    /**
     * @see org.kuali.kfs.sys.batch.Step#execute(java.lang.String, java.util.Date)
     */
    public boolean execute(String jobName, Date jobRunDate) {

        //get all done files
        List<String> fileNamesToLoad = batchInputFileService
                .listInputFileNamesWithDoneFile(psBudgetFeedFlatInputFileType);

        boolean processSuccess = false;
        List<String> doneFiles = new ArrayList<String>();

        // if multiple .done files present only process the most current one
        String fileToProcess = getMostCurrentFileName(fileNamesToLoad);

        // add all the .done files in the list of files to be removed in
        // case of success
        doneFiles.addAll(fileNamesToLoad);

        // get the current file name
        String currentFileName = null;
        List<String> currentFiles = listInputFileNamesWithCurrentFile(psBudgetFeedFlatInputFileType);

        if (currentFiles != null && currentFiles.size() > 0) {
            // there should only be one current file otherwise it's a mistake and we should just ignore them
            currentFileName = currentFiles.get(0);
        }

        boolean startFresh = getRunPopulateCSFTRackerForNewYear();

        // process the latest file
        if (fileToProcess != null) {
            // create a new _error file for the new .done file to be processed today
            File newErrorFile = createErrorFile(fileToProcess);
            // get the existing _error file for the last psBudgetFeed job run
            File existingErrorFile = getErrorFile(currentFileName);

            processSuccess = psBudgetFeedService.loadBCDataFromPS(fileToProcess, currentFileName, existingErrorFile,
                    newErrorFile, startFresh);
        }

        // if successfully processed remove all the .done files, remove .current files and create new current file
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

        Parameter runIndicatorParameter = getParameterService().getParameter( CUBCConstants.RUN_FOR_NEW_YEAR_PARAMETER_NAMESPACE_CODE, 
        																	  CUBCConstants.RUN_FOR_NEW_YEAR_PARAMETER_NAMESPACE_STEP, 
        																	  CUBCParameterKeyConstants.RUN_PS_BUDGET_FEED_FOR_NEW_YEAR);
        if (runIndicatorParameter == null) {
            Parameter.Builder newParm = Parameter.Builder.create(CUBCConstants.RUN_FOR_NEW_YEAR_PARAMETER_APPLICATION_NAMESPACE_CODE, 
            																   CUBCConstants.RUN_FOR_NEW_YEAR_PARAMETER_NAMESPACE_CODE, 
            																   CUBCConstants.RUN_FOR_NEW_YEAR_PARAMETER_NAMESPACE_STEP, 
            																   CUBCParameterKeyConstants.RUN_PS_BUDGET_FEED_FOR_NEW_YEAR, 
            																   ParameterType.Builder.create(CUBCConstants.RUN_FOR_NEW_YEAR_PARAMETER_TYPE));
            newParm.setEvaluationOperator(EvaluationOperator.ALLOW);
            newParm.setDescription(CUBCConstants.RUN_FOR_NEW_YEAR_PARAMETER_DESCRIPTION);
            newParm.setValue(CUBCConstants.RUN_FOR_NEW_YEAR_PARAMETER_VALUE);
            getParameterService().createParameter(newParm.build());


        } else {
            Parameter.Builder newParm = Parameter.Builder.create(runIndicatorParameter);
            newParm.setValue(CUBCConstants.RUN_FOR_NEW_YEAR_PARAMETER_VALUE);
            getParameterService().updateParameter(newParm.build());
        }
    }

    /**
     * Gets the value of the RUN_POPULATE_CSF_TRACKER_FOR_NEW_YEAR parameter.
     * 
     * @return a boolean value for this parameter, true if Y, false if N
     */
    private boolean getRunPopulateCSFTRackerForNewYear() {

        Parameter runIndicatorParameter = getParameterService().getParameter( CUBCConstants.RUN_FOR_NEW_YEAR_PARAMETER_NAMESPACE_CODE, 
				  CUBCConstants.RUN_FOR_NEW_YEAR_PARAMETER_NAMESPACE_STEP, 
				  CUBCParameterKeyConstants.RUN_PS_BUDGET_FEED_FOR_NEW_YEAR);
        
        if (runIndicatorParameter == null) {
            Parameter.Builder newParm = Parameter.Builder.create(CUBCConstants.RUN_FOR_NEW_YEAR_PARAMETER_APPLICATION_NAMESPACE_CODE, 
            																   CUBCConstants.RUN_FOR_NEW_YEAR_PARAMETER_NAMESPACE_CODE, 
            																   CUBCConstants.RUN_FOR_NEW_YEAR_PARAMETER_NAMESPACE_STEP, 
            																   CUBCParameterKeyConstants.RUN_PS_BUDGET_FEED_FOR_NEW_YEAR, 
            																   ParameterType.Builder.create(CUBCConstants.RUN_FOR_NEW_YEAR_PARAMETER_TYPE));
            newParm.setEvaluationOperator(EvaluationOperator.ALLOW);
            newParm.setDescription(CUBCConstants.RUN_FOR_NEW_YEAR_PARAMETER_DESCRIPTION);
            newParm.setValue(CUBCConstants.RUN_FOR_NEW_YEAR_PARAMETER_VALUE);
            getParameterService().createParameter(newParm.build());


        }

        boolean runPopulateCSFTRackerForNewYear = parameterService.getParameterValueAsBoolean(PSBudgetFeedStep.class,
                    CUBCParameterKeyConstants.RUN_PS_BUDGET_FEED_FOR_NEW_YEAR);

        return runPopulateCSFTRackerForNewYear;

    }

    /**
     * Gets the associated _error file for the given input file name if the file exists or
     * creates a new _error file if that does not exist.
     * 
     * @param fileToProcess
     * @return a _error file
     */
    protected File getErrorFile(String fileToProcess) {

        File errorFile = new File(StringUtils.substringBeforeLast(
                    fileToProcess, ".") + "_error.data");

        if (!errorFile.exists()) {
            LOG.info("Error file _error.data does not exist");
            return null;
        }

        return errorFile;

    }

    /**
     * Creates the associated _error file for the given input file name.
     * 
     * @param fileToProcess
     * @return a _error file
     */
    protected File createErrorFile(String fileToProcess) {

        File errorFile = new File(StringUtils.substringBeforeLast(
                    fileToProcess, ".") + "_error.data");
        try {
            if (!errorFile.exists()) {
                if (!errorFile.createNewFile()) {
                    LOG.info("Error creating the _error.data file");
                }
            } else {
                LOG.info("The _error.data file already exists for input: " + fileToProcess);
            }
        } catch (IOException e) {
            LOG.error("Exception while creating the _error.data file " + e.getMessage());
        }

        return errorFile;

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
     * This overridden method ...
     * 
     * @see org.kuali.kfs.sys.batch.AbstractStep#setParameterService(org.kuali.rice.kns.service.ParameterService)
     */
    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    /**
     * Sets the businessObjectService.
     * 
     * @param businessObjectService
     */
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    /**
     * Sets the psBudgetFeedService.
     * 
     * @param psBudgetFeedService
     */
    public void setPsBudgetFeedService(PSBudgetFeedService psBudgetFeedService) {
        this.psBudgetFeedService = psBudgetFeedService;
    }

    /**
     * Gets the psBudgetFeedService.
     * 
     * @return psBudgetFeedService
     */
    public PSBudgetFeedService getPsBudgetFeedService() {
        return psBudgetFeedService;
    }

    /**
     * Sets the psBudgetFeedFlatInputFileType.
     * 
     * @param psBudgetFeedFlatInputFileType
     */
    public void setPsBudgetFeedFlatInputFileType(BatchInputFileType psBudgetFeedFlatInputFileType) {
        this.psBudgetFeedFlatInputFileType = psBudgetFeedFlatInputFileType;
    }

    /**
     * Gets the psBudgetFeedFlatInputFileType.
     * 
     * @return psBudgetFeedFlatInputFileType
     */
    public BatchInputFileType getPsBudgetFeedFlatInputFileType() {
        return psBudgetFeedFlatInputFileType;
    }

}
