package edu.cornell.kfs.module.receiptProcessing.service;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import edu.cornell.kfs.module.receiptProcessing.batch.ReceiptProcessingCSV;
import edu.cornell.kfs.module.receiptProcessing.batch.ReceiptProcessingCSVInputFileType;
import edu.cornell.kfs.module.receiptProcessing.service.impl.ReceiptProcessingServiceImpl;
import edu.cornell.kfs.sys.batch.service.impl.CuBatchInputFileServiceImpl;
import junit.framework.TestCase;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.batch.BatchInputFileType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;


@ConfigureContext(session = ccs1)
public class CuReceiptProcessingServiceImplNegativeTest extends TestCase {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ReceiptProcessingService.class);

    private static final String DATA_FILE_PATH = "src/test/java/edu/cornell/kfs/module/receiptProcessing/service/fixture/receiptProcessing_bad_test.csv";
    private static final String IMG_FILE_PATH = "src/test/java/edu/cornell/kfs/module/receiptProcessing/service/attachements/testUnit.pdf";
    private static final String BATCH_DIRECTORY = "test/opt/work/staging/fp/receiptProcessing";
    private static final String RECEIPT_DIR = "/infra/receipt_processing/CIT-csv-archive/";

    private ReceiptProcessingServiceImpl receiptProcessingService;

    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        receiptProcessingService = new ReceiptProcessingServiceImpl();

        CuBatchInputFileServiceImpl batchInputFileService = new CuBatchInputFileServiceImpl();
        receiptProcessingService.setBatchInputFileService(batchInputFileService);

        ReceiptProcessingCSVInputFileType batchInputFileType = new ReceiptProcessingCSVInputFileType();
        batchInputFileType.setDirectoryPath(BATCH_DIRECTORY);
        batchInputFileType.setFileExtension("csv");
        batchInputFileType.setCsvEnumClass(ReceiptProcessingCSV.class);
        List<BatchInputFileType> batchInputFileTypeList = new ArrayList<>();
        batchInputFileTypeList.add(batchInputFileType);

        receiptProcessingService.setBatchInputFileTypes(batchInputFileTypeList);

        //make sure we have a batch directory
        File batchDirectoryFile = new File(BATCH_DIRECTORY);
        batchDirectoryFile.mkdir();

        //copy the data file into place
        File dataFileSrc = new File(DATA_FILE_PATH);
        File dataFileDest = new File(BATCH_DIRECTORY + "/receiptProcessing_test.csv");
        FileUtils.copyFile(dataFileSrc, dataFileDest);

        //create .done file
        String doneFileName = BATCH_DIRECTORY + "/receiptProcessing_test.done";
        File doneFile = new File(doneFileName);
        if (!doneFile.exists()) {
            LOG.info("Creating done file: " + doneFile.getAbsolutePath());
            doneFile.createNewFile();
        }
        
        //make sure we have a pdf directory
        File pdfDirectoryFile = new File(RECEIPT_DIR);
        pdfDirectoryFile.mkdir();

        //copy the data file into place
        File imgFileSrc = new File(IMG_FILE_PATH);
        File imgFileDest = new File(RECEIPT_DIR + "/testUnit.pdf");
        FileUtils.copyFile(imgFileSrc, imgFileDest);
    }

    @Override
    protected void tearDown() throws Exception {
        File batchDirectoryFile = new File(BATCH_DIRECTORY);
        batchDirectoryFile.delete();
    }


    public void testCanLoadFiles() {
        try {
            assertFalse(receiptProcessingService.loadFiles());
        }
        catch (RuntimeException e)
        {

        }
    }
    
}
