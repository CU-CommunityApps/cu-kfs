package edu.cornell.kfs.fp.batch;

import java.io.File;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.service.BusinessObjectService;

import edu.cornell.kfs.fp.batch.service.ProcurementCardSummaryFeedService;


public class ProcurementCardSummaryFeedStep extends AbstractStep {
	   	protected BatchInputFileService batchInputFileService;
	    protected BatchInputFileType procurementCardSummaryFlatInputFileType;
	    protected BusinessObjectService businessObjectService;
	    protected DateTimeService dateTimeService;
	    protected ProcurementCardSummaryFeedService procurementCardSummaryFeedService;
	    

	public boolean execute(String arg0, LocalDateTime arg1) throws InterruptedException {

		//get all done files
        List<String> fileNamesToLoad = batchInputFileService
                .listInputFileNamesWithDoneFile(procurementCardSummaryFlatInputFileType);

        boolean processSuccess = false;
        List<String> doneFiles = new ArrayList<String>();

        // if multiple .done files present only process the most current one
        String fileToProcess = getMostCurrentFileName(fileNamesToLoad);

        // add all the .done files in the list of files to be removed in
        // case of success
        doneFiles.addAll(fileNamesToLoad);

         // process the latest file 
        if (fileToProcess != null) {
             processSuccess = procurementCardSummaryFeedService.loadPCardDataFromBatchFile(fileToProcess);
         }

        // if successfully processed remove all the .done files, remove .current files and create new current file
        if (processSuccess) {
         removeDoneFiles(doneFiles);
         }

        return processSuccess;

    }

 
    /**
     * Extract the file date from the file name. The file name format is expected to be
     * fp_pcard_summary_yyyymmdd.data
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
         // the date comes in YYYYMMDD format so we change it to MM/DD/YYYY
            String outputDateString = dateString.substring(4, 6) 
            		+ "/" + dateString.substring(6) 
            		+ "/" + dateString.substring(0, 4);
            date = dateTimeService.convertToDate(outputDateString);
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

 	
		public BatchInputFileService getBatchInputFileService() {
			return batchInputFileService;
		}

		public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
			this.batchInputFileService = batchInputFileService;
		}

		public BatchInputFileType getProcurementCardSummaryFlatInputFileType() {
			return procurementCardSummaryFlatInputFileType;
		}

		public void setProcurementCardSummaryFlatInputFileType(
				BatchInputFileType procurementCardSummaryFlatInputFileType) {
			this.procurementCardSummaryFlatInputFileType = procurementCardSummaryFlatInputFileType;
		}

		public BusinessObjectService getBusinessObjectService() {
			return businessObjectService;
		}

		public void setBusinessObjectService(BusinessObjectService businessObjectService) {
			this.businessObjectService = businessObjectService;
		}


		   /**
	     * @see org.kuali.kfs.sys.batch.AbstractStep#getDateTimeService()
	     */
	    public DateTimeService getDateTimeService() {
	        return dateTimeService;
	    }

	    /**
	     * @see org.kuali.kfs.sys.batch.AbstractStep#setDateTimeService(org.kuali.kfs.kns.service.DateTimeService)
	     */
	    public void setDateTimeService(DateTimeService dateTimeService) {
	        this.dateTimeService = dateTimeService;
	    }

	
		public ProcurementCardSummaryFeedService getProcurementCardSummaryFeedService() {
			return procurementCardSummaryFeedService;
		}

		public void setProcurementCardSummaryFeedService(
				ProcurementCardSummaryFeedService procurementCardSummaryFeedService) {
			this.procurementCardSummaryFeedService = procurementCardSummaryFeedService;
		}

}
