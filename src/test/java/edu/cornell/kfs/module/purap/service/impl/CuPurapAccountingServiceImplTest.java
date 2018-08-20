package edu.cornell.kfs.module.purap.service.impl;

import java.math.BigDecimal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapConstants.PurchaseOrderStatuses;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestAccount;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.RequisitionItem;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.context.TestUtils;
import org.kuali.kfs.sys.document.AccountingDocumentTestUtils;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.kfs.krad.service.DocumentService;

import edu.cornell.kfs.module.purap.fixture.PaymentRequestFixture;
import edu.cornell.kfs.module.purap.fixture.PurapAccountingLineFixture;
import edu.cornell.kfs.module.purap.fixture.RequisitionFixture;
import edu.cornell.kfs.module.purap.fixture.RequisitionItemFixture;

@ConfigureContext(session = UserNameFixture.ccs1)
public class CuPurapAccountingServiceImplTest extends KualiTestBase {

	private static final Logger LOG = LogManager.getLogger(CuPurapAccountingServiceImplTest.class);

	private CuPurapAccountingServiceImpl cuPurapAccountingServiceImpl;
	private DocumentService documentService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		cuPurapAccountingServiceImpl = (CuPurapAccountingServiceImpl) TestUtils.getUnproxiedService("purapAccountingService");
		documentService = SpringContext.getBean(DocumentService.class);

	}
	
	public void testIsFiscalOfficersForAllAcctLines_True() throws Exception {
		RequisitionDocument requisitionDocument = RequisitionFixture.REQ_NON_B2B_WITH_ITEMS.createRequisition();
		requisitionDocument.getFinancialSystemDocumentHeader().setFinancialDocumentTotalAmount(new KualiDecimal(200));
		requisitionDocument.addItem(RequisitionItemFixture.REQ_ITEM3.createRequisitionItem(true));
		changeCurrentUser(UserNameFixture.nja3);
		assertTrue(cuPurapAccountingServiceImpl.isFiscalOfficersForAllAcctLines(requisitionDocument));
	}

	public void testIsFiscalOfficersForAllAcctLines_False() throws Exception {
		RequisitionDocument requisitionDocument = RequisitionFixture.REQ_NON_B2B_WITH_ITEMS.createRequisition();
		requisitionDocument.getFinancialSystemDocumentHeader().setFinancialDocumentTotalAmount(new KualiDecimal(200));
		requisitionDocument.addItem(RequisitionItemFixture.REQ_ITEM2.createRequisitionItem(true));
		changeCurrentUser(UserNameFixture.nja3);

		assertFalse(cuPurapAccountingServiceImpl.isFiscalOfficersForAllAcctLines(requisitionDocument));
	}

	public void testUpdateAccountAmounts_AccountingLinePercentChanged() throws Exception {
		changeCurrentUser(UserNameFixture.ccs1);

		// Save the requisition with items, but without accounting lines and then add the accounting lines and save again
		// This odd methodology is to workaround an NPE that occurs when access security is enabled and refreshNonUpdatableReferences
		// is called on the account. For some reason the RequisitionItem cannot be found in ojb's cache and so when
		// it is attempted to be instantiated and constructor methods called, an NPE is thrown. This little dance works around the exception.
		// More analysis could probably be done to determine the root cause and address it, but for now this is good enough.
		RequisitionDocument requisitionDocument = RequisitionFixture.REQ_NON_B2B_WITH_ITEMS.createRequisition(documentService);
		requisitionDocument.getFinancialSystemDocumentHeader().setFinancialDocumentTotalAmount(new KualiDecimal(200));
		RequisitionItem item = RequisitionItemFixture.REQ_ITEM3.createRequisitionItem(false);
		requisitionDocument.addItem(item);
		AccountingDocumentTestUtils.saveDocument(requisitionDocument, documentService);
		requisitionDocument.refreshNonUpdateableReferences();

		item.getSourceAccountingLines().add(PurapAccountingLineFixture.REQ_ITEM_ACCT_LINE3.createRequisitionAccount(item.getItemIdentifier()));
		item.getSourceAccountingLines().add(PurapAccountingLineFixture.REQ_ITEM_ACCT_LINE3.createRequisitionAccount(item.getItemIdentifier()));
		item.refreshNonUpdateableReferences();

		AccountingDocumentTestUtils.saveDocument(requisitionDocument, documentService);

		PurchaseOrderDocument purchaseOrderDocument = (PurchaseOrderDocument) documentService.getNewDocument(PurchaseOrderDocument.class);

		purchaseOrderDocument
				.populatePurchaseOrderFromRequisition(requisitionDocument);

		purchaseOrderDocument.setContractManagerCode(10);
		purchaseOrderDocument.setPurchaseOrderCurrentIndicator(true);
		purchaseOrderDocument.getDocumentHeader().setDocumentDescription("Description");
		purchaseOrderDocument.setApplicationDocumentStatus(PurchaseOrderStatuses.APPDOC_OPEN);

		purchaseOrderDocument.refreshNonUpdateableReferences();

		purchaseOrderDocument.setVendorShippingPaymentTermsCode("AL");
		purchaseOrderDocument.setVendorPaymentTermsCode("00N30");
		purchaseOrderDocument.refreshNonUpdateableReferences();
		AccountingDocumentTestUtils.saveDocument(purchaseOrderDocument, documentService);

		changeCurrentUser(UserNameFixture.mo14);
		PaymentRequestDocument paymentRequestDocument = PaymentRequestFixture.PAYMENT_REQ_DOC.createPaymentRequestDocument(purchaseOrderDocument.getPurapDocumentIdentifier());
		paymentRequestDocument.initiateDocument();
		paymentRequestDocument.populatePaymentRequestFromPurchaseOrder(purchaseOrderDocument);
		paymentRequestDocument.setApplicationDocumentStatus(PurapConstants.PaymentRequestStatuses.APPDOC_AWAITING_FISCAL_REVIEW);

		paymentRequestDocument.getItem(0).setExtendedPrice(new KualiDecimal(1));
		paymentRequestDocument.getItem(1).setExtendedPrice(new KualiDecimal(4));

		((PaymentRequestAccount) (((PaymentRequestItem) paymentRequestDocument.getItems().get(1)).getSourceAccountingLine(0))).setAmount(new KualiDecimal(3));
		((PaymentRequestAccount) (((PaymentRequestItem) paymentRequestDocument.getItems().get(1)).getSourceAccountingLine(1))).setAmount(new KualiDecimal(1));
		cuPurapAccountingServiceImpl.updateAccountAmounts(paymentRequestDocument);

		assertEquals(new BigDecimal(75).setScale(2),((PaymentRequestAccount) (((PaymentRequestItem) paymentRequestDocument.getItems().get(1)).getSourceAccountingLine(0))).getAccountLinePercent());
	}

	public void testUpdateAccountAmounts_AccountingLinePercentUnchanged() throws Exception {

		changeCurrentUser(UserNameFixture.ccs1);

		// Save the requisition with items, but without accounting lines and then add the accounting lines and save again
		// This odd methodology is to workaround an NPE that occurs when access security is enabled and refreshNonUpdatableReferences
		// is called on the account. For some reason the RequisitionItem cannot be found in ojb's cache and so when
		// it is attempted to be instantiated and constructor methods called, an NPE is thrown. This little dance works around the exception.
		// More analysis could probably be done to determine the root cause and address it, but for now this is good enough.
		RequisitionDocument requisitionDocument = RequisitionFixture.REQ_NON_B2B_WITH_ITEMS.createRequisition(documentService);
		requisitionDocument.getFinancialSystemDocumentHeader().setFinancialDocumentTotalAmount(new KualiDecimal(200));
		RequisitionItem item = RequisitionItemFixture.REQ_ITEM3.createRequisitionItem(false);
		requisitionDocument.addItem(item);
		AccountingDocumentTestUtils.saveDocument(requisitionDocument, documentService);
		requisitionDocument.refreshNonUpdateableReferences();

		item.getSourceAccountingLines().add(PurapAccountingLineFixture.REQ_ITEM_ACCT_LINE3.createRequisitionAccount(item.getItemIdentifier()));
		item.getSourceAccountingLines().add(PurapAccountingLineFixture.REQ_ITEM_ACCT_LINE3.createRequisitionAccount(item.getItemIdentifier()));
		item.refreshNonUpdateableReferences();

		AccountingDocumentTestUtils.saveDocument(requisitionDocument, documentService);

		PurchaseOrderDocument purchaseOrderDocument = (PurchaseOrderDocument) documentService.getNewDocument(PurchaseOrderDocument.class);

		purchaseOrderDocument.populatePurchaseOrderFromRequisition(requisitionDocument);

		purchaseOrderDocument.setContractManagerCode(10);
		purchaseOrderDocument.setPurchaseOrderCurrentIndicator(true);
		purchaseOrderDocument.getDocumentHeader().setDocumentDescription("Description");
		purchaseOrderDocument.setApplicationDocumentStatus(PurchaseOrderStatuses.APPDOC_OPEN);

		purchaseOrderDocument.refreshNonUpdateableReferences();

		purchaseOrderDocument.setVendorShippingPaymentTermsCode("AL");
		purchaseOrderDocument.setVendorPaymentTermsCode("00N30");
		purchaseOrderDocument.refreshNonUpdateableReferences();
		AccountingDocumentTestUtils.saveDocument(purchaseOrderDocument, documentService);

		changeCurrentUser(UserNameFixture.mo14);
		PaymentRequestDocument paymentRequestDocument = PaymentRequestFixture.PAYMENT_REQ_DOC.createPaymentRequestDocument(purchaseOrderDocument.getPurapDocumentIdentifier());
		paymentRequestDocument.initiateDocument();
		paymentRequestDocument.populatePaymentRequestFromPurchaseOrder(purchaseOrderDocument);
		paymentRequestDocument.setApplicationDocumentStatus(PurapConstants.PaymentRequestStatuses.APPDOC_IN_PROCESS);

		paymentRequestDocument.getItem(0).setExtendedPrice(new KualiDecimal(1));
		paymentRequestDocument.getItem(1).setExtendedPrice(new KualiDecimal(4));

		((PaymentRequestAccount) (((PaymentRequestItem) paymentRequestDocument.getItems().get(1)).getSourceAccountingLine(0))).setAmount(new KualiDecimal(3));
		((PaymentRequestAccount) (((PaymentRequestItem) paymentRequestDocument.getItems().get(1)).getSourceAccountingLine(1))).setAmount(new KualiDecimal(1));
		cuPurapAccountingServiceImpl.updateAccountAmounts(paymentRequestDocument);

		assertEquals(new BigDecimal(50).setScale(2),((PaymentRequestAccount) (((PaymentRequestItem) paymentRequestDocument.getItems().get(1)).getSourceAccountingLine(0))).getAccountLinePercent());

	}
}
