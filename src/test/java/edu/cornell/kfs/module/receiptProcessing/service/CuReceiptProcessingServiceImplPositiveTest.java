package edu.cornell.kfs.module.receiptProcessing.service;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.module.receiptProcessing.service.impl.ReceiptProcessingServiceImpl;
import edu.cornell.kfs.sys.CUKFSConstants.FileExtensions;

@ConfigureContext(session = ccs1)
public class CuReceiptProcessingServiceImplPositiveTest extends KualiIntegTestBase {

    private ReceiptProcessingService receiptProcessingService;
    private ConfigurationService  kualiConfigurationService;
    
    private static final Logger LOG = LogManager.getLogger(CuReceiptProcessingServiceImplPositiveTest.class);
    private static final String DATA_FILE_PATH = "src/test/resources/edu/cornell/kfs/module/receiptProcessing/service/fixture/receiptProcessing_test.csv";
    private static final String IMG_FILE_PATH = "src/test/resources/edu/cornell/kfs/module/receiptProcessing/service/attachments/testUnit.pdf";
    private static final String CSV_ARCHIVE_SUB_PATH = "/CIT-csv-archive/";
    private static final String TEST_CUSTOMER_CSV_ARCHIVE_SUB_PATH = "/CIT-test-csv-archive/";
    private String batchDirectory;  

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        receiptProcessingService = SpringContext.getBean(ReceiptProcessingService.class);
        kualiConfigurationService = SpringContext.getBean(ConfigurationService.class);
        batchDirectory = kualiConfigurationService.getPropertyValueAsString(com.rsmart.kuali.kfs.sys.KFSConstants.STAGING_DIRECTORY_KEY) + "/fp/receiptProcessing";
        
        //make sure we have a batch directory
        //batchDirectory = SpringContext.getBean(ReceiptProcessingService.class).getDirectoryPath();
        File batchDirectoryFile = new File(batchDirectory);
        batchDirectoryFile.mkdir();

        //copy the data file into place
        File dataFileSrc = new File(DATA_FILE_PATH);
        File dataFileDest = new File(batchDirectory + "/receiptProcessing_kfs_test_20220102030405.csv");
        FileUtils.copyFile(dataFileSrc, dataFileDest);

        //create .done file
        String doneFileName = batchDirectory + "/receiptProcessing_kfs_test_20220102030405.done";
        File doneFile = new File(doneFileName);
        if (!doneFile.exists()) {
            LOG.info("Creating done file: " + doneFile.getAbsolutePath());
            doneFile.createNewFile();
        }
        
        //make sure we have a pdf directory
        String pdfDirectory = ((ReceiptProcessingServiceImpl) receiptProcessingService).getPdfDirectory();
        File pdfDirectoryFile = new File(pdfDirectory);
        pdfDirectoryFile.mkdir();

        //copy the data file into place
        File imgFileSrc = new File(IMG_FILE_PATH);
        File imgFileDest = new File(pdfDirectory + CSV_ARCHIVE_SUB_PATH + "testUnit.pdf");
        FileUtils.copyFile(imgFileSrc, imgFileDest);
    }
    
    public void testCanLoadFiles() {
        Set<String> existingArchiveFiles = getExistingFilesInTestArchiveDirectory();
        assertTrue(receiptProcessingService.loadFiles());       
        
        List<String> newArchiveFiles = getAlphabetizedNewFilesInTestArchiveDirectory(existingArchiveFiles);
        assertEquals("Wrong number of new files in archive directory", 2, newArchiveFiles.size());
        
        String newCsvFile = newArchiveFiles.get(0);
        String newDoneFile = newArchiveFiles.get(1);
        assertTrue("New file should have been a .csv one", StringUtils.endsWith(newCsvFile, FileExtensions.CSV));
        assertTrue("New file should have been a .done one", StringUtils.endsWith(newDoneFile, FileExtensions.DONE));
        
        String csvNameWithoutExtension = StringUtils.substringBeforeLast(newCsvFile, KFSConstants.DELIMITER);
        String doneNameWithoutExtension = StringUtils.substringBeforeLast(newDoneFile, KFSConstants.DELIMITER);
        assertEquals("Wrong .done file name", csvNameWithoutExtension, doneNameWithoutExtension);
    }
    
    private Set<String> getExistingFilesInTestArchiveDirectory() {
        return getFilesInTestArchiveDirectoryAsStream((dir, name) -> true)
                .collect(Collectors.toUnmodifiableSet());
    }
    
    private List<String> getAlphabetizedNewFilesInTestArchiveDirectory(Set<String> existingFiles) {
        return getFilesInTestArchiveDirectoryAsStream((dir, name) -> !existingFiles.contains(name))
                .sorted()
                .collect(Collectors.toUnmodifiableList());
    }
    
    private Stream<String> getFilesInTestArchiveDirectoryAsStream(FilenameFilter filter) {
        String pdfDirectory = ((ReceiptProcessingServiceImpl) receiptProcessingService).getPdfDirectory();
        File testCustomerArchiveDirectory = new File(pdfDirectory + TEST_CUSTOMER_CSV_ARCHIVE_SUB_PATH);
        if (testCustomerArchiveDirectory.exists()) {
            return Arrays.stream(testCustomerArchiveDirectory.list(filter));
        } else {
            return Stream.empty();
        }
    }
    
}
