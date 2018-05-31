package edu.cornell.kfs.sys.batch.service.impl;

import java.util.Map;

import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchFile;
import org.kuali.kfs.sys.batch.service.BatchFileAdminAuthorizationService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.services.IdentityManagementService;
import org.kuali.kfs.krad.service.KualiModuleService;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.batch.service.CreateDoneBatchFileAuthorizationService;

public class CreateDoneBatchFileAuthorizationServiceImpl extends BatchFileAdminAuthorizationService implements CreateDoneBatchFileAuthorizationService {
	
    private IdentityManagementService identityManagementService;
    private KualiModuleService kualiModuleService;

	@Override
	public boolean canCreateDoneFile(BatchFile batchFile, Person user) {
        boolean isAuthorized = false;

            isAuthorized = getIdentityManagementService().isAuthorizedByTemplateName(user.getPrincipalId(),
                KFSConstants.CoreModuleNamespaces.KFS, CUKFSConstants.SysKimApiConstants.CREATE_DONE_FILE_PERMISSION_TEMPLATE_NAME,
                generateCreateDoneCheckPermissionDetails(batchFile, user), generateCreateDoneCheckRoleQualifiers(batchFile, user));
            
        return isAuthorized;
	}
	
    protected Map<String,String> generateCreateDoneCheckRoleQualifiers(BatchFile batchFile, Person user) {
        return generateRoleQualifiers(batchFile, user);
    }

    protected Map<String,String> generateCreateDoneCheckPermissionDetails(BatchFile batchFile, Person user) {
        return generatePermissionDetails(batchFile, user);
    }
	
    protected IdentityManagementService getIdentityManagementService() {
        if (identityManagementService == null) {
            identityManagementService = SpringContext.getBean(IdentityManagementService.class);
        }
        return identityManagementService;
    }

    public KualiModuleService getKualiModuleService() {
        if (kualiModuleService == null) {
            kualiModuleService = SpringContext.getBean(KualiModuleService.class);
        }
        return kualiModuleService;
    }

}
