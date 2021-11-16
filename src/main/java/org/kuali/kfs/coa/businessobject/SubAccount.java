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
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

/* Cornell Customization: backport redis*/
public class SubAccount extends PersistableBusinessObjectBase implements MutableInactivatable {

    private static final long serialVersionUID = 6853259976912014273L;

    public static final String CACHE_NAME = "SubAccount";

    private String chartOfAccountsCode;
    private String accountNumber;
    private String subAccountNumber;
    private String subAccountName;
    private boolean active;
    private String financialReportChartCode;
    private String finReportOrganizationCode;
    private String financialReportingCode;

    private A21SubAccount a21SubAccount;
    private Account account;
    private ReportingCode reportingCode;
    private Chart chart;
    private Organization org;
    private Chart financialReportChart;

    // Several kinds of Dummy Attributes for dividing sections on Inquiry page
    private String financialReportingCodeSectionBlank;
    private String financialReportingCodeSection;
    private String cgCostSharingSectionBlank;
    private String cgCostSharingSection;
    private String cgICRSectionBlank;
    private String cgICRSection;

    public SubAccount() {
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }

    public void setChartOfAccountsCode(String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }

    public void setReportingCode(ReportingCode reportingCode) {
        this.reportingCode = reportingCode;
    }

    public String getSubAccountName() {
        return subAccountName;
    }

    public void setSubAccountName(String subAccountName) {
        this.subAccountName = subAccountName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getSubAccountNumber() {
        return subAccountNumber;
    }

    public void setSubAccountNumber(String subAccountNumber) {
        this.subAccountNumber = subAccountNumber;
    }

    public ReportingCode getReportingCode() {
        return reportingCode;
    }

    public String getFinancialReportChartCode() {
        return financialReportChartCode;
    }

    public void setFinancialReportChartCode(String financialReportChartCode) {
        this.financialReportChartCode = financialReportChartCode;
    }

    public String getFinancialReportingCode() {
        return financialReportingCode;
    }

    public void setFinancialReportingCode(String financialReportingCode) {
        this.financialReportingCode = financialReportingCode;
    }

    public String getFinReportOrganizationCode() {
        return finReportOrganizationCode;
    }

    public void setFinReportOrganizationCode(String finReportOrganizationCode) {
        this.finReportOrganizationCode = finReportOrganizationCode;
    }

    public A21SubAccount getA21SubAccount() {
        return a21SubAccount;
    }

    public void setA21SubAccount(A21SubAccount subAccount) {
        a21SubAccount = subAccount;
    }

    public Chart getChart() {
        return chart;
    }

    public void setChart(Chart chart) {
        this.chart = chart;
    }

    public Chart getFinancialReportChart() {
        return financialReportChart;
    }

    public void setFinancialReportChart(Chart financialReportChart) {
        this.financialReportChart = financialReportChart;
    }

    public Organization getOrg() {
        return org;
    }

    public void setOrg(Organization org) {
        this.org = org;
    }

    public String getCgCostSharingSectionBlank() {
        return cgCostSharingSectionBlank;
    }

    public String getCgICRSectionBlank() {
        return cgICRSectionBlank;
    }

    public String getFinancialReportingCodeSectionBlank() {
        return financialReportingCodeSectionBlank;
    }

    public String getCgCostSharingSection() {
        return cgCostSharingSection;
    }

    public String getCgICRSection() {
        return cgICRSection;
    }

    public String getFinancialReportingCodeSection() {
        return financialReportingCodeSection;
    }
}
