package edu.cornell.kfs.fp.batch.service.impl;

import edu.cornell.kfs.fp.CuFPConstants;

public class CorporateBilledCorporatePaidErrorEmailServiceImpl extends ProcurementCardErrorEmailServiceImpl {
    
    @Override
    protected String buildErrorEmailSubject() {
        return CuFPConstants.CORPORATE_BILLED_CORPORATE_PAID_ERROR_EMAIL_SUBJECT;
    }
    
    @Override
    protected String buildErrorMessageBodyStarter() {
        return CuFPConstants.CORPORATE_BILLED_CORPORATE_PAID_ERROR_EMAIL_BODY_STARTER;
    }

}
