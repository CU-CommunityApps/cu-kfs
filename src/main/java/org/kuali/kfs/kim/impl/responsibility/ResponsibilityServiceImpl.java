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
package org.kuali.kfs.kim.impl.responsibility;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.criteria.CriteriaLookupService;
import org.kuali.kfs.core.api.criteria.GenericQueryResults;
import org.kuali.kfs.core.api.criteria.LookupCustomizer;
import org.kuali.kfs.core.api.criteria.Predicate;
import org.kuali.kfs.core.api.criteria.PredicateFactory;
import org.kuali.kfs.core.api.criteria.QueryByCriteria;
import org.kuali.kfs.core.api.membership.MemberType;
import org.kuali.kfs.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kim.api.responsibility.ResponsibilityAction;
import org.kuali.kfs.kim.api.responsibility.ResponsibilityService;
import org.kuali.kfs.kim.api.role.RoleMembership;
import org.kuali.kfs.kim.api.role.RoleService;
import org.kuali.kfs.kim.api.type.KimTypeInfoService;
import org.kuali.kfs.kim.framework.responsibility.ResponsibilityTypeService;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.kim.impl.common.attribute.AttributeTransform;
import org.kuali.kfs.kim.impl.common.attribute.KimAttributeData;
import org.kuali.kfs.kim.impl.common.template.Template;
import org.kuali.kfs.kim.impl.role.RoleResponsibility;
import org.kuali.kfs.kim.impl.role.RoleResponsibilityAction;
import org.kuali.kfs.kim.impl.type.KimType;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.util.CollectionUtils;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

/* Cornell Customization: backport redis*/
public class ResponsibilityServiceImpl implements ResponsibilityService {

    private static final Integer DEFAULT_PRIORITY_NUMBER = 1;
    private static final Logger LOG = LogManager.getLogger();

    private BusinessObjectService businessObjectService;
    private CriteriaLookupService criteriaLookupService;
    private ResponsibilityTypeService defaultResponsibilityTypeService;
    private KimTypeInfoService kimTypeInfoService;
    private RoleService roleService;

    @CacheEvict(value = {Responsibility.CACHE_NAME, ResponsibilityTemplate.CACHE_NAME}, allEntries = true)
    @Override
    public Responsibility createResponsibility(final Responsibility responsibility)
            throws IllegalArgumentException, IllegalStateException {
        incomingParamCheck(responsibility, "responsibility");

        if (StringUtils.isNotBlank(responsibility.getId()) && getResponsibility(responsibility.getId()) != null) {
            throw new IllegalStateException("the responsibility to create already exists: " + responsibility);
        }
        List<ResponsibilityAttribute> attrBos = Collections.emptyList();
        if (responsibility.getTemplate() != null) {
            attrBos = KimAttributeData.createFrom(ResponsibilityAttribute.class, responsibility.getAttributes(),
                    responsibility.getTemplate().getKimTypeId());
        }
        responsibility.setAttributeDetails(attrBos);
        return businessObjectService.save(responsibility);
    }

    @CacheEvict(value = {Responsibility.CACHE_NAME, ResponsibilityTemplate.CACHE_NAME}, allEntries = true)
    @Override
    public Responsibility updateResponsibility(final Responsibility responsibility)
            throws IllegalArgumentException, IllegalStateException {
        incomingParamCheck(responsibility, "responsibility");

        if (StringUtils.isBlank(responsibility.getId()) || getResponsibility(responsibility.getId()) == null) {
            throw new IllegalStateException("the responsibility does not exist: " + responsibility);
        }

        List<ResponsibilityAttribute> attrBos = Collections.emptyList();
        if (responsibility.getTemplate() != null) {
            attrBos = KimAttributeData.createFrom(ResponsibilityAttribute.class, responsibility.getAttributes(),
                    responsibility.getTemplate().getKimTypeId());
        }

        if (responsibility.getAttributeDetails() != null) {
            responsibility.getAttributeDetails().clear();
            responsibility.setAttributeDetails(attrBos);
        }

        return businessObjectService.save(responsibility);
    }

    @Cacheable(cacheNames = Responsibility.CACHE_NAME, key = "'{getResponsibility}|id=' + #p0")
    @Override
    public Responsibility getResponsibility(final String id) throws IllegalArgumentException {
        incomingParamCheck(id, "id");

        return businessObjectService.findBySinglePrimaryKey(Responsibility.class, id);
    }

