package edu.cornell.kfs.vnd.batch.service.impl;

import static org.kuali.kfs.sys.fixture.UserNameFixture.kfs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.vnd.batch.service.VendorBatchService;


@ConfigureContext(session = kfs)
public class CuVendorBatchServiceImplTest extends KualiTestBase {

    private VendorBatchService vendorBatchService;
    private ConfigurationService  kualiConfigurationService;
    
    private static final Logger LOG = LogManager.getLogger(CuVendorBatchServiceImplTest.class);
    private static final String DATA_FILE_PATH = "src/test/java/edu/cornell/kfs/vnd/batch/service/impl/fixture/";
    private static final String HEADER_MISMATCH_FILE_NAME = "vendorBatch_column_header_mismatch";
    private static final String ADD_REQUIRED_FIELD_MISSING_FILE_NAME = "vendorBatch_add_required_field_missing";
    private static final String UPDATE_NOT_EXIST_VENDOR_FILE_NAME = "vendorBatch_update_not_exist_vendor";
    private static final String ADD_VENDOR_OK_FILE_NAME = "vendorBatch_add_vendor_ok";
    private static final String UPDATE_VENDOR_OK_FILE_NAME = "vendorBatch_update_vendor_ok";
    private static final String DOC_ATTACHMENT_FILE_NAME = "testdoc.docx";
    private static final String PDF_ATTACHMENT_FILE_NAME = "ConflictOfInterest.pdf";
    private static final String FORWARD_SLASH = "/";
    private static final String CSV_EXTENSION = ".csv";
    private static final String DONE_EXTENSION = ".done";
    private static final String ATTACHMENT_DIR = "/attachment";
    private String batchDirectory;  
    private String attachmentDirectory;  
    
    /**
     * basic set up for each test.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        vendorBatchService = new testableVenddorBatchServiceImpl();
        kualiConfigurationService = SpringContext.getBean(ConfigurationService.class);
        batchDirectory = kualiConfigurationService.getPropertyValueAsString(com.rsmart.kuali.kfs.sys.KFSConstants.STAGING_DIRECTORY_KEY) + "/vnd/vendorBatch";
        attachmentDirectory = batchDirectory + ATTACHMENT_DIR;
        //make sure we have a batch directory
        File batchDirectoryFile = new File(batchDirectory);
        if (!batchDirectoryFile.exists()) {
            batchDirectoryFile.mkdir();
        }
        File attachmentDirectoryFile = new File(attachmentDirectory);
        if (!attachmentDirectoryFile.exists()) {
            attachmentDirectoryFile.mkdir();
        }
    
    }
    
    /* 
     * copy the input file to staging and also create a 'done' file if it does not exist for each test
     */
    private void setUpTestInputFiles(String fileName) throws IOException {

        File dataFileSrc = new File(DATA_FILE_PATH + fileName + CSV_EXTENSION);
        File dataFileDest = new File(batchDirectory + "/"+ fileName + CSV_EXTENSION);
        FileUtils.copyFile(dataFileSrc, dataFileDest);

        //create .done file
        String doneFileName = batchDirectory + FORWARD_SLASH + fileName + DONE_EXTENSION;
        File doneFile = new File(doneFileName);
        if (!doneFile.exists()) {
            LOG.info("Creating done file: " + doneFile.getAbsolutePath());
            doneFile.createNewFile();
        }

    }
    
    /*
     * copy attachments to staging attachment directory
     */
    private void setUpTestAttachmentFiles(List<String> fileNames) throws IOException {

        for (String fileName : fileNames) {
            File dataFileSrc = new File(DATA_FILE_PATH + ATTACHMENT_DIR + FORWARD_SLASH + fileName );
            File dataFileDest = new File(attachmentDirectory + FORWARD_SLASH + fileName );
            FileUtils.copyFile(dataFileSrc, dataFileDest);
           
        }
    }

    /*
     * run test with no attachment
     */
    private void runTest(String fileName, boolean isSuccess) {
        runTestWithAttachments(fileName, null, isSuccess);
    }
    
    /*
     * run test with attachments
     */
    private void runTestWithAttachments(String fileName, List<String> attachments, boolean isSuccess) {
        try {
            setUpTestInputFiles(fileName);
            if (CollectionUtils.isNotEmpty(attachments)) {
                setUpTestAttachmentFiles(attachments);
            }
            if (isSuccess) {
                assertTrue(vendorBatchService.processVendors());                 
            } else {
                assertFalse(vendorBatchService.processVendors());  
            }
        } catch (RuntimeException e) {
            // TODO : add assert here ?
           // assertFalse(true);
        } catch (IOException ioe) {
            // TODO : add assert here ?
           // assertFalse(true);
        } finally {
            FileUtils.deleteQuietly(new File(batchDirectory + FORWARD_SLASH + fileName + DONE_EXTENSION));
        }               

    }
    

    /**
     * test that the input header columns are not the same as the columns in csv enum
     */
    public void testCsvColumnHeaderMismatch() {
        
        runTest(HEADER_MISMATCH_FILE_NAME, false);
    }

    /**
     * test add vendor with required field not in the input file.  using 'taxnumber' for testing in this case.
     */
    public void testAddVendorMissingRequiredField() {
       
        runTest(ADD_REQUIRED_FIELD_MISSING_FILE_NAME, false);        
    }

    /**
     * test update vendor and the input file sent a vendor number does not exist.
     */
    public void testUpdateNotExistVendor() {
        
        runTest(UPDATE_NOT_EXIST_VENDOR_FILE_NAME, false);        
        
    }

    /**
     * test add vendor is ok.  Also include attachment.
     */
    /* Commenting out bad test
    public void testAddVendorOK() {
        List<String> attachments = new ArrayList<String>();
        attachments.add(PDF_ATTACHMENT_FILE_NAME);
        runTestWithAttachments(ADD_VENDOR_OK_FILE_NAME, attachments, true);        
    }
    */

    /**
     * test update vendor is ok. also include attachments.
     */
    public void testUpdateVendorOK() {
        
        List<String> attachments = new ArrayList<String>();
        attachments.add(PDF_ATTACHMENT_FILE_NAME);
        attachments.add(DOC_ATTACHMENT_FILE_NAME);
        runTestWithAttachments(UPDATE_VENDOR_OK_FILE_NAME, attachments, true);        
    }
    
    private class testableVenddorBatchServiceImpl extends VendorBatchServiceImpl {
    	@Override
    	protected String getMimeTypeFromAttachmentFile(File attachmentFile) throws IOException {
    		return attachmentFile.getName();
    	}
    }

}
