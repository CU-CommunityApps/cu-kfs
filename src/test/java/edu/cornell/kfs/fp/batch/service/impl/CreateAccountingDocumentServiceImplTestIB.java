package edu.cornell.kfs.fp.batch.service.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.fp.batch.xml.fixture.AccountingDocumentMapping;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentListWrapperFixture;

public class CreateAccountingDocumentServiceImplTestIB extends CreateAccountingDocumentServiceImplTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        createAccountingDocumentService = new TestCreateAccountingDocumentServiceImpl(buildMockPersonService(),
                buildAccountingXmlDocumentDownloadAttachmentService(), configurationService,
                buildMockUniversityDateService(), dateTimeService);
        createAccountingDocumentService.initializeDocumentGeneratorsFromMappings(AccountingDocumentMapping.IB_DOCUMENT);
        setupBasicCreateAccountingDocumentServices();
    }

    @Test
    public void testLoadSingleFileWithSingleIBDocument() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-ib-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_IB_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithSingleIBDocumentLackingItems() throws Exception {
        copyTestFilesAndCreateDoneFiles("ib-without-items-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_IB_DOCUMENT_NO_ITEMS_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleIBDocuments() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-ib-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_IB_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleIBDocumentsPlusDocumentWithRulesFailure() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-ib-plus-bad-rules-doc-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_IB_DOCUMENT_WITH_BAD_RULES_THIRD_DOCUMENT_TEST);
    }
    
    
}
