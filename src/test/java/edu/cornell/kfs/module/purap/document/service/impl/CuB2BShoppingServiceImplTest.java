package edu.cornell.kfs.module.purap.document.service.impl;

import java.util.List;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.module.purap.businessobject.RequisitionItem;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.util.cxml.B2BShoppingCart;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.IntegTestUtils;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.fixture.UserNameFixture;

import edu.cornell.kfs.module.purap.fixture.B2BShoppingCartItemFixture;
import edu.cornell.kfs.module.purap.fixture.CuB2BShoppingCartFixture;

@ConfigureContext(session = UserNameFixture.ccs1)
public class CuB2BShoppingServiceImplTest extends KualiIntegTestBase {
	private static final Logger LOG = LogManager.getLogger(CuB2BShoppingServiceImplTest.class);

	private CuB2BShoppingServiceImpl b2bShoppingService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		b2bShoppingService = (CuB2BShoppingServiceImpl) IntegTestUtils.getUnproxiedService("b2BShoppingService");

	}

	public void testCreateRequisitionsFromCxml() throws Exception {
		B2BShoppingCart cart = CuB2BShoppingCartFixture.B2B_CART_USING_VENDOR_ID.createB2BShoppingCart();

		List<RequisitionDocument> requisitions = b2bShoppingService.createRequisitionsFromCxml(cart, UserNameFixture.ccs1.getPerson());

		assertNotNull(requisitions);
	}

	public void testCreateRequisitionItem() {
		RequisitionItem  requisitionItem = b2bShoppingService.createRequisitionItem(B2BShoppingCartItemFixture.B2B_ITEM_USING_VENDOR_ID.createB2BShoppingCartItem(), 0, "80141605");
		assertNotNull(requisitionItem);
		assertTrue(requisitionItem.isControlled());
		assertTrue(requisitionItem.isRadioactiveMinor());
		assertTrue(requisitionItem.isGreen());
		assertTrue(requisitionItem.isHazardous());
		assertTrue(requisitionItem.isSelectAgent());
		assertTrue(requisitionItem.isRadioactive());
		assertTrue(requisitionItem.isToxin());
		assertTrue(requisitionItem.isRecycled());
		assertTrue(requisitionItem.isEnergyStar());
	}

	public void testCreateRequisitionItemTruncateDescription() {
		RequisitionItem  requisitionItem = b2bShoppingService.createRequisitionItem(B2BShoppingCartItemFixture.B2B_ITEM_WITH_LONG_DESCRIPTION.createB2BShoppingCartItem(), 0, "80141605");
		assertNotNull(requisitionItem);
		assertTrue(requisitionItem.isControlled());
		assertTrue(requisitionItem.isRadioactiveMinor());
		assertTrue(requisitionItem.isGreen());
		assertTrue(requisitionItem.isHazardous());
		assertTrue(requisitionItem.isSelectAgent());
		assertTrue(requisitionItem.isRadioactive());
		assertTrue(requisitionItem.isToxin());
		assertTrue(requisitionItem.isRecycled());
		assertTrue(requisitionItem.isEnergyStar());
		assertTrue(requisitionItem.getItemDescription().length() == CUPurapConstants.REQUISITION_ITEM_DESCRIPTION_MAX_LENGTH);
	}

}
