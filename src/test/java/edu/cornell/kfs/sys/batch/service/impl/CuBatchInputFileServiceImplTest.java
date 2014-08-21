package edu.cornell.kfs.sys.batch.service.impl;

import static org.kuali.kfs.sys.fixture.UserNameFixture.kfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.kuali.kfs.fp.batch.ProcurementCardInputFileType;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.impl.identity.PersonImpl;
import org.kuali.rice.krad.util.GlobalVariables;

import edu.cornell.kfs.sys.batch.AccountReversionInputFileType;
import edu.cornell.kfs.vnd.batch.VendorBatchCsvInputFileType;


/**
 * 
 * KFS already has BatchInputFileServiceImplTest.  So, this test focused on CU modification which related to create 'done' file or not.
 *
 */
@ConfigureContext(session = kfs)
public class CuBatchInputFileServiceImplTest extends KualiTestBase {

    private BatchInputFileService batchInputFileService;
    
    private static final String TEST_BATCH_DIRECTORY = "src/test/java/edu/cornell/kfs/sys/batch/service/impl/fixture/";

    private String testFileIdentifier;
    private InputStream validAccountReversionFileContents;
    private InputStream validPcdoFileContents;

    private BatchInputFileType pcdoBatchInputFileType;
    private BatchInputFileType accountReversionInputFileType;
    private List<File> createdTestFiles;

    private Person user;
    private BatchInputFileType batchInputFileType;
    private String fileUserIdentifier; 
    private InputStream fileContents;
    private Object parsedObject;
    /**
     * basic set up for each test.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        batchInputFileService = SpringContext.getBean(BatchInputFileService.class);
        pcdoBatchInputFileType = SpringContext.getBean(ProcurementCardInputFileType.class);
        accountReversionInputFileType = SpringContext.getBean(AccountReversionInputFileType.class);
        testFileIdentifier = "junit" + RandomUtils.nextInt();
        validAccountReversionFileContents = new FileInputStream(TEST_BATCH_DIRECTORY + "AccountReversion.csv");
        validPcdoFileContents = new FileInputStream(TEST_BATCH_DIRECTORY + "BatchInputValidPCDO.xml");
 
        createdTestFiles = new ArrayList<File>();
    
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();

        if (createdTestFiles != null) {
            for (File createdFile : createdTestFiles) {
                if (createdFile.exists()) {
                    createdFile.delete();
                }
                String doneFileName = StringUtils.substringBeforeLast(createdFile.getPath(), ".") + ".done";
                File doneFile = new File(doneFileName);
                if (doneFile.exists()) {
                    doneFile.delete();
                }
            }
        }
    }

    /*
     * test several potential IllegalArgumentException
     */
    public void testIllegalArguments() {
        runTestIllegalArgument();
        user = new PersonImpl();
        runTestIllegalArgument();
        batchInputFileType = new VendorBatchCsvInputFileType();
        runTestIllegalArgument();
        fileContents = new InputStream() {            
           @Override
            public int read() throws IOException {
                // TODO Auto-generated method stub
                return 0;
            }
        };
        fileUserIdentifier = " 123.abc"; // Only allow alphnumeric
        runTestIllegalArgument();
    }

    private void runTestIllegalArgument() {
        boolean isCorrectException = false;
        try {
            batchInputFileService.save(user, batchInputFileType, fileUserIdentifier, fileContents, parsedObject);
        } catch (IllegalArgumentException ie) {
            isCorrectException = true;
        }
        assertTrue("Not throw IllegalArgumentException", isCorrectException);

    }
    
    /*
     * This is the core test of CU modification
     * If the input file type is not CuBatchInputFileType then we create the
     * done file.  If it is of type CuBatchInputFileType then we also must call the
     * isDoneFileRequired function.
     * 1. PCDO is used to test the original save is fine.
     * 2. AccountReversion is used to test not to create 'done' file
     * This is based on BatchInputFileTypeServiceTest
     */
    public final void testSave() throws Exception {
        user = GlobalVariables.getUserSession().getPerson();
        String savedFileName = batchInputFileService.save(user, pcdoBatchInputFileType, testFileIdentifier, validPcdoFileContents, new ArrayList());

        File expectedFile = new File(savedFileName);
        createdTestFiles.add(expectedFile);

        // Make sure CU mods does not affect the inputfiletype is not an instance of CuBatchInputFileType
        assertTrue("uploaded pcdo file not found", expectedFile.exists());
        assertTrue("uploaded pcdo file is empty", expectedFile.length() > 0);


        checkForDoneFile(expectedFile, true);

        // remove file so we can test collector upload
        expectedFile.delete();

        // Make sure CU mods is correct; not to create done file
        savedFileName = batchInputFileService.save(user, accountReversionInputFileType, testFileIdentifier, validAccountReversionFileContents, null);

        expectedFile = new File(savedFileName);
        createdTestFiles.add(expectedFile);

        assertTrue("uploaded AccountReversion file not found", expectedFile.exists());
        assertTrue("uploaded AccountReversion file is empty", expectedFile.length() > 0);

        checkForDoneFile(expectedFile, false);
    }

    private final void checkForDoneFile(File batchFile, boolean isExist) {
        String doneFileName = StringUtils.substringBeforeLast(batchFile.getPath(), ".") + ".done";
        File doneFile = new File(doneFileName);

        if (isExist) {
            assertTrue("done file " + doneFile.getPath() + " does not exist", doneFile.exists());
        } else {
            assertTrue("done file " + doneFile.getPath() + " does exist", !doneFile.exists());
        }
    }

}
