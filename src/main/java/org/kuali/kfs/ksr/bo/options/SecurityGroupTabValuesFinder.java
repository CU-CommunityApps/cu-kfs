package org.kuali.kfs.ksr.bo.options;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kuali.kfs.ksr.KsrConstants;
import org.kuali.kfs.ksr.bo.SecurityGroupTab;
import org.kuali.kfs.ksr.bo.SecurityProvisioning;
import org.kuali.rice.core.api.criteria.PredicateFactory;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.data.DataObjectService;
import org.kuali.rice.krad.service.KRADServiceLocator;
import org.kuali.rice.krad.uif.control.UifKeyValuesFinderBase;
import org.kuali.rice.krad.uif.view.ViewModel;
import org.kuali.rice.krad.web.form.MaintenanceDocumentForm;

/**
 * ====
 * Copied over the version of this class from a more up-to-date rSmart KSR repository,
 * since it contains some fixes and features that are not present in the older KSR repository.
 * 
 * Also remediated this class as needed for Rice 2.x compatibility.
 * ====
 * 
 * Gets all the tab values associated with the SecurityGroup selected by the user.
 * 
 * @author rSmart Development Team
 */
public class SecurityGroupTabValuesFinder extends UifKeyValuesFinderBase {

    private static final long serialVersionUID = 1825873598309848494L;

    /**
     * Gets all SecurityGroupTab objects associated with a given SecurityGroup. Values will be populated into a html
     * select tag.
     * 
     * It is assumed that the ViewModel is a SecurityProvisioning maintenance document form.
     * 
     * @see org.kuali.rice.krad.uif.control.UifKeyValuesFinderBase#getKeyValues(org.kuali.rice.krad.uif.view.ViewModel)
     */
    public List<KeyValue> getKeyValues(ViewModel model) {
        if (model instanceof MaintenanceDocumentForm) {
            MaintenanceDocumentForm documentForm = (MaintenanceDocumentForm) model;
            SecurityProvisioning securityProvisioning = (SecurityProvisioning) documentForm.getDocument().getDocumentDataObject();
            Long securityGroupId = securityProvisioning.getSecurityGroupId();
            
            if (securityGroupId != null) {
                QueryByCriteria criteria = QueryByCriteria.Builder.fromPredicates(
                        PredicateFactory.equal(KsrConstants.SECURITY_GROUP_ID, securityGroupId));
                List<SecurityGroupTab> tabs = getDataObjectService()
                        .findMatching(SecurityGroupTab.class, criteria)
                        .getResults();
                
                return tabs.stream()
                        .map(this::buildKeyValueFromTab)
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            }
        }
        
        return Collections.emptyList();
    }

    protected KeyValue buildKeyValueFromTab(SecurityGroupTab tab) {
        return new ConcreteKeyValue(tab.getTabId().toString(), tab.getTabOrder() + " - " + tab.getTabName());
    }

    protected DataObjectService getDataObjectService() {
        return KRADServiceLocator.getDataObjectService();
    }

}
