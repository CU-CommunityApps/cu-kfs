package edu.cornell.kfs.fp.batch.service.impl;

import edu.cornell.kfs.sys.service.mock.MockParameterServiceImpl;
import junit.framework.TestCase;

import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.service.impl.EmailServiceImpl;

import java.util.ArrayList;

@ConfigureContext
public class ProcurementCardErrorEmailServiceImplTest extends TestCase {
	private ProcurementCardErrorEmailServiceImpl procurementCardErrorEmailService;

	@Override
    protected void setUp() throws Exception {
        super.setUp();
        procurementCardErrorEmailService = new ProcurementCardErrorEmailServiceImpl();
        procurementCardErrorEmailService.setEmailService(new EmailServiceImpl());
	procurementCardErrorEmailService.setParameterService(new MockParameterServiceImpl());
    }
	
	public void testProcurementCardErrorEmailService(){
		ArrayList<String> errorMessages = new ArrayList<String>();
		errorMessages.add("TEST ERROR FOR PROCUREMENT CARD");
		procurementCardErrorEmailService.sendErrorEmail(errorMessages);
	}

}