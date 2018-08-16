package edu.cornell.kfs.module.purap.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.service.PurchaseOrderService;
import org.kuali.kfs.module.purap.service.PurapAccountingService;
import org.kuali.kfs.module.purap.util.SummaryAccount;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.context.TestUtils;
import org.kuali.kfs.sys.document.AccountingDocumentTestUtils;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.kfs.krad.service.DocumentService;

import edu.cornell.kfs.module.purap.document.CuPaymentRequestDocument;
import edu.cornell.kfs.module.purap.fixture.PaymentRequestFixture;
import edu.cornell.kfs.module.purap.fixture.PurchaseOrderFixture;

@ConfigureContext(session = UserNameFixture.mo14)
public class CuPurapGeneralLedgerServiceImplTest extends KualiTestBase {
	
	private static final Logger LOG = LogManager.getLogger(CuPurapGeneralLedgerServiceImplTest.class);

	private CuPurapGeneralLedgerServiceImpl cuPurapGeneralLedgerServiceImpl;
	private PurapAccountingService purapAccountingService;
	private PurchaseOrderService purchaseOrderService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		cuPurapGeneralLedgerServiceImpl = (CuPurapGeneralLedgerServiceImpl)TestUtils.getUnproxiedService("purapGeneralLedgerService");
		purapAccountingService = SpringContext.getBean(PurapAccountingService.class);
		purchaseOrderService = SpringContext.getBean(PurchaseOrderService.class);

	}
	
	public void testGenerateEntriesPaymentRequest() throws Exception{

		changeCurrentUser(UserNameFixture.ccs1);
		PurchaseOrderDocument po = PurchaseOrderFixture.PO_NON_B2B_OPEN_WITH_ITEMS
				.createPurchaseOrderdDocument(SpringContext
						.getBean(DocumentService.class));
		
		po.setVendorShippingPaymentTermsCode("AL");
		po.setVendorPaymentTermsCode("00N30");
		po.refreshNonUpdateableReferences();


		changeCurrentUser(UserNameFixture.mo14);
		CuPaymentRequestDocument preq = (CuPaymentRequestDocument)PaymentRequestFixture.PAYMENT_REQ_DOC.createPaymentRequestDocument(po.getPurapDocumentIdentifier());
		preq.initiateDocument();
		preq.populatePaymentRequestFromPurchaseOrder(po);
		preq.setPaymentMethodCode("F");
		preq.setApplicationDocumentStatus(PurapConstants.PaymentRequestStatuses.APPDOC_AWAITING_ACCOUNTS_PAYABLE_REVIEW);
		((PaymentRequestItem)(preq.getItems().get(0))).setExtendedPrice(new KualiDecimal(1));
		
		AccountingDocumentTestUtils.saveDocument(preq, SpringContext.getBean(DocumentService.class));

		
		List<SummaryAccount> summaryAccounts = purapAccountingService.generateSummaryAccountsWithNoZeroTotalsNoUseTax(preq);
		cuPurapGeneralLedgerServiceImpl.generateEntriesPaymentRequest(preq, null, summaryAccounts, "create");
		
		boolean noBankOffsetGenerated = true;
		for(GeneralLedgerPendingEntry pe : preq.getGeneralLedgerPendingEntries()){
			if(KFSKeyConstants.Bank.DESCRIPTION_GLPE_BANK_OFFSET.equalsIgnoreCase(pe.getTransactionLedgerEntryDescription())){
				noBankOffsetGenerated = false;
				break;
			}
		}
		
		assertEquals(2, preq.getGeneralLedgerPendingEntries().size());
		assertTrue(noBankOffsetGenerated);
	}
	
	public void testReencumberEncumbrance() throws Exception{
		changeCurrentUser(UserNameFixture.ccs1);
		PurchaseOrderDocument po = PurchaseOrderFixture.PO_NON_B2B_OPEN_WITH_ITEMS
				.createPurchaseOrderdDocument(SpringContext
						.getBean(DocumentService.class));
		
		po.setVendorShippingPaymentTermsCode("AL");
		po.setVendorPaymentTermsCode("00N30");
		po.getItem(0).setItemQuantity(new KualiDecimal(6));
		((PurchaseOrderItem)po.getItem(0)).setItemInvoicedTotalQuantity(new KualiDecimal(4));
		po.refreshNonUpdateableReferences();
		AccountingDocumentTestUtils.saveDocument(po,  SpringContext.getBean(DocumentService.class));


		changeCurrentUser(UserNameFixture.mo14);
		CuPaymentRequestDocument preq = (CuPaymentRequestDocument)PaymentRequestFixture.PAYMENT_REQ_DOC.createPaymentRequestDocument(po.getPurapDocumentIdentifier());
		preq.initiateDocument();
		preq.populatePaymentRequestFromPurchaseOrder(po);
		preq.setPaymentMethodCode("F");
		preq.setApplicationDocumentStatus(PurapConstants.PaymentRequestStatuses.APPDOC_AWAITING_ACCOUNTS_PAYABLE_REVIEW);
		((PaymentRequestItem)(preq.getItems().get(0))).setExtendedPrice(KualiDecimal.ZERO);
		((PaymentRequestItem)(preq.getItems().get(0))).setItemUnitPrice(BigDecimal.ZERO);
		((PaymentRequestItem)(preq.getItems().get(0))).setItemQuantity(new KualiDecimal(2));
		
		AccountingDocumentTestUtils.saveDocument(preq, SpringContext.getBean(DocumentService.class));


		cuPurapGeneralLedgerServiceImpl.reencumberEncumbrance(preq);

		PurchaseOrderDocument po2 = purchaseOrderService.getCurrentPurchaseOrder(preq.getPurchaseOrderIdentifier());
		assertEquals(new KualiDecimal(2), ((PurchaseOrderItem)po2.getItem(0)).getItemInvoicedTotalQuantity());
	}
}
