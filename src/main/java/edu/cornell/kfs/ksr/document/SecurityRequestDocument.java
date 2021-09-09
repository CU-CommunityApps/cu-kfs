package edu.cornell.kfs.ksr.document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.kew.framework.postprocessor.DocumentRouteStatusChange;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.role.Role;
import org.kuali.kfs.krad.document.TransactionalDocumentBase;
import org.kuali.kfs.krad.util.BeanPropertyComparator;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.ksr.KSRConstants;
import edu.cornell.kfs.ksr.KSRPropertyConstants;
import edu.cornell.kfs.ksr.businessobject.SecurityGroup;
import edu.cornell.kfs.ksr.businessobject.SecurityGroupTab;
import edu.cornell.kfs.ksr.businessobject.SecurityProvisioningGroup;
import edu.cornell.kfs.ksr.businessobject.SecurityRequestRole;
import edu.cornell.kfs.ksr.service.SecurityRequestPostProcessingService;

public class SecurityRequestDocument extends TransactionalDocumentBase {
    private static final long serialVersionUID = 4006821516580654441L;

    private static final String REQUEST_PERSON_PROPERTY = "requestPerson";
    
    private String principalId;
    private String primaryDepartmentCode;
    private Long securityGroupId;

    private Person requestPerson;
    private SecurityGroup securityGroup;

    private List<SecurityRequestRole> securityRequestRoles;

    private List<TabRoleIndexes> tabRoleIndexes;
    
    public SecurityRequestDocument() {
        super();

        securityRequestRoles = new ArrayList<SecurityRequestRole>();
    }
    
    /**
     * Invoke <code>SecurityRequestPostProcessingService</code> to update KIM when security request becomes final
     * 
     * @see org.kuali.rice.kns.document.DocumentBase#doRouteStatusChange(org.kuali.rice.kew.dto.DocumentRouteStatusChangeDTO)
     */
    @Override
    public void doRouteStatusChange(DocumentRouteStatusChange statusChangeEvent) {
        super.doRouteStatusChange(statusChangeEvent);

        if (getDocumentHeader().getWorkflowDocument().isProcessed()) {
            SpringContext.getBean(SecurityRequestPostProcessingService.class).postProcessSecurityRequest(this);
        }
    }

    /**
     * @see org.kuali.rice.kns.bo.PersistableBusinessObjectBase#buildListOfDeletionAwareLists()
     */
    @SuppressWarnings({"rawtypes","unchecked"})
    @Override
    public List buildListOfDeletionAwareLists() {
        List managedLists = super.buildListOfDeletionAwareLists();

        for (SecurityRequestRole requestRole : securityRequestRoles) {
            managedLists.add(requestRole.getRequestRoleQualifications());
        }

        return managedLists;
    }

