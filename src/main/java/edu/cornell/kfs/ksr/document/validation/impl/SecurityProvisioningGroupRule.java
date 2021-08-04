package edu.cornell.kfs.ksr.document.validation.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.api.type.KimTypeInfoService;
import org.kuali.kfs.kim.framework.services.KimFrameworkServiceLocator;
import org.kuali.kfs.kim.framework.type.KimTypeService;
import org.kuali.kfs.kim.impl.role.RoleLite;
import org.kuali.kfs.kim.impl.type.KimType;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.maintenance.rules.MaintenanceDocumentRuleBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.MessageMap;

import edu.cornell.kfs.ksr.KSRConstants;
import edu.cornell.kfs.ksr.KSRKeyConstants;
import edu.cornell.kfs.ksr.KSRPropertyConstants;
import edu.cornell.kfs.ksr.businessobject.SecurityProvisioning;
import edu.cornell.kfs.ksr.businessobject.SecurityProvisioningGroup;
import edu.cornell.kfs.ksr.businessobject.SecurityProvisioningGroupDependentRoles;

public class SecurityProvisioningGroupRule extends MaintenanceDocumentRuleBase {


	@Override
	public boolean processCustomAddCollectionLineBusinessRules(MaintenanceDocument document, String collectionName, PersistableBusinessObject line) {
		boolean success = true;

		if (line instanceof SecurityProvisioningGroupDependentRoles) {
			SecurityProvisioningGroupDependentRoles dependentRole = (SecurityProvisioningGroupDependentRoles) line;
			success = validateDependentRole(dependentRole);
		} else if (line instanceof SecurityProvisioningGroup) {
			SecurityProvisioningGroup securityProvisioningGroup = (SecurityProvisioningGroup) line;
			success = validateSecurityProvisioningGroup(document, securityProvisioningGroup);
			if (success && securityProvisioningGroup.isActive()) {
				success = isDuplicateSecurityProvisioningGroup(document, (SecurityProvisioningGroup) line);
			}
		}

		success &= super.processCustomAddCollectionLineBusinessRules(document, collectionName, line);
		return success;
	}

	private boolean isDuplicateSecurityProvisioningGroup(MaintenanceDocument document, SecurityProvisioningGroup line) {
		return isDuplicateSecurityProvisioningGroup(document, line, -1);
	}

	private boolean isDuplicateSecurityProvisioningGroup(MaintenanceDocument document, SecurityProvisioningGroup securityProvisioningGroup, int index) {
		boolean success = true;
		SecurityProvisioning securityProvisioning = (SecurityProvisioning) document.getDocumentDataObject();

		for (int i = 0; i < securityProvisioning.getSecurityProvisioningGroups().size(); i++) {
			if (i == index) {
				continue;
			}
			if (!securityProvisioning.getSecurityProvisioningGroups().get(i).isActive()) {
				continue;
			}
			if (securityProvisioning.getSecurityProvisioningGroups().get(i).getRoleId().equals(securityProvisioningGroup.getRoleId())) {
				success = false;
				MessageMap map = GlobalVariables.getMessageMap();
				String errorPathPrefix = (index != -1 ? "" : "add.")
						+ KSRConstants.SECURITY_PROVISIONING_GROUPS
						+ showIndex(index);
				boolean newErrorPath = false;
				if (map.getErrorPath().size() == 0) {
					newErrorPath = true;
					GlobalVariables.getMessageMap().addToErrorPath(KSRPropertyConstants.KSR_DOCUMENT_MAINTAINABLE);
					GlobalVariables.getMessageMap().addToErrorPath(errorPathPrefix);
				}
				if ((index == -1) || (index > i)) {
					GlobalVariables.getMessageMap().putError(KSRPropertyConstants.PROVISIONING_ROLE_ID, KSRKeyConstants.ERROR_SECURITY_PROVISIONING_GROUP_ROLE_UNIQUE, new String[] { securityProvisioningGroup.getRole().getName() });
				}
				if (newErrorPath) {
					GlobalVariables.getMessageMap().removeFromErrorPath(KSRPropertyConstants.KSR_DOCUMENT_MAINTAINABLE);
					GlobalVariables.getMessageMap().removeFromErrorPath(errorPathPrefix);
				}
			}

		}
		return success;
	}

