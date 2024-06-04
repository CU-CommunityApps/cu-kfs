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
package org.kuali.kfs.kim.impl.identity;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.membership.MemberType;
import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.kim.impl.common.delegate.DelegateMember;
import org.kuali.kfs.kim.impl.common.delegate.DelegateType;
import org.kuali.kfs.kim.impl.group.GroupMember;
import org.kuali.kfs.kim.impl.role.RoleMember;
import org.kuali.kfs.kim.service.UiDocumentService;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.kim.impl.identity.PersonExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * CU Customizations:
 * 
 * -- Reintroduced masking of name/phone/etc. based on the user's privacy preferences.
 * -- Backported the FINP-9360 changes into this file.
 */
public class Person extends PersistableBusinessObjectBase implements MutableInactivatable {

    public static final String CACHE_NAME = "Person";
    private static final long serialVersionUID = 1L;

    private String principalId;
    private String principalName;
    private String entityId;
    private String entityTypeCode;
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
    private String emailAddress = "";
    private String phoneNumber = "";
    private String affiliationTypeCode = "";
    private String campusCode = "";
    private String taxId;
    private String employeeStatusCode = "";
    private String employeeTypeCode = "";
    private String primaryDepartmentCode = "";
    private String employeeId = "";
    private KualiDecimal baseSalaryAmount = KualiDecimal.ZERO;
    private boolean active = true;
    private String lookupRoleNamespaceCode;
    private String lookupRoleName;
    private List<GroupMember> groupMembers;
    private List<RoleMember> roleMembers;
    private List<DelegateMember> delegateMembers;

    private static BusinessObjectService businessObjectService;
    private static PersonService personService;
    private static UiDocumentService uiDocumentService;

