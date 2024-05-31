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
package org.kuali.kfs.sys.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.Organization;
import org.kuali.kfs.coa.identity.FinancialSystemUserRoleTypeServiceImpl;
import org.kuali.kfs.coa.service.ChartService;
import org.kuali.kfs.coa.service.OrganizationService;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.api.role.RoleService;
import org.kuali.kfs.kim.bo.impl.KimAttributes;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.ChartOrgHolder;
import org.kuali.kfs.sys.service.FinancialSystemUserService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// CU Customization: 
// Null check added to KualiCo base code file version 2023-01-29 for "person.getPrimaryDepartmentCode"
// data element due to users without primary departments defined on their person records generating stack
// traces under certain processing conditions.
// Existing null check for person object also modified to use ObjectUtils.

public class FinancialSystemUserServiceImpl implements FinancialSystemUserService {

    protected ChartService chartService;
    protected OrganizationService organizationService;
    protected RoleService roleService;
    protected PersonService personService;
    protected String userRoleId;
    private final List<String> userRoleIdList = new ArrayList<>(1);

    protected String getUserRoleId() {
        if (userRoleId == null) {
            userRoleId = roleService.getRoleIdByNamespaceCodeAndName(KFSConstants.CoreModuleNamespaces.KFS,
                    KFSConstants.SysKimApiConstants.KFS_USER_ROLE_NAME);
        }
        return userRoleId;
    }

    protected List<String> getUserRoleIdAsList() {
        if (userRoleIdList.isEmpty()) {
            userRoleIdList.add(getUserRoleId());
        }
        return userRoleIdList;
    }

    @Override
    public boolean isActiveFinancialSystemUser(final String principalId) {
        if (StringUtils.isBlank(principalId)) {
            return false;
        }
        return roleService.principalHasRole(principalId, getUserRoleIdAsList(), new HashMap<>());
    }

    @Override
    public ChartOrgHolder getPrimaryOrganization(final Person person, final String namespaceCode) {
        if (person == null) {
            return null;
        }
        ChartOrgHolder chartOrgHolder = getOrganizationForFinancialSystemUser(person.getPrincipalId(), namespaceCode);
        if (chartOrgHolder == null) {
            chartOrgHolder = getOrganizationForNonFinancialSystemUser(person);
        }
        return chartOrgHolder == null ? new ChartOrgHolderImpl() : chartOrgHolder;
    }

    @Override
    public ChartOrgHolder getPrimaryOrganization(final String principalId, final String namespaceCode) {
        if (StringUtils.isBlank(principalId)) {
            return new ChartOrgHolderImpl();
        }
        ChartOrgHolder chartOrgHolder = getOrganizationForFinancialSystemUser(principalId, namespaceCode);
        if (chartOrgHolder == null) {
            chartOrgHolder = getOrganizationForNonFinancialSystemUser(personService.getPerson(principalId));
        }
        return chartOrgHolder == null ? new ChartOrgHolderImpl() : chartOrgHolder;
    }

    protected ChartOrgHolder getOrganizationForFinancialSystemUser(final String principalId, final String namespaceCode) {
        if (StringUtils.isBlank(principalId)) {
            return null;
        }
        final Map<String, String> qualification = new HashMap<>(2);
        qualification.put(FinancialSystemUserRoleTypeServiceImpl.PERFORM_QUALIFIER_MATCH, "true");
        qualification.put(KimConstants.AttributeConstants.NAMESPACE_CODE, namespaceCode);
        final List<Map<String, String>> roleQualifiers = roleService.getRoleQualifersForPrincipalByNamespaceAndRolename(
                principalId, KFSConstants.CoreModuleNamespaces.KFS, KFSConstants.SysKimApiConstants.KFS_USER_ROLE_NAME,
                qualification);
        if (roleQualifiers != null && !roleQualifiers.isEmpty()) {
            int count = 0;
            while (count < roleQualifiers.size() && roleQualifiers.get(count) != null) {
                if (StringUtils.isNotBlank(roleQualifiers.get(count).get(KimAttributes.CHART_OF_ACCOUNTS_CODE))
                        && StringUtils.isNotBlank(roleQualifiers.get(count).get(KimAttributes.ORGANIZATION_CODE))) {
                    return new ChartOrgHolderImpl(roleQualifiers.get(count).get(KimAttributes.CHART_OF_ACCOUNTS_CODE),
                            roleQualifiers.get(count).get(KimAttributes.ORGANIZATION_CODE));
                }
                count += 1;
            }
        }
        return null;
    }

