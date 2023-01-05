package edu.cornell.kfs.module.purap.document.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.dataaccess.PurchaseOrderDao;
import org.kuali.kfs.module.purap.document.service.PurchaseOrderService;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.fixture.UserNameFixture;

import edu.cornell.kfs.module.purap.fixture.PurchaseOrderFixture;

@ConfigureContext(session = UserNameFixture.ccs1)
public class CuPurchaseOrderServiceImplIntegTest extends KualiIntegTestBase {

	private static final Logger LOG = LogManager.getLogger();

	private PurchaseOrderService purchaseOrderService;
	protected PurchaseOrderDao purchaseOrderDao;
	private DocumentService documentService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		purchaseOrderService = SpringContext.getBean(PurchaseOrderService.class);
		purchaseOrderDao = SpringContext.getBean(PurchaseOrderDao.class);
		documentService = SpringContext.getBean(DocumentService.class);

	}

	public void testPerformPurchaseOrderFirstTransmitViaPrinting()
			throws Exception {

		PurchaseOrderDocument po = PurchaseOrderFixture.PO_NON_B2B_OPEN.createPurchaseOrderdDocument(documentService);

		try {
			purchaseOrderService.performPurchaseOrderFirstTransmitViaPrinting(po);
			assertTrue(po.getPurchaseOrderFirstTransmissionTimestamp() != null);
			assertTrue(po.getPurchaseOrderLastTransmitTimestamp() != null);
		} catch (ValidationException e) {
			LOG.warn("Caught ValidationException while trying to retransmit PO with doc id " + po.getDocumentNumber());
		}
	}

	/**
	 * Tests that the PurchaseOrderService would do the completePurchaseOrder
	 * for non B2B purchase orders.
	 * 
	 * @throws Exception
	 */
	public void testCompletePurchaseOrderAmendment_NonB2B() throws Exception {
		PurchaseOrderDocument po = PurchaseOrderFixture.PO_NON_B2B_IN_PROCESS.createPurchaseOrderdDocument(documentService);

		purchaseOrderService.completePurchaseOrderAmendment(po);

		assertTrue(po.isPurchaseOrderCurrentIndicator());
		assertFalse(po.isPendingActionIndicator());

	}

	/**
	 * Tests that the PurchaseOrderService would do the completePurchaseOrder
	 * for B2B purchase orders.
	 * 
	 * @throws Exception
	 */
	public void testCompletePurchaseOrderAmendment_B2B() throws Exception {
		PurchaseOrderDocument po = PurchaseOrderFixture.PO_B2B.createPurchaseOrderdDocument(documentService);
		po.setPurchaseOrderTransmissionMethodCode(PurapConstants.POTransmissionMethods.ELECTRONIC);
		purchaseOrderService.completePurchaseOrderAmendment(po);

		assertTrue(po.isPurchaseOrderCurrentIndicator());
		assertFalse(po.isPendingActionIndicator());
	    assertNotNull(po.getPurchaseOrderLastTransmitTimestamp());

	}

}
