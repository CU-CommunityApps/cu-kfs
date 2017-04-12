package edu.cornell.kfs.module.purap.document.service.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.easymock.EasyMock;
import org.easymock.IMockBuilder;
import org.easymock.Mock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.service.impl.DocumentServiceImpl;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.service.DocumentService;

import edu.cornell.kfs.module.purap.document.CuVendorCreditMemoDocument;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.core.impl.datetime.DateTimeServiceImpl;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.impl.identity.PersonImpl;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CuCreditMemoServiceImplTest.TestCuCreditMemoServiceImpl.class)
public class CuCreditMemoServiceImplTest {

	private CuCreditMemoServiceImpl creditMemoServiceImpl;
	private DateTimeService dateTimeService;
	private VendorCreditMemoDocument creditMemoDocument;
	private DocumentService documentService;
	private NoteService noteService;
    private PurapService purapService;
    @Mock
    private static Person mo14Person;
    @Mock
    private static UserSession mo14Session;

	@Before
	public void setUp() throws Exception {
	    documentService = new MockDocumentServiceImpl();
        noteService = EasyMock.createMock(NoteService.class);
        purapService = EasyMock.createMock(PurapService.class);
        dateTimeService = new DateTimeServiceImpl();

        creditMemoServiceImpl = PowerMock.createPartialMock(CuCreditMemoServiceImplTest.TestCuCreditMemoServiceImpl.class, "reIndexDocument", "getCreditMemoDocumentById");

        creditMemoServiceImpl.setDocumentService(documentService);
        creditMemoServiceImpl.setNoteService(noteService);
        creditMemoServiceImpl.setPurapService(purapService);
		creditMemoDocument = setupVendorCreditMemoDocument();

		mo14Person = createMockPerson(UserNameFixture.mo14);
        mo14Session = createMockUserSession(mo14Person);
        GlobalVariables.setUserSession(mo14Session);
	}

    private VendorCreditMemoDocument setupVendorCreditMemoDocument() {
        ArrayList<String> methodNames = new ArrayList<>();
        for (Method method : VendorCreditMemoDocument.class.getMethods()) {
            if (!Modifier.isFinal(method.getModifiers()) && !method.getName().startsWith("set") && !method.getName().startsWith("get") && !method.getName().startsWith("is")) {
                methodNames.add(method.getName());
            }
        }
        IMockBuilder<VendorCreditMemoDocument> builder = EasyMock.createMockBuilder(VendorCreditMemoDocument.class).addMockedMethods(methodNames.toArray(new String[0]));
        creditMemoDocument = builder.createNiceMock();

	    // mock out DocumentService and assert number of times save is called?

        creditMemoDocument.setDocumentHeader(new FinancialSystemDocumentHeader());
        creditMemoDocument.getDocumentHeader().setDocumentDescription("Description");
        creditMemoDocument.setVendorDetailAssignedIdentifier(0);
        creditMemoDocument.setVendorHeaderGeneratedIdentifier(4291);
        creditMemoDocument.setCreditMemoNumber("12345");
        creditMemoDocument.setCreditMemoDate(dateTimeService.getCurrentSqlDate());
        creditMemoDocument.setCreditMemoAmount(new KualiDecimal(100));

        return creditMemoDocument;
    }

    protected static UserSession createMockUserSession(Person person) {
        UserSession userSession = EasyMock.createMock(UserSession.class);
        EasyMock.expect(userSession.getPrincipalId()).andStubReturn(person.getPrincipalId());
        EasyMock.expect(userSession.getPrincipalName()).andStubReturn(person.getPrincipalName());
        EasyMock.expect(userSession.getLoggedInUserPrincipalName()).andStubReturn(person.getPrincipalName());
        EasyMock.expect(userSession.getPerson()).andStubReturn(person);
        EasyMock.expect(userSession.getActualPerson()).andStubReturn(person);
        EasyMock.replay(userSession);
        return userSession;
    }

    protected static Person createMockPerson(UserNameFixture userNameFixture) {
        Person person = EasyMock.createMock(PersonImpl.class);
        EasyMock.expect(person.getPrincipalName()).andStubReturn(userNameFixture.toString());
        EasyMock.expect(person.getPrincipalId()).andStubReturn(userNameFixture.toString());
        EasyMock.replay(person);
        return person;
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

		Assert.assertNotNull(((CuVendorCreditMemoDocument)creditMemoDocument).getPaymentMethodCode());
		Assert.assertEquals("P", ((CuVendorCreditMemoDocument)creditMemoDocument).getPaymentMethodCode());
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
}
