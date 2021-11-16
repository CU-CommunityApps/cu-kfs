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

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.service.OrganizationService;
import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.sys.businessobject.Campus;
import org.kuali.kfs.sys.businessobject.Country;
import org.kuali.kfs.sys.businessobject.PostalCode;
import org.kuali.kfs.sys.context.SpringContext;

import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

/* Cornell Customization: backport redis*/
public class Organization extends PersistableBusinessObjectBase implements MutableInactivatable {

    private static final long serialVersionUID = 121873645110037203L;
    
    public static final String CACHE_NAME = "Organization";
    
    protected String organizationCode;
    protected String organizationName;
    protected String organizationCityName;
    protected String organizationStateCode;
    protected String organizationZipCode;
    protected Date organizationBeginDate;
    protected Date organizationEndDate;
    protected boolean organizationInFinancialProcessingIndicator = false;
    protected String organizationManagerUniversalId;
    protected String responsibilityCenterCode;
    protected String organizationPhysicalCampusCode;
    protected String organizationTypeCode;
    protected String reportsToChartOfAccountsCode;
    protected String reportsToOrganizationCode;
    protected String organizationPlantAccountNumber;
    protected String campusPlantAccountNumber;
    protected String organizationPlantChartCode;
    protected String campusPlantChartCode;
    protected String organizationCountryCode;
    protected String organizationLine1Address;
    protected String organizationLine2Address;

    protected Chart chartOfAccounts;
    protected Organization hrisOrganization;
    protected Account organizationDefaultAccount;
    protected Person organizationManagerUniversal;
    protected ResponsibilityCenter responsibilityCenter;
    protected Campus organizationPhysicalCampus;
    protected OrganizationType organizationType;
    protected Organization reportsToOrganization;
    protected Chart reportsToChartOfAccounts;
    protected Account organizationPlantAccount;
    protected Account campusPlantAccount;
    protected Chart organizationPlantChart;
    protected Chart campusPlantChart;
    protected PostalCode postalZip;
    protected Country organizationCountry;

    // HRMS Org fields
    protected OrganizationExtension organizationExtension;
    protected String editHrmsUnitSectionBlank;
    protected String editHrmsUnitSection;

    // fields for mixed anonymous keys
    protected String organizationDefaultAccountNumber;
    protected String chartOfAccountsCode;

    // Several kinds of Dummy Attributes for dividing sections on Inquiry page
    protected String editPlantAccountsSectionBlank;
    protected String editPlantAccountsSection;

    protected boolean active = true;

    public String getOrganizationCode() {
        return organizationCode;
    }

    public void setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getOrganizationCityName() {
        return organizationCityName;
    }

    public void setOrganizationCityName(String organizationCityName) {
        this.organizationCityName = organizationCityName;
    }

    public String getOrganizationStateCode() {
        return organizationStateCode;
    }

    public void setOrganizationStateCode(String organizationStateCode) {
        this.organizationStateCode = organizationStateCode;
    }

    public String getOrganizationZipCode() {
        return organizationZipCode;
    }

    public void setOrganizationZipCode(String organizationZipCode) {
        this.organizationZipCode = organizationZipCode;
    }

    public Date getOrganizationBeginDate() {
        return organizationBeginDate;
    }

    public void setOrganizationBeginDate(Date organizationBeginDate) {
        this.organizationBeginDate = organizationBeginDate;
    }

    public Date getOrganizationEndDate() {
        return organizationEndDate;
    }

    public void setOrganizationEndDate(Date organizationEndDate) {
        this.organizationEndDate = organizationEndDate;
    }

    public boolean isOrganizationInFinancialProcessingIndicator() {
        return organizationInFinancialProcessingIndicator;
    }

    public void setOrganizationInFinancialProcessingIndicator(boolean organizationInFinancialProcessingIndicator) {
        this.organizationInFinancialProcessingIndicator = organizationInFinancialProcessingIndicator;
    }

    public Chart getChartOfAccounts() {
        return chartOfAccounts;
    }

    @Deprecated
    public void setChartOfAccounts(Chart chartOfAccounts) {
        this.chartOfAccounts = chartOfAccounts;
    }

    public Account getOrganizationDefaultAccount() {
        return organizationDefaultAccount;
    }

    @Deprecated
    public void setOrganizationDefaultAccount(Account organizationDefaultAccount) {
        this.organizationDefaultAccount = organizationDefaultAccount;
    }

    public Person getOrganizationManagerUniversal() {
        organizationManagerUniversal = SpringContext.getBean(org.kuali.kfs.kim.api.identity.PersonService.class)
                .updatePersonIfNecessary(organizationManagerUniversalId, organizationManagerUniversal);
        return organizationManagerUniversal;
    }

