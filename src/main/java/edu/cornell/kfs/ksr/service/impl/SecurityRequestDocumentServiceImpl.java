package edu.cornell.kfs.ksr.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.core.api.parameter.ParameterEvaluator;
import org.kuali.kfs.core.api.parameter.ParameterEvaluatorService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.api.role.RoleService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.type.KimType;
import org.kuali.kfs.kim.impl.type.KimTypeAttribute;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.KRADConstants;

import edu.cornell.kfs.ksr.KSRConstants;
import edu.cornell.kfs.ksr.KSRPropertyConstants;
import edu.cornell.kfs.ksr.businessobject.SecurityGroup;
import edu.cornell.kfs.ksr.businessobject.SecurityGroupTab;
import edu.cornell.kfs.ksr.businessobject.SecurityProvisioningGroup;
import edu.cornell.kfs.ksr.businessobject.SecurityRequestRole;
import edu.cornell.kfs.ksr.businessobject.SecurityRequestRoleQualification;
import edu.cornell.kfs.ksr.businessobject.SecurityRequestRoleQualificationDetail;
import edu.cornell.kfs.ksr.document.SecurityRequestDocument;
import edu.cornell.kfs.ksr.service.SecurityRequestDocumentService;
import edu.cornell.kfs.ksr.util.KSRUtil;

public class SecurityRequestDocumentServiceImpl implements SecurityRequestDocumentService {
    private static final Logger LOG = LogManager.getLogger();

    private BusinessObjectService businessObjectService;
    private PersonService personService;
    private ParameterService parameterService;

    public List<SecurityGroup> getActiveSecurityGroups() {
        Map<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put(KSRPropertyConstants.SECURITY_GROUP_ACTIVE_INDICATOR, true);

        List<SecurityGroup> groupList = (List<SecurityGroup>) businessObjectService.findMatchingOrderBy(SecurityGroup.class, hashMap,
                KSRPropertyConstants.SECURITY_GROUP_NAME, true);

        return groupList;
    }
    
    public void prepareSecurityRequestDocument(SecurityRequestDocument document) {
        if (document.getDocumentHeader().getWorkflowDocument().isSaved()) {
            for (SecurityRequestRole requestRole : document.getSecurityRequestRoles()) {
                SecurityRequestRoleQualification newRequestRoleQualification = buildRoleQualificationLine(requestRole, null);
                requestRole.setNewRequestRoleQualification(newRequestRoleQualification);
            }
        }
    }
    
    public SecurityRequestRoleQualification buildRoleQualificationLine(SecurityRequestRole requestRole, Map<String,String> qualification) {
        SecurityRequestRoleQualification requestQualification = new SecurityRequestRoleQualification();

        requestQualification.setDocumentNumber(requestRole.getDocumentNumber());
        requestQualification.setRoleRequestId(requestRole.getRoleRequestId());

        KimType typeInfo = KSRUtil.getTypeInfoForRoleRequest(requestRole);

        List<KimTypeAttribute> typeAttributes = KSRUtil.getTypeAttributesForRoleRequest(requestRole);
        for (KimTypeAttribute attributeInfo : typeAttributes) {
            SecurityRequestRoleQualificationDetail requestQualificationDetail = new SecurityRequestRoleQualificationDetail();

            requestQualificationDetail.setDocumentNumber(requestRole.getDocumentNumber());
            requestQualificationDetail.setRoleRequestId(requestRole.getRoleRequestId());
            requestQualificationDetail.setQualificationId(requestRole.getNextQualificationId());
            requestQualificationDetail.setAttributeId(attributeInfo.getKimAttribute().getId());
            requestQualificationDetail.setRoleTypeId(typeInfo.getId());

            if ((qualification != null)
                    && qualification.containsKey(attributeInfo.getKimAttribute().getAttributeName())) {
                requestQualificationDetail.setAttributeValue(qualification.get(attributeInfo.getKimAttribute().getAttributeName()));
            }

            requestQualification.getRoleQualificationDetails().add(requestQualificationDetail);
        }

        requestQualification.setQualificationId(requestRole.getNextQualificationId());
        requestRole.setNextQualificationId(requestRole.getNextQualificationId() + 1);

        return requestQualification;
    }
    
    public void initiateSecurityRequestDocument(SecurityRequestDocument document, Person user) {
        LOG.info("initiateSecurityRequestDocument() Preparing security request document: "
                + document.getDocumentNumber());

        String principalId = document.getPrincipalId();

        if (StringUtils.isBlank(principalId)) {
            throw new RuntimeException("Principal id not set for new Security Request Document");
        }
        else if (document.getSecurityGroupId() == null) {
            throw new RuntimeException("Security group id not set for new Security Request Document");
        }

        Person person = personService.getPerson(principalId);
        if (person != null) {
            document.setRequestPerson(person);
            document.setPrimaryDepartmentCode(person.getPrimaryDepartmentCode());
        }
        else {
            LOG.error("initiateSecurityRequestDocument() Unable to find person record for principal id: "
                    + principalId);
            throw new RuntimeException("Error preparing document: Unable to find person record for principal id: "
                    + principalId);
        }

        buildSecurityRequestRoles(document);
    }
    
