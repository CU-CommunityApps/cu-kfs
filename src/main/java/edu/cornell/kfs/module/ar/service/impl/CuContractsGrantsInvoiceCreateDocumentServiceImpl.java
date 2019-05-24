package edu.cornell.kfs.module.ar.service.impl;

import java.sql.Date;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAward;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAwardAccount;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.ArKeyConstants;
import org.kuali.kfs.module.ar.businessobject.ContractsGrantsInvoiceDocumentErrorLog;
import org.kuali.kfs.module.ar.businessobject.ContractsGrantsInvoiceDocumentErrorMessage;
import org.kuali.kfs.module.ar.businessobject.ContractsGrantsLetterOfCreditReviewDetail;
import org.kuali.kfs.module.ar.businessobject.InvoiceAccountDetail;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.service.impl.ContractsGrantsInvoiceCreateDocumentServiceImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.ChartOrgHolder;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.document.validation.event.DocumentSystemSaveEvent;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.exception.WorkflowException;

import edu.cornell.kfs.module.ar.CuArParameterConstants;
import edu.cornell.kfs.module.ar.CuArParameterKeyConstants;
import edu.cornell.kfs.module.ar.batch.service.CuVerifyBillingFrequencyService;
import edu.cornell.kfs.module.ar.document.service.CuContractsGrantsInvoiceDocumentService;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

public class CuContractsGrantsInvoiceCreateDocumentServiceImpl extends ContractsGrantsInvoiceCreateDocumentServiceImpl {
    
    private static final Logger LOG = LogManager.getLogger(CuContractsGrantsInvoiceCreateDocumentServiceImpl.class);
    
    protected CuContractsGrantsInvoiceDocumentService cuContractsGrantsInvoiceDocumentService;
    
    /**
     * Fixes NullPointerException that can occur when getting the award total amount.
     * award.getAwardTotalAmount().bigDecimalValue() causes the NullPointerException if the total amount returned null.
     */
    @Override
    protected void storeValidationErrors(Map<ContractsAndGrantsBillingAward, List<String>> invalidGroup, Collection<ContractsGrantsInvoiceDocumentErrorLog> contractsGrantsInvoiceDocumentErrorLogs, String creationProcessTypeCode) {
        for (ContractsAndGrantsBillingAward award : invalidGroup.keySet()) {
            KualiDecimal cumulativeExpenses = KualiDecimal.ZERO;
            ContractsGrantsInvoiceDocumentErrorLog contractsGrantsInvoiceDocumentErrorLog = new ContractsGrantsInvoiceDocumentErrorLog();

            if (ObjectUtils.isNotNull(award)) {
                Date beginningDate = award.getAwardBeginningDate();
                Date endingDate = award.getAwardEndingDate();
                final SystemOptions systemOptions = optionsService.getCurrentYearOptions();

                contractsGrantsInvoiceDocumentErrorLog.setProposalNumber(award.getProposalNumber());
                contractsGrantsInvoiceDocumentErrorLog.setAwardBeginningDate(beginningDate);
                contractsGrantsInvoiceDocumentErrorLog.setAwardEndingDate(endingDate);
                KualiDecimal awardTotalAmount = award.getAwardTotalAmount() == null ? KualiDecimal.ZERO : award.getAwardTotalAmount();
                contractsGrantsInvoiceDocumentErrorLog.setAwardTotalAmount(awardTotalAmount.bigDecimalValue());
                if (ObjectUtils.isNotNull(award.getAwardPrimaryFundManager())) {
                    contractsGrantsInvoiceDocumentErrorLog.setPrimaryFundManagerPrincipalId(award.getAwardPrimaryFundManager().getPrincipalId());
                }
                if (!CollectionUtils.isEmpty(award.getActiveAwardAccounts())) {
                    boolean firstLineFlag = true;

                    for (ContractsAndGrantsBillingAwardAccount awardAccount : award.getActiveAwardAccounts()) {

                        cumulativeExpenses = cumulativeExpenses.add(contractsGrantsInvoiceDocumentService.getBudgetAndActualsForAwardAccount(awardAccount, systemOptions.getActualFinancialBalanceTypeCd()));
                        if (firstLineFlag) {
                            firstLineFlag = false;
                            contractsGrantsInvoiceDocumentErrorLog.setAccounts(awardAccount.getAccountNumber());
                        } else {
                            contractsGrantsInvoiceDocumentErrorLog.setAccounts(contractsGrantsInvoiceDocumentErrorLog.getAccounts() + ";" + awardAccount.getAccountNumber());
                        }
                    }
                }
                contractsGrantsInvoiceDocumentErrorLog.setCumulativeExpensesAmount(cumulativeExpenses.bigDecimalValue());
            }

            for (String vCat : invalidGroup.get(award)) {
                ContractsGrantsInvoiceDocumentErrorMessage contractsGrantsInvoiceDocumentErrorCategory = new ContractsGrantsInvoiceDocumentErrorMessage();
                contractsGrantsInvoiceDocumentErrorCategory.setErrorMessageText(vCat);
                contractsGrantsInvoiceDocumentErrorLog.getErrorMessages().add(contractsGrantsInvoiceDocumentErrorCategory);
            }

            contractsGrantsInvoiceDocumentErrorLog.setErrorDate(dateTimeService.getCurrentTimestamp());
            contractsGrantsInvoiceDocumentErrorLog.setCreationProcessTypeCode(creationProcessTypeCode);
            businessObjectService.save(contractsGrantsInvoiceDocumentErrorLog);
            contractsGrantsInvoiceDocumentErrorLogs.add(contractsGrantsInvoiceDocumentErrorLog);
        }
    }
    
