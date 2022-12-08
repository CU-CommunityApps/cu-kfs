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
package org.kuali.kfs.integration.cg;

import org.kuali.kfs.integration.ar.AccountsReceivableBillingFrequency;
import org.kuali.kfs.integration.ar.Billable;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.module.ar.ArConstants.ContractsAndGrantsInvoiceDocumentCreationProcessType;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

/**
 * Integration interface for Award (specific to CGB functionality)
 */
public interface ContractsAndGrantsBillingAward extends Billable, ContractsAndGrantsAward {

    @Override
    String getProposalNumber();

    /**
     * @return Returns the proposal object.
     */
    @Override
    ContractAndGrantsProposal getProposal();

    String getObjectId();

    /**
     * @return Returns the awardInquiryTitle.
     */
    @Override
    String getAwardInquiryTitle();

    /**
     * @return Returns the awardBeginningDate.
     */
    Date getAwardBeginningDate();

    /**
     * @return Returns the awardEndingDate.
     */
    Date getAwardEndingDate();

    /**
     * @return Returns the lastBilledDate.
     */
    Date getLastBilledDate();

    /**
     * @return Returns the awardTotalAmount.
     */
    KualiDecimal getAwardTotalAmount();

    /**
     * @return Returns the awardAddendumNumber.
     */
    String getAwardAddendumNumber();

    /**
     * @return Returns the awardAllocatedUniversityComputingServicesAmount.
     */
    KualiDecimal getAwardAllocatedUniversityComputingServicesAmount();

    /**
     * @return Returns the federalPassThroughFundedAmount.
     */
    KualiDecimal getFederalPassThroughFundedAmount();

    /**
     * @return Returns the awardEntryDate.
     */
    Date getAwardEntryDate();

    /**
     * @return Returns the agencyFuture1Amount.
     */
    KualiDecimal getAgencyFuture1Amount();

    /**
     * @return Returns the agencyFuture2Amount.
     */
    KualiDecimal getAgencyFuture2Amount();

    /**
     * @return Returns the agencyFuture3Amount.
     */
    KualiDecimal getAgencyFuture3Amount();

    /**
     * @return Returns the awardDocumentNumber.
     */
    String getAwardDocumentNumber();

    /**
     * @return Returns the awardLastUpdateDate.
     */
    Timestamp getAwardLastUpdateDate();

    /**
     * @return Returns the federalPassThroughIndicator.
     */
    boolean getFederalPassThroughIndicator();

    /**
     * @return Returns the oldProposalNumber.
     */
    String getOldProposalNumber();

    /**
     * @return Returns the awardDirectCostAmount.
     */
    KualiDecimal getAwardDirectCostAmount();

    /**
     * @return Returns the awardIndirectCostAmount.
     */
    KualiDecimal getAwardIndirectCostAmount();

    /**
     * @return Returns the federalFundedAmount.
     */
    KualiDecimal getFederalFundedAmount();

    /**
     * @return Returns the awardCreateTimestamp.
     */
    Timestamp getAwardCreateTimestamp();

    /**
     * @return Returns the awardClosingDate.
     */
    Date getAwardClosingDate();

    /**
     * @return Returns the proposalAwardTypeCode.
     */
    String getProposalAwardTypeCode();

    /**
     * @return Returns the awardStatusCode.
     */
    String getAwardStatusCode();

    /**
     * @return Returns the letterOfCreditFundCode.
     */
    String getLetterOfCreditFundCode();

    /**
     * @return Returns the grantDescriptionCode.
     */
    String getGrantDescriptionCode();

    /**
     * @return Returns the agencyNumber.
     */
    String getAgencyNumber();

    /**
     * @return Returns the federalPassThroughAgencyNumber.
     */
    String getFederalPassThroughAgencyNumber();

    /**
     * @return Returns the agencyAnalystName.
     */
    String getAgencyAnalystName();

    /**
     * @return Returns the analystTelephoneNumber.
     */
    String getAnalystTelephoneNumber();

    /**
     * @return Returns the billingFrequencyCode.
     */
    String getBillingFrequencyCode();

    /**
     * @return Returns the awardProjectTitle.
     */
    String getAwardProjectTitle();

    /**
     * @return Returns the awardPurposeCode.
     */
    String getAwardPurposeCode();

    /**
     * @return Returns the active.
     */
    boolean isActive();

