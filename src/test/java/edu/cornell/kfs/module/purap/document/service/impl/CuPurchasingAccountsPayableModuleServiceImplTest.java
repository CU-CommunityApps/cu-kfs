package edu.cornell.kfs.module.purap.document.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.TestUtils;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.rice.kew.api.exception.WorkflowException;

import edu.cornell.kfs.module.purap.fixture.VendorCreditMemoDocumentFixture;

@ConfigureContext(session = UserNameFixture.mo14)
public class CuPurchasingAccountsPayableModuleServiceImplTest extends KualiTestBase {

	private static final Logger LOG = LogManager.getLogger(CuPurchasingAccountsPayableModuleServiceImplTest.class);

	private CuPurchasingAccountsPayableModuleServiceImpl accountsPayableModuleServiceImpl;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		accountsPayableModuleServiceImpl = (CuPurchasingAccountsPayableModuleServiceImpl) TestUtils.getUnproxiedService("purchasingAccountsPayableModuleService");

	}
	
	public void testHandlePurchasingBatchCancels_CRCancel() throws WorkflowException{
		
		VendorCreditMemoDocument creditMemoDocument = VendorCreditMemoDocumentFixture.VENDOR_CREDIT_MEMO.createVendorCreditMemoDocument();
		accountsPayableModuleServiceImpl.handlePurchasingBatchCancels(creditMemoDocument.getDocumentNumber(), creditMemoDocument.getDocumentType(), true, false, true);
		
		assertFalse(PurapConstants.CreditMemoStatuses.CANCELLED_STATUSES.contains(creditMemoDocument.getApplicationDocumentStatus()));
	}
	
	public void testHandlePurchasingBatchCancels_NonCRCancel() throws WorkflowException{
		
		VendorCreditMemoDocument creditMemoDocument = VendorCreditMemoDocumentFixture.VENDOR_CREDIT_MEMO.createVendorCreditMemoDocument();
		accountsPayableModuleServiceImpl.handlePurchasingBatchCancels(creditMemoDocument.getDocumentNumber(), creditMemoDocument.getDocumentType(), true, false, false);
		
		assertTrue(PurapConstants.CreditMemoStatuses.CANCELLED_STATUSES.contains(creditMemoDocument.getApplicationDocumentStatus()));
	}
}
