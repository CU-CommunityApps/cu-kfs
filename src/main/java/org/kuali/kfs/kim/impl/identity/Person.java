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
import org.kuali.kfs.kim.api.identity.IdentityService;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.kim.impl.common.delegate.DelegateMember;
import org.kuali.kfs.kim.impl.common.delegate.DelegateType;
import org.kuali.kfs.kim.impl.group.GroupMember;
import org.kuali.kfs.kim.impl.identity.address.EntityAddress;
import org.kuali.kfs.kim.impl.identity.affiliation.EntityAffiliation;
import org.kuali.kfs.kim.impl.identity.email.EntityEmail;
import org.kuali.kfs.kim.impl.identity.employment.EntityEmployment;
import org.kuali.kfs.kim.impl.identity.employment.EntityEmploymentStatus;
import org.kuali.kfs.kim.impl.identity.employment.EntityEmploymentType;
import org.kuali.kfs.kim.impl.identity.entity.Entity;
import org.kuali.kfs.kim.impl.identity.external.EntityExternalIdentifier;
import org.kuali.kfs.kim.impl.identity.name.EntityName;
import org.kuali.kfs.kim.impl.identity.phone.EntityPhone;
import org.kuali.kfs.kim.impl.identity.principal.Principal;
import org.kuali.kfs.kim.impl.identity.type.EntityTypeContactInfo;
import org.kuali.kfs.kim.impl.role.RoleMember;
import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.context.SpringContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/*
 * CU Customization:
 * Fixed a few areas that were not referencing the unmasked values as expected.
 */
public class Person extends TransientBusinessObjectBase implements MutableInactivatable {

    private static final long serialVersionUID = 1L;

    // principal data
    protected String principalId;
    protected String principalName;
    protected String entityId;
    protected String entityTypeCode;
    // name data
    protected String firstName = "";
    protected String middleName = "";
    protected String lastName = "";
    protected String name = "";
    // address data
    protected EntityAddress address;
    // email data
    protected String emailAddress = "";
    // phone data
    protected String phoneNumber = "";
    // privacy preferences data
    protected boolean suppressName;
    protected boolean suppressAddress;
    protected boolean suppressPhone;
    protected boolean suppressPersonal;
    protected boolean suppressEmail;
    // affiliation data
    protected List<EntityAffiliation> affiliations;
    protected String campusCode = "";
    //protected Campus campus;
    // external identifier data
    protected Map<String, String> externalIdentifiers;
    // employment data
    protected String employeeStatusCode = "";
    protected EntityEmploymentStatus employeeStatus;
    protected String employeeTypeCode = "";
    protected EntityEmploymentType employeeType;
    protected String primaryDepartmentCode = "";
    protected String employeeId = "";
    protected KualiDecimal baseSalaryAmount = KualiDecimal.ZERO;
    protected boolean primary;
    protected boolean active = true;
    private String lookupRoleNamespaceCode;
    private String lookupRoleName;
    protected List<EntityName> names;
    protected List<EntityTypeContactInfo> entityTypeContactInfos;
    protected List<EntityAddress> addresses;
    protected List<EntityPhone> phoneNumbers;
    protected List<EntityEmail> emailAddresses;
    protected List<GroupMember> groupMembers;
    protected List<RoleMember> roleMembers;
    protected List<DelegateMember> delegateMembers;

    protected static BusinessObjectService businessObjectService;
    protected static IdentityService identityService;
    protected static PersonService personService;

    public Person() {
    }

    public Person(final Principal principal, final String personEntityTypeCode) {
        this(principal, null, personEntityTypeCode);
    }

    public Person(final Principal principal, final Entity entity, final String personEntityTypeCode) {
        setPrincipal(principal, entity, personEntityTypeCode);
    }

    public Person(final String principalId, final String personEntityTypeCode) {
        this(getIdentityService().getPrincipal(principalId), personEntityTypeCode);
    }

    /**
     * Sets the principal object and populates the person object from that.
     */
    public void setPrincipal(final Principal principal, Entity entity, final String personEntityTypeCode) {
        populatePrincipalInfo(principal);
        if (entity == null) {
            entity = getIdentityService().getEntity(principal.getEntityId());
        }
        populateEntityInfo(entity, principal, personEntityTypeCode);
    }

    protected void populatePrincipalInfo(final Principal principal) {
        entityId = principal.getEntityId();
        principalId = principal.getPrincipalId();
        principalName = principal.getPrincipalName();
        active = principal.isActive();
    }

