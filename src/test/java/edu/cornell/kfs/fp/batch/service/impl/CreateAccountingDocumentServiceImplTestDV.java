package edu.cornell.kfs.fp.batch.service.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.fp.batch.xml.fixture.AccountingDocumentMapping;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentListWrapperFixture;
import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherDefaultDueDateService;

public class CreateAccountingDocumentServiceImplTestDV extends CreateAccountingDocumentServiceImplTest {
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        CuDisbursementVoucherDefaultDueDateService cuDisbursementVoucherDefaultDueDateService = buildCuDisbursementVoucherDefaultDueDateService();
        createAccountingDocumentService = new TestCreateAccountingDocumentServiceImpl(
                buildMockPersonService(), buildAccountingXmlDocumentDownloadAttachmentService(),
                configurationService, buildMockFiscalYearFunctionControlService(), buildMockDisbursementVoucherTravelService(), buildMockUniversityDateService(),
                buildAccountingPeriodService(), dateTimeService, cuDisbursementVoucherDefaultDueDateService,
                buildCuDisbursementVoucherPayeeService(), buildMockVendorService());
        createAccountingDocumentService.initializeDocumentGeneratorsFromMappings(AccountingDocumentMapping.DV_DOCUMENT,
                AccountingDocumentMapping.YEDI_DOCUMENT);
        setupBasicCreateAccountingDocumentServices();
    }
    
    @Test
    public void testDvDocumentTest() throws Exception {
        copyTestFilesAndCreateDoneFiles("dv-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.DV_DOCUMENT_TEST);
    }

    @Test
    public void testDvDocumentWithVendorPayees() throws Exception {
        copyTestFilesAndCreateDoneFiles("dv-document-vendor-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.DV_DOCUMENT_VENDOR_TEST);
    }

}
