package edu.cornell.kfs.coa.batch.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.core.api.datetime.DateTimeService;

import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvException;

import edu.cornell.kfs.coa.batch.CuCoaBatchConstants;
import edu.cornell.kfs.coa.batch.CuCoaBatchParameterConstants;
import edu.cornell.kfs.coa.batch.businessobject.ClosedAccount;
import edu.cornell.kfs.coa.batch.dataaccess.ClosedAccountsByDateRangeDao;
import edu.cornell.kfs.coa.batch.service.CreateClosedAccountsCsvService;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

public class CreateClosedAccountsCsvServiceImpl implements CreateClosedAccountsCsvService {
    private static final Logger LOG = LogManager.getLogger(CreateClosedAccountsCsvServiceImpl.class);
    protected ClosedAccountsByDateRangeDao closedAccountsByDateRangeDao;
    protected DateTimeService dateTimeService;
    protected ParameterService parameterService;
    protected String csvClosedAccountsExportDirectory;
    protected String csvClosedAccountstFileCreationDirectory;
    protected Date seedFileFromDate;
    
    @Override
    public void createClosedAccountsCsvByParameterPastDays() throws IOException {
        Map<String, Integer> typeOfClosedAccountFileToCreate = getParameterValueForTypeOfClosedAccountFileToCreate();
        
        if (parameterValueForTypeOfClosedAccountFileToCreateIsSet(typeOfClosedAccountFileToCreate) && closedAccountsSeedFileDefaultFromDateParameterIsValid()) {
            Map<String, Date> fileDataRangeSpecified = getClosedAccountsDateRange(typeOfClosedAccountFileToCreate);
            writeClosedAccountsToCsvFormattedFile(fileDataRangeSpecified);
        } else {
            LOG.error("createClosedAccountsCsvByParameterPastDays: NO FILE CREATED. REQUIRED PARAMETERS ARE NOT SET.");
        }
    }
    
    private Map<String, Integer> getParameterValueForTypeOfClosedAccountFileToCreate() {
        Map<String, Integer> fileDataContentType = new HashMap<String, Integer>();
        Integer numberOfDaysPrevious = getAccountsClosedPastDaysParameter();
        
        if (numberOfDaysPrevious.intValue() == CuCoaBatchConstants.ClosedAccountsFileCreationConstants.PARAMETER_ACCOUNTS_CLOSED_OVER_PAST_DAYS_NOT_SET) {
            fileDataContentType.put(CuCoaBatchConstants.ClosedAccountsFileCreationConstants.FILE_DATA_CONTENT_TYPE_IS, CuCoaBatchConstants.ClosedAccountsFileCreationConstants.FILE_DATA_CONTENT_TYPES.NO_PARAMETER_FOUND);
            LOG.info("getParameterValueForTypeOfClosedAccountFileToCreate: Detected parameter ACCOUNTS_CLOSED_OVER_PAST_DAYS required for batch job execution is not set.");
            
        } else if (numberOfDaysPrevious.intValue() == CuCoaBatchConstants.ClosedAccountsFileCreationConstants.PARAMETER_SET_TO_CREATE_FULL_SEED_FILE) {
            fileDataContentType.put(CuCoaBatchConstants.ClosedAccountsFileCreationConstants.FILE_DATA_CONTENT_TYPE_IS, CuCoaBatchConstants.ClosedAccountsFileCreationConstants.FILE_DATA_CONTENT_TYPES.SEED);
            LOG.info("getParameterValueForTypeOfClosedAccountFileToCreate: Full seed file will be created.");
        } else {
            fileDataContentType.put(CuCoaBatchConstants.ClosedAccountsFileCreationConstants.FILE_DATA_CONTENT_TYPE_IS, CuCoaBatchConstants.ClosedAccountsFileCreationConstants.FILE_DATA_CONTENT_TYPES.RANGE);
            fileDataContentType.put(CuCoaBatchConstants.ClosedAccountsFileCreationConstants.FROM_DATE, numberOfDaysPrevious.intValue());
            LOG.info("getParameterValueForTypeOfClosedAccountFileToCreate: File will contain data for the previous " + numberOfDaysPrevious.intValue() +  " days.");
        }
        
        return fileDataContentType;
    }
    
