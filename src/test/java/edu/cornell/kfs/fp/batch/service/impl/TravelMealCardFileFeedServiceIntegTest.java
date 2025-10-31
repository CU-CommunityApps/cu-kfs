package edu.cornell.kfs.fp.batch.service.impl;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.fp.batch.service.CardServicesUtilityService;
import edu.cornell.kfs.fp.batch.service.TravelMealCardFileFeedService;
import edu.cornell.kfs.fp.businessobject.TravelMealCardFileLineEntry;
import edu.cornell.kfs.sys.CUKFSConstants;

@ConfigureContext(session = ccs1)
public class TravelMealCardFileFeedServiceIntegTest extends KualiIntegTestBase {
    private static final Logger LOG = LogManager.getLogger();
    private static final String DATA_FILE_NAME = "fp_tmcard_verify_20251017";
    private static final String DATA_EXTENSION = ".data";
    private static final String DONE_EXTENSION = ".done";
    private static final String FIXTURE_DATA_FILE_AND_PATH = "src/test/resources/edu/cornell/kfs/fp/batch/service/fixture/" + DATA_FILE_NAME + DATA_EXTENSION;
    private static final int LINES_THAT_SHOULD_HAVE_BEEN_READ = 17;
    private String batchDirectory;
    private ConfigurationService  kualiConfigurationService;
    private CardServicesUtilityService cardServicesUtilityService;
    private TravelMealCardFileFeedService travelMealCardFileFeedService;
    private String batchLocationDataFileNameWithExtension;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        cardServicesUtilityService= SpringContext.getBean(CardServicesUtilityService.class);
        travelMealCardFileFeedService= SpringContext.getBean(TravelMealCardFileFeedService.class);
        kualiConfigurationService = SpringContext.getBean(ConfigurationService.class);
        batchDirectory = kualiConfigurationService.getPropertyValueAsString(com.rsmart.kuali.kfs.sys.KFSConstants.STAGING_DIRECTORY_KEY) + "/fp/tmcardVerification";
        
        //Ensure there is a batch directory
        File batchDirectoryFile = new File(batchDirectory);
        batchDirectoryFile.mkdir();
        
        //copy the data file into place
        File dataFileSrc = new File(FIXTURE_DATA_FILE_AND_PATH);
        batchLocationDataFileNameWithExtension = batchDirectory + CUKFSConstants.SLASH + DATA_FILE_NAME + DATA_EXTENSION;
        File dataFileDest = new File(batchLocationDataFileNameWithExtension);
        FileUtils.copyFile(dataFileSrc, dataFileDest);
        
        //create .done file
        String doneFileName = batchDirectory + CUKFSConstants.SLASH + DATA_FILE_NAME + DONE_EXTENSION;
        File doneFile = new File(doneFileName);
        if (!doneFile.exists()) {
            LOG.info("Creating done file: " + doneFile.getAbsolutePath());
            doneFile.createNewFile();
        }
    }
    
    public void testCanParseFiles() {
        List<TravelMealCardFileLineEntry> fileLinesRead = travelMealCardFileFeedService.readTmCardFileContents(batchLocationDataFileNameWithExtension);
        assertEquals(LINES_THAT_SHOULD_HAVE_BEEN_READ, fileLinesRead.size());
        
        //cleanup after the unit test for now
        List<String> dataFileNames = new ArrayList<String>();
        dataFileNames.add(batchLocationDataFileNameWithExtension);
        cardServicesUtilityService.removeDoneFiles(dataFileNames);
    }
    
}

