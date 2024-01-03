package edu.cornell.kfs.module.purap.document.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.coreservice.api.parameter.EvaluationOperator;
import org.kuali.kfs.coreservice.impl.parameter.Parameter;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.PersistenceService;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.businessobject.OrganizationParameter;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.kfs.core.api.parameter.ParameterEvaluatorService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.impl.parameter.ParameterEvaluatorServiceImpl;
import org.mockito.invocation.InvocationOnMock;

import edu.cornell.kfs.module.purap.CUPurapParameterConstants;
import edu.cornell.kfs.module.purap.CuPurapTestConstants;
import edu.cornell.kfs.module.purap.fixture.RequisitionLiteFixture;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CuPurapServiceImplTest {

    private static final KualiDecimal TEST_VENDOR_CONTRACT_APO_LIMIT = new KualiDecimal(35000);
    private static final KualiDecimal TEST_ORG_PARM_APO_LIMIT = new KualiDecimal(7000);
    private static final KualiDecimal TEST_DEFAULT_REGULAR_APO_LIMIT = new KualiDecimal(25000);
    private static final KualiDecimal TEST_DEFAULT_FEDERAL_APO_LIMIT = new KualiDecimal(10000);
    public static final String PARAMETER_CONFIG_TYPE_CODE = "CONFG";

    private static final String TEST_EXCLUDED_COST_SOURCES = CuPurapTestConstants.COST_SOURCE_PRICING_AGREEMENT
            + CUKFSConstants.SEMICOLON + CuPurapTestConstants.COST_SOURCE_EDU_AND_INST_COOP;
    private static final String TEST_COST_SOURCES_FOR_APO_OVERRIDE = CuPurapTestConstants.COST_SOURCE_PREFERRED
            + CUKFSConstants.SEMICOLON + CuPurapTestConstants.COST_SOURCE_CONTRACT;

    private CuPurapServiceImpl purapService;

    @Before
    public void setUp() throws Exception {
        ParameterService parameterService = buildMockParameterService();
        ParameterEvaluatorService parameterEvaluatorService = buildParameterEvaluatorService(parameterService);
        
        purapService = new CuPurapServiceImpl();
        purapService.setParameterService(parameterService);
        purapService.setParameterEvaluatorService(parameterEvaluatorService);
        purapService.setVendorService(buildMockVendorService());
        purapService.setPersistenceService(buildMockPersistenceService());
        purapService.setBusinessObjectService(buildMockBusinessObjectService());
    }

    @Test
    public void testApoLimitForBareDocument() throws Exception {
        assertPurapServiceIdentifiesCorrectApoLimit(
                TEST_DEFAULT_REGULAR_APO_LIMIT, RequisitionLiteFixture.REQS_DEFAULT_DATA);
    }

    @Test
    public void testApoLimitForDocumentWithLineMissingAccount() throws Exception {
        assertPurapServiceIdentifiesCorrectApoLimit(
                TEST_DEFAULT_REGULAR_APO_LIMIT, RequisitionLiteFixture.REQS_LINE_WITHOUT_ACCOUNT);
    }

    @Test
    public void testApoLimitForDocumentWithOneNonCfdaLine() throws Exception {
        assertPurapServiceIdentifiesCorrectApoLimit(
                TEST_DEFAULT_REGULAR_APO_LIMIT, RequisitionLiteFixture.REQS_NON_CFDA_LINE);
    }

    @Test
    public void testApoLimitForDocumentWithOneCfdaLine() throws Exception {
        assertPurapServiceIdentifiesCorrectApoLimit(
                TEST_DEFAULT_FEDERAL_APO_LIMIT, RequisitionLiteFixture.REQS_CFDA_LINE);
    }

    @Test
    public void testApoLimitForDocumentWithMultipleItemLinesAndOneCfdaLine() throws Exception {
        assertPurapServiceIdentifiesCorrectApoLimit(
                TEST_DEFAULT_FEDERAL_APO_LIMIT, RequisitionLiteFixture.REQS_MULTI_LINE_ONE_CFDA);
    }

    @Test
    public void testApoLimitForDocumentWithMultipleNonCfdaItemLines() throws Exception {
        assertPurapServiceIdentifiesCorrectApoLimit(
                TEST_DEFAULT_REGULAR_APO_LIMIT, RequisitionLiteFixture.REQS_MULTI_LINE_NO_CFDA);
    }

    @Test
    public void testApoLimitForCfdaDocumentWithVendorContractLimit() throws Exception {
        assertPurapServiceIdentifiesCorrectApoLimit(
                TEST_VENDOR_CONTRACT_APO_LIMIT, RequisitionLiteFixture.REQS_CFDA_LINE_AND_VENDOR_CONTRACT_WITH_LIMIT);
    }

    @Test
    public void testApoLimitForCfdaDocumentWithVendorContractMissingLimit() throws Exception {
        assertPurapServiceIdentifiesCorrectApoLimit(
                TEST_DEFAULT_FEDERAL_APO_LIMIT, RequisitionLiteFixture.REQS_CFDA_LINE_AND_VENDOR_CONTRACT_WITHOUT_LIMIT);
    }

    @Test
    public void testApoLimitForCfdaDocumentWithNonFedCostSourceAndVendorContractMissingLimit() throws Exception {
        assertPurapServiceIdentifiesCorrectApoLimit(
                TEST_DEFAULT_REGULAR_APO_LIMIT, RequisitionLiteFixture.REQS_CFDA_LINE_AND_INELIGIBLE_COST_SOURCE_AND_VENDOR_CONTRACT_WITHOUT_LIMIT);
    }

    @Test
    public void testApoLimitForCfdaDocumentWithMatchingOrgParameter() throws Exception {
        assertPurapServiceIdentifiesCorrectApoLimit(
                TEST_DEFAULT_FEDERAL_APO_LIMIT, RequisitionLiteFixture.REQS_CFDA_LINE_AND_ORG_PARM);
    }

    @Test
    public void testApoLimitForNonCfdaDocumentWithMatchingOrgParameter() throws Exception {
        assertPurapServiceIdentifiesCorrectApoLimit(
                TEST_ORG_PARM_APO_LIMIT, RequisitionLiteFixture.REQS_NON_CFDA_LINE_AND_ORG_PARM);
    }

    @Test
    public void testApoLimitForCfdaDocumentWithMatchingOrgParameterAndVendorContractLimit() throws Exception {
        assertPurapServiceIdentifiesCorrectApoLimit(
                TEST_VENDOR_CONTRACT_APO_LIMIT, RequisitionLiteFixture.REQS_CFDA_LINE_AND_ORG_PARM_AND_VENDOR_CONTRACT_WITH_LIMIT);
    }

    @Test
    public void testApoLimitForCfdaDocumentWithMatchingOrgParameterAndVendorContractMissingLimit() throws Exception {
        assertPurapServiceIdentifiesCorrectApoLimit(
                TEST_ORG_PARM_APO_LIMIT, RequisitionLiteFixture.REQS_CFDA_LINE_AND_ORG_PARM_AND_VENDOR_CONTRACT_WITHOUT_LIMIT);
    }

    @Test
    public void testApoLimitForCfdaDocumentWithVendorContractLimitAndNonFedOverrideSource() throws Exception {
        assertPurapServiceIdentifiesCorrectApoLimit(
                TEST_DEFAULT_FEDERAL_APO_LIMIT, RequisitionLiteFixture.REQS_CFDA_LINE_AND_VENDOR_CONTRACT_NON_FED_OVERRIDE_SOURCE);
    }

    @Test
    public void testApoLimitForNoLinesDocumentWithVendorContractLimitAndNonFedOverrideSource() throws Exception {
        assertPurapServiceIdentifiesCorrectApoLimit(
                TEST_VENDOR_CONTRACT_APO_LIMIT, RequisitionLiteFixture.REQS_NO_LINES_AND_VENDOR_CONTRACT_NON_FED_OVERRIDE_SOURCE);
    }

    @Test
    public void testApoLimitForMultiLineNonCfdaDocumentWithVendorContractLimitAndNonFedOverrideSource() throws Exception {
        assertPurapServiceIdentifiesCorrectApoLimit(
                TEST_VENDOR_CONTRACT_APO_LIMIT, RequisitionLiteFixture.REQS_MULTI_LINE_NO_CFDA_AND_VENDOR_CONTRACT_NON_FED_OVERRIDE_SOURCE);
    }

    @Test
    public void testApoLimitForMultiLinePartialCfdaDocumentWithVendorContractLimitAndNonFedOverrideSource() throws Exception {
        assertPurapServiceIdentifiesCorrectApoLimit(
                TEST_VENDOR_CONTRACT_APO_LIMIT, RequisitionLiteFixture.REQS_MULTI_LINE_SOME_CFDA_AND_VENDOR_CONTRACT_NON_FED_OVERRIDE_SOURCE);
    }

    @Test
    public void testApoLimitForMultiLineAllCfdaDocumentWithVendorContractLimitAndNonFedOverrideSource() throws Exception {
        assertPurapServiceIdentifiesCorrectApoLimit(
                TEST_DEFAULT_FEDERAL_APO_LIMIT, RequisitionLiteFixture.REQS_MULTI_LINE_ALL_CFDA_AND_VENDOR_CONTRACT_NON_FED_OVERRIDE_SOURCE);
    }

    private void assertPurapServiceIdentifiesCorrectApoLimit(KualiDecimal expectedLimit, RequisitionLiteFixture documentFixture) throws Exception {
        RequisitionDocument document = documentFixture.toRequisitionDocument();
        KualiDecimal actualLimit = purapService.getApoLimit(document);
        assertEquals("Wrong APO limit", expectedLimit, actualLimit);
    }

    private VendorService buildMockVendorService() {
        VendorService vendorService = mock(VendorService.class);
        
        when(vendorService.getApoLimitFromContract(CuPurapTestConstants.TEST_CONTRACT_ID_1357,
                CuPurapTestConstants.TEST_CONTRACT_CHART, CuPurapTestConstants.TEST_CONTRACT_ORG))
                .thenReturn(TEST_VENDOR_CONTRACT_APO_LIMIT);
        when(vendorService.getApoLimitFromContract(CuPurapTestConstants.TEST_CONTRACT_ID_1357,
                CuPurapTestConstants.TEST_PARM_CHART, CuPurapTestConstants.TEST_PARM_ORG))
                .thenReturn(TEST_VENDOR_CONTRACT_APO_LIMIT);
        
        return vendorService;
    }

    private PersistenceService buildMockPersistenceService() {
        PersistenceService persistenceService = mock(PersistenceService.class);
        when(persistenceService.getPrimaryKeyFieldValues(any(OrganizationParameter.class)))
                .thenAnswer(this::getPrimaryKeyFieldValues);
        return persistenceService;
    }

    private Map<String, ?> getPrimaryKeyFieldValues(InvocationOnMock invocation) {
        OrganizationParameter orgParameter = invocation.getArgument(0);
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, orgParameter.getChartOfAccountsCode());
        propertyMap.put(KFSPropertyConstants.ORGANIZATION_CODE, orgParameter.getOrganizationCode());
        return propertyMap;
    }

    private BusinessObjectService buildMockBusinessObjectService() {
        Map<String, ?> testMap = buildMapForOrgParameterMatching();
        OrganizationParameter testParm = buildTestOrgParameter();
        BusinessObjectService businessObjectService = mock(BusinessObjectService.class);
        when(businessObjectService.findByPrimaryKey(OrganizationParameter.class, testMap))
                .thenReturn(testParm);
        return businessObjectService;
    }

    private Map<String, ?> buildMapForOrgParameterMatching() {
        Map<String, Object> testMap = new HashMap<>();
        testMap.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, CuPurapTestConstants.TEST_PARM_CHART);
        testMap.put(KFSPropertyConstants.ORGANIZATION_CODE, CuPurapTestConstants.TEST_PARM_ORG);
        testMap.put(KRADPropertyConstants.ACTIVE_INDICATOR, Boolean.TRUE);
        return testMap;
    }

    private OrganizationParameter buildTestOrgParameter() {
        OrganizationParameter testParm = new OrganizationParameter();
        testParm.setChartOfAccountsCode(CuPurapTestConstants.TEST_PARM_CHART);
        testParm.setOrganizationCode(CuPurapTestConstants.TEST_PARM_ORG);
        testParm.setOrganizationAutomaticPurchaseOrderLimit(TEST_ORG_PARM_APO_LIMIT);
        testParm.setActiveIndicator(true);
        return testParm;
    }

    private ParameterEvaluatorService buildParameterEvaluatorService(ParameterService parameterService) {
        ParameterEvaluatorServiceImpl parameterEvaluatorService = new ParameterEvaluatorServiceImpl();
        parameterEvaluatorService.setParameterService(parameterService);
        return parameterEvaluatorService;
    }

    private ParameterService buildMockParameterService() {
        ParameterService parameterService = mock(ParameterService.class);
        Parameter costSourceExclusionParameter = buildParameterForCostSourceExclusions();
        Parameter costSourceApoOverrideParameter = buildParameterForCostSourceApoOverride();

        when(parameterService.getParameterValueAsString(
                PurapConstants.PURAP_NAMESPACE, PurapParameterConstants.Components.PURCHASE_ORDER, PurapParameterConstants.APO_LIMIT))
                .thenReturn(TEST_DEFAULT_REGULAR_APO_LIMIT.toString());
        when(parameterService.getParameterValueAsString(
                RequisitionDocument.class, CUPurapParameterConstants.AUTOMATIC_FEDERAL_PURCHASE_ORDER_DEFAULT_LIMIT_AMOUNT))
                .thenReturn(TEST_DEFAULT_FEDERAL_APO_LIMIT.toString());
        when(parameterService.getParameter(
                RequisitionDocument.class, CUPurapParameterConstants.CONTRACTING_SOURCES_EXCLUDED_FROM_FEDERAL_PO_DEFAULT_LIMIT_AMOUNT))
                .thenReturn(costSourceExclusionParameter);
        when(parameterService.getParameter(
                RequisitionDocument.class, CUPurapParameterConstants.CONTRACTING_SOURCES_ALLOWED_OVERRIDE_OF_FEDERAL_PO_DEFAULT_LIMIT_AMOUNT))
                .thenReturn(costSourceApoOverrideParameter);
        
        return parameterService;
    }

    private Parameter buildParameterForCostSourceExclusions() {
        return buildCostSourceParameterDTO(CUPurapParameterConstants.CONTRACTING_SOURCES_EXCLUDED_FROM_FEDERAL_PO_DEFAULT_LIMIT_AMOUNT,
                TEST_EXCLUDED_COST_SOURCES, EvaluationOperator.DISALLOW);
    }

    private Parameter buildParameterForCostSourceApoOverride() {
        return buildCostSourceParameterDTO(CUPurapParameterConstants.CONTRACTING_SOURCES_ALLOWED_OVERRIDE_OF_FEDERAL_PO_DEFAULT_LIMIT_AMOUNT,
                TEST_COST_SOURCES_FOR_APO_OVERRIDE, EvaluationOperator.ALLOW);
    }

    private Parameter buildCostSourceParameterDTO(String parameterName, String parameterValue, EvaluationOperator evaluationOperator) {
        Parameter parameter = new Parameter();
        parameter.setNamespaceCode(PurapConstants.PURAP_NAMESPACE);
        parameter.setComponentCode("Requisition");
        parameter.setParameterTypeCode(PARAMETER_CONFIG_TYPE_CODE);
        parameter.setName(parameterName);
        parameter.setValue(parameterValue);
        parameter.setEvaluationOperatorCode(evaluationOperator.getCode());
        return parameter;
    }

}
