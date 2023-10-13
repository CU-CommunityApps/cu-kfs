package edu.cornell.kfs.module.ar.document.validation.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kuali.kfs.krad.document.DocumentBase;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.ArPropertyConstants;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.document.dataaccess.ContractsGrantsInvoiceDocumentDao;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.document.service.FinancialSystemDocumentService;
import org.kuali.kfs.sys.document.service.impl.FinancialSystemDocumentServiceImpl;
import org.kuali.kfs.core.api.search.SearchOperator;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import edu.cornell.kfs.module.ar.CuArPropertyConstants;
import edu.cornell.kfs.module.ar.document.fixture.ContractsGrantsInvoiceDocumentFixture;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ContractsGrantsInvoiceDocument.class})
@PowerMockIgnore({ "javax.xml.*"})
public class FirstInvoiceForAwardSuspensionCategoryTest {

    private FinancialSystemDocumentService financialSystemDocumentService;
    private FirstInvoiceForAwardSuspensionCategory suspensionCategory;

    @Before
    public void setUp() throws Exception {
        PowerMockito.suppress(PowerMockito.constructor(DocumentBase.class));
        financialSystemDocumentService = new FinancialSystemDocumentServiceImpl();
        suspensionCategory = new FirstInvoiceForAwardSuspensionCategory();
        suspensionCategory.setFinancialSystemDocumentService(financialSystemDocumentService);
        suspensionCategory.setContractsGrantsInvoiceDocumentDao(buildMockContractsGrantsInvoiceDocumentDao());
    }

    protected ContractsGrantsInvoiceDocumentDao buildMockContractsGrantsInvoiceDocumentDao() {
        ContractsGrantsInvoiceDocumentDao invoiceDocumentDao = Mockito.mock(ContractsGrantsInvoiceDocumentDao.class);
        Mockito.when(invoiceDocumentDao.getMatchingInvoicesByCollection(Mockito.anyMap()))
                .then(this::findInvoices);
        return invoiceDocumentDao;
    }

