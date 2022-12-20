package edu.cornell.kfs.module.purap.document.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.module.purap.CreditMemoStatuses;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.IntegTestUtils;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.kfs.kew.api.exception.WorkflowException;

import edu.cornell.kfs.module.purap.fixture.VendorCreditMemoDocumentFixture;

@ConfigureContext(session = UserNameFixture.mls398)
public class CuPurchasingAccountsPayableModuleServiceImplIntegTest extends KualiIntegTestBase {

	private static final Logger LOG = LogManager.getLogger();

	private CuPurchasingAccountsPayableModuleServiceImpl accountsPayableModuleServiceImpl;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		accountsPayableModuleServiceImpl = (CuPurchasingAccountsPayableModuleServiceImpl) IntegTestUtils.getUnproxiedService("purchasingAccountsPayableModuleService");

	}
	
	public void testHandlePurchasingBatchCancels_CRCancel() throws WorkflowException{
		
		VendorCreditMemoDocument creditMemoDocument = VendorCreditMemoDocumentFixture.VENDOR_CREDIT_MEMO.createVendorCreditMemoDocument();
		accountsPayableModuleServiceImpl.handlePurchasingBatchCancels(creditMemoDocument.getDocumentNumber(), creditMemoDocument.getDocumentType(), true, false, true);
		
		assertFalse(CreditMemoStatuses.CANCELLED_STATUSES.contains(creditMemoDocument.getApplicationDocumentStatus()));
	}
	
	public void testHandlePurchasingBatchCancels_NonCRCancel() throws WorkflowException{
		
		VendorCreditMemoDocument creditMemoDocument = VendorCreditMemoDocumentFixture.VENDOR_CREDIT_MEMO.createVendorCreditMemoDocument();
		accountsPayableModuleServiceImpl.handlePurchasingBatchCancels(creditMemoDocument.getDocumentNumber(), creditMemoDocument.getDocumentType(), true, false, false);
		
		assertTrue(CreditMemoStatuses.CANCELLED_STATUSES.contains(creditMemoDocument.getApplicationDocumentStatus()));
	}
}