    @Override
    public ContractsGrantsInvoiceDocument createCGInvoiceDocumentByAwardInfo(ContractsAndGrantsBillingAward awd,
            List<ContractsAndGrantsBillingAwardAccount> accounts, String chartOfAccountsCode, String organizationCode,
            List<ErrorMessage> errorMessages, List<ContractsGrantsLetterOfCreditReviewDetail> accountDetails, String locCreationType) {
        ContractsGrantsInvoiceDocument cgInvoiceDocument = 
                super.createCGInvoiceDocumentByAwardInfo(awd, accounts, chartOfAccountsCode, organizationCode, errorMessages, accountDetails, locCreationType);
        if (ObjectUtils.isNotNull(cgInvoiceDocument)) {
            populateDocumentDescription(cgInvoiceDocument);
        }
        return cgInvoiceDocument;
    }

    protected void populateDocumentDescription(ContractsGrantsInvoiceDocument cgInvoiceDocument) {
        String proposalNumber = cgInvoiceDocument.getInvoiceGeneralDetail().getProposalNumber();
        if (StringUtils.isNotBlank(proposalNumber)) {
            String contractControlAccount = findContractControlAccountNumber(cgInvoiceDocument.getAccountDetails());
            String newTitle =  MessageFormat.format(findTitleFormatString(), proposalNumber, contractControlAccount);
            cgInvoiceDocument.getDocumentHeader().setDocumentDescription(newTitle);
        }
    }
    
    protected String findContractControlAccountNumber(List<InvoiceAccountDetail> details) {
        for (InvoiceAccountDetail detail : details) {
            Account contractControlAccount = getCuContractsGrantsInvoiceDocumentService().determineContractControlAccount(detail);
            if (ObjectUtils.isNotNull(contractControlAccount) 
                    && StringUtils.isNotBlank(contractControlAccount.getAccountNumber())) {
                return contractControlAccount.getAccountNumber();
            }
        }
        return StringUtils.EMPTY;
    }
    
    protected String findTitleFormatString() {
        return getConfigurationService().getPropertyValueAsString(CuArParameterConstants.CONTRACTS_GRANTS_INVOICE_DOCUMENT_TITLE_FORMAT);
    }
    
    /**
     * Added evaluation of account sub-fund not being an expenditure to existing base code evaluation
     * of billing frequency and invoice document status when determining valid award accounts.
     * Expenditure sub-funds associated to accounts to exclude are defined in CG system parameter
     * CG_INVOICING_EXCLUDE_EXPENSES_SUB_FUNDS.
     */
    @Override
    protected List<ContractsAndGrantsBillingAwardAccount> getValidAwardAccounts(
            List<ContractsAndGrantsBillingAwardAccount> awardAccounts, ContractsAndGrantsBillingAward award) {
        if (!ArConstants.BillingFrequencyValues.isMilestone(award) && !ArConstants.BillingFrequencyValues.isPredeterminedBilling(award)) {
            List<ContractsAndGrantsBillingAwardAccount> validAwardAccounts = new ArrayList<>();
            Set<Account> invalidAccounts = harvestAccountsFromContractsGrantsInvoices(getInProgressInvoicesForAward(award));

            for (ContractsAndGrantsBillingAwardAccount awardAccount : awardAccounts) {
                if (!invalidAccounts.contains(awardAccount.getAccount())) {
                    if (verifyBillingFrequencyService.validateBillingFrequency(award, awardAccount)
                            && isNotExpenditureAccount(awardAccount)) {
                        validAwardAccounts.add(awardAccount);
                    }
                }
            }

            return validAwardAccounts;
        } else {
            return awardAccounts;
        }
    }
    
