package edu.cornell.kfs.kew.mail.service.impl;

import org.kuali.kfs.kew.mail.service.impl.CustomizableActionListEmailServiceImpl;

import edu.cornell.kfs.kew.mail.service.CuEmailContentService;

public class CuActionListEmailServiceImpl extends CustomizableActionListEmailServiceImpl {

    @Override
    protected boolean isProduction() {
        return super.isProduction() || shouldForceProductionWorkflowEmailBehavior();
    }

    protected boolean shouldForceProductionWorkflowEmailBehavior() {
        return getCuEmailContentService().isProductionWorkflowEmailModeEnabled();
    }

    protected CuEmailContentService getCuEmailContentService() {
        return (CuEmailContentService) getEmailContentGenerator();
    }

}
