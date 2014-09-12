package edu.cornell.kfs.module.purap.document.service.impl;

import java.io.ByteArrayOutputStream;

import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.dataaccess.PurchaseOrderDao;
import org.kuali.kfs.module.purap.document.service.PurchaseOrderService;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.krad.exception.ValidationException;
import org.kuali.rice.krad.service.DocumentService;

import edu.cornell.kfs.module.purap.fixture.PurchaseOrderFixture;

@ConfigureContext(session = UserNameFixture.ccs1)
public class CuPurchaseOrderServiceImplTest extends KualiTestBase {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(CuPurchaseOrderServiceImplTest.class);

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

		ByteArrayOutputStream baosPDF = new ByteArrayOutputStream();
		try {
			DateTimeService dtService = SpringContext
					.getBean(DateTimeService.class);
			StringBuffer sbFilename = new StringBuffer();
			sbFilename.append("PURAP_PO_QUOTE_REQUEST_LIST");
			sbFilename.append(po.getPurapDocumentIdentifier());
			sbFilename.append("_");
			sbFilename.append(dtService.getCurrentDate().getTime());
			sbFilename.append(".pdf");
			purchaseOrderService.performPurchaseOrderFirstTransmitViaPrinting(
					po.getDocumentNumber(), baosPDF);
			assertTrue(baosPDF.size() > 0);
		} catch (ValidationException e) {
			LOG.warn("Caught ValidationException while trying to retransmit PO with doc id "
					+ po.getDocumentNumber());
		} finally {
			if (baosPDF != null) {
				baosPDF.reset();
			}
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
