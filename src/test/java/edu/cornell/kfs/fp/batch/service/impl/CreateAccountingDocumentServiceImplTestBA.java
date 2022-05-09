package edu.cornell.kfs.fp.batch.service.impl;

import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.fp.batch.xml.fixture.AccountingDocumentMapping;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentListWrapperFixture;

public class CreateAccountingDocumentServiceImplTestBA extends CreateAccountingDocumentServiceImplBase {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        createAccountingDocumentService = new TestCreateAccountingDocumentServiceImpl(buildMockPersonService(),
                buildAccountingXmlDocumentDownloadAttachmentService(), configurationService,
                buildMockFiscalYearFunctionControlService(), buildMockUniversityDateService(), dateTimeService);
        createAccountingDocumentService.initializeDocumentGeneratorsFromMappings(AccountingDocumentMapping.BA_DOCUMENT,
                AccountingDocumentMapping.YEBA_DOCUMENT);
        setupBasicCreateAccountingDocumentServices();
    }

    @Test
    public void testLoadSingleFileWithSingleBADocument() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-ba-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_BA_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithSingleBADocumentLackingBAAccountProperties() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-ba-no-base-or-months-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_BA_NO_BASEAMOUNT_OR_MONTHS_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithSingleBADocumentUsingNonZeroBaseAmounts() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-ba-nonzero-base-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_BA_NONZERO_BASEAMOUNT_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithSingleBADocumentUsingMultipleMonthAmounts() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-ba-multi-months-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_BA_MULTI_MONTHS_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleBADocuments() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-ba-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_BA_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleBADocumentsPlusDocumentsWithRulesFailures() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-ba-plus-bad-rules-doc-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_BA_DOCUMENT_WITH_SOME_BAD_RULES_DOCUMENTS_TEST);
    }
    
    @Test
    public void testLoadSingleFileWithSingleYEBADocument() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-yeba-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_YEBA_DOCUMENT_TEST);
    }
    
    @Test
    public void testLoadSingleFileWithMutliYEBADocument() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-yeba-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MUTLI_YEBA_DOCUMENT_TEST);
    }
}
