package edu.cornell.kfs.module.ar.service.impl;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.gl.businessobject.Balance;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAgency;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAward;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAwardAccount;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.ArKeyConstants;
import org.kuali.kfs.module.ar.businessobject.AccountsReceivableDocumentHeader;
import org.kuali.kfs.module.ar.businessobject.AwardAccountObjectCodeTotalBilled;
import org.kuali.kfs.module.ar.businessobject.Bill;
import org.kuali.kfs.module.ar.businessobject.BillingPeriod;
import org.kuali.kfs.module.ar.businessobject.ContractsGrantsInvoiceDetail;
import org.kuali.kfs.module.ar.businessobject.ContractsGrantsInvoiceDocumentErrorLog;
import org.kuali.kfs.module.ar.businessobject.ContractsGrantsInvoiceDocumentErrorMessage;
import org.kuali.kfs.module.ar.businessobject.ContractsGrantsLetterOfCreditReviewDetail;
import org.kuali.kfs.module.ar.businessobject.InvoiceAccountDetail;
import org.kuali.kfs.module.ar.businessobject.InvoiceAddressDetail;
import org.kuali.kfs.module.ar.businessobject.InvoiceDetailAccountObjectCode;
import org.kuali.kfs.module.ar.businessobject.InvoiceGeneralDetail;
import org.kuali.kfs.module.ar.businessobject.Milestone;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.service.impl.ContractsGrantsInvoiceCreateDocumentServiceImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.ChartOrgHolder;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.document.validation.event.DocumentSystemSaveEvent;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.exception.WorkflowException;

import edu.cornell.kfs.module.ar.CuArConstants;
import edu.cornell.kfs.module.ar.CuArParameterConstants;
import edu.cornell.kfs.module.ar.CuArParameterKeyConstants;
import edu.cornell.kfs.module.ar.batch.service.CuVerifyBillingFrequencyService;
import edu.cornell.kfs.module.ar.document.service.CuContractsGrantsInvoiceDocumentService;
import edu.cornell.kfs.module.ar.service.CuContractsGrantsInvoiceCreateDocumentService;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

public class CuContractsGrantsInvoiceCreateDocumentServiceImpl extends ContractsGrantsInvoiceCreateDocumentServiceImpl implements CuContractsGrantsInvoiceCreateDocumentService {
    
    private static final Logger LOG = LogManager.getLogger(CuContractsGrantsInvoiceCreateDocumentServiceImpl.class);
    
