/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.kim.document;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kew.framework.postprocessor.DocumentRouteStatusChange;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.bo.ui.KimDocumentRoleMember;
import org.kuali.kfs.kim.bo.ui.KimDocumentRoleQualifier;
import org.kuali.kfs.kim.bo.ui.KimDocumentRoleResponsibilityAction;
import org.kuali.kfs.kim.bo.ui.PersonDocumentGroup;
import org.kuali.kfs.kim.bo.ui.PersonDocumentRole;
import org.kuali.kfs.kim.bo.ui.RoleDocumentDelegation;
import org.kuali.kfs.kim.bo.ui.RoleDocumentDelegationMember;
import org.kuali.kfs.kim.bo.ui.RoleDocumentDelegationMemberQualifier;
import org.kuali.kfs.kim.service.KIMServiceLocatorInternal;
import org.kuali.kfs.kim.service.UiDocumentService;
import org.kuali.kfs.kns.service.DocumentHelperService;
import org.kuali.kfs.kns.service.KNSServiceLocator;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.service.SequenceAccessorService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.context.SpringContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * CU Customization: Added CU-specific Person Document fields.
 *                   Also backported the FINP-9357 changes.
 */
public class IdentityManagementPersonDocument extends IdentityManagementKimDocument {

    private static final long serialVersionUID = -534993712085516925L;

    // principal data
    protected String principalId;

    protected String principalName;

    protected String entityId;

    private String affiliationTypeCode = "";
    protected String campusCode = "";

    // employment information
    private String employeeId = "";
    private String employeeStatusCode = "";
    private String employeeTypeCode = "";
    private String primaryDepartmentCode = "";
    private KualiDecimal baseSalaryAmount = KualiDecimal.ZERO;

    protected boolean active;

    private String firstName = "";
    private String middleName = "";
    private String lastName = "";
    private String name = "";

    private String addressTypeCode;
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String addressCity;
    private String addressStateProvinceCode;
    private String addressPostalCode;
    private String addressCountryCode;

    private String phoneNumber = "";
    private String emailAddress = "";

    // ==== CU Customization: Add more fields to the Person Document. ====
    private String academicAffiliation;
    private String affiliateAffiliation;
    private String alumniAffiliation;
    private String exceptionAffiliation;
    private String facultyAffiliation;
    private String staffAffiliation;
    private String studentAffiliation;
    private String altAddressTypeCode;
    private String altAddressLine1;
    private String altAddressLine2;
    private String altAddressLine3;
    private String altAddressCity;
    private String altAddressStateProvinceCode;
    private String altAddressPostalCode;
    private String altAddressCountryCode;
    private boolean suppressName;
    private boolean suppressEmail;
    private boolean suppressPhone;
    private boolean suppressPersonal;
    // ==== End CU-specific field additions ====

    protected List<PersonDocumentGroup> groups;

    protected List<PersonDocumentRole> roles;

    protected transient DocumentHelperService documentHelperService;
    private transient UiDocumentService uiDocumentService;

    public IdentityManagementPersonDocument() {
        groups = new ArrayList<>();
        roles = new ArrayList<>();
        active = true;
    }

    public String getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(final String principalId) {
        this.principalId = principalId;
    }

    public String getPrincipalName() {
        return principalName;
    }