    private Integer getAccountsClosedPastDaysParameter() {
        String daysPreviousParameterValue = getParameterService().getParameterValueAsString(KFSConstants.CoreModuleNamespaces.CHART, 
                CUKFSParameterKeyConstants.ALL_COMPONENTS, CuCoaBatchParameterConstants.ACCOUNTS_CLOSED_OVER_PAST_DAYS);
        
        int daysPreviousInt;
        try {
            daysPreviousInt = Integer.parseInt(daysPreviousParameterValue);
        } catch (NumberFormatException ne) {
            LOG.error("getAccountsClosedPastDaysParameter: Namespace KFS-COA system parameter ACCOUNTS_CLOSED_OVER_PAST_DAYS obtained was " 
                    + daysPreviousParameterValue + " so it was either not set or could not be converted to a number.");
            daysPreviousInt = CuCoaBatchConstants.ClosedAccountsFileCreationConstants.PARAMETER_ACCOUNTS_CLOSED_OVER_PAST_DAYS_NOT_SET;
        }
        return new Integer(daysPreviousInt);
    }
    
    private boolean closedAccountsSeedFileDefaultFromDateParameterIsValid() {
        String defaultFromDateParameterValue = getParameterService().getParameterValueAsString(KFSConstants.CoreModuleNamespaces.CHART, 
                CUKFSParameterKeyConstants.ALL_COMPONENTS, CuCoaBatchParameterConstants.CLOSED_ACCOUNTS_SEED_FILE_DEFAULT_FROM_DATE);
        
        Date defaultFromDate;
        try {
            defaultFromDate = Date.valueOf(defaultFromDateParameterValue);
        } catch (IllegalArgumentException ie) {
            LOG.error("closedAccountsSeedFileDefaultFromDateParameterIsValid: Namespace KFS-COA system parameter CLOSED_ACCOUNTS_SEED_FILE_DEFAULT_FROM_DATE was "
                    + defaultFromDateParameterValue + " so it was either not set or could not be converted to a SQL Date.");
            setSeedFileFromDate(null);
            return false;
        }
        setSeedFileFromDate(defaultFromDate);
        return true;
    }
    
    private boolean parameterValueForTypeOfClosedAccountFileToCreateIsSet(Map<String, Integer> typeOfClosedAccountFileToCreate) {
        return !(typeOfClosedAccountFileToCreate.isEmpty() ||
                 typeOfClosedAccountFileToCreate.containsValue(CuCoaBatchConstants.ClosedAccountsFileCreationConstants.FILE_DATA_CONTENT_TYPES.NO_PARAMETER_FOUND));
    }
    
    private Map<String, Date> getClosedAccountsDateRange(Map<String, Integer> typeOfClosedAccountFileToCreate) {
        Map<String, Date> fromToRange = new HashMap<String, Date>();
        
        if (typeOfClosedAccountFileToCreate.containsValue(CuCoaBatchConstants.ClosedAccountsFileCreationConstants.FILE_DATA_CONTENT_TYPES.SEED)) {
            fromToRange.put(CuCoaBatchConstants.ClosedAccountsFileCreationConstants.FROM_DATE, getSeedFileFromDate());
            fromToRange.put(CuCoaBatchConstants.ClosedAccountsFileCreationConstants.TO_DATE, getDateTimeService().getCurrentSqlDate());
            LOG.info("getClosedAccountsDateRange: Seed file will have FROM_DATE = " + fromToRange.get(CuCoaBatchConstants.ClosedAccountsFileCreationConstants.FROM_DATE) 
                + " and a TO_DATE = " + fromToRange.get(CuCoaBatchConstants.ClosedAccountsFileCreationConstants.TO_DATE)); 
            
        } else if (typeOfClosedAccountFileToCreate.containsValue(CuCoaBatchConstants.ClosedAccountsFileCreationConstants.FILE_DATA_CONTENT_TYPES.RANGE)) {
            Date sqlComputedFromDate = getJavaSqlDateForComputedDate(typeOfClosedAccountFileToCreate.get(CuCoaBatchConstants.ClosedAccountsFileCreationConstants.FROM_DATE));
            if (sqlComputedFromDate != null) {
                fromToRange.put(CuCoaBatchConstants.ClosedAccountsFileCreationConstants.FROM_DATE, sqlComputedFromDate);
                fromToRange.put(CuCoaBatchConstants.ClosedAccountsFileCreationConstants.TO_DATE, getDateTimeService().getCurrentSqlDate());
                LOG.info("getClosedAccountsDateRange: Periodic file will have FROM_DATE = " + fromToRange.get(CuCoaBatchConstants.ClosedAccountsFileCreationConstants.FROM_DATE) 
                    + " and a TO_DATE = " + fromToRange.get(CuCoaBatchConstants.ClosedAccountsFileCreationConstants.TO_DATE)); 
            } else {
                LOG.info("getClosedAccountsDateRange: Periodic file will NOT be created.");
            }
            
        } else {
            LOG.error("getClosedAccountsDateRange: Unknown Type of Closed Account File to Create = " + typeOfClosedAccountFileToCreate.get(CuCoaBatchConstants.ClosedAccountsFileCreationConstants.FILE_DATA_CONTENT_TYPE_IS));
        }
        return fromToRange;
    }
    
