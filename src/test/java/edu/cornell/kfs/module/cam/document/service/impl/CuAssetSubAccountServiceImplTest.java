package edu.cornell.kfs.module.cam.document.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.regex.PatternSyntaxException;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.module.cam.CamsConstants;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.module.cam.businessobject.AssetGlpeSourceDetail;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.coreservice.api.parameter.EvaluationOperator;
import org.kuali.kfs.coreservice.api.parameter.Parameter;
import org.kuali.kfs.coreservice.api.parameter.ParameterType;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.coreservice.impl.parameter.ParameterServiceImpl;
import edu.cornell.kfs.module.cam.CuCamsConstants;

public class CuAssetSubAccountServiceImplTest {

    private static final String TEST_ACCOUNT = "G104700";
    private static final String TEST_SUB_ACCOUNT = "TECH";
    private static final String TEST_PLANT_ACCOUNT = "G10CAPT";

    private static final String TEST_PATTERN_WILDCARD = "*CAPT";
    private static final String TEST_PATTERN_REGULAR_EXACT_MATCH = TEST_ACCOUNT;
    private static final String TEST_PATTERN_PLANT_EXACT_MATCH = TEST_PLANT_ACCOUNT;
    private static final String TEST_PATTERN_WILDCARD_BOTH_MATCH = "G10*";
    private static final String TEST_PATTERN_NO_MATCH = "NONE";
    private static final String TEST_MULTI_PATTERN = "*XXXX;*CAPT";
    private static final String TEST_MULTI_PATTERN_REGULAR_MATCH = "*XXXX;G104*";
    private static final String TEST_MULTI_PATTERN_NO_MATCH = "*XXXX;NONE";
    private static final String TEST_BAD_PATTERN = "[*CAPT}";

    private static final boolean FOR_WHITELIST = true;
    private static final boolean FOR_BLACKLIST = false;
    private static final boolean PLANT_DETAIL_PRESERVED = true;
    private static final boolean PLANT_DETAIL_NOT_PRESERVED = false;

    protected TestCuAssetSubAccountServiceImpl assetSubAccountService;
    protected AssetGlpeSourceDetail regularDetail;
    protected AssetGlpeSourceDetail plantDetail;

    @Before
    public void setUp() throws Exception {
        assetSubAccountService = new TestCuAssetSubAccountServiceImpl();
        regularDetail = createSourceDetail(TEST_ACCOUNT, TEST_SUB_ACCOUNT);
        plantDetail = createSourceDetail(TEST_PLANT_ACCOUNT, TEST_SUB_ACCOUNT);
    }

    @Test
    public void testWildcardPatternMatchForBlacklist() throws Exception {
        setupParameterServiceWithPlantAccountValue(TEST_PATTERN_WILDCARD, EvaluationOperator.DISALLOW);
        assertPreservationOfRegularDetailOnly(FOR_BLACKLIST);
    }

    @Test
    public void testExactPatternMatchForWhitelist() throws Exception {
        setupParameterServiceWithPlantAccountValue(TEST_PATTERN_REGULAR_EXACT_MATCH, EvaluationOperator.ALLOW);
        assertPreservationOfRegularDetailOnly(FOR_WHITELIST);
    }

    @Test
    public void testExactPatternMatchForBlacklist() throws Exception {
        setupParameterServiceWithPlantAccountValue(TEST_PATTERN_PLANT_EXACT_MATCH, EvaluationOperator.DISALLOW);
        assertPreservationOfRegularDetailOnly(FOR_BLACKLIST);
    }

    @Test
    public void testWildcardPatternMatchForBothAccountsInWhitelist() throws Exception {
        setupParameterServiceWithPlantAccountValue(TEST_PATTERN_WILDCARD_BOTH_MATCH, EvaluationOperator.ALLOW);
        assertPreservationOfBothDetails(FOR_WHITELIST);
    }

    @Test
    public void testExactPatternMatchForNeitherAccountInBlacklist() throws Exception {
        setupParameterServiceWithPlantAccountValue(TEST_PATTERN_NO_MATCH, EvaluationOperator.DISALLOW);
        assertPreservationOfBothDetails(FOR_BLACKLIST);
    }

    @Test
    public void testBothDetailsPreservedIfNoPatternsDefinedInBlacklist() throws Exception {
        setupParameterServiceWithPlantAccountValue(KFSConstants.EMPTY_STRING, EvaluationOperator.DISALLOW);
        assertPreservationOfBothDetails(FOR_BLACKLIST);
    }

    @Test
    public void testBothDetailsPreservedIfParameterDoesNotExist() throws Exception {
        setupParameterServiceToReturnNullPlantAccountParameter();
        assertPreservationOfBothDetails(FOR_BLACKLIST);
    }

    @Test
    public void testWildcardPatternMatchForMultiValueBlacklist() throws Exception {
        setupParameterServiceWithPlantAccountValue(TEST_MULTI_PATTERN, EvaluationOperator.DISALLOW);
        assertPreservationOfRegularDetailOnly(FOR_BLACKLIST);
    }

    @Test
    public void testWildcardPatternMatchForMultiValueWhitelist() throws Exception {
        setupParameterServiceWithPlantAccountValue(TEST_MULTI_PATTERN_REGULAR_MATCH, EvaluationOperator.ALLOW);
        assertPreservationOfRegularDetailOnly(FOR_WHITELIST);
    }

    @Test
    public void testWildcardPatternMatchForNeitherAccountInMultiValueBlacklist() throws Exception {
        setupParameterServiceWithPlantAccountValue(TEST_MULTI_PATTERN_NO_MATCH, EvaluationOperator.DISALLOW);
        assertPreservationOfBothDetails(FOR_BLACKLIST);
    }