	private boolean isDuplicateDependentRole(MaintenanceDocument document, SecurityProvisioningGroupDependentRoles dependentRole, int index, int indexRole) {
		boolean success = true;

		SecurityProvisioning securityProvisioning = (SecurityProvisioning) document.getDocumentDataObject();
		List<SecurityProvisioningGroup> securityProvisioningGroupList = securityProvisioning.getSecurityProvisioningGroups();
		SecurityProvisioningGroup securityProvisioningGroup = securityProvisioningGroupList.get(index);

		for (int i = 0; i < securityProvisioningGroup.getDependentRoles().size(); i++) {
			if (i == indexRole) {
				continue;
			}
			if (securityProvisioningGroup.getDependentRoles().get(i).getRoleId().equals(dependentRole.getRoleId())) {
				success = false;
				MessageMap map = GlobalVariables.getMessageMap();
				String errorPathPrefix = (indexRole != -1 ? "" : "add.")
						+ KSRConstants.SECURITY_PROVISIONING_GROUPS
						+ showIndex(index) + "." + KSRConstants.DEPENDENT_ROLES
						+ showIndex(indexRole);

				boolean newErrorPath = false;
				if (map.getErrorPath().size() == 0) {
					newErrorPath = true;
					GlobalVariables.getMessageMap().addToErrorPath(KSRPropertyConstants.KSR_DOCUMENT_MAINTAINABLE);
					GlobalVariables.getMessageMap().addToErrorPath(errorPathPrefix);
				}
				if ((indexRole == -1) || (indexRole > i)) {
					GlobalVariables.getMessageMap().putError(KSRPropertyConstants.PROVISIONING_ROLE_ID, KSRKeyConstants.ERROR_SECURITY_PROVISIONING_GROUP_DEPENDENT_ROLE_UNIQUE, new String[] { dependentRole.getRole().getName() });
				}
				if (newErrorPath) {
					GlobalVariables.getMessageMap().removeFromErrorPath(KSRPropertyConstants.KSR_DOCUMENT_MAINTAINABLE);
					GlobalVariables.getMessageMap().removeFromErrorPath(errorPathPrefix);
				}
			}

		}
		return success;
	}

	private boolean validateDependentRole(SecurityProvisioningGroupDependentRoles dependentRole) {
		return validateDependentRole(dependentRole, -1, -1);
	}

	private boolean validateDependentRole(SecurityProvisioningGroupDependentRoles dependentRole, int index, int indexRole) {
		boolean success = true;

		if (StringUtils.isBlank(dependentRole.getRoleId())) {
			success = false;
			MessageMap map = GlobalVariables.getMessageMap();
			String errorPathPrefix = (indexRole != -1 ? "" : "add.")
					+ KSRConstants.SECURITY_PROVISIONING_GROUPS
					+ showIndex(index) + "." + KSRConstants.DEPENDENT_ROLES
					+ showIndex(indexRole);

			boolean newErrorPath = false;
			if (map.getErrorPath().size() == 0) {
				newErrorPath = true;
				GlobalVariables.getMessageMap().addToErrorPath(KSRPropertyConstants.KSR_DOCUMENT_MAINTAINABLE);
				GlobalVariables.getMessageMap().addToErrorPath(errorPathPrefix);
			}
			GlobalVariables.getMessageMap().putError(KSRPropertyConstants.PROVISIONING_ROLE_ID, KSRKeyConstants.ERROR_SECURITY_PROVISIONING_GROUP_DEPENDENT_ROLE_BLANK);

			if (newErrorPath) {
				GlobalVariables.getMessageMap().removeFromErrorPath(KSRPropertyConstants.KSR_DOCUMENT_MAINTAINABLE);
				GlobalVariables.getMessageMap().removeFromErrorPath(errorPathPrefix);
			}
		}

		return success;
	}

	private boolean validateSecurityProvisioningGroup(MaintenanceDocument document, SecurityProvisioningGroup securityProvisioningGroup) {
		return validateSecurityProvisioningGroup(document, securityProvisioningGroup, -1);
	}

