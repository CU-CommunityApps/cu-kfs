/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2020 Kuali, Inc.
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
import org.eclipse.persistence.annotations.JoinFetch;
import org.eclipse.persistence.annotations.JoinFetchType;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.rice.kim.api.identity.employment.EntityEmployment;
import org.kuali.rice.kim.api.role.Role;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.kim.api.type.KimType;
import org.kuali.kfs.kim.bo.ui.KimDocumentRoleMember;
import org.kuali.kfs.kim.bo.ui.KimDocumentRoleQualifier;
import org.kuali.kfs.kim.bo.ui.KimDocumentRoleResponsibilityAction;
import org.kuali.kfs.kim.bo.ui.PersonDocumentAddress;
import org.kuali.kfs.kim.bo.ui.PersonDocumentAffiliation;
import org.kuali.kfs.kim.bo.ui.PersonDocumentCitizenship;
import org.kuali.kfs.kim.bo.ui.PersonDocumentEmail;
import org.kuali.kfs.kim.bo.ui.PersonDocumentEmploymentInfo;
import org.kuali.kfs.kim.bo.ui.PersonDocumentGroup;
import org.kuali.kfs.kim.bo.ui.PersonDocumentName;
import org.kuali.kfs.kim.bo.ui.PersonDocumentPhone;
import org.kuali.kfs.kim.bo.ui.PersonDocumentPrivacy;
import org.kuali.kfs.kim.bo.ui.PersonDocumentRole;
import org.kuali.kfs.kim.bo.ui.RoleDocumentDelegation;
import org.kuali.kfs.kim.bo.ui.RoleDocumentDelegationMember;
import org.kuali.kfs.kim.bo.ui.RoleDocumentDelegationMemberQualifier;
import org.kuali.kfs.kim.impl.role.RoleBo;
import org.kuali.kfs.kim.impl.role.RoleMemberBo;
import org.kuali.kfs.kim.impl.services.KimImplServiceLocator;
import org.kuali.kfs.kim.impl.type.KimTypeAttributesHelper;
import org.kuali.kfs.kim.service.KIMServiceLocatorInternal;
import org.kuali.kfs.kim.service.UiDocumentService;
import org.kuali.kfs.kns.service.DocumentHelperService;
import org.kuali.kfs.kns.service.KNSServiceLocator;
import org.kuali.kfs.krad.rules.rule.event.KualiDocumentEvent;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.rice.core.api.membership.MemberType;
import org.kuali.rice.kew.framework.postprocessor.DocumentRouteStatusChange;
import org.kuali.rice.krad.data.jpa.converters.BooleanYNConverter;
import org.kuali.rice.krad.data.jpa.converters.HashConverter;
import org.kuali.rice.krad.data.platform.MaxValueIncrementerFactory;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * CU Customization:
 * Added helper methods to control the editability of group memberships (such as "validAssignGroup"),
 * similar to the existing code that controls the editability of role memberships.
 */
@AttributeOverrides({@AttributeOverride(name = "documentNumber", column = @Column(name = "FDOC_NBR"))})
@Entity
@Table(name = "KRIM_PERSON_DOCUMENT_T")
public class IdentityManagementPersonDocument extends IdentityManagementKimDocument {

    protected static final long serialVersionUID = -534993712085516925L;

    // principal data
    @Column(name = "PRNCPL_ID")
    protected String principalId;

    @Column(name = "PRNCPL_NM")
    protected String principalName;

    @Column(name = "ENTITY_ID")
    protected String entityId;

    //@Type(type="org.kuali.rice.krad.util.HibernateKualiHashType")
    @Column(name = "PRNCPL_PSWD")
    @Convert(converter = HashConverter.class)
    protected String password;

    @Column(name = "UNIV_ID")
    protected String univId = "";

    // affiliation data
    @JoinFetch(value = JoinFetchType.OUTER)
    @OneToMany(targetEntity = PersonDocumentAffiliation.class, orphanRemoval = true,
            cascade = {CascadeType.REFRESH, CascadeType.REMOVE, CascadeType.PERSIST})
    @JoinColumn(name = "FDOC_NBR", referencedColumnName = "FDOC_NBR", insertable = false, updatable = false)
    protected List<PersonDocumentAffiliation> affiliations;

