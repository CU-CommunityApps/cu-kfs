package edu.cornell.kfs.vnd.batch.service.impl;

import java.io.File;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;

import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.CommodityCode;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.vnd.batch.service.CommodityCodeUpdateService;

@ConfigureContext
public class CuCommodityCodeUpdateServiceImplTest extends KualiTestBase {
	
	private CommodityCodeUpdateService commodityCodeUpdateService;
    private ConfigurationService  kualiConfigurationService;

	private static final String DATA_FILE_PATH = "src/test/resources/edu/cornell/kfs/vnd/fixture/commodityCodeFlatFile.txt";
    
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		commodityCodeUpdateService = SpringContext.getBean(CommodityCodeUpdateService.class);
	}
	
	public void testLoadCommodityCodeFile() {
		byte[] zeroLength = {};
		ArrayList<CommodityCode> noList = new ArrayList();
		byte[] noContent = commodityCodeUpdateService.getFileContent("no such file");
		ArrayList<CommodityCode> noCommodityList = (ArrayList<CommodityCode>) commodityCodeUpdateService.parseCommodityCodeList(zeroLength);
		byte[] content = commodityCodeUpdateService.getFileContent(DATA_FILE_PATH);
		ArrayList<CommodityCode> someCommodityCodes = (ArrayList<CommodityCode>) commodityCodeUpdateService.parseCommodityCodeList(content);
		
		assertEquals(noContent.length, zeroLength.length);
		assertEquals(noCommodityList.size(), noList.size());
		assertNotSame(content.length, 0);
		assertNotSame(someCommodityCodes.size(), 0);
		
		assert(commodityCodeUpdateService.loadCommodityCodeFile(DATA_FILE_PATH));
	}

}
