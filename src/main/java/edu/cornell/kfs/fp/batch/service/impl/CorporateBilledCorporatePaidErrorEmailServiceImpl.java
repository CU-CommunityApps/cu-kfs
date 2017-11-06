package edu.cornell.kfs.fp.batch.service.impl;

public class CorporateBilledCorporatePaidErrorEmailServiceImpl extends ProcurementCardErrorEmailServiceImpl {
    
    @Override
    protected String buildErrorEmailSubject() {
        return "Error occurred during Corporate Billed Corporate Paid batch upload process";
    }
    
    @Override
    protected String buildErrorMessageBodyStarter() {
        return "Errors occured during the Corporate Billed Corporate Paid upload process.";
    }

}
