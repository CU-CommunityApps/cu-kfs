package edu.cornell.kfs.module.purap.document.service.impl;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kuali.kfs.kns.service.DataDictionaryService;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.bo.DocumentHeader;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtension;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.document.AccountsPayableDocumentBase;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.module.purap.document.service.AccountsPayableService;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.kfs.vnd.document.service.impl.VendorServiceImpl;
import org.kuali.kfs.vnd.fixture.VendorAddressFixture;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.core.impl.datetime.DateTimeServiceImpl;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kim.api.identity.Person;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import edu.cornell.kfs.module.purap.document.CuVendorCreditMemoDocument;
import edu.cornell.kfs.sys.util.MockPersonUtil;
import edu.cornell.kfs.vnd.fixture.VendorDetailExtensionFixture;
import edu.cornell.kfs.vnd.fixture.VendorHeaderFixture;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CuCreditMemoServiceImplTest.TestCuCreditMemoServiceImpl.class, AccountsPayableDocumentBase.class})
public class CuCreditMemoServiceImplTest {

    private AccountsPayableService accountsPayableService;
    private CuCreditMemoServiceImpl creditMemoServiceImpl;
    private DataDictionaryService dataDictionaryService;
    private DateTimeService dateTimeService;
    private DocumentService documentService;
    private NoteService noteService;
    private PurapService purapService;
    private VendorService vendorService;
    private CuVendorCreditMemoDocument creditMemoDocument;
    private static Person mo14Person;
    private static UserSession mo14Session;

    @Before
	public void setUp() throws Exception {
        accountsPayableService = Mockito.mock(AccountsPayableService.class);
        Mockito.when(accountsPayableService.getExpiredOrClosedAccountList(Mockito.any())).thenReturn(new HashMap<>());
        
        dataDictionaryService = Mockito.mock(DataDictionaryService.class);
        Mockito.when(dataDictionaryService.getAttributeMaxLength(DocumentHeader.class, KRADPropertyConstants.DOCUMENT_DESCRIPTION)).thenReturn(200);
        
        dateTimeService = new DateTimeServiceImpl();
        documentService = buildMockDocumentService();
        noteService = Mockito.mock(NoteService.class);
        purapService = Mockito.mock(PurapService.class);
        vendorService = new MockVendorServiceImpl();

        
        creditMemoServiceImpl = PowerMockito.spy(new TestCuCreditMemoServiceImpl());
        PowerMockito.doNothing().when(creditMemoServiceImpl, "reIndexDocument", Mockito.any());

        creditMemoServiceImpl.setDocumentService(documentService);
        creditMemoServiceImpl.setNoteService(noteService);
        creditMemoServiceImpl.setPurapService(purapService);
        creditMemoServiceImpl.setAccountsPayableService(accountsPayableService);
        creditMemoServiceImpl.setVendorService(vendorService);
        creditMemoServiceImpl.setDataDictionaryService(dataDictionaryService);

		creditMemoDocument = setupVendorCreditMemoDocument();

		mo14Person = MockPersonUtil.createMockPerson(UserNameFixture.mo14);
        mo14Session = MockPersonUtil.createMockUserSession(mo14Person);

        GlobalVariables.setUserSession(mo14Session);
	}
    
    private DocumentService buildMockDocumentService() {
        DocumentService mockDocumentService = Mockito.mock(DocumentService.class);
        Mockito.when(mockDocumentService.createNoteFromDocument(Mockito.any(Document.class), Mockito.anyString())).then(this::buildDocumentNote);
        return mockDocumentService;
    }
    private Note buildDocumentNote(InvocationOnMock invocation) {
        Document document = invocation.getArgument(0);
        String noteText = invocation.getArgument(1);
        
        Note note = Mockito.mock(Note.class);
        Mockito.when(note.getNotePostedTimestamp()).thenReturn(dateTimeService.getCurrentTimestamp());
        Mockito.when(note.getVersionNumber()).thenReturn(Long.valueOf(1L));
        Mockito.when(note.getNoteText()).thenReturn(noteText);
        Mockito.when(note.getNoteTypeCode()).thenReturn("TST");

        return note;
    }

    private CuVendorCreditMemoDocument setupVendorCreditMemoDocument() {
        PowerMockito.suppress(PowerMockito.constructor(AccountsPayableDocumentBase.class));
        creditMemoDocument = PowerMockito.spy(new TestableCuVendorCreditMemoDocument());

        creditMemoDocument.setDocumentHeader(new MockFinancialSystemDocumentHeader());
        creditMemoDocument.getDocumentHeader().setDocumentDescription("Description");
        creditMemoDocument.setVendorDetailAssignedIdentifier(0);
        creditMemoDocument.setVendorHeaderGeneratedIdentifier(4291);
        creditMemoDocument.setCreditMemoNumber("12345");
        creditMemoDocument.setCreditMemoDate(dateTimeService.getCurrentSqlDate());
        creditMemoDocument.setCreditMemoAmount(new KualiDecimal(100));

        return creditMemoDocument;
    }

