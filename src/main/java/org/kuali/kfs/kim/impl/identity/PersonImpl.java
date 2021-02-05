/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
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

import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.kim.impl.common.delegate.DelegateMemberBo;
import org.kuali.kfs.kim.impl.common.delegate.DelegateTypeBo;
import org.kuali.kfs.kim.impl.group.GroupMemberBo;
import org.kuali.kfs.kim.impl.identity.address.EntityAddressBo;
import org.kuali.kfs.kim.impl.identity.affiliation.EntityAffiliationBo;
import org.kuali.kfs.kim.impl.identity.email.EntityEmailBo;
import org.kuali.kfs.kim.impl.identity.employment.EntityEmploymentStatusBo;
import org.kuali.kfs.kim.impl.identity.employment.EntityEmploymentTypeBo;
import org.kuali.kfs.kim.impl.identity.name.EntityNameBo;
import org.kuali.kfs.kim.impl.identity.phone.EntityPhoneBo;
import org.kuali.kfs.kim.impl.identity.type.EntityTypeContactInfoBo;
import org.kuali.kfs.kim.impl.role.RoleMemberBo;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.membership.MemberType;
import org.kuali.rice.core.api.mo.common.active.MutableInactivatable;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kim.api.identity.IdentityService;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.kim.api.identity.address.EntityAddress;
import org.kuali.rice.kim.api.identity.affiliation.EntityAffiliation;
import org.kuali.rice.kim.api.identity.email.EntityEmailContract;
import org.kuali.rice.kim.api.identity.employment.EntityEmployment;
import org.kuali.rice.kim.api.identity.entity.Entity;
import org.kuali.rice.kim.api.identity.entity.EntityDefault;
import org.kuali.rice.kim.api.identity.external.EntityExternalIdentifier;
import org.kuali.rice.kim.api.identity.name.EntityName;
import org.kuali.rice.kim.api.identity.phone.EntityPhoneContract;
import org.kuali.rice.kim.api.identity.principal.Principal;
import org.kuali.rice.kim.api.identity.type.EntityTypeContactInfoDefault;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.krad.bo.TransientBusinessObjectBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/*
 * CU Customization:
 * Fixed a few areas that were not referencing the unmasked values as expected.
 */
public class PersonImpl extends TransientBusinessObjectBase implements MutableInactivatable, Person {

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
    /*protected String addressLine1 = "";
    protected String addressLine2 = "";
    protected String addressLine3 = "";
    protected String addressCity = "";
    protected String addressStateProvinceCode = "";
    protected String addressPostalCode = "";
    protected String addressCountryCode = "";*/
    // email data
    protected String emailAddress = "";
    // phone data
    protected String phoneNumber = "";
    // privacy preferences data
    protected boolean suppressName = false;
    protected boolean suppressAddress = false;
    protected boolean suppressPhone = false;
    protected boolean suppressPersonal = false;
    protected boolean suppressEmail = false;
    // affiliation data
    protected List<EntityAffiliationBo> affiliations;
    protected String campusCode = "";
    //protected Campus campus;
    // external identifier data
    protected Map<String, String> externalIdentifiers = null;
    // employment data
    protected String employeeStatusCode = "";
    protected EntityEmploymentStatusBo employeeStatus;
    protected String employeeTypeCode = "";
    protected EntityEmploymentTypeBo employeeType;
    protected String primaryDepartmentCode = "";
    protected String employeeId = "";
    protected KualiDecimal baseSalaryAmount = KualiDecimal.ZERO;
    protected boolean primary = false;
    protected boolean active = true;
    private String lookupRoleNamespaceCode;
    private String lookupRoleName;
    protected List<EntityNameBo> names = null;
    protected List<EntityTypeContactInfoBo> entityTypeContactInfos = null;
    protected List<EntityAddressBo> addresses = null;
    protected List<EntityPhoneBo> phoneNumbers = null;
    protected List<EntityEmailBo> emailAddresses = null;
    protected List<GroupMemberBo> groupMembers = null;
    protected List<RoleMemberBo> roleMembers = null;
    protected List<DelegateMemberBo> delegateMembers = null;

    protected static BusinessObjectService businessObjectService;
    protected static IdentityService identityService;
    protected static PersonService personService;

    public PersonImpl() {
    }

