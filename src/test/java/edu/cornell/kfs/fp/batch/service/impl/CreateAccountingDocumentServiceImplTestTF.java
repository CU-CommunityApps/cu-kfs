package edu.cornell.kfs.fp.batch.service.impl;

import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.fp.batch.xml.fixture.AccountingDocumentMapping;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentListWrapperFixture;

public class CreateAccountingDocumentServiceImplTestTF extends CreateAccountingDocumentServiceImplBase {
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        createAccountingDocumentService = new TestCreateAccountingDocumentServiceImpl(buildMockPersonService(),
                buildAccountingXmlDocumentDownloadAttachmentService(), configurationService,
                buildMockUniversityDateService(), dateTimeService);
        createAccountingDocumentService.initializeDocumentGeneratorsFromMappings(AccountingDocumentMapping.TF_DOCUMENT,
                AccountingDocumentMapping.YETF_DOCUMENT);
        setupBasicCreateAccountingDocumentServices();
    }
    
    @Test
    public void testLoadSingleTFDocument() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-tf-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.TF_DOCUMENT_TEST);
    }
    
    @Test
    public void testLoadMultipleTFDocument() throws Exception {
        copyTestFilesAndCreateDoneFiles("multiple-tf-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.TF_DOCUMENT_TEST2);
    }
    
    @Test
    public void testLoadSingleYETFDocument() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-yetf-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_YETF_DOCUMENT_TEST);
    }
    

}