    /**
     * @return Returns the kimGroupNames.
     */
    String getKimGroupNames();

    /**
     * @return Returns the list of active awardAccounts.
     */
    List<ContractsAndGrantsBillingAwardAccount> getActiveAwardAccounts();

    /**
     * @return Returns the agency.
     */
    ContractsAndGrantsBillingAgency getAgency();

    /**
     * @return Returns the routingOrg.
     */
    String getRoutingOrg();

    /**
     * @return Returns the routingChart.
     */
    String getRoutingChart();

    /**
     * @return Returns the stateTransfer.
     */
    boolean isStateTransferIndicator();

    /**
     * @return Returns the excludedFromInvoicing.
     */
    boolean isExcludedFromInvoicing();

    /**
     * @return Returns the additionalFormsRequired.
     */
    boolean isAdditionalFormsRequiredIndicator();

    /**
     * @return Returns the additionalFormsDescription.
     */
    String getAdditionalFormsDescription();

    /**
     * @return Returns the excludedFromInvoicingReason.
     */
    String getExcludedFromInvoicingReason();

    /**
     * @return Returns the instrumentTypeCode.
     */
    String getInstrumentTypeCode();

    ContractsAndGrantsInstrumentType getInstrumentType();

    /**
     * @return Returns the invoicingOptionCode.
     */
    String getInvoicingOptionCode();

    String getCustomerNumber();

    Integer getCustomerAddressIdentifier();

    /**
     * Returns the module specific description for the invoicing option selected.
     *
     * @return
     */
    String getInvoicingOptionDescription();

    /**
     * @return Returns the minInvoiceAmount.
     */
    KualiDecimal getMinInvoiceAmount();

    /**
     * @return Returns the autoApprove.
     */
    boolean getAutoApproveIndicator();

    /**
     * @return Returns the lookupPersonUniversalIdentifier.
     */
    String getLookupProjectDirectorUniversalIdentifier();

    /**
     * @return Returns the lookupProjectDirector.
     */
    Person getLookupProjectDirector();

    /**
     * @return Returns the lookupFundMgrPersonUniversalIdentifier.
     */
    String getLookupFundMgrPersonUniversalIdentifier();

    /**
     * @return Returns the lookupFundMgrPerson.
     */
    Person getLookupFundMgrPerson();

    /**
     * @return Returns the userLookupRoleNamespaceCode.
     */
    String getUserLookupRoleNamespaceCode();

    /**
     * @return Returns the letterOfCreditFund.
     */
    ContractsAndGrantsLetterOfCreditFund getLetterOfCreditFund();

    /**
     * Sets the letterOfCreditFund attribute.
     * <p>
     * We normally wouldn't put a setter in an interface, but we are struggling with an NPE
     * when doing an Award inquiry related to the fact that the code can't find a setter
     * for this attribute.
     */
    void setLetterOfCreditFund(ContractsAndGrantsLetterOfCreditFund letterOfCreditFund);

    /**
     * @return Returns the userLookupRoleName.
     */
    String getUserLookupRoleName();

    /**
     * @return Returns the awardPrimaryFundManager.
     */
    ContractsAndGrantsFundManager getAwardPrimaryFundManager();

    /**
     * @return Returns the billingFrequency.
     */
    AccountsReceivableBillingFrequency getBillingFrequency();

    /**
     * @return Returns the awardPrimaryProjectDirector.
     */
    ContractsAndGrantsProjectDirector getAwardPrimaryProjectDirector();

    /**
     * @return Returns the primaryAwardOrganization.
     */
    ContractsAndGrantsOrganization getPrimaryAwardOrganization();

    /**
     * @return Returns the fundingExpirationDate.
     */
    Date getFundingExpirationDate();

    /**
     * @return Returns the dunningCampaign.
     */
    String getDunningCampaign();

    /**
     * @return Returns the stopWork indicator.
     */
    boolean isStopWorkIndicator();

    /**
     * @return Returns the stop work reason.
     */
    String getStopWorkReason();

    List<String> getSelectedAccounts();
    
    /*
     * CU Customization (KFSPTS-23690)
     */
    ContractsAndGrantsInvoiceDocumentCreationProcessType getCreationProcessType();
    
    /*
     * CU Customization (KFSPTS-23690)
     */
    void setCreationProcessType(ContractsAndGrantsInvoiceDocumentCreationProcessType creationProcessType);
}