    @Test
    public void testHandlingOfInvalidPatternSetup() throws Exception {
        setupParameterServiceWithPlantAccountValue(TEST_BAD_PATTERN, EvaluationOperator.DISALLOW);

        try {
            assetSubAccountService.shouldClearSubAccount(regularDetail);
            fail("CuAssetSubAccountServiceImpl should have thrown a PatternSyntaxException due to an invalid pattern string.");
        } catch (PatternSyntaxException e) {
        }

        assertPreservationOfDetailSubAccounts(PLANT_DETAIL_PRESERVED);
    }

    protected void assertPreservationOfRegularDetailOnly(boolean whitelist) throws Exception {
        assertFalse(whitelist ? "Regular Detail should have matched a whitelisted pattern" : "Regular Detail should not have matched a blacklisted pattern",
                assetSubAccountService.shouldClearSubAccount(regularDetail));
        assertTrue(whitelist ? "Plant Detail should not have matched a whitelisted pattern" : "Plant Detail should have matched a blacklisted pattern",
                assetSubAccountService.shouldClearSubAccount(plantDetail));
        assertPreservationOfDetailSubAccounts(PLANT_DETAIL_NOT_PRESERVED);
    }

    protected void assertPreservationOfBothDetails(boolean whitelist) throws Exception {
        assertFalse(whitelist ? "Regular Detail should have matched a whitelisted pattern" : "Regular Detail should not have matched a blacklisted pattern",
                assetSubAccountService.shouldClearSubAccount(regularDetail));
        assertFalse(whitelist ? "Plant Detail should have matched a whitelisted pattern" : "Plant Detail should not have matched a blacklisted pattern",
                assetSubAccountService.shouldClearSubAccount(plantDetail));
        assertPreservationOfDetailSubAccounts(PLANT_DETAIL_PRESERVED);
    }

    protected void assertPreservationOfDetailSubAccounts(boolean plantDetailPreserved) throws Exception {
        assertNotNull("Regular Detail should have had its sub-account defined prior to processing", regularDetail.getSubAccountNumber());
        assertNotNull("Plant Detail should have had its sub-account defined prior to processing", plantDetail.getSubAccountNumber());

        assetSubAccountService.clearSubAccountIfNecessary(regularDetail);
        assetSubAccountService.clearSubAccountIfNecessary(plantDetail);

        assertNotNull("Regular Detail should have had its sub-account preserved", regularDetail.getSubAccountNumber());
        if (plantDetailPreserved) {
            assertNotNull("Plant Detail should have had its sub-account preserved", plantDetail.getSubAccountNumber());
        } else {
            assertNull("Plant Detail should have had its sub-account cleared out", plantDetail.getSubAccountNumber());
        }
    }

    protected <T> T createMockServiceExpectingNoCalls(Class<T> serviceClass) {
        T mockService = EasyMock.createMock(serviceClass);
        EasyMock.replay(mockService);
        return mockService;
    }

    protected void setupParameterServiceWithPlantAccountValue(String parameterValue, EvaluationOperator operator) {
        Parameter parameter = createAssetTransferPlantAccountParameter(parameterValue, operator);
        assetSubAccountService.setParameterService(createMockParameterServiceForReturningFullParameter(parameter, parameter, Asset.class));
    }

    protected void setupParameterServiceToReturnNullPlantAccountParameter() {
        Parameter parameterForSearchArgs = createAssetTransferPlantAccountParameter(KFSConstants.EMPTY_STRING, EvaluationOperator.DISALLOW);
        assetSubAccountService.setParameterService(createMockParameterServiceForReturningFullParameter(parameterForSearchArgs, null, Asset.class));
    }

    protected ParameterService createMockParameterServiceForReturningFullParameter(Parameter searchArgs, Parameter returnValue, Class<?> componentClass) {
        ParameterService parameterService = EasyMock.createMock(ParameterServiceImpl.class);
        EasyMock.expect(parameterService.getParameter(componentClass, searchArgs.getName())).andStubReturn(returnValue);
        EasyMock.expect(parameterService.getParameter(
                searchArgs.getNamespaceCode(), searchArgs.getComponentCode(), searchArgs.getName())).andStubReturn(returnValue);
        EasyMock.replay(parameterService);
        return parameterService;
    }

    protected Parameter createAssetTransferPlantAccountParameter(String value, EvaluationOperator operator) {
        Parameter.Builder parameter = Parameter.Builder.create(
                KFSConstants.APPLICATION_NAMESPACE_CODE, CamsConstants.CAM_MODULE_CODE, Asset.class.getSimpleName(),
                CuCamsConstants.Parameters.ASSET_PLANT_ACCOUNTS_TO_FORCE_CLEARING_OF_GLPE_SUB_ACCOUNTS,
                ParameterType.Builder.create(KfsParameterConstants.PARAMETER_CONFIG_TYPE_CODE));
        parameter.setValue(value);
        parameter.setEvaluationOperator(operator);
        return parameter.build();
    }

    protected AssetGlpeSourceDetail createSourceDetail(String accountNumber, String subAccountNumber) {
        AssetGlpeSourceDetail sourceDetail = new AssetGlpeSourceDetail();
        sourceDetail.setChartOfAccountsCode("IT");
        sourceDetail.setAccountNumber(accountNumber);
        sourceDetail.setSubAccountNumber(subAccountNumber);
        return sourceDetail;
    }

    protected static class TestCuAssetSubAccountServiceImpl extends CuAssetSubAccountServiceImpl {
        @Override
        public void clearSubAccountIfNecessary(AssetGlpeSourceDetail postable) {
            super.clearSubAccountIfNecessary(postable);
        }

        @Override
        public boolean shouldClearSubAccount(AssetGlpeSourceDetail postable) {
            return super.shouldClearSubAccount(postable);
        }
    }
}
