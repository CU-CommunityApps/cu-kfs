package edu.cornell.kfs.module.purap.document.service.impl;

import org.kuali.kfs.module.purap.CreditMemoStatuses;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.IntegTestUtils;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.kfs.kew.api.exception.WorkflowException;

import edu.cornell.kfs.module.purap.document.CuVendorCreditMemoDocument;
import edu.cornell.kfs.module.purap.fixture.CuVendorCreditMemoDocumentFixture;

@ConfigureContext(session = UserNameFixture.mls398)
public class CuPurchasingAccountsPayableModuleServiceImplIntegTest extends KualiIntegTestBase {

    private CuPurchasingAccountsPayableModuleServiceImpl accountsPayableModuleServiceImpl;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        accountsPayableModuleServiceImpl = (CuPurchasingAccountsPayableModuleServiceImpl) IntegTestUtils.getUnproxiedService("purchasingAccountsPayableModuleService");
    }

    public void testHandlePurchasingBatchCancels_CRCancel() throws WorkflowException{
        CuVendorCreditMemoDocument creditMemoDocument = (CuVendorCreditMemoDocument)CuVendorCreditMemoDocumentFixture.VENDOR_CREDIT_MEMO.createVendorCreditMemoDocument();
        accountsPayableModuleServiceImpl.handlePurchasingBatchCancels(creditMemoDocument.getDocumentNumber(), creditMemoDocument.getDocumentType(), true, false, true);
        assertFalse(CreditMemoStatuses.CANCELLED_STATUSES.contains(creditMemoDocument.getApplicationDocumentStatus()));
    }

    public void testHandlePurchasingBatchCancels_NonCRCancel() throws WorkflowException{
        CuVendorCreditMemoDocument creditMemoDocument = (CuVendorCreditMemoDocument)CuVendorCreditMemoDocumentFixture.VENDOR_CREDIT_MEMO.createVendorCreditMemoDocument();
        accountsPayableModuleServiceImpl.handlePurchasingBatchCancels(creditMemoDocument.getDocumentNumber(), creditMemoDocument.getDocumentType(), true, false, false);
        assertTrue(CreditMemoStatuses.CANCELLED_STATUSES.contains(creditMemoDocument.getApplicationDocumentStatus()));
    }

}
