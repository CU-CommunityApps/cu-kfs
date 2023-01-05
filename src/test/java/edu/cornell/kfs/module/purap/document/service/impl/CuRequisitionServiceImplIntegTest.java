package edu.cornell.kfs.module.purap.document.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.IntegTestUtils;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.kfs.kew.api.exception.WorkflowException;

import edu.cornell.kfs.module.purap.fixture.RequisitionFixture;
import edu.cornell.kfs.module.purap.fixture.RequisitionItemFixture;


@ConfigureContext(session = UserNameFixture.ccs1)
public class CuRequisitionServiceImplIntegTest extends KualiIntegTestBase {
	
	private static final Logger LOG = LogManager.getLogger();

	private CuRequisitionServiceImpl cuRequisitionServiceImpl;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		cuRequisitionServiceImpl = (CuRequisitionServiceImpl)IntegTestUtils.getUnproxiedService("requisitionService");

	}
	
	public void testCheckAutomaticPurchaseOrderRules() throws WorkflowException{
		
		RequisitionDocument requisitionDocument = RequisitionFixture.REQ_NON_B2B_CAP_ASSET_ITEM.createRequisition();
		String result = cuRequisitionServiceImpl.checkAutomaticPurchaseOrderRules(requisitionDocument);
		// validation for capital asset items was removed with customization so validation should not return anything
		
		assertEquals(KFSConstants.EMPTY_STRING, result);
		
	}
	
	public void testCheckAPORulesPerItemForCommodityCodes_Active() throws WorkflowException{
		RequisitionDocument requisitionDocument = RequisitionFixture.REQ_NON_B2B_CAP_ASSET_ITEM.createRequisition();
		String result = cuRequisitionServiceImpl.checkAPORulesPerItemForCommodityCodes(RequisitionItemFixture.REQ_ITEM.createRequisitionItem(true), requisitionDocument.getVendorDetail().getVendorCommodities(), true);
		
		// check for active commodity code removed in customization, active commodity code should be fine
		assertEquals(KFSConstants.EMPTY_STRING, result);
	}
	
	public void testCheckAPORulesPerItemForCommodityCodes_Inactive() throws WorkflowException{
		RequisitionDocument requisitionDocument = RequisitionFixture.REQ_NON_B2B_CAP_ASSET_ITEM_INACTIVE_COMM_CODE.createRequisition();
		String result = cuRequisitionServiceImpl.checkAPORulesPerItemForCommodityCodes(RequisitionItemFixture.REQ_ITEM_INACTIVE_COMM_CD.createRequisitionItem(true), requisitionDocument.getVendorDetail().getVendorCommodities(), true);
		
		// check for active commodity code removed in customization, inactive commodity code should be fine
		assertEquals(KFSConstants.EMPTY_STRING, result);
	}

}
