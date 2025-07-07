package edu.cornell.kfs.sys.databaseviews;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.dataaccess.IntegTestSqlDao;

import edu.cornell.kfs.sys.databaseviews.fixture.ViewsIntegFixture;

@ConfigureContext(session = ccs1)
public class CuViewsIntegTest extends KualiIntegTestBase {

    private static final Logger LOG = LogManager.getLogger();
    private ConfigurationService kualiConfigurationService;
    private IntegTestSqlDao integTestSqlDao;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        kualiConfigurationService = SpringContext.getBean(ConfigurationService.class);
        integTestSqlDao = SpringContext.getBean(IntegTestSqlDao.class);
    }
    
    @Test
    public void testCollegeOrgHierarchyView() {
        List dataValuesReturned =  integTestSqlDao.sqlSelect(ViewsIntegFixture.COLLEGE_ORG_HRCY_V.query);
        assertTrue(actualResultsMatchExpectedResults(dataValuesReturned, ViewsIntegFixture.COLLEGE_ORG_HRCY_V.expectedResults));
    }
    
    @Test
    public void testPersonDepartmentInfoView() {
        List incompleteDataValuesReturned =  integTestSqlDao.sqlSelect(ViewsIntegFixture.PERSON_DEPARTMENT_INFO_V_INCOMPLETE_DATA.query);
        assertTrue(actualResultsMatchExpectedResults(incompleteDataValuesReturned, ViewsIntegFixture.PERSON_DEPARTMENT_INFO_V_INCOMPLETE_DATA.expectedResults));

        List dataValuesReturned =  integTestSqlDao.sqlSelect(ViewsIntegFixture.PERSON_DEPARTMENT_INFO_V_COMPLETE_DATA.query);
        assertTrue(actualResultsMatchExpectedResults(dataValuesReturned, ViewsIntegFixture.PERSON_DEPARTMENT_INFO_V_COMPLETE_DATA.expectedResults));
    }
    
    @Test
    public void testPcdoInfoView() {
        List dataValuesReturned =  integTestSqlDao.sqlSelect(ViewsIntegFixture.PCDO_INFO_V.query);
        assertTrue(actualResultsMatchExpectedResults(dataValuesReturned, ViewsIntegFixture.PCDO_INFO_V.expectedResults));
    }
    
    private boolean actualResultsMatchExpectedResults(List rowOfActualResults, HashMap<String, String> expectedResults) {
        if (rowOfActualResults.size() != 1) {
            LOG.error("actualResultsMatchExpectedResults: Query should have returned one row of multiple key-value pairs, instead rowOfActualResults.size() = {}", rowOfActualResults.size());
            return false;
        }
        
        Map<String, String> actualElementsFromResult = obtainKeyValueMapFromValueList(rowOfActualResults);
        if (actualElementsFromResult.size() != expectedResults.size()) {
            LOG.error("actualResultsMatchExpectedResults: Number of columns we expected were not returned by the query, instead actualElementsFromResult.size() = {}", actualElementsFromResult.size());
            return false;
        }
        
        for (Map.Entry<String, String> entry : actualElementsFromResult.entrySet()) {
            String columnName = entry.getKey();
            Object dataValue = entry.getValue();
            if (expectedResults.containsKey(columnName)) {
                if (!dataValuesMatch(dataValue, expectedResults.get(columnName))) {
                    LOG.error("actualResultsMatchExpectedResults: Value return by view {} did not match what was expected {}.", dataValue, expectedResults.get(columnName));
                    return false;
                }
            } else {
                LOG.error("actualResultsMatchExpectedResults: View returned columnName = {} was not found in unit test expected values.", columnName);
                return false;
            }
        }
        return true;
    }
    
    private Map<String, String> obtainKeyValueMapFromValueList(List<Map<String, String>> singleRowOfKeyValuePairs) {
        return singleRowOfKeyValuePairs.get(0);
    }
    
    private boolean dataValuesMatch(Object valueReturned, Object valueExpected) {
        if (ObjectUtils.isNull(valueReturned) & ObjectUtils.isNull(valueExpected)) {
            return true;
        }
        
        Class<? extends Object> returnedClassType = valueReturned.getClass();
        String transformed = transformDataIntoCorrectType(returnedClassType, valueReturned);
        if (transformed.equalsIgnoreCase((String) valueExpected)) {
            return true;
        }
        return false;
    }

    private String transformDataIntoCorrectType(Class<? extends Object> classType, Object dataValue) {
        String transformedValue = KFSConstants.EMPTY_STRING;
        if (classType.equals(String.class)) {
            transformedValue = (String) dataValue;
        }
        if (classType.equals(BigDecimal.class)) {
            transformedValue = dataValue.toString();
        }
        if (classType.equals(Timestamp.class)) {
            Timestamp dataAsTimestamp = (Timestamp) dataValue;
            transformedValue = dataAsTimestamp.toLocalDateTime().toString();
        }
        return transformedValue;
    }

} 
