/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
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
package org.kuali.kfs.module.cg.businessobject;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.module.ar.businessobject.Customer;
import org.kuali.kfs.module.cg.service.AgencyService;
import org.kuali.kfs.sys.context.SpringContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * CU Customization back-port FINP-11685 29-01-2025
 */

/**
 * This class defines an agency as it is used and referenced within the Contracts & Grants portion of a college or university
 * financial system.
 */
public class Agency extends PersistableBusinessObjectBase implements MutableInactivatable {

    private String agencyNumber;
    private String reportingName;
    private String fullName;
    private String agencyTypeCode;
    private String reportsToAgencyNumber;
    private KualiDecimal indirectAmount;
    private boolean inStateIndicator;
    private Agency reportsToAgency;
    private AgencyType agencyType;
    private boolean active;

    // Contracts & Grants fields
    private String cageNumber;
    private String dodacNumber;
    private String dunAndBradstreetNumber;
    private String dunsPlusFourNumber;

    private List<AgencyAddress> agencyAddresses;

    private boolean stateAgencyIndicator;

    // Creating Customer from Agency
    private Customer customer;
    private String customerCreationOptionCode;
    private String customerNumber;
    private String customerTypeCode;
    private String dunningCampaign;

    //To add boNotes
    private List boNotes;

    public Agency() {
        agencyAddresses = new ArrayList<>();
    }

    public String getAgencyTypeCode() {
        return agencyTypeCode;
    }

    public void setAgencyTypeCode(final String agencyTypeCode) {
        this.agencyTypeCode = agencyTypeCode;
    }

    public String getReportsToAgencyNumber() {
        return reportsToAgencyNumber;
    }

    public void setReportsToAgencyNumber(final String reportsToAgencyNumber) {
        this.reportsToAgencyNumber = reportsToAgencyNumber;
    }

    public String getAgencyNumber() {
        return agencyNumber;
    }

    public void setAgencyNumber(final String agencyNumber) {
        this.agencyNumber = agencyNumber;
    }

    public String getReportingName() {
        return reportingName;
    }

    public void setReportingName(final String reportingName) {
        this.reportingName = reportingName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(final String fullName) {
        this.fullName = fullName;
    }

    public KualiDecimal getIndirectAmount() {
        return indirectAmount;
    }

    public void setIndirectAmount(final KualiDecimal indirectAmount) {
        this.indirectAmount = indirectAmount;
    }

    public boolean isInStateIndicator() {
        return inStateIndicator;
    }

    public void setInStateIndicator(final boolean inStateIndicator) {
        this.inStateIndicator = inStateIndicator;
    }

    public Agency getReportsToAgency() {
        return reportsToAgency;
    }

    /**
     * todo Why is this deprecated?
     * @deprecated
     */
    @Deprecated
    public void setReportsToAgency(final Agency reportsToAgencyNumber) {
        reportsToAgency = reportsToAgencyNumber;
    }

    public AgencyType getAgencyType() {
        return agencyType;
    }

    /**
     * todo Why is this deprecated?
     * @deprecated
     */
    @Deprecated
    public void setAgencyType(final AgencyType agencyType) {
        this.agencyType = agencyType;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(final boolean active) {
        this.active = active;
    }

    public String getCageNumber() {
        return cageNumber;
    }

    public void setCageNumber(final String cageNumber) {
        this.cageNumber = cageNumber;
    }

    public String getDodacNumber() {
        return dodacNumber;
    }

    public void setDodacNumber(final String dodacNumber) {
        this.dodacNumber = dodacNumber;
    }

    public String getDunAndBradstreetNumber() {
        return dunAndBradstreetNumber;
    }

    public void setDunAndBradstreetNumber(final String dunAndBradstreetNumber) {
        this.dunAndBradstreetNumber = dunAndBradstreetNumber;
    }

    public String getDunsPlusFourNumber() {
        return dunsPlusFourNumber;
    }

    public void setDunsPlusFourNumber(final String dunsPlusFourNumber) {
        this.dunsPlusFourNumber = dunsPlusFourNumber;
    }

    public List<AgencyAddress> getAgencyAddresses() {
        return agencyAddresses;
    }

    public void setAgencyAddresses(final List<AgencyAddress> agencyAddresses) {
        this.agencyAddresses = agencyAddresses;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(final String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public Customer getCustomer() {
        /*
         * CU Customization back-port FINP-11685
         */
        if (StringUtils.isNotBlank(customerNumber)) {
            customer = SpringContext.getBean(BusinessObjectService.class)
                    .findBySinglePrimaryKey(Customer.class, customerNumber);
        }
        return customer;
    }

    public void setCustomer(final Customer customer) {
        this.customer = customer;
    }

    public String getCustomerCreationOptionCode() {
        return customerCreationOptionCode;
    }

    public void setCustomerCreationOptionCode(final String customerCreationOptionCode) {
        this.customerCreationOptionCode = customerCreationOptionCode;
    }

    public String getCustomerTypeCode() {
        return customerTypeCode;
    }

    public void setCustomerTypeCode(final String customerTypeCode) {
        this.customerTypeCode = customerTypeCode;
    }

    public String getDunningCampaign() {
        return dunningCampaign;
    }

    public void setDunningCampaign(final String dunningCampaign) {
        this.dunningCampaign = dunningCampaign;
    }

    public boolean isStateAgencyIndicator() {
        return stateAgencyIndicator;
    }

    public void setStateAgencyIndicator(final boolean stateAgencyIndicator) {
        this.stateAgencyIndicator = stateAgencyIndicator;
    }

    public List<Note> getBoNotes() {
        if (StringUtils.isEmpty(agencyNumber)) {
            return new ArrayList<>();
        }
        final AgencyService agencyService = SpringContext.getBean(AgencyService.class);
        return agencyService.getAgencyNotes(agencyNumber);
    }

    public void setBoNotes(final List boNotes) {
        this.boNotes = boNotes;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Agency)) {
            return false;
        }
        final Agency agency = (Agency) o;

        return agencyNumber.equals(agency.getAgencyNumber());
    }

    @Override
    public int hashCode() {
        return Objects.hash(agencyNumber);
    }
}
