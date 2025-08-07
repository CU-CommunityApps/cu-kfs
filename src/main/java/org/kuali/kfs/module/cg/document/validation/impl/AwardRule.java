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
package org.kuali.kfs.module.cg.document.validation.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.businessobject.Bill;
import org.kuali.kfs.module.ar.businessobject.Milestone;
import org.kuali.kfs.module.ar.businessobject.MilestoneSchedule;
import org.kuali.kfs.module.ar.businessobject.PredeterminedBillingSchedule;
import org.kuali.kfs.module.ar.document.service.ContractsGrantsInvoiceDocumentService;
import org.kuali.kfs.module.cg.CGKeyConstants;
import org.kuali.kfs.module.cg.CGPropertyConstants;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.businessobject.AwardAccount;
import org.kuali.kfs.module.cg.businessobject.AwardFundManager;
import org.kuali.kfs.module.cg.businessobject.AwardInvoicingOptionTypes;
import org.kuali.kfs.module.cg.businessobject.AwardOrganization;
import org.kuali.kfs.module.cg.businessobject.AwardProjectDirector;
import org.kuali.kfs.module.cg.businessobject.AwardSubcontractor;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.springframework.util.AutoPopulatingList;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Rules for the Award maintenance document.
 */
// Cornell customization: increase visibility for methods from private to protected so that they can be accessed
// from our local implementation of AwardExtensionRule
public class AwardRule extends CGMaintenanceDocumentRuleBase {

    private static final Logger LOG = LogManager.getLogger();

    protected Award newAwardCopy;
    protected Award oldAwardCopy;

    @Override
    protected boolean processCustomSaveDocumentBusinessRules(final MaintenanceDocument document) {
        LOG.debug("Entering AwardRule.processCustomSaveDocumentBusinessRules");

        processCustomRouteDocumentBusinessRules(document);

        LOG.info("Leaving AwardRule.processCustomSaveDocumentBusinessRules");

        // save despite error messages
        return true;
    }

    @Override
    protected boolean processCustomRouteDocumentBusinessRules(final MaintenanceDocument document) {
        LOG.debug("Entering AwardRule.processCustomRouteDocumentBusinessRules");

        boolean success = checkProposal();
        success &= checkEndAfterBegin(newAwardCopy.getAwardBeginningDate(), newAwardCopy.getAwardEndingDate(), KFSPropertyConstants.AWARD_ENDING_DATE);
        success &= checkPrimary(newAwardCopy.getAwardOrganizations(), AwardOrganization.class, KFSPropertyConstants.AWARD_ORGRANIZATIONS, Award.class);
        success &= checkPrimary(newAwardCopy.getAwardProjectDirectors(), AwardProjectDirector.class, KFSPropertyConstants.AWARD_PROJECT_DIRECTORS, Award.class);
        success &= checkForDuplicateAccounts();
        success &= checkForDuplicateAwardProjectDirector();
        success &= checkForDuplicateAwardFundManager();
        success &= checkForDuplicateAwardOrganization();
        success &= checkAccounts();
        success &= checkProjectDirectorsExist(newAwardCopy.getAwardProjectDirectors(), AwardProjectDirector.class, KFSPropertyConstants.AWARD_PROJECT_DIRECTORS);
        success &= checkFundManagersExist(newAwardCopy.getAwardFundManagers(), KFSPropertyConstants.AWARD_FUND_MANAGERS);
        success &= checkProjectDirectorsExist(newAwardCopy.getAwardAccounts(), AwardAccount.class, KFSPropertyConstants.AWARD_ACCOUNTS);
        success &= checkProjectDirectorsStatuses(newAwardCopy.getAwardProjectDirectors(), AwardProjectDirector.class, KFSPropertyConstants.AWARD_PROJECT_DIRECTORS);
        success &= checkFederalPassThrough();
        success &= checkExcludedFromInvoicing();
        success &= checkAgencyNotEqualToFederalPassThroughAgency(newAwardCopy.getAgency(),
                newAwardCopy.getFederalPassThroughAgency(), KFSPropertyConstants.AGENCY_NUMBER,
                KFSPropertyConstants.FEDERAL_PASS_THROUGH_AGENCY_NUMBER);
        success &= checkStopWorkReason();

        if (isContractsGrantsBillingEnhancementActive()) {
            success &= performContractsGrantsBillingChecks();
        }

        removeErrorMessagesIfInactiveAccountsExist();

        LOG.info("Leaving AwardRule.processCustomRouteDocumentBusinessRules");

        return success;
    }

