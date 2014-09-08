package edu.cornell.kfs.module.cam.document.service.impl;

import java.util.ArrayList;

import org.kuali.kfs.module.cam.businessobject.AssetGlobalDetail;
import org.kuali.kfs.module.cam.document.service.impl.AssetServiceImpl;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.module.cam.businessobject.lookup.CuAssetLookupableHelperServiceImpl;
import edu.cornell.kfs.module.cam.fixture.AssetGlobalDetailFixture;

@ConfigureContext
public class CuAssetServiceImplTest extends KualiTestBase {
	
	private CuAssetServiceImpl cuAssetServiceImpl;
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuAssetServiceImpl.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		cuAssetServiceImpl = (CuAssetServiceImpl) SpringContext.getBean(AssetServiceImpl.class);
	}
	
	public void testFindActiveAssetsMatchingTagNumber() {
		
		ArrayList resultsArray = (ArrayList)cuAssetServiceImpl.findActiveAssetsMatchingTagNumber("some tag number");

		assertTrue("the size of the array returned is zero", 0==resultsArray.size());
		LOG.info("A non-existent tag number generates zero results");
		
		AssetGlobalDetail assetGlobalDetail = AssetGlobalDetailFixture.ONE.createAssetGlobalDetail();
		
		resultsArray = (ArrayList) cuAssetServiceImpl.findActiveAssetsMatchingTagNumber(assetGlobalDetail.getCampusTagNumber());
		
		assertTrue("the size of the array returned is non-zero", 0!=resultsArray.size());
		LOG.info("Valid tag returns results");
				
	}

}
