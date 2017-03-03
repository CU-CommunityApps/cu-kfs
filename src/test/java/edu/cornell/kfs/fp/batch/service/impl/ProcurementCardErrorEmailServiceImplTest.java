package edu.cornell.kfs.fp.batch.service.impl;

import edu.cornell.kfs.sys.service.mock.MockParameterServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.service.impl.EmailServiceImpl;

import java.util.ArrayList;

public class ProcurementCardErrorEmailServiceImplTest {
    private ProcurementCardErrorEmailServiceImpl procurementCardErrorEmailService;

    @Before
    public void setUp() throws Exception {
        procurementCardErrorEmailService = new ProcurementCardErrorEmailServiceImpl();

        EmailServiceImpl emailService = new EmailServiceImpl();
        ParameterService parameterService = new MockParameterServiceImpl();

        emailService.setParameterService(parameterService);
        procurementCardErrorEmailService.setEmailService(emailService);
        procurementCardErrorEmailService.setParameterService(parameterService);
    }

    @Test
    public void testProcurementCardErrorEmailService() {
        ArrayList<String> errorMessages = new ArrayList<String>();
        errorMessages.add("TEST ERROR FOR PROCUREMENT CARD");
        procurementCardErrorEmailService.sendErrorEmail(errorMessages);
    }

}