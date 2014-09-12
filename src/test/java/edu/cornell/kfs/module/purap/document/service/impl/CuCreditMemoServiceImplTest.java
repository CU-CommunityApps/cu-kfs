package edu.cornell.kfs.module.purap.document.service.impl;

import java.sql.Date;
import java.sql.Timestamp;

import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.context.TestUtils;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.krad.service.DocumentService;

import edu.cornell.kfs.module.purap.document.CuVendorCreditMemoDocument;
import edu.cornell.kfs.module.purap.fixture.VendorCreditMemoDocumentFixture;

@ConfigureContext(session = UserNameFixture.mo14)
public class CuCreditMemoServiceImplTest extends KualiTestBase {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuCreditMemoServiceImplTest.class);

	private CuCreditMemoServiceImpl creditMemoServiceImpl;
	private DocumentService documentService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		creditMemoServiceImpl = (CuCreditMemoServiceImpl) TestUtils.getUnproxiedService("creditMemoService");
		documentService = SpringContext.getBean(DocumentService.class);

	}

	public void testAddHoldOnCreditMemo() throws Exception {
		VendorCreditMemoDocument creditMemoDocument = VendorCreditMemoDocumentFixture.VENDOR_CREDIT_MEMO.createVendorCreditMemoDocument();

		creditMemoServiceImpl.addHoldOnCreditMemo(creditMemoDocument, "unit test");

		assertTrue(creditMemoDocument.isHoldIndicator());
		assertTrue(UserNameFixture.mo14.getPerson().getPrincipalId().equalsIgnoreCase(creditMemoDocument.getLastActionPerformedByPersonId()));
	}
	
	public void testRemoveHoldOnCreditMemo() throws Exception {
		VendorCreditMemoDocument creditMemoDocument = VendorCreditMemoDocumentFixture.VENDOR_CREDIT_MEMO.createVendorCreditMemoDocument();

		creditMemoServiceImpl.removeHoldOnCreditMemo(creditMemoDocument, "unit test");

		assertFalse(creditMemoDocument.isHoldIndicator());
		assertNull(creditMemoDocument.getLastActionPerformedByPersonId());
	}
	
	public void testrResetExtractedCreditMemo_Successful() throws Exception {
		VendorCreditMemoDocument creditMemoDocument = VendorCreditMemoDocumentFixture.VENDOR_CREDIT_MEMO.createVendorCreditMemoDocument();
		creditMemoDocument.setExtractedTimestamp(SpringContext.getBean(DateTimeService.class).getCurrentTimestamp());
		creditMemoServiceImpl.resetExtractedCreditMemo(creditMemoDocument, "unit test");

		assertNull(creditMemoDocument.getExtractedTimestamp());
		assertNull(creditMemoDocument.getCreditMemoPaidTimestamp());
	}
	
	public void testrResetExtractedCreditMemo_Fail() throws Exception {
		VendorCreditMemoDocument creditMemoDocument = VendorCreditMemoDocumentFixture.VENDOR_CREDIT_MEMO.createVendorCreditMemoDocument();
		creditMemoDocument.setApplicationDocumentStatus(PurapConstants.CreditMemoStatuses.APPDOC_CANCELLED_IN_PROCESS);
		creditMemoDocument.setExtractedTimestamp(SpringContext.getBean(DateTimeService.class).getCurrentTimestamp());
		creditMemoDocument.setCreditMemoPaidTimestamp(SpringContext.getBean(DateTimeService.class).getCurrentTimestamp());

		creditMemoServiceImpl.resetExtractedCreditMemo(creditMemoDocument, "unit test");

		assertNotNull(creditMemoDocument.getExtractedTimestamp());
		assertNotNull(creditMemoDocument.getCreditMemoPaidTimestamp());
	}
	
	public void testMarkPaid() throws Exception {
		VendorCreditMemoDocument creditMemoDocument = VendorCreditMemoDocumentFixture.VENDOR_CREDIT_MEMO.createVendorCreditMemoDocument();

		Date currentDate = SpringContext.getBean(DateTimeService.class).getCurrentSqlDate();
		Timestamp currentTimeStamp = new Timestamp(currentDate.getTime());
		creditMemoServiceImpl.markPaid(creditMemoDocument, currentDate);

		assertNotNull(creditMemoDocument.getCreditMemoPaidTimestamp());
		assertEquals(currentTimeStamp, creditMemoDocument.getCreditMemoPaidTimestamp());
	}
	
	public void testPopulateDocumentAfterInit() throws Exception {
		
		CuVendorCreditMemoDocument creditMemoDocument = (CuVendorCreditMemoDocument)VendorCreditMemoDocumentFixture.VENDOR_CREDIT_MEMO.createVendorCreditMemoDocument();

		creditMemoServiceImpl.populateDocumentAfterInit(creditMemoDocument);

		assertNotNull(creditMemoDocument.getPaymentMethodCode());
		assertEquals("P", creditMemoDocument.getPaymentMethodCode());
		
	}

}