    @Transient
    protected String campusCode = "";

    // external identifier data
    @Transient
    protected Map<String, String> externalIdentifiers = null;

    @Column(name = "ACTV_IND")
    @Convert(converter = BooleanYNConverter.class)
    protected boolean active;

    // citizenship
    @Transient
    protected List<PersonDocumentCitizenship> citizenships;

    @JoinFetch(value = JoinFetchType.OUTER)
    @OneToMany(targetEntity = PersonDocumentName.class, orphanRemoval = true,
            cascade = {CascadeType.REFRESH, CascadeType.REMOVE, CascadeType.PERSIST})
    @JoinColumn(name = "FDOC_NBR", referencedColumnName = "FDOC_NBR", insertable = false, updatable = false)
    protected List<PersonDocumentName> names;

    @JoinFetch(value = JoinFetchType.OUTER)
    @OneToMany(targetEntity = PersonDocumentAddress.class, orphanRemoval = true,
            cascade = {CascadeType.REFRESH, CascadeType.REMOVE, CascadeType.PERSIST})
    @JoinColumn(name = "FDOC_NBR", referencedColumnName = "FDOC_NBR", insertable = false, updatable = false)
    protected List<PersonDocumentAddress> addrs;

    @JoinFetch(value = JoinFetchType.OUTER)
    @OneToMany(targetEntity = PersonDocumentPhone.class, orphanRemoval = true,
            cascade = {CascadeType.REFRESH, CascadeType.REMOVE, CascadeType.PERSIST})
    @JoinColumn(name = "FDOC_NBR", referencedColumnName = "FDOC_NBR", insertable = false, updatable = false)
    protected List<PersonDocumentPhone> phones;

    @JoinFetch(value = JoinFetchType.OUTER)
    @OneToMany(targetEntity = PersonDocumentEmail.class, orphanRemoval = true,
            cascade = {CascadeType.REFRESH, CascadeType.REMOVE, CascadeType.PERSIST})
    @JoinColumn(name = "FDOC_NBR", referencedColumnName = "FDOC_NBR", insertable = false, updatable = false)
    protected List<PersonDocumentEmail> emails;

    @JoinFetch(value = JoinFetchType.OUTER)
    @OneToMany(targetEntity = PersonDocumentGroup.class, orphanRemoval = true,
            cascade = {CascadeType.REFRESH, CascadeType.REMOVE, CascadeType.PERSIST})
    @JoinColumn(name = "FDOC_NBR", referencedColumnName = "FDOC_NBR", insertable = false, updatable = false)
    protected List<PersonDocumentGroup> groups;

    @JoinFetch(value = JoinFetchType.OUTER)
    @OneToMany(targetEntity = PersonDocumentRole.class, orphanRemoval = true,
            cascade = {CascadeType.REFRESH, CascadeType.REMOVE, CascadeType.PERSIST})
    @JoinColumn(name = "FDOC_NBR", referencedColumnName = "FDOC_NBR", insertable = false, updatable = false)
    protected List<PersonDocumentRole> roles;

    @JoinFetch(value = JoinFetchType.OUTER)
    @OneToOne(targetEntity = PersonDocumentPrivacy.class, orphanRemoval = true,
            cascade = {CascadeType.REFRESH, CascadeType.REMOVE, CascadeType.PERSIST})
    @PrimaryKeyJoinColumn(name = "FDOC_NBR", referencedColumnName = "FDOC_NBR")
    protected PersonDocumentPrivacy privacy;
    @Transient
    protected transient DocumentHelperService documentHelperService;
    @Transient
    protected transient UiDocumentService uiDocumentService;

