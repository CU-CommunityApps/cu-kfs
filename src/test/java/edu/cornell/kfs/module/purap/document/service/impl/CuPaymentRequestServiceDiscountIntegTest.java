package edu.cornell.kfs.module.purap.document.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.kfs.vnd.businessobject.PaymentTermType;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.module.purap.document.service.CuPaymentRequestService;
import edu.cornell.kfs.module.purap.fixture.PurchaseOrderFixture;
import edu.cornell.kfs.module.purap.rest.jsonObjects.PaymentRequestDto;
import edu.cornell.kfs.module.purap.rest.jsonObjects.PaymentRequestLineItemDto;
import edu.cornell.kfs.module.purap.rest.jsonObjects.PaymentRequestNoteDto;
import edu.cornell.kfs.module.purap.rest.jsonObjects.PaymentRequestResultsDto;

/**
 * Integration tests to verify discount calculation in Payment Request creation,
 * particularly through the API flow.
 */
@ConfigureContext(session = UserNameFixture.mls398)
public class CuPaymentRequestServiceDiscountIntegTest extends KualiIntegTestBase {

    private static final Logger LOG = LogManager.getLogger();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    
    private CuPaymentRequestService cuPaymentRequestService;
    private DocumentService documentService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        cuPaymentRequestService = SpringContext.getBean(CuPaymentRequestService.class);
        documentService = SpringContext.getBean(DocumentService.class);
    }

    /**
     * Test that discount items are created when a PO has payment terms with a discount percentage.
     * This tests the standard UI flow: populatePaymentRequestFromPurchaseOrder
     */
    public void testDiscountItemCreatedFromPOWithPaymentTerms() throws Exception {
        LOG.info("testDiscountItemCreatedFromPOWithPaymentTerms: Starting test");
        
        changeCurrentUser(UserNameFixture.ccs1);
        
        // Create a PO with payment terms that include a discount
        PurchaseOrderDocument po = PurchaseOrderFixture.PO_NON_B2B_OPEN
                .createPurchaseOrderdDocument(documentService);
        po.setVendorPaymentTermsCode("00N30");
        po.refreshNonUpdateableReferences();
        
        // Verify PO has payment terms with discount
        PaymentTermType paymentTerms = po.getVendorPaymentTerms();
        assertNotNull("PO should have payment terms", paymentTerms);
        LOG.info("testDiscountItemCreatedFromPOWithPaymentTerms: PO payment terms code={}, percent={}", 
                paymentTerms.getVendorPaymentTermsCode(), paymentTerms.getVendorPaymentTermsPercent());
        
        changeCurrentUser(UserNameFixture.mls398);
        
        // Create payment request using standard flow
        PaymentRequestDocument preq = (PaymentRequestDocument) documentService
                .getNewDocument(PaymentRequestDocument.class);
        preq.initiateDocument();
        preq.setInvoiceNumber("TEST-DISC-001");
        preq.setInvoiceDate(java.sql.Date.valueOf(LocalDate.now()));
        preq.setVendorInvoiceAmount(new KualiDecimal(1000));
        preq.setPurchaseOrderIdentifier(po.getPurapDocumentIdentifier());
        
        // This method calls addBelowLineItems which should create discount item
        preq.populatePaymentRequestFromPurchaseOrder(po);
        
        // Verify discount item was created
        PaymentRequestItem discountItem = findDiscountItem(preq);
        
        if (paymentTerms.getVendorPaymentTermsPercent() != null && 
                paymentTerms.getVendorPaymentTermsPercent().compareTo(BigDecimal.ZERO) > 0) {
            assertNotNull("Discount item should be created when payment terms have a discount percentage", 
                    discountItem);
            assertEquals("Discount item type should be DISC", 
                    PurapConstants.ItemTypeCodes.ITEM_TYPE_PMT_TERMS_DISCOUNT_CODE, 
                    discountItem.getItemTypeCode());
            LOG.info("testDiscountItemCreatedFromPOWithPaymentTerms: Discount item created with extended price={}", 
                    discountItem.getExtendedPrice());
        } else {
            LOG.info("testDiscountItemCreatedFromPOWithPaymentTerms: Payment terms has no discount percentage, no discount item expected");
        }
        
        LOG.info("testDiscountItemCreatedFromPOWithPaymentTerms: Test completed successfully");
    }

    /**
     * Test discount calculation through the API flow.
     * This verifies that when items are updated via API, the discount is recalculated correctly.
     */
    public void testDiscountCalculationThroughApiFlow() throws Exception {
        LOG.info("testDiscountCalculationThroughApiFlow: Starting test");
        
        changeCurrentUser(UserNameFixture.ccs1);
        
        // Create a PO with payment terms
        PurchaseOrderDocument po = PurchaseOrderFixture.PO_NON_B2B_OPEN
                .createPurchaseOrderdDocument(documentService);
        po.setVendorPaymentTermsCode("00N30");
        po.refreshNonUpdateableReferences();
        
        PaymentTermType paymentTerms = po.getVendorPaymentTerms();
        assertNotNull("PO should have payment terms", paymentTerms);
        
        BigDecimal discountPercent = paymentTerms.getVendorPaymentTermsPercent();
        LOG.info("testDiscountCalculationThroughApiFlow: Payment terms discount percent={}", discountPercent);
        
        if (discountPercent == null || discountPercent.compareTo(BigDecimal.ZERO) == 0) {
            LOG.info("testDiscountCalculationThroughApiFlow: No discount percentage, skipping discount calculation test");
            return;
        }
        
        changeCurrentUser(UserNameFixture.mls398);
        
        // Create PaymentRequestDto simulating API input
        PaymentRequestDto dto = createTestPaymentRequestDto(po, "2000.00");
        
        // Add line items - use first item from PO
        if (po.getItems() != null && po.getItems().size() > 0) {
            PaymentRequestLineItemDto itemDto = new PaymentRequestLineItemDto();
            itemDto.setLineNumber("1");
            itemDto.setItemQuantity("10");
            itemDto.setItemPrice("100.00"); // Total: $1000
            dto.getItems().add(itemDto);
        }
        
        // Add required note
        PaymentRequestNoteDto noteDto = new PaymentRequestNoteDto();
        noteDto.setNoteText("Test discount calculation");
        dto.getNotes().add(noteDto);
        
        PaymentRequestResultsDto results = new PaymentRequestResultsDto();
        
        // Create PREQ through API flow
        PaymentRequestDocument preq = cuPaymentRequestService.createPaymentRequestDocumentFromDto(dto, results);
        
        assertNotNull("Payment request should be created", preq);
        assertTrue("Payment request creation should be valid", results.isValid());
        
        // Calculate payment request (this recalculates discount)
        cuPaymentRequestService.calculatePaymentRequest(preq, true);
        
        // Verify discount item exists and has correct amount
        PaymentRequestItem discountItem = findDiscountItem(preq);
        assertNotNull("Discount item should exist after API creation", discountItem);
        
        // Calculate expected discount amount
        KualiDecimal preTaxTotal = preq.getGrandPreTaxTotalExcludingDiscount();
        KualiDecimal expectedDiscount = preTaxTotal.multiply(new KualiDecimal(discountPercent));
        expectedDiscount = expectedDiscount.negated(); // Discount is negative
        
        LOG.info("testDiscountCalculationThroughApiFlow: PreTax Total={}, Discount %={}, Expected Discount={}, Actual Discount={}", 
                preTaxTotal, discountPercent, expectedDiscount, discountItem.getExtendedPrice());
        
        // Allow small rounding differences
        KualiDecimal difference = discountItem.getExtendedPrice().subtract(expectedDiscount).abs();
        assertTrue("Discount amount should match expected (difference: " + difference + ")", 
                difference.isLessEqual(new KualiDecimal(0.01)));
        
        LOG.info("testDiscountCalculationThroughApiFlow: Test completed successfully");
    }

    /**
     * Test that discount is removed when payment terms have no discount percentage.
     */
    public void testDiscountRemovedWhenNoDiscountPercent() throws Exception {
        LOG.info("testDiscountRemovedWhenNoDiscountPercent: Starting test");
        
        changeCurrentUser(UserNameFixture.ccs1);
        
        // Create a PO with payment terms but no discount
        PurchaseOrderDocument po = PurchaseOrderFixture.PO_NON_B2B_OPEN
                .createPurchaseOrderdDocument(documentService);
        po.setVendorPaymentTermsCode("00N30");
        po.refreshNonUpdateableReferences();
        
        // Manually set discount percent to zero for this test
        PaymentTermType paymentTerms = po.getVendorPaymentTerms();
        if (paymentTerms != null) {
            paymentTerms.setVendorPaymentTermsPercent(BigDecimal.ZERO);
        }
        
        changeCurrentUser(UserNameFixture.mls398);
        
        PaymentRequestDocument preq = (PaymentRequestDocument) documentService
                .getNewDocument(PaymentRequestDocument.class);
        preq.initiateDocument();
        preq.setInvoiceNumber("TEST-NO-DISC-001");
        preq.setInvoiceDate(java.sql.Date.valueOf(LocalDate.now()));
        preq.setVendorInvoiceAmount(new KualiDecimal(1000));
        preq.setPurchaseOrderIdentifier(po.getPurapDocumentIdentifier());
        
        preq.populatePaymentRequestFromPurchaseOrder(po);
        
        // Call removeIneligibleAdditionalCharges which should remove discount items with no percentage
        cuPaymentRequestService.removeIneligibleAdditionalCharges(preq);
        
        PaymentRequestItem discountItem = findDiscountItem(preq);
        
        if (discountItem != null) {
            LOG.warn("testDiscountRemovedWhenNoDiscountPercent: Discount item still exists with extended price={}", 
                    discountItem.getExtendedPrice());
            fail("Discount item should be removed when payment terms have no discount percentage");
        }
        
        LOG.info("testDiscountRemovedWhenNoDiscountPercent: Test completed successfully - no discount item found");
    }

    // Helper methods
    
    private PaymentRequestItem findDiscountItem(PaymentRequestDocument preq) {
        for (Object item : preq.getItems()) {
            if (item instanceof PaymentRequestItem) {
                PaymentRequestItem preqItem = (PaymentRequestItem) item;
                if (PurapConstants.ItemTypeCodes.ITEM_TYPE_PMT_TERMS_DISCOUNT_CODE.equals(
                        preqItem.getItemTypeCode())) {
                    return preqItem;
                }
            }
        }
        return null;
    }

    private PaymentRequestDto createTestPaymentRequestDto(PurchaseOrderDocument po, String invoiceAmount) {
        PaymentRequestDto dto = new PaymentRequestDto();
        dto.setVendorNumber(po.getVendorNumber());
        dto.setPoNumber(po.getPurapDocumentIdentifier().toString());
        dto.setInvoiceDate(LocalDate.now().format(DATE_FORMATTER));
        dto.setReceivedDate(LocalDate.now().format(DATE_FORMATTER));
        dto.setInvoiceNumber("TEST-API-" + System.currentTimeMillis());
        dto.setInvoiceAmount(invoiceAmount);
        dto.setItems(new ArrayList<>());
        dto.setNotes(new ArrayList<>());
        return dto;
    }
}
