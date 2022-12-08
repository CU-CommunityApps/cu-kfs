package edu.cornell.kfs.coa.batch.service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import edu.cornell.kfs.coa.batch.CuCoaBatchConstants;
import edu.cornell.kfs.coa.batch.CuCoaBatchConstants.WorkdayOpenAccountDetailDTOCsvColumn;
import edu.cornell.kfs.coa.batch.businessobject.WorkdayOpenAccountDetailDTO;
import edu.cornell.kfs.coa.batch.dataaccess.WorkdayOpenAccountDao;
import edu.cornell.kfs.coa.batch.service.CreateWorkdayOpenAccountsCsvService;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.util.EnumConfiguredMappingStrategy;

public class CreateWorkdayOpenAccountsCsvServiceImpl implements CreateWorkdayOpenAccountsCsvService {
    private static final Logger LOG = LogManager.getLogger();
    
    protected String csvOpenAccountsExportDirectory;
    protected String csvOpenAccountsFileCreationDirectory;
    protected DateTimeService dateTimeService;
    protected WorkdayOpenAccountDao workdayOpenAccountDao;
    
    @Override
    public void createWorkdayOpenAccountsCsv() throws IOException {
        List<WorkdayOpenAccountDetailDTO> details = workdayOpenAccountDao.getWorkdayOpenAccountDetails();
        if (CollectionUtils.isNotEmpty(details)) {
            LOG.info("createWorkdayOpenAccountsCsv, number of open account detail lines to write: " + details.size());
            writeOpenAccountsToCsvFile(details);
        } else {
            LOG.error("createWorkdayOpenAccountsCsv, no open accounts were found, this shouldn't happen");
            throw new RuntimeException("No open accounts were found.");
        }
        
    }
    
    private void writeOpenAccountsToCsvFile(List<WorkdayOpenAccountDetailDTO> details) {
        String csvFileName = generateCsvOutputFileName();
        String fullyQualifiedCreationDirectoryFileName = fullyQualifyFileNameToCreationDirectory(csvFileName);
        LOG.info("writeOpenAccountsToCsvFile: fullyQualifiedOutputFile = " + fullyQualifiedCreationDirectoryFileName);
        
        try (FileOutputStream fileOutputStream = new FileOutputStream(fullyQualifiedCreationDirectoryFileName);
                OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
                BufferedWriter bufferedWriter = new BufferedWriter(writer);) {
            StatefulBeanToCsv<WorkdayOpenAccountDetailDTO> csvWriter = buildCsvWriter(bufferedWriter);
            csvWriter.write(details);
        } catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException | IOException e) {
            LOG.error("writeOpenAccountsToCsvFile, Error writing to file " + fullyQualifiedCreationDirectoryFileName, e);
            throw new RuntimeException(e);
        }

        try {
            String fullyQualifiedExportDirectoryFileName = fullyQualifyFileNameToExportDirectory(csvFileName);
            LOG.info("writeOpenAccountsToCsvFile, Moving data file from creation directory to fullyQualifiedExportDirectoryFileName = "
                            + fullyQualifiedExportDirectoryFileName);
            File outputFile = new File(fullyQualifiedCreationDirectoryFileName);
            moveCreatedFileToExportDirectory(outputFile, fullyQualifiedExportDirectoryFileName);
            LOG.info("writeOpenAccountsToCsvFile, file was successfully moved to export directory.");
        } catch (IOException ie) {
            LOG.error("writeOpenAccountsToCsvFile, had a problem moving the output file.", ie);
            throw new RuntimeException(ie);
        }

    }
    
    private StatefulBeanToCsv<WorkdayOpenAccountDetailDTO> buildCsvWriter(BufferedWriter bufferedWriter) {
        EnumConfiguredMappingStrategy<WorkdayOpenAccountDetailDTO, WorkdayOpenAccountDetailDTOCsvColumn> mappingStrategy = new EnumConfiguredMappingStrategy<>(
                WorkdayOpenAccountDetailDTOCsvColumn.class, WorkdayOpenAccountDetailDTOCsvColumn::getHeaderLabel,
                WorkdayOpenAccountDetailDTOCsvColumn::getWorkdayOpenAccountDetailPropertyName);
        mappingStrategy.setType(WorkdayOpenAccountDetailDTO.class);

        StatefulBeanToCsv<WorkdayOpenAccountDetailDTO> csvWriter = new StatefulBeanToCsvBuilder<WorkdayOpenAccountDetailDTO>(
                bufferedWriter).withMappingStrategy(mappingStrategy).build();
        return csvWriter;
    }
    
    private String generateCsvOutputFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        StringBuilder filename = new StringBuilder(CuCoaBatchConstants.WorkdayOpenAccountsFileCreationConstants.OUTPUT_FILE_NAME);
        filename.append("_").append(sdf.format(dateTimeService.getCurrentDate())).append(CUKFSConstants.FileExtensions.CSV);
        return filename.toString();
    }
    
    private String fullyQualifyFileNameToCreationDirectory(String csvFileName) {
        StringBuilder filename = new StringBuilder(csvOpenAccountsFileCreationDirectory);
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

    public void setCsvOpenAccountsExportDirectory(String csvOpenAccountsExportDirectory) {
        this.csvOpenAccountsExportDirectory = csvOpenAccountsExportDirectory;
    }

    public void setCsvOpenAccountsFileCreationDirectory(String csvOpenAccountsFileCreationDirectory) {
        this.csvOpenAccountsFileCreationDirectory = csvOpenAccountsFileCreationDirectory;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setWorkdayOpenAccountDao(WorkdayOpenAccountDao workdayOpenAccountDao) {
        this.workdayOpenAccountDao = workdayOpenAccountDao;
    }

}
