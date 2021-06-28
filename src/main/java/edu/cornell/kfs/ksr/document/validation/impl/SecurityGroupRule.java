package edu.cornell.kfs.ksr.document.validation.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.maintenance.rules.MaintenanceDocumentRuleBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.ksr.KSRConstants;
import edu.cornell.kfs.ksr.KSRKeyConstants;
import edu.cornell.kfs.ksr.businessobject.SecurityGroup;
import edu.cornell.kfs.ksr.businessobject.SecurityGroupTab;

public class SecurityGroupRule extends MaintenanceDocumentRuleBase {

	/**
	 * Validate the new SecurityGroupTab against ones that are already in the list - tab order must be unique - tab name must be unique
	 */
	@Override
	public boolean processCustomAddCollectionLineBusinessRules(MaintenanceDocument document, String collectionName, PersistableBusinessObject line) {
		boolean success = true;

        if (line instanceof SecurityGroupTab) {
            SecurityGroup securityGroup = (SecurityGroup) document.getDocumentDataObject();
            SecurityGroupTab tempTab = (SecurityGroupTab) line;
            if (securityGroup.getSecurityGroupTabs() != null) {
                success = validateSecurityGroupTabs(document);
                if (success) {
                    for (SecurityGroupTab tab : securityGroup.getSecurityGroupTabs()) {
                        if (tab.getTabOrder().equals(tempTab.getTabOrder())) {
                            GlobalVariables.getMessageMap().putError(KSRConstants.SECURITY_GROUP_TAB_ORDER, KSRKeyConstants.ERROR_SECURITY_GROUP_TAB_ORDER_UNIQUE);
                            success = false;
                        }
                        if (tab.getTabName().equals(tempTab.getTabName())) {
                            GlobalVariables.getMessageMap().putError(KSRConstants.SECURITY_GROUP_TAB_NAME, KSRKeyConstants.ERROR_SECURITY_GROUP_TAB_NAME_UNIQUE);
                            success = false;
                        }
                    }
                }
            }
        }

		return super.processCustomAddCollectionLineBusinessRules(document, collectionName, line)
				&& success;
	}

	/**
	 * Determines if there is at least one SecurityGroupTab in the list
	 * 
	 * @param tabs
	 *            - A list of tabs that a SecurityGroup is associated with.
	 * @return true if there is at least one SecurityGroupTab in the list
	 */
	public boolean hasAtLeastOneActiveTab(List<SecurityGroupTab> tabs) {
		if (tabs == null) {
			return false;
		}
		else if (tabs.size() > 0) {
		    return tabs.stream().anyMatch(tab -> tab.isActive());
		}

		return false;
	}

    /**
     * Validates the maint. doc before it is written to the db.
     */
    @Override
    protected boolean dataDictionaryValidate(MaintenanceDocument document) {
        boolean success = true;
        SecurityGroup securityGroup = (SecurityGroup) document.getDocumentDataObject();
        if (!hasAtLeastOneActiveTab(securityGroup.getSecurityGroupTabs())) {
            GlobalVariables.getMessageMap().putErrorForSectionId(KSRConstants.SECTION_SECURITY_TABS, KSRKeyConstants.ERROR_SECURITY_GROUP_TAB_MISSING);
            success = false;
        }

        // Check the Security Group Name against the DB
        SecurityGroup temp = retrieveSecurityGroupByName(securityGroup.getSecurityGroupName());
        if (ObjectUtils.isNotNull(temp)) {
            if (securityGroup.getSecurityGroupId() != null) {
                if (temp.getSecurityGroupName().equals(securityGroup.getSecurityGroupName())
                        && (!temp.getSecurityGroupId().equals(securityGroup.getSecurityGroupId()))) {
                    GlobalVariables.getMessageMap().putError(KSRConstants.KSR_DOCUMENT_MAINTANABLE + "." + KSRConstants.SECURITY_GROUP_NAME,
                            KSRKeyConstants.ERROR_SECURITY_GROUP_NAME_UNIQUE);
                    success = false;
                }
            } else {
                GlobalVariables.getMessageMap().putError(KSRConstants.KSR_DOCUMENT_MAINTANABLE + "." + KSRConstants.SECURITY_GROUP_NAME,
                        KSRKeyConstants.ERROR_SECURITY_GROUP_NAME_UNIQUE);
                success = false;
            }
        }

        success = validateSecurityGroupTabs(document);

        return (super.dataDictionaryValidate(document) && success);
    }

    private SecurityGroup retrieveSecurityGroupByName(String securityGroupName) {
        SecurityGroup securityGroup = null;
        Map<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put(KSRConstants.SECURITY_GROUP_NAME, securityGroupName);

        BusinessObjectService businessObjectService = SpringContext.getBean(BusinessObjectService.class);
        Collection<SecurityGroup> securityGroups = businessObjectService.findMatching(SecurityGroup.class, hashMap);

        if (CollectionUtils.isNotEmpty(securityGroups)) {
            securityGroup = securityGroups.iterator().next();
        }
        return securityGroup;
    }

	/**
	 * Determines uniqueness of each SecurityGroupTab in the maint. doc.
	 * 
	 * @param document
	 *            - the current maint. doc
	 * @return true if there are no duplicate tab orders or names
	 */
	public boolean validateSecurityGroupTabs(MaintenanceDocument document) {
		boolean successTabOrder = true;
		boolean successTabName = true;

		List<SecurityGroupTab> securityGroupTabs = ((SecurityGroup) document.getDocumentDataObject()).getSecurityGroupTabs();
		if (securityGroupTabs != null) {
			for (int i = 0; i < securityGroupTabs.size(); i++) {
				SecurityGroupTab tab = securityGroupTabs.get(i);

				if ((tab.getTabOrder() == null) && (tab.getTabName() == null)) {
					securityGroupTabs.remove(i);
				}
				else if (tab.getTabOrder() == null) { // Reset to old value
					tab.setTabOrder(((SecurityGroup) document.getOldMaintainableObject().getDataObject()).getSecurityGroupTabs().get(i).getTabOrder());
				}
				if (tab.getTabName() == null) {
					tab.setTabName(((SecurityGroup) document.getOldMaintainableObject().getDataObject()).getSecurityGroupTabs().get(i).getTabName());
				}

				for (int j = i + 1; j < securityGroupTabs.size(); j++) {
					SecurityGroupTab tempTab = securityGroupTabs.get(j);
					if (successTabOrder
							&& tab.getTabOrder().equals(tempTab.getTabOrder())) {
						GlobalVariables.getMessageMap().putErrorForSectionId(KSRConstants.SECTION_SECURITY_TABS, KSRKeyConstants.ERROR_SECURITY_GROUP_TAB_ORDER_UNIQUE);
						successTabOrder = false;
					}
					if (successTabName
							&& tab.getTabName().equals(tempTab.getTabName())) {
						GlobalVariables.getMessageMap().putErrorForSectionId(KSRConstants.SECTION_SECURITY_TABS, KSRKeyConstants.ERROR_SECURITY_GROUP_TAB_NAME_UNIQUE);
						successTabName = false;
					}

				}
			}
		}

		return successTabOrder && successTabName;
	}

}
