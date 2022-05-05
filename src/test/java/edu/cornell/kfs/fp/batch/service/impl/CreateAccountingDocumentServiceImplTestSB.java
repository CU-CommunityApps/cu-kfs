package edu.cornell.kfs.fp.batch.service.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.fp.batch.xml.fixture.AccountingDocumentMapping;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentListWrapperFixture;

public class CreateAccountingDocumentServiceImplTestSB extends CreateAccountingDocumentServiceImplTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        createAccountingDocumentService = new TestCreateAccountingDocumentServiceImpl(buildMockPersonService(),
                buildAccountingXmlDocumentDownloadAttachmentService(), configurationService,
                buildMockUniversityDateService(), dateTimeService);
        createAccountingDocumentService.initializeDocumentGeneratorsFromMappings(AccountingDocumentMapping.SB_DOCUMENT);
        setupBasicCreateAccountingDocumentServices();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testLoadSingleFileWithSingleSBDocument() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-sb-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_SB_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithSingleSBDocumentLackingItems() throws Exception {
        copyTestFilesAndCreateDoneFiles("sb-without-items-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_SB_DOCUMENT_NO_ITEMS_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleSBDocuments() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-sb-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_SB_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleSBDocumentsPlusDocumentWithRulesFailure() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-sb-plus-bad-rules-doc-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_SB_DOCUMENT_WITH_BAD_RULES_THIRD_DOCUMENT_TEST);
    }

    // @Test
    public void testLoadSingleFileWithMultipleSBDocumentsPlusDocumentWithBadDateValues() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-sb-plus-bad-date-values-doc-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_SB_DOCUMENT_WITH_BAD_DATE_VALUES_DOCUMENT_TEST);
    }

}
