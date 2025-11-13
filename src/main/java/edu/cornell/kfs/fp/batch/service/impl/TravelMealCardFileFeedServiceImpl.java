package edu.cornell.kfs.fp.batch.service.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.fp.batch.TravelMealCardFlatInputFileType;
import edu.cornell.kfs.fp.batch.service.CardServicesUtilityService;
import edu.cornell.kfs.fp.batch.service.TravelMealCardEmailService;
import edu.cornell.kfs.fp.batch.service.TravelMealCardFileFeedService;
import edu.cornell.kfs.fp.businessobject.TravelMealCardFileLineEntry;
import edu.cornell.kfs.fp.businessobject.TravelMealCardVerificationData;
import edu.cornell.kfs.fp.businessobject.TravelMealCardCertificationData;
import edu.cornell.kfs.fp.businessobject.TravelMealCardFileLineDataWrapper;

@Transactional
public class TravelMealCardFileFeedServiceImpl implements TravelMealCardFileFeedService {
    private static final Logger LOG = LogManager.getLogger();
    
    protected BatchInputFileService batchInputFileService;
    protected TravelMealCardFlatInputFileType travelMealCardFlatInputFileType;
    protected BusinessObjectService businessObjectService;
    protected DateTimeService dateTimeService;
    protected CardServicesUtilityService cardServicesUtilityService;
    protected TravelMealCardEmailService travelMealCardEmailService;
    
    public boolean loadTmCardDataFromBatchFile(String fileName) {
        LOG.info("Reading TMCard file data lines for .done file: {}", fileName);
        List<TravelMealCardFileLineEntry> tmCardFileLineEntryList = readTmCardFileContents(fileName);
        
        if (tmCardFileLineEntryList == null) {
            LOG.info("NO data found in input file. Previous data items retained in Travel and Meal Card Verification and Certification tables.");
            return true;
        }

        LOG.info("Generating TMCard Verification and Certification class pairs in TravelMealCardFileLineDataWrapper from {} file data rows.", tmCardFileLineEntryList.size());
        List<TravelMealCardFileLineDataWrapper> dataClassPairsToLoadToDatabase = new ArrayList<TravelMealCardFileLineDataWrapper>();
        generateTmCardCertifyVerifyPairsToLoadToDB(tmCardFileLineEntryList, dataClassPairsToLoadToDatabase); 

        LOG.info("Loading {} pairs of data objects into the appropriate table CU_FP_TMCARD_VERIFY_T or CU_FP_TMCARD_CERTIFY_T.", dataClassPairsToLoadToDatabase.size());
        loadTmCardObjectPairsIntoDatabase(dataClassPairsToLoadToDatabase);
        return true;
    }
    
    /**
     * Read the incoming TMCard Verification Certification file with the given file name and 
     * build a list of TravelMealCardFileLineEntry objects that represent each line of the file.
     * 
     * NOTE: Visibility of the method is public ONLY for unit testing purposes. and 
     *       is why it is not defined in the TravelMealCardFileFeedService interface.
     */
    public List<TravelMealCardFileLineEntry> readTmCardFileContents(String fileName) {
        try {
            FileInputStream fileContents = null;
            fileContents = new FileInputStream(fileName);
            List<TravelMealCardFileLineEntry> tmCardFileLineEntries = null;

            byte[] fileByteContent = IOUtils.toByteArray(fileContents);
            tmCardFileLineEntries = (List<TravelMealCardFileLineEntry>) batchInputFileService.parse(travelMealCardFlatInputFileType, fileByteContent);

            if (tmCardFileLineEntries == null || tmCardFileLineEntries.isEmpty()) {
                LOG.warn("No data in TMCard Verification-Certification file with name {} ", fileName);
                return null;
            }
            
            return tmCardFileLineEntries;
            
        } catch (FileNotFoundException e) {
            LOG.error("Cannot find TMCard Verification-Certification file to be parsed with name {}.", fileName, e);
            throw new RuntimeException("Cannot find TMCard Verification-Certification file to be parsed with name " 
                    + fileName + ". RuntimeException encountered: " + e.getMessage(), e);
        } catch (IOException e) {
            LOG.error("Error while reading file bytes for file with name " + fileName, e);
            throw new RuntimeException("Error while reading file bytes for file with name " 
                    + fileName + "Encountered IOException " + e.getMessage(), e);
        }
    }

    private void generateTmCardCertifyVerifyPairsToLoadToDB(List<TravelMealCardFileLineEntry> tmCardFileLinesList, 
            List<TravelMealCardFileLineDataWrapper> tmCardDataClassPairsToLoadToDatabase) {
        
        for (TravelMealCardFileLineEntry tmCardFileLine : tmCardFileLinesList) {
            tmCardDataClassPairsToLoadToDatabase.add(generateTmCardWrapperPair(tmCardFileLine));
        }
    }

