package edu.cornell.kfs.vnd.batch.service.impl;

//import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

import edu.cornell.kfs.vnd.document.service.CUVendorService;
import edu.cornell.kfs.vnd.fixture.VendorDetailFixture;

@ConfigureContext
public class CuVendorServiceImplIntegTest extends KualiIntegTestBase {
	
	private CUVendorService cuVendorService;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		cuVendorService = SpringContext.getBean(CUVendorService.class);
	}
	
	public void testGetVendorByVendorName() {
		VendorDetail anakFixtureDetail = VendorDetailFixture.ANAK_INC.createVendorDetail();
		VendorDetail anakDBDetail = cuVendorService.getVendorByVendorName("Anak Inc");
		VendorDetail addFixtureDetail = VendorDetailFixture.ADD_ASSOCIATES_INC.createVendorDetail();
		VendorDetail addDBDetail = cuVendorService.getVendorByVendorName("ADD Associates Inc");
		
		assertEquals(anakFixtureDetail.isVendorParentIndicator(), anakDBDetail.isVendorParentIndicator());
		assertEquals(anakFixtureDetail.getVendorDetailAssignedIdentifier(), anakDBDetail.getVendorDetailAssignedIdentifier());
		
		assertEquals(addFixtureDetail.isVendorParentIndicator(), addDBDetail.isVendorParentIndicator());
		assertEquals(addFixtureDetail.getVendorDetailAssignedIdentifier(), addDBDetail.getVendorDetailAssignedIdentifier());
		
	}
	
}
