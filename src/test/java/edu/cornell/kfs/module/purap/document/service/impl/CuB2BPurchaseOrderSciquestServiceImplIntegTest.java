package edu.cornell.kfs.module.purap.document.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.service.B2BPurchaseOrderService;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.IntegTestUtils;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.fixture.UserNameFixture;

import edu.cornell.kfs.module.purap.fixture.PurchaseOrderFixture;

@ConfigureContext(session = UserNameFixture.ccs1)
public class CuB2BPurchaseOrderSciquestServiceImplIntegTest extends KualiIntegTestBase {
	
	private static final Logger LOG = LogManager.getLogger();

	private B2BPurchaseOrderService b2bPurchaseOrderService; 
	private CuB2BPurchaseOrderSciquestServiceImpl cuB2BPurchaseOrderSciquestServiceImpl;
	private DocumentService documentService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		b2bPurchaseOrderService = SpringContext.getBean(B2BPurchaseOrderService.class);
		cuB2BPurchaseOrderSciquestServiceImpl = (CuB2BPurchaseOrderSciquestServiceImpl)IntegTestUtils.getUnproxiedService("b2bPurchaseOrderService");
		documentService = SpringContext.getBean(DocumentService.class);

	}

	public void testSendPurchaseOrder()
			throws Exception {
		PurchaseOrderDocument po = PurchaseOrderFixture.PO_B2B.createPurchaseOrderdDocument(documentService);

		String errors = b2bPurchaseOrderService.sendPurchaseOrder(po);

		assertTrue(errors.isEmpty());
	}

	public void testGetCxml() throws Exception {
		PurchaseOrderDocument po = PurchaseOrderFixture.PO_B2B.createPurchaseOrderdDocument(documentService);

		String cxml = b2bPurchaseOrderService.getCxml(po, UserNameFixture.ccs1.getPerson().getPrincipalId(), cuB2BPurchaseOrderSciquestServiceImpl.getB2bPurchaseOrderPassword(), po.getVendorContract().getContractManager(), cuB2BPurchaseOrderSciquestServiceImpl.getContractManagerEmail(po.getVendorContract().getContractManager()), po.getVendorDetail().getVendorDunsNumber());

		assertTrue(!cxml.isEmpty());

	}
	
	public void testVerifyCxmlPOData_Valid() throws Exception {
		PurchaseOrderDocument po = PurchaseOrderFixture.PO_B2B_CXML_VALIDATION.createPurchaseOrderdDocument(documentService);

		String errors = b2bPurchaseOrderService.verifyCxmlPOData(po, UserNameFixture.ccs1.getPerson().getPrincipalId(), cuB2BPurchaseOrderSciquestServiceImpl.getB2bPurchaseOrderPassword(), po.getVendorContract().getContractManager(), cuB2BPurchaseOrderSciquestServiceImpl.getContractManagerEmail(po.getVendorContract().getContractManager()), po.getVendorDetail().getVendorDunsNumber());

		assertTrue(errors.isEmpty());

	}
	
	
	public void testVerifyCxmlPOData_InValid() throws Exception {
		
		String errors = KFSConstants.EMPTY_STRING;
		PurchaseOrderDocument po = PurchaseOrderFixture.PO_B2B_CXML_VALIDATION_INVALID.createPurchaseOrderdDocument(documentService);
		po.setDeliveryPostalCode(null);
		po.setDeliveryStateCode(null);
		
		errors = b2bPurchaseOrderService.verifyCxmlPOData(po, UserNameFixture.ccs1.getPerson().getPrincipalId(), cuB2BPurchaseOrderSciquestServiceImpl.getB2bPurchaseOrderPassword(), po.getVendorContract().getContractManager(), cuB2BPurchaseOrderSciquestServiceImpl.getContractManagerEmail(po.getVendorContract().getContractManager()), po.getVendorDetail().getVendorDunsNumber());
		
		assertFalse(errors.isEmpty());

	}


}