    public PersonImpl(Principal principal, String personEntityTypeCode) {
        this(principal, null, personEntityTypeCode);
    }

    public PersonImpl(Principal principal, EntityDefault entity, String personEntityTypeCode) {
        setPrincipal(principal, entity, personEntityTypeCode);
    }

    public PersonImpl(String principalId, String personEntityTypeCode) {
        this(getIdentityService().getPrincipal(principalId), personEntityTypeCode);
    }

    public PersonImpl(EntityDefaultInfoCacheBo p) {
        entityId = p.getEntityId();
        principalId = p.getPrincipalId();
        principalName = p.getPrincipalName();
        entityTypeCode = p.getEntityTypeCode();
        firstName = p.getFirstName();
        middleName = p.getMiddleName();
        lastName = p.getLastName();
        name = p.getName();
        campusCode = p.getCampusCode();
        primaryDepartmentCode = p.getPrimaryDepartmentCode();
        employeeId = p.getEmployeeId();
        affiliations = new ArrayList<>(0);
        externalIdentifiers = new HashMap<>(0);
        names = new ArrayList<>(0);
        entityTypeContactInfos = new ArrayList<>(0);
        addresses = new ArrayList<>(0);
        phoneNumbers = new ArrayList<>(0);
        emailAddresses = new ArrayList<>(0);
        groupMembers = new ArrayList<>(0);
        roleMembers = new ArrayList<>(0);
        delegateMembers = new ArrayList<>(0);
    }

    /**
     * Sets the principal object and populates the person object from that.
     */
    public void setPrincipal(Principal principal, EntityDefault entity, String personEntityTypeCode) {
        populatePrincipalInfo(principal);
        if (entity == null) {
            entity = getIdentityService().getEntityDefault(principal.getEntityId());
        }
        populateEntityInfo(entity, principal, personEntityTypeCode);
    }

    protected void populatePrincipalInfo(Principal principal) {
        entityId = principal.getEntityId();
        principalId = principal.getPrincipalId();
        principalName = principal.getPrincipalName();
        active = principal.isActive();
    }

