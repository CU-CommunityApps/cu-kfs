package edu.cornell.kfs.sys.databaseviews;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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

import edu.cornell.kfs.sys.CUKFSConstants;
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
        runUnitTest(ViewsIntegFixture.COLLEGE_ORG_HRCY_V);
    }

    @Test
    public void testPersonDepartmentInfoView() {
        runUnitTest(ViewsIntegFixture.PERSON_DEPARTMENT_INFO_V_INCOMPLETE_DATA);
        runUnitTest(ViewsIntegFixture.PERSON_DEPARTMENT_INFO_V_COMPLETE_DATA);
    }

    @Test
    public void testPcdoInfoView() {
        runUnitTest(ViewsIntegFixture.PCDO_INFO_V);
    }

    @Test
    public void testPcardUserInfoView() {
       runUnitTest(ViewsIntegFixture.PCARD_USER_INFO_V_INCOMPLETE_DATA);
    }

    @Test
    public void testPersonEshopRoleView() {
        runUnitTest(ViewsIntegFixture.PERSON_ESHOP_ROLE_V_COMPLETE_DATA);
        runUnitTest(ViewsIntegFixture.PERSON_ESHOP_ROLE_V_INCOMPLETE_DATA);
    }

    @Test
    public void testPersonRoleView() {
        runUnitTest(ViewsIntegFixture.PERSON_ROLE_V_INCOMPLETE_DATA);
        runUnitTest(ViewsIntegFixture.PERSON_ROLE_V_COMPLETE_DATA);
    }

    @Test
    public void testPurchaseDetailsView() {
        runUnitTest(ViewsIntegFixture.PURCHASE_DETAILS_V_INCOMPLETE_DATA);
        runUnitTest(ViewsIntegFixture.PURCHASE_DETAILS_V_COMPLETE_DATA);
    }


    private void runUnitTest(ViewsIntegFixture dataFixtureForTest) {
        List incompleteDataValuesReturned =  integTestSqlDao.sqlSelect(dataFixtureForTest.getQuery());
        assertTrue(actualResultsMatchExpectedResults(dataFixtureForTest.name(), incompleteDataValuesReturned, dataFixtureForTest.getExpectedResults()));

    }


    private boolean actualResultsMatchExpectedResults(String identifierForViewAndDataBeingTested, List rowsOfActualResults, List rowsOfExpectedResults) {
        //Row counts for expected and actual do not match. Force unit test to fail.
        if (rowsOfActualResults.size() != rowsOfExpectedResults.size()) {
            LOG.error("actualResultsMatchExpectedResults: Query SHOULD HAVE returned the same number of key-value pairs we were expecting.");
            LOG.error("actualResultsMatchExpectedResults: Instead, query executed by unit test {} returned rowOfActualResults.size() = {} rather than expectedResults.size = {}", identifierForViewAndDataBeingTested, rowsOfActualResults.size(), rowsOfExpectedResults.size());
            return false;
        }
        
        //Previous mismatch check enforced that both expected results and actual query results were the same
        //so only have to verify one of them contains nothing to confirm that they match at nothing being returned.
        if (rowsOfActualResults.size() == 0) {
            return true;
        }
        
        //Now we are at the point of validating 1-to-many rowsOfActualResults to 1-to-many rowsOfExpectedResults
        //where each list element is a HashMap of (columnName, ObjectValue) pairs
        if (!queryDataRowValuesMatchExpectedDataRows(rowsOfActualResults, rowsOfExpectedResults)) {
            LOG.error("actualResultsMatchExpectedResults: Unit Test for view {} FAILED with query returned rows not matching expected results.", identifierForViewAndDataBeingTested);
            return false;
        }
        return true;
    }



    private boolean queryDataRowValuesMatchExpectedDataRows(List rowsOfActualResults, List rowsOfExpectedResults) {
        boolean foundMatchingExpectedRowForCurrentQueryRow = true;
        int currentQueryRowBeingValidated = 0;

        //loop through rows returned by unit test database query verifying every (column name, object value) matches an expected row
        while (foundMatchingExpectedRowForCurrentQueryRow && moreRowsToValidate(currentQueryRowBeingValidated, rowsOfActualResults.size())) {
            Map<String, String> actualRowOfDataFromQuery = obtainKeyValueMapFromValueList(rowsOfActualResults, currentQueryRowBeingValidated);
            
            foundMatchingExpectedRowForCurrentQueryRow = validateReturnedQueryDataRowIsInExpectedResults(actualRowOfDataFromQuery, rowsOfExpectedResults);
            
            if (!foundMatchingExpectedRowForCurrentQueryRow) {
                LOG.error("queryDataRowValuesMatchExpectedDataRows: currentQueryRowBeingValidated = {} with foundMatchingExpectedRowForCurrentQueryRow = {}", currentQueryRowBeingValidated, foundMatchingExpectedRowForCurrentQueryRow);
                return false;
            }
            currentQueryRowBeingValidated++;
        }
        return true;
    }



    private boolean validateReturnedQueryDataRowIsInExpectedResults(Map<String, String> actualRowOfDataFromQuery, List expectedResultsList) { //, boolean[] expectedRowsMatchedToValidatedQueryRows) {
        
        List<Map<String, String>> expectedResultsMapList = expectedResultsList;
        boolean exactMatchExpectedRowFound = false;
        
        for (Map<String, String> expectedResultsRow : expectedResultsMapList) {
            //array of booleans, each element corresponds to the map element being validated in the next for-loop
            boolean[] expectedRowElementsMatchedToQueryRowElements = new boolean[expectedResultsRow.size()];
            int queryRowElementIndex = 0;
            LOG.debug("validateReturnedQueryDataRowIsInExpectedResults: Validating   expectedResultsRow  = {} which has {} column,value pairs", expectedResultsRow.toString(), expectedResultsRow.size());
            LOG.debug("validateReturnedQueryDataRowIsInExpectedResults: against actualRowOfDataFromQuery = {} which has {} column,value pairs", actualRowOfDataFromQuery.toString(), actualRowOfDataFromQuery.size());
            
            
            for (Map.Entry<String, String> queryRowItem : actualRowOfDataFromQuery.entrySet()) {
                String queryColumnName = queryRowItem.getKey();
                Object queryDataValue = queryRowItem.getValue();
                
                if (expectedResultsRow.containsKey(queryColumnName)) {
                    if (!dataValuesMatch(queryDataValue, expectedResultsRow.get(queryColumnName))) {
                        LOG.debug("validateReturnedQueryDataRowIsInExpectedResults: Breaking out of comparision of result sets due to expected row for columnName {} not matching query returned value", queryColumnName);
                        break;
                    } else {
                        expectedRowElementsMatchedToQueryRowElements[queryRowElementIndex] = true;
                        LOG.debug("MATCH FOUND: setting boolean to true for unit test expected results columnName {} at index {}", queryColumnName, queryRowElementIndex);
                        queryRowElementIndex++;
                    }
                } else {
                    LOG.error("validateReturnedQueryDataRowIsInExpectedResults: View query returned columnName {} was not found in unit test expected values.", queryColumnName);
                    break;
                }
            }
            
            exactMatchExpectedRowFound = allExpectedRowElementsMatchedToQueryRowElements(expectedRowElementsMatchedToQueryRowElements);
            if (exactMatchExpectedRowFound) {
                LOG.debug("validateReturnedQueryDataRowIsInExpectedResults: Breaking out of validation");
                break;
            }
        }
        return exactMatchExpectedRowFound;
    }



    private boolean allExpectedRowElementsMatchedToQueryRowElements(boolean[] expectedRowElementsMatchedToQueryRowElements) {
        for (boolean item : expectedRowElementsMatchedToQueryRowElements) {
            if (!item) {
                return false;
            }
        }
        return true;
    }



    private Map<String, String> obtainKeyValueMapFromValueList(List<Map<String, String>> singleRowOfKeyValuePairs, int elementToReturn) {
        return singleRowOfKeyValuePairs.get(elementToReturn);
    }



    private boolean moreRowsToValidate (int currentRowBeingValidated, int sizeOfList) {
        if (currentRowBeingValidated < sizeOfList) {
            return true;
        }
        return false;
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
            transformedValue = (new SimpleDateFormat(CUKFSConstants.DATE_FORMAT_dd_MMM_yy)).format(dataAsTimestamp);
        }
        return transformedValue;
    }

}
