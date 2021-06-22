package edu.cornell.kfs.ksr.document.validation.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.maintenance.rules.MaintenanceDocumentRuleBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.ksr.KsrConstants;
import edu.cornell.kfs.ksr.businessobject.SecurityGroup;
import edu.cornell.kfs.ksr.businessobject.SecurityGroupTab;

public class SecurityGroupRule extends MaintenanceDocumentRuleBase {

	/**
	 * Validate the new SecurityGroupTab against ones that are already in the list - tab order must be unique - tab name must be unique
	 * 
	 * @see org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomAddCollectionLineBusinessRules(org.kuali.rice.kns.document.MaintenanceDocument, java.lang.String,
	 *      org.kuali.rice.kns.bo.PersistableBusinessObject)
	 */
	@Override
	public boolean processCustomAddCollectionLineBusinessRules(MaintenanceDocument document, String collectionName, PersistableBusinessObject line) {
		boolean success = true;

		SecurityGroup securityGroup = (SecurityGroup) document.getDocumentDataObject();
		SecurityGroupTab tempTab = (SecurityGroupTab) line;
		if (securityGroup.getSecurityGroupTabs() != null) {
			success = validateSecurityGroupTabs(document);
			if (success) {
				Iterator<SecurityGroupTab> it = securityGroup.getSecurityGroupTabs().iterator();
				while (it.hasNext()) {
					SecurityGroupTab tab = it.next();

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
	 * @see org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase#dataDictionaryValidate(org.kuali.rice.kns.document.MaintenanceDocument)
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
		Map<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put(KsrConstants.SECURITY_GROUP_NAME, securityGroup.getSecurityGroupName());

		BusinessObjectService businessObjectService = SpringContext.getBean(BusinessObjectService.class);
		SecurityGroup temp = (SecurityGroup) businessObjectService.findByPrimaryKey(SecurityGroup.class, hashMap);

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

		success = validateSecurityGroupTabs(document);

		return (super.dataDictionaryValidate(document) && success);
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
