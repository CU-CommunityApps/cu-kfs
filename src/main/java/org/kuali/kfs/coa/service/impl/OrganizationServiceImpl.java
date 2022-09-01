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
package org.kuali.kfs.coa.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.COAParameterConstants;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Organization;
import org.kuali.kfs.coa.service.ChartService;
import org.kuali.kfs.coa.service.OrganizationService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.ChartOrgHolderImpl;
import org.springframework.cache.annotation.Cacheable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* Cornell Customization: backport redis; backport redis fix on FINP-8169*/
public class OrganizationServiceImpl implements OrganizationService {
    private static final Logger LOG = LogManager.getLogger();

    protected ParameterService parameterService;
    protected ChartService chartService;
    protected BusinessObjectService boService;

    // parentOrgCache does not include inactive orgs
    protected Map<ChartOrgHolderImpl, ChartOrgHolderImpl> parentOrgCache = null;
    // allParentOrgCache includes inactive orgs
    protected Map<ChartOrgHolderImpl, ChartOrgHolderImpl> allParentOrgCache = null;

    @Override
    public Organization getByPrimaryId(String chartOfAccountsCode, String organizationCode) {
        Map<String, Object> keys = new HashMap<>();
        keys.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartOfAccountsCode);
        keys.put(KFSPropertyConstants.ORGANIZATION_CODE, organizationCode);
        return boService.findByPrimaryKey(Organization.class, keys);
    }

    /**
     * Implements the getByPrimaryId method defined by OrganizationService. Method is used by KualiOrgReviewAttribute to
     * enable caching of orgs for routing.
     *
     * @see org.kuali.kfs.coa.service.impl.OrganizationServiceImpl#getByPrimaryId(java.lang.String, java.lang.String)
     */
    @Override
    @Cacheable(cacheNames = Organization.CACHE_NAME, key = "'{" + Organization.CACHE_NAME + "}'+#p0+'-'+#p1")
    public Organization getByPrimaryIdWithCaching(String chartOfAccountsCode, String organizationCode) {
        return getByPrimaryId(chartOfAccountsCode, organizationCode);
    }

    @Override
    public List<Account> getActiveAccountsByOrg(String chartOfAccountsCode, String organizationCode) {
        if (StringUtils.isBlank(chartOfAccountsCode)) {
            throw new IllegalArgumentException("String parameter chartOfAccountsCode was null or blank.");
        }
        if (StringUtils.isBlank(organizationCode)) {
            throw new IllegalArgumentException("String parameter organizationCode was null or blank.");
        }

        Map<String, Object> criteria = new HashMap<>();
        criteria.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartOfAccountsCode);
        criteria.put(KFSPropertyConstants.ORGANIZATION_CODE, organizationCode);
        criteria.put(KFSPropertyConstants.ACTIVE, Boolean.TRUE);
        return new ArrayList<>(boService.findMatching(Account.class, criteria));
    }

    @Override
    public List<Organization> getActiveChildOrgs(String chartOfAccountsCode, String organizationCode) {
        if (StringUtils.isBlank(chartOfAccountsCode)) {
            throw new IllegalArgumentException("String parameter chartOfAccountsCode was null or blank.");
        }
        if (StringUtils.isBlank(organizationCode)) {
            throw new IllegalArgumentException("String parameter organizationCode was null or blank.");
        }

        Map<String, Object> criteria = new HashMap<>();
        criteria.put(KFSPropertyConstants.REPORTS_TO_CHART_OF_ACCOUNTS_CODE, chartOfAccountsCode);
        criteria.put(KFSPropertyConstants.REPORTS_TO_ORGANIZATION_CODE, organizationCode);
        criteria.put(KFSPropertyConstants.ACTIVE, Boolean.TRUE);

        return new ArrayList<>(boService.findMatching(Organization.class, criteria));
    }

    protected void loadParentOrgCache() {
        LOG.debug("START - Initializing parent organization cache");

        Collection<Organization> orgs = boService.findMatching(Organization.class,
                Collections.singletonMap(KFSPropertyConstants.ACTIVE, true));
        parentOrgCache = populateOrganizationCacheMap(orgs);

        if (LOG.isDebugEnabled()) {
            LOG.debug("COMPLETE - Initializing parent organization cache - " + parentOrgCache.size() +
                    " organizations loaded");
        }
    }

    protected void loadAllParentOrgCache() {
        LOG.debug("START - Initializing all parent organization cache");

        Collection<Organization> orgs = boService.findAll(Organization.class);
        allParentOrgCache = populateOrganizationCacheMap(orgs);

        if (LOG.isDebugEnabled()) {
            LOG.debug("COMPLETE - Initializing all parent organization cache - " + allParentOrgCache.size() +
                    " organizations loaded");
        }
    }

    private Map<ChartOrgHolderImpl, ChartOrgHolderImpl> populateOrganizationCacheMap(Collection<Organization> orgs) {
        Map<ChartOrgHolderImpl, ChartOrgHolderImpl> temp = new HashMap<>();
        for (Organization org : orgs) {
            ChartOrgHolderImpl keyOrg = new ChartOrgHolderImpl(org);
            if (StringUtils.isNotBlank(org.getReportsToChartOfAccountsCode())
                && StringUtils.isNotBlank(org.getReportsToOrganizationCode())) {
                ChartOrgHolderImpl parentOrg = new ChartOrgHolderImpl(org.getReportsToChartOfAccountsCode(),
                        org.getReportsToOrganizationCode());
                temp.put(keyOrg, parentOrg);
            }
        }
        return temp;
    }

    @Override
    public void flushParentOrgCache() {
        LOG.debug("Flushing parent organization cache");
        parentOrgCache = null;
    }

    @Override
    public boolean isParentOrganization(String childChartOfAccountsCode, String childOrganizationCode,
            String parentChartOfAccountsCode, String parentOrganizationCode) {
        if (StringUtils.isBlank(childChartOfAccountsCode)
            || StringUtils.isBlank(childOrganizationCode)
            || StringUtils.isBlank(parentChartOfAccountsCode)
            || StringUtils.isBlank(parentOrganizationCode)) {
            return false;
        }

        if (parentOrgCache == null) {
            loadParentOrgCache();
        }

        ChartOrgHolderImpl currOrg = new ChartOrgHolderImpl(childChartOfAccountsCode, childOrganizationCode);
        ChartOrgHolderImpl desiredParentOrg = new ChartOrgHolderImpl(parentChartOfAccountsCode, parentOrganizationCode);

        // the the orgs are the same, we can short circuit the search right now
        if (currOrg.equals(desiredParentOrg)) {
            return true;
        }

        return isParentOrganization_Internal(parentOrgCache, currOrg, desiredParentOrg, new ArrayList<>());
    }

    @Override
    public boolean isParentOrganizationAllowInactive(String childChartOfAccountsCode, String childOrganizationCode,
            String parentChartOfAccountsCode, String parentOrganizationCode) {
        if (StringUtils.isBlank(childChartOfAccountsCode)
            || StringUtils.isBlank(childOrganizationCode)
            || StringUtils.isBlank(parentChartOfAccountsCode)
            || StringUtils.isBlank(parentOrganizationCode)) {
            return false;
        }

        if (allParentOrgCache == null) {
            loadAllParentOrgCache();
        }

        ChartOrgHolderImpl currOrg = new ChartOrgHolderImpl(childChartOfAccountsCode, childOrganizationCode);
        ChartOrgHolderImpl desiredParentOrg = new ChartOrgHolderImpl(parentChartOfAccountsCode, parentOrganizationCode);

        // the the orgs are the same, we can short circuit the search right now
        if (currOrg.equals(desiredParentOrg)) {
            return true;
        }

        return isParentOrganization_Internal(allParentOrgCache, currOrg, desiredParentOrg, new ArrayList<>());
    }

    /**
     * This helper method handles the case where there might be cycles in the data.
     */
    protected boolean isParentOrganization_Internal(Map<ChartOrgHolderImpl, ChartOrgHolderImpl> cache,
            ChartOrgHolderImpl currOrg, ChartOrgHolderImpl desiredParentOrg, List<ChartOrgHolderImpl> traversedOrgs) {

        if (traversedOrgs.contains(currOrg)) {
            LOG.error("THERE IS A LOOP IN THE ORG DATA: " + currOrg + " found a second time after traversing the " +
                    "following orgs: " + traversedOrgs);
            return false;
        }

        ChartOrgHolderImpl parentOrg = cache.get(currOrg);

        // we could not find it in the table, return false
        if (parentOrg == null) {
            return false;
        }
        // it is its own parent, then false (we reached the top and did not find a match)
        if (parentOrg.equals(currOrg)) {
            return false;
        }
        // check parent org against desired parent organization
        if (parentOrg.equals(desiredParentOrg)) {
            return true;
        }
        // otherwise, we don't know yet - so re-call this method moving up to the next parent org
        traversedOrgs.add(currOrg);
        return isParentOrganization_Internal(cache, parentOrg, desiredParentOrg, traversedOrgs);
    }

    @Override
    public List<Organization> getActiveOrgsByType(String organizationTypeCode) {
        if (StringUtils.isBlank(organizationTypeCode)) {
            throw new IllegalArgumentException("String parameter organizationTypeCode was null or blank.");
        }
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(KFSPropertyConstants.ORGANIZATION_TYPE_CODE, organizationTypeCode);
        criteria.put(KFSPropertyConstants.ACTIVE, Boolean.TRUE);

        return new ArrayList<>(boService.findMatching(Organization.class, criteria));
    }

    @Override
    public List<Organization> getActiveFinancialOrgs() {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(KFSPropertyConstants.ORGANIZATION_IN_FINANCIAL_PROCESSING_INDICATOR, Boolean.TRUE);
        criteria.put(KFSPropertyConstants.ACTIVE, Boolean.TRUE);
        return new ArrayList<>(boService.findMatching(Organization.class, criteria));
    }

    /**
     * TODO: refactor me to a ChartOrgHolder
     */
    @Override
    public String[] getRootOrganizationCode() {
        String rootChart = chartService.getUniversityChart().getChartOfAccountsCode();
        String selfReportsOrgType = parameterService.getParameterValueAsString(Organization.class,
                COAParameterConstants.REPORT_TO_SELF);
        String[] returnValues = {null, null};

        Map<String, Object> criteria = new HashMap<>();
        criteria.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, rootChart);
        criteria.put(KFSPropertyConstants.ORGANIZATION_TYPE_CODE, selfReportsOrgType);
        criteria.put(KFSPropertyConstants.ACTIVE, Boolean.TRUE);

        Collection<Organization> results = boService.findMatching(Organization.class, criteria);
        if (results != null && !results.isEmpty()) {
            Organization org = results.iterator().next();
            returnValues[0] = org.getChartOfAccountsCode();
            returnValues[1] = org.getOrganizationCode();
        }

        return returnValues;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setBusinessObjectService(BusinessObjectService boService) {
        this.boService = boService;
    }

    public void setChartService(ChartService chartService) {
        this.chartService = chartService;
    }

}