    protected boolean performContractsGrantsBillingChecks() {
        boolean success = checkPrimary(newAwardCopy.getAwardFundManagers(), AwardFundManager.class,
                KFSPropertyConstants.AWARD_FUND_MANAGERS, Award.class);

        success &= checkConditionallyRequiredFields();
        success &= checkInvoicingOption();
        success &= checkBillingFrequency();
        success &= checkCustomerAddress();

        return success;
    }

    /**
     * Checks whether the award is excluded from invoicing.
     *
     * @return
     */
    protected boolean checkExcludedFromInvoicing() {
        if (newAwardCopy.isExcludedFromInvoicing()) {
            if (ObjectUtils.isNotNull(newAwardCopy.getExcludedFromInvoicingReason())) {
                return true;
            } else {
                putFieldError(KFSPropertyConstants.EXCLUDED_FROM_INVOICING_REASON, KFSKeyConstants.ERROR_EXCLUDED_FROM_INVOICING_REASON_REQUIRED);
                return false;
            }
        }
        return true;
    }

    /**
     * checks to see if at least 1 award account exists
     *
     * @return true if the award contains at least 1 {@link AwardAccount}, false otherwise
     */
    protected boolean checkAccounts() {
        boolean success = true;
        final Collection<AwardAccount> awardAccounts = newAwardCopy.getAwardAccounts();

        if (ObjectUtils.isNull(awardAccounts) || awardAccounts.isEmpty()) {
            final String elementLabel = getDataDictionaryService().getCollectionElementLabel(Award.class.getName(),
                    KFSPropertyConstants.AWARD_ACCOUNTS, AwardAccount.class);
            putFieldError(KFSPropertyConstants.AWARD_ACCOUNTS, KFSKeyConstants.ERROR_ONE_REQUIRED, elementLabel);
            success = false;
        }

        return success;
    }

    /**
     * checks to see if a proposal has already been awarded
     *
     * @return false if the proposal has already been awarded true otherwise
     */
    protected boolean checkProposal() {
        if (isProposalAwarded(newAwardCopy)) {
            putFieldError(KFSPropertyConstants.PROPOSAL_NUMBER, KFSKeyConstants.ERROR_AWARD_PROPOSAL_AWARDED,
                newAwardCopy.getProposalNumber());
            return false;
        }

        return true;
    }

    /**
     * checks if the required federal pass through fields are filled in if the federal pass through indicator is yes
     *
     * @return false if federal pass through fields are required and not filled in, true otherwise
     */
    protected boolean checkFederalPassThrough() {
        boolean success = super.checkFederalPassThrough(newAwardCopy.getFederalPassThroughIndicator(),
            newAwardCopy.getAgency(), newAwardCopy.getFederalPassThroughAgencyNumber(), Award.class,
            KFSPropertyConstants.FEDERAL_PASS_THROUGH_INDICATOR);

        if (newAwardCopy.getFederalPassThroughIndicator()) {
            if (StringUtils.isBlank(newAwardCopy.getFederalPassThroughAgencyNumber())) {
                putFieldError(KFSPropertyConstants.FEDERAL_PASS_THROUGH_AGENCY_NUMBER,
                    KFSKeyConstants.ERROR_FPT_AGENCY_NUMBER_REQUIRED);
                success = false;
            }
        }

        return success;
    }

    @Override
    public void setupConvenienceObjects() {
        newAwardCopy = (Award) super.getNewBo();
        oldAwardCopy = (Award) super.getOldBo();
    }

