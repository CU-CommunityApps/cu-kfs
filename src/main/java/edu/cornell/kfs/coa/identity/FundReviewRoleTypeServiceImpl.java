package edu.cornell.kfs.coa.identity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.util.KimCommonUtils;
import org.kuali.kfs.kns.kim.role.RoleTypeServiceBase;

import edu.cornell.kfs.kim.bo.impl.CuKimAttributes;

public class FundReviewRoleTypeServiceImpl extends RoleTypeServiceBase {
    
    @Override
    protected boolean performMatch(final Map<String, String> qualification, final Map<String, String> roleQualifier) {
        if (KimCommonUtils.storedValueNotSpecifiedOrInputValueMatches(roleQualifier, qualification,
                CuKimAttributes.FUND_GROUP_CODE)) {
            final Set<String> potentialParentDocumentTypeNames = new HashSet<>(1);
            if (roleQualifier.containsKey(KimConstants.AttributeConstants.DOCUMENT_TYPE_NAME)) {
                potentialParentDocumentTypeNames
                        .add(roleQualifier.get(KimConstants.AttributeConstants.DOCUMENT_TYPE_NAME));
            }
            if (qualification == null || qualification.isEmpty()) {
                return potentialParentDocumentTypeNames.isEmpty();
            }
            return potentialParentDocumentTypeNames.isEmpty()
                   || qualification.get(KimConstants.AttributeConstants.DOCUMENT_TYPE_NAME).equalsIgnoreCase(
                            roleQualifier.get(KimConstants.AttributeConstants.DOCUMENT_TYPE_NAME))
                   || getClosestParentDocumentTypeName(documentTypeService
                           .getDocumentTypeByName(qualification
                                   .get(KimConstants.AttributeConstants.DOCUMENT_TYPE_NAME)),
                                   potentialParentDocumentTypeNames) != null;
        }
        return false;
    }

}
