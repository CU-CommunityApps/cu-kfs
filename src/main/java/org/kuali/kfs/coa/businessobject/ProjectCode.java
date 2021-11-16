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
package org.kuali.kfs.coa.businessobject;

import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.krad.bo.KualiCodeBase;
import org.kuali.kfs.sys.context.SpringContext;

/* Cornell Customization: backport redis*/
public class ProjectCode extends KualiCodeBase implements MutableInactivatable {

    private static final long serialVersionUID = 4529316062843227897L;

    public static final String CACHE_NAME = "ProjectCode";

    private String projectDescription;
    private String projectManagerUniversalId;
    private String chartOfAccountsCode;
    private String organizationCode;

    private Person projectManagerUniversal;
    private Chart chartOfAccounts;
    private Organization organization;

    public ProjectCode() {
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public Person getProjectManagerUniversal() {
        projectManagerUniversal = SpringContext.getBean(org.kuali.kfs.kim.api.identity.PersonService.class)
                .updatePersonIfNecessary(projectManagerUniversalId, projectManagerUniversal);
        return projectManagerUniversal;
    }

    public void setProjectManagerUniversal(Person projectManagerUniversal) {
        this.projectManagerUniversal = projectManagerUniversal;
    }

    public Chart getChartOfAccounts() {
        return chartOfAccounts;
    }

    public void setChartOfAccounts(Chart chartOfAccounts) {
        this.chartOfAccounts = chartOfAccounts;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }

    public void setChartOfAccountsCode(String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }

    public String getOrganizationCode() {
        return organizationCode;
    }

    public void setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
    }

    public String getProjectManagerUniversalId() {
        return projectManagerUniversalId;
    }

    public void setProjectManagerUniversalId(String projectManagerUniversalId) {
        this.projectManagerUniversalId = projectManagerUniversalId;
    }

}