	@SuppressWarnings("deprecation")
	private boolean validateSecurityProvisioningGroup(MaintenanceDocument document, SecurityProvisioningGroup securityProvisioningGroup, int index) {
		boolean success = true;

		RoleLite role = getRoleService().getRoleWithoutMembers(securityProvisioningGroup.getRoleId());
		if (role != null) {
			KimType kimType = getKimTypeInfoService().getKimType(role.getKimTypeId());
			KimTypeService kimTypeService = getKimTypeService(kimType);
			
			if (kimTypeService != null && kimTypeService instanceof org.kuali.kfs.kns.kim.role.DerivedRoleTypeServiceBase) {
				success = false;
				MessageMap map = GlobalVariables.getMessageMap();
				String errorPathPrefix = (index != -1 ? "" : "add.")
						+ KSRConstants.SECURITY_PROVISIONING_GROUPS
						+ showIndex(index);
				boolean newErrorPath = false;
				if (map.getErrorPath().size() == 0) {
					newErrorPath = true;
					GlobalVariables.getMessageMap().addToErrorPath(KSRPropertyConstants.KSR_DOCUMENT_MAINTAINABLE);
					GlobalVariables.getMessageMap().addToErrorPath(errorPathPrefix);
				}

				GlobalVariables.getMessageMap().putError(KSRPropertyConstants.SECURITY_PROVISIONING_GROUP_ROLE_NAME,
				        KSRKeyConstants.ERROR_SECURITY_PROVISIONING_GROUP_ROLE_DERIVED, new String[] { securityProvisioningGroup.getRole().getName() });

				if (newErrorPath) {
					GlobalVariables.getMessageMap().removeFromErrorPath(KSRPropertyConstants.KSR_DOCUMENT_MAINTAINABLE);
					GlobalVariables.getMessageMap().removeFromErrorPath(errorPathPrefix);
				}
			}
		}

		if (success) {
			// Check them for uniqueness.
			// Check first against the other two
			// check second against last.
			if (StringUtils.isNotBlank(securityProvisioningGroup.getDistributedAuthorizerRoleId())) {
				if (StringUtils.isNotBlank(securityProvisioningGroup.getAdditionalAuthorizerRoleId())) {
					if (securityProvisioningGroup.getDistributedAuthorizerRoleId().equals(securityProvisioningGroup.getAdditionalAuthorizerRoleId())) {
						success = false;
						MessageMap map = GlobalVariables.getMessageMap();
						String errorPathPrefix = (index != -1 ? "" : "add.")
								+ KSRConstants.SECURITY_PROVISIONING_GROUPS
								+ showIndex(index);
						boolean newErrorPath = false;
						if (map.getErrorPath().size() == 0) {
							newErrorPath = true;
							GlobalVariables.getMessageMap().addToErrorPath(KSRPropertyConstants.KSR_DOCUMENT_MAINTAINABLE);
							GlobalVariables.getMessageMap().addToErrorPath(errorPathPrefix);
						}

						GlobalVariables.getMessageMap().putError(KSRPropertyConstants.SECURITY_PROVISIONING_GROUP_ADD_AUTH_ROLE_ID, KSRKeyConstants.ERROR_SECURITY_PROVISIONING_GROUP_AUTH_UNIQUE, new String[] {
								KSRConstants.SECURITY_PROVISIONING_GROUP_ADD_AUTH_LBL,
								KSRConstants.SECURITY_PROVISIONING_GROUP_DIST_AUTH_LBL,
								KSRConstants.SECURITY_PROVISIONING_GROUP_CENT_AUTH_LBL });
						if (newErrorPath) {
							GlobalVariables.getMessageMap().removeFromErrorPath(KSRPropertyConstants.KSR_DOCUMENT_MAINTAINABLE);
							GlobalVariables.getMessageMap().removeFromErrorPath(errorPathPrefix);
						}
					}
				}
				if (StringUtils.isNotBlank(securityProvisioningGroup.getCentralAuthorizerRoleId())) {
					if (securityProvisioningGroup.getDistributedAuthorizerRoleId().equals(securityProvisioningGroup.getCentralAuthorizerRoleId())) {
						success = false;
						MessageMap map = GlobalVariables.getMessageMap();
						String errorPathPrefix = (index != -1 ? "" : "add.")
								+ KSRConstants.SECURITY_PROVISIONING_GROUPS
								+ showIndex(index);
						boolean newErrorPath = false;
						if (map.getErrorPath().size() == 0) {
							newErrorPath = true;
							GlobalVariables.getMessageMap().addToErrorPath(KSRPropertyConstants.KSR_DOCUMENT_MAINTAINABLE);
							GlobalVariables.getMessageMap().addToErrorPath(errorPathPrefix);
						}

						GlobalVariables.getMessageMap().putError(KSRPropertyConstants.SECURITY_PROVISIONING_GROUP_CENT_AUTH_ROLE_ID, KSRKeyConstants.ERROR_SECURITY_PROVISIONING_GROUP_AUTH_UNIQUE, new String[] {
								KSRConstants.SECURITY_PROVISIONING_GROUP_CENT_AUTH_LBL,
								KSRConstants.SECURITY_PROVISIONING_GROUP_DIST_AUTH_LBL,
								KSRConstants.SECURITY_PROVISIONING_GROUP_ADD_AUTH_LBL });
						if (newErrorPath) {
							GlobalVariables.getMessageMap().removeFromErrorPath(KSRPropertyConstants.KSR_DOCUMENT_MAINTAINABLE);
							GlobalVariables.getMessageMap().removeFromErrorPath(errorPathPrefix);
						}
					}
				}

			}
			if (StringUtils.isNotBlank(securityProvisioningGroup.getAdditionalAuthorizerRoleId())) {
				if (StringUtils.isNotBlank(securityProvisioningGroup.getCentralAuthorizerRoleId())) {
					if (securityProvisioningGroup.getAdditionalAuthorizerRoleId().equals(securityProvisioningGroup.getCentralAuthorizerRoleId())) {
						success = false;
						MessageMap map = GlobalVariables.getMessageMap();
						String errorPathPrefix = (index != -1 ? "" : "add.")
								+ KSRConstants.SECURITY_PROVISIONING_GROUPS
								+ showIndex(index);
						boolean newErrorPath = false;
						if (map.getErrorPath().size() == 0) {
							newErrorPath = true;
							GlobalVariables.getMessageMap().addToErrorPath(KSRPropertyConstants.KSR_DOCUMENT_MAINTAINABLE);
							GlobalVariables.getMessageMap().addToErrorPath(errorPathPrefix);
						}

						GlobalVariables.getMessageMap().putError(KSRPropertyConstants.SECURITY_PROVISIONING_GROUP_CENT_AUTH_ROLE_ID, KSRKeyConstants.ERROR_SECURITY_PROVISIONING_GROUP_AUTH_UNIQUE, new String[] {
								KSRConstants.SECURITY_PROVISIONING_GROUP_CENT_AUTH_LBL,
								KSRConstants.SECURITY_PROVISIONING_GROUP_DIST_AUTH_LBL,
								KSRConstants.SECURITY_PROVISIONING_GROUP_ADD_AUTH_LBL });
						if (newErrorPath) {
							GlobalVariables.getMessageMap().removeFromErrorPath(KSRPropertyConstants.KSR_DOCUMENT_MAINTAINABLE);
							GlobalVariables.getMessageMap().removeFromErrorPath(errorPathPrefix);
						}
					}
				}

			}
		}

		SecurityProvisioning securityProvisioning = (SecurityProvisioning) document.getDocumentDataObject();
		List<SecurityProvisioningGroup> securityProvisioningGroupList = securityProvisioning.getSecurityProvisioningGroups();
		for (int i = 0; i < securityProvisioningGroupList.size(); i++) {
			if (i == index) {
				continue;
			}
			if (securityProvisioningGroup.getTabId() == null) {
				break;
			}
			if (securityProvisioningGroup.getTabId().equals(securityProvisioningGroupList.get(i).getTabId())) {
				if (securityProvisioningGroup.getRoleTabOrder() == securityProvisioningGroupList.get(i).getRoleTabOrder()) {
					success = false;
					MessageMap map = GlobalVariables.getMessageMap();
					String errorPathPrefix = (index != -1 ? "" : "add.")
							+ KSRConstants.SECURITY_PROVISIONING_GROUPS
							+ showIndex(index);
					boolean newErrorPath = false;
					if (map.getErrorPath().size() == 0) {
						newErrorPath = true;
						GlobalVariables.getMessageMap().addToErrorPath(KSRPropertyConstants.KSR_DOCUMENT_MAINTAINABLE);
						GlobalVariables.getMessageMap().addToErrorPath(errorPathPrefix);
					}

					GlobalVariables.getMessageMap().putError(KSRPropertyConstants.PROVISIONING_ROLE_TAB_ORDER, KSRKeyConstants.ERROR_SECURITY_PROVISIONING_GROUP_TAB_ORDER_UNIQUE);

					if (newErrorPath) {
						GlobalVariables.getMessageMap().removeFromErrorPath(KSRPropertyConstants.KSR_DOCUMENT_MAINTAINABLE);
						GlobalVariables.getMessageMap().removeFromErrorPath(errorPathPrefix);
					}

				}
			}

		}

		return success;
	}