    public String getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(final String principalId) {
        this.principalId = principalId;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(final String principalName) {
        this.principalName = principalName;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(final String entityId) {
        this.entityId = entityId;
    }

    public String getEntityTypeCode() {
        return entityTypeCode;
    }

    public void setEntityTypeCode(final String entityTypeCode) {
        this.entityTypeCode = entityTypeCode;
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
        if (StringUtils.isNotBlank(name)) {
            return name;
        } else {
            if (StringUtils.isNotBlank(lastName)) {
                return lastName + ", " + firstName + (StringUtils.isBlank(middleName) ? "" : " " + middleName);
            } else {
                return firstName + (StringUtils.isBlank(middleName) ? "" : " " + middleName);
            }
        }
    }

    public void setName(final String name) {
        this.name = name;
    }

    // ==== CU Customization: Added methods related to masking the person's name. ====

    public PersonExtension getPersonExtension() {
        return (PersonExtension) getExtension();
    }

    private boolean canViewName() {
        return !getPersonExtension().isSuppressName();
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
        return canViewName() ? getName() : KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    public void setNameMaskedIfNecessary(String nameMaskedIfNecessary) {
        // Do nothing; this method is only present for UI convenience.
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

    // used by Person Inquiry
    public String getAddressLine1MaskedIfNecessary() {
        if (canViewAddress()) {
            return addressLine1;
        }
        return KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    private boolean canViewAddress() {
        if (StringUtils.equals(addressTypeCode, KimConstants.AddressTypes.WORK)) {
            return true;
        }

        final UserSession userSession = GlobalVariables.getUserSession();
        if (userSession == null) {
            // internal system call - no need to check permission
            return true;
        }

        // ==== Start FINP-9360 Backport ====
        
        final String currentUserPrincipalId = userSession.getPrincipalId();
        return StringUtils.equals(currentUserPrincipalId, principalId) ||
               getUiDocumentService().canModifyPerson(currentUserPrincipalId, principalId);
        
        // ==== End FINP-9360 Backport ====
    }

    public void setAddressLine1(final String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    // used by Person Inquiry
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

    // used by Person Inquiry
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

    // used by Person Inquiry
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

    // used by Person Inquiry
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

    // used by Person Inquiry
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

    // used by Person Inquiry
    public String getAddressCountryCodeMaskedIfNecessary() {
        if (canViewAddress()) {
            return addressCountryCode;
        }
        return KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    public void setAddressCountryCode(final String addressCountryCode) {
        this.addressCountryCode = addressCountryCode;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(final String emailAddress) {
        this.emailAddress = emailAddress;
    }

    // ==== CU Customization: Added methods related to masking the person's email address. ====

    private boolean canViewEmailAddress() {
        return !getPersonExtension().isSuppressEmail();
    }

    public String getEmailAddressMaskedIfNecessary() {
        return canViewEmailAddress() ? emailAddress : KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    // ==== End CU Customization ====

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // ==== CU Customization: Added methods related to masking the person's phone number. ====

    private boolean canViewPhoneNumber() {
        return !getPersonExtension().isSuppressPhone();
    }

    public String getPhoneNumberMaskedIfNecessary() {
        return canViewPhoneNumber() ? phoneNumber : KimConstants.RestrictedMasks.RESTRICTED_DATA_MASK;
    }

    // ==== End CU Customization ====

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

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(final String taxId) {
        this.taxId = taxId;
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

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(final String employeeId) {
        this.employeeId = employeeId;
    }

    public KualiDecimal getBaseSalaryAmount() {
        return baseSalaryAmount;
    }

    public void setBaseSalaryAmount(final KualiDecimal baseSalaryAmount) {
        this.baseSalaryAmount = baseSalaryAmount;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(final boolean active) {
        this.active = active;
    }

    public String getLookupRoleNamespaceCode() {
        return lookupRoleNamespaceCode;
    }

    public void setLookupRoleNamespaceCode(final String lookupRoleNamespaceCode) {
        this.lookupRoleNamespaceCode = lookupRoleNamespaceCode;
    }

    public String getLookupRoleName() {
        return lookupRoleName;
    }

    public void setLookupRoleName(final String lookupRoleName) {
        this.lookupRoleName = lookupRoleName;
    }

    public List<GroupMember> getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(final List<GroupMember> groupMembers) {
        this.groupMembers = groupMembers;
    }

    public List<RoleMember> getRoleMembers() {
        return roleMembers;
    }

    public void setRoleMembers(final List<RoleMember> roleMembers) {
        this.roleMembers = roleMembers;
    }

    public List<DelegateMember> getDelegateMembers() {
        return delegateMembers;
    }

    public void setDelegateMembers(final List<DelegateMember> delegateMembers) {
        this.delegateMembers = delegateMembers;
    }

    public void populateMembers() {
        populateGroupMembers();
        populateRoleMembers();
        populateDelegateMembers();
    }

    private void populateGroupMembers() {
        final Map<String, String> criteria = new HashMap<>(2);
        criteria.put(KIMPropertyConstants.GroupMember.MEMBER_ID, principalId);
        criteria.put(KIMPropertyConstants.KimMember.MEMBER_TYPE_CODE,
                KimConstants.KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE.getCode());
        groupMembers = (List<GroupMember>) getBusinessObjectService().findMatching(GroupMember.class, criteria);
    }

    private void populateRoleMembers() {
        final Map<String, String> criteria = new HashMap<>(2);
        criteria.put(KIMPropertyConstants.RoleMember.MEMBER_ID, principalId);
        criteria.put(KIMPropertyConstants.RoleMember.MEMBER_TYPE_CODE,
                KimConstants.KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE.getCode());
        roleMembers = (List<RoleMember>) getBusinessObjectService().findMatching(RoleMember.class, criteria);
    }

    private void populateDelegateMembers() {
        final Map<String, String> criteria = new HashMap<>(2);
        criteria.put(KIMPropertyConstants.DelegationMember.MEMBER_ID, principalId);
        criteria.put(KIMPropertyConstants.DelegationMember.MEMBER_TYPE_CODE, MemberType.PRINCIPAL.getCode());
        delegateMembers = (List<DelegateMember>) getBusinessObjectService().findMatching(DelegateMember.class,
                criteria);
        criteria.clear();
        for (final DelegateMember dmb: delegateMembers) {
            criteria.put(KIMPropertyConstants.Delegation.DELEGATION_ID, dmb.getDelegationId());
            final DelegateType delegate = getBusinessObjectService().findByPrimaryKey(DelegateType.class, criteria);
            dmb.setDelegationType(delegate.getDelegationTypeCode());
        }
    }

    private static BusinessObjectService getBusinessObjectService() {
        if (businessObjectService == null) {
            businessObjectService = SpringContext.getBean(BusinessObjectService.class);
        }
        return businessObjectService;
    }

    public static PersonService getPersonService() {
        if (personService == null) {
            personService = KimApiServiceLocator.getPersonService();
        }
        return personService;
    }

    private UiDocumentService getUiDocumentService() {
        if (uiDocumentService == null) {
            uiDocumentService = SpringContext.getBean(UiDocumentService.class);
        }
        return uiDocumentService;
    }

}
