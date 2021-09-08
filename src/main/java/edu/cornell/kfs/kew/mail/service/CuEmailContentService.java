package edu.cornell.kfs.kew.mail.service;

import org.kuali.kfs.kew.mail.service.EmailContentService;

public interface CuEmailContentService extends EmailContentService {

    boolean isProductionWorkflowEmailModeEnabled();

}
