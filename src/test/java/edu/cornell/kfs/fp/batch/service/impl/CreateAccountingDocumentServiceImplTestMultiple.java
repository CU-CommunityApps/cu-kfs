package edu.cornell.kfs.fp.batch.service.impl;

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
    
}