    public IdentityManagementPersonDocument() {
        affiliations = new ArrayList<>();
        citizenships = new ArrayList<>();
        names = new ArrayList<>();
        addrs = new ArrayList<>();
        phones = new ArrayList<>();
        emails = new ArrayList<>();
        groups = new ArrayList<>();
        roles = new ArrayList<>();
        privacy = new PersonDocumentPrivacy();
        this.active = true;
    }

    public String getPrincipalId() {
        return this.principalId;
    }

    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }

    public String getPrincipalName() {
        return this.principalName;
    }

    /*
     * sets the principal name.
     * Principal names are converted to lower case.
     */
    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    public String getEntityId() {
        return this.entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public List<PersonDocumentAffiliation> getAffiliations() {
        return this.affiliations;
    }

    public void setAffiliations(List<PersonDocumentAffiliation> affiliations) {
        this.affiliations = affiliations;
    }

    public String getCampusCode() {
        return this.campusCode;
    }

    public void setCampusCode(String campusCode) {
        this.campusCode = campusCode;
    }

    public Map<String, String> getExternalIdentifiers() {
        return this.externalIdentifiers;
    }

    public void setExternalIdentifiers(Map<String, String> externalIdentifiers) {
        this.externalIdentifiers = externalIdentifiers;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<PersonDocumentCitizenship> getCitizenships() {
        return this.citizenships;
    }

    public void setCitizenships(List<PersonDocumentCitizenship> citizenships) {
        this.citizenships = citizenships;
    }

    public List<PersonDocumentName> getNames() {
        return this.names;
    }

    public void setNames(List<PersonDocumentName> names) {
        this.names = names;
    }

    public List<PersonDocumentAddress> getAddrs() {
        return this.addrs;
    }

    public void setAddrs(List<PersonDocumentAddress> addrs) {
        this.addrs = addrs;
    }

    public List<PersonDocumentPhone> getPhones() {
        return this.phones;
    }

    public void setPhones(List<PersonDocumentPhone> phones) {
        this.phones = phones;
    }

    public List<PersonDocumentEmail> getEmails() {
        return this.emails;
    }

    public void setEmails(List<PersonDocumentEmail> emails) {
        this.emails = emails;
    }

    public List<PersonDocumentRole> getRoles() {
        return this.roles;
    }

    public void setRoles(List<PersonDocumentRole> roles) {
        this.roles = roles;
    }

    public List<PersonDocumentGroup> getGroups() {
        return this.groups;
    }

    public void setGroups(List<PersonDocumentGroup> groups) {
        this.groups = groups;
    }

    public String getUnivId() {
        return this.univId;
    }

    public void setUnivId(String univId) {
        this.univId = univId;
    }

    public PersonDocumentPrivacy getPrivacy() {
        return this.privacy;
    }

    public void setPrivacy(PersonDocumentPrivacy privacy) {
        this.privacy = privacy;
    }

    public void initializeDocumentForNewPerson() {
        if (StringUtils.isBlank(this.principalId)) {
            DataFieldMaxValueIncrementer incrementer = MaxValueIncrementerFactory.getIncrementer(
                    KimImplServiceLocator.getDataSource(), KimConstants.SequenceNames.KRIM_PRNCPL_ID_S);
            this.principalId = incrementer.nextStringValue();
        }
        if (StringUtils.isBlank(this.entityId)) {
            DataFieldMaxValueIncrementer incrementer = MaxValueIncrementerFactory.getIncrementer(
                    KimImplServiceLocator.getDataSource(), KimConstants.SequenceNames.KRIM_ENTITY_ID_S);
            this.entityId = incrementer.nextStringValue();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public List buildListOfDeletionAwareLists() {
        List managedLists = super.buildListOfDeletionAwareLists();
        List<PersonDocumentEmploymentInfo> empInfos = new ArrayList<>();
        for (PersonDocumentAffiliation affiliation : getAffiliations()) {
            empInfos.addAll(affiliation.getEmpInfos());
        }
        managedLists.add(empInfos);
        managedLists.add(getAffiliations());
        managedLists.add(getCitizenships());
        managedLists.add(getPhones());
        managedLists.add(getAddrs());
        managedLists.add(getEmails());
        managedLists.add(getNames());
        managedLists.add(getGroups());
        managedLists.add(getRoles());
        return managedLists;
    }

    @Override
    public void doRouteStatusChange(DocumentRouteStatusChange statusChangeEvent) {
        super.doRouteStatusChange(statusChangeEvent);
        if (getDocumentHeader().getWorkflowDocument().isProcessed()) {
            setIfRolesEditable();
            // CU Customization: Added group-editable flag updates.
            setIfGroupsEditable();
            KIMServiceLocatorInternal.getUiDocumentService().saveEntityPerson(this);
        }
    }

    @Override
    public void prepareForSave() {
        if (StringUtils.isBlank(getPrivacy().getDocumentNumber())) {
            getPrivacy().setDocumentNumber(getDocumentNumber());
        }
        setEmployeeRecordIds();
        for (PersonDocumentRole role : getRoles()) {
            role.setDocumentNumber(getDocumentNumber());
            for (KimDocumentRoleMember rolePrncpl : role.getRolePrncpls()) {
                rolePrncpl.setDocumentNumber(getDocumentNumber());
                rolePrncpl.setRoleId(role.getRoleId());
                if (StringUtils.isEmpty(rolePrncpl.getRoleMemberId())) {
                    DataFieldMaxValueIncrementer incrementer = MaxValueIncrementerFactory.getIncrementer(
                            KimImplServiceLocator.getDataSource(), "KRIM_ROLE_MBR_ID_S");
                    rolePrncpl.setRoleMemberId(incrementer.nextStringValue());
                }
                for (KimDocumentRoleQualifier qualifier : rolePrncpl.getQualifiers()) {
                    qualifier.setDocumentNumber(getDocumentNumber());
                    qualifier.setRoleMemberId(rolePrncpl.getRoleMemberId());
                    qualifier.setKimTypId(role.getKimTypeId());
                }
                for (KimDocumentRoleResponsibilityAction responsibilityAction : rolePrncpl.getRoleRspActions()) {
                    responsibilityAction.setDocumentNumber(getDocumentNumber());
                    responsibilityAction.setRoleMemberId(rolePrncpl.getRoleMemberId());
                    responsibilityAction.setRoleResponsibilityId("*");
                }
            }
        }
        if (getDelegationMembers() != null) {
            for (RoleDocumentDelegationMember delegationMember : getDelegationMembers()) {
                delegationMember.setDocumentNumber(getDocumentNumber());
                for (RoleDocumentDelegationMemberQualifier qualifier : delegationMember.getQualifiers()) {
                    qualifier.setDocumentNumber(getDocumentNumber());
                    qualifier.setKimTypId(delegationMember.getRoleBo().getKimTypeId());
                }
                addDelegationMemberToDelegation(delegationMember);
            }
        }
        // important to do this after getDelegationMembers since the addDelegationMemberToDelegation method will create
        // primary and/or secondary delegations for us in a "just-in-time" fashion
        if (getDelegations() != null) {
            List<RoleDocumentDelegation> emptyDelegations = new ArrayList<>();
            for (RoleDocumentDelegation delegation : getDelegations()) {
                delegation.setDocumentNumber(getDocumentNumber());
                if (delegation.getMembers().isEmpty()) {
                    emptyDelegations.add(delegation);
                }
            }
            // remove any empty delegations because we just don't need them
            getDelegations().removeAll(emptyDelegations);
        }
        if (getAddrs() != null) {
            for (PersonDocumentAddress address : getAddrs()) {
                address.setDocumentNumber(getDocumentNumber());
                if (StringUtils.isEmpty(address.getEntityAddressId())) {
                    DataFieldMaxValueIncrementer incrementer = MaxValueIncrementerFactory.getIncrementer(
                            KimImplServiceLocator.getDataSource(), "KRIM_ENTITY_ADDR_ID_S");
                    address.setEntityAddressId(incrementer.nextStringValue());
                }
            }
        }
        if (getAffiliations() != null) {
            String nextValue = null;

            for (PersonDocumentAffiliation affiliation : getAffiliations()) {
                affiliation.setDocumentNumber(getDocumentNumber());
                if (StringUtils.isEmpty(affiliation.getEntityAffiliationId())) {
                    DataFieldMaxValueIncrementer incrementer = MaxValueIncrementerFactory.getIncrementer(
                            KimImplServiceLocator.getDataSource(), "KRIM_ENTITY_AFLTN_ID_S");
                    nextValue = incrementer.nextStringValue();
                    affiliation.setEntityAffiliationId(nextValue);
                }
                for (PersonDocumentEmploymentInfo empInfo : affiliation.getEmpInfos()) {
                    empInfo.setDocumentNumber(getDocumentNumber());
                    if (StringUtils.isEmpty(empInfo.getEntityAffiliationId())) {
                        empInfo.setEntityAffiliationId(nextValue);
                    }
                }
            }
        }
        if (getEmails() != null) {
            for (PersonDocumentEmail email : getEmails()) {
                email.setDocumentNumber(getDocumentNumber());
                if (StringUtils.isEmpty(email.getEntityEmailId())) {
                    DataFieldMaxValueIncrementer incrementer = MaxValueIncrementerFactory.getIncrementer(
                            KimImplServiceLocator.getDataSource(), "KRIM_ENTITY_EMAIL_ID_S");
                    email.setEntityEmailId(incrementer.nextStringValue());
                }
            }
        }
        if (getGroups() != null) {
            for (PersonDocumentGroup group : getGroups()) {
                group.setDocumentNumber(getDocumentNumber());
                if (StringUtils.isEmpty(group.getGroupMemberId())) {
                    DataFieldMaxValueIncrementer incrementer = MaxValueIncrementerFactory.getIncrementer(
                            KimImplServiceLocator.getDataSource(), "KRIM_GRP_MBR_ID_S");
                    group.setGroupMemberId(incrementer.nextStringValue());
                }
            }
        }
        if (getNames() != null) {
            for (PersonDocumentName name : getNames()) {
                name.setDocumentNumber(getDocumentNumber());
                if (StringUtils.isEmpty(name.getEntityNameId())) {
                    DataFieldMaxValueIncrementer incrementer = MaxValueIncrementerFactory.getIncrementer(
                            KimImplServiceLocator.getDataSource(), "KRIM_ENTITY_NM_ID_S");
                    name.setEntityNameId(incrementer.nextStringValue());
                }
            }
        }
        if (getPhones() != null) {
            for (PersonDocumentPhone phone : getPhones()) {
                phone.setDocumentNumber(getDocumentNumber());
                if (StringUtils.isEmpty(phone.getEntityPhoneId())) {
                    DataFieldMaxValueIncrementer incrementer = MaxValueIncrementerFactory.getIncrementer(
                            KimImplServiceLocator.getDataSource(), "KRIM_ENTITY_PHONE_ID_S");
                    phone.setEntityPhoneId(incrementer.nextStringValue());
                }
            }
        }
    }

    @Override
    public void postProcessSave(KualiDocumentEvent event) {
        super.postProcessSave(event);
        // after the save has completed, we want to restore any potentially @Transient state that JPA might have
        // discarded, specifically the delegation members have a lot of this
        resyncTransientState();
    }

    public void resyncTransientState() {
        getDelegationMembers().clear();
        for (RoleDocumentDelegation delegation : getDelegations()) {
            for (RoleDocumentDelegationMember delegationMember : delegation.getMembers()) {

                // RoleDocumentDelegationMember has a number of transient fields that are derived from the role member,
                // we must populate them in order for the person document to work properly when loading an existing
                // person document

                RoleMemberBo roleMember = getUiDocumentService().getRoleMember(delegationMember.getRoleMemberId());
                delegationMember.setRoleMemberMemberId(roleMember.getMemberId());
                delegationMember.setRoleMemberMemberTypeCode(roleMember.getType().getCode());
                delegationMember.setRoleMemberName(getUiDocumentService().getMemberName(
                        MemberType.fromCode(delegationMember.getRoleMemberMemberTypeCode()),
                        delegationMember.getRoleMemberMemberId()));
                delegationMember.setRoleMemberNamespaceCode(getUiDocumentService().getMemberNamespaceCode(
                        MemberType.fromCode(delegationMember.getRoleMemberMemberTypeCode()),
                        delegationMember.getRoleMemberMemberId()));
                delegationMember.setDelegationTypeCode(delegation.getDelegationTypeCode());
                Role role = KimApiServiceLocator.getRoleService().getRole(roleMember.getRoleId());
                delegationMember.setRoleBo(RoleBo.from(role));

                // don't want to be able to "delete" existing delegation members from the person document, so we
                // indicate that we are editing the delegation member, which we are
                delegationMember.setEdit(true);

                getDelegationMembers().add(delegationMember);
            }
        }
    }

    protected void setEmployeeRecordIds() {
        List<EntityEmployment> empInfos = getUiDocumentService().getEntityEmploymentInformationInfo(getEntityId());
        for (PersonDocumentAffiliation affiliation : getAffiliations()) {
            int employeeRecordCounter = CollectionUtils.isEmpty(empInfos) ? 0 : empInfos.size();
            for (PersonDocumentEmploymentInfo empInfo : affiliation.getEmpInfos()) {
                if (CollectionUtils.isNotEmpty(empInfos)) {
                    for (EntityEmployment origEmpInfo : empInfos) {
                        if (origEmpInfo.getId().equals(empInfo.getEntityEmploymentId())) {
                            empInfo.setEmploymentRecordId(origEmpInfo.getEmploymentRecordId());
                        }
                    }
                }
                if (StringUtils.isEmpty(empInfo.getEmploymentRecordId())) {
                    employeeRecordCounter++;
                    empInfo.setEmploymentRecordId(employeeRecordCounter + "");
                }
            }
        }
    }

    public KimTypeAttributesHelper getKimTypeAttributesHelper(String roleId) {
        Role role = KimApiServiceLocator.getRoleService().getRole(roleId);
        KimType kimTypeInfo = KimApiServiceLocator.getKimTypeInfoService().getKimType(role.getKimTypeId());
        return new KimTypeAttributesHelper(kimTypeInfo);
    }

    public void setIfRolesEditable() {
        if (CollectionUtils.isNotEmpty(getRoles())) {
            for (PersonDocumentRole role : getRoles()) {
                role.setEditable(validAssignRole(role));
            }
        }
    }

    // CU Customization: Added method for setting group-editable flags.
    public void setIfGroupsEditable() {
        if (CollectionUtils.isNotEmpty(getGroups())) {
            for (PersonDocumentGroup group : getGroups()) {
                group.setEditable(validAssignGroup(group));
            }
        }
    }

    public boolean validAssignRole(PersonDocumentRole role) {
        boolean rulePassed = true;
        if (StringUtils.isNotEmpty(role.getNamespaceCode())) {
            Map<String, String> additionalPermissionDetails = new HashMap<>();
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

    // CU Customization: Added method for checking group editability.
    public boolean validAssignGroup(PersonDocumentGroup group) {
        boolean rulePassed = true;
        if (StringUtils.isNotEmpty(group.getNamespaceCode())) {
            Map<String, String> additionalPermissionDetails = new HashMap<>();
            additionalPermissionDetails.put(KimConstants.AttributeConstants.NAMESPACE_CODE, group.getNamespaceCode());
            additionalPermissionDetails.put(KimConstants.AttributeConstants.GROUP_NAME, group.getGroupName());
            if (!getDocumentHelperService().getDocumentAuthorizer(this).isAuthorizedByTemplate(this,
                    KimConstants.NAMESPACE_CODE, KimConstants.PermissionTemplateNames.POPULATE_GROUP,
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
        return this.documentHelperService;
    }

    protected UiDocumentService getUiDocumentService() {
        if (uiDocumentService == null) {
            uiDocumentService = KIMServiceLocatorInternal.getUiDocumentService();
        }
        return this.uiDocumentService;
    }
}