    @Test
	public void testAddHoldOnCreditMemo() throws Exception {
        PowerMockito.doReturn(setupVendorCreditMemoDocument()).when(creditMemoServiceImpl, "getCreditMemoDocumentById", Mockito.any());
        PowerMockito.doNothing().when(creditMemoServiceImpl, "reIndexDocument", creditMemoDocument);
        creditMemoServiceImpl.addHoldOnCreditMemo(creditMemoDocument, "unit test");

		Assert.assertTrue(creditMemoDocument.isHoldIndicator());
		Assert.assertTrue(mo14Person.getPrincipalId().equalsIgnoreCase(creditMemoDocument.getLastActionPerformedByPersonId()));
	}

	@Test
    public void testRemoveHoldOnCreditMemo() throws Exception {
        PowerMockito.doReturn(setupVendorCreditMemoDocument()).when(creditMemoServiceImpl, "getCreditMemoDocumentById", Mockito.any());
        PowerMockito.doNothing().when(creditMemoServiceImpl, "reIndexDocument", creditMemoDocument);
        creditMemoServiceImpl.removeHoldOnCreditMemo(creditMemoDocument, "unit test");

        Assert.assertFalse(creditMemoDocument.isHoldIndicator());
        Assert.assertNull(creditMemoDocument.getLastActionPerformedByPersonId());
    }

	@Test
	public void testrResetExtractedCreditMemo_Successful() throws Exception {
		creditMemoDocument.setExtractedTimestamp(dateTimeService.getCurrentTimestamp());
		creditMemoServiceImpl.resetExtractedCreditMemo(creditMemoDocument, "unit test");

		Assert.assertNull(creditMemoDocument.getExtractedTimestamp());
		Assert.assertNull(creditMemoDocument.getCreditMemoPaidTimestamp());
	}

	@Test
	public void testrResetExtractedCreditMemo_Fail() throws Exception {
		creditMemoDocument.setApplicationDocumentStatus(PurapConstants.CreditMemoStatuses.APPDOC_CANCELLED_IN_PROCESS);
		creditMemoDocument.setExtractedTimestamp(dateTimeService.getCurrentTimestamp());
		creditMemoDocument.setCreditMemoPaidTimestamp(dateTimeService.getCurrentTimestamp());

		creditMemoServiceImpl.resetExtractedCreditMemo(creditMemoDocument, "unit test");

		Assert.assertNotNull(creditMemoDocument.getExtractedTimestamp());
		Assert.assertNotNull(creditMemoDocument.getCreditMemoPaidTimestamp());
	}

	@Test
	public void testMarkPaid() throws Exception {
		Date currentDate = dateTimeService.getCurrentSqlDate();
		Timestamp currentTimeStamp = new Timestamp(currentDate.getTime());
		creditMemoServiceImpl.markPaid(creditMemoDocument, currentDate);

		Assert.assertNotNull(creditMemoDocument.getCreditMemoPaidTimestamp());
		Assert.assertEquals(currentTimeStamp, creditMemoDocument.getCreditMemoPaidTimestamp());
	}

	@Test
	public void testPopulateDocumentAfterInit() throws WorkflowException {
		creditMemoServiceImpl.populateDocumentAfterInit(creditMemoDocument);

		Assert.assertNotNull(creditMemoDocument.getPaymentMethodCode());
		Assert.assertEquals("P", creditMemoDocument.getPaymentMethodCode());
	}

    class TestCuCreditMemoServiceImpl extends CuCreditMemoServiceImpl {
        @Override
        public VendorCreditMemoDocument getCreditMemoDocumentById(Integer purchasingDocumentIdentifier) {
            return setupVendorCreditMemoDocument();
        }
    }

    private class MockVendorDetail extends VendorDetail {
        @Override
        public PersistableBusinessObjectExtension getExtension() {
            return VendorDetailExtensionFixture.EXTENSION.createVendorDetailExtension();
        }
    }

    private class MockVendorServiceImpl extends VendorServiceImpl {
        @Override
        public VendorDetail getVendorDetail(Integer headerId, Integer detailId) {
            VendorDetail vendorDetail = new MockVendorDetail();
            vendorDetail.setVendorName("Anak Inc");
            vendorDetail.setVendorHeaderGeneratedIdentifier(4506);
            vendorDetail.setVendorDetailAssignedIdentifier(0);
            vendorDetail.setVendorParentIndicator(true);
            vendorDetail.setVendorHeader(VendorHeaderFixture.ONE.createVendorHeader());

            return vendorDetail;
        }

        @Override
        public VendorAddress getVendorDefaultAddress(Integer vendorHeaderId, Integer vendorDetailId, String addressType, String campus, boolean activeCheck) {
            return VendorAddressFixture.address1.createAddress();
        }
    }

    private class MockFinancialSystemDocumentHeader extends FinancialSystemDocumentHeader {
        private static final long serialVersionUID = 1L;

        @Override
        public void setApplicationDocumentStatus(String applicationDocumentStatus) {
            this.applicationDocumentStatus = applicationDocumentStatus;
        }

    }
    
    private class TestableCuVendorCreditMemoDocument extends CuVendorCreditMemoDocument {
        private static final long serialVersionUID = 1L;

        public TestableCuVendorCreditMemoDocument() {
            setNotes(new ArrayList<Note>());
        }
    }

}
