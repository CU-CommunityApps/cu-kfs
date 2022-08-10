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
package org.kuali.kfs.kim.impl.permission;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.cache.CacheKeyUtils;
import org.kuali.kfs.core.api.criteria.CriteriaLookupService;
import org.kuali.kfs.core.api.criteria.GenericQueryResults;
import org.kuali.kfs.core.api.criteria.LookupCustomizer;
import org.kuali.kfs.core.api.criteria.PredicateFactory;
import org.kuali.kfs.core.api.criteria.QueryByCriteria;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.permission.PermissionService;
import org.kuali.kfs.kim.api.role.RoleMembership;
import org.kuali.kfs.kim.api.role.RoleService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.api.type.KimTypeInfoService;
import org.kuali.kfs.kim.framework.permission.PermissionTypeService;
import org.kuali.kfs.kim.impl.common.attribute.AttributeTransform;
import org.kuali.kfs.kim.impl.common.attribute.KimAttributeData;
import org.kuali.kfs.kim.impl.common.template.Template;
import org.kuali.kfs.kim.impl.identity.principal.Principal;
import org.kuali.kfs.kim.impl.role.RolePermission;
import org.kuali.kfs.kim.impl.type.KimType;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.support.NoOpCacheManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/* Cornell Customization: backport redis*/
public class PermissionServiceImpl implements PermissionService {

    private static final Logger LOG = LogManager.getLogger();
    private final CopyOnWriteArrayList<Template> allTemplates = new CopyOnWriteArrayList<>();
    private RoleService roleService;
    private PermissionTypeService defaultPermissionTypeService;
    private KimTypeInfoService kimTypeInfoService;
    private BusinessObjectService businessObjectService;
    private CriteriaLookupService criteriaLookupService;
    private CacheManager cacheManager;

    public PermissionServiceImpl() {
        this.cacheManager = new NoOpCacheManager();
    }

    protected PermissionTypeService getPermissionTypeService(PermissionTemplate permissionTemplate) {
        if (permissionTemplate == null) {
            throw new IllegalArgumentException("permissionTemplate may not be null");
        }
        KimType kimType = kimTypeInfoService.getKimType(permissionTemplate.getKimTypeId());
        String serviceName = kimType.getServiceName();
        // if no service specified, return a default implementation
        if (StringUtils.isBlank(serviceName)) {
            return defaultPermissionTypeService;
        }
        try {
            PermissionTypeService service = SpringContext.getBean(PermissionTypeService.class, serviceName);
            // if we have a service name, it must exist
            if (service == null) {
                throw new RuntimeException("null returned for permission type service for service name: " +
                        serviceName);
            }
            return service;
        } catch (Exception ex) {
            // sometimes service locators throw exceptions rather than returning null, handle that
            throw new RuntimeException("Error retrieving service: " + serviceName + " from the SpringContext.", ex);
        }
    }

    @Override
    public boolean hasPermission(String principalId, String namespaceCode,
            String permissionName) throws IllegalArgumentException {
        incomingParamCheck(principalId, "principalId");
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(permissionName, "permissionName");

        return isAuthorized(principalId, namespaceCode, permissionName, Collections.emptyMap());
    }

    @Override
    public boolean isAuthorized(String principalId, String namespaceCode,
            String permissionName, Map<String, String> qualification) throws IllegalArgumentException {
        incomingParamCheck(principalId, "principalId");
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(permissionName, "permissionName");
        incomingParamCheck(qualification, "qualification");

        if (LOG.isDebugEnabled()) {
            logAuthorizationCheck(principalId, namespaceCode, permissionName, qualification);
        }

        List<String> roleIds = getRoleIdsForPermission(namespaceCode, permissionName);
        if (roleIds.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Result: false");
            }
            return false;
        }

