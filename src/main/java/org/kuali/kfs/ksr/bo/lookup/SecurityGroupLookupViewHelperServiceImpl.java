package org.kuali.kfs.ksr.bo.lookup;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.ksr.bo.SecurityProvisioning;
import org.kuali.rice.krad.lookup.LookupForm;
import org.kuali.rice.krad.lookup.LookupView;
import org.kuali.rice.krad.lookup.LookupableImpl;
import org.kuali.rice.krad.uif.UifConstants;
import org.kuali.rice.krad.uif.UifParameters;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.KRADUtils;
import org.kuali.rice.krad.util.UrlFactory;

/**
 * ====
 * CU Customization:
 * Added this class to replace the equivalent KNS lookupable helper service class.
 * ====
 */
public class SecurityGroupLookupViewHelperServiceImpl extends LookupableImpl {

    private static final long serialVersionUID = -4779180720846781746L;

    protected static final String EDIT_PROVISIONING_ACTION = "editProvisioning";

    /**
     * Overridden so that if a special "editProvisioning" methodToCall is specified,
     * then a link for creating a SecurityProvisioning maintenance "edit" document
     * will be returned instead.
     * 
     * @see org.kuali.rice.krad.lookup.LookupableImpl#getMaintenanceActionUrl(
     * org.kuali.rice.krad.lookup.LookupForm, java.lang.Object, java.lang.String, java.util.List)
     */
    @Override
    protected String getMaintenanceActionUrl(LookupForm lookupForm, Object dataObject, String methodToCall,
            List<String> pkNames) {
        if (EDIT_PROVISIONING_ACTION.equals(methodToCall)) {
            return getProvisioningMaintenanceActionUrl(
                    lookupForm, dataObject, KRADConstants.Maintenance.METHOD_TO_CALL_EDIT, pkNames);
        }
        return super.getMaintenanceActionUrl(lookupForm, dataObject, methodToCall, pkNames);
    }

    /**
     * Creates an action URL for opening a Security Provisioning maintenance document
     * instead of a Security Group maintenance document, but otherwise uses
     * the same code and configuration from the superclass's "getMaintenanceActionUrl" method.
     */
    protected String getProvisioningMaintenanceActionUrl(LookupForm lookupForm, Object dataObject, String methodToCall,
            List<String> pkNames) {
        LookupView lookupView = (LookupView) lookupForm.getView();

        Properties props = new Properties();
        props.put(KRADConstants.DISPATCH_REQUEST_PARAMETER, methodToCall);

        Map<String, String> primaryKeyValues = KRADUtils.getPropertyKeyValuesFromDataObject(pkNames, dataObject);
        for (String primaryKey : primaryKeyValues.keySet()) {
            String primaryKeyValue = primaryKeyValues.get(primaryKey);

            props.put(primaryKey, primaryKeyValue);
        }

        if (StringUtils.isNotBlank(lookupForm.getReturnLocation())) {
            props.put(KRADConstants.RETURN_LOCATION_PARAMETER, lookupForm.getReturnLocation());
        }

        props.put(UifParameters.DATA_OBJECT_CLASS_NAME, SecurityProvisioning.class.getName());
        props.put(UifParameters.VIEW_TYPE_NAME, UifConstants.ViewType.MAINTENANCE.name());

        String maintenanceMapping = KRADConstants.Maintenance.REQUEST_MAPPING_MAINTENANCE;
        if (lookupView != null && StringUtils.isNotBlank(lookupView.getMaintenanceUrlMapping())) {
            maintenanceMapping = lookupView.getMaintenanceUrlMapping();
        }

        return UrlFactory.parameterizeUrl(maintenanceMapping, props);
    }

}