    protected CuContractsGrantsInvoiceDocumentService cuContractsGrantsInvoiceDocumentService;
    protected CuVerifyBillingFrequencyService cuVerifyBillingFrequencyService;
    
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
        LOG.info("getValidAwardAccounts: CU Customization invoked with creationProcessTypeCode = " + ArConstants.ContractsAndGrantsInvoiceDocumentCreationProcessType.getName(award.getCgInvoiceDocumentCreationProcessTypeCode()));
        if (!ArConstants.BillingFrequencyValues.isMilestone(award) && !ArConstants.BillingFrequencyValues.isPredeterminedBilling(award)) {
            List<ContractsAndGrantsBillingAwardAccount> validAwardAccounts = new ArrayList<>();
            Set<Account> invalidAccounts = harvestAccountsFromContractsGrantsInvoices(getInProgressInvoicesForAward(award));

            for (ContractsAndGrantsBillingAwardAccount awardAccount : awardAccounts) {
                if (!invalidAccounts.contains(awardAccount.getAccount())) {
                    if (getCuVerifyBillingFrequencyService().validateBillingFrequency(award, awardAccount)
                            && isNotExpenditureAccount(awardAccount)) {
                        LOG.info("getValidAwardAccounts: Evaluation of account sub-fund not being an expenditure was performed.");
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

    private Date determineLastBilledDateByInvoicingOption(List<ContractsAndGrantsBillingAwardAccount> awardAccounts, String invoicingOptionCode, Date awardLastBilledDate) {
        Date computedLastBilledDate = null;
        if (StringUtils.equalsIgnoreCase(CuArConstants.AwardInvoicingOptionCodeToName.INV_ACCOUNT.getCode(), invoicingOptionCode)
                || StringUtils.equalsIgnoreCase(CuArConstants.AwardInvoicingOptionCodeToName.INV_CONTRACT_CONTROL_ACCOUNT.getCode(), invoicingOptionCode)) {
            ContractsAndGrantsBillingAwardAccount accountToUse = null;
            for (ContractsAndGrantsBillingAwardAccount awardAccount : awardAccounts) {
                if (ObjectUtils.isNotNull(accountToUse)) {
                    if (isNotExpenditureAccount(awardAccount)
                            && ObjectUtils.isNotNull(awardAccount.getCurrentLastBilledDate())
                            && ObjectUtils.isNotNull(accountToUse.getCurrentLastBilledDate())
                            && awardAccount.getCurrentLastBilledDate().after(accountToUse.getCurrentLastBilledDate())) {
                        accountToUse = awardAccount;
                    }
                } else if (isNotExpenditureAccount(awardAccount)) {
                    accountToUse = awardAccount;
                }
            }
            if (ObjectUtils.isNotNull(accountToUse)) {
                computedLastBilledDate = accountToUse.getCurrentLastBilledDate();
            } else {
                LOG.error("determineLastBilledDateByInvoicingOption: Either NO award accounts OR NO NON-Expenditure award accounts passed to method when award had invoice option of Account or Contract Control Account. lastBilledDate being returned as computedLastBilledDate =" + computedLastBilledDate);
            }
        } else if (StringUtils.equalsIgnoreCase(CuArConstants.AwardInvoicingOptionCodeToName.INV_AWARD.getCode(), invoicingOptionCode)
                || StringUtils.equalsIgnoreCase(CuArConstants.AwardInvoicingOptionCodeToName.INV_SCHEDULE.getCode(), invoicingOptionCode)) {
            computedLastBilledDate = awardLastBilledDate;
        }
        return computedLastBilledDate;
    }
    
    @Override
    public ContractsGrantsInvoiceDocument createCGInvoiceDocumentByAwardInfo(ContractsAndGrantsBillingAward awd,
            List<ContractsAndGrantsBillingAwardAccount> accounts, String chartOfAccountsCode, String organizationCode,
            List<ErrorMessage> errorMessages, List<ContractsGrantsLetterOfCreditReviewDetail> accountDetails,
            String locCreationType) {
        LOG.info("createCGInvoiceDocumentByAwardInfo: Basecode method was called. Calculating lastBilledDate to send to CU Customization method of same name.");
        Date calculatedLastBilledDate = determineLastBilledDateByInvoicingOption(accounts, awd.getInvoicingOptionCode(), awd.getLastBilledDate());
        LOG.info("createCGInvoiceDocumentByAwardInfo: calculatedLastBilledDate = " + calculatedLastBilledDate
                + " determined by invoicingOptionCode = " + CuArConstants.AwardInvoicingOptionCodeToName.getName(awd.getInvoicingOptionCode()));
        return this.createCGInvoiceDocumentByAwardInfo(awd, calculatedLastBilledDate, accounts, chartOfAccountsCode, organizationCode, errorMessages, accountDetails, locCreationType);
    }

    /**
     *   CU Customization:
     *   Overridden method signature above is from base code interface.
     *   This method is in Cornell's customized interface due to method signature change.
     *   Code being used here is minimally modified base code.
     */
    public ContractsGrantsInvoiceDocument createCGInvoiceDocumentByAwardInfo(ContractsAndGrantsBillingAward awd, Date calculatedLastBilledDate,
            List<ContractsAndGrantsBillingAwardAccount> accounts, String chartOfAccountsCode, String organizationCode,
            List<ErrorMessage> errorMessages, List<ContractsGrantsLetterOfCreditReviewDetail> accountDetails,
            String locCreationType) {
        ContractsGrantsInvoiceDocument cgInvoiceDocument = null;
        if (ObjectUtils.isNotNull(accounts) && !accounts.isEmpty()) {
            if (chartOfAccountsCode != null && organizationCode != null) {
                try {
                    cgInvoiceDocument = (ContractsGrantsInvoiceDocument) documentService.getNewDocument(ContractsGrantsInvoiceDocument.class);
                    // setup several Default Values for CGInvoice document which extends from Customer Invoice Document

                    // a) set billing org and chart code
                    cgInvoiceDocument.setBillByChartOfAccountCode(chartOfAccountsCode);
                    cgInvoiceDocument.setBilledByOrganizationCode(organizationCode);

                    // b) set processing org and chart code
                    List<String> procCodes = getContractsGrantsInvoiceDocumentService().getProcessingFromBillingCodes(chartOfAccountsCode, organizationCode);

                    AccountsReceivableDocumentHeader accountsReceivableDocumentHeader = new AccountsReceivableDocumentHeader();
                    accountsReceivableDocumentHeader.setDocumentNumber(cgInvoiceDocument.getDocumentNumber());

                    // Set processing chart and org codes
                    if (procCodes != null) {
                        int procCodesSize = procCodes.size();

                        // Set processing chart
                        if (procCodesSize > 0) {
                            accountsReceivableDocumentHeader.setProcessingChartOfAccountCode(procCodes.get(0));
                        }

                        // Set processing org code
                        if (procCodesSize > 1) {
                            accountsReceivableDocumentHeader.setProcessingOrganizationCode(procCodes.get(1));
                        }
                    }

                    cgInvoiceDocument.setAccountsReceivableDocumentHeader(accountsReceivableDocumentHeader);

                    populateInvoiceFromAward(awd, calculatedLastBilledDate, accounts, cgInvoiceDocument, accountDetails, locCreationType);
                    contractsGrantsInvoiceDocumentService.createSourceAccountingLines(cgInvoiceDocument, accounts);
                    if (ObjectUtils.isNotNull(cgInvoiceDocument.getInvoiceGeneralDetail().getAward())) {
                        contractsGrantsInvoiceDocumentService.updateSuspensionCategoriesOnDocument(cgInvoiceDocument);
                    }
                    
                    populateDocumentDescription(cgInvoiceDocument);
                    
                    LOG.info("createCGInvoiceDocumentByAwardInfo: Created Contracts & Grants Invoice Document " + cgInvoiceDocument.getDocumentNumber());
                } catch (WorkflowException ex) {
                    LOG.error("createCGInvoiceDocumentByAwardInfo: Error creating cgin documents: " + ex.getMessage(), ex);
                    throw new RuntimeException("Error creating cgin documents: " + ex.getMessage(), ex);
                }
            } else {
                // if chart of account code or organization code is not available, output the error
                final ErrorMessage errorMessage = new ErrorMessage(
                        ArKeyConstants.ContractsGrantsInvoiceCreateDocumentConstants.NO_CHART_OR_ORG,
                        awd.getProposalNumber());
                errorMessages.add(errorMessage);
            }
        }

        return cgInvoiceDocument;
    }
    
    @Override
    protected void populateInvoiceFromAward(ContractsAndGrantsBillingAward award,
            List<ContractsAndGrantsBillingAwardAccount> awardAccounts, ContractsGrantsInvoiceDocument document,
            List<ContractsGrantsLetterOfCreditReviewDetail> accountDetails, String locCreationType) {
        LOG.info("populateInvoiceFromAward: Basecode method was called. Sending award lastBilledDate to CU Customization method of same name.");
        populateInvoiceFromAward(award, award.getLastBilledDate(), awardAccounts, document, accountDetails, locCreationType);
    }
  
    /**
     *   CU Customization:
     *   Overridden method signature above is from base code interface.
     *   This method is in Cornell's customized interface due to method signature change.
     *   Code being used here is minimally modified base code.
     */
    protected void populateInvoiceFromAward(ContractsAndGrantsBillingAward award, Date calculatedLastBilledDate,
            List<ContractsAndGrantsBillingAwardAccount> awardAccounts, ContractsGrantsInvoiceDocument document,
            List<ContractsGrantsLetterOfCreditReviewDetail> accountDetails, String locCreationType) {
        LOG.info("populateInvoiceFromAward: CU Customization  calculatedLastBilledDate = " + calculatedLastBilledDate 
                + " locCreationType = " + locCreationType + " creationProcessType = " 
                + ArConstants.ContractsAndGrantsInvoiceDocumentCreationProcessType.getName(award.getCgInvoiceDocumentCreationProcessTypeCode()));
        if (ObjectUtils.isNotNull(award)) {
            InvoiceGeneralDetail invoiceGeneralDetail = new InvoiceGeneralDetail();
            invoiceGeneralDetail.setDocumentNumber(document.getDocumentNumber());
            invoiceGeneralDetail.setProposalNumber(award.getProposalNumber());
            invoiceGeneralDetail.setAward(award);

            Timestamp ts = new Timestamp(new java.util.Date().getTime());
            java.sql.Date today = new java.sql.Date(ts.getTime());
            AccountingPeriod currPeriod = accountingPeriodService.getByDate(today);
            BillingPeriod billingPeriod = getCuVerifyBillingFrequencyService().getStartDateAndEndDateOfPreviousBillingPeriod(award, calculatedLastBilledDate, currPeriod, award.getCgInvoiceDocumentCreationProcessTypeCode());
            invoiceGeneralDetail.setBillingPeriod(getDateTimeService().toDateString(billingPeriod.getStartDate()) + " to " +
                    getDateTimeService().toDateString(billingPeriod.getEndDate()));
            invoiceGeneralDetail.setLastBilledDate(billingPeriod.getEndDate());

            populateInvoiceDetailFromAward(invoiceGeneralDetail, award);
            document.setInvoiceGeneralDetail(invoiceGeneralDetail);
            // To set Bill by address identifier because it is a required field - set the value to 1 as it is never
            // being used.
            document.setCustomerBillToAddressIdentifier(1);

            // Set Invoice due date to current date as it is required field and never used.
            document.setInvoiceDueDate(dateTimeService.getCurrentSqlDateMidnight());

            document.getInvoiceAddressDetails().clear();

            ContractsAndGrantsBillingAgency agency = award.getAgency();
            if (ObjectUtils.isNotNull(agency)) {
                final List<InvoiceAddressDetail> invoiceAddressDetails =
                        buildInvoiceAddressDetailsFromAgency(agency, document);
                document.getInvoiceAddressDetails().addAll(invoiceAddressDetails);
            }

            if (ArConstants.BillingFrequencyValues.isMilestone(document.getInvoiceGeneralDetail())) {
                ContractsAndGrantsBillingAwardAccount awardAccount = awardAccounts.get(0);
                final List<Milestone> milestones = getContractsGrantsBillingUtilityService()
                        .getActiveMilestonesForProposalNumber(award.getProposalNumber(),
                                awardAccount.getChartOfAccountsCode(), awardAccount.getAccountNumber());
                if (!CollectionUtils.isEmpty(milestones)) {
                    document.getInvoiceMilestones().clear();
                    document.getInvoiceMilestones().addAll(buildInvoiceMilestones(milestones));
                }
            } else if (ArConstants.BillingFrequencyValues.isPredeterminedBilling(document.getInvoiceGeneralDetail())) {
                ContractsAndGrantsBillingAwardAccount awardAccount = awardAccounts.get(0);
                final List<Bill> bills = getContractsGrantsBillingUtilityService()
                        .getActiveBillsForAwardAccount(award.getProposalNumber(), awardAccount.getChartOfAccountsCode(),
                                awardAccount.getAccountNumber());
                if (!CollectionUtils.isEmpty(bills)) {
                    document.getInvoiceBills().clear();
                    document.getInvoiceBills().addAll(buildInvoiceBills(bills));
                }
            }

            document.getAccountDetails().clear();
            final List<InvoiceAccountDetail> invoiceAccountDetails = new ArrayList<>();
            List<InvoiceDetailAccountObjectCode> invoiceDetailAccountObjectsCodes = new ArrayList<>();
            Map<String, KualiDecimal> budgetAmountsByCostCategory = new HashMap<>();

            Integer currentYear = getUniversityDateService().getCurrentFiscalYear();
            final boolean firstFiscalPeriod = isFirstFiscalPeriod();
            final Integer fiscalYear = firstFiscalPeriod && ArConstants.BillingFrequencyValues
                    .isTimeBased(document.getInvoiceGeneralDetail()) ? currentYear - 1 : currentYear;

            final SystemOptions systemOptions = optionsService.getOptions(fiscalYear);

            List<String> balanceTypeCodeList = new ArrayList<>();
            balanceTypeCodeList.add(systemOptions.getBudgetCheckingBalanceTypeCd());
            balanceTypeCodeList.add(systemOptions.getActualFinancialBalanceTypeCd());
            for (ContractsAndGrantsBillingAwardAccount awardAccount : awardAccounts) {
                InvoiceAccountDetail invoiceAccountDetail =
                        buildInvoiceAccountDetailForAwardAccount(awardAccount, document.getDocumentNumber());
                final ContractsGrantsLetterOfCreditReviewDetail locReviewDetail =
                        retrieveMatchingLetterOfCreditReviewDetail(awardAccount, accountDetails);

                List<Balance> glBalances = retrieveBalances(fiscalYear, awardAccount.getChartOfAccountsCode(),
                        awardAccount.getAccountNumber(), balanceTypeCodeList);
                KualiDecimal awardAccountBudgetAmount = KualiDecimal.ZERO;
                KualiDecimal awardAccountCumulativeAmount = KualiDecimal.ZERO;
                for (Balance balance : glBalances) {
                    if (!isBalanceCostShare(balance)) {
                        if (balance.getBalanceTypeCode().equalsIgnoreCase(systemOptions.getBudgetCheckingBalanceTypeCd())) {
                            awardAccountBudgetAmount = addBalanceToAwardAccountBudgetAmount(balance,
                                    awardAccountBudgetAmount, firstFiscalPeriod);
                            updateCategoryBudgetAmountsByBalance(balance, budgetAmountsByCostCategory, firstFiscalPeriod);
                        } else if (balance.getBalanceTypeCode().equalsIgnoreCase(systemOptions
                                .getActualFinancialBalanceTypeCd())) {
                            awardAccountCumulativeAmount = addBalanceToAwardAccountCumulativeAmount(document, balance,
                                    award, awardAccountCumulativeAmount, firstFiscalPeriod);
                            updateCategoryActualAmountsByBalance(document, balance, award,
                                    invoiceDetailAccountObjectsCodes, firstFiscalPeriod);
                        }
                    }
                    invoiceAccountDetail.setTotalBudget(awardAccountBudgetAmount);
                    invoiceAccountDetail.setCumulativeExpenditures(awardAccountCumulativeAmount);
                }
                invoiceAccountDetails.add(invoiceAccountDetail);
                if (!ObjectUtils.isNull(locReviewDetail)
                        && !locReviewDetail.getClaimOnCashBalance().negated().equals(locReviewDetail.getAmountToDraw())
                        && ArConstants.BillingFrequencyValues.isLetterOfCredit(award)) {
                    distributeAmountAmongAllAccountObjectCodes(document, awardAccount, invoiceDetailAccountObjectsCodes,
                            locReviewDetail);
                } else {
                    updateInvoiceDetailAccountObjectCodesByBilledAmount(awardAccount, invoiceDetailAccountObjectsCodes);
                }
            }
            document.getAccountDetails().addAll(invoiceAccountDetails);
            if (!ArConstants.BillingFrequencyValues.isMilestone(document.getInvoiceGeneralDetail())
                    && !ArConstants.BillingFrequencyValues.isPredeterminedBilling(document.getInvoiceGeneralDetail())) {
                document.getInvoiceDetailAccountObjectCodes().addAll(invoiceDetailAccountObjectsCodes);
                List<AwardAccountObjectCodeTotalBilled> awardAccountObjectCodeTotalBilleds =
                        getAwardAccountObjectCodeTotalBilledDao()
                                .getAwardAccountObjectCodeTotalBuildByProposalNumberAndAccount(awardAccounts);
                List<ContractsGrantsInvoiceDetail> invoiceDetails =
                        generateValuesForCategories(document.getDocumentNumber(),
                                document.getInvoiceDetailAccountObjectCodes(), budgetAmountsByCostCategory,
                                awardAccountObjectCodeTotalBilleds);
                document.getInvoiceDetails().addAll(invoiceDetails);
            }
            populateContractsGrantsInvoiceDocument(award, document, accountDetails, locCreationType);
        }
        
        getCuContractsGrantsInvoiceDocumentService().setInvoiceDueDateBasedOnNetTermsAndCurrentDate(document);
    }
    
    /*
     * CU Customization: 
     *    When billable amount is 0, allow manually created CINV to be created and saved. 
     *    KFS application user creating CINV edoc will manually change billable amount from 0 to desired value
     *    after edoc is created.
     */
    @Override
    protected void generateAndSaveContractsAndGrantsInvoiceDocument(ContractsAndGrantsBillingAward awd,
            List<ContractsAndGrantsBillingAwardAccount> validAwardAccounts, List<ErrorMessage> errorMessages,
            List<ContractsGrantsLetterOfCreditReviewDetail> accountDetails, String locCreationType) {
        ChartOrgHolder chartOrgHolder = financialSystemUserService.getPrimaryOrganization(
                awd.getAwardPrimaryFundManager().getFundManager().getPrincipalId(),
                KFSConstants.OptionalModuleNamespaces.ACCOUNTS_RECEIVABLE);
        Date calculatedLastBilledDate = determineLastBilledDateByInvoicingOption(validAwardAccounts, awd.getInvoicingOptionCode(), awd.getLastBilledDate());
        LOG.info("generateAndSaveContractsAndGrantsInvoiceDocument: CU Customization version: calculatedLastBilledDate = " + calculatedLastBilledDate
                + " determined by invoicingOptionCode = " + CuArConstants.AwardInvoicingOptionCodeToName.getName(awd.getInvoicingOptionCode()));
        ContractsGrantsInvoiceDocument cgInvoiceDocument = createCGInvoiceDocumentByAwardInfo(awd, calculatedLastBilledDate, validAwardAccounts,
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
                ArConstants.BillingFrequencyValues.isPredeterminedBilling(awd) ||
                ( (StringUtils.isNotBlank(awd.getCgInvoiceDocumentCreationProcessTypeCode())
                        && (StringUtils.equalsIgnoreCase(ArConstants.ContractsAndGrantsInvoiceDocumentCreationProcessType.MANUAL.getCode(),
                                                         awd.getCgInvoiceDocumentCreationProcessTypeCode()))) )) {
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
    
    private Collection<ContractsAndGrantsBillingAward> setContractGrantInvoiceDocumentCreationProcessTypeCodeOnEachAward(Collection<ContractsAndGrantsBillingAward> awards, String creationProcessTypeCode) {
        for (ContractsAndGrantsBillingAward award : awards) {
            award.setCgInvoiceDocumentCreationProcessTypeCode(creationProcessTypeCode);
        }
        return awards;
    }
    
    @Override
    public Collection<ContractsAndGrantsBillingAward> validateAwards(Collection<ContractsAndGrantsBillingAward> awards,
            Collection<ContractsGrantsInvoiceDocumentErrorLog> contractsGrantsInvoiceDocumentErrorLogs,
            String errOutputFile, String creationProcessTypeCode) {
        LOG.info("validateAwards: creationProcessTypeCode = " + ArConstants.ContractsAndGrantsInvoiceDocumentCreationProcessType.getName(creationProcessTypeCode));
        Map<ContractsAndGrantsBillingAward, List<String>> invalidGroup = new HashMap<>();
        List<ContractsAndGrantsBillingAward> qualifiedAwards = new ArrayList<>();

        if (ObjectUtils.isNull(contractsGrantsInvoiceDocumentErrorLogs)) {
            contractsGrantsInvoiceDocumentErrorLogs = new ArrayList<>();
        }

        awards = setContractGrantInvoiceDocumentCreationProcessTypeCodeOnEachAward(awards, creationProcessTypeCode);
        
        performAwardValidation(awards, invalidGroup, qualifiedAwards);

        if (!MapUtils.isEmpty(invalidGroup)) {
            if (StringUtils.isNotBlank(errOutputFile)) {
                writeErrorToFile(invalidGroup, errOutputFile);
            }
            storeValidationErrors(invalidGroup, contractsGrantsInvoiceDocumentErrorLogs, creationProcessTypeCode);
        }

        return qualifiedAwards;
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
                            if (getCuVerifyBillingFrequencyService().validateBillingFrequency(award)) {
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

    public CuVerifyBillingFrequencyService getCuVerifyBillingFrequencyService() {
        return cuVerifyBillingFrequencyService;
    }

    public void setCuVerifyBillingFrequencyService(CuVerifyBillingFrequencyService cuVerifyBillingFrequencyService) {
        this.cuVerifyBillingFrequencyService = cuVerifyBillingFrequencyService;
    }

}
