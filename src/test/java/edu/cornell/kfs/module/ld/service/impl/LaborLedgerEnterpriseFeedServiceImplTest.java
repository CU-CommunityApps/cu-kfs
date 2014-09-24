package edu.cornell.kfs.module.ld.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.module.ld.service.LaborLedgerEnterpriseFeedService;

@ConfigureContext
public class LaborLedgerEnterpriseFeedServiceImplTest extends KualiTestBase {
    private static final String DATA_FILE_PATH = "src/test/java/edu/cornell/kfs/module/ld/fixture/SMGROS.data";
    private static final String BAD_DATA_FILE_PATH = "src/test/java/edu/cornell/kfs/module/ld/fixture/SMGROSBAD.data";

    private static final String DISENCUMBRANCE_ACCOUNTING_LINE = "2014IT6258326-----5020---AC";
    
    private LaborLedgerEnterpriseFeedService ldService;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ldService = SpringContext.getBean(LaborLedgerEnterpriseFeedService.class);
    }
    

    public void testCreateDisencumbrance() throws IOException {
    	File dataFile = new File(DATA_FILE_PATH);
    	InputStream disencumFileInputStream = null;
        disencumFileInputStream = ldService.createDisencumbrance(new FileInputStream(dataFile));
        
        assertNotNull(disencumFileInputStream);
        
        StringWriter writer = new StringWriter();
        IOUtils.copy(disencumFileInputStream, writer, "UTF-8");
        String theString = writer.toString();
        assertTrue(theString.contains(DISENCUMBRANCE_ACCOUNTING_LINE));

    }
    
    public void testCreateDisencumbranceBadFile() throws FileNotFoundException {
    	File dataFile = new File(BAD_DATA_FILE_PATH);
    	InputStream disencumFileInputStream = null;
        disencumFileInputStream = ldService.createDisencumbrance(new FileInputStream(dataFile));
        
        assertNull(disencumFileInputStream);
   
    }

}