    protected void populateEntityInfo(EntityDefault entity, Principal principal, String personEntityTypeCode) {
        if (entity != null) {
            populatePrivacyInfo(entity);
            EntityTypeContactInfoDefault entityTypeContactInfoDefault = entity.getEntityType(personEntityTypeCode);
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

    protected void populateNameInfo(String entityTypeCode, EntityDefault entity, Principal principal) {
        if (entity != null) {
            EntityName entityName = entity.getName();
            if (entityName != null) {
                firstName = unNullify(entityName.getFirstNameUnmasked());
                middleName = unNullify(entityName.getMiddleNameUnmasked());
                lastName = unNullify(entityName.getLastNameUnmasked());
                if (entityTypeCode.equals(KimConstants.EntityTypes.SYSTEM)) {
                    name = principal.getPrincipalName().toUpperCase(Locale.US);
                } else {
                    name = unNullify(entityName.getCompositeNameUnmasked());
                    if ("".equals(name) || name == null) {
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

    protected void populatePrivacyInfo(EntityDefault entity) {
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

    protected void populateAddressInfo(EntityTypeContactInfoDefault contactInfoDefault) {
        if (contactInfoDefault != null) {
            EntityAddress defaultAddress = contactInfoDefault.getDefaultAddress();
            if (defaultAddress != null) {
                address = defaultAddress;
            } else {
                EntityAddress.Builder builder = EntityAddress.Builder.create();
                builder.setCity("");
                builder.setCountryCode("");
                builder.setLine1("");
                builder.setLine2("");
                builder.setLine3("");
                builder.setCity("");
                builder.setPostalCode("");
                builder.setStateProvinceCode("");
                builder.setActive(true);
                address = builder.build();
            }
        }
    }

    protected void populateEmailInfo(EntityTypeContactInfoDefault contactInfoDefault) {
        if (contactInfoDefault != null) {
            EntityEmailContract entityEmail = contactInfoDefault.getDefaultEmailAddress();
            if (entityEmail != null) {
                emailAddress = unNullify(entityEmail.getEmailAddressUnmasked());
            } else {
                emailAddress = "";
            }
        }
    }

    protected void populatePhoneInfo(EntityTypeContactInfoDefault contactInfoDefault) {
        if (contactInfoDefault != null) {
            EntityPhoneContract entityPhone = contactInfoDefault.getDefaultPhoneNumber();
            if (entityPhone != null) {
                phoneNumber = unNullify(entityPhone.getFormattedPhoneNumberUnmasked());
            } else {
                phoneNumber = "";
            }
        }
    }

    protected void populateAffiliationInfo(EntityDefault entity) {
        if (entity != null) {
            if (affiliations == null) {
                affiliations = new ArrayList<>();
            }
            entity.getAffiliations().forEach(affiliation -> affiliations.add(EntityAffiliationBo.from(affiliation)));

            EntityAffiliation defaultAffiliation = entity.getDefaultAffiliation();
            if (defaultAffiliation != null) {
                campusCode = unNullify(defaultAffiliation.getCampusCode());
            } else {
                campusCode = "";
            }
        }
    }

    protected void populateEmploymentInfo(EntityDefault entity) {
        if (entity != null) {
            EntityEmployment employmentInformation = entity.getEmployment();
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

    protected void populateExternalIdentifiers(EntityDefault entity) {
        if (entity != null) {
            List<? extends EntityExternalIdentifier> externalIds = entity.getExternalIdentifiers();
            externalIdentifiers = new HashMap<>(externalIds.size());
            for (EntityExternalIdentifier eei : externalIds) {
                externalIdentifiers.put(eei.getExternalIdentifierTypeCode(), eei.getExternalId());
            }
        }
    }

    protected void populateFullEntityInfo() {
        Entity entity = getIdentityService().getEntity(entityId);

        if (entity != null) {
            populateNames(entity);
            populateEntityTypeContactInfo(entity);
            populateAddresses(entity);
            populatePhoneNumbers(entity);
            populateEmailAddresses(entity);
        }
    }

    private void populateNames(Entity entity) {
        if (names == null) {
            names = new ArrayList<>();
        }
        entity.getNames().forEach(name -> names.add(EntityNameBo.from(name)));
    }

    private void populateEntityTypeContactInfo(Entity entity) {
        if (entityTypeContactInfos == null) {
            entityTypeContactInfos = new ArrayList<>();
        }
        entity.getEntityTypeContactInfos().forEach(entityTypeContactInfo ->
                entityTypeContactInfos.add(EntityTypeContactInfoBo.from(entityTypeContactInfo)));
    }

    private void populateAddresses(Entity entity) {
        if (addresses == null) {
            addresses = new ArrayList<>();
        }
        entity.getEntityTypeContactInfos().forEach(entityTypeContactInfo ->
                entityTypeContactInfo.getAddresses().forEach(address ->
                        addresses.add(EntityAddressBo.from(address))));
    }

    private void populatePhoneNumbers(Entity entity) {
        if (phoneNumbers == null) {
            phoneNumbers = new ArrayList<>();
        }
        entity.getEntityTypeContactInfos().forEach(entityTypeContactInfo ->
                entityTypeContactInfo.getPhoneNumbers().forEach(phoneNumber ->
                        phoneNumbers.add(EntityPhoneBo.from(phoneNumber))));
    }

    private void populateEmailAddresses(Entity entity) {
        if (emailAddresses == null) {
            emailAddresses = new ArrayList<>();
        }
        entity.getEntityTypeContactInfos().forEach(entityTypeContactInfo ->
                entityTypeContactInfo.getEmailAddresses().forEach(emailAddress ->
                        emailAddresses.add(EntityEmailBo.from(emailAddress))));
    }

    private void populateGroupMembers() {
        Map<String, String> criteria = new HashMap<>(2);
        criteria.put(KIMPropertyConstants.GroupMember.MEMBER_ID, principalId);
        criteria.put(KIMPropertyConstants.GroupMember.MEMBER_TYPE_CODE,
                KimConstants.KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE.getCode());
        groupMembers = (List<GroupMemberBo>) getBusinessObjectService().findMatching(GroupMemberBo.class, criteria);
    }

    private void populateRoleMembers() {
        Map<String, String> criteria = new HashMap<>(2);
        criteria.put(KIMPropertyConstants.RoleMember.MEMBER_ID, principalId);
        criteria.put(KIMPropertyConstants.RoleMember.MEMBER_TYPE_CODE,
                KimConstants.KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE.getCode());
        roleMembers = (List<RoleMemberBo>) getBusinessObjectService().findMatching(RoleMemberBo.class, criteria);
    }

    private void populateDelegateMembers() {
        Map<String, String> criteria = new HashMap<>(2);
        criteria.put(KIMPropertyConstants.DelegationMember.MEMBER_ID, principalId);
        criteria.put(KIMPropertyConstants.DelegationMember.MEMBER_TYPE_CODE, MemberType.PRINCIPAL.getCode());
        delegateMembers = (List<DelegateMemberBo>) getBusinessObjectService().findMatching(DelegateMemberBo.class,
                criteria);
        criteria.clear();
        for (DelegateMemberBo dmb: delegateMembers) {
            criteria.put(KIMPropertyConstants.Delegation.DELEGATION_ID, dmb.getDelegationId());
            DelegateTypeBo delegate = getBusinessObjectService().findByPrimaryKey(DelegateTypeBo.class, criteria);
            dmb.setDelegationType(delegate.getDelegationTypeCode());
        }
    }

    /**
     * So users of this class don't need to program around nulls.
     */
    private String unNullify(String str) {
        if (str == null) {
            return "";
        }
        return str;
    }

    @Override
    public String getEntityId() {
        return entityId;
    }

    @Override
    public String getPrincipalId() {
        return principalId;
    }

    @Override
    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    @Override
    public String getFirstName() {
        if (suppressName) {
            return KimConstants.RESTRICTED_DATA_MASK;
        }
        return firstName;
    }

    @Override
    public String getFirstNameUnmasked() {
        return firstName;
    }

    @Override
    public String getMiddleName() {
        if (suppressName) {
            return KimConstants.RESTRICTED_DATA_MASK;
        }
        return middleName;
    }

    @Override
    public String getMiddleNameUnmasked() {
        return middleName;
    }

    @Override
    public String getLastName() {
        if (suppressName) {
            return KimConstants.RESTRICTED_DATA_MASK;
        }
        return lastName;
    }

    @Override
    public String getLastNameUnmasked() {
        return lastName;
    }

    @Override
    public String getName() {
        if (suppressName) {
            return KimConstants.RESTRICTED_DATA_MASK;
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getNameUnmasked() {
        return this.name;
    }

    @Override
    public String getPhoneNumber() {
        if (suppressPhone) {
            return KimConstants.RESTRICTED_DATA_MASK;
        }
        return phoneNumber;
    }

    @Override
    public String getPhoneNumberUnmasked() {
        return phoneNumber;
    }

    @Override
    public String getEmailAddress() {
        if (suppressEmail) {
            return KimConstants.RESTRICTED_DATA_MASK;
        }
        return emailAddress;
    }

    @Override
    public String getEmailAddressUnmasked() {
        return emailAddress;
    }

    public boolean isSuppressName() {
        return suppressName;
    }

    public void setSuppressName(boolean suppressName) {
        this.suppressName = suppressName;
    }

    public boolean isSuppressAddress() {
        return suppressAddress;
    }

    public void setSuppressAddress(boolean suppressAddress) {
        this.suppressAddress = suppressAddress;
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

    public boolean isSuppressEmail() {
        return suppressEmail;
    }

    public void setSuppressEmail(boolean suppressEmail) {
        this.suppressEmail = suppressEmail;
    }

    public List<EntityAffiliationBo> getAffiliations() {
        return affiliations;
    }

    public void setAffiliations(List<EntityAffiliationBo> affiliations) {
        this.affiliations = affiliations;
    }

    @Override
    public boolean hasAffiliationOfType(String affiliationTypeCode) {
        return getCampusCodesForAffiliationOfType(affiliationTypeCode).size() > 0;
    }

    @Override
    public List<String> getCampusCodesForAffiliationOfType(String affiliationTypeCode) {
        ArrayList<String> campusCodes = new ArrayList<>(3);
        if (affiliationTypeCode == null) {
            return campusCodes;
        }
        for (EntityAffiliationBo a : getAffiliations()) {
            if (a.getAffiliationType().getCode().equals(affiliationTypeCode)) {
                campusCodes.add(a.getCampusCode());
            }
        }
        return campusCodes;
    }

    @Override
    public String getExternalId(String externalIdentifierTypeCode) {
        return externalIdentifiers.get(externalIdentifierTypeCode);
    }

    /**
     * @return the campus code from the default affiliation for the identity; null if no default affiliation is set.
     */
    @Override
    public String getCampusCode() {
        return campusCode;
    }

    @Override
    public Map<String, String> getExternalIdentifiers() {
        return externalIdentifiers;
    }

    @Override
    public String getAddressLine1() {
        return address.getLine1();
    }

    @Override
    public String getAddressLine1Unmasked() {
        return address.getLine1Unmasked();
    }

    @Override
    public String getAddressLine2() {
        return address.getLine2();
    }

    @Override
    public String getAddressLine2Unmasked() {
        return address.getLine2Unmasked();
    }

    @Override
    public String getAddressLine3() {
        return address.getLine3();
    }

    @Override
    public String getAddressLine3Unmasked() {
        return address.getLine3Unmasked();
    }

    @Override
    public String getAddressCity() {
        return address.getCity();
    }

    @Override
    public String getAddressCityUnmasked() {
        return address.getCityUnmasked();
    }

    @Override
    public String getAddressStateProvinceCode() {
        return address.getStateProvinceCode();
    }

    @Override
    public String getAddressStateProvinceCodeUnmasked() {
        return address.getStateProvinceCodeUnmasked();
    }

    @Override
    public String getAddressPostalCode() {
        return address.getPostalCode();
    }

    @Override
    public String getAddressPostalCodeUnmasked() {
        return address.getPostalCodeUnmasked();
    }

    @Override
    public String getAddressCountryCode() {
        return address.getCountryCode();
    }

    @Override
    public String getAddressCountryCodeUnmasked() {
        return address.getCountryCodeUnmasked();
    }

    @Override
    public String getEmployeeStatusCode() {
        return this.employeeStatusCode;
    }

    @Override
    public String getEmployeeTypeCode() {
        return this.employeeTypeCode;
    }

    @Override
    public KualiDecimal getBaseSalaryAmount() {
        return this.baseSalaryAmount;
    }

    @Override
    public String getEmployeeId() {
        return this.employeeId;
    }

    @Override
    public String getPrimaryDepartmentCode() {
        return this.primaryDepartmentCode;
    }

    @Override
    public String getEntityTypeCode() {
        return this.entityTypeCode;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    public String getLookupRoleNamespaceCode() {
        return this.lookupRoleNamespaceCode;
    }

    public void setLookupRoleNamespaceCode(String lookupRoleNamespaceCode) {
        this.lookupRoleNamespaceCode = lookupRoleNamespaceCode;
    }

    public String getLookupRoleName() {
        return this.lookupRoleName;
    }

    public void setLookupRoleName(String lookupRoleName) {
        this.lookupRoleName = lookupRoleName;
    }

    public EntityEmploymentStatusBo getEmployeeStatus() {
        return this.employeeStatus;
    }

    public EntityEmploymentTypeBo getEmployeeType() {
        return this.employeeType;
    }

    public List<EntityNameBo> getNames() {
        return names;
    }

    public void setNames(List<EntityNameBo> names) {
        this.names = names;
    }

    public List<EntityTypeContactInfoBo> getEntityTypeContactInfos() {
        return entityTypeContactInfos;
    }

    public void setEntityTypeContactInfos(List<EntityTypeContactInfoBo> entityTypeContactInfos) {
        this.entityTypeContactInfos = entityTypeContactInfos;
    }

    public List<EntityAddressBo> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<EntityAddressBo> addresses) {
        this.addresses = addresses;
    }

    public List<EntityPhoneBo> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(List<EntityPhoneBo> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public List<EntityEmailBo> getEmailAddresses() {
        return emailAddresses;
    }

    public void setEmailAddresses(List<EntityEmailBo> emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

    public List<GroupMemberBo> getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(List<GroupMemberBo> groupMembers) {
        this.groupMembers = groupMembers;
    }

    public List<RoleMemberBo> getRoleMembers() {
        return roleMembers;
    }

    public void setRoleMembers(List<RoleMemberBo> roleMembers) {
        this.roleMembers = roleMembers;
    }

    public List<DelegateMemberBo> getDelegateMembers() {
        return delegateMembers;
    }

    public void setDelegateMembers(List<DelegateMemberBo> delegateMembers) {
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
