package edu.cornell.kfs.ksr.document.validation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coreservice.framework.CoreFrameworkServiceLocator;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.framework.services.KimFrameworkServiceLocator;
import org.kuali.kfs.kim.framework.type.KimTypeService;
import org.kuali.kfs.kim.impl.type.KimType;
import org.kuali.kfs.kns.rules.TransactionalDocumentRuleBase;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;

import edu.cornell.kfs.ksr.KSRConstants;
import edu.cornell.kfs.ksr.businessobject.SecurityProvisioningGroupDependentRoles;
import edu.cornell.kfs.ksr.businessobject.SecurityRequestRole;
import edu.cornell.kfs.ksr.businessobject.SecurityRequestRoleQualification;
import edu.cornell.kfs.ksr.businessobject.SecurityRequestRoleQualificationDetail;
import edu.cornell.kfs.ksr.document.SecurityRequestDocument;
import edu.cornell.kfs.ksr.util.KSRUtil;

/**
 * ==== CU Customization: The "isDocumentChanged" method has been altered so that the lines which overwrite the "success" flag's value will instead have its old
 * value ORed with any newly-introduced value.
 * 
 * CU Customization (CYNERGY-2290): Fixed an area of the code where it was possible for inactive provisioning groups to be used by mistake.
 * 
 * CU Customization (CYNERGY-2412): Corrected a qualification validation section so that qualified request roles lacking qualifications will not fail validation
 * if the associated role type service does not consider them required.
 * 
 * CU Customization (CYNERGY-2513): Updated validation code to automatically add a blank qualifier if the role membership is active, does not have any
 * qualifiers, and does not expect non-blank qualifications.
 * 
 * CU Customization: Remediated this file as needed for Rice 2.x compatibility. ====
 * 
 * Validate parts and/or all of the SecurityRequestDocument
 * 
 * @author rSmart Development Team
 */
public class SecurityRequestDocumentRule extends TransactionalDocumentRuleBase implements AddQualificationRule {

