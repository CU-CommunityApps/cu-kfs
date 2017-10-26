package edu.cornell.kfs.fp.batch.service.impl;

import java.util.HashSet;
import java.util.Set;

public class CbcpErrorEmailServiceImpl extends ProcurementCardErrorEmailServiceImpl {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CbcpErrorEmailServiceImpl.class);
    
    @Override
    protected Set<String> getToAddresses() {
        Set<String> addresses = new HashSet<String>();
        //addresses.add(parameterService.getParameterValueAsString("KFS-FP", "ProcurementCard", "PCARD_UPLOAD_ERROR_EMAIL_ADDR"));
        /**
         * @todo really implement this
         */
        addresses.add("jdh34@cornell.edu");
        return addresses;
    }
    
    @Override
    protected String buildErrorEmailSubject() {
        return "Error occurred during CBCP batch upload process";
    }
    
    @Override
    protected String buildErrorMessageBodyStarter() {
        return "Errors occured during the CBCP upload process.";
    }

}