    public String getPrincipalId() {
        return this.principalId;
    }

    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }

    public String getPrimaryDepartmentCode() {
        return this.primaryDepartmentCode;
    }

    public void setPrimaryDepartmentCode(String primaryDepartmentCode) {
        this.primaryDepartmentCode = primaryDepartmentCode;
    }

    public Long getSecurityGroupId() {
        return this.securityGroupId;
    }

    public void setSecurityGroupId(Long securityGroupId) {
        this.securityGroupId = securityGroupId;
    }

    public Person getRequestPerson() {
            // ==== CU Customization: Altered this method to resemble a similar one from a rice-sampleapp form. ====
            PersonService personService = KimApiServiceLocator.getPersonService();
            requestPerson = personService.getPerson(principalId);
            if (requestPerson == null) {
                try {
                    requestPerson = KimApiServiceLocator.getPersonService().getPersonImplementationClass().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return requestPerson;
    }

    public void setRequestPerson(Person requestPerson) {
        if (requestPerson != null) {
            this.principalId = requestPerson.getPrincipalId();
        }
        this.requestPerson = requestPerson;
    }

    public SecurityGroup getSecurityGroup() {
        return this.securityGroup;
    }

    public void setSecurityGroup(SecurityGroup securityGroup) {
        this.securityGroup = securityGroup;
    }

    public List<SecurityRequestRole> getSecurityRequestRoles() {
        return this.securityRequestRoles;
    }

    public void setSecurityRequestRoles(List<SecurityRequestRole> securityRequestRoles) {
        this.securityRequestRoles = securityRequestRoles;
    }

    public String getPrincipalNameForSearch() {
    	return null;
    }
    
    public void setPrincipalNameForSearch(String principalNameForSearch) {
    }
    
    public Role getRoleForSearch() {
    	return null;
    }
    
    public void setRoleForSearch(Role roleForSearch) {
    }
    
	public String getRoleIdForSearch() {
		return null;
	}

	public void setRoleIdForSearch(String roleIdForSearch) {
	}

	public String getRoleQualifierValueForSearch() {
		return null;
	}

	public void setRoleQualifierValueForSearch(String roleQualifierValueForSearch) {
	    
	}
	@Override
	public void refreshReferenceObject(String referenceObjectName) {
		// We need to explicitly avoid refreshing the "requestPerson" object since we should not need to have an OJB mapping for it.
		if (!REQUEST_PERSON_PROPERTY.equals(referenceObjectName)) {
			super.refreshReferenceObject(referenceObjectName);
		}
	}
	public List<TabRoleIndexes> getTabRoleIndexes() {
		// When retrieving tab role indexes, reload them if necessary, since we do not persist the TabRoleIndexes objects.
		if (tabRoleIndexes == null || tabRoleIndexes.isEmpty()) {
			if (securityRequestRoles != null && !securityRequestRoles.isEmpty()) {
				buildTabRoleIndexes();
			}
		}
		return tabRoleIndexes;
	}
	
	public void setTabRoleIndexes(List<TabRoleIndexes> tabRoleIndexes) {
		this.tabRoleIndexes = tabRoleIndexes;
	}
	
	@SuppressWarnings("unchecked")
    public void buildTabRoleIndexes() {
        final List<SecurityRequestDocument.TabRoleIndexes> tabRoleIndexes = new ArrayList<SecurityRequestDocument.TabRoleIndexes>();

        // sort security group tabs
        List<String> sortPropertyNames = new ArrayList<String>();
        sortPropertyNames.add(KSRPropertyConstants.SECURITY_REQUEST_DOCUMENT_TAB_ORDER);

        Collections.sort(getSecurityGroup().getSecurityGroupTabs(), new BeanPropertyComparator(sortPropertyNames));

        for (SecurityGroupTab groupTab : getSecurityGroup().getSecurityGroupTabs()) {
            SecurityRequestDocument.TabRoleIndexes tabIndexes = new SecurityRequestDocument.TabRoleIndexes();
            tabIndexes.setTabId(groupTab.getTabId());
            tabIndexes.setTabName(groupTab.getTabName());

            // sort tab roles
            sortPropertyNames = new ArrayList<String>();
            sortPropertyNames.add(KSRPropertyConstants.SECURITY_REQUEST_DOCUMENT_ROLE_TAB_ORDER);

            Collections.sort(groupTab.getSecurityProvisioningGroups(), new BeanPropertyComparator(sortPropertyNames));

            // build list of index for request roles for tab, ordered by the tab
            // role order
            List<Integer> requestRoleIndexes = new ArrayList<Integer>();
            List<SecurityRequestRole> requestRoles = new ArrayList<SecurityRequestRole>();
            for (SecurityProvisioningGroup provisioningGroup : groupTab.getSecurityProvisioningGroups()) {
                int roleIndex = findSecurityRequestRoleIndex(provisioningGroup.getRoleId());
                // ==== CU Customization (CYNERGY-2475): Skip unknown role indexes without throwing an exception. ====
                if (roleIndex != -1) {
                    //throw new RuntimeException("Unable to find security request role record for role id: "
                            //+ provisioningGroup.getRoleId());
                //}

                    if(provisioningGroup.isActive()){
                    	requestRoleIndexes.add(Integer.valueOf(roleIndex));
                    	requestRoles.add(getSecurityRequestRoles().get(roleIndex));
                	}
                }
                // ==== End CU Customization ====
            }

            if(requestRoleIndexes.size() > 0){
                tabIndexes.setRoleRequestIndexes(requestRoleIndexes);
                tabIndexes.setRoleRequests(requestRoles);
                tabRoleIndexes.add(tabIndexes);
            }
            
        }

        this.tabRoleIndexes = tabRoleIndexes;
    }
    
    /**
     * Searches the securityRequestRoles collection of the given security request document for the request role instance
     * that is associated with the given role id, only one such instance should exist
     * 
     * @param roleId
     *            - id for the role to match
     * @return index of the security request role in the collection, or -1 if not found
     */
    protected int findSecurityRequestRoleIndex(final String roleId) {
        int roleIndex = -1;

        for (int i = 0; i < getSecurityRequestRoles().size(); i++) {
            final SecurityRequestRole requestRole = getSecurityRequestRoles().get(i);

            if (StringUtils.equals(roleId, requestRole.getRoleId())) {
                roleIndex = i;
                break;
            }
        }

        return roleIndex;
    }

	public static class TabRoleIndexes implements java.io.Serializable {

		private static final long serialVersionUID = 7522534024320122122L;

		private Long tabId;
        private String tabName;
        private List<Integer> roleRequestIndexes;
        private List<SecurityRequestRole> roleRequests;

        public Long getTabId() {
            return tabId;
        }

        public void setTabId(Long tabId) {
            this.tabId = tabId;
        }

        public String getTabName() {
            return tabName;
        }

        public void setTabName(String tabName) {
            this.tabName = tabName;
        }

        public List<Integer> getRoleRequestIndexes() {
            return roleRequestIndexes;
        }

        public void setRoleRequestIndexes(List<Integer> roleRequestIndexes) {
            this.roleRequestIndexes = roleRequestIndexes;
        }

		public List<SecurityRequestRole> getRoleRequests() {
			return roleRequests;
		}

		public void setRoleRequests(List<SecurityRequestRole> roleRequests) {
			this.roleRequests = roleRequests;
		}
        
    }
	
}