    protected boolean isNotExpenditureAccount(ContractsAndGrantsBillingAwardAccount billingAwardAccount) {
        Account accountLinkedToAward = getAccountService().getByPrimaryId(billingAwardAccount.getChartOfAccountsCode(), billingAwardAccount.getAccountNumber());
        if (ObjectUtils.isNotNull(accountLinkedToAward)) {
            return !isExpenditureSubFund(accountLinkedToAward.getSubFundGroupCode());
        }
        return true;
    }
    
    protected boolean isExpenditureSubFund(String subFundGroupCode) {
        if (StringUtils.isNotBlank(subFundGroupCode)) {
            Collection<String> acceptedValuesForExpenditureSubFundCodes = parameterService.getParameterValuesAsString(KFSConstants.OptionalModuleNamespaces.ACCOUNTS_RECEIVABLE,
                    CUKFSParameterKeyConstants.ALL_COMPONENTS, CuArParameterKeyConstants.CG_INVOICING_EXCLUDE_EXPENSES_SUB_FUNDS);
            if (CollectionUtils.isNotEmpty(acceptedValuesForExpenditureSubFundCodes)) {
                return acceptedValuesForExpenditureSubFundCodes.stream().anyMatch(subFundGroupCode::equalsIgnoreCase);
            }
        }
        return false;
    }

    @Override
    protected void populateInvoiceFromAward(ContractsAndGrantsBillingAward award,
            List<ContractsAndGrantsBillingAwardAccount> awardAccounts, ContractsGrantsInvoiceDocument document,
            List<ContractsGrantsLetterOfCreditReviewDetail> accountDetails, String locCreationType) {
        super.populateInvoiceFromAward(award, awardAccounts, document, accountDetails, locCreationType);
        cuContractsGrantsInvoiceDocumentService.setInvoiceDueDateBasedOnNetTermsAndCurrentDate(document);
    }
    
    /**
     * Generates and then saves a Contracts & Grants Invoice Document
     *
     * @param awd                the award for the document
     * @param validAwardAccounts the award accounts which should appear on the document
     * @param errorMessages      a List of error messages, to be appended to if there are errors in document generation
     * @param accountDetails     letter of credit details if we're creating via loc
     * @param locCreationType    letter of credit creation type if we're creating via loc
     */
    @Override
    protected void generateAndSaveContractsAndGrantsInvoiceDocument(ContractsAndGrantsBillingAward awd,
            List<ContractsAndGrantsBillingAwardAccount> validAwardAccounts, List<ErrorMessage> errorMessages,
            List<ContractsGrantsLetterOfCreditReviewDetail> accountDetails, String locCreationType) {
        ChartOrgHolder chartOrgHolder = financialSystemUserService.getPrimaryOrganization(
                awd.getAwardPrimaryFundManager().getFundManager().getPrincipalId(),
                KFSConstants.OptionalModuleNamespaces.ACCOUNTS_RECEIVABLE);
        ContractsGrantsInvoiceDocument cgInvoiceDocument = createCGInvoiceDocumentByAwardInfo(awd, validAwardAccounts,
                chartOrgHolder.getChartOfAccountsCode(), chartOrgHolder.getOrganizationCode(), errorMessages,
                accountDetails, locCreationType);
        if (ObjectUtils.isNotNull(cgInvoiceDocument)) {
            
            LOG.info("generateAndSaveContractsAndGrantsInvoiceDocument: Award/Proposal# = " + awd.getProposalNumber() 
                    + " Save will be attempted for Positive Total Invoice Amount OR Milestone Billing OR Predetermined Billing"
                    + " cgInvoiceDocument.getTotalInvoiceAmount() = " + cgInvoiceDocument.getTotalInvoiceAmount() 
                    + " cgInvoiceDocument.getTotalInvoiceAmount().isPositive() = " +  cgInvoiceDocument.getTotalInvoiceAmount().isPositive() 
                    + " isMilstone = " + ArConstants.BillingFrequencyValues.isMilestone(awd) 
                    + " isPredeterminedBilling = " + ArConstants.BillingFrequencyValues.isPredeterminedBilling(awd));

            if (cgInvoiceDocument.getTotalInvoiceAmount().isPositive() ||
                ArConstants.BillingFrequencyValues.isMilestone(awd) ||
                ArConstants.BillingFrequencyValues.isPredeterminedBilling(awd)) {
                // Saving the document
                try {
                    documentService.saveDocument(cgInvoiceDocument, DocumentSystemSaveEvent.class);
                } catch (WorkflowException ex) {
                    LOG.error("Error creating cgin documents: " + ex.getMessage(), ex);
                    throw new RuntimeException("Error creating cgin documents: " + ex.getMessage(), ex);
                }
            } else {
                ErrorMessage errorMessage;
                List<InvoiceAccountDetail> invoiceAccounts = cgInvoiceDocument.getAccountDetails();
                if (!invoiceAccounts.isEmpty()) {
                    errorMessage = new ErrorMessage(
                      ArKeyConstants.ContractsGrantsInvoiceCreateDocumentConstants.NON_BILLABLE, 
                      invoiceAccounts.get(0).getAccountNumber(), awd.getProposalNumber());
                } else {
                    errorMessage = new ErrorMessage(
                            ArKeyConstants.ContractsGrantsInvoiceCreateDocumentConstants.NON_BILLABLE, null,
                            awd.getProposalNumber());
                }
                errorMessages.add(errorMessage);
            }
        } else {
            LOG.info("generateAndSaveContractsAndGrantsInvoiceDocument: cgInvoiceDocument is NULL. Not attempting Save.");
        }
    }
    
