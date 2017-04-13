package edu.cornell.kfs.module.purap.document.service.impl;

import edu.cornell.kfs.module.purap.document.CuVendorCreditMemoDocument;
import edu.cornell.kfs.sys.util.MockPersonUtil;
import edu.cornell.kfs.vnd.fixture.VendorDetailExtensionFixture;
import edu.cornell.kfs.vnd.fixture.VendorHeaderFixture;
import org.easymock.EasyMock;
import org.easymock.IMockBuilder;
import org.easymock.Mock;
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
import org.kuali.kfs.krad.service.impl.DocumentServiceImpl;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.module.purap.PurapConstants;
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
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CuCreditMemoServiceImplTest.TestCuCreditMemoServiceImpl.class)
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
    @Mock
    private static Person mo14Person;
    @Mock
    private static UserSession mo14Session;

    @Before
	public void setUp() throws Exception {
        accountsPayableService = EasyMock.createNiceMock(AccountsPayableService.class);
        EasyMock.expect(accountsPayableService.getExpiredOrClosedAccountList(creditMemoDocument)).andReturn(new HashMap<>());
        EasyMock.replay(accountsPayableService);

        dataDictionaryService = EasyMock.createNiceMock(DataDictionaryService.class);
        EasyMock.expect(dataDictionaryService.getAttributeMaxLength(DocumentHeader.class, KRADPropertyConstants.DOCUMENT_DESCRIPTION)).andReturn(200);
        EasyMock.replay(dataDictionaryService);

        dateTimeService = new DateTimeServiceImpl();
        documentService = new MockDocumentServiceImpl();
        noteService = EasyMock.createMock(NoteService.class);
        purapService = EasyMock.createMock(PurapService.class);
        vendorService = new MockVendorServiceImpl();

        creditMemoServiceImpl = PowerMock.createPartialMock(CuCreditMemoServiceImplTest.TestCuCreditMemoServiceImpl.class, "reIndexDocument", "getCreditMemoDocumentById");

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

    private CuVendorCreditMemoDocument setupVendorCreditMemoDocument() {
        ArrayList<String> methodNames = new ArrayList<>();
        for (Method method : VendorCreditMemoDocument.class.getMethods()) {
            if (!Modifier.isFinal(method.getModifiers()) && !method.getName().startsWith("set") && !method.getName().startsWith("get") && !method.getName().startsWith("is")) {
                methodNames.add(method.getName());
            }
        }
        IMockBuilder<CuVendorCreditMemoDocument> builder = EasyMock.createMockBuilder(CuVendorCreditMemoDocument.class).addMockedMethods(methodNames.toArray(new String[0]));
        creditMemoDocument = builder.createNiceMock();

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
        EasyMock.expect(creditMemoServiceImpl.getCreditMemoDocumentById(null)).andReturn(setupVendorCreditMemoDocument());
        PowerMock.expectPrivate(creditMemoServiceImpl, "reIndexDocument", creditMemoDocument).times(2);
        EasyMock.replay(creditMemoServiceImpl);
        creditMemoServiceImpl.addHoldOnCreditMemo(creditMemoDocument, "unit test");

		Assert.assertTrue(creditMemoDocument.isHoldIndicator());
		Assert.assertTrue(mo14Person.getPrincipalId().equalsIgnoreCase(creditMemoDocument.getLastActionPerformedByPersonId()));
	}

	@Test
	public void testRemoveHoldOnCreditMemo() throws Exception {
        EasyMock.expect(creditMemoServiceImpl.getCreditMemoDocumentById(null)).andReturn(setupVendorCreditMemoDocument());
        PowerMock.expectPrivate(creditMemoServiceImpl, "reIndexDocument", creditMemoDocument).times(2);
        EasyMock.replay(creditMemoServiceImpl);
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

	private class MockDocumentServiceImpl extends DocumentServiceImpl {

	    @Override
        public Note createNoteFromDocument(Document document, String text) {
            ArrayList<String> methodNames = new ArrayList<>();
            for (Method method : Note.class.getMethods()) {
                if (!Modifier.isFinal(method.getModifiers()) && !method.getName().startsWith("set") && !method.getName().startsWith("get")) {
                    methodNames.add(method.getName());
                }
            }
            IMockBuilder<Note> builder = EasyMock.createMockBuilder(Note.class).addMockedMethods(methodNames.toArray(new String[0]));
            Note note = builder.createNiceMock();

            note.setNotePostedTimestamp(dateTimeService.getCurrentTimestamp());
            note.setVersionNumber(Long.valueOf(1L));
            note.setNoteText(text);
            note.setNoteTypeCode(document.getNoteType().getCode());

            return note;
        }

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

        @Override
        public void setApplicationDocumentStatus(String applicationDocumentStatus) {
            this.applicationDocumentStatus = applicationDocumentStatus;
        }

    }

}
