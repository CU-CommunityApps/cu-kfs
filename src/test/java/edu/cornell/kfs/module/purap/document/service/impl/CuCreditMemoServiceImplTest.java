package edu.cornell.kfs.module.purap.document.service.impl;

import java.sql.Date;
import java.sql.Timestamp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.context.TestUtils;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.service.DocumentService;

import edu.cornell.kfs.module.purap.document.CuVendorCreditMemoDocument;
import edu.cornell.kfs.module.purap.fixture.VendorCreditMemoDocumentFixture;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.core.impl.datetime.DateTimeServiceImpl;
import org.kuali.rice.kew.api.exception.WorkflowException;

public class CuCreditMemoServiceImplTest {

	private CuCreditMemoServiceImpl creditMemoServiceImpl;
	private DateTimeService dateTimeService;
	private VendorCreditMemoDocument creditMemoDocument;

	@Before
	public void setUp() throws Exception {
		creditMemoServiceImpl = new CuCreditMemoServiceImpl();
		dateTimeService = new DateTimeServiceImpl();
		creditMemoDocument = setupVendorCreditMemoDocument();
	}

    private VendorCreditMemoDocument setupVendorCreditMemoDocument() {
	    // Mock to avoid calling super constructor
	    VendorCreditMemoDocument creditMemoDocument = new VendorCreditMemoDocument();

	    // mock out DocumentService and assert number of times save is called?

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
		creditMemoServiceImpl.addHoldOnCreditMemo(creditMemoDocument, "unit test");

		Assert.assertTrue(creditMemoDocument.isHoldIndicator());
		Assert.assertTrue(UserNameFixture.mo14.getPerson().getPrincipalId().equalsIgnoreCase(creditMemoDocument.getLastActionPerformedByPersonId()));
	}

	@Test
	public void testRemoveHoldOnCreditMemo() throws Exception {
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

}