    @Override
    public Collection<ContractsAndGrantsBillingAward> validateAwards(Collection<ContractsAndGrantsBillingAward> awards,
            Collection<ContractsGrantsInvoiceDocumentErrorLog> contractsGrantsInvoiceDocumentErrorLogs,
            String errOutputFile, String creationProcessTypeCode) {
        LOG.info("validateAwards: creationProcessTypeCode = " + ArConstants.ContractsAndGrantsInvoiceDocumentCreationProcessType.getName(creationProcessTypeCode));
        return super.validateAwards(awards, contractsGrantsInvoiceDocumentErrorLogs, errOutputFile, creationProcessTypeCode);
    }
    
    @Override
    protected void performAwardValidation(Collection<ContractsAndGrantsBillingAward> awards,
            Map<ContractsAndGrantsBillingAward, List<String>> invalidGroup,
            List<ContractsAndGrantsBillingAward> qualifiedAwards) {
        Set<ContractsAndGrantsBillingAward> awardsWithDuplicateAccounts = findAwardsWithDuplicateAccounts(awards);

        for (ContractsAndGrantsBillingAward award : awards) {
            List<String> errorList = new ArrayList<>();

            if (award.isExcludedFromInvoicing()) {
                errorList.add(configurationService.getPropertyValueAsString(ArKeyConstants.CGINVOICE_CREATION_AWARD_EXCLUDED_FROM_INVOICING));
            } else {
                if (awardsWithDuplicateAccounts.contains(award)) {
                    errorList.add(configurationService.getPropertyValueAsString(ArKeyConstants.CGINVOICE_CREATION_ACCOUNT_ON_MULTIPLE_AWARDS));
                }
                if (ArConstants.BillingFrequencyValues.isLetterOfCredit(award)) {
                    errorList.add(configurationService.getPropertyValueAsString(ArKeyConstants.CGINVOICE_CREATION_AWARD_LOCB_BILLING_FREQUENCY));
                } else {
                    if (award.getAwardBeginningDate() != null) {
                        if (award.getBillingFrequencyCode() != null && getContractsGrantsBillingAwardVerificationService().isValueOfBillingFrequencyValid(award)) {
                            if (verifyBillingFrequencyService.validateBillingFrequency(award)) {
                                validateAward(errorList, award);
                            } else {
                                errorList.add(configurationService.getPropertyValueAsString(ArKeyConstants.CGINVOICE_CREATION_AWARD_INVALID_BILLING_PERIOD));
                            }
                        } else {
                            errorList.add(configurationService.getPropertyValueAsString(ArKeyConstants.CGINVOICE_CREATION_BILLING_FREQUENCY_MISSING_ERROR));
                        }
                    } else {
                        errorList.add(configurationService.getPropertyValueAsString(ArKeyConstants.CGINVOICE_CREATION_AWARD_START_DATE_MISSING_ERROR));
                    }
                }
            }
            if (errorList.size() > 0) {
                invalidGroup.put(award, errorList);
            } else {
                qualifiedAwards.add(award);
            }

        }
    }

    public CuContractsGrantsInvoiceDocumentService getCuContractsGrantsInvoiceDocumentService() {
        return cuContractsGrantsInvoiceDocumentService;
    }

    public void setCuContractsGrantsInvoiceDocumentService(
            CuContractsGrantsInvoiceDocumentService cuContractsGrantsInvoiceDocumentService) {
        this.cuContractsGrantsInvoiceDocumentService = cuContractsGrantsInvoiceDocumentService;
    }

}