    @Override
    public boolean processCustomAddCollectionLineBusinessRules(
            final MaintenanceDocument document, final String collectionName,
            final PersistableBusinessObject bo) {
        boolean success = true;

        if (bo instanceof AwardProjectDirector) {
            final AwardProjectDirector awardProjectDirector = (AwardProjectDirector) bo;
            success = checkAwardProjectDirector(awardProjectDirector);
        } else if (bo instanceof AwardFundManager) {
            final AwardFundManager awardFundManager = (AwardFundManager) bo;
            success = checkAwardFundManager(awardFundManager);
        } else if (bo instanceof AwardAccount) {
            final AwardAccount awardAccount = (AwardAccount) bo;
            success = checkAwardAccount(awardAccount);
        } else if (bo instanceof AwardSubcontractor) {
            final AwardSubcontractor awardSubcontractor = (AwardSubcontractor) bo;
            success = checkAwardSubcontractor(awardSubcontractor);
        } else if (bo instanceof AwardOrganization) {
            final AwardOrganization awardOrganization = (AwardOrganization) bo;
            success = checkAwardOrganization(awardOrganization);
        }

        return success;
    }

    /**
     * Overrides the method in MaintenanceDocumentRuleBase to give error message to the user when the user tries to add multiple
     * Primary Fund Managers. At most one Primary Fund Manager is allowed. contract.
     */
    @Override
    public boolean processAddCollectionLineBusinessRules(
            final MaintenanceDocument document, final String collectionName,
            final PersistableBusinessObject line) {
        if (line instanceof AwardFundManager) {
            final AwardFundManager newAwardFundManager = (AwardFundManager) line;
            if (collectionName.equals(CGPropertyConstants.AWARD_FUND_MANAGERS)) {
                newAwardCopy = (Award) document.getNewMaintainableObject().getBusinessObject();
                final List<AwardFundManager> awardFundManagers = newAwardCopy.getAwardFundManagers();

                // Check if there is already an Award Primary Fund Manager in the collection lines.
                int count = 0;
                for (final AwardFundManager awardFundManager : awardFundManagers) {
                    if (awardFundManager.isPrimary()) {
                        count++;
                        if (newAwardFundManager.isPrimary()) {
                            final String elementLabel = getDataDictionaryService()
                                    .getCollectionElementLabel(Award.class.getName(), collectionName, AwardFundManager.class);
                            putFieldError(collectionName, KFSKeyConstants.ERROR_MULTIPLE_PRIMARY, elementLabel);
                            return false;
                        }
                    }
                }

                if (count > 1) {
                    final String elementLabel = getDataDictionaryService()
                            .getCollectionElementLabel(Award.class.getName(), collectionName, AwardFundManager.class);
                    putFieldError(collectionName, KFSKeyConstants.ERROR_MULTIPLE_PRIMARY, elementLabel);
                    return false;
                }
            }
        }

        return super.processAddCollectionLineBusinessRules(document, collectionName, line);
    }

    /**
     * check if the given award organization exists
     *
     * @param awardOrganization
     * @return
     */
    protected boolean checkAwardOrganization(final AwardOrganization awardOrganization) {
        final int originalErrorCount = GlobalVariables.getMessageMap().getErrorCount();
        final String errorPathPrefix = KFSConstants.MAINTENANCE_ADD_PREFIX + KFSPropertyConstants.AWARD_ORGRANIZATIONS + ".";

        getDictionaryValidationService().validateBusinessObject(awardOrganization);
        if (StringUtils.isNotBlank(awardOrganization.getOrganizationCode())
                && StringUtils.isNotBlank(awardOrganization.getChartOfAccountsCode())) {
            awardOrganization.refreshReferenceObject(KFSPropertyConstants.ORGANIZATION);

            if (ObjectUtils.isNull(awardOrganization.getOrganization())) {
                final String label = getDataDictionaryService().getAttributeLabel(AwardOrganization.class,
                        KFSPropertyConstants.ORGANIZATION_CODE);
                final String message = label + "(" + awardOrganization.getOrganizationCode() + ")";

                putFieldError(errorPathPrefix + KFSPropertyConstants.ORGANIZATION_CODE,
                        KFSKeyConstants.ERROR_EXISTENCE, message);
            }
        }

        return GlobalVariables.getMessageMap().getErrorCount() == originalErrorCount;
    }