    @Override
    public boolean processAddRoleQualification(SecurityRequestDocument document, SecurityRequestRoleQualification roleQualification) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * protected Map<String,String[]> getRequiredQualificationsMap() { Map<String,String[]> currentQualMap = new HashMap<String,String[]>(50);
     * Collection<String> newQualifications = getParameterService().getParameterValuesAsString( KSRConstants.KSR_NAMESPACE,
     * KRADConstants.DetailTypes.DOCUMENT_DETAIL_TYPE, KSRConstants.REQUIRED_QUALIFICATIONS_PARAMETER); if (newQualifications != null) { for (String
     * newQualification : newQualifications) { String newKey = newQualification.substring(0, newQualification.indexOf(':')); String[] newValues =
     * newQualification.substring(newQualification.indexOf(':') + 1).split(","); currentQualMap.put(newKey, newValues); } } return currentQualMap; }
     * 
     *//**
        * Apply validation to all the qualifiers in the document Checks: -If a role requires qualifiers and has at least 1. -The entered qualifiers are in fact
        * valid.
        * 
        * @see org.kuali.rice.krad.rules.DocumentRuleBase#isDocumentAttributesValid(org.kuali.rice.krad.document.Document, boolean)
        */
    /*
     * @Override public boolean isDocumentAttributesValid(Document document, boolean validateRequired) { boolean success = true; SecurityRequestDocument
     * securityRequestDocument = (SecurityRequestDocument) document;
     * 
     * for (int i = 0; i < securityRequestDocument.getSecurityRequestRoles().size(); i++) { SecurityRequestRole securityRequestRole =
     * securityRequestDocument.getSecurityRequestRoles().get(i); KimType tempTypeInfo =
     * getKimTypeInfoService().getKimType(securityRequestRole.getRoleInfo().getKimTypeId());
     * 
     * if (!securityRequestRole.isActive()) { continue; } else if (!securityRequestRole.isQualifiedRole()) { continue; } else { // Check if each qualifier
     * contains non-blank values for any required qualifications. String[] requiredQualifications =
     * getRequiredQualificationsMap().get(tempTypeInfo.getServiceName()); if (requiredQualifications != null && requiredQualifications.length > 0) { boolean
     * qualsValid = true; boolean[] hasQuals = new boolean[requiredQualifications.length]; for (SecurityRequestRoleQualification roleQualification :
     * securityRequestRole.getRequestRoleQualifications()) { Arrays.fill(hasQuals, false); for (SecurityRequestRoleQualificationDetail qualDetail :
     * roleQualification.getRoleQualificationDetails()) { for (int j = requiredQualifications.length - 1; j >= 0; j--) { if
     * (requiredQualifications[j].equals(qualDetail.getAttributeName()) && StringUtils.isNotBlank(qualDetail.getAttributeValue())) { hasQuals[j] = true; } } }
     * for (boolean hasQual : hasQuals) { qualsValid &= hasQual; } } if (!qualsValid || securityRequestRole.getRequestRoleQualifications().size() == 0) {
     * success = false; GlobalVariables.getMessageMap().putError(KsrConstants.SECURITY_REQUEST_DOC_REQUEST_ROLE + "[" + i + "].active",
     * "error.ksr.securityrequestdocument.qualifier.multi.missing", new String[]{securityRequestRole.getRoleInfo().getName()}); } } else if
     * (securityRequestRole.getRequestRoleQualifications().isEmpty()) { securityRequestRole.getRequestRoleQualifications().add(
     * getSecurityRequestDocumentService().buildRoleQualificationLine(securityRequestRole, Collections.emptyMap())); } }
     * 
     * for (int j = 0; j < securityRequestRole.getRequestRoleQualifications().size(); j++) { SecurityRequestRoleQualification requestRoleQualification =
     * securityRequestRole.getRequestRoleQualifications().get(j);
     * 
     * String typeId = securityRequestRole.getRoleInfo().getKimTypeId(); KimType typeInfo = KimApiServiceLocator.getKimTypeInfoService().getKimType(typeId);
     * 
     * String fieldKeyPrefix = KSRConstants.SECURITY_REQUEST_DOC_REQUEST_ROLE + "[" + i + "]." + KSRConstants.SECURITY_REQUEST_DOC_ROLE_QUAL + "[" + j + "].";
     * success &= validateQualification(typeInfo, requestRoleQualification, fieldKeyPrefix); } }
     * 
     * return super.isDocumentAttributesValid(document, validateRequired) && success; }
     * 
     * protected boolean validateQualification(KimType typeInfo, SecurityRequestRoleQualification requestRoleQualification, String fieldKeyPrefix) { boolean
     * success = true;
     * 
     * KimTypeService kimTypeService = KimFrameworkServiceLocator.getKimTypeService(typeInfo); try { List<RemotableAttributeError> attributeErrors =
     * kimTypeService.validateAttributes(typeInfo.getId(), requestRoleQualification.buildQualificationMap()); if (!attributeErrors.isEmpty()) { success = false;
     * }
     * 
     * // add error to message map for each attribute error for (RemotableAttributeError entry : attributeErrors) { String attributeName =
     * entry.getAttributeName(); int qualificationDetailIndex = findQualificationRecordForAttribute( requestRoleQualification.getRoleQualificationDetails(),
     * attributeName); String fieldKey = fieldKeyPrefix + KsrConstants.SECURITY_REQUEST_DOC_ROLE_QUAL_DETAILS + "[" + qualificationDetailIndex + "]." +
     * KsrConstants.SECURITY_REQUEST_DOC_ROLE_ATTR_VALUE;
     * 
     * String attributeError = entry.getMessage(); String messageKey = StringUtils.substringBefore(attributeError, ":"); String messageParameter =
     * StringUtils.substringAfter(attributeError, ":");
     * 
     * GlobalVariables.getMessageMap().putError(fieldKey, messageKey, messageParameter); } } catch (Exception e) { // success = false; String input =
     * KRADConstants.DOCUMENT_PROPERTY_NAME + "." + KRADConstants.DOCUMENT_HEADER_PROPERTY_NAME;
     * 
     * GlobalVariables.getMessageMap().putInfo(input, KsrConstants.ERROR_SECURITY_REQUEST_DOC_SERVICE_EXCEPTION, new String[]{"SOME FILLER TEXT"}); }
     * 
     * return success; }
     * 
     * protected int findQualificationRecordForAttribute(List<SecurityRequestRoleQualificationDetail> qualificationDetails, String attributeName) { int index =
     * -1;
     * 
     * for (int i = 0; i < qualificationDetails.size(); i++) { SecurityRequestRoleQualificationDetail qualificationDetail = qualificationDetails.get(i); if
     * (StringUtils.equals(qualificationDetail.getAttributeName(), attributeName)) { return i; } }
     * 
     * return index; }
     * 
     *//**
        * Validate that required fields for the db are in the document (principleId)
        * 
        * @see org.kuali.rice.kns.rules.DocumentRuleBase#processCustomSaveDocumentBusinessRules(org.kuali.rice.kns.document.Document)
        */
    /*
     * @Override protected boolean processCustomSaveDocumentBusinessRules(Document document) { boolean success = true; SecurityRequestDocument
     * securityRequestDocument = (SecurityRequestDocument) document;
     * 
     * if (StringUtils.isEmpty(securityRequestDocument.getPrincipalId())) { success = false; String input = KsrConstants.SECURITY_REQUEST_DOC_PRINCIPAL_ID;
     * GlobalVariables.getMessageMap().putErrorForSectionId(input, KsrConstants.ERROR_SECURITY_REQUEST_DOC_PRINCIPAL_ID_MISSING); }
     * 
     * return super.processCustomSaveDocumentBusinessRules(document) && success; }
     * 
     *//**
        * @see org.kuali.rice.ksr.document.validation.AddQualificationRule#processAddRoleQualification(org.kuali.rice.ksr.document.SecurityRequestDocument,
        *      org.kuali.rice.ksr.bo.SecurityRequestRoleQualification)
        */
    /*
     * @Override public boolean processAddRoleQualification(SecurityRequestDocument document, SecurityRequestRoleQualification roleQualification) { if
     * (!roleQualification.getRoleQualificationDetails().isEmpty()) { SecurityRequestRoleQualificationDetail qualificationDetail =
     * roleQualification.getRoleQualificationDetails().get(0);
     * 
     * return validateQualification(qualificationDetail.getKimType(), roleQualification, ""); }
     * 
     * return true; }
     * 
     *//**
        * Cycle through all roles and apply validation: -Checks that roles that all dependent roles for role are selected -Checks that there is at least one
        * change
        * 
        * @see org.kuali.rice.kns.rules.DocumentRuleBase#processCustomRouteDocumentBusinessRules(org.kuali.rice.kns.document.Document)
        */
    /*
     * @Override protected boolean processCustomRouteDocumentBusinessRules(Document document) { boolean success = true; SecurityRequestDocument
     * securityRequestDocument = (SecurityRequestDocument) document;
     * 
     * if (!isDocumentChanged(securityRequestDocument)) { success = false; String input = KRADConstants.DOCUMENT_PROPERTY_NAME + "." +
     * KRADConstants.DOCUMENT_HEADER_PROPERTY_NAME;
     * 
     * GlobalVariables.getMessageMap().putError(input, KsrConstants.ERROR_SECURITY_REQUEST_DOC_CHANGE_MISSING); } else { List<SecurityRequestRole> activeRoles =
     * new ArrayList<SecurityRequestRole>(); Map<String, String> indexes = new HashMap<String, String>(); for (int i = 0; i <
     * securityRequestDocument.getSecurityRequestRoles().size(); i++) { SecurityRequestRole securityRequestRole =
     * securityRequestDocument.getSecurityRequestRoles().get(i); indexes.put(securityRequestRole.getRoleId(), Integer.toString(i)); if
     * (securityRequestRole.isActive()) { activeRoles.add(securityRequestRole); } } for (int i = 0; i < activeRoles.size(); i++) { List<String> rolesNeeded =
     * new ArrayList<String>(); SecurityRequestRole securityRequestRole = activeRoles.get(i); QueryByCriteria criteria = QueryByCriteria.Builder.fromPredicates(
     * PredicateFactory.equal(KsrConstants.PROVISIONING_ROLE_ID, securityRequestRole.getRoleId()), PredicateFactory.equal(KsrConstants.SECURITY_GROUP_ID,
     * securityRequestDocument.getSecurityGroupId()), PredicateFactory.equal(KRADPropertyConstants.ACTIVE, Boolean.TRUE));
     * 
     * SecurityProvisioningGroup securityProvisioningGroup = getDataObjectService() .findUnique(SecurityProvisioningGroup.class, criteria);
     * 
     * if (securityProvisioningGroup != null) { for (int j = 0; j < securityProvisioningGroup.getDependentRoles().size(); j++) {
     * SecurityProvisioningGroupDependentRoles dependentRole = securityProvisioningGroup.getDependentRoles().get(j); boolean found = false; for (int k = 0; k <
     * activeRoles.size(); k++) { if (activeRoles.get(k).getRoleId().equals(dependentRole.getRoleId())) { found = true; } } if (!found) { String index =
     * indexes.get(dependentRole.getRoleId()); String input = KsrConstants.SECURITY_REQUEST_DOC_REQUEST_ROLE + "[" + index + "].active";
     * GlobalVariables.getMessageMap().putError(input, KsrConstants.ERROR_SECURITY_REQUEST_DOC_DEPENDENT_ROLE_MISSING, new String[] {
     * securityRequestRole.getRoleInfo().getName(), dependentRole.getRole().getName() }); rolesNeeded.add(dependentRole.getRole().getName()); success = false; }
     * } }
     * 
     * } }
     * 
     * return super.processCustomRouteDocumentBusinessRules(document) && success; }
     * 
     *//**
        * Find any changes in the document. If no changes, return false;
        * 
        * @param securityRequestDocument
        *            -the submitted document
        * @return
        *//*
           * protected boolean isDocumentChanged(SecurityRequestDocument securityRequestDocument) { boolean success = false;
           * 
           * for (int i = 0; i < securityRequestDocument.getSecurityRequestRoles().size(); i++) { SecurityRequestRole securityRequestRole =
           * securityRequestDocument.getSecurityRequestRoles().get(i); if (securityRequestRole.isCurrentActive() != securityRequestRole.isActive()) { success |=
           * true; } else if (securityRequestRole.isQualifiedRole()) { success |= KsrUtil.isQualificationChangeRequested(securityRequestRole); } }
           * 
           * return success; }
           * 
           * protected ParameterService getParameterService() { return CoreFrameworkServiceLocator.getParameterService(); }
           * 
           * protected KimTypeInfoService getKimTypeInfoService() { return KimApiServiceLocator.getKimTypeInfoService(); }
           * 
           * protected SecurityRequestDocumentService getSecurityRequestDocumentService() { return KSRServiceLocator.getSecurityRequestDocumentService(); }
           * 
           * protected DataObjectService getDataObjectService() { return KRADServiceLocator.getDataObjectService(); }
           */

}