    @Cacheable(cacheNames = Responsibility.CACHE_NAME, key = "'{findRespByNamespaceCodeAndName}|namespaceCode=' + #p0 + '|' + 'name=' + #p1")
    @Override
    public Responsibility findRespByNamespaceCodeAndName(final String namespaceCode, final String name)
            throws IllegalArgumentException {
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(name, "name");

        final var fieldValues = Map.ofEntries(
                entry("namespaceCode", namespaceCode),
                entry("name", name),
                entry("active", "Y")
        );
        final Collection<Responsibility> bos =
                businessObjectService.findMatching(Responsibility.class, fieldValues);

        if (bos != null) {
            if (bos.size() > 1) {
                throw new IllegalStateException("more than one Responsibility found with namespace code: " +
                        namespaceCode + " and name: " + name);
            }

            final Iterator<Responsibility> i = bos.iterator();
            return i.hasNext() ? i.next() : null;
        }
        return null;
    }

    @Cacheable(cacheNames = ResponsibilityTemplate.CACHE_NAME, key = "'{getResponsibilityTemplate}|id=' + #p0")
    @Override
    public Template getResponsibilityTemplate(final String id) throws IllegalArgumentException {
        incomingParamCheck(id, "id");

        return businessObjectService.findBySinglePrimaryKey(ResponsibilityTemplate.class, id);
    }

    @Cacheable(cacheNames = ResponsibilityTemplate.CACHE_NAME, key = "'{findRespByNamespaceCodeAndName}|namespaceCode=' + #p0 + '|' + 'name=' + #p1")
    @Override
    public Template findRespTemplateByNamespaceCodeAndName(final String namespaceCode, final String name) throws
            IllegalArgumentException {
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(name, "name");

        final var fieldValues = Map.ofEntries(
                entry("namespaceCode", namespaceCode),
                entry("name", name),
                entry("active", "Y")
        );
        final Collection<ResponsibilityTemplate> bos =
                businessObjectService.findMatching(ResponsibilityTemplate.class, fieldValues);
        if (bos != null) {
            if (bos.size() > 1) {
                throw new IllegalStateException("more than one Responsibility Template found with namespace " +
                        "code: " + namespaceCode + " and name: " + name);
            }

            final Iterator<ResponsibilityTemplate> i = bos.iterator();
            return i.hasNext() ? i.next() : null;
        }
        return null;
    }

    @Override
    public List<ResponsibilityAction> getResponsibilityActionsByTemplate(final String namespaceCode,
            final String respTemplateName, final Map<String, String> qualification,
            final Map<String, String> responsibilityDetails) throws IllegalArgumentException {
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(respTemplateName, "respTemplateName");
        incomingParamCheck(qualification, "qualification");

        if (LOG.isDebugEnabled()) {
            logResponsibilityCheck(namespaceCode, respTemplateName, qualification, responsibilityDetails);
        }

        // get all the responsibility objects whose name match that requested
        List<Responsibility> responsibilities = findResponsibilitiesByTemplate(namespaceCode, respTemplateName);
        return getRespActions(responsibilities, qualification, responsibilityDetails);
    }

    private List<ResponsibilityAction> getRespActions(final List<Responsibility> responsibilities,
            final Map<String, String> qualification, final Map<String, String> responsibilityDetails) {
        // now, filter the full list by the detail passed
        List<Responsibility> applicableResponsibilities = getMatchingResponsibilities(responsibilities,
                responsibilityDetails);
        List<ResponsibilityAction> results = new ArrayList<>();
        for (Responsibility r : applicableResponsibilities) {
            List<String> roleIds = getRoleIdsForResponsibility(r.getId());
            results.addAll(getActionsForResponsibilityRoles(r, roleIds, qualification));
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Found " + results.size() + " matching ResponsibilityAction objects");
            if (LOG.isTraceEnabled()) {
                LOG.trace(results);
            }
        }
        return results;
    }

