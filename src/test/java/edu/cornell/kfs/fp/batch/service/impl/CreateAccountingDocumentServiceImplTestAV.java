package edu.cornell.kfs.fp.batch.service.impl;

import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.fp.batch.xml.fixture.AccountingDocumentMapping;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentListWrapperFixture;

public class CreateAccountingDocumentServiceImplTestAV extends CreateAccountingDocumentServiceImplBase {
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        createAccountingDocumentService = new TestCreateAccountingDocumentServiceImpl(buildMockPersonService(),
                buildAccountingXmlDocumentDownloadAttachmentService(), configurationService,
                buildMockFiscalYearFunctionControlService(), buildMockUniversityDateService(), 
                buildAccountingPeriodService(), dateTimeService);
        createAccountingDocumentService.initializeDocumentGeneratorsFromMappings(AccountingDocumentMapping.AV_DOCUMENT,
                AccountingDocumentMapping.YEDI_DOCUMENT);
        setupBasicCreateAccountingDocumentServices();
    }

    @Test
    public void testLoadSingleFileWithSingleAVDocument() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-av-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_AV_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleAVDocuments() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-av-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_AV_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleAVDocumentsPlusDocumentsWithRulesFailures() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-av-plus-bad-rules-doc-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_AV_DOCUMENT_WITH_SOME_BAD_RULES_DOCUMENTS_TEST);
    }
}