    /**
     * Generate a wrapper class collection containing pairs of TMCard certification and verification objects 
     * which represent a single line from the incoming data file.
     * 
     * Any special adjustments required to convert from data file Strings to the appropriate
     * attribute data types for the wrapper class objects will start with this method.
     * 
     */
    protected TravelMealCardFileLineDataWrapper generateTmCardWrapperPair(TravelMealCardFileLineEntry tmCardFileLine) {
        KualiDecimal creditLine = cardServicesUtilityService.generateKualiDecimal(tmCardFileLine.getCreditLine());
        
        Date fileCreateDate = cardServicesUtilityService.convertCardDateToSqlDate(tmCardFileLine.getFileCreateDate());
        
        Date loadDate = (tmCardFileLine.getLoadDate() == null || tmCardFileLine.getLoadDate().isBlank()) 
                ? dateTimeService.getCurrentSqlDateMidnight()
                : cardServicesUtilityService.convertCardDateToSqlDate(tmCardFileLine.getLoadDate());
        
        TravelMealCardVerificationData verifyData = new TravelMealCardVerificationData();
        verifyData.setCardHolderAccountNumber(tmCardFileLine.getCardHolderAccountNumber());
        verifyData.setEmplid(tmCardFileLine.getEmplid());
        verifyData.setCardHolderLine1Address(tmCardFileLine.getCardHolderLine1Address());
        verifyData.setCardHolderLine2Address(tmCardFileLine.getCardHolderLine2Address());
        verifyData.setCardHolderCityName(tmCardFileLine.getCardHolderCityName());
        verifyData.setCardHolderStateCode(tmCardFileLine.getCardHolderStateCode());
        verifyData.setCardHolderPostalCode(tmCardFileLine.getCardHolderPostalCode());
        verifyData.setCardHolderWorkPhoneNumber(tmCardFileLine.getCardHolderWorkPhoneNumber());
        verifyData.setCardHolderPersonalPhoneNumber(tmCardFileLine.getCardHolderPersonalPhoneNumber());
        verifyData.setCreditLine(creditLine);
        verifyData.setExpireDate(cardServicesUtilityService.convertCardDateToSqlDate(tmCardFileLine.getExpireDate()));
        verifyData.setFileCreateDate(fileCreateDate);
        verifyData.setLoadDate(loadDate);
        
        TravelMealCardCertificationData certifyData = new TravelMealCardCertificationData();
        certifyData.setCardHolderAccountNumber(tmCardFileLine.getCardHolderAccountNumber());
        certifyData.setEmplid(tmCardFileLine.getEmplid());
        certifyData.setCardHolderName(tmCardFileLine.getCardHolderName());
        certifyData.setCardStatus(tmCardFileLine.getCardStatus());
        certifyData.setCardType(tmCardFileLine.getCardType());
        certifyData.setNetid(tmCardFileLine.getNetid());
        certifyData.setLastTransactionDate(cardServicesUtilityService.convertCardDateToSqlDate(tmCardFileLine.getLastTransactionDate()));
        certifyData.setDefaultAccountNumber(tmCardFileLine.getDefaultAccountNumber());
        certifyData.setOpenDate(cardServicesUtilityService.convertCardDateToSqlDate(tmCardFileLine.getOpenDate()));
        certifyData.setCycleStartDate(cardServicesUtilityService.convertCardDateToSqlDate(tmCardFileLine.getCycleStartDate()));
        certifyData.setCycleSpendToDate(cardServicesUtilityService.generateKualiDecimal(tmCardFileLine.getCycleSpendToDate()));
        certifyData.setActivationStatus(tmCardFileLine.getActivationStatus());
        certifyData.setFileCreateDate(fileCreateDate);
        certifyData.setLoadDate(loadDate);
        
        return new TravelMealCardFileLineDataWrapper(verifyData, certifyData);
    }
    
    /**
     * Load object pairs into CU_FP_TMCARD_VERIFY_T and CU_FP_TMCARD_CERTIFY_T that represent a single line from the file.
     */
    @Transactional
    protected void loadTmCardObjectPairsIntoDatabase(List<TravelMealCardFileLineDataWrapper> dataClassPairsToLoadToDatabase) {
        businessObjectService.deleteMatching(TravelMealCardVerificationData.class, new HashMap<String, String>());
        businessObjectService.deleteMatching(TravelMealCardCertificationData.class, new HashMap<String, String>());
        
        for (TravelMealCardFileLineDataWrapper wrapperPair : dataClassPairsToLoadToDatabase) {
            TravelMealCardVerificationData retrievedVerifyDataRecord = (TravelMealCardVerificationData) businessObjectService.retrieve(wrapperPair.getTravelMealCardVerificationData());
            TravelMealCardCertificationData retrievedCertifyDataRecord = (TravelMealCardCertificationData) businessObjectService.retrieve(wrapperPair.getTravelMealCardCertificationData());
            
            try {
                businessObjectService.save(wrapperPair.getTravelMealCardVerificationData());
            } catch (RuntimeException e) {
                LOG.error("loadTmCardObjectPairsIntoDatabase could not save TravelMealCardVerificationData to database. " + e.getMessage(), e);
                logClassLoaderDebugInfo(wrapperPair.getTravelMealCardVerificationData(), retrievedVerifyDataRecord);
                throw e;
            }
            
            try {
                businessObjectService.save(wrapperPair.getTravelMealCardCertificationData());
            } catch (RuntimeException e) {
                LOG.error("loadTmCardObjectPairsIntoDatabase could not save TravelMealCardCertificationData record to database. " + e.getMessage(), e);
                logClassLoaderDebugInfo(retrievedCertifyDataRecord, wrapperPair.getTravelMealCardCertificationData());
                throw e;
            }
        }
    }
    
