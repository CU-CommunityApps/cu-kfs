package edu.cornell.kfs.fp.batch.service.impl;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.mockito.Mockito;

import edu.cornell.kfs.fp.batch.service.AccountingDocumentGenerator;
import edu.cornell.kfs.fp.batch.service.AccountingXmlDocumentDownloadAttachmentService;
import edu.cornell.kfs.fp.batch.service.impl.fixture.DocGenVendorFixture;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingDocumentMapping;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentListWrapperFixture;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherDefaultDueDateService;
import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherPayeeService;
import edu.cornell.kfs.fp.document.service.impl.CuDisbursementVoucherPayeeServiceImpl;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CreateAccountingDocumentServiceImplTestDV extends CreateAccountingDocumentServiceImplTestBase {

    private SimpleDateFormat dateFormat;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        dateFormat = new SimpleDateFormat(CUKFSConstants.DATE_FORMAT_yyyyMMdd, Locale.US);
        CuDisbursementVoucherDefaultDueDateService cuDisbursementVoucherDefaultDueDateService = buildCuDisbursementVoucherDefaultDueDateService();
        createAccountingDocumentService = new DvTestCreateAccountingDocumentServiceImpl(buildMockPersonService(),
                buildAccountingXmlDocumentDownloadAttachmentService(), configurationService,
                buildMockUniversityDateService(), dateTimeService, cuDisbursementVoucherDefaultDueDateService,
                buildCuDisbursementVoucherPayeeService(), buildMockVendorService());
        createAccountingDocumentService.initializeDocumentGeneratorsFromMappings(AccountingDocumentMapping.DV_DOCUMENT);
        setupBasicCreateAccountingDocumentServices();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        dateFormat = null;
    }

    @Test
    public void testDvDocumentTest() throws Exception {
        copyTestFilesAndCreateDoneFiles("dv-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(AccountingXmlDocumentListWrapperFixture.DV_DOCUMENT_TEST);
    }

    @Test
    public void testDvDocumentWithVendorPayees() throws Exception {
        copyTestFilesAndCreateDoneFiles("dv-document-vendor-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.DV_DOCUMENT_VENDOR_TEST);
    }

    protected void assertAccountingDocumentIsCorrect(Class<? extends AccountingDocument> documentClass,
            AccountingDocument expectedDocument, AccountingDocument actualDocument) {
        super.assertAccountingDocumentIsCorrect(documentClass, expectedDocument, actualDocument);

        assertCuDisbursementVoucherDocumentsCorrect((CuDisbursementVoucherDocument) expectedDocument,
                (CuDisbursementVoucherDocument) actualDocument);
    }

    private void assertCuDisbursementVoucherDocumentsCorrect(CuDisbursementVoucherDocument expectedDvDocument,
            CuDisbursementVoucherDocument actualDvDocument) {
        assertEquals("Wrong bank code", expectedDvDocument.getDisbVchrBankCode(),
                actualDvDocument.getDisbVchrBankCode());
        assertEquals("Wrong contact name", expectedDvDocument.getDisbVchrContactPersonName(),
                actualDvDocument.getDisbVchrContactPersonName());
        assertEquals("payment reason code not correct",
                expectedDvDocument.getDvPayeeDetail().getDisbVchrPaymentReasonCode(),
                actualDvDocument.getDvPayeeDetail().getDisbVchrPaymentReasonCode());
        assertEquals("payee type code not correct",
                expectedDvDocument.getDvPayeeDetail().getDisbursementVoucherPayeeTypeCode(),
                actualDvDocument.getDvPayeeDetail().getDisbursementVoucherPayeeTypeCode());
        assertEquals("conference destination not correct",
                expectedDvDocument.getDvPreConferenceDetail().getDvConferenceDestinationName(),
                actualDvDocument.getDvPreConferenceDetail().getDvConferenceDestinationName());
        assertEquals("Due Dates should match", dateFormat.format(expectedDvDocument.getDisbursementVoucherDueDate()),
                dateFormat.format(actualDvDocument.getDisbursementVoucherDueDate()));
        assertEquals("Invoice Dates should match", expectedDvDocument.getInvoiceDate(),
                actualDvDocument.getInvoiceDate());
        assertEquals("Invoice numbers should match", expectedDvDocument.getInvoiceNumber(),
                actualDvDocument.getInvoiceNumber());
    }

    private VendorService buildMockVendorService() {
        VendorService vendorService = Mockito.mock(VendorService.class);
        DocGenVendorFixture[] vendorFixtures = { DocGenVendorFixture.XYZ_INDUSTRIES, DocGenVendorFixture.REE_PHUND };
        for (DocGenVendorFixture vendorFixture : vendorFixtures) {
            Mockito.when(vendorService.getByVendorNumber(vendorFixture.getVendorNumber()))
                    .thenReturn(vendorFixture.createVendorDetail());
        }
        return vendorService;
    }

    private CuDisbursementVoucherDefaultDueDateService buildCuDisbursementVoucherDefaultDueDateService() {
        CuDisbursementVoucherDefaultDueDateService service = Mockito
                .mock(CuDisbursementVoucherDefaultDueDateService.class);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Mockito.when(service.findDefaultDueDate()).thenReturn(new java.sql.Date(calendar.getTimeInMillis()));
        return service;
    }

    private CuDisbursementVoucherPayeeService buildCuDisbursementVoucherPayeeService() {
        return new CuDisbursementVoucherPayeeServiceImpl();
    }

    protected static class DvTestCreateAccountingDocumentServiceImpl extends TestCreateAccountingDocumentServiceImpl {

        public DvTestCreateAccountingDocumentServiceImpl(PersonService personService,
                AccountingXmlDocumentDownloadAttachmentService downloadAttachmentService,
                ConfigurationService configurationService, UniversityDateService universityDateService,
                DateTimeService dateTimeService,
                CuDisbursementVoucherDefaultDueDateService cuDisbursementVoucherDefaultDueDateService,
                CuDisbursementVoucherPayeeService cuDisbursementVoucherPayeeService, VendorService vendorService) {
            super(personService, downloadAttachmentService, configurationService, universityDateService,
                    dateTimeService);
            this.cuDisbursementVoucherDefaultDueDateService = cuDisbursementVoucherDefaultDueDateService;
            this.cuDisbursementVoucherPayeeService = cuDisbursementVoucherPayeeService;
            this.vendorService = vendorService;
        }

        @Override
        protected AccountingDocumentGenerator<?> buildAccountingDocumentGenerator(
                BiFunction<Supplier<Note>, Supplier<AdHocRoutePerson>, AccountingDocumentGeneratorBase<?>> generatorConstructor) {
            CuDisbursementVoucherDocumentGenerator dvGenerator = (CuDisbursementVoucherDocumentGenerator) super.buildAccountingDocumentGenerator(
                    generatorConstructor);
            dvGenerator.setUniversityDateService(universityDateService);
            dvGenerator.setCuDisbursementVoucherDefaultDueDateService(cuDisbursementVoucherDefaultDueDateService);
            dvGenerator.setCuDisbursementVoucherPayeeService(cuDisbursementVoucherPayeeService);
            dvGenerator.setVendorService(vendorService);
            return dvGenerator;
        }
    }

}