    /**
     * check if the given award subcontractor exists
     *
     * @param awardSubcontractor
     * @return
     */
    protected boolean checkAwardSubcontractor(final AwardSubcontractor awardSubcontractor) {
        final int originalErrorCount = GlobalVariables.getMessageMap().getErrorCount();
        final String errorPathPrefix = KFSConstants.MAINTENANCE_ADD_PREFIX + KFSPropertyConstants.AWARD_SUBCONTRACTORS + ".";

        getDictionaryValidationService().validateBusinessObject(awardSubcontractor);
        if (StringUtils.isNotBlank(awardSubcontractor.getSubcontractorNumber())) {
            awardSubcontractor.refreshReferenceObject("subcontractor");

            if (ObjectUtils.isNull(awardSubcontractor.getSubcontractor())) {
                final String label = getDataDictionaryService().getAttributeLabel(AwardSubcontractor.class,
                        KFSPropertyConstants.SUBCONTRACTOR_NUMBER);
                final String message = label + "(" + awardSubcontractor.getSubcontractorNumber() + ")";

                putFieldError(errorPathPrefix + KFSPropertyConstants.SUBCONTRACTOR_NUMBER,
                        KFSKeyConstants.ERROR_EXISTENCE, message);
            }
        }

        return GlobalVariables.getMessageMap().getErrorCount() == originalErrorCount;
    }

    /**
     * check if the given award account exists
     *
     * @param awardAccount
     * @return
     */
    protected boolean checkAwardAccount(final AwardAccount awardAccount) {
        final int originalErrorCount = GlobalVariables.getMessageMap().getErrorCount();
        final String errorPathPrefix = KFSConstants.MAINTENANCE_ADD_PREFIX + KFSPropertyConstants.AWARD_ACCOUNTS + ".";

        getDictionaryValidationService().validateBusinessObject(awardAccount);
        if (StringUtils.isNotBlank(awardAccount.getAccountNumber()) && StringUtils.isNotBlank(awardAccount.getChartOfAccountsCode())) {
            awardAccount.refreshReferenceObject(KFSPropertyConstants.ACCOUNT);

            if (ObjectUtils.isNull(awardAccount.getAccount())) {
                final String label = getDataDictionaryService().getAttributeLabel(AwardAccount.class, KFSPropertyConstants.ACCOUNT_NUMBER);
                final String message = label + "(" + awardAccount.getChartOfAccountsCode() + "-" + awardAccount.getAccountNumber() + ")";

                putFieldError(errorPathPrefix + KFSPropertyConstants.ACCOUNT_NUMBER, KFSKeyConstants.ERROR_EXISTENCE, message);
            }
        }

        final Person projectDirector = awardAccount.getProjectDirector();
        if (StringUtils.isBlank(awardAccount.getPrincipalId()) || ObjectUtils.isNull(projectDirector)) {
            final String label = getDataDictionaryService().getAttributeLabel(AwardAccount.class, "projectDirector.principalName");
            final String message = label + "(" + awardAccount.getPrincipalId() + ")";

            putFieldError(errorPathPrefix + "projectDirector.principalName",
                    KFSKeyConstants.ERROR_EXISTENCE, message);
        }

        return GlobalVariables.getMessageMap().getErrorCount() == originalErrorCount;
    }

    /**
     * check if the given award project director exists
     *
     * @param awardProjectDirector
     * @return
     */
    protected boolean checkAwardProjectDirector(final AwardProjectDirector awardProjectDirector) {
        boolean success = true;

        final Person projectDirector = awardProjectDirector.getProjectDirector();
        if (StringUtils.isBlank(awardProjectDirector.getPrincipalId()) || ObjectUtils.isNull(projectDirector)) {
            final String errorPath = KFSConstants.MAINTENANCE_ADD_PREFIX + KFSPropertyConstants.AWARD_PROJECT_DIRECTORS +
                                     "." + "projectDirector.principalName";
            final String label = getDataDictionaryService().getAttributeLabel(AwardProjectDirector.class,
                    "projectDirector.principalName");
            final String message = label + "(" + awardProjectDirector.getPrincipalId() + ")";

            putFieldError(errorPath, KFSKeyConstants.ERROR_EXISTENCE, message);

            success = false;
        }
        return success;
    }

