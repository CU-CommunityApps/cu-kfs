package edu.cornell.kfs.ksr.document.validation.impl;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.criteria.PredicateFactory;
import org.kuali.kfs.core.api.criteria.QueryByCriteria;
import org.kuali.kfs.core.api.uif.AttributeError;
import org.kuali.kfs.coreservice.framework.CoreFrameworkServiceLocator;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.framework.services.KimFrameworkServiceLocator;
import org.kuali.kfs.kim.framework.type.KimTypeService;
import org.kuali.kfs.kim.impl.type.KimType;
import org.kuali.kfs.kns.rules.TransactionalDocumentRuleBase;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.ksr.KSRConstants;
import edu.cornell.kfs.ksr.KSRKeyConstants;
import edu.cornell.kfs.ksr.KSRPropertyConstants;
import edu.cornell.kfs.ksr.businessobject.SecurityProvisioningGroup;
import edu.cornell.kfs.ksr.businessobject.SecurityProvisioningGroupDependentRoles;
import edu.cornell.kfs.ksr.businessobject.SecurityRequestRole;
import edu.cornell.kfs.ksr.businessobject.SecurityRequestRoleQualification;
import edu.cornell.kfs.ksr.businessobject.SecurityRequestRoleQualificationDetail;
import edu.cornell.kfs.ksr.document.SecurityRequestDocument;
import edu.cornell.kfs.ksr.service.SecurityRequestDocumentService;
import edu.cornell.kfs.ksr.util.KSRUtil;

public class SecurityRequestDocumentRule extends TransactionalDocumentRuleBase  implements AddQualificationRule {

    protected Map<String,String[]> getRequiredQualificationsMap() {
        Map<String,String[]> currentQualMap = new HashMap<String,String[]>(50);
        Collection<String> newQualifications = getParameterService().getParameterValuesAsString(
                KSRConstants.KSR_NAMESPACE, KRADConstants.DetailTypes.DOCUMENT_DETAIL_TYPE, KSRConstants.REQUIRED_QUALIFICATIONS_PARAMETER);
        if (newQualifications != null) {
            for (String newQualification : newQualifications) {
                String newKey = newQualification.substring(0, newQualification.indexOf(':'));
                String[] newValues = newQualification.substring(newQualification.indexOf(':') + 1).split(",");
                currentQualMap.put(newKey, newValues);
            }
        }
        return currentQualMap;
    }

    @Override
    public boolean isDocumentAttributesValid(Document document, boolean validateRequired) {
        SecurityRequestDocument securityRequestDocument = (SecurityRequestDocument) document;
        boolean success = validateDepartmentCode(securityRequestDocument);

        for (int i = 0; i < securityRequestDocument.getSecurityRequestRoles().size(); i++) {
            SecurityRequestRole securityRequestRole = securityRequestDocument.getSecurityRequestRoles().get(i);
            KimType tempTypeInfo = KimApiServiceLocator.getKimTypeInfoService().getKimType(securityRequestRole.getRoleInfo().getKimTypeId());

            if (!securityRequestRole.isActive()) {
                continue;
            } else if (!securityRequestRole.isQualifiedRole()) {
                continue;
            } else {
                String[] requiredQualifications = getRequiredQualificationsMap().get(tempTypeInfo.getServiceName());
                if (requiredQualifications != null && requiredQualifications.length > 0) {
                    boolean qualsValid = true;
                    boolean[] hasQuals = new boolean[requiredQualifications.length];
                    for (SecurityRequestRoleQualification roleQualification : securityRequestRole.getRequestRoleQualifications()) {
                        Arrays.fill(hasQuals, false);
                        for (SecurityRequestRoleQualificationDetail qualDetail : roleQualification.getRoleQualificationDetails()) {
                            for (int j = requiredQualifications.length - 1; j >= 0; j--) {
                                if (requiredQualifications[j].equals(qualDetail.getAttributeName()) && StringUtils.isNotBlank(qualDetail.getAttributeValue())) {
                                    hasQuals[j] = true;
                                }
                            }
                        }
                        for (boolean hasQual : hasQuals) {
                            qualsValid &= hasQual;
                        }
                    }
                    if (!qualsValid || securityRequestRole.getRequestRoleQualifications().size() == 0) {
                        success = false;
                        GlobalVariables.getMessageMap().putError(KSRPropertyConstants.SECURITY_REQUEST_DOC_REQUEST_ROLE + "[" + i + "].active",
                                KSRKeyConstants.ERROR_SECURITY_REQUEST_DOC_QUALIFIER_MULTI_MISSING, new String[]{securityRequestRole.getRoleInfo().getName()});
                    }
                } else if (securityRequestRole.getRequestRoleQualifications().size() == 0) {
                    securityRequestRole.getRequestRoleQualifications().add(
                            SpringContext.getBean(SecurityRequestDocumentService.class).buildRoleQualificationLine(securityRequestRole, null));
                }
            }
            
            for (int j = 0; j < securityRequestRole.getRequestRoleQualifications().size(); j++) {
                SecurityRequestRoleQualification requestRoleQualification = securityRequestRole.getRequestRoleQualifications().get(j);
                
                String typeId = securityRequestRole.getRoleInfo().getKimTypeId();
                KimType typeInfo = KimApiServiceLocator.getKimTypeInfoService().getKimType(typeId);
            
                String fieldKeyPrefix = KSRPropertyConstants.SECURITY_REQUEST_DOC_REQUEST_ROLE + "[" + i + "]."
                + KSRPropertyConstants.SECURITY_REQUEST_DOC_ROLE_QUAL + "[" + j + "].";
                success &= validateQualification(typeInfo, requestRoleQualification, fieldKeyPrefix);
            }
        }

        return super.isDocumentAttributesValid(document, validateRequired)  && success;
    }

