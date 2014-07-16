package edu.cornell.kfs.module.receiptProcessing.service.impl;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import edu.cornell.kfs.module.receiptProcessing.service.ReceiptProcessingService;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;

@ConfigureContext(session = ccs1)
public class CuReceiptProcessingServiceImplTest extends KualiTestBase {

    private ReceiptProcessingService receiptProcessingService;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        receiptProcessingService = SpringContext.getBean(ReceiptProcessingService.class);
    }
    
    public void testCanLoadFiles() {
        assertTrue(receiptProcessingService.loadFiles());       
    }
    
}
