/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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
package org.kuali.kfs.module.ar.businessobject;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.Organization;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.businessobject.SubObjectCode;
import org.kuali.kfs.integration.ar.AccountsReceivableSystemInformation;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.sys.businessobject.Country;
import org.kuali.kfs.sys.businessobject.County;
import org.kuali.kfs.sys.businessobject.FiscalYearBasedBusinessObject;
import org.kuali.kfs.sys.businessobject.PostalCode;
import org.kuali.kfs.sys.businessobject.State;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;

/*
 * CU Customization: Backported the FINP-7147 changes into this file.
 * This overlay can be removed when we upgrade to the 2023-06-28 financials patch.
 */
public class SystemInformation extends PersistableBusinessObjectBase implements MutableInactivatable,
        AccountsReceivableSystemInformation, FiscalYearBasedBusinessObject {

    protected Integer universityFiscalYear;
    protected String processingChartOfAccountCode;
    protected String processingOrganizationCode;
    protected String universityFederalEmployerIdentificationNumber;
    protected String discountObjectCode;
    protected String universityClearingChartOfAccountsCode;
    protected String universityClearingAccountNumber;
    protected String universityClearingSubAccountNumber;
    protected String universityClearingObjectCode;
    protected String universityClearingSubObjectCode;
    protected String creditCardObjectCode;
    protected String lockboxNumber;
    protected boolean active;
    protected String organizationRemitToAddressName;
    protected String organizationRemitToLine1StreetAddress;
    protected String organizationRemitToLine2StreetAddress;
    protected String organizationRemitToCityName;
    protected String organizationRemitToStateCode;
    protected String organizationRemitToZipCode;
    protected String organizationRemitToCountryCode;
    protected String organizationCheckPayableToName;
    protected String financialDocumentInitiatorIdentifier;
    private String organizationRemitToCountyCode;
    private String uniqueEntityId;
    protected ObjectCode creditCardFinancialObject;
    protected SubObjectCode universityClearingSubObject;
    protected ObjectCode universityClearingObject;
    protected ObjectCode discountFinancialObject;
    protected Organization processingOrganization;
    protected Chart processingChartOfAccount;
    protected Account universityClearingAccount;
    protected Chart universityClearingChartOfAccounts;
    protected SubAccount universityClearingSubAccount;
    protected ObjectCode universityFiscalYearObject;
    protected State organizationRemitToState;
    protected Person financialDocumentInitiator;
    protected SystemOptions universityFiscal;
    protected PostalCode orgRemitToZipCode;
    private Country orgRemitToCountry;
    private County organizationRemitToCounty;

    public Person getFinancialDocumentInitiator() {
        financialDocumentInitiator = SpringContext.getBean(org.kuali.kfs.kim.api.identity.PersonService.class)
                .updatePersonIfNecessary(financialDocumentInitiatorIdentifier, financialDocumentInitiator);
        return financialDocumentInitiator;
    }

    public void setFinancialDocumentInitiator(final Person financialDocumentInitiator) {
        this.financialDocumentInitiator = financialDocumentInitiator;
    }

    @Override
    public Integer getUniversityFiscalYear() {
        return universityFiscalYear;
    }

    @Override
    public void setUniversityFiscalYear(final Integer universityFiscalYear) {
        this.universityFiscalYear = universityFiscalYear;
    }

    @Override
    public String getProcessingChartOfAccountCode() {
        return processingChartOfAccountCode;
    }

    public void setProcessingChartOfAccountCode(final String processingChartOfAccountCode) {
        this.processingChartOfAccountCode = processingChartOfAccountCode;
    }

    @Override
    public String getProcessingOrganizationCode() {
        return processingOrganizationCode;
    }

    public void setProcessingOrganizationCode(final String processingOrganizationCode) {
        this.processingOrganizationCode = processingOrganizationCode;
    }

    public String getUniversityFederalEmployerIdentificationNumber() {
        return universityFederalEmployerIdentificationNumber;
    }

    public void setUniversityFederalEmployerIdentificationNumber(final String universityFederalEmployerIdentificationNumber) {
        this.universityFederalEmployerIdentificationNumber = universityFederalEmployerIdentificationNumber;
    }

    public String getDiscountObjectCode() {
        return discountObjectCode;
    }

    public void setDiscountObjectCode(final String refundFinancialObjectCode) {
        this.discountObjectCode = refundFinancialObjectCode;
    }

    public String getUniversityClearingChartOfAccountsCode() {
        return universityClearingChartOfAccountsCode;
    }

    public void setUniversityClearingChartOfAccountsCode(final String universityClearingChartOfAccountsCode) {
        this.universityClearingChartOfAccountsCode = universityClearingChartOfAccountsCode;
    }

    public String getUniversityClearingAccountNumber() {
        return universityClearingAccountNumber;
    }

    public void setUniversityClearingAccountNumber(final String universityClearingAccountNumber) {
        this.universityClearingAccountNumber = universityClearingAccountNumber;
    }

    public String getUniversityClearingSubAccountNumber() {
        return universityClearingSubAccountNumber;
    }

    public void setUniversityClearingSubAccountNumber(final String universityClearingSubAccountNumber) {
        this.universityClearingSubAccountNumber = universityClearingSubAccountNumber;
    }

    public String getUniversityClearingObjectCode() {
        return universityClearingObjectCode;
    }

    public void setUniversityClearingObjectCode(final String universityClearingObjectCode) {
        this.universityClearingObjectCode = universityClearingObjectCode;
    }

    public String getUniversityClearingSubObjectCode() {
        return universityClearingSubObjectCode;
    }

    public void setUniversityClearingSubObjectCode(final String universityClearingSubObjectCode) {
        this.universityClearingSubObjectCode = universityClearingSubObjectCode;
    }

    public String getCreditCardObjectCode() {
        return creditCardObjectCode;
    }

    public void setCreditCardObjectCode(final String creditCardObjectCode) {
        this.creditCardObjectCode = creditCardObjectCode;
    }

    public String getLockboxNumber() {
        return lockboxNumber;
    }

    public void setLockboxNumber(final String lockboxNumber) {
        this.lockboxNumber = lockboxNumber;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public boolean getActive() {
        return active;
    }

    @Override
    public void setActive(final boolean active) {
        this.active = active;
    }

    public String getFinancialDocumentInitiatorIdentifier() {
        return financialDocumentInitiatorIdentifier;
    }

    public void setFinancialDocumentInitiatorIdentifier(final String financialDocumentInitiatorIdentifier) {
        this.financialDocumentInitiatorIdentifier = financialDocumentInitiatorIdentifier;
    }

    public String getOrganizationRemitToCountyCode() {
        return organizationRemitToCountyCode;
    }

    public void setOrganizationRemitToCountyCode(final String countyCode) {
        this.organizationRemitToCountyCode = countyCode;
    }

    public String getUniqueEntityId() {
        return uniqueEntityId;
    }

    public void setUniqueEntityId(final String uniqueEntityId) {
        this.uniqueEntityId = uniqueEntityId;
    }

    public String getOrganizationCheckPayableToName() {
        return organizationCheckPayableToName;
    }

    public void setOrganizationCheckPayableToName(final String organizationCheckPayableToName) {
        this.organizationCheckPayableToName = organizationCheckPayableToName;
    }

    public String getOrganizationRemitToAddressName() {
        return organizationRemitToAddressName;
    }

    public void setOrganizationRemitToAddressName(final String organizationRemitToAddressName) {
        this.organizationRemitToAddressName = organizationRemitToAddressName;
    }

    public String getOrganizationRemitToCityName() {
        return organizationRemitToCityName;
    }

    public void setOrganizationRemitToCityName(final String organizationRemitToCityName) {
        this.organizationRemitToCityName = organizationRemitToCityName;
    }

    public String getOrganizationRemitToLine1StreetAddress() {
        return organizationRemitToLine1StreetAddress;
    }

    public void setOrganizationRemitToLine1StreetAddress(final String organizationRemitToLine1StreetAddress) {
        this.organizationRemitToLine1StreetAddress = organizationRemitToLine1StreetAddress;
    }

    public String getOrganizationRemitToLine2StreetAddress() {
        return organizationRemitToLine2StreetAddress;
    }

    public void setOrganizationRemitToLine2StreetAddress(final String organizationRemitToLine2StreetAddress) {
        this.organizationRemitToLine2StreetAddress = organizationRemitToLine2StreetAddress;
    }

    public String getOrganizationRemitToStateCode() {
        return organizationRemitToStateCode;
    }

    public void setOrganizationRemitToStateCode(final String organizationRemitToStateCode) {
        this.organizationRemitToStateCode = organizationRemitToStateCode;
    }

    public String getOrganizationRemitToZipCode() {
        return organizationRemitToZipCode;
    }

    public void setOrganizationRemitToZipCode(final String organizationRemitToZipCode) {
        this.organizationRemitToZipCode = organizationRemitToZipCode;
    }

    public ObjectCode getCreditCardFinancialObject() {
        return creditCardFinancialObject;
    }

    @Deprecated
    public void setCreditCardFinancialObject(final ObjectCode creditCardFinancialObject) {
        this.creditCardFinancialObject = creditCardFinancialObject;
    }

    public SubObjectCode getUniversityClearingSubObject() {
        return universityClearingSubObject;
    }

    @Deprecated
    public void setUniversityClearingSubObject(final SubObjectCode universityClearingSubObject) {
        this.universityClearingSubObject = universityClearingSubObject;
    }

    public ObjectCode getUniversityClearingObject() {
        return universityClearingObject;
    }

    @Deprecated
    public void setUniversityClearingObject(final ObjectCode universityClearingObject) {
        this.universityClearingObject = universityClearingObject;
    }

    public ObjectCode getDiscountFinancialObject() {
        return discountFinancialObject;
    }

    @Deprecated
    public void setDiscountFinancialObject(final ObjectCode refundFinancialObject) {
        this.discountFinancialObject = refundFinancialObject;
    }

    public Organization getProcessingOrganization() {
        return processingOrganization;
    }

    @Deprecated
    public void setProcessingOrganization(final Organization processingOrganization) {
        this.processingOrganization = processingOrganization;
    }

    public Chart getProcessingChartOfAccount() {
        return processingChartOfAccount;
    }

    @Deprecated
    public void setProcessingChartOfAccount(final Chart processingChartOfAccount) {
        this.processingChartOfAccount = processingChartOfAccount;
    }

    public Account getUniversityClearingAccount() {
        return universityClearingAccount;
    }

    @Deprecated
    public void setUniversityClearingAccount(final Account universityClearingAccount) {
        this.universityClearingAccount = universityClearingAccount;
    }

    public Chart getUniversityClearingChartOfAccounts() {
        return universityClearingChartOfAccounts;
    }

    @Deprecated
    public void setUniversityClearingChartOfAccounts(final Chart universityClearingChartOfAccounts) {
        this.universityClearingChartOfAccounts = universityClearingChartOfAccounts;
    }

    public SubAccount getUniversityClearingSubAccount() {
        return universityClearingSubAccount;
    }

    @Deprecated
    public void setUniversityClearingSubAccount(final SubAccount universityClearingSubAccount) {
        this.universityClearingSubAccount = universityClearingSubAccount;
    }

    public State getOrganizationRemitToState() {
        return organizationRemitToState;
    }

    @Deprecated
    public void setOrganizationRemitToState(final State organizationRemitToState) {
        this.organizationRemitToState = organizationRemitToState;
    }

    @Override
    public String toString() {
        return (universityFiscalYear == null ? "" : universityFiscalYear + "-") +
               processingChartOfAccountCode + "-" + processingOrganizationCode;
    }

    public ObjectCode getUniversityFiscalYearObject() {
        return universityFiscalYearObject;
    }

    public void setUniversityFiscalYearObject(final ObjectCode universityFiscalYearObject) {
        this.universityFiscalYearObject = universityFiscalYearObject;
    }

    /**
     * This method (a hack by any other name...) returns a string so that an organization options can have a link to
     * view its own inquiry page after a look up
     *
     * @return the String "View System Information"
     */
    public String getSystemInformationViewer() {
        return "View System Information";
    }

    public SystemOptions getUniversityFiscal() {
        return universityFiscal;
    }

    public void setUniversityFiscal(SystemOptions universityFiscal) {
        this.universityFiscal = universityFiscal;
    }

    public PostalCode getOrgRemitToZipCode() {
        return orgRemitToZipCode;
    }

    public void setOrgRemitToZipCode(final PostalCode orgRemitToZipCode) {
        this.orgRemitToZipCode = orgRemitToZipCode;
    }

    public String getOrganizationRemitToCountryCode() {
        return organizationRemitToCountryCode;
    }

    public void setOrganizationRemitToCountryCode(final String organizationRemitToCountryCode) {
        this.organizationRemitToCountryCode = organizationRemitToCountryCode;
    }

    public Country getOrgRemitToCountry() {
        return orgRemitToCountry;
    }

    public void setOrgRemitToCountry(final Country orgRemitToCountry) {
        this.orgRemitToCountry = orgRemitToCountry;
    }

    public County getOrganizationRemitToCounty() {
        return organizationRemitToCounty;
    }

    public void setOrganizationRemitToCounty(final County county) {
        this.organizationRemitToCounty = county;
    }
}