    private List<ResponsibilityAction> getActionsForResponsibilityRoles(Responsibility responsibility,
                                                                        List<String> roleIds, Map<String, String> qualification) {
        List<ResponsibilityAction> results = new ArrayList<>();
        Collection<RoleMembership> roleMembers = roleService.getRoleMembers(roleIds, qualification);
        for (RoleMembership rm : roleMembers) {
            // only add them to the list if the member ID has been populated
            if (StringUtils.isNotBlank(rm.getMemberId())) {
                final ResponsibilityAction.Builder rai = ResponsibilityAction.Builder.create();
                rai.setMemberRoleId(rm.getEmbeddedRoleId() == null ? rm.getRoleId() : rm.getEmbeddedRoleId());
                rai.setRoleId(rm.getRoleId());
                rai.setQualifier(rm.getQualifier());
                rai.setDelegates(rm.getDelegates());
                rai.setResponsibilityId(responsibility.getId());
                rai.setResponsibilityName(responsibility.getName());
                rai.setResponsibilityNamespaceCode(responsibility.getNamespaceCode());

                if (MemberType.PRINCIPAL.equals(rm.getType())) {
                    rai.setPrincipalId(rm.getMemberId());
                } else {
                    rai.setGroupId(rm.getMemberId());
                }
                // get associated resp resolution objects
                RoleResponsibilityAction action = getResponsibilityAction(rm.getRoleId(), responsibility.getId(),
                        rm.getId());
                if (action == null) {
                    LOG.error("Unable to get responsibility action record for role/responsibility/roleMember: "
                            + rm.getRoleId() + "/" + responsibility.getId() + "/" + rm.getId());
                    LOG.error("Skipping this role member in getActionsForResponsibilityRoles()");
                    continue;
                }
                // add the data to the ResponsibilityActionInfo objects
                rai.setActionTypeCode(action.getActionTypeCode());
                rai.setActionPolicyCode(action.getActionPolicyCode());
                rai.setPriorityNumber(action.getPriorityNumber() == null ? DEFAULT_PRIORITY_NUMBER :
                        action.getPriorityNumber());
                rai.setForceAction(action.isForceAction());
                rai.setParallelRoutingGroupingCode(rm.getRoleSortingCode() == null ? "" : rm.getRoleSortingCode());
                rai.setRoleResponsibilityActionId(action.getId());
                results.add(rai.build());
            }
        }
        return Collections.unmodifiableList(results);
    }

    private RoleResponsibilityAction getResponsibilityAction(String roleId, String responsibilityId,
                                                             String roleMemberId) {
        RoleResponsibilityAction result = null;

        // KULRICE-7459: Requisition, PO and its subtype documents are going to final status where they should not.
        //
        // need to do in 2 steps due to "*" wildcard convention in column data for role member id and role
        // responsibility id.  Well, we could do in 1 step w/ straight SQL, but not w/ Criteria API due to the
        // INNER JOIN automatically created between RoleResponsibility and RoleResponsibilityAction tables.

        final Predicate roleResponsibilityPredicate =
                PredicateFactory.and(
                        PredicateFactory.equal("responsibilityId", responsibilityId),
                        PredicateFactory.equal("roleId", roleId),
                        PredicateFactory.equal("active", "Y")
                );

        // First get RoleResponsibilities
        final QueryByCriteria.Builder roleResponsibilityQueryBuilder = QueryByCriteria.Builder.create();
        roleResponsibilityQueryBuilder.setPredicates(roleResponsibilityPredicate);
        final GenericQueryResults<RoleResponsibility> roleResponsibilityResults =
                criteriaLookupService.lookup(RoleResponsibility.class, roleResponsibilityQueryBuilder.build());
        final List<RoleResponsibility> roleResponsibilities = roleResponsibilityResults.getResults();

        if (!CollectionUtils.isEmpty(roleResponsibilities)) {
            // if there are any...
            // Then query RoleResponsibilityActions based on them

            List<String> roleResponsibilityIds = new ArrayList<>(roleResponsibilities.size());
            for (RoleResponsibility roleResponsibility : roleResponsibilities) {
                roleResponsibilityIds.add(roleResponsibility.getRoleResponsibilityId());
            }

            final Predicate roleResponsibilityActionPredicate =
                    PredicateFactory.or(
                            PredicateFactory.and(
                                    PredicateFactory.in("roleResponsibilityId",
                                            roleResponsibilityIds.toArray()),
                                    PredicateFactory.or(
                                            PredicateFactory.equal(org.kuali.kfs.kim.impl.KIMPropertyConstants.RoleMember.ROLE_MEMBER_ID,
                                                    roleMemberId),
                                            PredicateFactory.equal(org.kuali.kfs.kim.impl.KIMPropertyConstants.RoleMember.ROLE_MEMBER_ID,
                                                    "*")
                                    )
                            ),
                            PredicateFactory.and(
                                    PredicateFactory.equal("roleResponsibilityId", "*"),
                                    PredicateFactory.equal(KIMPropertyConstants.RoleMember.ROLE_MEMBER_ID,
                                            roleMemberId)
                            )
                    );

            final QueryByCriteria.Builder roleResponsibilityActionQueryBuilder = QueryByCriteria.Builder.create();
            roleResponsibilityActionQueryBuilder.setPredicates(roleResponsibilityActionPredicate);

            final GenericQueryResults<RoleResponsibilityAction> roleResponsibilityActionResults =
                    criteriaLookupService.lookup(RoleResponsibilityAction.class,
                            roleResponsibilityActionQueryBuilder.build());

            final List<RoleResponsibilityAction> roleResponsibilityActions =
                    roleResponsibilityActionResults.getResults();
            //seems a little dubious that we are just returning the first result...
            if (!roleResponsibilityActions.isEmpty()) {
                result = roleResponsibilityActions.get(0);
            }
        }

        return result;
    }

