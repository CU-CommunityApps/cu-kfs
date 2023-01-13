package edu.cornell.kfs.sys.batch.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchFile;
import org.kuali.kfs.sys.batch.service.BatchFileAdminAuthorizationService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.service.KualiModuleService;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.batch.service.CreateDoneBatchFileAuthorizationService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;

public class CreateDoneBatchFileAuthorizationServiceImpl extends BatchFileAdminAuthorizationService implements CreateDoneBatchFileAuthorizationService {
	
    private KualiModuleService kualiModuleService;

	@Override
	public boolean canCreateDoneFile(BatchFile batchFile, Person user) {
        boolean isAuthorized = false;

            isAuthorized = KimApiServiceLocator.getPermissionService().isAuthorizedByTemplate(user.getPrincipalId(),
                KFSConstants.CoreModuleNamespaces.KFS, CUKFSConstants.SysKimApiConstants.CREATE_DONE_FILE_PERMISSION_TEMPLATE_NAME,
                generateCreateDoneCheckPermissionDetails(batchFile, user), new HashMap<>());
            
        return isAuthorized;
	}

    protected Map<String,String> generateCreateDoneCheckPermissionDetails(BatchFile batchFile, Person user) {
        return generatePermissionDetails(batchFile);
    }
	
    public KualiModuleService getKualiModuleService() {
        if (kualiModuleService == null) {
            kualiModuleService = SpringContext.getBean(KualiModuleService.class);
        }
        return kualiModuleService;
    }

}
