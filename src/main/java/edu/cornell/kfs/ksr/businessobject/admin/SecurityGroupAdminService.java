package edu.cornell.kfs.ksr.businessobject.admin;

import java.util.HashMap;
import java.util.Map;

import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.admin.DefaultBoAdminService;

import edu.cornell.kfs.ksr.KSRConstants;

public class SecurityGroupAdminService extends DefaultBoAdminService {
    
    public boolean allowsEditProvisioning(Person person) {
//        Map<String, String> permissionDetails = new HashMap<>();
//        permissionDetails.put(KimConstants.AttributeConstants.NAMESPACE_CODE, KSRConstants.KSR_NAMESPACE);
//
//        return permissionService.isAuthorizedByTemplate(person.getPrincipalId(),
//                KFSConstants.CoreModuleNamespaces.KFS, KimConstants.PermissionTemplateNames.USE_SCREEN, permissionDetails,
//                new HashMap<>());
        
        return true;
    }

}