    protected void buildSecurityRequestRoles(SecurityRequestDocument document) {
        List<SecurityRequestRole> requestRoles = new ArrayList<SecurityRequestRole>();

        long roleRequestId = 1;

        for (SecurityGroupTab groupTab : document.getSecurityGroup().getSecurityGroupTabs()) {
            if (groupTab.isActive()) {
                for (SecurityProvisioningGroup provisioningGroup : groupTab.getSecurityProvisioningGroups()) {
                    if (provisioningGroup.isActive()) {
                        SecurityRequestRole requestRole = new SecurityRequestRole();

                        requestRole.setDocumentNumber(document.getDocumentNumber());
                        requestRole.setRoleId(provisioningGroup.getRoleId());
                        requestRole.setRoleRequestId(Long.valueOf(roleRequestId));
                        requestRole.setAllowKSRToManageQualifications(!isKSRAllowedToEditRoleQualifiersForRole(provisioningGroup.getRoleId()));

                        buildSecurityRequestRoleQualifications(requestRole, document.getPrincipalId());

                        roleRequestId++;
                        requestRoles.add(requestRole);
                    }
                }
            }
        }

        document.setSecurityRequestRoles(requestRoles);
    }
    
    protected boolean isKSRAllowedToEditRoleQualifiersForRole(String roleId) {
        Collection<String> roleIdsPreventQualificationEdit = parameterService.getParameterValuesAsString(KSRConstants.KSR_NAMESPACE,
                KRADConstants.DetailTypes.DOCUMENT_DETAIL_TYPE,
                KSRConstants.NO_QUALIFIFIER_EDIT_ROLES_ON_KSR_PARAMETER);
        
        if (roleIdsPreventQualificationEdit.contains(roleId)) {
            LOG.debug("isKSRAllowedToEditRoleQualifiersForRole, role ID {} is in parameter returning true", roleId);
            return true;
        } else {
            LOG.debug("isKSRAllowedToEditRoleQualifiersForRole, role ID {} is NOT in parameter returning false", roleId);
            return false;
        }
    }
    
    protected void buildSecurityRequestRoleQualifications(SecurityRequestRole requestRole, String principalId) {
        List<String> roleIds = new ArrayList<String>();
        roleIds.add(requestRole.getRoleId());

        RoleService roleService = KimApiServiceLocator.getRoleService();

        boolean hasRole = roleService.principalHasRole(principalId, roleIds, Collections.emptyMap());
        if (hasRole) {
            requestRole.setActive(true);
            requestRole.setCurrentActive(true);
            requestRole.setCurrentQualifications("");

            if (requestRole.isQualifiedRole()) {
                List<Map<String,String>> principalQualifications = roleService.getNestedRoleQualifiersForPrincipalByRoleIds(principalId, roleIds, Collections.emptyMap());

                List<SecurityRequestRoleQualification> requestQualifications = new ArrayList<SecurityRequestRoleQualification>();
                for (Map<String,String> qualification : principalQualifications) {
                    SecurityRequestRoleQualification requestQualification = buildRoleQualificationLine(requestRole, qualification);

                    requestQualifications.add(requestQualification);
                }

                requestRole.setRequestRoleQualifications(requestQualifications);
                buildCurrentQualificationsString(requestRole, principalId, principalQualifications);
            }
        }
        else {
            requestRole.setActive(false);
            requestRole.setCurrentActive(false);
        }

        if (requestRole.isQualifiedRole()) {
            SecurityRequestRoleQualification newRequestRoleQualification = buildRoleQualificationLine(requestRole, null);
            requestRole.setNewRequestRoleQualification(newRequestRoleQualification);
        }
    }
    
    protected void buildCurrentQualificationsString(SecurityRequestRole requestRole, String principalId, List<Map<String,String>> principalQualifications) {
        List<KimTypeAttribute> typeAttributes = KSRUtil.getTypeAttributesForRoleRequest(requestRole);

        List<String> roleIds = new ArrayList<String>();
        roleIds.add(requestRole.getRoleId());

        if (principalQualifications == null) {
            principalQualifications = KimApiServiceLocator.getRoleService().getNestedRoleQualifiersForPrincipalByRoleIds(
                    principalId, roleIds, Collections.emptyMap());
        }

        String currentQualificationsString = KSRUtil.buildQualificationString(principalQualifications, typeAttributes);
        requestRole.setCurrentQualifications(currentQualificationsString);
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

}