    @Cacheable(value = Responsibility.CACHE_NAME, key = "'{getRoleIdsForResponsibility}' + 'id=' + #p0")
    @Override
    public List<String> getRoleIdsForResponsibility(String id) throws IllegalArgumentException {
        incomingParamCheck(id, "id");

        return getRoleIdsForPredicate(PredicateFactory.and(PredicateFactory.equal("responsibilityId", id),
                PredicateFactory.equal("active", "Y")));
    }

    @Override
    public GenericQueryResults<Responsibility> findResponsibilities(final QueryByCriteria queryByCriteria) throws
            IllegalArgumentException {
        incomingParamCheck(queryByCriteria, "queryByCriteria");

        LookupCustomizer.Builder<Responsibility> lc = LookupCustomizer.Builder.create();
        lc.setPredicateTransform(AttributeTransform.getInstance());

        return criteriaLookupService.lookup(Responsibility.class, queryByCriteria, lc.build());
    }

    /**
     * Compare each of the passed in responsibilities with the given responsibilityDetails. Those that match are
     * added to the result list.
     */
    private List<Responsibility> getMatchingResponsibilities(List<Responsibility> responsibilities,
                                                             Map<String, String> responsibilityDetails) {
        // if no details passed, assume that all match
        if (responsibilityDetails == null || responsibilityDetails.isEmpty()) {
            return responsibilities;
        }

        final List<Responsibility> applicableResponsibilities = new ArrayList<>();
        // otherwise, attempt to match the permission details
        // build a map of the template IDs to the type services
        Map<String, ResponsibilityTypeService> responsibilityTypeServices =
                getResponsibilityTypeServicesByTemplateId(responsibilities);
        // build a map of permissions by template ID
        Map<String, List<Responsibility>> responsibilityMap = groupResponsibilitiesByTemplate(responsibilities);
        // loop over the different templates, matching all of the same template against the type
        // service at once
        for (Map.Entry<String, List<Responsibility>> respEntry : responsibilityMap.entrySet()) {
            ResponsibilityTypeService responsibilityTypeService = responsibilityTypeServices.get(respEntry.getKey());
            List<Responsibility> responsibilityInfos = respEntry.getValue();
            if (responsibilityTypeService == null) {
                responsibilityTypeService = defaultResponsibilityTypeService;
            }
            applicableResponsibilities.addAll(responsibilityTypeService.getMatchingResponsibilities(
                    responsibilityDetails, responsibilityInfos));
        }
        return Collections.unmodifiableList(applicableResponsibilities);
    }

    private Map<String, ResponsibilityTypeService> getResponsibilityTypeServicesByTemplateId(
            Collection<Responsibility> responsibilities) {
        Map<String, ResponsibilityTypeService> responsibilityTypeServices = new HashMap<>(responsibilities.size());
        for (Responsibility responsibility : responsibilities) {
            final Template t = responsibility.getTemplate();
            final KimType type = kimTypeInfoService.getKimType(t.getKimTypeId());

            final String serviceName = type.getServiceName();
            if (serviceName != null) {
                ResponsibilityTypeService responsibiltyTypeService = GlobalResourceLoader.getService(
                        QName.valueOf(serviceName));
                if (responsibiltyTypeService != null) {
                    responsibilityTypeServices.put(responsibility.getTemplate().getId(), responsibiltyTypeService);
                } else {
                    responsibilityTypeServices.put(responsibility.getTemplate().getId(),
                            defaultResponsibilityTypeService);
                }
            }
        }
        return Collections.unmodifiableMap(responsibilityTypeServices);
    }

