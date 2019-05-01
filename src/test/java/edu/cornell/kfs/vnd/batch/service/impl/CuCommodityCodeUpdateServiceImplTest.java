package edu.cornell.kfs.vnd.batch.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.vnd.businessobject.CommodityCode;
import org.mockito.Mockito;

import edu.cornell.kfs.sys.batch.service.impl.CuBatchInputFileServiceImpl;
import edu.cornell.kfs.vnd.batch.CommodityCodeInputFileType;

public class CuCommodityCodeUpdateServiceImplTest {

    private CommodityCodeUpdateServiceImpl commodityCodeUpdateService;

    private static final String DATA_FILE_PATH = "src/test/resources/edu/cornell/kfs/vnd/fixture/commodityCodeFlatFile.txt";
    private static final int NUMBER_OF_RECORDS_IN_DATA_FILE = 53321;

    @Before
    public void setUp() throws Exception {
        commodityCodeUpdateService = Mockito.spy(new CommodityCodeUpdateServiceImpl());
        Mockito.doReturn(true).when(commodityCodeUpdateService).updateCommodityCodes(Mockito.any());

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
    public void testParseCommodityCodeList() {
        byte[] content = commodityCodeUpdateService.getFileContent(DATA_FILE_PATH);
        Collection<CommodityCode> someCommodityCodes = commodityCodeUpdateService.parseCommodityCodeList(content);
        assertNotEquals(content.length, 0);
        assertEquals(NUMBER_OF_RECORDS_IN_DATA_FILE, someCommodityCodes.size());
    }

    @Test
    public void testLoadCommodityCodeFile() {
        assertTrue(commodityCodeUpdateService.loadCommodityCodeFile(DATA_FILE_PATH));
    }

}