    protected Collection<ContractsGrantsInvoiceDocument> findInvoices(InvocationOnMock invocation) {
        Map<String, String> criteria = invocation.getArgument(0);
        String documentNumberCriteria = criteria.get(KFSPropertyConstants.DOCUMENT_NUMBER);
        String documentNumber = StringUtils.substringAfter(documentNumberCriteria, SearchOperator.NOT.op());
        String proposalNumber = criteria.get(ArPropertyConstants.ContractsGrantsInvoiceDocumentFields.PROPOSAL_NUMBER);
        String detailChartCode = criteria.get(
                CuArPropertyConstants.ContractsGrantsInvoiceAccountDetailFields.CHART_OF_ACCOUNTS_CODE);
        String detailAccountNumber = criteria.get(
                CuArPropertyConstants.ContractsGrantsInvoiceAccountDetailFields.ACCOUNT_NUMBER);
        String ccChartCode = criteria.get(
                CuArPropertyConstants.ContractsGrantsInvoiceAccountDetailFields.CONTRACT_CONTROL_CHART_OF_ACCOUNTS_CODE);
        String ccAccountNumber = criteria.get(
                CuArPropertyConstants.ContractsGrantsInvoiceAccountDetailFields.CONTRACT_CONTROL_ACCOUNT_NUMBER);
        
        assertExpectedDocumentStatusCriteriaArePresent(criteria);
        assertDocumentNumberCriteriaHasCorrectPrefix(documentNumberCriteria);
        assertAccountCriteriaPresenceIsExpectedForDocument(documentNumber, detailChartCode, detailAccountNumber);
        assertContractControlAccountCriteriaPresenceIsExpectedForDocument(documentNumber, ccChartCode, ccAccountNumber);
        
        return Arrays.stream(ContractsGrantsInvoiceDocumentFixture.values())
                .filter(fixture -> StringUtils.equals(proposalNumber, fixture.awardFixture.proposalNumber))
                .filter(fixture -> !StringUtils.equals(documentNumber, fixture.documentNumber))
                .filter(fixture -> documentHasAccountOrAccountCriteriaNotSpecified(fixture, detailChartCode, detailAccountNumber))
                .filter(fixture -> documentHasCCAccountOrCCAccountCriteriaNotSpecified(fixture, ccChartCode, ccAccountNumber))
                .map(this::buildMockContractsGrantsInvoiceDocument)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    protected boolean documentHasAccountOrAccountCriteriaNotSpecified(
            ContractsGrantsInvoiceDocumentFixture fixture, String detailChartCode, String detailAccountNumber) {
        if (StringUtils.isBlank(detailChartCode) || StringUtils.isBlank(detailAccountNumber)) {
            return true;
        }
        return fixture.accountDetails.stream()
                .anyMatch(accountFixture -> accountFixture.hasChartAndAccount(detailChartCode, detailAccountNumber));
    }

    protected boolean documentHasCCAccountOrCCAccountCriteriaNotSpecified(
            ContractsGrantsInvoiceDocumentFixture fixture, String ccChartCode, String ccAccountNumber) {
        if (StringUtils.isBlank(ccChartCode) || StringUtils.isBlank(ccAccountNumber)) {
            return true;
        }
        return fixture.accountDetails.stream()
                .anyMatch(accountFixture -> accountFixture.hasContractControlChartAndAccount(ccChartCode, ccAccountNumber));
    }

    protected ContractsGrantsInvoiceDocument buildMockContractsGrantsInvoiceDocument(ContractsGrantsInvoiceDocumentFixture fixture) {
        Supplier<ContractsGrantsInvoiceDocument> mockDocumentGenerator = () -> PowerMockito.spy(new ContractsGrantsInvoiceDocument());
        return fixture.toMockContractsGrantsInvoiceDocument(mockDocumentGenerator);
    }

    protected void assertExpectedDocumentStatusCriteriaArePresent(Map<String, String> criteria) {
        Set<String> expectedStatusCriteria = financialSystemDocumentService.getSuccessfulDocumentStatuses();
        String actualStatusCriteriaString = criteria.get(
                KFSPropertyConstants.DOCUMENT_HEADER + KFSConstants.DELIMITER + KFSPropertyConstants.WORKFLOW_DOCUMENT_STATUS_CODE);
        assertNotNull("Document status criteria should have been present", actualStatusCriteriaString);
        String[] actualStatusCriteriaArray = StringUtils.split(actualStatusCriteriaString, SearchOperator.OR.op());
        Set<String> actualStatusCriteria = new HashSet<>(Arrays.asList(actualStatusCriteriaArray));
        assertEquals("Wrong document status criteria were specified", expectedStatusCriteria, actualStatusCriteria);
    }

    protected void assertDocumentNumberCriteriaHasCorrectPrefix(String documentNumberCriteria) {
        assertTrue("documentNumber criteria did not start with the NOT operator '" + SearchOperator.NOT.op() + "'",
                StringUtils.startsWith(documentNumberCriteria, SearchOperator.NOT.op()));
    }

    protected void assertAccountCriteriaPresenceIsExpectedForDocument(
            String documentNumber, String detailChartCode, String detailAccountNumber) {
        ContractsGrantsInvoiceDocumentFixture fixture = ContractsGrantsInvoiceDocumentFixture.getFixtureByDocumentNumber(documentNumber);
        switch (fixture.awardFixture.invoicingOptionCode) {
            case ArConstants.INV_ACCOUNT :
            case ArConstants.INV_SCHEDULE :
                assertTrue("Chart criteria should be present when invoicing by Account or Schedule for doc " + fixture.name(),
                        StringUtils.isNotBlank(detailChartCode));
                assertTrue("Account Number criteria should be present when invoicing by Account or Schedule for doc " + fixture.name(),
                        StringUtils.isNotBlank(detailAccountNumber));
                break;
            default :
                assertTrue("Chart criteria should be absent when not invoicing by Account or Schedule for doc " + fixture.name(),
                        StringUtils.isBlank(detailChartCode));
                assertTrue("Account Number criteria should be absent when not invoicing by Account or Schedule for doc " + fixture.name(),
                        StringUtils.isBlank(detailAccountNumber));
                break;
        }
    }

    protected void assertContractControlAccountCriteriaPresenceIsExpectedForDocument(
            String documentNumber, String ccChartCode, String ccAccountNumber) {
        ContractsGrantsInvoiceDocumentFixture fixture = ContractsGrantsInvoiceDocumentFixture.getFixtureByDocumentNumber(documentNumber);
        if (StringUtils.equals(ArConstants.INV_CONTRACT_CONTROL_ACCOUNT, fixture.awardFixture.invoicingOptionCode)) {
            assertTrue("CC Chart criteria should be present when invoicing by Contract Control Account for doc " + fixture.name(),
                    StringUtils.isNotBlank(ccChartCode));
            assertTrue("CC Account Number criteria should be present when invoicing Contract Control Account for doc " + fixture.name(),
                    StringUtils.isNotBlank(ccAccountNumber));
        } else {
            assertTrue("CC Chart criteria should be absent when not invoicing by Contract Control Account for doc " + fixture.name(),
                    StringUtils.isBlank(ccChartCode));
            assertTrue("CC Account Number criteria should be absent when not invoicing by Contract Control Account for doc " + fixture.name(),
                    StringUtils.isBlank(ccAccountNumber));
        }
    }

    @Test
    public void testShouldSuspendWhenInvoicingByAwardAndDocumentIsFirstInvoiceForAward() throws Exception {
        assertDocumentSuspends(ContractsGrantsInvoiceDocumentFixture.DOCUMENT_3_AWARD_66666);
    }

    @Test
    public void testShouldNotSuspendWhenInvoicingByAwardAndOtherSuccessfulInvoicesExistForAward() throws Exception {
        assertDocumentDoesNotSuspend(ContractsGrantsInvoiceDocumentFixture.DOCUMENT_2_AWARD_12345);
        assertDocumentDoesNotSuspend(ContractsGrantsInvoiceDocumentFixture.DOCUMENT_5_AWARD_97979);
    }

    @Test
    public void testShouldSuspendWhenInvoicingByAccountAndDocumentIsFirstInvoiceForAwardAccountCombo() throws Exception {
        assertDocumentSuspends(ContractsGrantsInvoiceDocumentFixture.DOCUMENT_11_AWARD_11114);
    }

    @Test
    public void testShouldNotSuspendWhenInvoicingByAccountAndOtherSuccessfulInvoicesExistForAwardAccountCombo() throws Exception {
        assertDocumentDoesNotSuspend(ContractsGrantsInvoiceDocumentFixture.DOCUMENT_7_AWARD_30000);
        assertDocumentDoesNotSuspend(ContractsGrantsInvoiceDocumentFixture.DOCUMENT_9_AWARD_11114);
    }

    @Test
    public void testShouldSuspendWhenInvoicingByScheduleAndDocumentIsFirstInvoiceForAwardAccountCombo() throws Exception {
        assertDocumentSuspends(ContractsGrantsInvoiceDocumentFixture.DOCUMENT_16_AWARD_97531);
    }

    @Test
    public void testShouldNotSuspendWhenInvoicingByScheduleAndOtherSuccessfulInvoicesExistForAwardAccountCombo() throws Exception {
        assertDocumentDoesNotSuspend(ContractsGrantsInvoiceDocumentFixture.DOCUMENT_13_AWARD_24680);
        assertDocumentDoesNotSuspend(ContractsGrantsInvoiceDocumentFixture.DOCUMENT_15_AWARD_97531);
    }

    @Test
    public void testShouldSuspendWhenInvoicingByCCAccountAndDocumentIsFirstInvoiceForAwardCCAccountCombo() throws Exception {
        assertDocumentSuspends(ContractsGrantsInvoiceDocumentFixture.DOCUMENT_20_AWARD_99899);
    }

    @Test
    public void testShouldNotSuspendWhenInvoicingByCCAccountAndOtherSuccessfulInvoicesExistForAwardCCAccountCombo() throws Exception {
        assertDocumentDoesNotSuspend(ContractsGrantsInvoiceDocumentFixture.DOCUMENT_17_AWARD_33433);
        assertDocumentDoesNotSuspend(ContractsGrantsInvoiceDocumentFixture.DOCUMENT_21_AWARD_99899);
    }

    protected void assertDocumentSuspends(ContractsGrantsInvoiceDocumentFixture fixture) {
        assertDocumentProducesExpectedSuspendResult(true, fixture);
    }

    protected void assertDocumentDoesNotSuspend(ContractsGrantsInvoiceDocumentFixture fixture) {
        assertDocumentProducesExpectedSuspendResult(false, fixture);
    }

    protected void assertDocumentProducesExpectedSuspendResult(boolean expectedSuspendResult, ContractsGrantsInvoiceDocumentFixture fixture) {
        ContractsGrantsInvoiceDocument testDocument = buildMockContractsGrantsInvoiceDocument(fixture);
        boolean actualSuspendResult = suspensionCategory.shouldSuspend(testDocument);
        assertEquals("Wrong suspension result for document " + fixture.name(), expectedSuspendResult, actualSuspendResult);
    }

}
