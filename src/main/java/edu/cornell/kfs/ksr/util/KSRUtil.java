package edu.cornell.kfs.ksr.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.role.RoleLite;
import org.kuali.kfs.kim.impl.type.KimType;
import org.kuali.kfs.kim.impl.type.KimTypeAttribute;
import org.kuali.kfs.krad.util.BeanPropertyComparator;

import edu.cornell.kfs.ksr.businessobject.SecurityRequestRole;
import edu.cornell.kfs.ksr.businessobject.SecurityRequestRoleQualification;

public class KSRUtil {

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

	@SuppressWarnings("unchecked")
	public static List<KimTypeAttribute> getTypeAttributesForRoleRequest(SecurityRequestRole requestRole) {
		List<KimTypeAttribute> typeAttributes = new ArrayList<KimTypeAttribute>();

		KimType typeInfo = getTypeInfoForRoleRequest(requestRole);
		if (requestRole.isQualifiedRole()) {
			typeAttributes.addAll(typeInfo.getAttributeDefinitions());

			List<String> typeSort = new ArrayList<String>();
			typeSort.add("sortCode");
			Collections.<KimTypeAttribute>sort(typeAttributes, new BeanPropertyComparator(typeSort));
		}

		return typeAttributes;
	}

	public static Map<String, KimTypeAttribute> getTypeAttributesMappedByAttributeId(KimType kimType) {
	    return kimType.getAttributeDefinitions().stream()
	            .collect(Collectors.toUnmodifiableMap(
	                    KimTypeAttribute::getKimAttributeId, attribute -> attribute));
	}

	public static KimType getTypeInfoForRoleRequest(SecurityRequestRole requestRole) {
		RoleLite roleInfo = KimApiServiceLocator.getRoleService().getRoleWithoutMembers(requestRole.getRoleId());

		return KimApiServiceLocator.getKimTypeInfoService().getKimType(roleInfo.getKimTypeId());
	}

	public static boolean isQualificationChangeRequested(SecurityRequestRole securityRequestRole) {
		boolean changeRequested = false;

		List<KimTypeAttribute> typeAttributes = getTypeAttributesForRoleRequest(securityRequestRole);

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