    private Map<String, List<Responsibility>> groupResponsibilitiesByTemplate(
            Collection<Responsibility> responsibilities) {
        final Map<String, List<Responsibility>> results = new HashMap<>();
        for (Responsibility responsibility : responsibilities) {
            List<Responsibility> responsibilityInfos =
                    results.computeIfAbsent(responsibility.getTemplate().getId(), k -> new ArrayList<>());
            responsibilityInfos.add(responsibility);
        }
        return Collections.unmodifiableMap(results);
    }

    private List<String> getRoleIdsForPredicate(Predicate p) {
        final QueryByCriteria.Builder builder = QueryByCriteria.Builder.create();
        builder.setPredicates(p);
        final GenericQueryResults<RoleResponsibility> qr = criteriaLookupService.lookup(RoleResponsibility.class,
                builder.build());

        final List<String> roleIds = new ArrayList<>();
        for (RoleResponsibility bo : qr.getResults()) {
            roleIds.add(bo.getRoleId());
        }
        return Collections.unmodifiableList(roleIds);
    }

    @Cacheable(value = Responsibility.CACHE_NAME, key = "'namespaceCode=' + #p1 + '|' + 'templateName=' + #p2")
    @Override
    public List<Responsibility> findResponsibilitiesByTemplate(String namespaceCode, String templateName) {
        final var fieldValues = Map.ofEntries(
                entry("template.namespaceCode", namespaceCode),
                entry("template.name", templateName),
                entry("active", "Y")
        );
        return List.copyOf(businessObjectService.findMatching(Responsibility.class, fieldValues));
    }

    @Override
    public List<Responsibility> findWorkflowResponsibilities(String documentTypeName)
            throws IllegalArgumentException {
        QueryByCriteria queryByCriteria = buildWorkflowResponsibilitiesQueryByCriteria(documentTypeName,
                KewApiConstants.DEFAULT_RESPONSIBILITY_TEMPLATE_NAME);
        return findResponsibilities(queryByCriteria).getResults();
    }

    @Override
    public List<Responsibility> findWorkflowExceptionResponsibilities(String documentTypeName)
            throws IllegalArgumentException {
        QueryByCriteria queryByCriteria = buildWorkflowResponsibilitiesQueryByCriteria(documentTypeName,
                KewApiConstants.EXCEPTION_ROUTING_RESPONSIBILITY_TEMPLATE_NAME);
        return findResponsibilities(queryByCriteria).getResults();
    }

    private QueryByCriteria buildWorkflowResponsibilitiesQueryByCriteria(String documentTypeName,
            String exceptionRoutingResponsibilityTemplateName) {
        QueryByCriteria.Builder builder = QueryByCriteria.Builder.create();
        Predicate p = PredicateFactory.and(
                PredicateFactory.equal("template.namespaceCode", KFSConstants.CoreModuleNamespaces.WORKFLOW),
                PredicateFactory.equal("template.name", exceptionRoutingResponsibilityTemplateName),
                PredicateFactory.equal("active", "Y"),
                PredicateFactory.equal("attributes[documentTypeName]", documentTypeName)
        );
        builder.setPredicates(p);
        return builder.build();
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setCriteriaLookupService(final CriteriaLookupService criteriaLookupService) {
        this.criteriaLookupService = criteriaLookupService;
    }

    public void setDefaultResponsibilityTypeService(final ResponsibilityTypeService defaultResponsibilityTypeService) {
        this.defaultResponsibilityTypeService = defaultResponsibilityTypeService;
    }

    public void setKimTypeInfoService(final KimTypeInfoService kimTypeInfoService) {
        this.kimTypeInfoService = kimTypeInfoService;
    }

    public void setRoleService(final RoleService roleService) {
        this.roleService = roleService;
    }

    protected void logResponsibilityCheck(String namespaceCode, String responsibilityName,
            Map<String, String> responsibilityDetails, Map<String, String> qualification) {
        StringBuilder sb = new StringBuilder();
        sb.append('\n');
        sb.append("Get Resp Actions: ").append(namespaceCode).append("/").append(responsibilityName).append('\n');
        sb.append("             Details:\n");
        if (responsibilityDetails != null) {
            sb.append(responsibilityDetails);
        } else {
            sb.append("                         [null]\n");
        }
        sb.append("             Qualifiers:\n");
        if (qualification != null) {
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
