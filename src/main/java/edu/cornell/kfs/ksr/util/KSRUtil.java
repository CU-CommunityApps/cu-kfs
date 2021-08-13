package edu.cornell.kfs.ksr.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.role.Role;
import org.kuali.kfs.kim.impl.role.RoleLite;
import org.kuali.kfs.kim.impl.type.KimType;
import org.kuali.kfs.kim.impl.type.KimTypeAttribute;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.util.BeanPropertyComparator;

import edu.cornell.kfs.ksr.businessobject.SecurityRequestRole;
import edu.cornell.kfs.ksr.businessobject.SecurityRequestRoleQualification;

public class KSRUtil {

	/**
	 * Append namespace to front of Role name, ex: "KR-KR - Kuali Rice Engineer"
	 * 
	 * @param role
	 *            - role class containing all info
	 * @param roleID
	 *            - role ID for possible lookup
	 */
	public static void appendNameSpace(Role role, String roleID) {
		if ((role == null) || (!StringUtils.equals(roleID, role.getId()))) {
			role = KRADServiceLocator.getBusinessObjectService().findBySinglePrimaryKey(Role.class, roleID);

			if (role != null) {
				String temp = role.getNamespaceCode() + " - "
						+ role.getName();
				role.setName(temp);
			}
		}
		else if ((role != null) && (role.getName() != null)
				&& (role.getName().indexOf(role.getNamespaceCode()) == -1)) {
			String temp = role.getNamespaceCode() + " - " + role.getName();
			role.setName(temp);
		}
	}

	/**
	 * Builds a String representation for the given set of qualifications
	 * 
	 * @param qualifications
	 *            - set of qualifications to build string for
	 * @param typeAttributes
	 *            - KIM type attributes for which the qualifications apply to
	 * @return String containing each qualification set separated by semicolon, and each attribute value separated by space
	 */
	public static String buildQualificationString(List<Map<String,String>> qualifications, List<KimTypeAttribute> typeAttributes) {
		String qualificationsString = "";

		for (Map<String,String> qualification : qualifications) {
			String attributesString = "";
			for (KimTypeAttribute attributeInfo : typeAttributes) {
				if (StringUtils.isNotBlank(attributesString)) {
					attributesString += " ";
				}

				if ((qualification != null)
						&& qualification.containsKey(attributeInfo.getKimAttribute().getAttributeName())) {
					attributesString += "'"
							+ qualification.get(attributeInfo.getKimAttribute().getAttributeName())
							+ "'";
				}
				else {
					attributesString += "''";
				}
			}

			if (StringUtils.isNotBlank(attributesString)) {
				if (StringUtils.isNotBlank(qualificationsString)) {
					qualificationsString += ";";
				}
				qualificationsString += attributesString;
			}
		}

		return qualificationsString;
	}

	/**
	 * Retrieves the type attributes for the role on the given request role instance
	 * 
	 * @param requestRole
	 *            - security request role instance to retrieve attribute info for
	 * @return List<KimTypeAttributeInfo> type attribute information
	 */
	@SuppressWarnings("unchecked")
	public static List<KimTypeAttribute> getTypeAttributesForRoleRequest(SecurityRequestRole requestRole) {
		List<KimTypeAttribute> typeAttributes = new ArrayList<KimTypeAttribute>();

		KimType typeInfo = getTypeInfoForRoleRequest(requestRole);
		if (requestRole.isQualifiedRole()) {
			// ==== CU Customization: Adjusted as needed to prevent sorting of an immutable list. ====
			typeAttributes.addAll(typeInfo.getAttributeDefinitions());

			List<String> typeSort = new ArrayList<String>();
			typeSort.add("sortCode");
			Collections.<KimTypeAttribute>sort(typeAttributes, new BeanPropertyComparator(typeSort));
		}

		return typeAttributes;
	}

	/**
	 * Retrieves KIM type information for the role on the given request role instance
	 * 
	 * @param requestRole
	 *            - security request role instance to retrieve type info for
	 * @return KimTypeInfo type info for role
	 */
	public static KimType getTypeInfoForRoleRequest(SecurityRequestRole requestRole) {
		RoleLite roleInfo = KimApiServiceLocator.getRoleService().getRoleWithoutMembers(requestRole.getRoleId());

		return KimApiServiceLocator.getKimTypeInfoService().getKimType(roleInfo.getKimTypeId());
	}

	/**
	 * Indicates whether the qualifications requested on the give security request role are different from the principal's current qualifications
	 * 
	 * @param securityRequestRole
	 *            - security request role to check qualifications for
	 * @return boolean true if qualifications have changed, false if they are the same
	 */
	public static boolean isQualificationChangeRequested(SecurityRequestRole securityRequestRole) {
		boolean changeRequested = false;

		List<KimTypeAttribute> typeAttributes = getTypeAttributesForRoleRequest(securityRequestRole);

		// build qualifications string and compare to current
		List<Map<String,String>> requestedQualifications = new ArrayList<Map<String,String>>();
		for (SecurityRequestRoleQualification requestRoleQualification : securityRequestRole.getRequestRoleQualifications()) {
			requestedQualifications.add(requestRoleQualification.buildQualificationAttributeSet());
		}

		String qualificationsString = KSRUtil.buildQualificationString(requestedQualifications, typeAttributes);
		if (StringUtils.isBlank(qualificationsString)) {
			qualificationsString = null;
		}

		if (!StringUtils.equals(qualificationsString, securityRequestRole.getCurrentQualifications())) {
			changeRequested = true;
		}

		return changeRequested;
	}

    /**
     * Determines whether the two qualification sets match. A match is made if both are same size, both contain the same
     * attributes, and contain the same values for each attribute
     * 
     * @param qualification1
     *            - qualification set to match
     * @param qualification2
     *            - second qualification set to match
     * @return boolean true if qualification sets match, false if they do not match
     */
    public static boolean doQualificationsMatch(Map<String,String> qualification1, Map<String,String> qualification2) {
        boolean match = true;

        if (qualification1.size() != qualification2.size()) {
            match = false;
        }
        else {
            for (String attributeName : qualification1.keySet()) {
                String attributeValue = qualification1.get(attributeName);

                if (!qualification2.containsKey(attributeName)) {
                    match = false;
                    break;
                }

                String attributeValue2 = qualification2.get(attributeName);
                if (!StringUtils.equals(attributeValue, attributeValue2)) {
                    match = false;
                    break;
                }
            }
        }

        return match;
    }
}
