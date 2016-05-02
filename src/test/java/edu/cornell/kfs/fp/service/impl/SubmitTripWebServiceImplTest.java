package edu.cornell.kfs.fp.service.impl;

import java.lang.reflect.Constructor;
import java.util.List;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.service.DocumentService;

import edu.cornell.kfs.fp.service.SubmitTripWebServiceImpl;

@ConfigureContext(session = ccs1)
public class SubmitTripWebServiceImplTest extends KualiTestBase {
	private SubmitTripWebServiceImpl submitTripWebService;
	private DocumentService documentService;

	
	@Override
    protected void setUp() throws Exception {
        super.setUp();
        submitTripWebService = SubmitTripWebServiceImpl.class.newInstance();
        documentService = SpringContext.getBean(DocumentService.class);
	}
                  
	public void test(){
		String tripDI = "";
		String tripDV = "";
		Boolean docExists;
		
		double di = -1.01;
		double dv = 1.01;
		try {
			tripDI	= submitTripWebService.submitTrip("New trip", "Training", "12345", "cab379", "ccs1", di, "Check Sub Text");
		}
		catch (Exception e)
		{
			
		}
		
		try {
			tripDV	= submitTripWebService.submitTrip("New trip", "Training", "12345", "cab379", "ccs1", dv, "Check Sub Text");
		}
		catch (Exception e)
		{
			
		}
				
		assertTrue(documentService.documentExists(tripDI));
		assertTrue(documentService.documentExists(tripDV));
	}
    

}
