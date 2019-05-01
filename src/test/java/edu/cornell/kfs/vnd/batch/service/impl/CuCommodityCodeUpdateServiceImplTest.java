package edu.cornell.kfs.vnd.batch.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.vnd.businessobject.CommodityCode;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import edu.cornell.kfs.sys.batch.service.impl.CuBatchInputFileServiceImpl;
import edu.cornell.kfs.vnd.batch.CommodityCodeInputFileType;

public class CuCommodityCodeUpdateServiceImplTest {
	
	private CommodityCodeUpdateServiceImpl commodityCodeUpdateService;

	private static final String DATA_FILE_PATH = "src/test/resources/edu/cornell/kfs/vnd/fixture/commodityCodeFlatFile.txt";
    
	@Before
	public void setUp() throws Exception {
		commodityCodeUpdateService = PowerMockito.spy(new CommodityCodeUpdateServiceImpl());
		PowerMockito.doReturn(true).when(commodityCodeUpdateService, "updateCommodityCodes", Mockito.any());
		
		CuBatchInputFileServiceImpl batchInputFileService = new CuBatchInputFileServiceImpl();
		commodityCodeUpdateService.setBatchInputFileService(batchInputFileService);
		
		CommodityCodeInputFileType commodityCodeInputFileType = new CommodityCodeInputFileType();
		commodityCodeUpdateService.setCommodityCodeInputFileType(commodityCodeInputFileType);
	}
	
	@After
	public void tearDown() {
	    commodityCodeUpdateService = null;
	}
	
	@Test
	public void testLoadCommodityCodeFile() {
		byte[] content = commodityCodeUpdateService.getFileContent(DATA_FILE_PATH);
		ArrayList<CommodityCode> someCommodityCodes = (ArrayList<CommodityCode>) commodityCodeUpdateService.parseCommodityCodeList(content);
		
		assertNotSame(content.length, 0);
		assertEquals(53321, someCommodityCodes.size());
	}
	
	@Test
	public void testloadCommodityCodeFile() {
	    assertTrue(commodityCodeUpdateService.loadCommodityCodeFile(DATA_FILE_PATH));
	}

}