    protected void populateEntityInfo(final Entity entity, final Principal principal, final String personEntityTypeCode) {
        if (entity != null) {
            populatePrivacyInfo(entity);
            final EntityTypeContactInfo entityTypeContactInfoDefault =
                    entity.getEntityTypeContactInfoByTypeCode(personEntityTypeCode);
            entityTypeCode = personEntityTypeCode;
            populateNameInfo(personEntityTypeCode, entity, principal);
            populateAddressInfo(entityTypeContactInfoDefault);
            populateEmailInfo(entityTypeContactInfoDefault);
            populatePhoneInfo(entityTypeContactInfoDefault);
            populateAffiliationInfo(entity);
            populateEmploymentInfo(entity);
            populateExternalIdentifiers(entity);
            populateFullEntityInfo();
        }
    }

    protected void populateNameInfo(final String entityTypeCode, final Entity entity, final Principal principal) {
        if (entity != null) {
            final EntityName entityName = entity.getDefaultName();
            if (entityName != null) {
                firstName = unNullify(entityName.getFirstNameUnmasked());
                middleName = unNullify(entityName.getMiddleNameUnmasked());
                lastName = unNullify(entityName.getLastNameUnmasked());
                if (entityTypeCode.equals(KimConstants.EntityTypes.SYSTEM)) {
                    name = principal.getPrincipalName().toUpperCase(Locale.US);
                } else {
                    name = entityName.getCompositeNameUnmasked();
                    if (StringUtils.isEmpty(name)) {
                        name = lastName + ", " + firstName;
                    }
                }
            } else {
                firstName = "";
                middleName = "";
                if (entityTypeCode.equals(KimConstants.EntityTypes.SYSTEM)) {
                    name = principal.getPrincipalName().toUpperCase(Locale.US);
                    lastName = principal.getPrincipalName().toUpperCase(Locale.US);
                } else {
                    name = "";
                    lastName = "";
                }
            }
        }
    }

    public void populateMembers() {
        populateGroupMembers();
        populateRoleMembers();
        populateDelegateMembers();
    }

    protected void populatePrivacyInfo(final Entity entity) {
        if (entity != null) {
            if (entity.getPrivacyPreferences() != null) {
                suppressName = entity.getPrivacyPreferences().isSuppressName();
                suppressAddress = entity.getPrivacyPreferences().isSuppressAddress();
                suppressPhone = entity.getPrivacyPreferences().isSuppressPhone();
                suppressPersonal = entity.getPrivacyPreferences().isSuppressPersonal();
                suppressEmail = entity.getPrivacyPreferences().isSuppressEmail();
            }
        }
    }

    protected void populateAddressInfo(final EntityTypeContactInfo contactInfoDefault) {
        if (contactInfoDefault != null) {
            final EntityAddress defaultAddress = contactInfoDefault.getDefaultAddress();
            if (defaultAddress != null) {
                address = defaultAddress;
            } else {
                address = new EntityAddress();
                address.setEntityId(contactInfoDefault.getEntityId());
                address.setCity("");
                address.setCountryCode("");
                address.setLine1("");
                address.setLine2("");
                address.setLine3("");
                address.setCity("");
                address.setPostalCode("");
                address.setStateProvinceCode("");
                address.setActive(true);
            }
        }
    }

    protected void populateEmailInfo(final EntityTypeContactInfo contactInfoDefault) {
        if (contactInfoDefault != null) {
            final EntityEmail entityEmail = contactInfoDefault.getDefaultEmailAddress();
            if (entityEmail != null) {
                emailAddress = unNullify(entityEmail.getEmailAddressUnmasked());
            } else {
                emailAddress = "";
            }
        }
    }

    protected void populatePhoneInfo(final EntityTypeContactInfo contactInfoDefault) {
        if (contactInfoDefault != null) {
            final EntityPhone entityPhone = contactInfoDefault.getDefaultPhoneNumber();
            if (entityPhone != null) {
                phoneNumber = unNullify(entityPhone.getFormattedPhoneNumberUnmasked());
            } else {
                phoneNumber = "";
            }
        }
    }

    protected void populateAffiliationInfo(final Entity entity) {
        if (entity != null) {
            if (affiliations == null) {
                affiliations = new ArrayList<>();
            }
            affiliations.addAll(entity.getAffiliations());

            final EntityAffiliation defaultAffiliation = entity.getDefaultAffiliation();
            if (defaultAffiliation != null) {
                campusCode = unNullify(defaultAffiliation.getCampusCode());
            } else {
                campusCode = "";
            }
        }
    }