    @Deprecated
    public void setOrganizationManagerUniversal(Person organizationManagerUniversal) {
        this.organizationManagerUniversal = organizationManagerUniversal;
    }

    public ResponsibilityCenter getResponsibilityCenter() {
        return responsibilityCenter;
    }

    @Deprecated
    public void setResponsibilityCenter(ResponsibilityCenter responsibilityCenter) {
        this.responsibilityCenter = responsibilityCenter;
    }

    public Campus getOrganizationPhysicalCampus() {
        return organizationPhysicalCampus;
    }

    @Deprecated
    public void setOrganizationPhysicalCampus(Campus organizationPhysicalCampus) {
        this.organizationPhysicalCampus = organizationPhysicalCampus;
    }

    public OrganizationType getOrganizationType() {
        return organizationType;
    }

    @Deprecated
    public void setOrganizationType(OrganizationType organizationType) {
        this.organizationType = organizationType;
    }

    public Organization getReportsToOrganization() {
        return reportsToOrganization;
    }

    @Deprecated
    public void setReportsToOrganization(Organization reportsToOrganization) {
        this.reportsToOrganization = reportsToOrganization;
    }

    public Chart getReportsToChartOfAccounts() {
        return reportsToChartOfAccounts;
    }

    @Deprecated
    public void setReportsToChartOfAccounts(Chart reportsToChartOfAccounts) {
        this.reportsToChartOfAccounts = reportsToChartOfAccounts;
    }

    public Account getOrganizationPlantAccount() {
        return organizationPlantAccount;
    }

    @Deprecated
    public void setOrganizationPlantAccount(Account organizationPlantAccount) {
        this.organizationPlantAccount = organizationPlantAccount;
    }

    public Account getCampusPlantAccount() {
        return campusPlantAccount;
    }

    @Deprecated
    public void setCampusPlantAccount(Account campusPlantAccount) {
        this.campusPlantAccount = campusPlantAccount;
    }

    public Chart getOrganizationPlantChart() {
        return organizationPlantChart;
    }

    @Deprecated
    public void setOrganizationPlantChart(Chart organizationPlantChart) {
        this.organizationPlantChart = organizationPlantChart;
    }

    public Chart getCampusPlantChart() {
        return campusPlantChart;
    }

    @Deprecated
    public void setCampusPlantChart(Chart campusPlantChart) {
        this.campusPlantChart = campusPlantChart;
    }

    public Country getOrganizationCountry() {
        return organizationCountry;
    }

