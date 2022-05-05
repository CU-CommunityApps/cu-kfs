package edu.cornell.kfs.fp.batch.service.impl;

import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.fp.batch.xml.fixture.AccountingDocumentMapping;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentListWrapperFixture;
import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherDefaultDueDateService;

public class CreateAccountingDocumentServiceImplTestMultiple extends CreateAccountingDocumentServiceImplTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        CuDisbursementVoucherDefaultDueDateService cuDisbursementVoucherDefaultDueDateService = buildCuDisbursementVoucherDefaultDueDateService();
        createAccountingDocumentService = new TestCreateAccountingDocumentServiceImpl(buildMockPersonService(),
                buildAccountingXmlDocumentDownloadAttachmentService(), configurationService,
                buildMockFiscalYearFunctionControlService(), buildMockDisbursementVoucherTravelService(),
                buildMockUniversityDateService(), buildAccountingPeriodService(), dateTimeService,
                cuDisbursementVoucherDefaultDueDateService, buildCuDisbursementVoucherPayeeService(),
                buildMockVendorService());
        createAccountingDocumentService.initializeDocumentGeneratorsFromMappings(AccountingDocumentMapping.DI_DOCUMENT,
                AccountingDocumentMapping.IB_DOCUMENT, AccountingDocumentMapping.TF_DOCUMENT,
                AccountingDocumentMapping.BA_DOCUMENT, AccountingDocumentMapping.SB_DOCUMENT,
                AccountingDocumentMapping.YEDI_DOCUMENT, AccountingDocumentMapping.DV_DOCUMENT,
                AccountingDocumentMapping.YEBA_DOCUMENT, AccountingDocumentMapping.YETF_DOCUMENT,
                AccountingDocumentMapping.AV_DOCUMENT);
        setupBasicCreateAccountingDocumentServices();
    }

    @Test
    public void testLoadSingleFileWithMultipleDocumentTypes() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-doc-types-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_DOCUMENT_TYPES_TEST);
    }

    @Test
    public void testLoadSingleFileWithDIDocumentContainingIBItemsToIgnore() throws Exception {
        copyTestFilesAndCreateDoneFiles("di-with-ib-items-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.DI_WITH_IB_ITEMS_TEST);
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
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(AccountingXmlDocumentListWrapperFixture.BAD_XML_DOCUMENT_TEST, 
                AccountingXmlDocumentListWrapperFixture.EMPTY_DOCUMENT_TEST, AccountingXmlDocumentListWrapperFixture.MULTI_YEDI_DOCUMENT_TEST);
    }
    
    @Test
    public void testLoadSingleFileWithZeroDocuments() throws Exception {
        copyTestFilesAndCreateDoneFiles("empty-document-list-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(AccountingXmlDocumentListWrapperFixture.EMPTY_DOCUMENT_LIST_TEST);
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
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(AccountingXmlDocumentListWrapperFixture.BAD_XML_DOCUMENT_TEST);
    }

}