    protected void populateEmploymentInfo(final Entity entity) {
        if (entity != null) {
            final EntityEmployment employmentInformation = entity.getPrimaryEmployment();
            if (employmentInformation != null) {
                employeeStatusCode = unNullify(employmentInformation.getEmployeeStatus() != null ?
                        employmentInformation.getEmployeeStatus().getCode() : null);
                employeeTypeCode = unNullify(employmentInformation.getEmployeeType() != null ?
                        employmentInformation.getEmployeeType().getCode() : null);
                primaryDepartmentCode = unNullify(employmentInformation.getPrimaryDepartmentCode());
                employeeId = unNullify(employmentInformation.getEmployeeId());
                if (employmentInformation.getBaseSalaryAmount() != null) {
                    baseSalaryAmount = employmentInformation.getBaseSalaryAmount();
                } else {
                    baseSalaryAmount = KualiDecimal.ZERO;
                }
                primary = employmentInformation.isPrimary();
            } else {
                employeeStatusCode = "";
                employeeTypeCode = "";
                primaryDepartmentCode = "";
                employeeId = "";
                baseSalaryAmount = KualiDecimal.ZERO;
            }
        }
    }

    protected void populateExternalIdentifiers(final Entity entity) {
        if (entity != null) {
            final List<? extends EntityExternalIdentifier> externalIds = entity.getExternalIdentifiers();
            externalIdentifiers = new HashMap<>(externalIds.size());
            for (final EntityExternalIdentifier eei : externalIds) {
                externalIdentifiers.put(eei.getExternalIdentifierTypeCode(), eei.getExternalId());
            }
        }
    }

    protected void populateFullEntityInfo() {
        final Entity entity = getIdentityService().getEntity(entityId);

        if (entity != null) {
            populateNames(entity);
            populateEntityTypeContactInfo(entity);
            populateAddresses(entity);
            populatePhoneNumbers(entity);
            populateEmailAddresses(entity);
        }
    }

    private void populateNames(final Entity entity) {
        if (names == null) {
            names = new ArrayList<>();
        }
        names.addAll(entity.getNames());
    }

    private void populateEntityTypeContactInfo(final Entity entity) {
        if (entityTypeContactInfos == null) {
            entityTypeContactInfos = new ArrayList<>();
        }
        entityTypeContactInfos.addAll(entity.getEntityTypeContactInfos());
    }

    private void populateAddresses(final Entity entity) {
        if (addresses == null) {
            addresses = new ArrayList<>();
        }
        entity.getEntityTypeContactInfos().forEach(entityTypeContactInfo ->
                addresses.addAll(entityTypeContactInfo.getAddresses()));
    }

    private void populatePhoneNumbers(final Entity entity) {
        if (phoneNumbers == null) {
            phoneNumbers = new ArrayList<>();
        }
        entity.getEntityTypeContactInfos().forEach(entityTypeContactInfo ->
                phoneNumbers.addAll(entityTypeContactInfo.getPhoneNumbers()));
    }

