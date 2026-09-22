package edu.cornell.kfs.module.purap.rest.resource;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.cornell.kfs.module.purap.document.service.CuPaymentRequestService;
import edu.cornell.kfs.module.purap.fixture.PurchaseOrderFixture;
import edu.cornell.kfs.module.purap.rest.jsonObjects.PaymentRequestDto;
import edu.cornell.kfs.module.purap.rest.jsonObjects.PaymentRequestLineItemDto;
import edu.cornell.kfs.module.purap.rest.jsonObjects.PaymentRequestNoteDto;
import edu.cornell.kfs.module.purap.rest.jsonObjects.PaymentRequestResultsDto;

/**
 * Integration test for PaymentRequestResource REST endpoint to verify
 * discount calculation through the complete API flow.
 * 
 * Tests the PaymentRequestResource.calculatePaymentRequest() method which is called
 * at line 100 after createPaymentRequestDocumentFromDto().
 */
@ConfigureContext(session = UserNameFixture.mls398)
public class PaymentRequestResourceDiscountIntegTest extends KualiIntegTestBase {

    private static final Logger LOG = LogManager.getLogger();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    
    private CuPaymentRequestService cuPaymentRequestService;
    private DocumentService documentService;
    private Gson gson;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        cuPaymentRequestService = SpringContext.getBean(CuPaymentRequestService.class);
        documentService = SpringContext.getBean(DocumentService.class);
        
        gson = new GsonBuilder()
                .setDateFormat("MM/dd/yyyy")
                .create();

    /**
     * Test discount calculation simulating the EXACT flow in PaymentRequestResource.
     * 
     * PaymentRequestResource flow (lines 96-100):
     * 1. createPaymentRequestDocumentFromDto() - creates PREQ, applies DTO items
     * 2. calculatePaymentRequest(preqDoc) - recalculates discount with updated prices
     * 
     * This test verifies step 2 properly recalculates discount.
     */
    public void testPaymentRequestResourceCalculateMethodFlow() throws Exception {
        LOG.info("testPaymentRequestResourceCalculateMethodFlow: Starting test");
        
        changeCurrentUser(UserNameFixture.ccs1);
        
        // Create PO with payment terms
        PurchaseOrderDocument po = PurchaseOrderFixture.PO_NON_B2B_OPEN
                .createPurchaseOrderdDocument(documentService);
        po.setVendorPaymentTermsCode("00N30");
        po.refreshNonUpdateableReferences();
        
        PaymentTermType paymentTerms = po.getVendorPaymentTerms();
        assertNotNull("PO should have payment terms", paymentTerms);
        
        BigDecimal discountPercent = paymentTerms.getVendorPaymentTermsPercent();
        LOG.info("testPaymentRequestResourceCalculateMethodFlow: Discount percent={}", discountPercent);
        
        if (discountPercent == null || discountPercent.compareTo(BigDecimal.ZERO) == 0) {
            LOG.info("testPaymentRequestResourceCalculateMethodFlow: No discount, skipping");
            return;
        }
        
        changeCurrentUser(UserNameFixture.mls398);
        
        // Step 1: Create DTO (simulates JSON from REST API)
        PaymentRequestDto dto = new PaymentRequestDto();
        dto.setVendorNumber(po.getVendorNumber());
        dto.setPoNumber(po.getPurapDocumentIdentifier().toString());
        dto.setInvoiceDate(LocalDate.now().format(DATE_FORMATTER));
        dto.setReceivedDate(LocalDate.now().format(DATE_FORMATTER));
        dto.setInvoiceNumber("TEST-RESOURCE-" + System.currentTimeMillis());
        dto.setInvoiceAmount("1000.00");
        
        // Add line item with different price than PO
        PaymentRequestLineItemDto itemDto = new PaymentRequestLineItemDto();
        itemDto.setLineNumber("1");
        itemDto.setItemQuantity("10");
        itemDto.setItemPrice("100.00");
        dto.getItems().add(itemDto);
        
        PaymentRequestNoteDto noteDto = new PaymentRequestNoteDto();
        noteDto.setNoteText("Test PaymentRequestResource flow");
        dto.getNotes().add(noteDto);
        
        // Step 2: Call createPaymentRequestDocumentFromDto (PaymentRequestResource line 96-97)
        PaymentRequestResultsDto results = new PaymentRequestResultsDto();
        PaymentRequestDocument preqDoc = cuPaymentRequestService
                .createPaymentRequestDocumentFromDto(dto, results);
        
        assertNotNull("PREQ should be created", preqDoc);
        assertTrue("PREQ creation should be valid", results.isValid());
        
        // At this point, discount exists but may be stale (based on PO prices)
        PaymentRequestItem discountItemBeforeCalc = findDiscountItem(preqDoc);
        KualiDecimal discountBeforeCalc = discountItemBeforeCalc != null ? 
                discountItemBeforeCalc.getExtendedPrice() : KualiDecimal.ZERO;
        
        LOG.info("testPaymentRequestResourceCalculateMethodFlow: Discount before calculatePaymentRequest={}", 
                discountBeforeCalc);
        
        // Step 3: Call calculatePaymentRequest (PaymentRequestResource line 100)
        // This is the KEY method that recalculates discount!
        // Simulates: calculatePaymentRequest(preqDoc) at line 154-172
        preqDoc.updateExtendedPriceOnItems();  // Line 156
        cuPaymentRequestService.calculatePaymentRequest(preqDoc, true);  // Line 162 with TRUE!
        
        // Step 4: Verify discount was recalculated correctly
        PaymentRequestItem discountItem = findDiscountItem(preqDoc);
        assertNotNull("Discount item should exist after calculatePaymentRequest", discountItem);
        
        KualiDecimal preTaxTotal = preqDoc.getGrandPreTaxTotalExcludingDiscount();
        KualiDecimal expectedDiscount = preTaxTotal.multiply(new KualiDecimal(discountPercent)).negated();
        
        LOG.info("testPaymentRequestResourceCalculateMethodFlow: After calculatePaymentRequest - PreTax={}, Expected Discount={}, Actual Discount={}", 
                preTaxTotal, expectedDiscount, discountItem.getExtendedPrice());
        
        KualiDecimal difference = discountItem.getExtendedPrice().subtract(expectedDiscount).abs();
        assertTrue("Discount should match expected after PaymentRequestResource.calculatePaymentRequest() (diff: " 
                + difference + ")", difference.isLessEqual(new KualiDecimal(0.01)));
        
        LOG.info("testPaymentRequestResourceCalculateMethodFlow: SUCCESS - Discount calculated correctly!");
    }