    @Deprecated
    protected ChartOrgHolder getOrganizationForNonFinancialSystemUser(final Person person) {
        // HACK ALERT!!!!! - This is to support the original universal user table setup where the home department
        // was encoded as CAMPUS-ORG (Where campus happened to match the chart) in the original FS_UNIVERSAL_USR_T table.
        // This **REALLY** needs a new implementation
        
        // CU Customization: 
        // Null check added to KualiCo base code file version 2023-01-29 for "person.getPrimaryDepartmentCode"
        // data element due to users without primary departments defined on their person records generating stack
        // traces under certain processing conditions.
        // Existing null check for person object also modified to use ObjectUtils.
        if (ObjectUtils.isNotNull(person) 
                && StringUtils.isNotBlank(person.getPrimaryDepartmentCode())
                && person.getPrimaryDepartmentCode().contains("-")) {
            return new ChartOrgHolderImpl(StringUtils.substringBefore(person.getPrimaryDepartmentCode(), "-"),
                    StringUtils.substringAfter(person.getPrimaryDepartmentCode(), "-"));
        }
        return null;
    }

    @Override
    public Collection<String> getPrincipalIdsForFinancialSystemOrganizationUsers(
            final String namespaceCode,
            final ChartOrgHolder chartOrg) {
        final Map<String, String> qualification = new HashMap<>(4);
        qualification.put(FinancialSystemUserRoleTypeServiceImpl.PERFORM_QUALIFIER_MATCH, "true");
        qualification.put(KimConstants.AttributeConstants.NAMESPACE_CODE, namespaceCode);
        qualification.put(KimAttributes.CHART_OF_ACCOUNTS_CODE, chartOrg.getChartOfAccountsCode());
        qualification.put(KimAttributes.ORGANIZATION_CODE, chartOrg.getOrganizationCode());
        return roleService.getRoleMemberPrincipalIds(KFSConstants.CoreModuleNamespaces.KFS,
                KFSConstants.SysKimApiConstants.KFS_USER_ROLE_NAME, qualification);
    }

    @Override
    public Collection<String> getPrincipalIdsForFinancialSystemOrganizationUsers(
            final String namespaceCode,
            final List<ChartOrgHolder> chartOrgs) {
        final List<String> principalIds = new ArrayList<>();
        for (final ChartOrgHolder chartOrg : chartOrgs) {
            principalIds.addAll(getPrincipalIdsForFinancialSystemOrganizationUsers(namespaceCode, chartOrg));
        }
        return principalIds;
    }

    @Override
    public String getPersonNameByEmployeeId(final String employeeId) {
        final Person person = personService.getPersonByEmployeeId(employeeId);
        if (ObjectUtils.isNotNull(person)) {
            return person.getName();
        }
        return null;
    }

    public void setChartService(final ChartService chartService) {
        this.chartService = chartService;
    }

    public void setOrganizationService(final OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    public void setRoleService(final RoleService roleService) {
        this.roleService = roleService;
    }

    public void setPersonService(final PersonService personService) {
        this.personService = personService;
    }

    public class ChartOrgHolderImpl implements ChartOrgHolder {
        protected String chartOfAccountsCode;
        protected String organizationCode;

        public ChartOrgHolderImpl() {
        }

        public ChartOrgHolderImpl(final String chartOfAccountsCode, final String organizationCode) {
            this.chartOfAccountsCode = chartOfAccountsCode;
            this.organizationCode = organizationCode;
        }

        @Override
        public Chart getChartOfAccounts() {
            return chartService.getByPrimaryId(chartOfAccountsCode);
        }

        @Override
        public Organization getOrganization() {
            return organizationService.getByPrimaryIdWithCaching(chartOfAccountsCode, organizationCode);
        }

        @Override
        public String getChartOfAccountsCode() {
            return chartOfAccountsCode;
        }

        public void setChartOfAccountsCode(final String chartOfAccountsCode) {
            this.chartOfAccountsCode = chartOfAccountsCode;
        }

        @Override
        public String getOrganizationCode() {
            return organizationCode;
        }

        public void setOrganizationCode(final String organizationCode) {
            this.organizationCode = organizationCode;
        }

        @Override
        public boolean equals(final Object obj) {
            if (!(obj instanceof ChartOrgHolder)) {
                return false;
            }
            return chartOfAccountsCode.equals(((ChartOrgHolder) obj).getChartOfAccountsCode())
                    && organizationCode.equals(((ChartOrgHolder) obj).getOrganizationCode());
        }

        @Override
        public int hashCode() {
            return chartOfAccountsCode.hashCode() + organizationCode.hashCode();
        }
    }

}
