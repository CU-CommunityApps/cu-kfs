package edu.cornell.kfs.concur.services;

import edu.cornell.kfs.sys.service.ErrorMessageUtilsService;
import edu.cornell.kfs.sys.service.impl.ErrorMessageUtilsServiceImpl;

public class MockErrorMessageUtilsService implements ErrorMessageUtilsService {
    protected ErrorMessageUtilsServiceImpl errorMessageUtilsService;
    
    public MockErrorMessageUtilsService() {
        errorMessageUtilsService = new ErrorMessageUtilsServiceImpl();
        errorMessageUtilsService.setConfigurationService(new MockConfigurationService());
    }

    @Override
    public String createErrorString(String errorKey, String... params) {
        return errorMessageUtilsService.createErrorString(errorKey, params);
    }

}
