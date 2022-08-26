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

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.criteria.CriteriaLookupService;
import org.kuali.kfs.core.api.criteria.GenericQueryResults;
import org.kuali.kfs.core.api.criteria.QueryByCriteria;
import org.kuali.kfs.kim.api.identity.IdentityService;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.kim.impl.identity.address.EntityAddressType;
import org.kuali.kfs.kim.impl.identity.affiliation.EntityAffiliationType;
import org.kuali.kfs.kim.impl.identity.email.EntityEmailType;
import org.kuali.kfs.kim.impl.identity.employment.EntityEmployment;
import org.kuali.kfs.kim.impl.identity.employment.EntityEmploymentStatus;
import org.kuali.kfs.kim.impl.identity.employment.EntityEmploymentType;
import org.kuali.kfs.kim.impl.identity.entity.Entity;
import org.kuali.kfs.kim.impl.identity.external.EntityExternalIdentifierType;
import org.kuali.kfs.kim.impl.identity.name.EntityName;
import org.kuali.kfs.kim.impl.identity.name.EntityNameType;
import org.kuali.kfs.kim.impl.identity.phone.EntityPhoneType;
import org.kuali.kfs.kim.impl.identity.principal.Principal;
import org.kuali.kfs.kim.impl.identity.privacy.EntityPrivacyPreferences;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.springframework.cache.annotation.Cacheable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Base implementation of the identity (identity) service. This version assumes the KimEntity and related data is
 * located within the KIM database.
 */
/* Cornell Customization: backport redis */
public class IdentityServiceImpl implements IdentityService {

    private static final String UNAVAILABLE = "Unavailable";
    private CriteriaLookupService criteriaLookupService;
    private BusinessObjectService businessObjectService;

    @Cacheable(cacheNames = Entity.CACHE_NAME, key = "'{getEntity}-id=' + #p0")
    @Override
    public Entity getEntity(String entityId) throws IllegalArgumentException {
        incomingParamCheck(entityId, "entityId");

        Entity entity = businessObjectService.findByPrimaryKey(Entity.class, Collections.singletonMap("id", entityId));
        if (entity == null) {
            return null;
        }
        // Since there is a circular reference between EntityEmployment and EntityAffiliation, we have OJB
        // configured to not automatically load entityAffiliation references when an EntityEmployment is loaded
        // from the db. This will load those references so Employment Information is populated when editing a Person.
        for (EntityEmployment entityEmployment : entity.getEmploymentInformation()) {
            if (ObjectUtils.isNull(entityEmployment.getEntityAffiliation())) {
                entityEmployment.refreshReferenceObject("entityAffiliation");
            }
        }
        return entity;
    }

    @Cacheable(cacheNames = Entity.CACHE_NAME, key = "'{getEntityByPrincipalId}-principalId=' + #p0")
    @Override
    public Entity getEntityByPrincipalId(String principalId) throws IllegalArgumentException {
        incomingParamCheck(principalId, "principalId");

        if (StringUtils.isBlank(principalId)) {
            return null;
        }
        return getEntityByKeyValue("principals." + KIMPropertyConstants.Principal.PRINCIPAL_ID, principalId);
    }

    @Cacheable(cacheNames = Entity.CACHE_NAME, key = "'{getEntityByPrincipalName}-principalName=' + #p0")
    @Override
    public Entity getEntityByPrincipalName(String principalName) throws IllegalArgumentException {
        incomingParamCheck(principalName, "principalName");

        if (StringUtils.isBlank(principalName)) {
            return null;
        }
        return getEntityByKeyValue("LOWER(principals." + KIMPropertyConstants.Principal.PRINCIPAL_NAME + ")",
                principalName.toLowerCase(Locale.US));
    }

    @Cacheable(cacheNames = Entity.CACHE_NAME, key = "'{getEntityByEmployeeId}-employeeId=' + #p0")
    @Override
    public Entity getEntityByEmployeeId(String employeeId) throws IllegalArgumentException {
        incomingParamCheck(employeeId, "employeeId");

        if (StringUtils.isBlank(employeeId)) {
            return null;
        }
        return getEntityByKeyValue("employmentInformation." + KIMPropertyConstants.Person.EMPLOYEE_ID, employeeId);
    }

    @Override
    public GenericQueryResults<Entity> findEntities(QueryByCriteria queryByCriteria) throws
            IllegalArgumentException {
        incomingParamCheck(queryByCriteria, "queryByCriteria");

        return criteriaLookupService.lookup(Entity.class, queryByCriteria);
    }

