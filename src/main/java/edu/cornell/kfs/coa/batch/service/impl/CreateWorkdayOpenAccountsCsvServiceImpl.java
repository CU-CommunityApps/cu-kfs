package edu.cornell.kfs.coa.batch.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.datetime.DateTimeService;

import edu.cornell.kfs.coa.batch.CuCoaBatchConstants;
import edu.cornell.kfs.coa.batch.businessobject.ClosedAccount;
import edu.cornell.kfs.coa.batch.businessobject.WorkdayOpenAccountDetail;
import edu.cornell.kfs.coa.batch.dataaccess.WorkdayOpenAccountDao;
import edu.cornell.kfs.coa.batch.service.CreateWorkdayOpenAccountsCsvService;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CreateWorkdayOpenAccountsCsvServiceImpl implements CreateWorkdayOpenAccountsCsvService {
    private static final Logger LOG = LogManager.getLogger();
    
    protected String csvOpenAccountsExportDirectory;
    protected String csvOpenAccountstFileCreationDirectory;
    protected DateTimeService dateTimeService;
    protected WorkdayOpenAccountDao workdayOpenAccountDao;
    
    @Override
    public void createWorkdayOpenAccountsCsv() throws IOException {
        List<WorkdayOpenAccountDetail> details = workdayOpenAccountDao.getWorkdayOpenAccountDetail();
        if (CollectionUtils.isNotEmpty(details)) {
            LOG.info("createWorkdayOpenAccountsCsv, number of open account detail lines to write: " + details.size());
            writeOpenAccountsToCsvFile(details);
        } else {
            LOG.error("createWorkdayOpenAccountsCsv, no open accounts were found, this shouldn't happen");
            throw new RuntimeException("No open accounts were found.");
        }
        
    }
    
    private void writeOpenAccountsToCsvFile(List<WorkdayOpenAccountDetail> details) throws IOException{
        String csvFileName = generateCsvOutputFileName();
        String fullyQualifiedCreationDirectoryFileName = fullyQualifyFileNameToCreationDirectory(csvFileName);
        LOG.info("writeOpenAccountsToCsvFile: fullyQualifiedOutputFile = " + fullyQualifiedCreationDirectoryFileName);
        File outputFile = new File(fullyQualifiedCreationDirectoryFileName);
        FileWriter outputFileWriter;
        outputFileWriter = new FileWriter(outputFile);
        try {
            outputFileWriter.write(buildWorkdayOpenAccountDetailHeaderRow().toCsvString());
            outputFileWriter.write(KFSConstants.NEWLINE);
            for (WorkdayOpenAccountDetail closedAccount : details) {
                outputFileWriter.write(closedAccount.toCsvString());
                outputFileWriter.write(KFSConstants.NEWLINE);
            }
            outputFileWriter.flush();
            LOG.info("writeOpenAccountsToCsvFile: CSV file in being-written directory has all the data, was flushed, and file will attempt to now be closed.");
        } catch (IOException io) {
            LOG.error("writeOpenAccountsToCsvFile: Caught IOException attempting to create CSV file of closded accounts => ", io);
            throw io;
        } finally {
            IOUtils.closeQuietly(outputFileWriter);
        }
        try {
            String fullyQualifiedExportDirectoryFileName = fullyQualifyFileNameToExportDirectory(csvFileName);
            LOG.info("writeOpenAccountsToCsvFile: Moving data file from creation directory to fullyQualifiedExportDirectoryFileName = " + fullyQualifiedExportDirectoryFileName);
            moveCreatedFileToExportDirectory(outputFile, fullyQualifiedExportDirectoryFileName);
            LOG.info("writeClosedAccountsToCsvFile: File was successfully moved to export directory.");
        } catch (IOException io2) {
            LOG.error("writeOpenAccountsToCsvFile: Caught IOException attempting to move CSV file of closed accounts to export directory => ", io2);
            throw io2;
        }
    }
    
    private String generateCsvOutputFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        StringBuilder filename = new StringBuilder(CuCoaBatchConstants.WorkdayOpenAccountsFileCreationConstants.OUTPUT_FILE_NAME);
        filename.append("_").append(sdf.format(dateTimeService.getCurrentDate())).append(CUKFSConstants.FILE_EXTENSIONS.CSV_FILE_EXTENSION);
        return filename.toString();
    }
    
    private String fullyQualifyFileNameToCreationDirectory(String csvFileName) {
        StringBuilder filename = new StringBuilder(csvOpenAccountstFileCreationDirectory);
        filename.append(csvFileName);
        return filename.toString();
    }
    
    private String fullyQualifyFileNameToExportDirectory(String csvFileName) {
        StringBuilder filename = new StringBuilder(csvOpenAccountsExportDirectory);
        filename.append(csvFileName);
        return filename.toString();
    }
    
    private void moveCreatedFileToExportDirectory (File outputFile, String fullyQualifiedTargetFile) throws IOException {
        Path sourceFilePath = outputFile.toPath();
        File exportFile = new File(fullyQualifiedTargetFile);
        Path exportFilePath = exportFile.toPath();
        java.nio.file.Files.move(sourceFilePath, exportFilePath, StandardCopyOption.ATOMIC_MOVE);
    }
    
    private WorkdayOpenAccountDetail buildWorkdayOpenAccountDetailHeaderRow() {
        WorkdayOpenAccountDetail detail = new WorkdayOpenAccountDetail();
        detail.setHeaderDetailRow(true);
        detail.setAccountClosedIndicator("accountClosedIndicator");
        detail.setAccountEffectiveDateString("accountEffectiveDate");
        detail.setAccountName("accountName");
        detail.setAccountNumber("accountNumber");
        detail.setAccountTypeCode("accountTypeCode");
        detail.setChart("chart");
        detail.setHigherEdFunctionCode("higherEdFunctionCode");
        detail.setObjectCode("objectCode");
        detail.setSubAccountActiveIndicator("subAccountActiveIndicator");
        detail.setSubAccountName("subAccountName");
        detail.setSubAccountNumber("subAccountNumber");
        detail.setSubFundGroupCode("subFundGroupCode");
        detail.setSubFundGroupWageIndicator("subFundGroupWageIndicator");
        detail.setSubObjectCode("subObjectCode");
        detail.setSubObjectName("subObjectName");
        return detail;
    }

    public void setCsvOpenAccountsExportDirectory(String csvOpenAccountsExportDirectory) {
        this.csvOpenAccountsExportDirectory = csvOpenAccountsExportDirectory;
    }

    public void setCsvOpenAccountstFileCreationDirectory(String csvOpenAccountstFileCreationDirectory) {
        this.csvOpenAccountstFileCreationDirectory = csvOpenAccountstFileCreationDirectory;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setWorkdayOpenAccountDao(WorkdayOpenAccountDao workdayOpenAccountDao) {
        this.workdayOpenAccountDao = workdayOpenAccountDao;
    }

}
