package edu.cornell.kfs.fp.batch.service.impl;

import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.fp.batch.xml.fixture.AccountingDocumentMapping;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentListWrapperFixture;

public class CreateAccountingDocumentServiceImplTestDI extends CreateAccountingDocumentServiceImplBase {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        createAccountingDocumentService = new TestCreateAccountingDocumentServiceImpl(buildMockPersonService(),
                buildAccountingXmlDocumentDownloadAttachmentService(), configurationService,
                buildMockUniversityDateService(), dateTimeService);
        createAccountingDocumentService.initializeDocumentGeneratorsFromMappings(AccountingDocumentMapping.DI_DOCUMENT,
                AccountingDocumentMapping.YEDI_DOCUMENT);
        setupBasicCreateAccountingDocumentServices();
    }

    @Test
    public void testLoadSingleFileWithSingleDIDocument() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-di-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_DI_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleDIDocuments() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-di-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_DI_DOCUMENT_TEST);
    }

    @Test
    public void testLoadMultipleFilesWithDIDocuments() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-di-document-test", "multi-di-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_DI_DOCUMENT_TEST,
                AccountingXmlDocumentListWrapperFixture.MULTI_DI_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleDIDocumentsPlusDocumentWithInvalidDocType() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-di-plus-invalid-doc-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_DI_DOCUMENT_WITH_BAD_CONVERSION_SECOND_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleDIDocumentsPlusDocumentWithRulesFailure() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-di-plus-bad-rules-doc-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_DI_DOCUMENT_WITH_BAD_RULES_FIRST_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleDIDocumentsPlusDocumentsWithBadAttachments() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-di-plus-bad-attachments-doc-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_DI_DOCUMENT_WITH_BAD_ATTACHMENTS_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileForDIDocumentWithBadAmount() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-di-bad-amount-doc-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_DI_DOCUMENT_WITH_BAD_AMOUNT_TEST);
    }

    @Test
    public void testLoadSingleFileWithSingleDIDocumentWithBadAttachmentUrl() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-di-bad-attach-document-test");
        TestAccountingXmlDocumentDownloadAttachmentService attachService = (TestAccountingXmlDocumentDownloadAttachmentService) createAccountingDocumentService.downloadAttachmentService;
        attachService.setForceUseOfRealClientToTestAttachmentUrls(true);
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_DI_DOCUMENT_WITH_BAD_ATTACHMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithSingleYEDIDocument() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-yedi-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_YEDI_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleYEDIDocuments() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-yedi-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_YEDI_DOCUMENT_TEST);
    }

    @Test
    public void testLoadMultipleFilesWithYEDIDocuments() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-yedi-document-test", "multi-yedi-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_YEDI_DOCUMENT_TEST,
                AccountingXmlDocumentListWrapperFixture.MULTI_YEDI_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleYEDIDocumentsPlusDocumentWithInvalidDocType() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-yedi-plus-invalid-doc-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_YEDI_DOCUMENT_WITH_BAD_CONVERSION_SECOND_DOCUMENT_TEST);
    }

    @Test
    public void testServiceError() throws Exception {
        createAccountingDocumentService.setFailToCreateDocument(true);

        copyTestFilesAndCreateDoneFiles("single-yedi-document-test");
        boolean results = createAccountingDocumentService.createAccountingDocumentsFromXml();
        assertFalse("When there is a problem calling services, the job should fail", results);
    }

    @Test
    public void testBadXmlEmptyFileWithGoodFile() throws Exception {
        copyTestFilesAndCreateDoneFiles("bad-xml-test", "empty-file-test", "multi-yedi-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.BAD_XML_DOCUMENT_TEST,
                AccountingXmlDocumentListWrapperFixture.EMPTY_DOCUMENT_TEST,
                AccountingXmlDocumentListWrapperFixture.MULTI_YEDI_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithZeroDocuments() throws Exception {
        copyTestFilesAndCreateDoneFiles("empty-document-list-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.EMPTY_DOCUMENT_LIST_TEST);
    }

    @Test
    public void testEmptyFile() throws Exception {
        copyTestFilesAndCreateDoneFiles("empty-file-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(AccountingXmlDocumentListWrapperFixture.EMPTY_DOCUMENT_TEST);
    }

    @Test
    public void testEmptyFileWithGoodFile() throws Exception {
        copyTestFilesAndCreateDoneFiles("empty-file-test", "multi-yedi-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(AccountingXmlDocumentListWrapperFixture.EMPTY_DOCUMENT_TEST,
                AccountingXmlDocumentListWrapperFixture.MULTI_YEDI_DOCUMENT_TEST);
    }

    @Test
    public void testBadXmlFile() throws Exception {
        copyTestFilesAndCreateDoneFiles("bad-xml-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.BAD_XML_DOCUMENT_TEST);
    }

}