    @Cacheable(cacheNames = EntityPrivacyPreferences.CACHE_NAME, key = "'getEntityPrivacyPreferences|id=' + #p0")
    @Override
    public EntityPrivacyPreferences getEntityPrivacyPreferences(String entityId) throws IllegalArgumentException {
        incomingParamCheck(entityId, "entityId");
        Map<String, String> criteria = new HashMap<>(1);
        criteria.put(KIMPropertyConstants.Entity.ENTITY_ID, entityId);
        return businessObjectService.findByPrimaryKey(EntityPrivacyPreferences.class, criteria);
    }

    @Cacheable(cacheNames = Principal.CACHE_NAME, key = "'{getPrincipal}-principalId=' + #p0")
    @Override
    public Principal getPrincipal(String principalId) throws IllegalArgumentException {
        incomingParamCheck(principalId, "principalId");

        Map<String, String> criteria = new HashMap<>(1);
        criteria.put(KIMPropertyConstants.Principal.PRINCIPAL_ID, principalId);
        Principal principal = businessObjectService.findByPrimaryKey(Principal.class, criteria);
        if (principal == null) {
            return null;
        }
        if (StringUtils.isBlank(principal.getPrincipalName())) {
            principal.setPrincipalName(UNAVAILABLE);
        }
        return principal;
    }

    @Override
    public List<Principal> getPrincipals(List<String> principalIds) {
        List<Principal> ret = new ArrayList<>();
        for (String p : principalIds) {
            Principal principalInfo = getPrincipal(p);

            if (principalInfo != null) {
                ret.add(principalInfo);
            }
        }
        return ret;
    }

    @Cacheable(cacheNames = Principal.CACHE_NAME, key = "'{getPrincipalByPrincipalName}-principalName=' + #p0")
    @Override
    public Principal getPrincipalByPrincipalName(String principalName) throws IllegalArgumentException {
        incomingParamCheck(principalName, "principalName");

        Map<String, Object> criteria = new HashMap<>(1);
        criteria.put("LOWER(" + KIMPropertyConstants.Principal.PRINCIPAL_NAME + ")",
                principalName.toLowerCase(Locale.US));
        Collection<Principal> principals = businessObjectService.findMatching(Principal.class, criteria);
        if (principals.size() == 1) {
            return principals.iterator().next();
        }
        return null;
    }

    private List<Principal> getPrincipalsByEntityId(String entityId) throws IllegalArgumentException {
        incomingParamCheck(entityId, "entityId");

        Map<String, Object> criteria = new HashMap<>(2);
        criteria.put(KIMPropertyConstants.Person.ENTITY_ID, entityId);
        return new ArrayList<>(businessObjectService.findMatching(Principal.class, criteria));
    }

    @Override
    public List<Principal> getPrincipalsByEmployeeId(String employeeId) throws IllegalArgumentException {
        incomingParamCheck(employeeId, "employeeId");

        List<Principal> principals = new ArrayList<>();
        Map<String, Object> criteria = new HashMap<>(2);
        criteria.put(KIMPropertyConstants.Person.EMPLOYEE_ID, employeeId);
        Collection<EntityEmployment> entityEmployments =
                businessObjectService.findMatching(EntityEmployment.class, criteria);

        if (entityEmployments != null && !entityEmployments.isEmpty()) {
            List<String> entityIds = new ArrayList<>();
            for (EntityEmployment entityEmployment : entityEmployments) {
                String entityId = entityEmployment.getEntityId();
                if (StringUtils.isNotBlank(entityId) && !entityIds.contains(entityId)) {
                    entityIds.add(entityId);
                }
            }

            for (String entityId : entityIds) {
                List<Principal> principalsForEntity = getPrincipalsByEntityId(entityId);
                if (principalsForEntity != null && !principalsForEntity.isEmpty()) {
                    principals.addAll(principalsForEntity);
                }
            }
            if (!principals.isEmpty()) {
                return principals;
            }
        }
        return null;
    }

    /**
     * Generic helper method for performing a lookup through the business object service.
     */
    protected Entity getEntityByKeyValue(String key, String value) {
        Map<String, String> criteria = new HashMap<>(1);
        criteria.put(key, value);
        Collection<Entity> entities = businessObjectService.findMatching(Entity.class, criteria);
        if (entities != null && entities.size() >= 1) {
            return entities.iterator().next();
        }
        return null;
    }

    @Cacheable(cacheNames = CodedAttribute.CACHE_NAME, key = "'{getAddressType}-code=' + #p0")
    @Override
    public EntityAddressType getAddressType(String code) throws IllegalArgumentException {
        incomingParamCheck(code, "code");
        return businessObjectService.findBySinglePrimaryKey(EntityAddressType.class, code);
    }

