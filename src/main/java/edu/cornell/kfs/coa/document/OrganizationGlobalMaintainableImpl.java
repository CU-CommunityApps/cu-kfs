package edu.cornell.kfs.coa.document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.Organization;
import org.kuali.kfs.coa.service.OrganizationService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.FinancialSystemGlobalMaintainable;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.maintenance.MaintenanceLock;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.businessobject.PostalCode;

import edu.cornell.kfs.coa.businessobject.OrganizationGlobal;
import edu.cornell.kfs.coa.businessobject.OrganizationGlobalDetail;

/**
 * Maintainable implementation for OrganizationGlobal BOs.
 */
@SuppressWarnings("deprecation")
public class OrganizationGlobalMaintainableImpl extends FinancialSystemGlobalMaintainable {

    private static final long serialVersionUID = 5569987847895123223L;

    @Override
    public List<MaintenanceLock> generateMaintenanceLocks() {
        /*
         * Generate the maintenance locks in a similar manner as the account global maintainable.
         */
        OrganizationGlobal orgGlobal = (OrganizationGlobal) getBusinessObject();
        List<MaintenanceLock> maintenanceLocks = new ArrayList<MaintenanceLock>();
        final int BUILDER_SIZE = 100;
        
        for (OrganizationGlobalDetail detail : orgGlobal.getOrganizationGlobalDetails()) {
            MaintenanceLock maintenanceLock = new MaintenanceLock();
            maintenanceLock.setDocumentNumber(orgGlobal.getDocumentNumber());
            maintenanceLock.setLockingRepresentation(new StringBuilder(BUILDER_SIZE)
                    .append(Organization.class.getName()).append(KFSConstants.Maintenance.AFTER_CLASS_DELIM)
                    .append("chartOfAccountsCode").append(KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM)
                    .append(detail.getChartOfAccountsCode()).append(KFSConstants.Maintenance.AFTER_VALUE_DELIM)
                    .append("organizationCode").append(KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM)
                    .append(detail.getOrganizationCode()).toString());
            
            maintenanceLocks.add(maintenanceLock);
        }
        
        return maintenanceLocks;
    }

    @Override
    public Class<? extends PersistableBusinessObject> getPrimaryEditedBusinessObjectClass() {
        return Organization.class;
    }

    /**
     * Overridden to also add special handling for organization manager, city name and state code.
     * This is necessary to properly update the city and state information based on the zip and
     * country codes, and to properly clear out the manager object when the most recently stored
     * or refreshed principal name is an invalid one.
     * 
     * Ideally, this would have been placed in a DerivedValuesSetter implementation; however, such
     * utility classes do not get called when performing a "save" action, so we resort to using
     * this method override instead.
     * 
     * @see org.kuali.kfs.sys.document.FinancialSystemGlobalMaintainable#processAfterPost(org.kuali.kfs.kns.document.MaintenanceDocument, java.util.Map)
     */
    @Override
    public void processAfterPost(MaintenanceDocument document, Map<String,String[]> requestParameters) {
        super.processAfterPost(document, requestParameters);
        OrganizationGlobal orgGlobal = (OrganizationGlobal) getBusinessObject();
        
        // Forcibly set city name and state code if valid zip code is given, otherwise clear them.
        PostalCode zipCode = orgGlobal.getPostalZip();
        if (ObjectUtils.isNotNull(zipCode)) {
            // Valid zip-and-country combination is given; update city and state with corresponding values.
            orgGlobal.setOrganizationCityName(zipCode.getCityName());
            orgGlobal.setOrganizationStateCode(zipCode.getStateCode());
        } else {
            // Invalid or blank zip-and-country combination is given; clear city and state.
            orgGlobal.setOrganizationCityName(null);
            orgGlobal.setOrganizationStateCode(null);
        }
        
        // If the user is attempting to clear out an already-invalid principal name on the form, then clear out the manager object.
        Person orgManager = orgGlobal.getOrganizationManagerUniversal();
        String[] principalName = requestParameters.get("document.newMaintainableObject.organizationManagerUniversal.principalName");
        if (ObjectUtils.isNotNull(orgManager) && StringUtils.isBlank(orgManager.getEntityId()) && StringUtils.isNotBlank(orgManager.getPrincipalName())
                && (principalName == null || principalName.length == 0 || StringUtils.isBlank(principalName[0]))) {
            // User is trying to clear out an invalid principal name; clear out principal ID and Person object accordingly.
            orgGlobal.setOrganizationManagerUniversalId(null);
            orgGlobal.setOrganizationManagerUniversal(null);
        }
    }

    /**
     * Overridden to also include the parent-org-cache-flushing from KualiOrgMaintainable.
     * 
     * @see org.kuali.kfs.kns.maintenance.KualiGlobalMaintainableImpl#saveBusinessObject()
     */
    @Override
    public void saveBusinessObject() {
        // Perform the same cache-flushing as KualiOrgMaintainable.
        super.saveBusinessObject();
        SpringContext.getBean(OrganizationService.class).flushParentOrgCache();
    }

}