	private String showIndex(int index) {
		return (index == -1 ? "" : "[" + index + "]");
	}

	@Override
	protected boolean dataDictionaryValidate(MaintenanceDocument document) {
		boolean success = true;
		Map<String, SecurityProvisioningGroup> provisioningMap = new HashMap<String, SecurityProvisioningGroup>();
		SecurityProvisioning securityProvisioning = (SecurityProvisioning) document.getDocumentDataObject();

		// Loop through all SecurityProvisioningGroups
		// Validate the SecurityProvisioningGroup and check it for duplicates
		for (int i = 0; i < securityProvisioning.getSecurityProvisioningGroups().size(); i++) {
			SecurityProvisioningGroup securityProvisioningGroup = securityProvisioning.getSecurityProvisioningGroups().get(i);
			success = validateSecurityProvisioningGroup(document, securityProvisioningGroup, i);
			if (!securityProvisioningGroup.isActive()) {
				continue;
			}
			success &= isDuplicateSecurityProvisioningGroup(document, securityProvisioningGroup, i);
			provisioningMap.put(securityProvisioningGroup.getRoleId(), securityProvisioningGroup);

			// Loop through all SecurityProvisioningGroupDependentRoles of the current SecurityProvisioningGroup.
			// Validate the SecurityProvisioningGroup and check it for duplicates.
			for (int j = 0; j < securityProvisioningGroup.getDependentRoles().size(); j++) {
				success &= validateDependentRole(securityProvisioningGroup.getDependentRoles().get(j), i, j);
				success &= isDuplicateDependentRole(document, securityProvisioningGroup.getDependentRoles().get(j), i, j);
				success &= hasSecurityProvisioningGroup(document, securityProvisioningGroup.getDependentRoles().get(j), i, j);
			}
		}
		if (success) {
			// Loop through each SecurityProvisioningGroup to find a circular reference
			for (int i = 0; i < securityProvisioning.getSecurityProvisioningGroups().size(); i++) {
				if (!securityProvisioning.getSecurityProvisioningGroups().get(i).isActive()) {
					continue;
				}
				Map<String, SecurityProvisioningGroup> provisioningMapTemp = new HashMap<String, SecurityProvisioningGroup>();
				provisioningMapTemp.putAll(provisioningMap);
				String roleStr = buildDependentString(securityProvisioning.getSecurityProvisioningGroups().get(i).getRoleId(), provisioningMapTemp);
				roleStr = StringUtils.strip(roleStr, ",");
				int matches = StringUtils.countMatches(roleStr, "|"
						+ securityProvisioning.getSecurityProvisioningGroups().get(i).getRoleId()
						+ "|");
				if (matches > 1) {
					success = false;
					MessageMap map = GlobalVariables.getMessageMap();
					String errorPathPrefix = KSRConstants.SECURITY_PROVISIONING_GROUPS
							+ showIndex(i);
					boolean newErrorPath = false;
					if (map.getErrorPath().size() == 0) {
						newErrorPath = true;
						GlobalVariables.getMessageMap().addToErrorPath(KSRPropertyConstants.KSR_DOCUMENT_MAINTAINABLE);
						GlobalVariables.getMessageMap().addToErrorPath(errorPathPrefix);
					}

					GlobalVariables.getMessageMap().putError(KSRPropertyConstants.SECURITY_PROVISIONING_GROUP_ROLE_NAME, KSRKeyConstants.ERROR_SECURITY_PROVISIONING_GROUP_CIRCULAR_REFERENCE, new String[] { securityProvisioning.getSecurityProvisioningGroups().get(i).getRole().getName() });

					if (newErrorPath) {
						GlobalVariables.getMessageMap().removeFromErrorPath(KSRPropertyConstants.KSR_DOCUMENT_MAINTAINABLE);
						GlobalVariables.getMessageMap().removeFromErrorPath(errorPathPrefix);
					}
				}
			}
		}
		return super.dataDictionaryValidate(document) && success;

	}