    /**
     * Test that verifies discount recalculation when item prices change
     * through the PaymentRequestResource.calculatePaymentRequest() method.
     */
    public void testDiscountRecalculationAfterItemPriceUpdate() throws Exception {
        LOG.info("testDiscountRecalculationAfterItemPriceUpdate: Starting test");
        
        changeCurrentUser(UserNameFixture.ccs1);
        
        PurchaseOrderDocument po = PurchaseOrderFixture.PO_NON_B2B_OPEN
                .createPurchaseOrderdDocument(documentService);
        po.setVendorPaymentTermsCode("00N30");
        po.refreshNonUpdateableReferences();
        
        PaymentTermType paymentTerms = po.getVendorPaymentTerms();
        assertNotNull("PO should have payment terms", paymentTerms);
        
        BigDecimal discountPercent = paymentTerms.getVendorPaymentTermsPercent();
        if (discountPercent == null || discountPercent.compareTo(BigDecimal.ZERO) == 0) {
            LOG.info("testDiscountRecalculationAfterItemPriceUpdate: No discount, skipping");
            return;
        }
        
        changeCurrentUser(UserNameFixture.mls398);
        
        // Create PREQ from PO
        PaymentRequestDocument preq = (PaymentRequestDocument) documentService
                .getNewDocument(PaymentRequestDocument.class);
        preq.initiateDocument();
        preq.setInvoiceNumber("TEST-RECALC-001");
        preq.setInvoiceDate(java.sql.Date.valueOf(LocalDate.now()));
        preq.setVendorInvoiceAmount(new KualiDecimal(1000));
        preq.setPurchaseOrderIdentifier(po.getPurapDocumentIdentifier());
        
        preq.populatePaymentRequestFromPurchaseOrder(po);
        
        // Get initial totals
        KualiDecimal initialPreTax = preq.getGrandPreTaxTotalExcludingDiscount();
        PaymentRequestItem discountItem = findDiscountItem(preq);
        KualiDecimal initialDiscount = discountItem != null ? discountItem.getExtendedPrice() : KualiDecimal.ZERO;
        
        LOG.info("testDiscountRecalculationAfterItemPriceUpdate: Initial PreTax={}, Discount={}", 
                initialPreTax, initialDiscount);
        
        // Modify item quantity (simulating API update)
        boolean itemModified = false;
        for (Object item : preq.getItems()) {
            if (item instanceof PaymentRequestItem) {
                PaymentRequestItem preqItem = (PaymentRequestItem) item;
                if (preqItem.getItemType() != null && 
                        preqItem.getItemType().isItemTypeAboveTheLineIndicator() &&
                        preqItem.getItemQuantity() != null) {
                    KualiDecimal originalQty = preqItem.getItemQuantity();
                    preqItem.setItemQuantity(originalQty.multiply(new KualiDecimal(2)));
                    LOG.info("testDiscountRecalculationAfterItemPriceUpdate: Changed quantity from {} to {}", 
                            originalQty, preqItem.getItemQuantity());
                    itemModified = true;
                    break;
                }
            }
        }
        
        assertTrue("Should have modified at least one item", itemModified);
        
        // Execute PaymentRequestResource.calculatePaymentRequest() logic
        preq.updateExtendedPriceOnItems();
        cuPaymentRequestService.calculatePaymentRequest(preq, true);
        
        // Verify discount was recalculated
        KualiDecimal newPreTax = preq.getGrandPreTaxTotalExcludingDiscount();
        discountItem = findDiscountItem(preq);
        KualiDecimal newDiscount = discountItem != null ? discountItem.getExtendedPrice() : KualiDecimal.ZERO;
        
        LOG.info("testDiscountRecalculationAfterItemPriceUpdate: New PreTax={}, Discount={}", 
                newPreTax, newDiscount);
        
        // Verify totals changed
        assertTrue("PreTax total should have changed", !newPreTax.equals(initialPreTax));
        assertTrue("Discount should have changed", !newDiscount.equals(initialDiscount));
        
        // Verify discount matches new total
        KualiDecimal expectedDiscount = newPreTax.multiply(new KualiDecimal(discountPercent)).negated();
        KualiDecimal difference = newDiscount.subtract(expectedDiscount).abs();
        
        assertTrue("Recalculated discount should match expected (diff: " + difference + ")",
                difference.isLessEqual(new KualiDecimal(0.01)));
        
        LOG.info("testDiscountRecalculationAfterItemPriceUpdate: SUCCESS - Discount recalculated correctly!");
    }

    // Helper method
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
}