    protected boolean checkForDuplicateAccounts() {
        String accountNumber;
        String accountChart;
        final Collection<AwardAccount> awardAccounts = newAwardCopy.getAwardAccounts();
        final HashSet<String> accountHash = new HashSet<>();

        //validate if the newly entered award account is already on that award
        for (final AwardAccount account : awardAccounts) {
            if (account != null && StringUtils.isNotEmpty(account.getAccountNumber())) {
                accountNumber = account.getAccountNumber();
                accountChart = account.getChartOfAccountsCode();
                if (!accountHash.add(accountChart + accountNumber)) {
                    putFieldError(KFSPropertyConstants.AWARD_ACCOUNTS, CGKeyConstants.ERROR_DUPLICATE_AWARD_ACCOUNT,
                            accountChart + "-" + accountNumber);
                    return false;
                }
            }
        }
        return true;
    }

    protected boolean checkForDuplicateAwardProjectDirector() {
        String principalId;
        final Collection<AwardProjectDirector> awardProjectDirectors = newAwardCopy.getAwardProjectDirectors();
        final HashSet<String> principalIdHash = new HashSet<>();

        //validate if the newly entered AwardProjectDirector is already on that award
        for (final AwardProjectDirector projectDirector : awardProjectDirectors) {
            if (projectDirector != null && StringUtils.isNotEmpty(projectDirector.getPrincipalId())) {
                principalId = projectDirector.getPrincipalId();
                if (!principalIdHash.add(principalId)) {
                    putFieldError(KFSPropertyConstants.AWARD_PROJECT_DIRECTORS,
                            CGKeyConstants.ERROR_DUPLICATE_AWARD_PROJECT_DIRECTOR, principalId);
                    return false;
                }
            }
        }
        return true;
    }

    protected boolean checkForDuplicateAwardFundManager() {
        String principalId;
        final Collection<AwardFundManager> awardFundManagers = newAwardCopy.getAwardFundManagers();
        final Set<String> principalIdHash = new HashSet<>();

        for (final AwardFundManager fundManager : awardFundManagers) {
            if (fundManager != null && StringUtils.isNotEmpty(fundManager.getPrincipalId())) {
                principalId = fundManager.getPrincipalId();
                if (!principalIdHash.add(principalId)) {
                    putFieldError(KFSPropertyConstants.AWARD_FUND_MANAGERS,
                            CGKeyConstants.ERROR_DUPLICATE_AWARD_FUND_MANAGER, principalId);
                    return false;
                }
            }
        }
        return true;
    }

