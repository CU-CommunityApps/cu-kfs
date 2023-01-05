package edu.cornell.kfs.module.cam.document.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.module.cam.businessobject.AssetGlobal;
import org.kuali.kfs.module.cam.businessobject.AssetGlobalDetail;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.module.cam.fixture.AssetGlobalDetailFixture;
import edu.cornell.kfs.module.cam.fixture.AssetGlobalFixture;

@ConfigureContext
public class CuAssetGlobalServiceImplIntegTest extends KualiIntegTestBase {
	
	private CuAssetGlobalServiceImpl assetGlobalService;
	
	private static final Logger LOG = LogManager.getLogger();
	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		assetGlobalService = SpringContext.getBean(CuAssetGlobalServiceImpl.class);
	}
	
	public void testSetupAsset() {
		AssetGlobal assetGlobal = AssetGlobalFixture.ONE.createAssetGlobal();
		AssetGlobalDetail assetGlobalDetail = AssetGlobalDetailFixture.ONE.createAssetGlobalDetail();
		
		Asset asset = assetGlobalService.setupAsset(assetGlobal, assetGlobalDetail, false);
		
		assertTrue("Asset was not null", null!=asset);
		LOG.info("Asset created was not null");
	}
	

}