    private Date getJavaSqlDateForComputedDate(int daysPrevious) {
        LocalDate computedFromDate = computeDateBeingDaysPreviousFromToday(daysPrevious);
        java.sql.Date sqlDate;
        try {
            sqlDate = getDateTimeService().convertToSqlDate(computedFromDate.toString());
        } catch (ParseException pe) {
            LOG.error("getJavaSqlDateForComputedDate: ParseException caught for computedFromDate = " + computedFromDate.toString());
            sqlDate = null;
        }
        return sqlDate;
    }
    
    private LocalDate computeDateBeingDaysPreviousFromToday(int daysPrevious) {
        LOG.info("computeDateBeingDaysPreviousFromToday: daysPrevious = " + daysPrevious);
        DateTime todayAsDateTime = new DateTime(getDateTimeService().getCurrentDate());
        DateTime datePreviousAsDateTime = todayAsDateTime.minusDays(daysPrevious);
        LOG.info("computeDateBeingDaysPreviousFromToday: datePreviousAsDateTime = " + datePreviousAsDateTime.toLocalDate().toString());
        return datePreviousAsDateTime.toLocalDate();
    }
    
    private void writeClosedAccountsToCsvFormattedFile(Map<String, Date> fileDataRangeSpecified) throws IOException {
        LOG.info("writeClosedAccountsToCsvFormattedFile: Prior to ensuring directories to hold outbound file exist.");
        ensureOutbundDirectoriesExist();
        
        LOG.info("writeClosedAccountsToCsvFormattedFile: Prior to obtaining data with SQL query for date range. This may take a while.");
        List<ClosedAccount> closedAccountsData = getClosedAccountsByDateRangeDao().obtainClosedAccountsDataFor(fileDataRangeSpecified);
        
        if (ObjectUtils.isNotNull(closedAccountsData) && closedAccountsData.size() > 0) {
            LOG.info("writeClosedAccountsToCsvFormattedFile: Have the data. Will now write out " + closedAccountsData.size() + " closed accounts to CSV file.");
            writeClosedAccountsToCsvFile(closedAccountsData);
        } else {
            LOG.info("writeClosedAccountsToCsvFormattedFile: NO CSV file being created. " 
                    + (ObjectUtils.isNotNull(closedAccountsData) ? (closedAccountsData.size() + " accounts being written to file.") : ("null returned for requested account data.") ));
        }
    }
    
    private void ensureOutbundDirectoriesExist() {
        try {
            FileUtils.forceMkdir(new File(getCsvClosedAccountstFileCreationDirectory()));
        } catch (IOException e) {
            LOG.info("ensureOutbundDirectoriesExist: Could not make file creation directory => " + getCsvClosedAccountstFileCreationDirectory() + " Throwing RuntimeException to force batch job failure.");
            throw new RuntimeException(e);
        }
        try {
            FileUtils.forceMkdir(new File(getCsvClosedAccountsExportDirectory()));
        } catch (IOException e) {
            LOG.info("ensureOutbundDirectoriesExist: Could not make file export directory => " + getCsvClosedAccountsExportDirectory() + " Throwing RuntimeException to force batch job failure.");
            throw new RuntimeException(e);
        }
    }
    
