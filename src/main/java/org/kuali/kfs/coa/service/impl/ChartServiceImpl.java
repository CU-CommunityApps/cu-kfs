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
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.Organization;
import org.kuali.kfs.coa.service.ChartService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kim.bo.impl.KimAttributes;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.api.role.RoleService;
import org.springframework.cache.annotation.Cacheable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* Cornell Customization: backport redis*/
public class ChartServiceImpl implements ChartService {
    private static final Logger LOG = LogManager.getLogger();

    protected BusinessObjectService businessObjectService;
    protected RoleService roleService;
    protected PersonService personService;
    protected ParameterService parameterService;

    @Override
    @Cacheable(cacheNames = Chart.CACHE_NAME, key = "'chartOfAccountsCode='+#p0")
    public Chart getByPrimaryId(String chartOfAccountsCode) {
        return businessObjectService.findBySinglePrimaryKey(Chart.class, chartOfAccountsCode);
    }

    @Override
    @Cacheable(cacheNames = Chart.CACHE_NAME, key = "'UniversityChart'")
    public Chart getUniversityChart() {
        // 1. find the organization with the type which reports to itself
        String organizationReportsToSelfParameterValue = parameterService.getParameterValueAsString(Organization.class,
                COAParameterConstants.REPORT_TO_SELF);

        Map<String, String> orgCriteria = new HashMap<>(2);
        orgCriteria.put(KFSPropertyConstants.ORGANIZATION_TYPE_CODE, organizationReportsToSelfParameterValue);
        orgCriteria.put(KFSPropertyConstants.ACTIVE, KFSConstants.ACTIVE_INDICATOR);
        Collection<Organization> orgs = businessObjectService.findMatching(Organization.class, orgCriteria);
        if (orgs != null && !orgs.isEmpty()) {
            return getByPrimaryId(orgs.iterator().next().getChartOfAccountsCode());
        }

        return null;
    }

    @Override
    @Cacheable(cacheNames = Chart.CACHE_NAME, key = "'AllChartCodes'")
    public List<String> getAllChartCodes() {
        Collection<Chart> charts = businessObjectService.findAllOrderBy(Chart.class,
                KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, true);
        return getChartCodes(charts);
    }

    @Override
    @Cacheable(cacheNames = Chart.CACHE_NAME, key = "'AllActiveChartCodes'")
    public List<String> getAllActiveChartCodes() {
        return getChartCodes(getAllActiveCharts());
    }

    @Override
    @Cacheable(cacheNames = Chart.CACHE_NAME, key = "'AllActiveCharts'")
    public Collection<Chart> getAllActiveCharts() {
        return businessObjectService.findMatchingOrderBy(Chart.class,
            Collections.singletonMap(KFSPropertyConstants.ACTIVE, true),
            KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, true);
    }

    @Override
    @Cacheable(cacheNames = Chart.CACHE_NAME, key = "'ReportsToHierarchy'")
    public Map<String, String> getReportsToHierarchy() {
        Map<String, String> reportsToHierarchy = new HashMap<>();

        for (String chartCode : getAllChartCodes()) {
            Chart chart = businessObjectService.findBySinglePrimaryKey(Chart.class, chartCode);

            if (LOG.isDebugEnabled()) {
                LOG.debug("adding " + chart.getChartOfAccountsCode() + "-->" + chart.getReportsToChartOfAccountsCode());
            }
            reportsToHierarchy.put(chart.getChartOfAccountsCode(), chart.getReportsToChartOfAccountsCode());
        }

        return reportsToHierarchy;
    }

    @Override
    @Cacheable(cacheNames = Chart.CACHE_NAME, key = "'{isParentChart?}'+#p0+'-->'+#p1")
    public boolean isParentChart(String potentialChildChartCode, String potentialParentChartCode) {
        if (potentialChildChartCode == null || potentialParentChartCode == null) {
            throw new IllegalArgumentException("The isParentChartCode method requires a non-null potentialChildChartCode " +
                    "and potentialParentChartCode");
        }
        Chart thisChart = getByPrimaryId(potentialChildChartCode);
        if (thisChart == null || StringUtils.isBlank(thisChart.getChartOfAccountsCode())) {
            throw new IllegalArgumentException("The isParentChartCode method requires a valid potentialChildChartCode");
        }
        if (thisChart.getCode().equals(thisChart.getReportsToChartOfAccountsCode())) {
            return false;
        } else if (potentialParentChartCode.equals(thisChart.getReportsToChartOfAccountsCode())) {
            return true;
        } else {
            return isParentChart(thisChart.getReportsToChartOfAccountsCode(), potentialParentChartCode);
        }
    }

    @Override
    public Person getChartManager(String chartOfAccountsCode) {
        String chartManagerId = null;
        Person chartManager = null;

        Map<String, String> qualification = new HashMap<>();
        qualification.put(KimAttributes.CHART_OF_ACCOUNTS_CODE, chartOfAccountsCode);

        Collection<String> chartManagerList = roleService.getRoleMemberPrincipalIds(
                KFSConstants.CoreModuleNamespaces.KFS, KFSConstants.SysKimApiConstants.CHART_MANAGER_KIM_ROLE_NAME,
                qualification);

        if (!chartManagerList.isEmpty()) {
            chartManagerId = chartManagerList.iterator().next();
        }

        if (chartManagerId != null) {
            chartManager = personService.getPerson(chartManagerId);
        }

        return chartManager;
    }

    protected List<String> getChartCodes(Collection<Chart> charts) {
        List<String> chartCodes = new ArrayList<>(charts.size());
        for (Chart chart : charts) {
            chartCodes.add(chart.getChartOfAccountsCode());
        }
        return chartCodes;
    }

    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
