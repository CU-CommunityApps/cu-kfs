package edu.cornell.kfs.fp.batch.service.impl;

import org.kuali.kfs.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.fp.CuFPKeyConstants;

public class CorporateBilledCorporatePaidErrorEmailServiceImpl extends ProcurementCardErrorEmailServiceImpl {
    
    private ConfigurationService configurationService;
    
    @Override
    protected String buildErrorEmailSubject() {
        return configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_CBCP_BATCH_UPLOAD_EMAIL_SUBJECT);
    }
    
    @Override
    protected String buildErrorMessageBodyStarter() {
        return configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_CBCP_BATCH_UPLOAD_EMAIL_BODY_STARTER);
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