    private void writeClosedAccountsToCsvFile(List<ClosedAccount> closedAccountsDataList) throws IOException {
        String csvFileName = generateCsvOutputFileName();
        String fullyQualifiedCreationDirectoryFileName = fullyQualifyFileNameToCreationDirectory(csvFileName);
        LOG.info("writeClosedAccountsToCsvFile: fullyQualifiedOutputFile = " + fullyQualifiedCreationDirectoryFileName);
        File outputFile = new File(fullyQualifiedCreationDirectoryFileName);
        FileWriter outputFileWriter;
        outputFileWriter = new FileWriter(outputFile);
        try {
            for (ClosedAccount closedAccount : closedAccountsDataList) {
                outputFileWriter.write(closedAccount.toCsvString());
                outputFileWriter.write(KFSConstants.NEWLINE);
            }
            outputFileWriter.flush();
            LOG.info("writeClosedAccountsToCsvFile: CSV file in being-written directory has all the data, was flushed, and file will attempt to now be closed.");
        } catch (IOException io) {
            LOG.error("writeClosedAccountsToCsvFile: Caught IOException attempting to create CSV file of closded accounts => ", io);
            throw io;
        } finally {
            IOUtils.closeQuietly(outputFileWriter);
        }
        try {
            String fullyQualifiedExportDirectoryFileName = fullyQualifyFileNameToExportDirectory(csvFileName);
            LOG.info("writeClosedAccountsToCsvFile: Moving data file from creation directory to fullyQualifiedExportDirectoryFileName = " + fullyQualifiedExportDirectoryFileName);
            moveCreatedFileToExportDirectory(outputFile, fullyQualifiedExportDirectoryFileName);
            LOG.info("writeClosedAccountsToCsvFile: File was successfully moved to export directory.");
        } catch (IOException io2) {
            LOG.error("writeClosedAccountsToCsvFile: Caught IOException attempting to move CSV file of closed accounts to export directory => ", io2);
            throw io2;
        }
    }
    
    private String generateCsvOutputFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        StringBuilder filename = new StringBuilder(CuCoaBatchConstants.ClosedAccountsFileCreationConstants.OUTPUT_FILE_NAME);
        filename.append("_").append(sdf.format(getDateTimeService().getCurrentDate())).append(CUKFSConstants.FileExtensions.CSV);
        return filename.toString();
    }
    
    private String fullyQualifyFileNameToCreationDirectory(String csvFileName) {
        StringBuilder filename = new StringBuilder(getCsvClosedAccountstFileCreationDirectory());
        filename.append(csvFileName);
        return filename.toString();
    }
    
    private String fullyQualifyFileNameToExportDirectory(String csvFileName) {
        StringBuilder filename = new StringBuilder(getCsvClosedAccountsExportDirectory());
        filename.append(csvFileName);
        return filename.toString();
    }
    
    private void moveCreatedFileToExportDirectory (File outputFile, String fullyQualifiedTargetFile) throws IOException {
        Path sourceFilePath = outputFile.toPath();
        File exportFile = new File(fullyQualifiedTargetFile);
        Path exportFilePath = exportFile.toPath();
        java.nio.file.Files.move(sourceFilePath, exportFilePath, StandardCopyOption.ATOMIC_MOVE);
    }
    
    public ClosedAccountsByDateRangeDao getClosedAccountsByDateRangeDao() {
        return closedAccountsByDateRangeDao;
    }

    public void setClosedAccountsByDateRangeDao(ClosedAccountsByDateRangeDao closedAccountsByDateRangeDao) {
        this.closedAccountsByDateRangeDao = closedAccountsByDateRangeDao;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public String getCsvClosedAccountsExportDirectory() {
        return csvClosedAccountsExportDirectory;
    }

    public void setCsvClosedAccountsExportDirectory(String csvClosedAccountsExportDirectory) {
        this.csvClosedAccountsExportDirectory = csvClosedAccountsExportDirectory;
    }

    public String getCsvClosedAccountstFileCreationDirectory() {
        return csvClosedAccountstFileCreationDirectory;
    }

    public void setCsvClosedAccountstFileCreationDirectory(String csvClosedAccountstFileCreationDirectory) {
        this.csvClosedAccountstFileCreationDirectory = csvClosedAccountstFileCreationDirectory;
    }

    public Date getSeedFileFromDate() {
        return seedFileFromDate;
    }

    public void setSeedFileFromDate(Date seedFileFromDate) {
        this.seedFileFromDate = seedFileFromDate;
    }

}