    private void populateEmailAddresses(final Entity entity) {
        if (emailAddresses == null) {
            emailAddresses = new ArrayList<>();
        }
        entity.getEntityTypeContactInfos().forEach(entityTypeContactInfo ->
                emailAddresses.addAll(entityTypeContactInfo.getEmailAddresses()));
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

    /**
     * So users of this class don't need to program around nulls.
     */
    private String unNullify(final String str) {
        if (str == null) {
            return "";
        }
        return str;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getPrincipalId() {
        return principalId;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(final String principalName) {
        this.principalName = principalName;
    }

    public String getFirstName() {
        if (suppressName) {
            return KimConstants.RESTRICTED_DATA_MASK;
        }
        return firstName;
    }

    public String getFirstNameUnmasked() {
        return firstName;
    }

    public String getMiddleName() {
        if (suppressName) {
            return KimConstants.RESTRICTED_DATA_MASK;
        }
        return middleName;
    }

    public String getMiddleNameUnmasked() {
        return middleName;
    }

    public String getLastName() {
        if (suppressName) {
            return KimConstants.RESTRICTED_DATA_MASK;
        }
        return lastName;
    }

    public String getLastNameUnmasked() {
        return lastName;
    }

    public String getName() {
        if (suppressName) {
            return KimConstants.RESTRICTED_DATA_MASK;
        }
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getNameUnmasked() {
        return name;
    }

    public String getPhoneNumber() {
        if (suppressPhone) {
            return KimConstants.RESTRICTED_DATA_MASK;
        }
        return phoneNumber;
    }

    public String getPhoneNumberUnmasked() {
        return phoneNumber;
    }

    public String getEmailAddress() {
        if (suppressEmail) {
            return KimConstants.RESTRICTED_DATA_MASK;
        }
        return emailAddress;
    }

    public String getEmailAddressUnmasked() {
        return emailAddress;
    }

    public boolean isSuppressName() {
        return suppressName;
    }

    public void setSuppressName(final boolean suppressName) {
        this.suppressName = suppressName;
    }

    public boolean isSuppressAddress() {
        return suppressAddress;
    }

    public void setSuppressAddress(final boolean suppressAddress) {
        this.suppressAddress = suppressAddress;
    }

    public boolean isSuppressPhone() {
        return suppressPhone;
    }

    public void setSuppressPhone(final boolean suppressPhone) {
        this.suppressPhone = suppressPhone;
    }

    public boolean isSuppressPersonal() {
        return suppressPersonal;
    }

    public void setSuppressPersonal(final boolean suppressPersonal) {
        this.suppressPersonal = suppressPersonal;
    }

    public boolean isSuppressEmail() {
        return suppressEmail;
    }

    public void setSuppressEmail(final boolean suppressEmail) {
        this.suppressEmail = suppressEmail;
    }

    public List<EntityAffiliation> getAffiliations() {
        return affiliations;
    }

    public void setAffiliations(final List<EntityAffiliation> affiliations) {
        this.affiliations = affiliations;
    }

    public String getExternalId(final String externalIdentifierTypeCode) {
        return externalIdentifiers.get(externalIdentifierTypeCode);
    }

    /**
     * @return the campus code from the default affiliation for the identity; null if no default affiliation is set.
     */
    public String getCampusCode() {
        return campusCode;
    }

    public Map<String, String> getExternalIdentifiers() {
        return externalIdentifiers;
    }

    public String getAddressLine1() {
        return address.getLine1();
    }

    public String getAddressLine1Unmasked() {
        return address.getLine1Unmasked();
    }

    public String getAddressLine2() {
        return address.getLine2();
    }

    public String getAddressLine2Unmasked() {
        return address.getLine2Unmasked();
    }

    public String getAddressLine3() {
        return address.getLine3();
    }

    public String getAddressLine3Unmasked() {
        return address.getLine3Unmasked();
    }

    public String getAddressCity() {
        return address.getCity();
    }

    public String getAddressCityUnmasked() {
        return address.getCityUnmasked();
    }

    public String getAddressStateProvinceCode() {
        return address.getStateProvinceCode();
    }

    public String getAddressStateProvinceCodeUnmasked() {
        return address.getStateProvinceCodeUnmasked();
    }

    public String getAddressPostalCode() {
        return address.getPostalCode();
    }

    public String getAddressPostalCodeUnmasked() {
        return address.getPostalCodeUnmasked();
    }

    public String getAddressCountryCode() {
        return address.getCountryCode();
    }

    public String getAddressCountryCodeUnmasked() {
        return address.getCountryCodeUnmasked();
    }

    public String getEmployeeStatusCode() {
        return employeeStatusCode;
    }

    public String getEmployeeTypeCode() {
        return employeeTypeCode;
    }

    public KualiDecimal getBaseSalaryAmount() {
        return baseSalaryAmount;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getPrimaryDepartmentCode() {
        return primaryDepartmentCode;
    }

    public String getEntityTypeCode() {
        return entityTypeCode;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(final boolean primary) {
        this.primary = primary;
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

    public EntityEmploymentStatus getEmployeeStatus() {
        return employeeStatus;
    }

    public EntityEmploymentType getEmployeeType() {
        return employeeType;
    }

    public List<EntityName> getNames() {
        return names;
    }

    public void setNames(final List<EntityName> names) {
        this.names = names;
    }

    public List<EntityTypeContactInfo> getEntityTypeContactInfos() {
        return entityTypeContactInfos;
    }

    public void setEntityTypeContactInfos(final List<EntityTypeContactInfo> entityTypeContactInfos) {
        this.entityTypeContactInfos = entityTypeContactInfos;
    }

    public List<EntityAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(final List<EntityAddress> addresses) {
        this.addresses = addresses;
    }

    public List<EntityPhone> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(final List<EntityPhone> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public List<EntityEmail> getEmailAddresses() {
        return emailAddresses;
    }

    public void setEmailAddresses(final List<EntityEmail> emailAddresses) {
        this.emailAddresses = emailAddresses;
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

    private static BusinessObjectService getBusinessObjectService() {
        if (businessObjectService == null) {
            businessObjectService = SpringContext.getBean(BusinessObjectService.class);
        }
        return businessObjectService;
    }

    public static IdentityService getIdentityService() {
        if (identityService == null) {
            identityService = KimApiServiceLocator.getIdentityService();
        }
        return identityService;
    }

    public static PersonService getPersonService() {
        if (personService == null) {
            personService = KimApiServiceLocator.getPersonService();
        }
        return personService;
    }
}
