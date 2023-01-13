package edu.cornell.kfs.ksr.businessobject.actions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.datadictionary.Action;
import org.kuali.kfs.datadictionary.BusinessObjectAdminService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.actions.BusinessObjectActionsProvider;

import edu.cornell.kfs.ksr.KSRConstants;
import edu.cornell.kfs.ksr.KSRPropertyConstants;
import edu.cornell.kfs.ksr.businessobject.SecurityGroup;
import edu.cornell.kfs.ksr.businessobject.SecurityProvisioning;

public class SecurityGroupActionsProvider extends BusinessObjectActionsProvider {

    @Override
    public List<Action> getActionLinks(BusinessObjectBase businessObject, Person user) {
        final List<Action> actions = super.getActionLinks(businessObject, user);       
        BusinessObjectAdminService businessObjectAdminService = businessObjectDictionaryService.getBusinessObjectAdminService(businessObject.getClass());
        
        if (businessObjectAdminService.allowsEdit(businessObject, user)) {
            String securityProvisioningUrl = generateSecurityProvisioningUrl((SecurityGroup) businessObject);
            final Action action = new Action(KSRConstants.SECURITY_PROVISIONING_URL_NAME, "GET", securityProvisioningUrl);
            actions.add(action);
        }

        return actions;
    }

    private static String generateSecurityProvisioningUrl(SecurityGroup securityGroup) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(KSRPropertyConstants.SECURITY_GROUP_ID,
                UrlFactory.encode(String.valueOf(securityGroup.getSecurityGroupId())));
        parameters.put(KFSConstants.BUSINESS_OBJECT_CLASS_ATTRIBUTE, SecurityProvisioning.class.getName());
        parameters.put(KFSConstants.DISPATCH_REQUEST_PARAMETER, KFSConstants.MAINTENANCE_EDIT_METHOD_TO_CALL);

        return UrlFactory.parameterizeUrl(KRADConstants.MAINTENANCE_ACTION, parameters);
    }

}
