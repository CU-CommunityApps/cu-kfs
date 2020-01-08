package edu.cornell.kfs.coa.batch.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.datetime.DateTimeService;

import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvException;

import edu.cornell.kfs.coa.batch.CuCoaBatchConstants;
import edu.cornell.kfs.coa.batch.CuCoaBatchParameterConstants;
import edu.cornell.kfs.coa.batch.businessobject.LaborClosedAccount;
import edu.cornell.kfs.coa.batch.dataaccess.ClosedLaborAccountsByDateRangeDao;
import edu.cornell.kfs.coa.batch.service.CreateClosedLaborAccountsCsvService;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

public class CreateClosedLaborAccountsCsvServiceImpl implements CreateClosedLaborAccountsCsvService {
    private static final Logger LOG = LogManager.getLogger(CreateClosedLaborAccountsCsvServiceImpl.class);
    protected ClosedLaborAccountsByDateRangeDao closedLaborAccountsByDateRangeDao;
    protected DateTimeService dateTimeService;
    protected ParameterService parameterService;
    protected String csvLaborClosedAccountsExportDirectory;
    protected Date seedFileFromDate;
    
    @Override
    public void createClosedLaborAccountCsvByParameterPastDays() throws IOException {
        Map<String, Integer> typeOfClosedAccountFileToCreate = getParameterValueForTypeOfClosedAccountFileToCreate();
        
        if (parameterValueForTypeOfClosedAccountFileToCreateIsSet(typeOfClosedAccountFileToCreate) && laborClosedAccountsSeedFileDefaultFromDateParameterIsValid()) {
            Map<String, Date> fileDataRangeSpecified = getClosedLaborAccountsDateRange(typeOfClosedAccountFileToCreate);
            writeClosedLaborAccountsToCsvFormattedFile(fileDataRangeSpecified);
        } else {
            LOG.error("createClosedLaborAccountCsvByParameterPastDays: NO FILE CREATED. REQUIRED PARAMETERS ARE NOT SET.");
        }
    }
    
    private Map<String, Integer> getParameterValueForTypeOfClosedAccountFileToCreate() {
        Map<String, Integer> fileDataContentType = new HashMap<String, Integer>();
        Integer numberOfDaysPrevious = getLaborAccoutsClosedPastDaysParameter();
        
        if (numberOfDaysPrevious.intValue() == CuCoaBatchConstants.ClosedLaborAccountsFileCreationConstants.PARAMETER_LABOR_ACCOUNTS_CLOSED_OVER_PAST_DAYS_NOT_SET) {
            fileDataContentType.put(CuCoaBatchConstants.ClosedLaborAccountsFileCreationConstants.FILE_DATA_CONTENT_TYPE_IS, CuCoaBatchConstants.ClosedLaborAccountsFileCreationConstants.FILE_DATA_CONTENT_TYPES.NO_PARMETER_FOUND);
            LOG.info("parameterValueForTypeOfClosedAccountFileToCreate: Detected parameter LABOR_ACCOUNTS_CLOSED_OVER_PAST_DAYS required for batch job execution is not set.");
            
        } else if (numberOfDaysPrevious.intValue() == CuCoaBatchConstants.ClosedLaborAccountsFileCreationConstants.PARAMETER_SET_TO_CREATE_FULL_SEED_FILE) {
            fileDataContentType.put(CuCoaBatchConstants.ClosedLaborAccountsFileCreationConstants.FILE_DATA_CONTENT_TYPE_IS, CuCoaBatchConstants.ClosedLaborAccountsFileCreationConstants.FILE_DATA_CONTENT_TYPES.SEED);
            LOG.info("parameterValueForTypeOfClosedAccountFileToCreate: Full seed file will be created.");
        } else {
            fileDataContentType.put(CuCoaBatchConstants.ClosedLaborAccountsFileCreationConstants.FILE_DATA_CONTENT_TYPE_IS, CuCoaBatchConstants.ClosedLaborAccountsFileCreationConstants.FILE_DATA_CONTENT_TYPES.RANGE);
            fileDataContentType.put(CuCoaBatchConstants.ClosedLaborAccountsFileCreationConstants.FROM_DATE, numberOfDaysPrevious.intValue());
            LOG.info("parameterValueForTypeOfClosedAccountFileToCreate: File will contain data for the previous " + numberOfDaysPrevious.intValue() +  " days.");
        }
        
        return fileDataContentType;
    }
    
