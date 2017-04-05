package edu.cornell.kfs.module.receiptProcessing.service;

import edu.cornell.kfs.module.receiptProcessing.batch.ReceiptProcessingCSV;
import edu.cornell.kfs.module.receiptProcessing.batch.ReceiptProcessingCSVInputFileType;
import edu.cornell.kfs.module.receiptProcessing.service.impl.ReceiptProcessingServiceImpl;
import edu.cornell.kfs.sys.batch.service.impl.CuBatchInputFileServiceImpl;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.sys.batch.BatchInputFileType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CuReceiptProcessingServiceImplNegativeTest {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ReceiptProcessingService.class);

    private static final String DATA_FILE_PATH = "src/test/java/edu/cornell/kfs/module/receiptProcessing/service/fixture/receiptProcessing_bad_test.csv";
    private static final String IMG_FILE_PATH = "src/test/java/edu/cornell/kfs/module/receiptProcessing/service/attachements/testUnit.pdf";
    private static final String BATCH_DIRECTORY = "test/opt/work/staging/fp/receiptProcessing";
    private static final String RECEIPT_DIR = "/infra/receipt_processing/CIT-csv-archive/";

    private ReceiptProcessingServiceImpl receiptProcessingService;

    @Before
    public void setUp() throws Exception {
        receiptProcessingService = new ReceiptProcessingServiceImpl();

        CuBatchInputFileServiceImpl batchInputFileService = new CuBatchInputFileServiceImpl();
        receiptProcessingService.setBatchInputFileService(batchInputFileService);
        receiptProcessingService.setBatchInputFileTypes(setupBatchInputFileTypes());

        createDirectory(BATCH_DIRECTORY);
        copyFile(DATA_FILE_PATH, BATCH_DIRECTORY + "/receiptProcessing_test.csv");
        createDoneFile();
        createDirectory(RECEIPT_DIR);
        copyFile(IMG_FILE_PATH, RECEIPT_DIR + "/testUnit.pdf");
    }

    private List<BatchInputFileType> setupBatchInputFileTypes() {
        ReceiptProcessingCSVInputFileType batchInputFileType = new ReceiptProcessingCSVInputFileType();
        batchInputFileType.setDirectoryPath(BATCH_DIRECTORY);
        batchInputFileType.setFileExtension("csv");
        batchInputFileType.setCsvEnumClass(ReceiptProcessingCSV.class);
        List<BatchInputFileType> batchInputFileTypeList = new ArrayList<>();
        batchInputFileTypeList.add(batchInputFileType);

        return batchInputFileTypeList;
    }

    private void createDirectory(String batchDirectory) {
        File batchDirectoryFile = new File(batchDirectory);
        batchDirectoryFile.mkdir();
    }

    private void copyFile(String dataFilePath, String pathname) throws IOException {
        File dataFileSrc = new File(dataFilePath);
        File dataFileDest = new File(pathname);
        FileUtils.copyFile(dataFileSrc, dataFileDest);
    }

    private void createDoneFile() throws IOException {
        String doneFileName = BATCH_DIRECTORY + "/receiptProcessing_test.done";
        File doneFile = new File(doneFileName);
        if (!doneFile.exists()) {
            LOG.info("Creating done file: " + doneFile.getAbsolutePath());
            doneFile.createNewFile();
        }
    }

    @After
    public void tearDown() throws Exception {
        deleteDirectory(BATCH_DIRECTORY);
        deleteDirectory(RECEIPT_DIR);
    }

    private void deleteDirectory(String batchDirectory) {
        File batchDirectoryFile = new File(batchDirectory);
        batchDirectoryFile.delete();
    }
    
    @Test(expected=RuntimeException.class)
    public void testCanLoadFiles() {
        Assert.assertFalse(receiptProcessingService.loadFiles());
    }
    
}
