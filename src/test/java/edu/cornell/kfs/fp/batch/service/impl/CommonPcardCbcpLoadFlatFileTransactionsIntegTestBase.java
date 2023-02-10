package edu.cornell.kfs.fp.batch.service.impl;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.dataaccess.UnitTestSqlDao;

import edu.cornell.kfs.sys.CUKFSConstants;

import org.kuali.kfs.core.api.config.property.ConfigurationService;
import com.rsmart.kuali.kfs.sys.KFSConstants;

/**
 * CBCP processing is based on PCard and in some cases CBCP calls the actual PCard routines to perform its processing.
 * Uniting the parts of the integration tests for these two areas to achieve economy of scale and reduce maintenance.
 */

public abstract class CommonPcardCbcpLoadFlatFileTransactionsIntegTestBase extends KualiIntegTestBase {
    private static final Logger LOG = LogManager.getLogger(); 

    private static final String GOOD_DATA_FILE_NAME = "fp_pcdo_cbcp_usbank_good_fixed_width";
    protected static final String GOOD_DATA_FILE_NAME_WITH_DATA_EXTENSION = GOOD_DATA_FILE_NAME + CUKFSConstants.FileExtensions.DATA;
    protected static final String GOOD_DATA_FILE_NAME_WITH_DONE_EXTENSION = GOOD_DATA_FILE_NAME + CUKFSConstants.FileExtensions.DONE;
    
    private static final String BAD_DATA_FILE_NAME = "fp_pcdo_cbcp_usbank_bad_fixed_width";
    protected static final String BAD_DATA_FILE_NAME_WITH_DATA_EXTENSION = BAD_DATA_FILE_NAME + CUKFSConstants.FileExtensions.DATA;
    protected static final String BAD_DATA_FILE_NAME_WITH_DONE_EXTENSION = BAD_DATA_FILE_NAME + CUKFSConstants.FileExtensions.DONE;
    
    private static final String INTEGRATION_TEST_DATA_FILE_BASE_PATH_LOCATION = "src/test/resources/edu/cornell/kfs/fp/batch/service/fixture/";
    
    protected static final String GOOD_DATA_INTEGRATION_TEST_FULLY_QUALIFIED_PATH_FILE_EXTENSION = INTEGRATION_TEST_DATA_FILE_BASE_PATH_LOCATION + GOOD_DATA_FILE_NAME_WITH_DATA_EXTENSION;
    protected static final String BAD_DATA_INTEGRATION_TEST_FULLY_QUALIFIED_PATH_FILE_EXTENSION = INTEGRATION_TEST_DATA_FILE_BASE_PATH_LOCATION + BAD_DATA_FILE_NAME_WITH_DATA_EXTENSION;
    
    private ConfigurationService kualiConfigurationService;
    private UnitTestSqlDao unitTestSqlDao;
    private String stagingBatchDirectory;
    private String dataFileSubDirectory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        kualiConfigurationService = SpringContext.getBean(ConfigurationService.class);
        stagingBatchDirectory = kualiConfigurationService.getPropertyValueAsString(KFSConstants.STAGING_DIRECTORY_KEY) + getDataFileSubDirectory();
        unitTestSqlDao = SpringContext.getBean(UnitTestSqlDao.class);
        
        //make sure we have a batch directory
        File batchDirectoryFile = new File(stagingBatchDirectory);
        batchDirectoryFile.mkdir();

        //copy the data files into place
        File goodDataFileSrc = new File(GOOD_DATA_INTEGRATION_TEST_FULLY_QUALIFIED_PATH_FILE_EXTENSION);
        File goodDataFileDest = new File(stagingBatchDirectory + GOOD_DATA_FILE_NAME_WITH_DATA_EXTENSION);
        FileUtils.copyFile(goodDataFileSrc, goodDataFileDest);
        File badDataFileSrc = new File(BAD_DATA_INTEGRATION_TEST_FULLY_QUALIFIED_PATH_FILE_EXTENSION);
        File badDataFileDest = new File(stagingBatchDirectory + BAD_DATA_FILE_NAME_WITH_DATA_EXTENSION);
        FileUtils.copyFile(badDataFileSrc, badDataFileDest);

        //create .done files
        String goodDoneFileName = stagingBatchDirectory + GOOD_DATA_FILE_NAME_WITH_DONE_EXTENSION;
        File goodDoneFile = new File(goodDoneFileName);
        if (!goodDoneFile.exists()) {
            LOG.info("Creating good data done file: " + goodDoneFile.getAbsolutePath());
            goodDoneFile.createNewFile();
        }
        String badDoneFileName = stagingBatchDirectory + BAD_DATA_FILE_NAME_WITH_DONE_EXTENSION;
        File badDoneFile = new File(badDoneFileName);
        if (!badDoneFile.exists()) {
            LOG.info("Creating bad data done file: " + badDoneFile.getAbsolutePath());
            badDoneFile.createNewFile();
        }
    }

    public ConfigurationService getKualiConfigurationService() {
        return kualiConfigurationService;
    }

    public void setKualiConfigurationService(ConfigurationService kualiConfigurationService) {
        this.kualiConfigurationService = kualiConfigurationService;
    }

    public UnitTestSqlDao getUnitTestSqlDao() {
        return unitTestSqlDao;
    }

    public void setUnitTestSqlDao(UnitTestSqlDao unitTestSqlDao) {
        this.unitTestSqlDao = unitTestSqlDao;
    }

    public String getStagingBatchDirectory() {
        return stagingBatchDirectory;
    }

    public void setStagingBatchDirectory(String stagingBatchDirectory) {
        this.stagingBatchDirectory = stagingBatchDirectory;
    }

    public String getDataFileSubDirectory() {
        return dataFileSubDirectory;
    }

    public void setDataFileSubDirectory(String dataFileSubDirectory) {
        this.dataFileSubDirectory = dataFileSubDirectory;
    }

}