        boolean isAuthorized = roleService.principalHasRole(principalId, roleIds, qualification);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Result: " + isAuthorized);
        }
        return isAuthorized;

    }

    @Override
    public boolean hasPermissionByTemplate(String principalId, String namespaceCode, String permissionTemplateName,
            Map<String, String> permissionDetails) throws IllegalArgumentException {
        incomingParamCheck(principalId, "principalId");
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(permissionTemplateName, "permissionTemplateName");

        return isAuthorizedByTemplate(principalId, namespaceCode, permissionTemplateName, permissionDetails,
                Collections.emptyMap());
    }

    @Override
    public boolean isAuthorizedByTemplate(String principalId, String namespaceCode, String permissionTemplateName,
            Map<String, String> permissionDetails, Map<String, String> qualification) throws
            IllegalArgumentException {
        incomingParamCheck(principalId, "principalId");
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(permissionTemplateName, "permissionTemplateName");
        incomingParamCheck(qualification, "qualification");

        if (LOG.isDebugEnabled()) {
            logAuthorizationCheckByTemplate(principalId, namespaceCode, permissionTemplateName,
                    permissionDetails, qualification);
        }

        List<String> roleIds = getRoleIdsForPermissionTemplate(namespaceCode, permissionTemplateName,
                permissionDetails);
        if (roleIds.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Result: false");
            }
            return false;
        }
        boolean isAuthorized = roleService.principalHasRole(principalId, roleIds, qualification);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Result: " + isAuthorized);
        }
        return isAuthorized;

    }

    @Override
    public List<Permission> getAuthorizedPermissionsByTemplate(String principalId, String namespaceCode,
                                                               String permissionTemplateName, Map<String, String> permissionDetails, Map<String, String> qualification)
            throws IllegalArgumentException {
        incomingParamCheck(principalId, "principalId");
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(permissionTemplateName, "permissionTemplateName");
        incomingParamCheck(qualification, "qualification");

        // get all the permission objects whose name match that requested
        List<Permission> permissions = getPermissionsByTemplateName(namespaceCode, permissionTemplateName);
        // now, filter the full list by the detail passed
        List<Permission> applicablePermissions = getMatchingPermissions(permissions, permissionDetails);

        return getPermissionsForUser(principalId, applicablePermissions, qualification);
    }

    /**
     * Checks the list of permissions against the principal's roles and returns a subset of the list which match.
     */
    protected List<Permission> getPermissionsForUser(String principalId, List<Permission> permissions,
                                                     Map<String, String> qualification) {
        List<Permission> results = new ArrayList<>();
        for (Permission perm : permissions) {
            List<String> roleIds = getRoleIdsForPermissions(Collections.singletonList(perm));
            if (roleIds != null && !roleIds.isEmpty()) {
                if (roleService.principalHasRole(principalId, roleIds, qualification)) {
                    results.add(perm);
                }
            }
        }
        return Collections.unmodifiableList(results);
    }

    protected Map<String, PermissionTypeService> getPermissionTypeServicesByTemplateId(
            Collection<Permission> permissions) {
        Map<String, PermissionTypeService> permissionTypeServices = new HashMap<>(permissions.size());
        for (Permission perm : permissions) {
            if (!permissionTypeServices.containsKey(perm.getTemplate().getId())) {
                permissionTypeServices.put(perm.getTemplate().getId(), getPermissionTypeService(perm.getTemplate()));
            }
        }
        return permissionTypeServices;
    }

    protected Map<String, List<Permission>> groupPermissionsByTemplate(Collection<Permission> permissions) {
        Map<String, List<Permission>> results = new HashMap<>();
        for (Permission perm : permissions) {
            List<Permission> perms = results.computeIfAbsent(perm.getTemplate().getId(), k -> new ArrayList<>());
            perms.add(perm);
        }
        return results;
    }

    /**
     * Compare each of the passed in permissions with the given permissionDetails. Those that match are added to the
     * result list.
     */
    protected List<Permission> getMatchingPermissions(List<Permission> permissions,
                                                      Map<String, String> permissionDetails) {
        List<String> permissionIds = new ArrayList<>(permissions.size());
        for (Permission permission : permissions) {
            permissionIds.add(permission.getId());
        }
        String cacheKey = "{getMatchingPermissions}permissionIds=" + CacheKeyUtils.key(permissionIds) + "|" +
                "permissionDetails=" + CacheKeyUtils.mapKey(permissionDetails);
        Cache.ValueWrapper cachedValue = cacheManager.getCache(Permission.CACHE_NAME).get(cacheKey);
        if (cachedValue != null && cachedValue.get() instanceof List) {
            return (List<Permission>) cachedValue.get();
        }

        List<Permission> applicablePermissions = new ArrayList<>();
        if (permissionDetails == null || permissionDetails.isEmpty()) {
            // if no details passed, assume that all match
            applicablePermissions.addAll(permissions);
        } else {
            // otherwise, attempt to match the permission details
            // build a map of the template IDs to the type services
            Map<String, PermissionTypeService> permissionTypeServices =
                    getPermissionTypeServicesByTemplateId(permissions);
            // build a map of permissions by template ID
            Map<String, List<Permission>> permissionMap = groupPermissionsByTemplate(permissions);
            // loop over the different templates, matching all of the same template against the type
            // service at once
            for (Map.Entry<String, List<Permission>> entry : permissionMap.entrySet()) {
                PermissionTypeService permissionTypeService = permissionTypeServices.get(entry.getKey());
                List<Permission> permissionList = entry.getValue();
                applicablePermissions.addAll(permissionTypeService.getMatchingPermissions(permissionDetails,
                        permissionList));
            }
        }
        applicablePermissions = Collections.unmodifiableList(applicablePermissions);
        cacheManager.getCache(Permission.CACHE_NAME).put(cacheKey, applicablePermissions);
        return applicablePermissions;
    }

    @Override
    public List<RoleMembership> getRoleMembershipByTemplate(String namespaceCode, String permissionTemplateName,
            Map<String, String> permissionDetails, Map<String, String> qualification) throws
            IllegalArgumentException {
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(permissionTemplateName, "permissionTemplateName");
        incomingParamCheck(qualification, "qualification");

        List<String> roleIds = getRoleIdsForPermissionTemplate(namespaceCode, permissionTemplateName,
                permissionDetails);
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(roleService.getRoleMembers(roleIds, qualification));
    }

    @Cacheable(value = Permission.CACHE_NAME,
            key = "'{isPermissionDefinedByTemplate}' + 'namespaceCode=' + #p0 + '|' + 'permissionTemplateName=' + " +
                    "#p1 + '|' + 'permissionDetails=' + T(org.kuali.kfs.core.api.cache.CacheKeyUtils).mapKey(#p2)")
    @Override
    public boolean isPermissionDefinedByTemplate(String namespaceCode, String permissionTemplateName,
            Map<String, String> permissionDetails) throws IllegalArgumentException {

        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(permissionTemplateName, "permissionTemplateName");

        // get all the permission objects whose name match that requested
        List<Permission> permissions = getPermissionsByTemplateName(namespaceCode, permissionTemplateName);
        // now, filter the full list by the detail passed
        return !getMatchingPermissions(permissions, permissionDetails).isEmpty();
    }

    @Cacheable(value = Permission.CACHE_NAME, key = "'{RoleIds}namespaceCode=' + #p0 + '|' + 'name=' + #p1")
    @Override
    public List<String> getRoleIdsForPermission(String namespaceCode, String permissionName) throws
            IllegalArgumentException {
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(permissionName, "permissionName");
        // note...this method is cached at the RoleService interface level using an annotation, but it's called quite
        // a bit internally, so we'll reproduce the caching here using the same key to help optimize
        String cacheKey = "{RoleIds}namespaceCode=" + namespaceCode + "|name=" + permissionName;
        Cache.ValueWrapper cachedValue = cacheManager.getCache(Permission.CACHE_NAME).get(cacheKey);
        if (cachedValue != null && cachedValue.get() instanceof List) {
            return (List<String>) cachedValue.get();
        }
        // get all the permission objects whose name match that requested
        List<Permission> permissions = getPermissionsByName(namespaceCode, permissionName);
        // now, filter the full list by the detail passed
        List<Permission> applicablePermissions = getMatchingPermissions(permissions, null);
        List<String> roleIds = getRoleIdsForPermissions(applicablePermissions);
        cacheManager.getCache(Permission.CACHE_NAME).put(cacheKey, roleIds);
        return roleIds;
    }

    protected List<String> getRoleIdsForPermissionTemplate(String namespaceCode,
            String permissionTemplateName,
            Map<String, String> permissionDetails) {
        String cacheKey = "{getRoleIdsForPermissionTemplate}namespaceCode=" + namespaceCode +
                "|permissionTemplateName=" + permissionTemplateName + "|permissionDetails=" +
                CacheKeyUtils.mapKey(permissionDetails);
        Cache.ValueWrapper cachedValue = cacheManager.getCache(Permission.CACHE_NAME).get(cacheKey);
        if (cachedValue != null && cachedValue.get() instanceof List) {
            return (List<String>) cachedValue.get();
        }
        // get all the permission objects whose name match that requested
        List<Permission> permissions = getPermissionsByTemplateName(namespaceCode, permissionTemplateName);
        // now, filter the full list by the detail passed
        List<Permission> applicablePermissions = getMatchingPermissions(permissions, permissionDetails);
        List<String> roleIds = getRoleIdsForPermissions(applicablePermissions);
        cacheManager.getCache(Permission.CACHE_NAME).put(cacheKey, roleIds);
        return roleIds;
    }

    @Cacheable(value = Permission.CACHE_NAME, key = "'{getPermission}-id=' + #p0")
    @Override
    public Permission getPermission(String permissionId) throws IllegalArgumentException {
        incomingParamCheck(permissionId, "permissionId");
        return getPermissionImpl(permissionId);
    }

    @Cacheable(value = Permission.CACHE_NAME, key = "'{findPermissionsByTemplate}-namespaceCode=' + #p1 + '|' + 'templateName=' + #p2")
    @Override
    public List<Permission> findPermissionsByTemplate(String namespaceCode, String permissionTemplateName) throws
            IllegalArgumentException {
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(permissionTemplateName, "permissionTemplateName");

        List<Permission> perms = getPermissionsByTemplateName(namespaceCode, permissionTemplateName);
        List<Permission> results = new ArrayList<>(perms.size());
        results.addAll(perms);
        return Collections.unmodifiableList(results);
    }

    protected Permission getPermissionImpl(String permissionId) throws IllegalArgumentException {
        incomingParamCheck(permissionId, "permissionId");

        HashMap<String, Object> pk = new HashMap<>(1);
        pk.put(KimConstants.PrimaryKeyConstants.PERMISSION_ID, permissionId);
        return businessObjectService.findByPrimaryKey(Permission.class, pk);
    }

    protected List<Permission> getPermissionsByTemplateName(String namespaceCode, String permissionTemplateName) {
        String cacheKey = "{getPermissionsByTemplateName}namespaceCode=" + namespaceCode +
                "|permissionTemplateName=" + permissionTemplateName;
        Cache.ValueWrapper cachedValue = cacheManager.getCache(Permission.CACHE_NAME).get(cacheKey);
        if (cachedValue != null && cachedValue.get() instanceof List) {
            return (List<Permission>) cachedValue.get();
        }
        HashMap<String, Object> criteria = new HashMap<>(3);
        criteria.put("template.namespaceCode", namespaceCode);
        criteria.put("template.name", permissionTemplateName);
        criteria.put(KRADPropertyConstants.ACTIVE, "Y");
        List<Permission> permissions = new ArrayList<>(businessObjectService.findMatching(Permission.class,
                criteria));
        cacheManager.getCache(Permission.CACHE_NAME).put(cacheKey, permissions);
        return permissions;
    }

    protected List<Permission> getPermissionsByName(String namespaceCode, String permissionName) {
        String cacheKey = "{getPermissionsByName}namespaceCode=" + namespaceCode + "|permissionName=" +
                permissionName;
        Cache.ValueWrapper cachedValue = cacheManager.getCache(Permission.CACHE_NAME).get(cacheKey);
        if (cachedValue != null && cachedValue.get() instanceof List) {
            return (List<Permission>) cachedValue.get();
        }
        HashMap<String, Object> criteria = new HashMap<>(3);
        criteria.put(KimConstants.UniqueKeyConstants.NAMESPACE_CODE, namespaceCode);
        criteria.put(KimConstants.UniqueKeyConstants.PERMISSION_NAME, permissionName);
        criteria.put(KRADPropertyConstants.ACTIVE, "Y");
        List<Permission> permissions = new ArrayList<>(businessObjectService.findMatching(Permission.class,
                criteria));
        cacheManager.getCache(Permission.CACHE_NAME).put(cacheKey, permissions);
        return permissions;
    }

    @Cacheable(value = PermissionTemplate.CACHE_NAME, key = "'{getPermissionTemplate}id=' + #p0")
    @Override
    public PermissionTemplate getPermissionTemplate(String permissionTemplateId) throws IllegalArgumentException {
        incomingParamCheck(permissionTemplateId, "permissionTemplateId");

        return businessObjectService.findBySinglePrimaryKey(PermissionTemplate.class, permissionTemplateId);
    }

    @Cacheable(value = PermissionTemplate.CACHE_NAME, key = "'{findPermTemplateByNamespaceCodeAndName}|namespaceCode=' + #p0 + '|' + 'name=' + #p1")
    @Override
    public PermissionTemplate findPermTemplateByNamespaceCodeAndName(String namespaceCode,
                                                                     String permissionTemplateName) throws IllegalArgumentException {
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(permissionTemplateName, "permissionTemplateName");

        Map<String, String> criteria = new HashMap<>(2);
        criteria.put(KimConstants.UniqueKeyConstants.NAMESPACE_CODE, namespaceCode);
        criteria.put(KimConstants.UniqueKeyConstants.PERMISSION_TEMPLATE_NAME, permissionTemplateName);
        return businessObjectService.findByPrimaryKey(PermissionTemplate.class, criteria);
    }

    @Cacheable(value = PermissionTemplate.CACHE_NAME, key = "'all'")
    @Override
    public List<Template> getAllTemplates() {
        if (allTemplates.isEmpty()) {
            Map<String, String> criteria = new HashMap<>(1);
            criteria.put(KRADPropertyConstants.ACTIVE, "Y");
            List<PermissionTemplate> impls = (List<PermissionTemplate>) businessObjectService.findMatching(
                    PermissionTemplate.class, criteria);
            impls.sort(Comparator.comparing(Template::getNamespaceCode).thenComparing(Template::getName));
            allTemplates.addAll(impls);
        }
        return Collections.unmodifiableList(allTemplates);
    }

    @CacheEvict(value = {Permission.CACHE_NAME, PermissionTemplate.CACHE_NAME}, allEntries = true)
    @Override
    public Permission createPermission(Permission permission) throws IllegalArgumentException,
            IllegalStateException {
        incomingParamCheck(permission, "permission");

        if (StringUtils.isNotBlank(permission.getId()) && getPermission(permission.getId()) != null) {
            throw new IllegalStateException("the permission to create already exists: " + permission);
        }
        List<PermissionAttribute> attrBos = Collections.emptyList();
        if (permission.getTemplate() != null) {
            attrBos = KimAttributeData.createFrom(PermissionAttribute.class,
                    permission.getAttributes(), permission.getTemplate().getKimTypeId());
        }
        if (permission.getTemplate() == null && permission.getTemplateId() != null) {
            permission.setTemplate(getPermissionTemplate(permission.getTemplateId()));
        }
        permission.setAttributeDetails(attrBos);
        return businessObjectService.save(permission);
    }

    @CacheEvict(value = {Permission.CACHE_NAME, PermissionTemplate.CACHE_NAME}, allEntries = true)
    @Override
    public Permission updatePermission(Permission permission) throws IllegalArgumentException,
            IllegalStateException {
        incomingParamCheck(permission, "permission");

        Permission oldPermission = getPermissionImpl(permission.getId());
        if (StringUtils.isBlank(permission.getId()) || oldPermission == null) {
            throw new IllegalStateException("the permission does not exist: " + permission);
        }

        List<PermissionAttribute> oldAttrBos = oldPermission.getAttributeDetails();
        //put old attributes in map for easier updating
        Map<String, PermissionAttribute> oldAttrMap = new HashMap<>();
        for (PermissionAttribute oldAttr : oldAttrBos) {
            oldAttrMap.put(oldAttr.getKimAttribute().getAttributeName(), oldAttr);
        }
        List<PermissionAttribute> newAttrBos = new ArrayList<>();
        for (String key : permission.getAttributes().keySet()) {
            if (oldAttrMap.containsKey(key)) {
                PermissionAttribute updatedAttr = oldAttrMap.get(key);
                updatedAttr.setAttributeValue(permission.getAttributes().get(key));
                newAttrBos.add(updatedAttr);
            } else {
                newAttrBos.addAll(KimAttributeData.createFrom(PermissionAttribute.class,
                        Collections.singletonMap(key, permission.getAttributes().get(key)),
                        permission.getTemplate().getKimTypeId()));
            }
        }
        if (CollectionUtils.isNotEmpty(newAttrBos)) {
            if (null != permission.getAttributeDetails()) {
                permission.getAttributeDetails().clear();
            }
            permission.setAttributeDetails(newAttrBos);
        }
        if (permission.getTemplate() == null && permission.getTemplateId() != null) {
            permission.setTemplate(getPermissionTemplate(permission.getTemplateId()));
        }

        return businessObjectService.save(permission);
    }

    @Cacheable(value = Permission.CACHE_NAME, key = "'{findPermByNamespaceCodeAndName}-namespaceCode=' + #p0 + '|' + 'name=' + #p1")
    @Override
    public Permission findPermByNamespaceCodeAndName(String namespaceCode, String permissionName)
            throws IllegalArgumentException {
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(permissionName, "permissionName");

        return getPermissionByName(namespaceCode, permissionName);
    }

    protected Permission getPermissionByName(String namespaceCode, String permissionName) {
        if (StringUtils.isBlank(namespaceCode) || StringUtils.isBlank(permissionName)) {
            return null;
        }
        Map<String, String> criteria = new HashMap<>();
        criteria.put(KimConstants.UniqueKeyConstants.NAMESPACE_CODE, namespaceCode);
        criteria.put(KimConstants.UniqueKeyConstants.NAME, permissionName);
        criteria.put(KRADPropertyConstants.ACTIVE, "Y");
        // while this is not actually the primary key - there will be at most one row with these criteria
        return businessObjectService.findByPrimaryKey(Permission.class, criteria);
    }

    @Override
    public GenericQueryResults<Permission> findPermissions(final QueryByCriteria queryByCriteria) throws
            IllegalArgumentException {
        incomingParamCheck(queryByCriteria, "queryByCriteria");

        LookupCustomizer.Builder<Permission> lc = LookupCustomizer.Builder.create();
        lc.setPredicateTransform(AttributeTransform.getInstance());

        return criteriaLookupService.lookup(Permission.class, queryByCriteria, lc.build());
    }

    private List<String> getRoleIdsForPermissions(Collection<Permission> permissions) {
        if (CollectionUtils.isEmpty(permissions)) {
            return Collections.emptyList();
        }
        List<String> ids = new ArrayList<>();
        for (Permission p : permissions) {
            ids.add(p.getId());
        }

        return getRoleIdsForPermissionIds(ids);
    }

    private List<String> getRoleIdsForPermissionIds(Collection<String> permissionIds) {
        if (CollectionUtils.isEmpty(permissionIds)) {
            return Collections.emptyList();
        }
        String cacheKey = "{getRoleIdsForPermissionIds}permissionIds=" + CacheKeyUtils.key(permissionIds);
        Cache.ValueWrapper cachedValue = cacheManager.getCache(Permission.CACHE_NAME).get(cacheKey);
        if (cachedValue != null && cachedValue.get() instanceof List) {
            return (List<String>) cachedValue.get();
        }
        QueryByCriteria query = QueryByCriteria.Builder.fromPredicates(PredicateFactory.equal("active", "true"),
                PredicateFactory.in("permissionId", permissionIds.toArray(new String[]{})));
        GenericQueryResults<RolePermission> results = criteriaLookupService.lookup(RolePermission.class, query);
        List<String> roleIds = new ArrayList<>();
        for (RolePermission bo : results.getResults()) {
            roleIds.add(bo.getRoleId());
        }
        roleIds = Collections.unmodifiableList(roleIds);
        cacheManager.getCache(Permission.CACHE_NAME).put(cacheKey, roleIds);
        return roleIds;
    }

    public void setKimTypeInfoService(KimTypeInfoService kimTypeInfoService) {
        this.kimTypeInfoService = kimTypeInfoService;
    }

    public void setDefaultPermissionTypeService(PermissionTypeService defaultPermissionTypeService) {
        this.defaultPermissionTypeService = defaultPermissionTypeService;
    }

    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setCriteriaLookupService(final CriteriaLookupService criteriaLookupService) {
        this.criteriaLookupService = criteriaLookupService;
    }

    /**
     * Sets the cache manager which this service implementation can for internal caching. Calling this setter is
     * optional, though the value passed to it must not be null.
     *
     * @param cacheManager the cache manager to use for internal caching, must not be null
     * @throws IllegalArgumentException if a null cache manager is passed
     */
    public void setCacheManager(final CacheManager cacheManager) {
        if (cacheManager == null) {
            throw new IllegalArgumentException("cacheManager must not be null");
        }
        this.cacheManager = cacheManager;
    }

    protected void logAuthorizationCheck(String principalId, String namespaceCode,
            String permissionName, Map<String, String> qualification) {
        StringBuilder sb = new StringBuilder();
        sb.append('\n');
        sb.append("Is AuthZ for ").append("Permission").append(": ").append(namespaceCode).append("/")
                .append(permissionName).append('\n');
        sb.append("             Principal:  ").append(principalId);
        if (principalId != null) {
            Principal principal = KimApiServiceLocator.getIdentityService().getPrincipal(principalId);
            if (principal != null) {
                sb.append(" (").append(principal.getPrincipalName()).append(')');
            }
        }
        sb.append('\n');
        sb.append("             Qualifiers:\n");
        if (qualification != null && !qualification.isEmpty()) {
            sb.append(qualification);
        } else {
            sb.append("                         [null]\n");
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace(sb.append(ExceptionUtils.getStackTrace(new Throwable())));
        } else {
            LOG.debug(sb.toString());
        }
    }

    protected void logAuthorizationCheckByTemplate(String principalId, String namespaceCode,
            String permissionName,
            Map<String, String> permissionDetails, Map<String, String> qualification) {
        StringBuilder sb = new StringBuilder();
        sb.append('\n');
        sb.append("Is AuthZ for ").append("Perm Templ").append(": ").append(namespaceCode).append("/")
                .append(permissionName).append('\n');
        sb.append("             Principal:  ").append(principalId);
        if (principalId != null) {
            Principal principal = KimApiServiceLocator.getIdentityService().getPrincipal(principalId);
            if (principal != null) {
                sb.append(" (").append(principal.getPrincipalName()).append(')');
            }
        }
        sb.append('\n');
        sb.append("             Details:\n");
        if (permissionDetails != null) {
            sb.append(permissionDetails);
        } else {
            sb.append("                         [null]\n");
        }
        sb.append("             Qualifiers:\n");
        if (qualification != null && !qualification.isEmpty()) {
            sb.append(qualification);
        } else {
            sb.append("                         [null]\n");
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace(sb.append(ExceptionUtils.getStackTrace(new Throwable())));
        } else {
            LOG.debug(sb.toString());
        }
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
