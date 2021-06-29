package edu.cornell.kfs.ksr.businessobject.actions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.datadictionary.Action;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.kfs.sys.businessobject.actions.BusinessObjectActionsProvider;

import edu.cornell.kfs.ksr.KSRConstants;

public class SecurityGroupActionsProvider extends BusinessObjectActionsProvider {

    @Override
    public List<Action> getActionLinks(BusinessObjectBase businessObject, Person user) {
        final List<Action> actions = super.getActionLinks(businessObject, user);

        String securityProvisioningUrl = generateSecurityProvisioningUrl();
        final Action action = new Action(KSRConstants.SECURITY_PROVISIONING_URL_NAME, "GET", securityProvisioningUrl);
        actions.add(action);

        return actions;
    }

    private static String generateSecurityProvisioningUrl() {
        Map<String, String> parameters = new HashMap<>();

        return UrlFactory.parameterizeUrl(KRADConstants.MAINTENANCE_ACTION, parameters);
    }

}