    private Integer getLaborAccoutsClosedPastDaysParameter() {
        String daysPreviousParameterValue = getParameterService().getParameterValueAsString(KFSConstants.CoreModuleNamespaces.CHART, 
                CUKFSParameterKeyConstants.ALL_COMPONENTS, CuCoaBatchParameterConstants.LABOR_ACCOUNTS_CLOSED_OVER_PAST_DAYS);
        
        int daysPreviousInt;
        try {
            daysPreviousInt = Integer.parseInt(daysPreviousParameterValue);
        } catch (NumberFormatException ne) {
            LOG.error("getLaborAccoutsClosedPastDaysParameter: Namespace KFS-COA system parameter LABOR_ACCOUNTS_CLOSED_OVER_PAST_DAYS obtained was " 
                    + daysPreviousParameterValue + " so it was either not set or could not be converted to a number.");
            daysPreviousInt = CuCoaBatchConstants.ClosedLaborAccountsFileCreationConstants.PARAMETER_LABOR_ACCOUNTS_CLOSED_OVER_PAST_DAYS_NOT_SET;
        }
        return new Integer(daysPreviousInt);
    }
    
    private boolean laborClosedAccountsSeedFileDefaultFromDateParameterIsValid() {
        String defaultFromDateParameterValue = getParameterService().getParameterValueAsString(KFSConstants.CoreModuleNamespaces.CHART, 
                CUKFSParameterKeyConstants.ALL_COMPONENTS, CuCoaBatchParameterConstants.LABOR_CLOSED_ACCOUNTS_SEED_FILE_DEFAULT_FROM_DATE);
        
        Date defaultFromDate;
        try {
            defaultFromDate = Date.valueOf(defaultFromDateParameterValue);
        } catch (IllegalArgumentException ie) {
            LOG.error("getLaborClosedAccountsSeedFileDefaultFromDateParameter: Namespace KFS-COA system parameter LABOR_CLOSED_ACCOUNTS_SEED_FILE_DEFAULT_FROM_DATE was "
                    + defaultFromDateParameterValue + " so it was either not set or could not be converted to a SQL Date.");
            setSeedFileFromDate(null);
            return false;
        }
        setSeedFileFromDate(defaultFromDate);
        return true;
    }
    
    private boolean parameterValueForTypeOfClosedAccountFileToCreateIsSet(Map<String, Integer> typeOfClosedAccountFileToCreate) {
        if (typeOfClosedAccountFileToCreate.isEmpty() ||
                typeOfClosedAccountFileToCreate.containsValue(CuCoaBatchConstants.ClosedLaborAccountsFileCreationConstants.FILE_DATA_CONTENT_TYPES.NO_PARMETER_FOUND)) {
            return false;
        } else {
            return true;
        }
    }
    