    protected boolean validateQualification(KimType typeInfo, SecurityRequestRoleQualification requestRoleQualification, String fieldKeyPrefix) {
        boolean success = true;
        
        KimTypeService kimTypeService = KimFrameworkServiceLocator.getKimTypeService(typeInfo);
        try {
            List<AttributeError> attributeErrors = kimTypeService.validateAttributes(typeInfo.getId(), requestRoleQualification.buildQualificationAttributeSet());
            if (!attributeErrors.isEmpty()) {
                success = false;
            }

            for (AttributeError entry : attributeErrors) {
                String attributeName = entry.getAttributeName();
                int qualificationDetailIndex = findQualificationRecordForAttribute(
                        requestRoleQualification.getRoleQualificationDetails(), attributeName);
                String fieldKey = fieldKeyPrefix + KSRPropertyConstants.SECURITY_REQUEST_DOC_ROLE_QUAL_DETAILS + "["
                        + qualificationDetailIndex + "]." + KSRPropertyConstants.SECURITY_REQUEST_DOC_ROLE_ATTR_VALUE;

                String attributeError = entry.getMessage();
                String messageKey = StringUtils.substringBefore(attributeError, ":");
                String messageParameter = StringUtils.substringAfter(attributeError, ":");

                GlobalVariables.getMessageMap().putError(fieldKey, messageKey, messageParameter);
            }
        }
        catch (Exception e) {
            String input = KRADConstants.DOCUMENT_PROPERTY_NAME + "." + KRADConstants.DOCUMENT_HEADER_PROPERTY_NAME;

            GlobalVariables.getMessageMap().putInfo(input, KSRKeyConstants.ERROR_SECURITY_REQUEST_DOC_SERVICE_EXCEPTION,
                    new String[]{"SOME FILLER TEXT"});
        }
        
        return success;
    }

    protected boolean validateDepartmentCode(SecurityRequestDocument securityRequestDocument) {
        boolean success = true;

        try {
            String primaryDepartmentCode = securityRequestDocument.getPrimaryDepartmentCode();

            if (StringUtils.isBlank(primaryDepartmentCode) || StringUtils.contains(primaryDepartmentCode, " ")) {
                GlobalVariables.getMessageMap().putError("document.primaryDepartmentCode", "Invalid Primary Department"); //todo cleanup
                return false;
            }

            //todo check for valid department
        }
        catch (Exception e) {
            String documentFieldName = KRADConstants.DOCUMENT_PROPERTY_NAME + "." + "primaryDepartmentCode";
            GlobalVariables.getMessageMap().putInfo(documentFieldName, KSRKeyConstants.ERROR_SECURITY_REQUEST_DOC_SERVICE_EXCEPTION, new String[]{"SOME FILLER TEXT"});
        }

        return success;
    }
    
    protected int findQualificationRecordForAttribute(List<SecurityRequestRoleQualificationDetail> qualificationDetails,
            String attributeName) {
        int index = -1;

        for (int i = 0; i < qualificationDetails.size(); i++) {
            SecurityRequestRoleQualificationDetail qualificationDetail = qualificationDetails.get(i);
            if (StringUtils.equals(qualificationDetail.getAttributeName(), attributeName)) {
                return i;
            }
        }

        return index;
    }

    @Override
    protected boolean processCustomSaveDocumentBusinessRules(Document document) {
        boolean success = true;
        SecurityRequestDocument securityRequestDocument = (SecurityRequestDocument) document;

        if (StringUtils.isEmpty(securityRequestDocument.getPrincipalId())) {
            success = false;
            String input = KSRPropertyConstants.SECURITY_REQUEST_DOC_PRINCIPAL_ID;
            GlobalVariables.getMessageMap().putErrorForSectionId(input, KSRKeyConstants.ERROR_SECURITY_REQUEST_DOC_PRINCIPAL_ID_MISSING);
        }

        return super.processCustomSaveDocumentBusinessRules(document)
                && success;
    }