    /*
     * sets the principal name.
     * Principal names are converted to lower case.
     */
    public void setPrincipalName(final String principalName) {
        this.principalName = principalName;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(final String entityId) {
        this.entityId = entityId;
    }

    public String getAffiliationTypeCode() {
        return affiliationTypeCode;
    }

    public void setAffiliationTypeCode(final String affiliationTypeCode) {
        this.affiliationTypeCode = affiliationTypeCode;
    }

    public String getCampusCode() {
        return campusCode;
    }

    public void setCampusCode(final String campusCode) {
        this.campusCode = campusCode;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(final String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeStatusCode() {
        return employeeStatusCode;
    }

    public void setEmployeeStatusCode(final String employeeStatusCode) {
        this.employeeStatusCode = employeeStatusCode;
    }

    public String getEmployeeTypeCode() {
        return employeeTypeCode;
    }

    public void setEmployeeTypeCode(final String employeeTypeCode) {
        this.employeeTypeCode = employeeTypeCode;
    }

    public String getPrimaryDepartmentCode() {
        return primaryDepartmentCode;
    }

    public void setPrimaryDepartmentCode(final String primaryDepartmentCode) {
        this.primaryDepartmentCode = primaryDepartmentCode;
    }

    public KualiDecimal getBaseSalaryAmount() {
        return baseSalaryAmount;
    }

    public void setBaseSalaryAmount(final KualiDecimal baseSalaryAmount) {
        this.baseSalaryAmount = baseSalaryAmount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(final String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    // ==== CU Customization: Added methods related to masking the person's name. ====

    private boolean canViewName() {
        return !suppressName || canModifyPerson();
    }

    private boolean canModifyPerson() {
        return getUiDocumentService()
                .canModifyPerson(GlobalVariables.getUserSession().getPrincipalId(), principalId);
    }

    public String getFirstNameMaskedIfNecessary() {
        return canViewName() ? firstName : KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    public String getMiddleNameMaskedIfNecessary() {
        return canViewName() ? middleName : KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    public String getLastNameMaskedIfNecessary() {
        return canViewName() ? lastName : KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    public String getNameMaskedIfNecessary() {
        return canViewName() ? name : KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    // ==== End CU Customization ====

    public String getAddressTypeCode() {
        return addressTypeCode;
    }

    public void setAddressTypeCode(final String addressTypeCode) {
        this.addressTypeCode = addressTypeCode;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    // used by personAddress.tag
    public String getAddressLine1MaskedIfNecessary() {
        if (canViewAddress()) {
            return addressLine1;
        }
        return KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    private boolean canViewAddress() {
        return StringUtils.equals(addressTypeCode, KimConstants.AddressTypes.WORK)
               || getUiDocumentService()
                       .canModifyPerson(GlobalVariables.getUserSession().getPrincipalId(), principalId);
    }

    public void setAddressLine1(final String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    // used by personAddress.tag
    public String getAddressLine2MaskedIfNecessary() {
        if (canViewAddress()) {
            return addressLine2;
        }
        return KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    public void setAddressLine2(final String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getAddressLine3() {
        return addressLine3;
    }

    // used by personAddress.tag
    public String getAddressLine3MaskedIfNecessary() {
        if (canViewAddress()) {
            return addressLine3;
        }
        return KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    public void setAddressLine3(final String addressLine3) {
        this.addressLine3 = addressLine3;
    }

    public String getAddressCity() {
        return addressCity;
    }

    // used by personAddress.tag
    public String getAddressCityMaskedIfNecessary() {
        if (canViewAddress()) {
            return addressCity;
        }
        return KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    public void setAddressCity(final String addressCity) {
        this.addressCity = addressCity;
    }

    public String getAddressStateProvinceCode() {
        return addressStateProvinceCode;
    }

    // used by personAddress.tag
    public String getAddressStateProvinceCodeMaskedIfNecessary() {
        if (canViewAddress()) {
            return addressStateProvinceCode;
        }
        return KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    public void setAddressStateProvinceCode(final String addressStateProvinceCode) {
        this.addressStateProvinceCode = addressStateProvinceCode;
    }

    public String getAddressPostalCode() {
        return addressPostalCode;
    }

    // used by personAddress.tag
    public String getAddressPostalCodeMaskedIfNecessary() {
        if (canViewAddress()) {
            return addressPostalCode;
        }
        return KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK_ZIP;
    }

    public void setAddressPostalCode(final String addressPostalCode) {
        this.addressPostalCode = addressPostalCode;
    }

    public String getAddressCountryCode() {
        return addressCountryCode;
    }

    // used by personAddress.tag
    public String getAddressCountryCodeMaskedIfNecessary() {
        if (canViewAddress()) {
            return addressCountryCode;
        }
        return KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    public void setAddressCountryCode(final String addressCountryCode) {
        this.addressCountryCode = addressCountryCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // ==== CU Customization: Added methods related to masking the person's phone number. ====

    public boolean canViewPhoneNumber() {
        return !suppressPhone || canModifyPerson();
    }

    public String getPhoneNumberMaskedIfNecessary() {
        return canViewPhoneNumber() ? phoneNumber : KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    // ==== End CU Customization ====

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(final String emailAddress) {
        this.emailAddress = emailAddress;
    }

    // ==== CU Customization: Added methods related to masking the person's email address. ====

    private boolean canViewEmailAddress() {
        return !suppressEmail || canModifyPerson();
    }

    public String getEmailAddressMaskedIfNecessary() {
        return canViewEmailAddress() ? emailAddress : KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    // ==== End CU Customization ====

    public List<PersonDocumentRole> getRoles() {
        return roles;
    }

    public void setRoles(final List<PersonDocumentRole> roles) {
        this.roles = roles;
    }

    public List<PersonDocumentGroup> getGroups() {
        return groups;
    }

    public void setGroups(final List<PersonDocumentGroup> groups) {
        this.groups = groups;
    }

    // ==== CU Customization: Add getters and setters for CU-specific fields. ====

    public String getAcademicAffiliation() {
        return academicAffiliation;
    }

    public void setAcademicAffiliation(String academicAffiliation) {
        this.academicAffiliation = academicAffiliation;
    }

    public String getAffiliateAffiliation() {
        return affiliateAffiliation;
    }

    public void setAffiliateAffiliation(String affiliateAffiliation) {
        this.affiliateAffiliation = affiliateAffiliation;
    }

    public String getAlumniAffiliation() {
        return alumniAffiliation;
    }

    public void setAlumniAffiliation(String alumniAffiliation) {
        this.alumniAffiliation = alumniAffiliation;
    }

    public String getExceptionAffiliation() {
        return exceptionAffiliation;
    }

    public void setExceptionAffiliation(String exceptionAffiliation) {
        this.exceptionAffiliation = exceptionAffiliation;
    }

    public String getFacultyAffiliation() {
        return facultyAffiliation;
    }

    public void setFacultyAffiliation(String facultyAffiliation) {
        this.facultyAffiliation = facultyAffiliation;
    }

    public String getStaffAffiliation() {
        return staffAffiliation;
    }

    public void setStaffAffiliation(String staffAffiliation) {
        this.staffAffiliation = staffAffiliation;
    }

    public String getStudentAffiliation() {
        return studentAffiliation;
    }

    public void setStudentAffiliation(String studentAffiliation) {
        this.studentAffiliation = studentAffiliation;
    }

    public String getAltAddressTypeCode() {
        return altAddressTypeCode;
    }

    public void setAltAddressTypeCode(String altAddressTypeCode) {
        this.altAddressTypeCode = altAddressTypeCode;
    }

    public String getAltAddressLine1() {
        return altAddressLine1;
    }

    public String getAltAddressLine1MaskedIfNecessary() {
        return canViewAltAddress() ? altAddressLine1 : KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    private boolean canViewAltAddress() {
        return StringUtils.equals(altAddressTypeCode, KimConstants.AddressTypes.WORK)
                || canModifyPerson();
    }

    public void setAltAddressLine1(String altAddressLine1) {
        this.altAddressLine1 = altAddressLine1;
    }

    public String getAltAddressLine2() {
        return altAddressLine2;
    }

    public String getAltAddressLine2MaskedIfNecessary() {
        return canViewAltAddress() ? altAddressLine2 : KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    public void setAltAddressLine2(String altAddressLine2) {
        this.altAddressLine2 = altAddressLine2;
    }

    public String getAltAddressLine3() {
        return altAddressLine3;
    }

    public String getAltAddressLine3MaskedIfNecessary() {
        return canViewAltAddress() ? altAddressLine3 : KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    public void setAltAddressLine3(String altAddressLine3) {
        this.altAddressLine3 = altAddressLine3;
    }

    public String getAltAddressCity() {
        return altAddressCity;
    }

    public String getAltAddressCityMaskedIfNecessary() {
        return canViewAltAddress() ? altAddressCity : KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    public void setAltAddressCity(String altAddressCity) {
        this.altAddressCity = altAddressCity;
    }

    public String getAltAddressStateProvinceCode() {
        return altAddressStateProvinceCode;
    }

    public String getAltAddressStateProvinceCodeMaskedIfNecessary() {
        return canViewAltAddress() ? altAddressStateProvinceCode : KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    public void setAltAddressStateProvinceCode(String altAddressStateProvinceCode) {
        this.altAddressStateProvinceCode = altAddressStateProvinceCode;
    }

    public String getAltAddressPostalCode() {
        return altAddressPostalCode;
    }

    public String getAltAddressPostalCodeMaskedIfNecessary() {
        return canViewAltAddress() ? altAddressPostalCode : KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    public void setAltAddressPostalCode(String altAddressPostalCode) {
        this.altAddressPostalCode = altAddressPostalCode;
    }

    public String getAltAddressCountryCode() {
        return altAddressCountryCode;
    }

    public String getAltAddressCountryCodeMaskedIfNecessary() {
        return canViewAltAddress() ? altAddressCountryCode : KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    public void setAltAddressCountryCode(String altAddressCountryCode) {
        this.altAddressCountryCode = altAddressCountryCode;
    }

    public boolean isSuppressName() {
        return suppressName;
    }

    public void setSuppressName(boolean suppressName) {
        this.suppressName = suppressName;
    }

    public boolean isSuppressEmail() {
        return suppressEmail;
    }

    public void setSuppressEmail(boolean suppressEmail) {
        this.suppressEmail = suppressEmail;
    }

    public boolean isSuppressPhone() {
        return suppressPhone;
    }

    public void setSuppressPhone(boolean suppressPhone) {
        this.suppressPhone = suppressPhone;
    }

    public boolean isSuppressPersonal() {
        return suppressPersonal;
    }

    public void setSuppressPersonal(boolean suppressPersonal) {
        this.suppressPersonal = suppressPersonal;
    }

    // ==== End CU-specific getters and setters ====

    public void initializeDocumentForNewPerson() {
        if (StringUtils.isBlank(principalId)) {
            principalId = getSequenceAccessorService()
                    .getNextAvailableSequenceNumber(KimConstants.SequenceNames.KRIM_PRNCPL_ID_S,
                            getClass()).toString();
        }
        if (StringUtils.isBlank(entityId)) {
            entityId = getSequenceAccessorService()
                    .getNextAvailableSequenceNumber(KimConstants.SequenceNames.KRIM_ENTITY_ID_S,
                            getClass()).toString();
        }
    }

    @Override
    public List<Collection<PersistableBusinessObject>> buildListOfDeletionAwareLists() {
        final List<Collection<PersistableBusinessObject>> managedLists = super.buildListOfDeletionAwareLists();
        managedLists.add(new ArrayList<>(groups));
        managedLists.add(new ArrayList<>(roles));
        return managedLists;
    }

    @Override
    public void doRouteStatusChange(final DocumentRouteStatusChange statusChangeEvent) {
        super.doRouteStatusChange(statusChangeEvent);
        if (getDocumentHeader().getWorkflowDocument().isProcessed()) {
            setIfGroupsEditable();
            setIfRolesEditable();
            KIMServiceLocatorInternal.getUiDocumentService().savePerson(this);
        }
    }

    @Override
    public void prepareForSave() {
        super.prepareForSave();

        for (final PersonDocumentRole role : roles) {
            role.setDocumentNumber(getDocumentNumber());
            for (final KimDocumentRoleMember rolePrncpl : role.getRolePrncpls()) {
                rolePrncpl.setDocumentNumber(getDocumentNumber());
                rolePrncpl.setRoleId(role.getRoleId());
                if (StringUtils.isEmpty(rolePrncpl.getRoleMemberId())) {
                    final SequenceAccessorService sas = getSequenceAccessorService();
                    final Long nextSeq = sas.getNextAvailableSequenceNumber(
                            KimConstants.SequenceNames.KRIM_ROLE_MBR_ID_S,
                            getClass()
                    );
                    final String roleMemberId = nextSeq.toString();
                    rolePrncpl.setRoleMemberId(roleMemberId);
                }
                for (final KimDocumentRoleQualifier qualifier : rolePrncpl.getQualifiers()) {
                    qualifier.setDocumentNumber(getDocumentNumber());
                    qualifier.setRoleMemberId(rolePrncpl.getRoleMemberId());
                    qualifier.setKimTypId(role.getKimTypeId());
                }
                for (final KimDocumentRoleResponsibilityAction responsibilityAction : rolePrncpl.getRoleRspActions()) {
                    responsibilityAction.setDocumentNumber(getDocumentNumber());
                    responsibilityAction.setRoleMemberId(rolePrncpl.getRoleMemberId());
                    responsibilityAction.setRoleResponsibilityId("*");
                }
            }
        }
        if (getDelegationMembers() != null) {
            for (final RoleDocumentDelegationMember delegationMember : getDelegationMembers()) {
                delegationMember.setDocumentNumber(getDocumentNumber());
                for (final RoleDocumentDelegationMemberQualifier qualifier : delegationMember.getQualifiers()) {
                    qualifier.setDocumentNumber(getDocumentNumber());
                    qualifier.setKimTypId(delegationMember.getMemberRole().getKimTypeId());
                }
                addDelegationMemberToDelegation(delegationMember);
            }
        }
        // important to do this after getDelegationMembers since the addDelegationMemberToDelegation method will create
        // primary and/or secondary delegations for us in a "just-in-time" fashion
        if (getDelegations() != null) {
            final List<RoleDocumentDelegation> emptyDelegations = new ArrayList<>();
            for (final RoleDocumentDelegation delegation : getDelegations()) {
                delegation.setDocumentNumber(getDocumentNumber());
                if (delegation.getMembers().isEmpty()) {
                    emptyDelegations.add(delegation);
                }
            }
            // remove any empty delegations because we just don't need them
            getDelegations().removeAll(emptyDelegations);
        }
        if (groups != null) {
            String groupMemberId;
            for (final PersonDocumentGroup group : groups) {
                group.setDocumentNumber(getDocumentNumber());
                if (StringUtils.isEmpty(group.getGroupMemberId())) {
                    final SequenceAccessorService sas = getSequenceAccessorService();
                    final Long nextSeq = sas.getNextAvailableSequenceNumber(
                            KimConstants.SequenceNames.KRIM_GRP_MBR_ID_S,
                            getClass()
                    );
                    groupMemberId = nextSeq.toString();
                    group.setGroupMemberId(groupMemberId);
                }
            }
        }
    }

    public void setIfGroupsEditable() {
        if (CollectionUtils.isNotEmpty(groups)) {
            for (final PersonDocumentGroup group : groups) {
                group.setEditable(validPopulateGroup(group));
            }
        }
    }

    private boolean validPopulateGroup(final PersonDocumentGroup group) {
        if (StringUtils.isNotEmpty(group.getNamespaceCode())) {
            final Map<String, String> roleDetails = new HashMap<>();
            roleDetails.put(KimConstants.AttributeConstants.NAMESPACE_CODE, group.getNamespaceCode());
            roleDetails.put(KimConstants.AttributeConstants.GROUP_NAME, group.getGroupName());
            return getDocumentHelperService().getDocumentAuthorizer(this).isAuthorizedByTemplate(this,
                    KimConstants.NAMESPACE_CODE, KimConstants.PermissionTemplateNames.POPULATE_GROUP,
                    GlobalVariables.getUserSession().getPerson().getPrincipalId(), roleDetails, null);
        }

        return true;
    }

    public void setIfRolesEditable() {
        if (CollectionUtils.isNotEmpty(roles)) {
            for (final PersonDocumentRole role : roles) {
                role.setEditable(validAssignRole(role));
            }
        }
    }

    public boolean validAssignRole(final PersonDocumentRole role) {
        boolean rulePassed = true;
        if (StringUtils.isNotEmpty(role.getNamespaceCode())) {
            final Map<String, String> additionalPermissionDetails = new HashMap<>();
            additionalPermissionDetails.put(KimConstants.AttributeConstants.NAMESPACE_CODE, role.getNamespaceCode());
            additionalPermissionDetails.put(KimConstants.AttributeConstants.ROLE_NAME, role.getRoleName());
            if (!getDocumentHelperService().getDocumentAuthorizer(this).isAuthorizedByTemplate(this,
                    KimConstants.NAMESPACE_CODE, KimConstants.PermissionTemplateNames.ASSIGN_ROLE,
                    GlobalVariables.getUserSession().getPrincipalId(), additionalPermissionDetails, null)) {
                rulePassed = false;
            }
        }
        return rulePassed;
    }

    protected DocumentHelperService getDocumentHelperService() {
        if (documentHelperService == null) {
            documentHelperService = KNSServiceLocator.getDocumentHelperService();
        }
        return documentHelperService;
    }

    private UiDocumentService getUiDocumentService() {
        if (uiDocumentService == null) {
            uiDocumentService = SpringContext.getBean(UiDocumentService.class);
        }
        return uiDocumentService;
    }

}
