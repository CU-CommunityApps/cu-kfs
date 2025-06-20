package edu.cornell.kfs.module.purap.document.service.impl;

import org.kuali.kfs.module.purap.CreditMemoStatuses;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.IntegTestUtils;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.kfs.kew.api.exception.WorkflowException;

import edu.cornell.kfs.module.purap.document.CuVendorCreditMemoDocument;
import edu.cornell.kfs.module.purap.fixture.CuVendorCreditMemoDocumentFixture;
import edu.cornell.kfs.pdp.batch.service.impl.CuProcessIndividualPdpCancelPaidServiceImpl;

@ConfigureContext(session = UserNameFixture.mls398)
public class CuProcessIndividualPdpCancelPaidServiceImplIntegTest extends KualiIntegTestBase {

    private CuProcessIndividualPdpCancelPaidServiceImpl processIndividualPdpCancelPaidService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        processIndividualPdpCancelPaidService = (CuProcessIndividualPdpCancelPaidServiceImpl) IntegTestUtils.getUnproxiedService("processIndividualPdpCancelPaidService");
    }

    public void testHandlePurchasingBatchCancels_CRCancel() throws WorkflowException{
        CuVendorCreditMemoDocument creditMemoDocument = (CuVendorCreditMemoDocument)CuVendorCreditMemoDocumentFixture.VENDOR_CREDIT_MEMO.createVendorCreditMemoDocument();
        processIndividualPdpCancelPaidService.handlePurchasingBatchCancels(creditMemoDocument.getDocumentNumber(), creditMemoDocument.getDocumentType(), true, false, true);
        assertFalse(CreditMemoStatuses.CANCELLED_STATUSES.contains(creditMemoDocument.getApplicationDocumentStatus()));
    }

    public void testHandlePurchasingBatchCancels_NonCRCancel() throws WorkflowException{
        CuVendorCreditMemoDocument creditMemoDocument = (CuVendorCreditMemoDocument)CuVendorCreditMemoDocumentFixture.VENDOR_CREDIT_MEMO.createVendorCreditMemoDocument();
        processIndividualPdpCancelPaidService.handlePurchasingBatchCancels(creditMemoDocument.getDocumentNumber(), creditMemoDocument.getDocumentType(), true, false, false);
        assertTrue(CreditMemoStatuses.CANCELLED_STATUSES.contains(creditMemoDocument.getApplicationDocumentStatus()));
    }

}
