package edu.cornell.kfs.module.purap.document.service.impl;

import java.math.BigDecimal;

import edu.cornell.kfs.module.purap.CUPurapParameterConstants;
import edu.cornell.kfs.module.purap.document.CuPaymentRequestDocument;
import edu.cornell.kfs.module.purap.document.service.CuPaymentRequestService;
import edu.cornell.kfs.module.purap.fixture.PaymentRequestFixture;
import edu.cornell.kfs.module.purap.fixture.PurchaseOrderFixture;
import edu.cornell.kfs.module.purap.fixture.RequisitionItemFixture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.impl.parameter.Parameter;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.module.purap.PurapConstants.ItemTypeCodes;
import org.kuali.kfs.module.purap.PaymentRequestStatuses;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.service.PaymentRequestService;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocumentTestUtils;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;



@ConfigureContext(session = UserNameFixture.mls398)
public class CuPaymentRequestServiceImplIntegTest extends KualiIntegTestBase {

	private static final Logger LOG = LogManager.getLogger();

    private PaymentRequestService paymentRequestService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        paymentRequestService = SpringContext
                .getBean(PaymentRequestService.class);


        // Override auto-approval-limit parameter for the duration of the test. The old value will be restored by the post-test rollback.
        // NOTE: If this test class ever gets configured to not roll back, then this override should change accordingly to restore the old param value.
        ParameterService parameterService = SpringContext.getBean(ParameterService.class);
        Parameter amountLimit = parameterService.getParameter(
                PaymentRequestDocument.class, CUPurapParameterConstants.DEFAULT_PURCHASE_ORDER_POS_APRVL_LMT_FOR_PREQ);
        amountLimit.setValue(RequisitionItemFixture.REQ_QTY_ITEM_AMOUNT_AT_5K.extendedPrice.toString());
        parameterService.updateParameter(amountLimit);
    }

    public void testRemoveIneligibleAdditionalCharges_NoEligibleItems()
            throws Exception {
        changeCurrentUser(UserNameFixture.ccs1);
        PurchaseOrderDocument po = PurchaseOrderFixture.PO_NON_B2B_OPEN
                .createPurchaseOrderdDocument(SpringContext
                        .getBean(DocumentService.class));
        changeCurrentUser(UserNameFixture.mls398);
        PaymentRequestDocument paymentRequestDocument = PaymentRequestFixture.PAYMENT_REQ_DOC
                .createPaymentRequestDocument(po.getPurapDocumentIdentifier());
        paymentRequestDocument.initiateDocument();
        paymentRequestDocument.populatePaymentRequestFromPurchaseOrder(po);
        int numberOfItems = paymentRequestDocument.getItems().size();
        paymentRequestService
                .removeIneligibleAdditionalCharges(paymentRequestDocument);

        assertEquals(numberOfItems, paymentRequestDocument.getItems().size());
    }

    public void testRemoveIneligibleAdditionalCharges_WithEligibleItems()
            throws Exception {
        changeCurrentUser(UserNameFixture.ccs1);
        PurchaseOrderDocument po = PurchaseOrderFixture.PO_NON_B2B_OPEN_TRADE_IN_ITEMS
                .createPurchaseOrderdDocument(SpringContext
                        .getBean(DocumentService.class));
        changeCurrentUser(UserNameFixture.mls398);
        PaymentRequestDocument paymentRequestDocument = PaymentRequestFixture.PAYMENT_REQ_DOC
                .createPaymentRequestDocument(po.getPurapDocumentIdentifier());
        paymentRequestDocument.initiateDocument();
        paymentRequestDocument.populatePaymentRequestFromPurchaseOrder(po);

        PaymentRequestItem preqItem = new PaymentRequestItem();
        preqItem.setItemTypeCode(ItemTypeCodes.ITEM_TYPE_TRADE_IN_CODE);
        preqItem.setItemDescription("test");
        preqItem.setItemUnitOfMeasureCode("EA");
        paymentRequestDocument.addItem(preqItem);

        int numberOfItems = paymentRequestDocument.getItems().size();
        paymentRequestService
                .removeIneligibleAdditionalCharges(paymentRequestDocument);

        assertFalse(numberOfItems == paymentRequestDocument.getItems().size());
    }

    public void testAddHoldOnPaymentRequest() throws Exception {
        changeCurrentUser(UserNameFixture.ccs1);
        PurchaseOrderDocument po = PurchaseOrderFixture.PO_NON_B2B_OPEN
                .createPurchaseOrderdDocument(SpringContext
                        .getBean(DocumentService.class));
        po.setVendorShippingPaymentTermsCode("AL");
        po.setVendorPaymentTermsCode("00N30");
        po.refreshNonUpdateableReferences();

        changeCurrentUser(UserNameFixture.mls398);
        PaymentRequestDocument paymentRequestDocument = PaymentRequestFixture.PAYMENT_REQ_DOC
                .createPaymentRequestDocument(po.getPurapDocumentIdentifier());
        paymentRequestDocument.initiateDocument();
        paymentRequestDocument.populatePaymentRequestFromPurchaseOrder(po);

        paymentRequestDocument.prepareForSave();

        AccountingDocumentTestUtils.saveDocument(paymentRequestDocument,
                SpringContext.getBean(DocumentService.class));

        paymentRequestService.addHoldOnPaymentRequest(paymentRequestDocument,
                "test");

        assertTrue(paymentRequestDocument.isHoldIndicator());
        assertEquals(UserNameFixture.mls398.getPerson().getPrincipalId(),
                paymentRequestDocument.getLastActionPerformedByPersonId());
    }

    public void testRemoveHoldOnPaymentRequest() throws Exception {
        changeCurrentUser(UserNameFixture.ccs1);
        PurchaseOrderDocument po = PurchaseOrderFixture.PO_NON_B2B_OPEN
                .createPurchaseOrderdDocument(SpringContext
                        .getBean(DocumentService.class));
        po.setVendorShippingPaymentTermsCode("AL");
        po.setVendorPaymentTermsCode("00N30");
        po.refreshNonUpdateableReferences();

        changeCurrentUser(UserNameFixture.mls398);
        PaymentRequestDocument paymentRequestDocument = PaymentRequestFixture.PAYMENT_REQ_DOC
                .createPaymentRequestDocument(po.getPurapDocumentIdentifier());
        paymentRequestDocument.initiateDocument();
        paymentRequestDocument.populatePaymentRequestFromPurchaseOrder(po);

        paymentRequestDocument.prepareForSave();

        AccountingDocumentTestUtils.saveDocument(paymentRequestDocument,
                SpringContext.getBean(DocumentService.class));

        paymentRequestService.removeHoldOnPaymentRequest(
                paymentRequestDocument, "test");

        assertFalse(paymentRequestDocument.isHoldIndicator());
        assertNull(paymentRequestDocument.getLastActionPerformedByPersonId());
    }

    public void testRequestCancelOnPaymentRequest() throws Exception {
        changeCurrentUser(UserNameFixture.ccs1);
        PurchaseOrderDocument po = PurchaseOrderFixture.PO_NON_B2B_OPEN
                .createPurchaseOrderdDocument(SpringContext
                        .getBean(DocumentService.class));
        po.setVendorShippingPaymentTermsCode("AL");
        po.setVendorPaymentTermsCode("00N30");
        po.refreshNonUpdateableReferences();

        changeCurrentUser(UserNameFixture.mls398);
        PaymentRequestDocument paymentRequestDocument = PaymentRequestFixture.PAYMENT_REQ_DOC
                .createPaymentRequestDocument(po.getPurapDocumentIdentifier());
        paymentRequestDocument.initiateDocument();
        paymentRequestDocument.populatePaymentRequestFromPurchaseOrder(po);

        paymentRequestDocument.prepareForSave();

        AccountingDocumentTestUtils.saveDocument(paymentRequestDocument,
                SpringContext.getBean(DocumentService.class));

        paymentRequestService.requestCancelOnPaymentRequest(
                paymentRequestDocument, "test");

        assertTrue(paymentRequestDocument.isPaymentRequestedCancelIndicator());
        assertNotNull(paymentRequestDocument.getLastActionPerformedByPersonId());
        assertNotNull(paymentRequestDocument
                .getAccountsPayableRequestCancelIdentifier());
    }

    public void testRemoveRequestCancelOnPaymentRequest() throws Exception {
        changeCurrentUser(UserNameFixture.ccs1);
        PurchaseOrderDocument po = PurchaseOrderFixture.PO_NON_B2B_OPEN
                .createPurchaseOrderdDocument(SpringContext
                        .getBean(DocumentService.class));
        po.setVendorShippingPaymentTermsCode("AL");
        po.setVendorPaymentTermsCode("00N30");
        po.refreshNonUpdateableReferences();

        changeCurrentUser(UserNameFixture.mls398);
        PaymentRequestDocument paymentRequestDocument = PaymentRequestFixture.PAYMENT_REQ_DOC
                .createPaymentRequestDocument(po.getPurapDocumentIdentifier());
        paymentRequestDocument.initiateDocument();
        paymentRequestDocument.populatePaymentRequestFromPurchaseOrder(po);

        paymentRequestDocument.prepareForSave();

        AccountingDocumentTestUtils.saveDocument(paymentRequestDocument,
                SpringContext.getBean(DocumentService.class));

        paymentRequestService.removeRequestCancelOnPaymentRequest(
                paymentRequestDocument, "test");

        assertFalse(paymentRequestDocument.isPaymentRequestedCancelIndicator());
        assertNull(paymentRequestDocument.getLastActionPerformedByPersonId());
        assertNull(paymentRequestDocument
                .getAccountsPayableRequestCancelIdentifier());
    }

    public void testCancelExtractedPaymentRequest() throws Exception {
        changeCurrentUser(UserNameFixture.ccs1);
        PurchaseOrderDocument po = PurchaseOrderFixture.PO_NON_B2B_OPEN
                .createPurchaseOrderdDocument(SpringContext
                        .getBean(DocumentService.class));
        po.setVendorShippingPaymentTermsCode("AL");
        po.setVendorPaymentTermsCode("00N30");
        po.refreshNonUpdateableReferences();

        changeCurrentUser(UserNameFixture.mls398);
        PaymentRequestDocument paymentRequestDocument = PaymentRequestFixture.PAYMENT_REQ_DOC
                .createPaymentRequestDocument(po.getPurapDocumentIdentifier());
        paymentRequestDocument.initiateDocument();
        paymentRequestDocument.populatePaymentRequestFromPurchaseOrder(po);

        paymentRequestDocument.prepareForSave();

        AccountingDocumentTestUtils.saveDocument(paymentRequestDocument,
                SpringContext.getBean(DocumentService.class));

        paymentRequestService.cancelExtractedPaymentRequest(
                paymentRequestDocument, "test");

        assertTrue(PaymentRequestStatuses.CANCELLED_STATUSES
                .contains(paymentRequestDocument.getApplicationDocumentStatus()));

    }

    public void testResetExtractedPaymentRequest() throws Exception {
        changeCurrentUser(UserNameFixture.ccs1);
        PurchaseOrderDocument po = PurchaseOrderFixture.PO_NON_B2B_OPEN
                .createPurchaseOrderdDocument(SpringContext
                        .getBean(DocumentService.class));
        po.setVendorShippingPaymentTermsCode("AL");
        po.setVendorPaymentTermsCode("00N30");
        po.refreshNonUpdateableReferences();

        changeCurrentUser(UserNameFixture.mls398);
        PaymentRequestDocument paymentRequestDocument = PaymentRequestFixture.PAYMENT_REQ_DOC
                .createPaymentRequestDocument(po.getPurapDocumentIdentifier());
        paymentRequestDocument.initiateDocument();
        paymentRequestDocument.populatePaymentRequestFromPurchaseOrder(po);

        paymentRequestDocument.setExtractedTimestamp(SpringContext.getBean(
                DateTimeService.class).getCurrentTimestamp());
        paymentRequestDocument.setPaymentPaidTimestamp(SpringContext.getBean(
                DateTimeService.class).getCurrentTimestamp());

        paymentRequestDocument.prepareForSave();

        AccountingDocumentTestUtils.saveDocument(paymentRequestDocument,
                SpringContext.getBean(DocumentService.class));

        paymentRequestService.resetExtractedPaymentRequest(
                paymentRequestDocument, "test");

        assertNull(paymentRequestDocument.getExtractedTimestamp());
        assertNull(paymentRequestDocument.getPaymentPaidTimestamp());
    }

    public void testMarkPaid() throws Exception {
        changeCurrentUser(UserNameFixture.ccs1);
        PurchaseOrderDocument po = PurchaseOrderFixture.PO_NON_B2B_OPEN
                .createPurchaseOrderdDocument(SpringContext
                        .getBean(DocumentService.class));
        po.setVendorShippingPaymentTermsCode("AL");
        po.setVendorPaymentTermsCode("00N30");
        po.refreshNonUpdateableReferences();

        changeCurrentUser(UserNameFixture.mls398);
        PaymentRequestDocument paymentRequestDocument = PaymentRequestFixture.PAYMENT_REQ_DOC
                .createPaymentRequestDocument(po.getPurapDocumentIdentifier());
        paymentRequestDocument.initiateDocument();
        paymentRequestDocument.populatePaymentRequestFromPurchaseOrder(po);
        paymentRequestDocument.setPaymentPaidTimestamp(null);

        paymentRequestDocument.prepareForSave();

        AccountingDocumentTestUtils.saveDocument(paymentRequestDocument,
                SpringContext.getBean(DocumentService.class));

        paymentRequestService.markPaid(paymentRequestDocument, SpringContext
                .getBean(DateTimeService.class).getCurrentSqlDate());

        assertNotNull(paymentRequestDocument.getPaymentPaidTimestamp());
    }

    public void testPopulatePaymentRequest() throws Exception {
        changeCurrentUser(UserNameFixture.ccs1);
        PurchaseOrderDocument po = PurchaseOrderFixture.PO_NON_B2B_OPEN
                .createPurchaseOrderdDocument(SpringContext
                        .getBean(DocumentService.class));
        po.setVendorShippingPaymentTermsCode("AL");
        po.setVendorPaymentTermsCode("00N30");
        po.refreshNonUpdateableReferences();

        changeCurrentUser(UserNameFixture.mls398);
        CuPaymentRequestDocument paymentRequestDocument = (CuPaymentRequestDocument) PaymentRequestFixture.PAYMENT_REQ_DOC
                .createPaymentRequestDocument(po.getPurapDocumentIdentifier());
        paymentRequestDocument.initiateDocument();
        paymentRequestDocument.populatePaymentRequestFromPurchaseOrder(po);

        paymentRequestDocument.prepareForSave();

        AccountingDocumentTestUtils.saveDocument(paymentRequestDocument,
                SpringContext.getBean(DocumentService.class));

        paymentRequestService.populatePaymentRequest(paymentRequestDocument);

        assertNotNull(paymentRequestDocument.getBankCode());
    }

    public void testChangeVendor() throws Exception {
        changeCurrentUser(UserNameFixture.ccs1);
        PurchaseOrderDocument po = PurchaseOrderFixture.PO_NON_B2B_OPEN
                .createPurchaseOrderdDocument(SpringContext
                        .getBean(DocumentService.class));
        po.setVendorShippingPaymentTermsCode("AL");
        po.setVendorPaymentTermsCode("00N30");
        po.refreshNonUpdateableReferences();

        changeCurrentUser(UserNameFixture.mls398);
        CuPaymentRequestDocument paymentRequestDocument = (CuPaymentRequestDocument) PaymentRequestFixture.PAYMENT_REQ_DOC
                .createPaymentRequestDocument(po.getPurapDocumentIdentifier());
        paymentRequestDocument.initiateDocument();
        paymentRequestDocument.populatePaymentRequestFromPurchaseOrder(po);

        paymentRequestDocument.prepareForSave();

        AccountingDocumentTestUtils.saveDocument(paymentRequestDocument,
                SpringContext.getBean(DocumentService.class));

        paymentRequestService.changeVendor(paymentRequestDocument, 5314, 0);

        assertEquals("P", paymentRequestDocument.getPaymentMethodCode());
    }

    public void testClearTax() throws Exception {
        changeCurrentUser(UserNameFixture.ccs1);
        PurchaseOrderDocument po = PurchaseOrderFixture.PO_NON_B2B_OPEN
                .createPurchaseOrderdDocument(SpringContext
                        .getBean(DocumentService.class));
        po.setVendorShippingPaymentTermsCode("AL");
        po.setVendorPaymentTermsCode("00N30");
        po.refreshNonUpdateableReferences();

        changeCurrentUser(UserNameFixture.mls398);
        CuPaymentRequestDocument paymentRequestDocument = (CuPaymentRequestDocument) PaymentRequestFixture.PAYMENT_REQ_DOC
                .createPaymentRequestDocument(po.getPurapDocumentIdentifier());
        paymentRequestDocument.initiateDocument();
        paymentRequestDocument.populatePaymentRequestFromPurchaseOrder(po);
        paymentRequestDocument.setTaxClassificationCode("XX");
        paymentRequestDocument.setTaxFederalPercent(new BigDecimal(30));
        paymentRequestDocument.setTaxStatePercent(new BigDecimal(10));
        paymentRequestDocument.setTaxCountryCode("US");
        paymentRequestDocument.setTaxNQIId(null);

        paymentRequestDocument.setTaxForeignSourceIndicator(true);
        paymentRequestDocument.setTaxExemptTreatyIndicator(true);
        paymentRequestDocument.setTaxOtherExemptIndicator(true);
        paymentRequestDocument.setTaxGrossUpIndicator(true);
        paymentRequestDocument.setTaxUSAIDPerDiemIndicator(true);
        paymentRequestDocument.setTaxSpecialW4Amount(new KualiDecimal(10));

        PaymentRequestItem paymentRequestItem = new PaymentRequestItem();
        paymentRequestItem
                .setItemTypeCode(ItemTypeCodes.ITEM_TYPE_FEDERAL_TAX_CODE);

        paymentRequestService.clearTax(paymentRequestDocument);

        assertNull(paymentRequestDocument.getTaxClassificationCode());
        assertNull(paymentRequestDocument.getTaxFederalPercent());
        assertNull(paymentRequestDocument.getTaxStatePercent());
        assertNull(paymentRequestDocument.getTaxCountryCode());
        assertNull(paymentRequestDocument.getTaxNQIId());

        assertFalse(paymentRequestDocument.getTaxForeignSourceIndicator());
        assertFalse(paymentRequestDocument.getTaxExemptTreatyIndicator());
        assertFalse(paymentRequestDocument.getTaxOtherExemptIndicator());
        assertFalse(paymentRequestDocument.getTaxGrossUpIndicator());
        assertFalse(paymentRequestDocument.getTaxUSAIDPerDiemIndicator());
        assertNull(paymentRequestDocument.getTaxSpecialW4Amount());

    }

    public void testAmountAnalysisForNonQuantityOrderBelowLimit() throws Exception {
        CuPaymentRequestDocument paymentRequestDocument = createPaymentRequestForTestingPOAmountLimit(
                PurchaseOrderFixture.PO_NON_B2B_OPEN_WITH_NON_QTY_ITEM_BELOW_5K);
        assertTrue("Payment should have passed the within-amount-limit check, due to non-quantity order with amount below the threshold",
                ((CuPaymentRequestService) paymentRequestService).purchaseOrderForPaymentRequestIsWithinAutoApproveAmountLimit(paymentRequestDocument));
    }

    public void testAmountAnalysisForNonQuantityOrderAtLimit() throws Exception {
        CuPaymentRequestDocument paymentRequestDocument = createPaymentRequestForTestingPOAmountLimit(
                PurchaseOrderFixture.PO_NON_B2B_OPEN_WITH_NON_QTY_ITEM_AT_5K);
        assertFalse("Payment should have failed the within-amount-limit check, due to non-quantity order with amount equal to the threshold",
                ((CuPaymentRequestService) paymentRequestService).purchaseOrderForPaymentRequestIsWithinAutoApproveAmountLimit(paymentRequestDocument));
    }

    public void testAmountAnalysisForNonQuantityOrderAboveLimit() throws Exception {
        CuPaymentRequestDocument paymentRequestDocument = createPaymentRequestForTestingPOAmountLimit(
                PurchaseOrderFixture.PO_NON_B2B_OPEN_WITH_NON_QTY_ITEM_ABOVE_5K);
        assertFalse("Payment should have failed the within-amount-limit check, due to non-quantity order with amount above the threshold",
                ((CuPaymentRequestService) paymentRequestService).purchaseOrderForPaymentRequestIsWithinAutoApproveAmountLimit(paymentRequestDocument));
    }

    public void testAmountAnalysisForQuantityOrderBelowLimit() throws Exception {
        CuPaymentRequestDocument paymentRequestDocument = createPaymentRequestForTestingPOAmountLimit(
                PurchaseOrderFixture.PO_NON_B2B_OPEN_WITH_QTY_ITEM_BELOW_5K);
        assertTrue("Payment should have passed the within-amount-limit check, due to quantity-based order with amount below the threshold",
                ((CuPaymentRequestService) paymentRequestService).purchaseOrderForPaymentRequestIsWithinAutoApproveAmountLimit(paymentRequestDocument));
    }

    public void testAmountAnalysisForQuantityOrderAtLimit() throws Exception {
        CuPaymentRequestDocument paymentRequestDocument = createPaymentRequestForTestingPOAmountLimit(
                PurchaseOrderFixture.PO_NON_B2B_OPEN_WITH_QTY_ITEM_AT_5K);
        assertFalse("Payment should have failed the within-amount-limit check, due to quantity-based order with amount equal to the threshold",
                ((CuPaymentRequestService) paymentRequestService).purchaseOrderForPaymentRequestIsWithinAutoApproveAmountLimit(paymentRequestDocument));
    }

    public void testAmountAnalysisForQuantityOrderAboveLimit() throws Exception {
        CuPaymentRequestDocument paymentRequestDocument = createPaymentRequestForTestingPOAmountLimit(
                PurchaseOrderFixture.PO_NON_B2B_OPEN_WITH_QTY_ITEM_ABOVE_5K);
        assertFalse("Payment should have failed the within-amount-limit check, due to quantity-based order with amount above the threshold",
                ((CuPaymentRequestService) paymentRequestService).purchaseOrderForPaymentRequestIsWithinAutoApproveAmountLimit(paymentRequestDocument));
    }

    protected void changeCurrentUser(UserNameFixture sessionUser) {
        GlobalVariables.setUserSession(new UserSession(sessionUser.toString()));
    }

    protected CuPaymentRequestDocument createPaymentRequestForTestingPOAmountLimit(PurchaseOrderFixture poFixture) throws Exception {
        changeCurrentUser(UserNameFixture.ccs1);
        PurchaseOrderDocument po = poFixture.createPurchaseOrderdDocument(SpringContext.getBean(DocumentService.class));
        changeCurrentUser(UserNameFixture.mls398);
        PaymentRequestDocument paymentRequestDocument = PaymentRequestFixture.PAYMENT_REQ_DOC.createPaymentRequestDocument(po.getPurapDocumentIdentifier());
        paymentRequestDocument.initiateDocument();
        paymentRequestDocument.populatePaymentRequestFromPurchaseOrder(po);

        return (CuPaymentRequestDocument) paymentRequestDocument;
    }
}