    @Cacheable(cacheNames = CodedAttribute.CACHE_NAME, key = "'{getAffiliationType}-code=' + #p0")
    @Override
    public EntityAffiliationType getAffiliationType(String code) throws IllegalArgumentException {
        incomingParamCheck(code, "code");

        return businessObjectService.findBySinglePrimaryKey(EntityAffiliationType.class, code);
    }

    @Cacheable(cacheNames = CodedAttribute.CACHE_NAME, key = "'{getEmailType}-code=' + #p0")
    @Override
    public EntityEmailType getEmailType(String code) throws IllegalArgumentException {
        incomingParamCheck(code, "code");
        return businessObjectService.findBySinglePrimaryKey(EntityEmailType.class, code);
    }

    @Override
    public GenericQueryResults<Principal> findPrincipals(QueryByCriteria query) throws IllegalArgumentException {
        incomingParamCheck(query, "query");

        return criteriaLookupService.lookup(Principal.class, query);
    }

    @Cacheable(cacheNames = CodedAttribute.CACHE_NAME, key = "'{getEmploymentStatus}-code=' + #p0")
    @Override
    public EntityEmploymentStatus getEmploymentStatus(String code) throws IllegalArgumentException {
        incomingParamCheck(code, "code");
        return businessObjectService.findBySinglePrimaryKey(EntityEmploymentStatus.class, code);
    }

    @Cacheable(cacheNames = CodedAttribute.CACHE_NAME, key = "'{getEmploymentType}-code=' + #p0")
    @Override
    public EntityEmploymentType getEmploymentType(String code) throws IllegalArgumentException {
        incomingParamCheck(code, "code");
        return businessObjectService.findBySinglePrimaryKey(EntityEmploymentType.class, code);
    }

    @Cacheable(cacheNames = CodedAttribute.CACHE_NAME, key = "'{getNameType}-code=' + #p0")
    @Override
    public EntityNameType getNameType(String code) throws IllegalArgumentException {
        incomingParamCheck(code, "code");
        return businessObjectService.findBySinglePrimaryKey(EntityNameType.class, code);
    }

    @Cacheable(cacheNames = CodedAttribute.CACHE_NAME, key = "'{getExternalIdentifierType}-code=' + #p0")
    @Override
    public EntityExternalIdentifierType getExternalIdentifierType(String code) throws IllegalArgumentException {
        incomingParamCheck(code, "code");

        return businessObjectService.findBySinglePrimaryKey(EntityExternalIdentifierType.class, code);
    }

    @Cacheable(cacheNames = CodedAttribute.CACHE_NAME, key = "'{getPhoneType}-code=' + #p0")
    @Override
    public EntityPhoneType getPhoneType(String code) throws IllegalArgumentException {
        incomingParamCheck(code, "code");
        return businessObjectService.findBySinglePrimaryKey(EntityPhoneType.class, code);
    }

    @Cacheable(cacheNames = EntityName.CACHE_NAME, key = "'{getDefaultNamesForPrincipalId}-principalId=' + #p0")
    @Override
    public EntityName getDefaultNamesForPrincipalId(String principalId) {
        Map<String, String> criteria = new HashMap<>();
        criteria.put(KIMPropertyConstants.Principal.PRINCIPAL_ID, principalId);
        Principal principal = businessObjectService.findByPrimaryKey(Principal.class, criteria);

        if (null != principal) {
            criteria.clear();
            criteria.put(KIMPropertyConstants.Entity.ENTITY_ID, principal.getEntityId());
            criteria.put("DFLT_IND", "Y");
            criteria.put("ACTV_IND", "Y");
            EntityName name = businessObjectService.findByPrimaryKey(EntityName.class, criteria);

            if (name == null) {
                // to make this simple for now, assume if there is no default name that this is a system entity we are
                // dealing with here
                name = new EntityName();
                name.setLastName(principal.getPrincipalName().toUpperCase(Locale.US));
            }
            return name;
        }
        return null;
    }

    public void setCriteriaLookupService(final CriteriaLookupService criteriaLookupService) {
        this.criteriaLookupService = criteriaLookupService;
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    private void incomingParamCheck(Object object, String name) {
        if (object == null) {
            throw new IllegalArgumentException(name + " was null");
        } else if (object instanceof String
                && StringUtils.isBlank((String) object)) {
            throw new IllegalArgumentException(name + " was blank");
        }
    }
}