	private boolean hasSecurityProvisioningGroup(MaintenanceDocument document, SecurityProvisioningGroupDependentRoles dependentRole, int index, int indexRole) {
		boolean success = false;
		SecurityProvisioning securityProvisioning = (SecurityProvisioning) document.getDocumentDataObject();
		List<SecurityProvisioningGroup> securityProvisioningGroups = securityProvisioning.getSecurityProvisioningGroups();
		for (int i = 0; i < securityProvisioningGroups.size(); i++) {
			if (securityProvisioningGroups.get(i).getRoleId().equals(dependentRole.getRoleId())
					&& securityProvisioningGroups.get(i).isActive()) {
				success = true;
			}
		}

		if (!success) {
			MessageMap map = GlobalVariables.getMessageMap();
			String errorPathPrefix = (indexRole != -1 ? "" : "add.")
					+ KSRConstants.SECURITY_PROVISIONING_GROUPS
					+ showIndex(index) + "." + KSRConstants.DEPENDENT_ROLES
					+ showIndex(indexRole);

			boolean newErrorPath = false;
			if (map.getErrorPath().size() == 0) {
				newErrorPath = true;
				GlobalVariables.getMessageMap().addToErrorPath(KSRPropertyConstants.KSR_DOCUMENT_MAINTAINABLE);
				GlobalVariables.getMessageMap().addToErrorPath(errorPathPrefix);
			}
			GlobalVariables.getMessageMap().putError(KSRPropertyConstants.PROVISIONING_ROLE_ID, KSRKeyConstants.ERROR_SECURITY_PROVISIONING_GROUP_DEPENDENT_ROLE_MATCH, new String[] { dependentRole.getRole().getName() });

			if (newErrorPath) {
				GlobalVariables.getMessageMap().removeFromErrorPath(KSRPropertyConstants.KSR_DOCUMENT_MAINTAINABLE);
				GlobalVariables.getMessageMap().removeFromErrorPath(errorPathPrefix);
			}
		}
		return success;
	}

	private String buildDependentString(String roleID, Map<String, SecurityProvisioningGroup> provisioningMap) {
		String temp = "|" + roleID + "|";
		SecurityProvisioningGroup securityProvisioningGroup = provisioningMap.get(roleID);
		provisioningMap.remove(roleID);
		if ((securityProvisioningGroup != null)
				&& (securityProvisioningGroup.getDependentRoles().size() > 0)) {
			for (int i = 0; i < securityProvisioningGroup.getDependentRoles().size(); i++) {
				String dependentRoleID = securityProvisioningGroup.getDependentRoles().get(i).getRoleId();
				temp += buildDependentString(dependentRoleID, provisioningMap);
			}
		}
		return "," + temp;
	}

	protected KimTypeInfoService getKimTypeInfoService() {
	    return KimApiServiceLocator.getKimTypeInfoService();
	}

	protected KimTypeService getKimTypeService(KimType kimType) {
	    return (kimType == null) ? null : KimFrameworkServiceLocator.getKimTypeService(kimType);
	}

}