    @Deprecated
    public void setOrganizationCountry(Country organizationCountry) {
        this.organizationCountry = organizationCountry;
    }

    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }

    public void setChartOfAccountsCode(String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }

    public String getOrganizationDefaultAccountNumber() {
        return organizationDefaultAccountNumber;
    }

    public void setOrganizationDefaultAccountNumber(String organizationDefaultAccountNumber) {
        this.organizationDefaultAccountNumber = organizationDefaultAccountNumber;
    }

    public String getCampusPlantAccountNumber() {
        return campusPlantAccountNumber;
    }

    public void setCampusPlantAccountNumber(String campusPlantAccountNumber) {
        this.campusPlantAccountNumber = campusPlantAccountNumber;
    }

    public String getCampusPlantChartCode() {
        return campusPlantChartCode;
    }

    public void setCampusPlantChartCode(String campusPlantChartCode) {
        this.campusPlantChartCode = campusPlantChartCode;
    }

    public String getOrganizationManagerUniversalId() {
        return organizationManagerUniversalId;
    }

    public void setOrganizationManagerUniversalId(String organizationManagerUniversalId) {
        this.organizationManagerUniversalId = organizationManagerUniversalId;
    }

    public String getOrganizationPhysicalCampusCode() {
        return organizationPhysicalCampusCode;
    }

    public void setOrganizationPhysicalCampusCode(String organizationPhysicalCampusCode) {
        this.organizationPhysicalCampusCode = organizationPhysicalCampusCode;
    }

    public String getOrganizationPlantAccountNumber() {
        return organizationPlantAccountNumber;
    }

    public void setOrganizationPlantAccountNumber(String organizationPlantAccountNumber) {
        this.organizationPlantAccountNumber = organizationPlantAccountNumber;
    }

    public String getOrganizationPlantChartCode() {
        return organizationPlantChartCode;
    }

    public void setOrganizationPlantChartCode(String organizationPlantChartCode) {
        this.organizationPlantChartCode = organizationPlantChartCode;
    }

    public String getOrganizationTypeCode() {
        return organizationTypeCode;
    }

    public void setOrganizationTypeCode(String organizationTypeCode) {
        this.organizationTypeCode = organizationTypeCode;
    }

    public String getReportsToChartOfAccountsCode() {
        return reportsToChartOfAccountsCode;
    }

    public void setReportsToChartOfAccountsCode(String reportsToChartOfAccountsCode) {
        this.reportsToChartOfAccountsCode = reportsToChartOfAccountsCode;
    }

    public String getReportsToOrganizationCode() {
        return reportsToOrganizationCode;
    }

    public void setReportsToOrganizationCode(String reportsToOrganizationCode) {
        this.reportsToOrganizationCode = reportsToOrganizationCode;
    }

    public String getResponsibilityCenterCode() {
        return responsibilityCenterCode;
    }

    public void setResponsibilityCenterCode(String responsibilityCenterCode) {
        this.responsibilityCenterCode = responsibilityCenterCode;
    }

    public PostalCode getPostalZip() {
        return postalZip;
    }

    public void setPostalZip(PostalCode postalZip) {
        this.postalZip = postalZip;
    }

    public String getOrganizationCountryCode() {
        return organizationCountryCode;
    }

    public void setOrganizationCountryCode(String organizationCountryCode) {
        this.organizationCountryCode = organizationCountryCode;
    }

    public String getOrganizationLine1Address() {
        return organizationLine1Address;
    }

    public void setOrganizationLine1Address(String organizationLine1Address) {
        this.organizationLine1Address = organizationLine1Address;
    }

    public String getOrganizationLine2Address() {
        return organizationLine2Address;
    }

    public void setOrganizationLine2Address(String organizationLine2Address) {
        this.organizationLine2Address = organizationLine2Address;
    }

    public String getEditPlantAccountsSection() {
        return editPlantAccountsSection;
    }

    public String getEditPlantAccountsSectionBlank() {
        return editPlantAccountsSectionBlank;
    }

    public final String getEditHrmsUnitSection() {
        return editHrmsUnitSection;
    }

    public final void setEditHrmsUnitSection(String editHrmsUnitSection) {
        this.editHrmsUnitSection = editHrmsUnitSection;
    }

    public final String getEditHrmsUnitSectionBlank() {
        return editHrmsUnitSectionBlank;
    }

    public final void setEditHrmsUnitSectionBlank(String editHrmsUnitSectionBlank) {
        this.editHrmsUnitSectionBlank = editHrmsUnitSectionBlank;
    }

    public final OrganizationExtension getOrganizationExtension() {
        return organizationExtension;
    }

    public final void setOrganizationExtension(OrganizationExtension organizationExtension) {
        this.organizationExtension = organizationExtension;
    }

    public String getOrganizationHierarchy() {
        OrganizationService organizationService = SpringContext.getBean(OrganizationService.class);
        StringBuffer result = new StringBuffer();
        Set<Organization> seen = new HashSet<>();

        Organization org = this;

        while (org != null && org.getReportsToOrganizationCode() != null && !seen.contains(org)) {
            String rChart = org.getReportsToChartOfAccountsCode();
            String rOrg = org.getReportsToOrganizationCode();

            seen.add(org);
            org = organizationService.getByPrimaryIdWithCaching(rChart, rOrg);

            result.append(rChart).append("/").append(rOrg).append(" ");
            result.append(org == null ? "" : org.getOrganizationName());
            if (org != null && org.getReportsToOrganizationCode() != null && !seen.contains(org)) {
                result.append(" ==> ");
            }
            result.append("\n");
        }

        return result.toString();
    }

    /**
     * Gets the campus code for Endowment Report
     *
     * @return
     */
    public String getOrganizationPhysicalCampusCodeForReport() {
        return organizationPhysicalCampusCode;
    }

    /**
     * Implementing equals so Org will behave reasonably in a hashed data structure.
     */
    @Override
    public boolean equals(Object obj) {
        boolean equal = false;

        if (obj != null) {

            if (this == obj) {
                return true;
            }

            if (this.getClass().isAssignableFrom(obj.getClass())) {

                Organization other = (Organization) obj;

                if (StringUtils.equals(this.getChartOfAccountsCode(), other.getChartOfAccountsCode())) {
                    if (StringUtils.equals(this.getOrganizationCode(), other.getOrganizationCode())) {
                        equal = true;
                    }
                }
            }
        }

        return equal;
    }

    /**
     * @return Returns the code and description in format: xx - xxxxxxxxxxxxxxxx
     */
    public String getCodeAndDescription() {
        return getOrganizationCode() + "-" + getOrganizationName();
    }

    @Override
    public int hashCode() {
        String hashString = getChartOfAccountsCode() + "|" + getOrganizationCode();
        return hashString.hashCode();
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    public String getOrganizationCodeForReport() {
        return organizationCode;
    }

    @Override
    public void refreshReferenceObject(String referenceObjectName) {
        if ("organizationManagerUniversal".equals(referenceObjectName)) {
            getOrganizationManagerUniversal();
        } else {
            super.refreshReferenceObject(referenceObjectName);
        }
    }
}