    @Override
    public boolean processAddRoleQualification(SecurityRequestDocument document, SecurityRequestRoleQualification roleQualification) {
        if (!roleQualification.getRoleQualificationDetails().isEmpty()) {
            SecurityRequestRoleQualificationDetail qualificationDetail = roleQualification.getRoleQualificationDetails().get(0);

            return validateQualification(qualificationDetail.getKimType(), roleQualification, "");
        }

        return true;
    }

    @Override
    protected boolean processCustomRouteDocumentBusinessRules(Document document) {
        boolean success = true;
        SecurityRequestDocument securityRequestDocument = (SecurityRequestDocument) document;

        if (!isDocumentChanged(securityRequestDocument)) {
            success = false;
            String input = KRADConstants.DOCUMENT_PROPERTY_NAME + "."
                    + KRADConstants.DOCUMENT_HEADER_PROPERTY_NAME;

            GlobalVariables.getMessageMap().putError(input, KSRKeyConstants.ERROR_SECURITY_REQUEST_DOC_CHANGE_MISSING);
        }
        else {
            List<SecurityRequestRole> activeRoles = new ArrayList<SecurityRequestRole>();
            Map<String, String> indexes = new HashMap<String, String>();
            for (int i = 0; i < securityRequestDocument.getSecurityRequestRoles().size(); i++) {
                SecurityRequestRole securityRequestRole = securityRequestDocument.getSecurityRequestRoles().get(i);
                indexes.put(securityRequestRole.getRoleId(), Integer.toString(i));
                if (securityRequestRole.isActive()) {
                    activeRoles.add(securityRequestRole);
                }
            }
            for (int i = 0; i < activeRoles.size(); i++) {
                List<String> rolesNeeded = new ArrayList<String>();
                SecurityRequestRole securityRequestRole = activeRoles.get(i);

                Map<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put(KSRPropertyConstants.PROVISIONING_ROLE_ID, securityRequestRole.getRoleId());
                hashMap.put(KSRPropertyConstants.SECURITY_GROUP_ID, securityRequestDocument.getSecurityGroupId());
                hashMap.put(KSRPropertyConstants.SECURITY_GROUP_ACTIVE_INDICATOR, Boolean.TRUE);

                List<SecurityProvisioningGroup> objList = (List<SecurityProvisioningGroup>) KRADServiceLocator.getBusinessObjectService().findMatching(SecurityProvisioningGroup.class, hashMap);
                SecurityProvisioningGroup securityProvisioningGroup = null;
                if ((objList != null) && (objList.size() != 0)) {
                    securityProvisioningGroup = objList.get(0);
                }
                if (securityProvisioningGroup != null) {
                    for (int j = 0; j < securityProvisioningGroup.getDependentRoles().size(); j++) {
                        SecurityProvisioningGroupDependentRoles dependentRole = securityProvisioningGroup.getDependentRoles().get(j);
                        if (!dependentRole.isActive()) {
                            continue;
                        }
                        boolean found = false;
                        for (int k = 0; k < activeRoles.size(); k++) {
                            if (activeRoles.get(k).getRoleId().equals(dependentRole.getRoleId())) {
                                found = true;
                            }
                        }
                        if (!found) {
                            String index = indexes.get(dependentRole.getRoleId());
                            String input = KSRPropertyConstants.SECURITY_REQUEST_DOC_REQUEST_ROLE
                                    + "[" + index + "].active";
                            GlobalVariables.getMessageMap().putError(input, KSRKeyConstants.ERROR_SECURITY_REQUEST_DOC_DEPENDENT_ROLE_MISSING, new String[] {
                                    securityRequestRole.getRoleInfo().getName(),
                                    dependentRole.getRole().getName() });
                            rolesNeeded.add(dependentRole.getRole().getName());
                            success = false;
                        }
                    }
                }

            }
        }
        
        return super.processCustomRouteDocumentBusinessRules(document)
                && success;
    }

    protected boolean isDocumentChanged(SecurityRequestDocument securityRequestDocument) {
        boolean success = false;

        for (int i = 0; i < securityRequestDocument.getSecurityRequestRoles().size(); i++) {
            SecurityRequestRole securityRequestRole = securityRequestDocument.getSecurityRequestRoles().get(i);
            if (securityRequestRole.isCurrentActive() != securityRequestRole.isActive()) {
                success |= true;
            } else if (securityRequestRole.isQualifiedRole()) {
                success |= KSRUtil.isQualificationChangeRequested(securityRequestRole);
            }
        }
        
        return success;
    }
    
    protected ParameterService getParameterService() {
        return CoreFrameworkServiceLocator.getParameterService();
    }
    
    protected BusinessObjectService getBusinessObjectService() {
        return KRADServiceLocator.getBusinessObjectService();
    }
}