    protected boolean checkForDuplicateAwardOrganization() {
        String organizationCode;
        String organizationChart;
        final Collection<AwardOrganization> awardOrganizations = newAwardCopy.getAwardOrganizations();
        final HashSet<String> organizationHash = new HashSet<>();

        //validate if the newly entered awardOrganization is already on that award
        for (final AwardOrganization awardOrganization : awardOrganizations) {
            if (awardOrganization != null && StringUtils.isNotEmpty(awardOrganization.getOrganizationCode())) {
                organizationCode = awardOrganization.getOrganizationCode();
                organizationChart = awardOrganization.getChartOfAccountsCode();
                if (!organizationHash.add(organizationChart + organizationCode)) {
                    putFieldError(KFSPropertyConstants.AWARD_ORGRANIZATIONS,
                            CGKeyConstants.ERROR_DUPLICATE_AWARD_ORGANIZATION, organizationChart + "-" +
                                    organizationCode);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * check if the given award fund manager exists
     *
     * @param awardFundManager
     * @return
     */
    protected boolean checkAwardFundManager(final AwardFundManager awardFundManager) {
        boolean success = true;

        final Person fundManager = awardFundManager.getFundManager();
        if (isContractsGrantsBillingEnhancementActive()) {
            if (StringUtils.isBlank(awardFundManager.getPrincipalId()) || ObjectUtils.isNull(fundManager)) {
                final String errorPath = KFSConstants.MAINTENANCE_ADD_PREFIX + KFSPropertyConstants.AWARD_FUND_MANAGERS +
                                         "." + "fundManager.principalName";
                final String label = getDataDictionaryService().getAttributeLabel(AwardFundManager.class,
                        "fundManager.principalName");
                final String message = label + "(" + awardFundManager.getPrincipalId() + ")";

                putFieldError(errorPath, KFSKeyConstants.ERROR_EXISTENCE, message);

                success = false;
            }
        }
        return success;
    }

    /**
     * @return false if invoicing option is selected without billing frequency or vice versa, true otherwise
     */
    protected boolean checkConditionallyRequiredFields() {
        final String billingFrequencyCode = newAwardCopy.getBillingFrequencyCode();
        final String invoicingOptionCode = newAwardCopy.getInvoicingOptionCode();

        final String billingFrequencyLabel = getDataDictionaryService()
                .getAttributeLabel(Award.class, CGPropertyConstants.AwardFields.BILLING_FREQUENCY_CODE);
        final String invoicingOptionLabel = getDataDictionaryService()
                .getAttributeLabel(Award.class, CGPropertyConstants.AwardFields.INVOICING_OPTION_CODE);

        if (StringUtils.isNotBlank(billingFrequencyCode) && StringUtils.isBlank(invoicingOptionCode)) {
            putFieldError(CGPropertyConstants.AwardFields.INVOICING_OPTION_CODE,
                    CGKeyConstants.AwardConstants.ERROR_CG_BILLING_FREQUENCY_AND_INVOICING_OPTION_REQUIRED,
                    new String[]{invoicingOptionLabel, billingFrequencyLabel});
            return false;
        }

        if (StringUtils.isNotBlank(invoicingOptionCode) && StringUtils.isBlank(billingFrequencyCode)) {
            putFieldError(CGPropertyConstants.AwardFields.BILLING_FREQUENCY_CODE,
                    CGKeyConstants.AwardConstants.ERROR_CG_BILLING_FREQUENCY_AND_INVOICING_OPTION_REQUIRED,
                    new String[]{billingFrequencyLabel, invoicingOptionLabel});
            return false;
        }

        return true;
    }

    /**
     * This method checks the Contract Control account set for Award Account based on award's invoicing option and also
     * checks if the invoicing option is valid based on the selected billing frequency.
     *
     * @return
     */
    protected boolean checkInvoicingOption() {
        boolean success = true;
        final List<String> errorString =
                getContractsGrantsInvoiceDocumentService().checkAwardContractControlAccounts(newAwardCopy);

        if (CollectionUtils.isNotEmpty(errorString) && errorString.size() > 1) {
            success = false;
            putFieldError(CGPropertyConstants.AwardFields.INVOICING_OPTION_CODE, errorString.get(0), errorString.get(1));
        }

        if (isInvalidInvoicingOption()) {
            success = false;
            putFieldError(CGPropertyConstants.AwardFields.INVOICING_OPTION_CODE,
                    CGKeyConstants.AwardConstants.ERROR_CG_INVALID_INVOICING_OPTION,
                    new String[]{newAwardCopy.getInvoicingOptionDescription(),
                            newAwardCopy.getBillingFrequency().getFrequencyDescription()});
        }

        return success;
    }

    private boolean isInvalidInvoicingOption() {
        final String billingFrequencyCode = newAwardCopy.getBillingFrequencyCode();

        final boolean isScheduledBilling = StringUtils.equals(billingFrequencyCode,
                ArConstants.BillingFrequencyValues.MILESTONE.getCode()
        )
                || StringUtils.equals(billingFrequencyCode,
                ArConstants.BillingFrequencyValues.PREDETERMINED_BILLING.getCode()
        );

        final boolean isScheduledInvoicingOptionCode = StringUtils.equals(
                AwardInvoicingOptionTypes.SCHEDULE.getCode(),
                newAwardCopy.getInvoicingOptionCode());

        return isScheduledBilling && !isScheduledInvoicingOptionCode
               || !isScheduledBilling && isScheduledInvoicingOptionCode;
    }

    /**
     * This method checks if the Stop Work Reason has been entered if the Stop Work flag has been checked.
     *
     * @return true if Stop Work flag hasn't been checked, or if it has been checked and the Stop Work Reason has
     *         been entered, false otherwise
     */
    protected boolean checkStopWorkReason() {
        boolean success = true;
        if (newAwardCopy.isStopWorkIndicator()) {
            if (StringUtils.isBlank(newAwardCopy.getStopWorkReason())) {
                success = false;
                putFieldError(KFSPropertyConstants.STOP_WORK_REASON, KFSKeyConstants.ERROR_STOP_WORK_REASON_REQUIRED);
            }
        }
        return success;
    }

    /**
     * Checks if the user tries to change the billing frequency with active Milestones or Bills, and if so
     * returns an error.
     *
     * @return true if the billing frequency can be changed, false otherwise
     */
    protected boolean checkBillingFrequency() {
        boolean success = true;

        final String newBillingFrequencyCode = newAwardCopy.getBillingFrequencyCode();
        final String oldBillingFrequencyCode = oldAwardCopy.getBillingFrequencyCode();

        if (!StringUtils.equals(newBillingFrequencyCode, oldBillingFrequencyCode)) {
            final String proposalNumber = newAwardCopy.getProposalNumber();
            for (final AwardAccount awardAccount: newAwardCopy.getActiveAwardAccounts()) {
                if (StringUtils.equals(oldBillingFrequencyCode, ArConstants.BillingFrequencyValues.MILESTONE.getCode())
                    && hasActiveUnbilledMilestones(proposalNumber,
                        awardAccount.getChartOfAccountsCode(),
                        awardAccount.getAccountNumber()
                )) {
                    success = false;
                    putFieldError(CGPropertyConstants.AwardFields.BILLING_FREQUENCY_CODE,
                        CGKeyConstants.AwardConstants.ERROR_CG_ACTIVE_MILESTONES_EXIST,
                        newAwardCopy.getBillingFrequency().getFrequencyDescription());
                    break;
                } else if (StringUtils.equals(oldBillingFrequencyCode,
                        ArConstants.BillingFrequencyValues.PREDETERMINED_BILLING.getCode()
                ) && hasActiveUnbilledBills(proposalNumber,
                        awardAccount.getChartOfAccountsCode(),
                        awardAccount.getAccountNumber()
                )) {
                    success = false;
                    putFieldError(CGPropertyConstants.AwardFields.BILLING_FREQUENCY_CODE,
                        CGKeyConstants.AwardConstants.ERROR_CG_ACTIVE_BILLS_EXIST,
                        newAwardCopy.getBillingFrequency().getFrequencyDescription());
                    break;
                }
            }
        }

        return success;
    }
    
    // CU customization: increase visibility
    protected boolean hasActiveUnbilledMilestones(
            final String proposalNumber,
            final String chartOfAccountsCode,
            final String accountNumber
    ) {
        final Map<String, Object> primaryKeys = new HashMap<>();
        primaryKeys.put(KFSPropertyConstants.PROPOSAL_NUMBER, proposalNumber);
        primaryKeys.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartOfAccountsCode);
        primaryKeys.put(KFSPropertyConstants.ACCOUNT_NUMBER, accountNumber);

        final MilestoneSchedule schedule = getBoService().findByPrimaryKey(MilestoneSchedule.class, primaryKeys);
        if (ObjectUtils.isNotNull(schedule)) {
            for (final Milestone milestone : schedule.getMilestones()) {
                if (milestone.isActive() && !milestone.isBilled()) {
                    return true;
                }
            }
        }

        return false;
    }

    // CU customization: increase visibility
    protected boolean hasActiveUnbilledBills(
            final String proposalNumber,
            final String chartOfAccountsCode,
            final String accountNumber
    ) {
        final Map<String, Object> primaryKeys = new HashMap<>();
        primaryKeys.put(KFSPropertyConstants.PROPOSAL_NUMBER, proposalNumber);
        primaryKeys.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartOfAccountsCode);
        primaryKeys.put(KFSPropertyConstants.ACCOUNT_NUMBER, accountNumber);

        final PredeterminedBillingSchedule schedule =
                getBoService().findByPrimaryKey(PredeterminedBillingSchedule.class, primaryKeys);

        if (ObjectUtils.isNotNull(schedule)) {
            for (final Bill bill : schedule.getBills()) {
                if (bill.isActive() && !bill.isBilled()) {
                    return true;
                }
            }
        }

        return false;
    }

    protected boolean checkCustomerAddress() {
        if (newAwardCopy.getAgency() != null &&
                !StringUtils.equals(newAwardCopy.getCustomerNumber(), newAwardCopy.getAgency().getCustomerNumber())) {
            putFieldError(CGPropertyConstants.AwardFields.CUSTOMER_ADDRESS_IDENTIFIER,
                    CGKeyConstants.AwardConstants.ERROR_CG_BILLING_CUSTOMER_ADDRESS_MUST_MATCH_AGENCY_CUSTOMER);
            return false;
        }
        return true;
    }

    /**
     * If inactive accounts are associated with an existing award, on save the error messages
     * for this validation need to be removed as this is valid.
     */
    private void removeErrorMessagesIfInactiveAccountsExist() {
        final Collection<AwardAccount> newAwardAccounts = newAwardCopy.getAwardAccounts();
        final Collection<String> oldAwardAccounts = oldAwardCopy.getAwardAccounts().stream().map(AwardAccount::getObjectId).collect(Collectors.toList());

        int pos = 0;
        // If the award is not new, and is inactive, remove global error message.
        // The global validation for active documents necessitates this change.
        for (final AwardAccount account : newAwardAccounts) {
            if (account != null && oldAwardAccounts.contains(account.getObjectId()) && !account.getAccount().isActive()) {
                final String mapKey = String.format("document.newMaintainableObject.%s[%d].%s", KFSPropertyConstants.AWARD_ACCOUNTS, pos, KFSPropertyConstants.ACCOUNT_NUMBER);
                final AutoPopulatingList<ErrorMessage> errorList = GlobalVariables.getMessageMap().getErrorMessages().get(mapKey);
                if (errorList != null) {
                    errorList.removeIf(t -> Objects.equals(t.getErrorKey(), KFSKeyConstants.ERROR_INACTIVE) && t.getMessageParameters().length > 0 && Objects.equals(t.getMessageParameters()[0], "Account Number"));
                    if (errorList.isEmpty()) {
                        GlobalVariables.getMessageMap().removeAllErrorMessagesForProperty(mapKey);
                    }
                }
            }
            pos++;
        }
    }

    /*
     * Wrapping static utility class in a method so tests can use a spy to mock this call; this way,
     * static mocking is not necessary.
     */
    ContractsGrantsInvoiceDocumentService getContractsGrantsInvoiceDocumentService() {
        return SpringContext.getBean(ContractsGrantsInvoiceDocumentService.class);
    }

    /*
     * Wrapping static utility class in a method so tests can use a spy to mock this call; this way,
     * static mocking is not necessary.
     *
     * TODO: Can the overridden one, using KNSServiceLocator, be used instead?
     */
    @Override
    protected DataDictionaryService getDataDictionaryService() {
        return SpringContext.getBean(DataDictionaryService.class);
    }

    /*
     * Wrapping static utility class in a method so tests can use a spy to mock this call; this way,
     * static mocking is not necessary.
     */
    boolean isProposalAwarded(final Award award) {
        return AwardRuleUtil.isProposalAwarded(award);
    }

}
