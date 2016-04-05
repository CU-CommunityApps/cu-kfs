package edu.cornell.kfs.fp.batch.service.impl;

import java.util.ArrayList;

import org.kuali.kfs.fp.document.ProcurementCardDocument;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.context.TestUtils;
import org.kuali.kfs.krad.service.MailService;

import edu.cornell.kfs.fp.batch.service.ProcurementCardErrorEmailService;


@ConfigureContext
public class ProcurementCardErrorEmailServiceImplTest extends KualiTestBase {
	private ProcurementCardErrorEmailService procurementCardErrorEmailService;	
	private MailService mailService;
	
	private static String updateParm = "update KRCR_PARM_T SET  from GL_RVRSN_CTGRY_AMT_T";
	
	@Override
    protected void setUp() throws Exception {
        super.setUp();
        procurementCardErrorEmailService = SpringContext.getBean(ProcurementCardErrorEmailService.class);       
        mailService = SpringContext.getBean(MailService.class);
    }
	
	public void testProcurementCardErrorEmailService(){
		TestUtils.setSystemParameter(ProcurementCardDocument.class, "PCARD_UPLOAD_ERROR_EMAIL_ADDR", "fake@cornell.edu");

		ArrayList<String> errorMessages = new ArrayList<String>();
		errorMessages.add("TEST ERROR FOR PROCUREMENT CARD");
		procurementCardErrorEmailService.sendErrorEmail(errorMessages);

		
	}
    

}