    private Map<String, Date> getClosedLaborAccountsDateRange(Map<String, Integer> typeOfClosedAccountFileToCreate) {
        Map<String, Date> fromToRange = new HashMap<String, Date>();
        
        if (typeOfClosedAccountFileToCreate.containsValue(CuCoaBatchConstants.ClosedLaborAccountsFileCreationConstants.FILE_DATA_CONTENT_TYPES.SEED)) {
            fromToRange.put(CuCoaBatchConstants.ClosedLaborAccountsFileCreationConstants.FROM_DATE, getSeedFileFromDate());
            fromToRange.put(CuCoaBatchConstants.ClosedLaborAccountsFileCreationConstants.TO_DATE, getDateTimeService().getCurrentSqlDate());
            LOG.info("getClosedLaborAccountsDateRange: Seed file will have FROM_DATE = " + fromToRange.get(CuCoaBatchConstants.ClosedLaborAccountsFileCreationConstants.FROM_DATE) 
                + " and a TO_DATE = " + fromToRange.get(CuCoaBatchConstants.ClosedLaborAccountsFileCreationConstants.TO_DATE)); 
            
        } else if (typeOfClosedAccountFileToCreate.containsValue(CuCoaBatchConstants.ClosedLaborAccountsFileCreationConstants.FILE_DATA_CONTENT_TYPES.RANGE)) {
            Date sqlComputedFromDate = getJavaSqlDateForComputedDate(typeOfClosedAccountFileToCreate.get(CuCoaBatchConstants.ClosedLaborAccountsFileCreationConstants.FROM_DATE));
            if (sqlComputedFromDate != null) {
                fromToRange.put(CuCoaBatchConstants.ClosedLaborAccountsFileCreationConstants.FROM_DATE, sqlComputedFromDate);
                fromToRange.put(CuCoaBatchConstants.ClosedLaborAccountsFileCreationConstants.TO_DATE, getDateTimeService().getCurrentSqlDate());
                LOG.info("getClosedLaborAccountsDateRange: Periodic file will have FROM_DATE = " + fromToRange.get(CuCoaBatchConstants.ClosedLaborAccountsFileCreationConstants.FROM_DATE) 
                    + " and a TO_DATE = " + fromToRange.get(CuCoaBatchConstants.ClosedLaborAccountsFileCreationConstants.TO_DATE)); 
            } else {
                LOG.info("getClosedLaborAccountsDateRange: Periodic file will NOT be created.");
            }
            
        } else {
            LOG.error("getClosedLaborAccountsDateRange: Unknown Type of Closed Account File to Create = " + typeOfClosedAccountFileToCreate.get(CuCoaBatchConstants.ClosedLaborAccountsFileCreationConstants.FILE_DATA_CONTENT_TYPE_IS));
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
    
    private void writeClosedLaborAccountsToCsvFormattedFile(Map<String, Date> fileDataRangeSpecified) throws IOException {
        LOG.info("writeClosedLaborAccountsToCsvFormattedFile: Prior to obtaining data with SQL query for date range. This may take a while.");
        List<LaborClosedAccount> laborClosedAccountsData = getClosedLaborAccountsByDateRangeDao().obtainLaborClosedAccountsDataFor(fileDataRangeSpecified);
        
        if (ObjectUtils.isNotNull(laborClosedAccountsData) && laborClosedAccountsData.size() > 0) {
            LOG.info("writeClosedLaborAccountsToCsvFormattedFile: Have the data. Will now write out " + laborClosedAccountsData.size() + " closed accounts to CSV file.");
            writeClosedAccountsToCsvFile(laborClosedAccountsData);
        } else {
            LOG.info("writeClosedLaborAccountsToCsvFormattedFile: NO CSV file being created. " 
                    + (ObjectUtils.isNotNull(laborClosedAccountsData) ? (laborClosedAccountsData.size() + " accounts being written to file.") : ("null returned for requested account data.") ));
        }
    }
    
    private void writeClosedAccountsToCsvFile(List<LaborClosedAccount> laborClosedAccountsDataList) throws IOException {
        String fullyQualifiedOutputFileName = generateFullyQualifiedOutputFileName();
        LOG.info("writeClosedAccountsToCsvFile: fullyQualifiedOutputFile = " + fullyQualifiedOutputFileName);
        File outputFile = new File(fullyQualifiedOutputFileName);
        FileWriter outputFileWriter;
        try {
            outputFileWriter = new FileWriter(outputFile);
            for (LaborClosedAccount closedAccount : laborClosedAccountsDataList) {
                outputFileWriter.write(closedAccount.toCsvString());
                outputFileWriter.write(KFSConstants.NEWLINE);
            }
            outputFileWriter.close();
        } catch (IOException io) {
            LOG.error("writeClosedAccountsToCsvFile: Caught IOException => ", io);
            throw io;
        }
    }
    
    private String generateFullyQualifiedOutputFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        StringBuilder filename = new StringBuilder(getCsvLaborClosedAccountsExportDirectory());
        filename.append(CuCoaBatchConstants.ClosedLaborAccountsFileCreationConstants.OUTPUT_FILE_NAME).append("_");
        filename.append(sdf.format(getDateTimeService().getCurrentDate())).append(CUKFSConstants.FILE_EXTENSIONS.CSV_FILE_EXTENSION);
        return filename.toString();
    }
    
    public ClosedLaborAccountsByDateRangeDao getClosedLaborAccountsByDateRangeDao() {
        return closedLaborAccountsByDateRangeDao;
    }

    public void setClosedLaborAccountsByDateRangeDao(ClosedLaborAccountsByDateRangeDao closedLaborAccountsByDateRangeDao) {
        this.closedLaborAccountsByDateRangeDao = closedLaborAccountsByDateRangeDao;
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

    public String getCsvLaborClosedAccountsExportDirectory() {
        return csvLaborClosedAccountsExportDirectory;
    }

    public void setCsvLaborClosedAccountsExportDirectory(String csvLaborClosedAccountsExportDirectory) {
        this.csvLaborClosedAccountsExportDirectory = csvLaborClosedAccountsExportDirectory;
    }

    public Date getSeedFileFromDate() {
        return seedFileFromDate;
    }

    public void setSeedFileFromDate(Date seedFileFromDate) {
        this.seedFileFromDate = seedFileFromDate;
    }

}
