package edu.cornell.kfs.ksr.document.validation.impl;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.criteria.CriteriaLookupService;
import org.kuali.kfs.core.api.criteria.PredicateFactory;
import org.kuali.kfs.core.api.criteria.QueryByCriteria;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.maintenance.rules.MaintenanceDocumentRuleBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.ksr.KSRConstants;
import edu.cornell.kfs.ksr.KSRKeyConstants;
import edu.cornell.kfs.ksr.KSRPropertyConstants;
import edu.cornell.kfs.ksr.businessobject.SecurityGroup;
import edu.cornell.kfs.ksr.businessobject.SecurityGroupTab;

public class SecurityGroupRule extends MaintenanceDocumentRuleBase {

    private CriteriaLookupService criteriaLookupService;

	/**
	 * Validate the new SecurityGroupTab against ones that are already in the list - tab order must be unique - tab name must be unique
	 */
	@Override
	public boolean processCustomAddCollectionLineBusinessRules(MaintenanceDocument document, String collectionName, PersistableBusinessObject line) {
		boolean success = true;

        if (line instanceof SecurityGroupTab) {
            SecurityGroup securityGroup = (SecurityGroup) document.getDocumentDataObject();
            SecurityGroupTab tempTab = (SecurityGroupTab) line;
            if (CollectionUtils.isNotEmpty(securityGroup.getSecurityGroupTabs())) {
                for (SecurityGroupTab tab : securityGroup.getSecurityGroupTabs()) {
                    if (tab.getTabOrder() != null && tab.getTabOrder().equals(tempTab.getTabOrder())) {
                        GlobalVariables.getMessageMap().putError(KSRPropertyConstants.SECURITY_GROUP_TAB_ORDER, KSRKeyConstants.ERROR_SECURITY_GROUP_TAB_ORDER_UNIQUE);
                        success = false;
                    }
                    if (StringUtils.isNotBlank(tab.getTabName()) && StringUtils.equalsIgnoreCase(tab.getTabName(), tempTab.getTabName())) {
                        GlobalVariables.getMessageMap().putError(KSRPropertyConstants.SECURITY_GROUP_TAB_NAME, KSRKeyConstants.ERROR_SECURITY_GROUP_TAB_NAME_UNIQUE);
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
                if (StringUtils.equalsIgnoreCase(temp.getSecurityGroupName(), securityGroup.getSecurityGroupName())
                        && !temp.getSecurityGroupId().equals(securityGroup.getSecurityGroupId())) {
                    GlobalVariables.getMessageMap().putError(KSRPropertyConstants.KSR_DOCUMENT_MAINTAINABLE + "." + KSRPropertyConstants.SECURITY_GROUP_NAME,
                            KSRKeyConstants.ERROR_SECURITY_GROUP_NAME_UNIQUE);
                    success = false;
                }
            } else {
                GlobalVariables.getMessageMap().putError(KSRPropertyConstants.KSR_DOCUMENT_MAINTAINABLE + "." + KSRPropertyConstants.SECURITY_GROUP_NAME,
                        KSRKeyConstants.ERROR_SECURITY_GROUP_NAME_UNIQUE);
                success = false;
            }
        }

        success = validateSecurityGroupTabs(document);

        return (super.dataDictionaryValidate(document) && success);
    }

    private SecurityGroup retrieveSecurityGroupByName(String securityGroupName) {
        if (StringUtils.isBlank(securityGroupName)) {
            return null;
        }
        SecurityGroup securityGroup = null;
        QueryByCriteria criteria = QueryByCriteria.Builder.fromPredicates(
                PredicateFactory.equalIgnoreCase(KSRPropertyConstants.SECURITY_GROUP_NAME, securityGroupName));

        List<SecurityGroup> securityGroups = getCriteriaLookupService()
                .lookup(SecurityGroup.class, criteria)
                .getResults();
        if (CollectionUtils.isNotEmpty(securityGroups)) {
            if (securityGroups.size() > 1) {
                throw new IllegalStateException("Found multiple security groups with name '" +
                        securityGroupName + "' (case-insensitive); this should NEVER happen!");
            }
            securityGroup = securityGroups.get(0);
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
		if (CollectionUtils.isNotEmpty(securityGroupTabs)) {
			for (int i = 0; i < securityGroupTabs.size(); i++) {
				SecurityGroupTab tab = securityGroupTabs.get(i);
				for (int j = i + 1; j < securityGroupTabs.size(); j++) {
					SecurityGroupTab tempTab = securityGroupTabs.get(j);
					if (successTabOrder && tab.getTabOrder() != null
							&& tab.getTabOrder().equals(tempTab.getTabOrder())) {
						GlobalVariables.getMessageMap().putErrorForSectionId(KSRConstants.SECTION_SECURITY_TABS, KSRKeyConstants.ERROR_SECURITY_GROUP_TAB_ORDER_UNIQUE);
						successTabOrder = false;
					}
					if (successTabName && StringUtils.isNotBlank(tab.getTabName())
							&& StringUtils.equalsIgnoreCase(tab.getTabName(), tempTab.getTabName())) {
						GlobalVariables.getMessageMap().putErrorForSectionId(KSRConstants.SECTION_SECURITY_TABS, KSRKeyConstants.ERROR_SECURITY_GROUP_TAB_NAME_UNIQUE);
						successTabName = false;
					}

				}
			}
		}

		return successTabOrder && successTabName;
	}

    public CriteriaLookupService getCriteriaLookupService() {
        if (criteriaLookupService == null) {
            criteriaLookupService = SpringContext.getBean(CriteriaLookupService.class);
        }
        return criteriaLookupService;
    }

}
