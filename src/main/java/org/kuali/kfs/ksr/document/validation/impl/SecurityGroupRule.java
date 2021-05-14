package org.kuali.kfs.ksr.document.validation.impl;

import java.util.Iterator;
import java.util.List;

import org.kuali.kfs.ksr.KsrConstants;
import org.kuali.kfs.ksr.bo.SecurityGroup;
import org.kuali.kfs.ksr.bo.SecurityGroupTab;
import org.kuali.rice.core.api.criteria.PredicateFactory;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.krad.maintenance.MaintenanceDocument;
import org.kuali.rice.krad.rules.MaintenanceDocumentRuleBase;
import org.kuali.rice.krad.util.GlobalVariables;

/**
 * ====
 * CU Customization:
 * Remediated this class as needed for Rice 2.x compatibility.
 * ====
 * 
 * Used to validate the SecurityGroup and SecurityGroupTab list
 * 
 * @author rSmart Development Team
 */
public class SecurityGroupRule extends MaintenanceDocumentRuleBase {

    /**
     * Validate the new SecurityGroupTab against ones that are already in the list - tab order must be unique - tab name must be unique
     * 
     * @see org.kuali.rice.krad.rules.MaintenanceDocumentRuleBase#processCustomAddCollectionLineBusinessRules(
     * org.kuali.rice.krad.maintenance.MaintenanceDocument, java.lang.String, java.lang.Object)
     */
    @Override
    public boolean processCustomAddCollectionLineBusinessRules(MaintenanceDocument document, String collectionName, Object line) {
        boolean success = true;

        if (line instanceof SecurityGroupTab) {
            SecurityGroup securityGroup = (SecurityGroup) document.getDocumentDataObject();
            SecurityGroupTab tempTab = (SecurityGroupTab) line;
            if (securityGroup.getSecurityGroupTabs() != null) {
                success = validateSecurityGroupTabs(document);
                if (success) {
                    for (SecurityGroupTab tab : securityGroup.getSecurityGroupTabs()) {
                        if (tab.getTabOrder().equals(tempTab.getTabOrder())) {
                            GlobalVariables.getMessageMap().putError(KsrConstants.SECURITY_GROUP_TAB_ORDER, KsrConstants.ERROR_SECURITY_GROUP_TAB_ORDER_UNIQUE);
                            success = false;
                        }
                        if (tab.getTabName().equals(tempTab.getTabName())) {
                            GlobalVariables.getMessageMap().putError(KsrConstants.SECURITY_GROUP_TAB_NAME, KsrConstants.ERROR_SECURITY_GROUP_TAB_NAME_UNIQUE);
                            success = false;
                        }
                    }
                }
            }
        }

        success &= super.processCustomAddCollectionLineBusinessRules(document, collectionName, line);
        return success;
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
			Iterator<SecurityGroupTab> it = tabs.iterator();
			while (it.hasNext()) {
				SecurityGroupTab tab = it.next();
				if (tab.isActive()) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Validates the maint. doc before it is written to the db.
	 * 
	 * @see org.kuali.rice.krad.rules.MaintenanceDocumentRuleBase#dataDictionaryValidate(org.kuali.rice.krad.maintenance.MaintenanceDocument)
	 */
	@Override
	protected boolean dataDictionaryValidate(MaintenanceDocument document) {
		boolean success = true;
		SecurityGroup securityGroup = (SecurityGroup) document.getDocumentDataObject();
		if (!hasAtLeastOneActiveTab(securityGroup.getSecurityGroupTabs())) {
			GlobalVariables.getMessageMap().putErrorForSectionId(KsrConstants.SECTION_SECURITY_TABS, KsrConstants.ERROR_SECURITY_GROUP_TAB_MISSING, new String());
			success = false;
		}

		// Check the Security Group Name against the DB
		QueryByCriteria criteria = QueryByCriteria.Builder.fromPredicates(
		        PredicateFactory.equal(KsrConstants.SECURITY_GROUP_NAME, securityGroup.getSecurityGroupName()));

		SecurityGroup temp = getDataObjectService().findUnique(SecurityGroup.class, criteria);

		if (temp != null) {
			if (securityGroup.getSecurityGroupId() != null) {
				if (temp.getSecurityGroupName().equals(securityGroup.getSecurityGroupName())
						&& (!temp.getSecurityGroupId().equals(securityGroup.getSecurityGroupId()))) {
					GlobalVariables.getMessageMap().putError(KsrConstants.KSR_DOCUMENT_MAINTANABLE
							+ "." + KsrConstants.SECURITY_GROUP_NAME, KsrConstants.ERROR_SECURITY_GROUP_NAME_UNIQUE);
					success = false;
				}
			}
			else {
				GlobalVariables.getMessageMap().putError(KsrConstants.KSR_DOCUMENT_MAINTANABLE
						+ "." + KsrConstants.SECURITY_GROUP_NAME, KsrConstants.ERROR_SECURITY_GROUP_NAME_UNIQUE);
				success = false;
			}

		}

		success &= validateSecurityGroupTabs(document);
		success &= super.dataDictionaryValidate(document);
		return success;
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
						GlobalVariables.getMessageMap().putErrorForSectionId(KsrConstants.SECTION_SECURITY_TABS, KsrConstants.ERROR_SECURITY_GROUP_TAB_ORDER_UNIQUE, new String());
						successTabOrder = false;
					}
					if (successTabName
							&& tab.getTabName().equals(tempTab.getTabName())) {
						GlobalVariables.getMessageMap().putErrorForSectionId(KsrConstants.SECTION_SECURITY_TABS, KsrConstants.ERROR_SECURITY_GROUP_TAB_NAME_UNIQUE, new String());
						successTabName = false;
					}

				}
			}
		}

		return successTabOrder && successTabName;
	}

}
