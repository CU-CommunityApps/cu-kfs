package edu.cornell.kfs.module.ar.document.validation.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kuali.kfs.krad.document.DocumentBase;
import org.kuali.kfs.module.ar.ArPropertyConstants;
import org.kuali.kfs.module.ar.businessobject.InvoiceGeneralDetail;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.document.dataaccess.ContractsGrantsInvoiceDocumentDao;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.document.service.FinancialSystemDocumentService;
import org.kuali.kfs.sys.document.service.impl.FinancialSystemDocumentServiceImpl;
import org.kuali.rice.core.api.search.SearchOperator;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import edu.cornell.kfs.module.ar.document.fixture.ContractsGrantsInvoiceDocumentFixture;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ContractsGrantsInvoiceDocument.class})
public class FirstInvoiceForAwardSuspensionCategoryTest {

    private FinancialSystemDocumentService financialSystemDocumentService;
    private FirstInvoiceForAwardSuspensionCategory suspensionCategory;
    private EnumMap<ContractsGrantsInvoiceDocumentFixture, ContractsGrantsInvoiceDocument> testDocuments;

    @Before
    public void setUp() throws Exception {
        initializeDocumentMappings();
        financialSystemDocumentService = new FinancialSystemDocumentServiceImpl();
        suspensionCategory = new FirstInvoiceForAwardSuspensionCategory();
        suspensionCategory.setFinancialSystemDocumentService(financialSystemDocumentService);
        suspensionCategory.setContractsGrantsInvoiceDocumentDao(buildMockContractsGrantsInvoiceDocumentDao());
    }

    protected void initializeDocumentMappings() {
        testDocuments = new EnumMap<>(ContractsGrantsInvoiceDocumentFixture.class);
        for (ContractsGrantsInvoiceDocumentFixture fixture : getTestFixtures()) {
            testDocuments.put(fixture, buildMockInvoiceDocument(fixture));
        }
    }

    protected List<ContractsGrantsInvoiceDocumentFixture> getTestFixtures() {
        return Arrays.asList(ContractsGrantsInvoiceDocumentFixture.DOCUMENT_1_PROPOSAL_12345,
                ContractsGrantsInvoiceDocumentFixture.DOCUMENT_2_PROPOSAL_12345,
                ContractsGrantsInvoiceDocumentFixture.DOCUMENT_3_PROPOSAL_66666,
                ContractsGrantsInvoiceDocumentFixture.DOCUMENT_4_PROPOSAL_97979,
                ContractsGrantsInvoiceDocumentFixture.DOCUMENT_5_PROPOSAL_97979,
                ContractsGrantsInvoiceDocumentFixture.DOCUMENT_6_PROPOSAL_97979);
    }

    protected ContractsGrantsInvoiceDocument buildMockInvoiceDocument(ContractsGrantsInvoiceDocumentFixture fixture) {
        PowerMockito.suppress(PowerMockito.constructor(DocumentBase.class));
        ContractsGrantsInvoiceDocument document = PowerMockito.spy(new ContractsGrantsInvoiceDocument());
        InvoiceGeneralDetail invoiceGeneralDetail = new InvoiceGeneralDetail();
        invoiceGeneralDetail.setProposalNumber(fixture.proposalNumber);
        document.setInvoiceGeneralDetail(invoiceGeneralDetail);
        document.setDocumentNumber(fixture.documentNumber);
        return document;
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
        
        assertExpectedDocumentStatusCriteriaArePresent(criteria);
        assertDocumentNumberCriteriaHasCorrectPrefix(documentNumberCriteria);
        
        return testDocuments.values().stream()
                .filter(document -> StringUtils.equals(proposalNumber, document.getInvoiceGeneralDetail().getProposalNumber()))
                .filter(document -> !StringUtils.equals(documentNumber, document.getDocumentNumber()))
                .collect(Collectors.toCollection(ArrayList::new));
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

    @Test
    public void testShouldSuspendWhenDocumentIsFirstInvoiceForProposal() throws Exception {
        assertDocumentProducesExpectedSuspendResult(true, ContractsGrantsInvoiceDocumentFixture.DOCUMENT_3_PROPOSAL_66666);
    }

    @Test
    public void testShouldNotSuspendWhenOneOtherSuccessfulInvoiceExistsForProposal() throws Exception {
        assertDocumentProducesExpectedSuspendResult(false, ContractsGrantsInvoiceDocumentFixture.DOCUMENT_2_PROPOSAL_12345);
    }

    @Test
    public void testShouldNotSuspendWhenMultipleOtherSuccessfulInvoicesExistForProposal() throws Exception {
        assertDocumentProducesExpectedSuspendResult(false, ContractsGrantsInvoiceDocumentFixture.DOCUMENT_5_PROPOSAL_97979);
    }

    protected void assertDocumentProducesExpectedSuspendResult(boolean expectedSuspendResult, ContractsGrantsInvoiceDocumentFixture fixture) {
        ContractsGrantsInvoiceDocument testDocument = testDocuments.get(fixture);
        boolean actualSuspendResult = suspensionCategory.shouldSuspend(testDocument);
        assertEquals("Wrong suspension result for document " + fixture.name(), expectedSuspendResult, actualSuspendResult);
    }

}
