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
package org.kuali.kfs.module.cg.businessobject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.integration.ar.AccountsReceivableBillingFrequency;
import org.kuali.kfs.integration.ar.AccountsReceivableCustomer;
import org.kuali.kfs.integration.ar.AccountsReceivableCustomerAddress;
import org.kuali.kfs.integration.ar.AccountsReceivableMilestoneSchedule;
import org.kuali.kfs.integration.ar.AccountsReceivableModuleBillingService;
import org.kuali.kfs.integration.ar.AccountsReceivablePredeterminedBillingSchedule;
import org.kuali.kfs.integration.cg.CGIntegrationConstants;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAward;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAwardAccount;
import org.kuali.kfs.integration.cg.ContractsAndGrantsLetterOfCreditFund;
import org.kuali.kfs.kim.impl.identity.PersonImpl;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.service.KualiModuleService;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cg.CGPropertyConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kim.api.identity.PersonService;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Defines a financial award object.
 */
public class Award extends PersistableBusinessObjectBase implements MutableInactivatable,
        ContractsAndGrantsBillingAward {

    private static final String AWARD_INQUIRY_TITLE_PROPERTY = "message.inquiry.award.title";
    private String proposalNumber;
    private Date awardBeginningDate;
    private Date awardEndingDate;
    private Date lastBilledDate;

    /**
     * This field is for write-only to the database via OJB, not the corresponding property of this BO. OJB uses
     * reflection to read it, so the compiler warns because it doesn't know.
     *
     * @see #getAwardTotalAmount
     * @see #setAwardTotalAmount
     */
    protected KualiDecimal awardTotalAmount;

    private String awardAddendumNumber;
    private KualiDecimal awardAllocatedUniversityComputingServicesAmount;
    private KualiDecimal federalPassThroughFundedAmount;
    private Date awardEntryDate;
    private KualiDecimal agencyFuture1Amount;
    private KualiDecimal agencyFuture2Amount;
    private KualiDecimal agencyFuture3Amount;
    private String awardDocumentNumber;
    private Timestamp awardLastUpdateDate;
    private boolean federalPassThroughIndicator;
    private String oldProposalNumber;
    private KualiDecimal awardDirectCostAmount;
    private KualiDecimal awardIndirectCostAmount;
    private KualiDecimal federalFundedAmount;
    private Timestamp awardCreateTimestamp;
    private Date awardClosingDate;
    private String proposalAwardTypeCode;
    private String awardStatusCode;
    private String letterOfCreditFundCode;
    private String grantDescriptionCode;
    private String agencyNumber;
    private String federalPassThroughAgencyNumber;
    private String agencyAnalystName;
    private String analystTelephoneNumber;
    private String billingFrequencyCode;
    private String awardProjectTitle;
    private String awardPurposeCode;
    private boolean active;
    private String kimGroupNames;
    private List<AwardProjectDirector> awardProjectDirectors;
    private AwardProjectDirector awardPrimaryProjectDirector;
    private List<AwardFundManager> awardFundManagers;
    private AwardFundManager awardPrimaryFundManager;
    private List<AwardAccount> awardAccounts;
    private List<AwardSubcontractor> awardSubcontractors;
    private List<AwardOrganization> awardOrganizations;

    private Proposal proposal;
    private ProposalAwardType proposalAwardType;
    private AwardStatus awardStatus;
    protected ContractsAndGrantsLetterOfCreditFund letterOfCreditFund;
    private GrantDescription grantDescription;
    private Agency agency;
    private Agency federalPassThroughAgency;
    private ProposalPurpose awardPurpose;
    private AwardOrganization primaryAwardOrganization;
    private InstrumentType instrumentType;
    private String routingOrg;
    private String routingChart;

    private boolean stateTransferIndicator;
    private boolean excludedFromInvoicing;
    private boolean additionalFormsRequiredIndicator;
    private String additionalFormsDescription;
    private String excludedFromInvoicingReason;
    private String instrumentTypeCode;
    private String invoicingOptionCode;
    private String customerNumber;
    private Integer customerAddressIdentifier;
    private String removeAddressButton;

    private KualiDecimal minInvoiceAmount = KualiDecimal.ZERO;

    private boolean autoApproveIndicator;

    private AccountsReceivableCustomerAddress customerAddress;
    private AccountsReceivableMilestoneSchedule milestoneSchedule;
    private AccountsReceivablePredeterminedBillingSchedule predeterminedBillingSchedule;
    private AccountsReceivableBillingFrequency billingFrequency;

    private Date fundingExpirationDate;
    private String dunningCampaign;
    private boolean stopWorkIndicator;
    private String stopWorkReason;

    private List<Note> boNotes;

    private transient List<String> selectedAccounts = new ArrayList<>();

    /**
     * Dummy value used to facilitate lookups
     */
    private transient String lookupProjectDirectorUniversalIdentifier;
    private transient PersonImpl lookupProjectDirector;

    private final String userLookupRoleNamespaceCode = KFSConstants.CoreModuleNamespaces.KFS;
    private final String userLookupRoleName = KFSConstants.SysKimApiConstants.CONTRACTS_AND_GRANTS_PROJECT_DIRECTOR;

    private transient String lookupFundMgrPersonUniversalIdentifier;
    private transient PersonImpl lookupFundMgrPerson;

    private transient String scheduleInquiryTitle;

    // CU Customization: Added additional fields to the Award BO
    private String letterOfCreditFundGroupCode;
    private LetterOfCreditFundGroup letterOfCreditFundGroup;
    private transient String invoiceLink;
    private transient String cgInvoiceDocumentCreationProcessTypeCode;

    public Award() {
        // Must use ArrayList because its get() method automatically grows the array for Struts.
        awardProjectDirectors = new ArrayList<>();
        awardFundManagers = new ArrayList<>();
        awardAccounts = new ArrayList<>();
        awardSubcontractors = new ArrayList<>();
        awardOrganizations = new ArrayList<>();
    }

    /**
     * Constructs an Award.
     *
     * @param proposal The associated proposal that the award will be linked to.
     */
    public Award(Proposal proposal) {
        this();
        populateFromProposal(proposal);
    }

    @Override
    public String getExcludedFromInvoicingReason() {
        return excludedFromInvoicingReason;
    }

    @Override
    public boolean isStateTransferIndicator() {
        return stateTransferIndicator;
    }

    public void setStateTransferIndicator(boolean stateTransferIndicator) {
        this.stateTransferIndicator = stateTransferIndicator;
    }

    public void setExcludedFromInvoicingReason(String excludedFromInvoicingReason) {
        this.excludedFromInvoicingReason = excludedFromInvoicingReason;
    }

    /**
     * Creates a collection of lists within this award object that should be aware of when the deletion of one of their
     * elements occurs. This collection is used to refresh the display upon deletion of an element to ensure that the
     * deleted element is not longer visible on the interface.
     */
    @Override
    public List<Collection<PersistableBusinessObject>> buildListOfDeletionAwareLists() {
        List<Collection<PersistableBusinessObject>> managedLists = super.buildListOfDeletionAwareLists();
        managedLists.add(ObjectUtils.isNull(getAwardAccounts()) ? new ArrayList() : new ArrayList(getAwardAccounts()));
        managedLists.add(ObjectUtils.isNull(getAwardOrganizations()) ? new ArrayList() :
            new ArrayList(getAwardOrganizations()));
        managedLists.add(ObjectUtils.isNull(getAwardProjectDirectors()) ? new ArrayList() :
            new ArrayList(getAwardProjectDirectors()));
        managedLists.add(ObjectUtils.isNull(getAwardFundManagers()) ? new ArrayList() :
            new ArrayList(getAwardFundManagers()));
        managedLists.add(ObjectUtils.isNull(getAwardSubcontractors()) ? new ArrayList() :
            new ArrayList(getAwardSubcontractors()));
        return managedLists;
    }

    // CU Customization: Altered this method to adjust the fields used for population and to eliminate some duplicates.
    /**
     * This method takes all the applicable attributes from the associated proposal object and sets those attributes
     * into their corresponding award attributes.
     *
     * @param proposal The associated proposal that the award will be linked to.
     */
    public void populateFromProposal(Proposal proposal) {
        if (ObjectUtils.isNotNull(proposal)) {
            setProposalNumber(proposal.getProposalNumber());
            setAgencyNumber(proposal.getAgencyNumber());
            setAwardProjectTitle(proposal.getProposalProjectTitle());
            setAwardDirectCostAmount(proposal.getProposalDirectCostAmount());
            setAwardIndirectCostAmount(proposal.getProposalIndirectCostAmount());
            setProposalAwardTypeCode(proposal.getProposalAwardTypeCode());
            setFederalPassThroughIndicator(proposal.getProposalFederalPassThroughIndicator());
            setFederalPassThroughAgencyNumber(proposal.getFederalPassThroughAgencyNumber());
            setAwardPurposeCode(proposal.getProposalPurposeCode());

            // copy proposal organizations to award organizations
            getAwardOrganizations().clear();
            for (ProposalOrganization pOrg : proposal.getProposalOrganizations()) {
                AwardOrganization awardOrg = new AwardOrganization();
                // newCollectionRecord is set to true to allow deletion of this record after being populated from proposal
                awardOrg.setNewCollectionRecord(true);
                awardOrg.setProposalNumber(pOrg.getProposalNumber());
                awardOrg.setChartOfAccountsCode(pOrg.getChartOfAccountsCode());
                awardOrg.setOrganizationCode(pOrg.getOrganizationCode());
                awardOrg.setAwardPrimaryOrganizationIndicator(pOrg.isProposalPrimaryOrganizationIndicator());
                awardOrg.setActive(pOrg.isActive());
                awardOrg.setVersionNumber(pOrg.getVersionNumber());
                getAwardOrganizations().add(awardOrg);
            }

            // copy proposal subcontractors to award subcontractors
            getAwardSubcontractors().clear();
            int awardSubcontractAmendment = 1;
            for (ProposalSubcontractor pSubcontractor : proposal.getProposalSubcontractors()) {
                AwardSubcontractor awardSubcontractor = new AwardSubcontractor();
                // newCollectionRecord is set to true to allow deletion of this record after being populated from proposal
                awardSubcontractor.setNewCollectionRecord(true);
                awardSubcontractor.setProposalNumber(pSubcontractor.getProposalNumber());
                awardSubcontractor.setAwardSubcontractorNumber(pSubcontractor.getProposalSubcontractorNumber());

                // Since we might possibly pulled multiples of same subcontractor from the proposal, we cannot set them
                // all to 1s.
                // Increment the amendment number for every subcontractor from the proposal
                awardSubcontractor.setAwardSubcontractorAmendmentNumber(String.valueOf(awardSubcontractAmendment++));
                awardSubcontractor.setSubcontractorAmount(pSubcontractor.getProposalSubcontractorAmount());
                awardSubcontractor.setAwardSubcontractorDescription(pSubcontractor.getProposalSubcontractorDescription());
                awardSubcontractor.setSubcontractorNumber(pSubcontractor.getSubcontractorNumber());
                awardSubcontractor.setActive(pSubcontractor.isActive());
                awardSubcontractor.setVersionNumber(pSubcontractor.getVersionNumber());
                getAwardSubcontractors().add(awardSubcontractor);
            }

            // copy proposal project directors to award project directors
            getAwardProjectDirectors().clear();
            Set<String> directors = new HashSet<String>(); // use this to filter out duplicate projectdirectors
            for (ProposalProjectDirector pDirector : proposal.getProposalProjectDirectors()) {
                if (directors.add(pDirector.getPrincipalId())) {
                    AwardProjectDirector awardDirector = new AwardProjectDirector();
                    // newCollectionRecord is set to true to allow deletion of this record after being populated from proposal
                    awardDirector.setNewCollectionRecord(true);
                    awardDirector.setProposalNumber(pDirector.getProposalNumber());
                    awardDirector.setAwardPrimaryProjectDirectorIndicator(pDirector.
                            isProposalPrimaryProjectDirectorIndicator());
                    awardDirector.setAwardProjectDirectorProjectTitle(pDirector.getProposalProjectDirectorProjectTitle());
                    awardDirector.setPrincipalId(pDirector.getPrincipalId());
                    awardDirector.setActive(pDirector.isActive());
                    awardDirector.setVersionNumber(pDirector.getVersionNumber());
                    getAwardProjectDirectors().add(awardDirector);
                }
            }
        }
    }

    @Override
    public String getProposalNumber() {
        return proposalNumber;
    }

    public void setProposalNumber(String proposalNumber) {
        this.proposalNumber = proposalNumber;
    }

    @Override
    public Date getAwardBeginningDate() {
        return awardBeginningDate;
    }

    public void setAwardBeginningDate(Date awardBeginningDate) {
        this.awardBeginningDate = awardBeginningDate;
    }

    @Override
    public Date getAwardEndingDate() {
        return awardEndingDate;
    }

    @Override
    public String getKimGroupNames() {
        return kimGroupNames;
    }

    public void setKimGroupNames(String kimGroupNames) {
        this.kimGroupNames = kimGroupNames;
    }

    public void setAwardEndingDate(Date awardEndingDate) {
        this.awardEndingDate = awardEndingDate;
    }

    /**
     * Gets the lastBilledDate attribute. This value is derived from the active Award Accounts for this Award.
     *
     * @return Returns the lastBilledDate.
     */
    @Override
    public Date getLastBilledDate() {
        return SpringContext.getBean(AccountsReceivableModuleBillingService.class).getLastBilledDate(this);
    }

    @Override
    public KualiDecimal getAwardTotalAmount() {
        KualiDecimal direct = getAwardDirectCostAmount();
        KualiDecimal indirect = getAwardIndirectCostAmount();
        return ObjectUtils.isNull(direct) || ObjectUtils.isNull(indirect) ? null : direct.add(indirect);
    }

    /**
     * Does nothing. This property is determined by the direct and indirect cost amounts. This setter is here only
     * because without it, the maintenance framework won't display this attribute.
     *
     * @param awardTotalAmount The awardTotalAmount to set.
     * @deprecated Should not be used. See method description above.
     */
    @Deprecated
    public void setAwardTotalAmount(KualiDecimal awardTotalAmount) {
        // do nothing
    }

    /**
     * OJB calls this method as the first operation before this BO is inserted into the database. The database contains
     * CGAWD_TOT_AMT, a denormalized column that Kuali does not use but needs to maintain with this method because OJB
     * bypasses the getter.
     */
    @Override
    protected void beforeInsert() {
        super.beforeInsert();
        awardTotalAmount = getAwardTotalAmount();
    }

    /**
     * OJB calls this method as the first operation before this BO is updated to the database. The database contains
     * CGAWD_TOT_AMT, a denormalized column that Kuali does not use but needs to maintain with this method because OJB
     * bypasses the getter.
     */
    @Override
    protected void beforeUpdate() {
        super.beforeUpdate();
        awardTotalAmount = getAwardTotalAmount();
    }

    @Override
    public String getAwardAddendumNumber() {
        return awardAddendumNumber;
    }

    public void setAwardAddendumNumber(String awardAddendumNumber) {
        this.awardAddendumNumber = awardAddendumNumber;
    }

    @Override
    public KualiDecimal getAwardAllocatedUniversityComputingServicesAmount() {
        return awardAllocatedUniversityComputingServicesAmount;
    }

    public void setAwardAllocatedUniversityComputingServicesAmount(KualiDecimal awardAllocatedUniversityComputingServicesAmount) {
        this.awardAllocatedUniversityComputingServicesAmount = awardAllocatedUniversityComputingServicesAmount;
    }

    @Override
    public KualiDecimal getFederalPassThroughFundedAmount() {
        return federalPassThroughFundedAmount;
    }

    public void setFederalPassThroughFundedAmount(KualiDecimal federalPassThroughFundedAmount) {
        this.federalPassThroughFundedAmount = federalPassThroughFundedAmount;
    }

    @Override
    public Date getAwardEntryDate() {
        return awardEntryDate;
    }

    public void setAwardEntryDate(Date awardEntryDate) {
        this.awardEntryDate = awardEntryDate;
    }

    @Override
    public KualiDecimal getAgencyFuture1Amount() {
        return agencyFuture1Amount;
    }

    public void setAgencyFuture1Amount(KualiDecimal agencyFuture1Amount) {
        this.agencyFuture1Amount = agencyFuture1Amount;
    }

    @Override
    public KualiDecimal getAgencyFuture2Amount() {
        return agencyFuture2Amount;
    }

    public void setAgencyFuture2Amount(KualiDecimal agencyFuture2Amount) {
        this.agencyFuture2Amount = agencyFuture2Amount;
    }

    @Override
    public KualiDecimal getAgencyFuture3Amount() {
        return agencyFuture3Amount;
    }

    public void setAgencyFuture3Amount(KualiDecimal agencyFuture3Amount) {
        this.agencyFuture3Amount = agencyFuture3Amount;
    }

    @Override
    public String getAwardDocumentNumber() {
        return awardDocumentNumber;
    }

    public void setAwardDocumentNumber(String awardDocumentNumber) {
        this.awardDocumentNumber = awardDocumentNumber;
    }

    @Override
    public Timestamp getAwardLastUpdateDate() {
        return awardLastUpdateDate;
    }

    public void setAwardLastUpdateDate(Timestamp awardLastUpdateDate) {
        this.awardLastUpdateDate = awardLastUpdateDate;
    }

    @Override
    public boolean getFederalPassThroughIndicator() {
        return federalPassThroughIndicator;
    }

    public void setFederalPassThroughIndicator(boolean federalPassThroughIndicator) {
        this.federalPassThroughIndicator = federalPassThroughIndicator;
    }

    @Override
    public String getOldProposalNumber() {
        return oldProposalNumber;
    }

    public void setOldProposalNumber(String oldProposalNumber) {
        this.oldProposalNumber = oldProposalNumber;
    }

    @Override
    public KualiDecimal getAwardDirectCostAmount() {
        return awardDirectCostAmount;
    }

    public void setAwardDirectCostAmount(KualiDecimal awardDirectCostAmount) {
        this.awardDirectCostAmount = awardDirectCostAmount;
    }

    @Override
    public KualiDecimal getAwardIndirectCostAmount() {
        return awardIndirectCostAmount;
    }

    public void setAwardIndirectCostAmount(KualiDecimal awardIndirectCostAmount) {
        this.awardIndirectCostAmount = awardIndirectCostAmount;
    }

    @Override
    public KualiDecimal getFederalFundedAmount() {
        return federalFundedAmount;
    }

    public void setFederalFundedAmount(KualiDecimal federalFundedAmount) {
        this.federalFundedAmount = federalFundedAmount;
    }

    @Override
    public Timestamp getAwardCreateTimestamp() {
        return awardCreateTimestamp;
    }

    public void setAwardCreateTimestamp(Timestamp awardCreateTimestamp) {
        this.awardCreateTimestamp = awardCreateTimestamp;
    }

    @Override
    public Date getAwardClosingDate() {
        return awardClosingDate;
    }

    public void setAwardClosingDate(Date awardClosingDate) {
        this.awardClosingDate = awardClosingDate;
    }

    @Override
    public String getProposalAwardTypeCode() {
        return proposalAwardTypeCode;
    }

    public void setProposalAwardTypeCode(String proposalAwardTypeCode) {
        this.proposalAwardTypeCode = proposalAwardTypeCode;
    }

    @Override
    public String getAwardStatusCode() {
        return awardStatusCode;
    }

    public void setAwardStatusCode(String awardStatusCode) {
        this.awardStatusCode = awardStatusCode;
    }

    @Override
    public String getLetterOfCreditFundCode() {
        return letterOfCreditFundCode;
    }

    public void setLetterOfCreditFundCode(String letterOfCreditFundCode) {
        this.letterOfCreditFundCode = letterOfCreditFundCode;
    }

    @Override
    public String getGrantDescriptionCode() {
        return grantDescriptionCode;
    }

    public void setGrantDescriptionCode(String grantDescriptionCode) {
        this.grantDescriptionCode = grantDescriptionCode;
    }

    @Override
    public String getAgencyNumber() {
        return agencyNumber;
    }

    public void setAgencyNumber(String agencyNumber) {
        this.agencyNumber = agencyNumber;
    }

    @Override
    public String getFederalPassThroughAgencyNumber() {
        return federalPassThroughAgencyNumber;
    }

    public void setFederalPassThroughAgencyNumber(String federalPassThroughAgencyNumber) {
        this.federalPassThroughAgencyNumber = federalPassThroughAgencyNumber;
    }

    @Override
    public String getAgencyAnalystName() {
        return agencyAnalystName;
    }

    public void setAgencyAnalystName(String agencyAnalystName) {
        this.agencyAnalystName = agencyAnalystName;
    }

    @Override
    public String getAnalystTelephoneNumber() {
        return analystTelephoneNumber;
    }

    public void setAnalystTelephoneNumber(String analystTelephoneNumber) {
        this.analystTelephoneNumber = analystTelephoneNumber;
    }

    @Override
    public String getAwardProjectTitle() {
        return awardProjectTitle;
    }

    public void setAwardProjectTitle(String awardProjectTitle) {
        this.awardProjectTitle = awardProjectTitle;
    }

    @Override
    public String getAwardPurposeCode() {
        return awardPurposeCode;
    }

    public void setAwardPurposeCode(String awardPurposeCode) {
        this.awardPurposeCode = awardPurposeCode;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public Proposal getProposal() {
        return proposal;
    }

    /**
     * Sets the proposal attribute.
     *
     * Setter is required by OJB, but should not be used to modify this attribute. This attribute is set on the initial
     * creation of the object and should not be changed.
     *
     * @param proposal The proposal to set.
     */
    public void setProposal(Proposal proposal) {
        this.proposal = proposal;
    }

    public ProposalAwardType getProposalAwardType() {
        return proposalAwardType;
    }

    /**
     * Sets the proposalAwardType attribute.
     *
     * Setter is required by OJB, but should not be used to modify this attribute. This attribute is set on the initial
     * creation of the object and should not be changed.
     *
     * @param proposalAwardType The proposalAwardType to set.
     */
    public void setProposalAwardType(ProposalAwardType proposalAwardType) {
        this.proposalAwardType = proposalAwardType;
    }

    public AwardStatus getAwardStatus() {
        return awardStatus;
    }

    /**
     * Sets the awardStatus attribute.
     *
     * Setter is required by OJB, but should not be used to modify this attribute. This attribute is set on the initial
     * creation of the object and should not be changed.
     *
     * @param awardStatus The awardStatus to set.
     */
    public void setAwardStatus(AwardStatus awardStatus) {
        this.awardStatus = awardStatus;
    }

    @Override
    public ContractsAndGrantsLetterOfCreditFund getLetterOfCreditFund() {
        return letterOfCreditFund;
    }

    /**
     * Sets the letterOfCreditFund attribute.
     *
     * Setter is required by OJB, but should not be used to modify this attribute. This attribute is set on the initial
     * creation of the object and should not be changed.
     *
     * @param letterOfCreditFund The letterOfCreditFund to set.
     */
    @Override
    public void setLetterOfCreditFund(ContractsAndGrantsLetterOfCreditFund letterOfCreditFund) {
        this.letterOfCreditFund = letterOfCreditFund;
    }

    public GrantDescription getGrantDescription() {
        return grantDescription;
    }

    /**
     * Sets the grantDescription attribute.
     *
     * Setter is required by OJB, but should not be used to modify this attribute. This attribute is set on the initial
     * creation of the object and should not be changed.
     *
     * @param grantDescription The grantDescription to set.
     */
    public void setGrantDescription(GrantDescription grantDescription) {
        this.grantDescription = grantDescription;
    }

    @Override
    public Agency getAgency() {
        return agency;
    }

    /**
     * Sets the agency attribute.
     *
     * Setter is required by OJB, but should not be used to modify this attribute. This attribute is set on the initial
     * creation of the object and should not be changed.
     *
     * @param agency The agency to set.
     */
    public void setAgency(Agency agency) {
        this.agency = agency;
    }

    public Agency getFederalPassThroughAgency() {
        return federalPassThroughAgency;
    }

    /**
     * Sets the federalPassThroughAgency attribute.
     *
     * Setter is required by OJB, but should not be used to modify this attribute. This attribute is set on the initial
     * creation of the object and should not be changed.
     *
     * @param federalPassThroughAgency The federalPassThroughAgency to set.
     */
    public void setFederalPassThroughAgency(Agency federalPassThroughAgency) {
        this.federalPassThroughAgency = federalPassThroughAgency;
    }

    public ProposalPurpose getAwardPurpose() {
        return awardPurpose;
    }

    /**
     * Sets the awardPurpose attribute.
     *
     * Setter is required by OJB, but should not be used to modify this attribute. This attribute is set on the initial
     * creation of the object and should not be changed.
     *
     * @param awardPurpose The awardPurpose to set.
     */
    public void setAwardPurpose(ProposalPurpose awardPurpose) {
        this.awardPurpose = awardPurpose;
    }

    public List<AwardProjectDirector> getAwardProjectDirectors() {
        return awardProjectDirectors;
    }

    public void setAwardProjectDirectors(List<AwardProjectDirector> awardProjectDirectors) {
        this.awardProjectDirectors = awardProjectDirectors;
    }

    public List<AwardFundManager> getAwardFundManagers() {
        return awardFundManagers;
    }

    public void setAwardFundManagers(List<AwardFundManager> awardFundManagers) {
        this.awardFundManagers = awardFundManagers;
    }

    public List<AwardAccount> getAwardAccounts() {
        return awardAccounts;
    }

    /**
     * Gets the list of active award accounts. The integration object is used here - as this would be referred only
     * from AR module. For Milestone and Predetermined Billing individual Award Accounts can be selected to generate
     * invoices from. We want this to only return the selected ones so the validation and creation logic that uses
     * this is only applied to the individual Award Accounts the user selected.
     *
     * @return Returns the active (and selected if applicable) awardAccounts.
     */
    @Override
    public List<ContractsAndGrantsBillingAwardAccount> getActiveAwardAccounts() {
        return awardAccounts.stream()
                .filter(awardAccount -> awardAccount.isActive()
                        && (CollectionUtils.isEmpty(selectedAccounts)
                            || selectedAccounts.contains(
                                    awardAccount.getChartOfAccountsCode() + awardAccount.getAccountNumber())))
                .collect(Collectors.toList());
    }

    public void setAwardAccounts(List<AwardAccount> awardAccounts) {
        this.awardAccounts = awardAccounts;
    }

    public List<AwardOrganization> getAwardOrganizations() {
        return awardOrganizations;
    }

    public void setAwardOrganizations(List<AwardOrganization> awardOrganizations) {
        this.awardOrganizations = awardOrganizations;
    }

    public List<AwardSubcontractor> getAwardSubcontractors() {
        return awardSubcontractors;
    }

    public void setAwardSubcontractors(List<AwardSubcontractor> awardSubcontractors) {
        this.awardSubcontractors = awardSubcontractors;
    }

    /**
     * This method gets the primary award organization.
     *
     * @return The award organization object marked as primary in the award organizations collection.
     */
    @Override
    public AwardOrganization getPrimaryAwardOrganization() {
        for (AwardOrganization ao : awardOrganizations) {
            if (ao != null && ao.isAwardPrimaryOrganizationIndicator()) {
                setPrimaryAwardOrganization(ao);
                break;
            }
        }

        return primaryAwardOrganization;
    }

    public void setPrimaryAwardOrganization(AwardOrganization primaryAwardOrganization) {
        this.primaryAwardOrganization = primaryAwardOrganization;
        this.routingChart = primaryAwardOrganization.getChartOfAccountsCode();
        this.routingOrg = primaryAwardOrganization.getOrganizationCode();
    }

    public InstrumentType getInstrumentType() {
        return instrumentType;
    }

    public void setInstrumentType(InstrumentType instrumentType) {
        this.instrumentType = instrumentType;
    }

    /**
     * Sums the total for all award subcontractors
     *
     * @return Returns the total of all the award subcontractor's amounts
     */
    public KualiDecimal getAwardSubcontractorsTotalAmount() {
        KualiDecimal total = KualiDecimal.ZERO;
        for (AwardSubcontractor subcontractor : getAwardSubcontractors()) {
            KualiDecimal amount = subcontractor.getSubcontractorAmount();
            if (ObjectUtils.isNotNull(amount)) {
                total = total.add(amount);
            }
        }
        return total;
    }

    @Override
    public String getRoutingChart() {
        return routingChart;
    }

    public void setRoutingChart(String routingChart) {
        this.routingChart = routingChart;
    }

    @Override
    public String getRoutingOrg() {
        return routingOrg;
    }

    public void setRoutingOrg(String routingOrg) {
        this.routingOrg = routingOrg;
    }

    @Override
    public PersonImpl getLookupProjectDirector() {
        return lookupProjectDirector;
    }

    public void setLookupProjectDirector(PersonImpl lookupProjectDirector) {
        this.lookupProjectDirector = lookupProjectDirector;
    }

    @Override
    public List<String> getSelectedAccounts() {
        return selectedAccounts;
    }

    public void setSelectedAccounts(List<String> selectedAccounts) {
        this.selectedAccounts = selectedAccounts;
    }

    @Override
    public String getLookupProjectDirectorUniversalIdentifier() {
        lookupProjectDirector = (PersonImpl) SpringContext.getBean(PersonService.class).updatePersonIfNecessary(
                lookupProjectDirectorUniversalIdentifier, lookupProjectDirector);
        return lookupProjectDirectorUniversalIdentifier;
    }

    public void setLookupProjectDirectorUniversalIdentifier(String lookupPersonId) {
        this.lookupProjectDirectorUniversalIdentifier = lookupPersonId;
    }

    @Override
    public String getUserLookupRoleNamespaceCode() {
        return userLookupRoleNamespaceCode;
    }

    @Override
    public String getUserLookupRoleName() {
        return userLookupRoleName;
    }

    /**
     * @return a String to represent this field on the inquiry
     */
    @Override
    public String getAwardInquiryTitle() {
        return SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(AWARD_INQUIRY_TITLE_PROPERTY);
    }

    /**
     * Pretends to set the inquiry title
     */
    public void setAwardInquiryTitle(String inquiryTitle) {
        // ain't nothing to do
    }

    @Override
    public String getBillingFrequencyCode() {
        return billingFrequencyCode;
    }

    public void setBillingFrequencyCode(String billingFrequencyCode) {
        this.billingFrequencyCode = billingFrequencyCode;
    }

    @Override
    public boolean isExcludedFromInvoicing() {
        return excludedFromInvoicing;
    }

    public void setExcludedFromInvoicing(boolean excludedFromInvoicing) {
        this.excludedFromInvoicing = excludedFromInvoicing;
    }

    @Override
    public boolean isAdditionalFormsRequiredIndicator() {
        return additionalFormsRequiredIndicator;
    }

    public void setAdditionalFormsRequiredIndicator(boolean additionalFormsRequiredIndicator) {
        this.additionalFormsRequiredIndicator = additionalFormsRequiredIndicator;
    }

    @Override
    public String getAdditionalFormsDescription() {
        return additionalFormsDescription;
    }

    public void setAdditionalFormsDescription(String additionalFormsDescription) {
        this.additionalFormsDescription = additionalFormsDescription;
    }

    @Override
    public String getInstrumentTypeCode() {
        return instrumentTypeCode;
    }

    public void setInstrumentTypeCode(String instrumentTypeCode) {
        this.instrumentTypeCode = instrumentTypeCode;
    }

    @Override
    public AccountsReceivableBillingFrequency getBillingFrequency() {
        if (billingFrequency == null || !StringUtils.equals(billingFrequency.getFrequency(), billingFrequencyCode)) {
            billingFrequency = SpringContext.getBean(KualiModuleService.class)
                .getResponsibleModuleService(AccountsReceivableBillingFrequency.class)
                .retrieveExternalizableBusinessObjectIfNecessary(this, billingFrequency,
                    CGPropertyConstants.BILLING_FREQUENCY);
        }
        return billingFrequency;
    }

    public void setBillingFrequency(AccountsReceivableBillingFrequency billingFrequency) {
        this.billingFrequency = billingFrequency;
    }

    @Override
    public boolean getAutoApproveIndicator() {
        return autoApproveIndicator;
    }

    public void setAutoApproveIndicator(boolean autoApproveIndicator) {
        this.autoApproveIndicator = autoApproveIndicator;
    }

    @Override
    public KualiDecimal getMinInvoiceAmount() {
        return minInvoiceAmount;
    }

    public void setMinInvoiceAmount(KualiDecimal minInvoiceAmount) {
        this.minInvoiceAmount = minInvoiceAmount;
    }

    @Override
    public String getInvoicingOptionCode() {
        return invoicingOptionCode;
    }

    @Override
    public String getCustomerNumber() {
        // if we don't have a customerNumber, but we do have an agency, we want to seed it from the agency so the
        // customer lookup will work appropriately
        if (agency != null && StringUtils.isBlank(customerNumber)) {
            customerNumber = agency.getCustomerNumber();
        }
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    @Override
    public Integer getCustomerAddressIdentifier() {
        return customerAddressIdentifier;
    }

    public void setCustomerAddressIdentifier(Integer customerAddressIdentifier) {
        this.customerAddressIdentifier = customerAddressIdentifier;
    }

    @Override
    public String getInvoicingOptionDescription() {
        return CGIntegrationConstants.AwardInvoicingOption.Types.get(invoicingOptionCode);
    }

    public void setInvoicingOptionCode(String invoicingOptionCode) {
        this.invoicingOptionCode = invoicingOptionCode;
    }

    public AccountsReceivableCustomerAddress getCustomerAddress() {
        if (customerAddress == null || !StringUtils.equals(customerAddress.getCustomerNumber(), customerNumber)
            || customerAddressIdentifier != null
            && customerAddressIdentifier.intValue() != customerAddress.getCustomerAddressIdentifier().intValue()) {
            customerAddress = SpringContext.getBean(KualiModuleService.class)
                    .getResponsibleModuleService(AccountsReceivableCustomer.class)
                    .retrieveExternalizableBusinessObjectIfNecessary(this, customerAddress, "customerAddress");
        }
        return customerAddress;
    }

    public void setCustomerAddress(AccountsReceivableCustomerAddress customerAddress) {
        this.customerAddress = customerAddress;
    }

    public AccountsReceivableMilestoneSchedule getMilestoneSchedule() {
        return milestoneSchedule;
    }

    public void setMilestoneSchedule(AccountsReceivableMilestoneSchedule milestoneSchedule) {
        this.milestoneSchedule = milestoneSchedule;
    }

    public AccountsReceivablePredeterminedBillingSchedule getPredeterminedBillingSchedule() {
        return predeterminedBillingSchedule;
    }

    public void setPredeterminedBillingSchedule(
            AccountsReceivablePredeterminedBillingSchedule predeterminedBillingSchedule) {
        this.predeterminedBillingSchedule = predeterminedBillingSchedule;
    }

    @Override
    public AwardProjectDirector getAwardPrimaryProjectDirector() {
        for (AwardProjectDirector awardProjectDirector : awardProjectDirectors) {
            if (awardProjectDirector != null && awardProjectDirector.isAwardPrimaryProjectDirectorIndicator()) {
                return awardProjectDirector;
            }
        }

        // typically, this value will be null, but we want to return it instead of null for the special cases
        // (PojoPropertyUtilsBean calls, tests) where the setter is called and this value is populated
        return awardPrimaryProjectDirector;
    }

    public void setAwardPrimaryProjectDirector(AwardProjectDirector awardPrimaryProjectDirector) {
        this.awardPrimaryProjectDirector = awardPrimaryProjectDirector;
    }

    /**
     * @return the awardPrimaryFundManager attribute. This field would not be persisted into the DB, just for display
     *         purposes.
     */
    @Override
    public AwardFundManager getAwardPrimaryFundManager() {
        for (AwardFundManager awdFundMgr : awardFundManagers) {
            if (awdFundMgr != null && awdFundMgr.isPrimaryFundManagerIndicator()) {
                return awdFundMgr;
            }
        }

        // typically, this value will be null, but we want to return it instead of null for the special cases
        // (PojoPropertyUtilsBean calls, tests) where the setter is called and this value is populated
        return awardPrimaryFundManager;
    }

    public void setAwardPrimaryFundManager(AwardFundManager awardPrimaryFundManager) {
        this.awardPrimaryFundManager = awardPrimaryFundManager;
    }

    @Override
    public String getLookupFundMgrPersonUniversalIdentifier() {
        return lookupFundMgrPersonUniversalIdentifier;
    }

    public void setLookupFundMgrPersonUniversalIdentifier(String lookupFundMgrPersonUniversalIdentifier) {
        this.lookupFundMgrPersonUniversalIdentifier = lookupFundMgrPersonUniversalIdentifier;
    }

    @Override
    public PersonImpl getLookupFundMgrPerson() {
        return lookupFundMgrPerson;
    }

    public void setLookupFundMgrPerson(PersonImpl lookupFundMgrPerson) {
        this.lookupFundMgrPerson = lookupFundMgrPerson;
    }

    @Override
    public Date getFundingExpirationDate() {
        return fundingExpirationDate;
    }

    public void setFundingExpirationDate(Date fundingExpirationDate) {
        this.fundingExpirationDate = fundingExpirationDate;
    }

    @Override
    public String getDunningCampaign() {
        return dunningCampaign;
    }

    public void setDunningCampaign(String dunningCampaign) {
        this.dunningCampaign = dunningCampaign;
    }

    @Override
    public boolean isStopWorkIndicator() {
        return stopWorkIndicator;
    }

    public void setStopWorkIndicator(boolean stopWorkIndicator) {
        this.stopWorkIndicator = stopWorkIndicator;
    }

    @Override
    public String getStopWorkReason() {
        return stopWorkReason;
    }

    public void setStopWorkReason(String stopWorkReason) {
        this.stopWorkReason = stopWorkReason;
    }

    public List<Note> getBoNotes() {
        List<Note> boNotes = new ArrayList<>();

        if (StringUtils.isNotBlank(getObjectId())) {
            boNotes = SpringContext.getBean(NoteService.class).getByRemoteObjectId(getObjectId());
        }

        return boNotes;
    }

    public void setBoNotes(List boNotes) {
        this.boNotes = boNotes;
    }

    public String getScheduleInquiryTitle() {
        return scheduleInquiryTitle;
    }

    public void setScheduleInquiryTitle(String scheduleInquiryTitle) {
        this.scheduleInquiryTitle = scheduleInquiryTitle;
    }

    public String getRemoveAddressButton() {
        return removeAddressButton;
    }

    public void setRemoveAddressButton(String removeAddressButton) {
        this.removeAddressButton = removeAddressButton;
    }

    public void clearCustomerAddressIfNecessary() {
        String originalAgencyNumber = ObjectUtils.isNull(agency) ? null : getAgency().getAgencyNumber();

        if (!StringUtils.equals(agencyNumber, originalAgencyNumber)) {
            customerAddressIdentifier = null;
            customerAddress = null;
            refreshReferenceObject("agency");
            customerNumber = ObjectUtils.isNull(agency) ? null : getAgency().getCustomerNumber();
        }
    }

    // CU Customization: Added getters and setters for new Award fields.
    public String getLetterOfCreditFundGroupCode() {
        return letterOfCreditFundGroupCode;
    }

    public void setLetterOfCreditFundGroupCode(String letterOfCreditFundGroupCode) {
        this.letterOfCreditFundGroupCode = letterOfCreditFundGroupCode;
    }

    public LetterOfCreditFundGroup getLetterOfCreditFundGroup() {
        return letterOfCreditFundGroup;
    }

    public void setLetterOfCreditFundGroup(LetterOfCreditFundGroup letterOfCreditFundGroup) {
        this.letterOfCreditFundGroup = letterOfCreditFundGroup;
    }

    public String getInvoiceLink() {
        return invoiceLink;
    }

    public void setInvoiceLink(String invoiceLink) {
        this.invoiceLink = invoiceLink;
    }

}