    private void logClassLoaderDebugInfo(TravelMealCardVerificationData tryingToSaveVerifyRecord, TravelMealCardVerificationData retrievedVerifyRecord) {
        LOG.info("logClassLoaderDebugInfo:: OJB Broker ClassHelper ClassLoader: {}", org.apache.ojb.broker.util.ClassHelper.getClassLoader());
        
        Class repoClass = org.apache.ojb.broker.metadata.ClassDescriptor.class;
        LOG.info("logClassLoaderDebugInfo:: Repository Class Loader: {}", repoClass.getClassLoader());
        
        Class travelMealCardVerificationDataClass = edu.cornell.kfs.fp.businessobject.TravelMealCardVerificationData.class;
        LOG.info("logClassLoaderDebugInfo:: ProcurementCardSummaryEntry Class Loader: {} ", travelMealCardVerificationDataClass.getClassLoader());
        
        LOG.info("logClassLoaderDebugInfo:: ProcurementCardSummaryEntry Class Loader: entry = {}", (ObjectUtils.isNull(tryingToSaveVerifyRecord) ? "IS NULL" : tryingToSaveVerifyRecord.toString()));
        
        LOG.info("logClassLoaderDebugInfo:: ProcurementCardSummaryEntry Class Loader: retrievedEntry = {}", (ObjectUtils.isNull(retrievedVerifyRecord) ? "IS NULL" : retrievedVerifyRecord.toString()));
    }
    
    private void logClassLoaderDebugInfo(TravelMealCardCertificationData tryingToSaveCertifyRecord, TravelMealCardCertificationData retrievedCertifyRecord) {
        LOG.info("logClassLoaderDebugInfo:: OJB Broker ClassHelper ClassLoader: {}", org.apache.ojb.broker.util.ClassHelper.getClassLoader());
        
        Class repoClass = org.apache.ojb.broker.metadata.ClassDescriptor.class;
        LOG.info("logClassLoaderDebugInfo:: Repository Class Loader: {}", repoClass.getClassLoader());
        
        Class travelMealCardCertificationDataClass = edu.cornell.kfs.fp.businessobject.TravelMealCardCertificationData.class;
        LOG.info("logClassLoaderDebugInfo:: ProcurementCardSummaryEntry Class Loader: {} ", travelMealCardCertificationDataClass.getClassLoader());
        
        LOG.info("logClassLoaderDebugInfo:: ProcurementCardSummaryEntry Class Loader: entry = {}", (ObjectUtils.isNull(tryingToSaveCertifyRecord) ? "IS NULL" : tryingToSaveCertifyRecord.toString()));
        
        LOG.info("logClassLoaderDebugInfo:: ProcurementCardSummaryEntry Class Loader: retrievedEntry = {}", (ObjectUtils.isNull(retrievedCertifyRecord) ? "IS NULL" : retrievedCertifyRecord.toString()));
    }
    
    public void sendNotificationFileNotReceived() {
        travelMealCardEmailService.sendErrorEmail(travelMealCardEmailService.getFileNotReceivedRecipentEmailAddress(),
                travelMealCardEmailService.generateNewFileNotReceivedSubject(),
                travelMealCardEmailService.generateNewFileNotReceivedMessage());
    }

    public BatchInputFileService getBatchInputFileService() {
        return batchInputFileService;
    }
    
    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
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

    public TravelMealCardFlatInputFileType getTravelMealCardFlatInputFileType() {
        return travelMealCardFlatInputFileType;
    }

    public void setTravelMealCardFlatInputFileType(TravelMealCardFlatInputFileType travelMealCardFlatInputFileType) {
        this.travelMealCardFlatInputFileType = travelMealCardFlatInputFileType;
    }

    public CardServicesUtilityService getCardServicesUtilityService() {
        return cardServicesUtilityService;
    }

    public void setCardServicesUtilityService(CardServicesUtilityService cardServicesUtilityService) {
        this.cardServicesUtilityService = cardServicesUtilityService;
    }

    public TravelMealCardEmailService getTravelMealCardEmailService() {
        return travelMealCardEmailService;
    }

    public void setTravelMealCardEmailService(TravelMealCardEmailService travelMealCardEmailService) {
        this.travelMealCardEmailService = travelMealCardEmailService;
    }

}
