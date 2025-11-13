package edu.cornell.kfs.fp.batch;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;

import edu.cornell.kfs.fp.batch.service.CardServicesUtilityService;
import edu.cornell.kfs.fp.batch.service.TravelMealCardFileFeedService;
import edu.cornell.kfs.sys.CUKFSConstants;

import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.service.BusinessObjectService;

public class TravelMealCardLoadFileStep extends AbstractStep {

    protected BatchInputFileService batchInputFileService;
    protected BatchInputFileType travelMealCardFlatInputFileType;
    protected BusinessObjectService businessObjectService;
    protected DateTimeService dateTimeService;
    protected TravelMealCardFileFeedService travelMealCardFileFeedService;
    protected CardServicesUtilityService cardServicesUtilityService;

    public boolean execute(String arg0, LocalDateTime arg1) throws InterruptedException {
        boolean processSuccess = false;

        //Obtain all .done file names, there could be more than one
        List<String> fileNamesToLoad = batchInputFileService.listInputFileNamesWithDoneFile(travelMealCardFlatInputFileType);
        List<String> doneFiles = new ArrayList<String>(fileNamesToLoad);

        //Identify and process the most current file
        String fileToProcess = getMostCurrentFileName(fileNamesToLoad);
        if (fileToProcess != null) {
            processSuccess = travelMealCardFileFeedService.loadTmCardDataFromBatchFile(fileToProcess);
        } else {
            //Retain current data in the tables and send email that no new data file received for processing.
            travelMealCardFileFeedService.sendNotificationFileNotReceived();
        }
        
        if (processSuccess) {
            cardServicesUtilityService.removeDoneFiles(doneFiles);
        }
        return processSuccess;
    }

    /**
     * Extract the file date from the file name. 
     * The file name format is expected to be  fp_tmcard_verify_yyyymmdd.data
     */
    private Date extractDateFromFileName(String inputFileName) throws ParseException {
        Date date = null;
        if (inputFileName != null) {
            String dateString = inputFileName.substring(inputFileName.lastIndexOf(CUKFSConstants.UNDERSCORE) + 1,
                    inputFileName.lastIndexOf(CUKFSConstants.DELIMITER));
            // date in filename is YYYYMMDD format, change to MM/DD/YYYY required by DateTimeService
            String outputDateString = cardServicesUtilityService.changeFormatFromYYYYMMDDToSlashedMMDDYYYY(dateString);
            date = dateTimeService.convertToDate(outputDateString);
        }
        return date;
    }

    /**
     * Obtain most current file name from a list of dated file names.
     */
    private String getMostCurrentFileName(List<String> fileNames) {
        String mostCurrentFileName = null;
        Date mostCurrentDate = null;

        if (fileNames != null) {
            for (String inputFileName : fileNames) {
                Date fileNameDate;
                try {
                    fileNameDate = extractDateFromFileName(inputFileName);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

                if (mostCurrentDate == null) {
                    mostCurrentDate = fileNameDate;
                    mostCurrentFileName = inputFileName;
                }
                if (mostCurrentDate != null && mostCurrentDate.compareTo(fileNameDate) < 0) {
                    mostCurrentDate = fileNameDate;
                    mostCurrentFileName = inputFileName;
                }
            }
        }
        return mostCurrentFileName;
    }

     public BatchInputFileService getBatchInputFileService() {
         return batchInputFileService;
     }

     public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
         this.batchInputFileService = batchInputFileService;
     }

     public BatchInputFileType getTravelMealCardFlatInputFileType() {
         return travelMealCardFlatInputFileType;
     }

     public void setTravelMealCardFlatInputFileType(BatchInputFileType travelMealCardFlatInputFileType) {
         this.travelMealCardFlatInputFileType = travelMealCardFlatInputFileType;
     }

     public BusinessObjectService getBusinessObjectService() {
         return businessObjectService;
     }

     public void setBusinessObjectService(BusinessObjectService businessObjectService) {
         this.businessObjectService = businessObjectService;
     }

     public DateTimeService getDateTimeService() {
         return dateTimeService;
     }

     public void setDateTimeService(DateTimeService dateTimeService) {
         this.dateTimeService = dateTimeService;
     }

     public TravelMealCardFileFeedService getTravelMealCardFileFeedService() {
         return travelMealCardFileFeedService;
     }

     public void setTravelMealCardFileFeedService(TravelMealCardFileFeedService travelMealCardFileFeedService) {
         this.travelMealCardFileFeedService = travelMealCardFileFeedService;
     }

     public CardServicesUtilityService getCardServicesUtilityService() {
         return cardServicesUtilityService;
     }

     public void setCardServicesUtilityService(CardServicesUtilityService cardServicesUtilityService) {
         this.cardServicesUtilityService = cardServicesUtilityService;
     }

}
