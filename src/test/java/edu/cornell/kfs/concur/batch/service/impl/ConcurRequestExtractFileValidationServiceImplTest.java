package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.Assert.*;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractFile;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractRequestDetailFileLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractRequestEntryDetailFileLine;
import edu.cornell.kfs.concur.batch.fixture.ConcurApplicationPropertiesFixture;
import edu.cornell.kfs.concur.batch.fixture.ConcurParameterConstantsFixture;
import edu.cornell.kfs.concur.batch.fixture.ConcurRequestExtractFileFixture;
import edu.cornell.kfs.concur.batch.report.ConcurRequestExtractBatchReportData;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

public class ConcurRequestExtractFileValidationServiceImplTest {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurRequestExtractFileValidationServiceImplTest.class);
    
    private static final String PARAM_VALUES_SPLIT_CHAR = ";";

    private ConcurRequestExtractFileValidationServiceImpl concurRequestExtractFileValidationService;
    private ConcurParameterConstantsFixture concurParameterConstantsFixture;
    private ConcurBatchUtilityServiceImpl concurBatchUtilityService;
    private ConcurApplicationPropertiesFixture concurApplicationPropertiesFixture;
    private TestableConfigurationServiceImpl configurationService;
    private ConcurEmployeeInfoValidationServiceImpl concurEmployeeInfoValidationService;
    
    @Before
    public void setUp() throws Exception {
        Logger.getLogger(ConcurRequestExtractFileValidationServiceImpl.class).setLevel(Level.DEBUG);
        concurRequestExtractFileValidationService = new ConcurRequestExtractFileValidationServiceImpl();
        concurBatchUtilityService = new TestableConcurBatchUtilityServiceImpl();
        concurEmployeeInfoValidationService = new TestableConcurEmployeeInfoValidationServiceImpl();
        concurRequestExtractFileValidationService.setConcurBatchUtilityService(concurBatchUtilityService);
        concurRequestExtractFileValidationService.setConcurEmployeeInfoValidationService(concurEmployeeInfoValidationService);
        configurationService = new TestableConfigurationServiceImpl();
        concurRequestExtractFileValidationService.setConfigurationService(configurationService);
        concurParameterConstantsFixture = new ConcurParameterConstantsFixture();
        concurApplicationPropertiesFixture = new ConcurApplicationPropertiesFixture();
    }
    
    @After
    public void tearDown() throws Exception {
        concurRequestExtractFileValidationService = null;
        concurBatchUtilityService = null;
        concurParameterConstantsFixture = null;
        concurApplicationPropertiesFixture = null;
        configurationService = null;
    }
    
    @Test
    public void testHeaderAmountAndHeaderRowCountsMatch() {
        LOG.info("testHeaderAmountAndHeaderRowCountsMatch");
        ConcurRequestExtractFile testFile = ConcurRequestExtractFileFixture.GOOD_FILE.createConcurRequestExtractFile();
        ConcurRequestExtractBatchReportData reportData = new ConcurRequestExtractBatchReportData();
        assertTrue("Expected Result: Header amount SHOULD match sum of row amounts from file.", concurRequestExtractFileValidationService.requestExtractHeaderRowValidatesToFileContents(testFile, reportData));
        LOG.info(reportData.getHeaderValidationErrors());
        LOG.info(KFSConstants.NEWLINE);
        reportData = null;
    }
    
    @Test
    public void testHeaderAmountDoesNotMatch() {
        LOG.info("testHeaderAmountDoesNotMatch");
        ConcurRequestExtractFile testFile = ConcurRequestExtractFileFixture.BAD_REQUEST_AMOUNT_FILE.createConcurRequestExtractFile();
        ConcurRequestExtractBatchReportData reportData = new ConcurRequestExtractBatchReportData();
        assertFalse("Expected Result: Header amount should NOT match sum of row amounts from file.", concurRequestExtractFileValidationService.requestExtractHeaderRowValidatesToFileContents(testFile, reportData));
        LOG.info(reportData.getHeaderValidationErrors());
        LOG.info(KFSConstants.NEWLINE);
        reportData = null;
    }

    @Test
    public void testHeaderRowCountDoesNotMatch() {
        LOG.info("testHeaderRowCountDoesNotMatch");
        ConcurRequestExtractFile testFile = ConcurRequestExtractFileFixture.BAD_FILE_COUNT_FILE.createConcurRequestExtractFile();
        ConcurRequestExtractBatchReportData reportData = new ConcurRequestExtractBatchReportData();
        assertFalse("Expected Result: Header row count should NOT match file row count.", concurRequestExtractFileValidationService.requestExtractHeaderRowValidatesToFileContents(testFile, reportData));
        LOG.info(reportData.getHeaderValidationErrors());
        LOG.info(KFSConstants.NEWLINE);
        reportData = null;
    }

    @Test
    public void testFileContainsBadEmployeeGroupId() {
        LOG.info("testFileContainsBadEmployeeGroupId");
        ConcurRequestExtractFile testFile = ConcurRequestExtractFileFixture.BAD_EMPLOYEE_GROUP_ID_FILE.createConcurRequestExtractFile();
        ConcurRequestExtractBatchReportData reportData = new ConcurRequestExtractBatchReportData();
        assertFalse("Expected Result: Request Detail row contains BAD Employee Group Id.", concurRequestExtractFileValidationService.requestExtractHeaderRowValidatesToFileContents(testFile, reportData));
        LOG.info(reportData.getHeaderValidationErrors());
        LOG.info(KFSConstants.NEWLINE);
        reportData = null;
    }

    @Test
    public void testFileContainsMultipleRequestDetailLinesHeaderAmountMatch() {
        LOG.info("testFileContainsMultipleRequestDetailLinesHeaderAmountMatch");
        ConcurRequestExtractFile testFile = ConcurRequestExtractFileFixture.GOOD_FILE_MULTIPLE_DETAILS.createConcurRequestExtractFile();
        ConcurRequestExtractBatchReportData reportData = new ConcurRequestExtractBatchReportData();
        assertTrue("Expected Result: Header amount SHOULD match sum of row amounts from file.", concurRequestExtractFileValidationService.requestExtractHeaderRowValidatesToFileContents(testFile, reportData));
        LOG.info(reportData.getHeaderValidationErrors());
        LOG.info(KFSConstants.NEWLINE);
        reportData = null;
    }

    @Test
    public void testFileContainsMultipleRequestDetailLinesHeaderAmountDoesNotMatch() {
        LOG.info("testFileContainsMultipleRequestDetailLinesHeaderAmountDoesNotMatch");
        ConcurRequestExtractFile testFile = ConcurRequestExtractFileFixture.BAD_REQUEST_AMOUNT_MULTIPLE_DETAILS_FILE.createConcurRequestExtractFile();
        ConcurRequestExtractBatchReportData reportData = new ConcurRequestExtractBatchReportData();
        assertFalse("Expected Result: Header amount should NOT match sum of row amounts from file.", concurRequestExtractFileValidationService.requestExtractHeaderRowValidatesToFileContents(testFile, reportData));
        LOG.info(reportData.getHeaderValidationErrors());
        LOG.info(KFSConstants.NEWLINE);
        reportData = null;
    }

    private class TestableConcurBatchUtilityServiceImpl extends ConcurBatchUtilityServiceImpl {
        @Override
        public String getConcurParameterValue(String parameterName) {
            String parameterValue = concurParameterConstantsFixture.getValueForConcurParameter(parameterName);
            if (StringUtils.isEmpty(parameterValue)) {
                return parameterName;
            }
            else {
                return parameterValue;
            }
        }
    }

    private class TestableConfigurationServiceImpl implements ConfigurationService {
        public String getPropertyValueAsString(String propertyName) {
            String propertyValue = concurApplicationPropertiesFixture.getPropertyValueAsString(propertyName);
            if (StringUtils.isEmpty(propertyValue)) {
                return propertyName;
            }
            else {
                return propertyValue;
            }
        }

        public Map<String, String> getAllProperties() {
            return concurApplicationPropertiesFixture.getAllProperties();
        }

        public boolean getPropertyValueAsBoolean(String key) {
            return false;
        }

        public boolean getPropertyValueAsBoolean(String key, boolean defaultValue) {
            return false;
        }
    }
    
    private class TestableConcurEmployeeInfoValidationServiceImpl extends ConcurEmployeeInfoValidationServiceImpl {
        @Override
        public boolean isEmployeeGroupIdValid(String employeeGroupId) {
            if(StringUtils.isNotBlank(employeeGroupId)){
                String parameterValue = concurParameterConstantsFixture.getValueForConcurParameter(ConcurParameterConstants.CONCUR_CUSTOMER_PROFILE_GROUP_ID);

                if(StringUtils.isNotBlank(parameterValue) && StringUtils.contains(parameterValue, PARAM_VALUES_SPLIT_CHAR)){
                    String[] parameterValues = parameterValue.split(PARAM_VALUES_SPLIT_CHAR);
                    for(String value : parameterValues){
                        if(StringUtils.equalsIgnoreCase(value, employeeGroupId)){
                            return true;
                        }
                    }
                }
            }
            return false;           
        }
    }

}
