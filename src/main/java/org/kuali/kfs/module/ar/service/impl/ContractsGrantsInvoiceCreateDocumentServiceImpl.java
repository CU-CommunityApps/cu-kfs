/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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
package org.kuali.kfs.module.ar.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.businessobject.ObjectType;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.gl.businessobject.Balance;
import org.kuali.kfs.integration.ar.AccountsReceivableCustomer;
import org.kuali.kfs.integration.ar.AccountsReceivableCustomerAddressEmail;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAgency;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAward;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAwardAccount;
import org.kuali.kfs.integration.cg.ContractsAndGrantsModuleBillingService;
import org.kuali.kfs.integration.cg.ContractsAndGrantsOrganization;
import org.kuali.kfs.kew.api.document.DocumentStatus;
import org.kuali.kfs.kew.api.document.WorkflowDocumentService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.KualiModuleService;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.ArConstants.ContractsAndGrantsInvoiceDocumentCreationProcessType;
import org.kuali.kfs.module.ar.ArKeyConstants;
import org.kuali.kfs.module.ar.ArPropertyConstants;
import org.kuali.kfs.module.ar.ArPropertyConstants.ContractsGrantsInvoiceDocumentErrorLogLookupFields;
import org.kuali.kfs.module.ar.batch.service.VerifyBillingFrequencyService;
import org.kuali.kfs.module.ar.businessobject.AccountsReceivableDocumentHeader;
import org.kuali.kfs.module.ar.businessobject.AwardAccountObjectCodeTotalBilled;
import org.kuali.kfs.module.ar.businessobject.Bill;
import org.kuali.kfs.module.ar.businessobject.BillingPeriod;
import org.kuali.kfs.module.ar.businessobject.ContractsGrantsInvoiceDetail;
import org.kuali.kfs.module.ar.businessobject.ContractsGrantsInvoiceDocumentErrorLog;
import org.kuali.kfs.module.ar.businessobject.ContractsGrantsInvoiceDocumentErrorMessage;
import org.kuali.kfs.module.ar.businessobject.ContractsGrantsLetterOfCreditReviewDetail;
import org.kuali.kfs.module.ar.businessobject.CostCategory;
import org.kuali.kfs.module.ar.businessobject.Customer;
import org.kuali.kfs.module.ar.businessobject.CustomerAddress;
import org.kuali.kfs.module.ar.businessobject.InvoiceAccountDetail;
import org.kuali.kfs.module.ar.businessobject.InvoiceAddressDetail;
import org.kuali.kfs.module.ar.businessobject.InvoiceBill;
import org.kuali.kfs.module.ar.businessobject.InvoiceDetailAccountObjectCode;
import org.kuali.kfs.module.ar.businessobject.InvoiceGeneralDetail;
import org.kuali.kfs.module.ar.businessobject.InvoiceMilestone;
import org.kuali.kfs.module.ar.businessobject.Milestone;
import org.kuali.kfs.module.ar.dataaccess.AwardAccountObjectCodeTotalBilledDao;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.document.service.AccountsReceivableDocumentHeaderService;
import org.kuali.kfs.module.ar.document.service.ContractsGrantsBillingAwardVerificationService;
import org.kuali.kfs.module.ar.document.service.ContractsGrantsInvoiceDocumentService;
import org.kuali.kfs.module.ar.document.service.CustomerAddressService;
import org.kuali.kfs.module.ar.document.service.CustomerService;
import org.kuali.kfs.module.ar.service.ContractsGrantsBillingUtilityService;
import org.kuali.kfs.module.ar.service.ContractsGrantsInvoiceCreateDocumentService;
import org.kuali.kfs.module.ar.service.CostCategoryService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.ChartOrgHolder;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.document.service.FinancialSystemDocumentService;
import org.kuali.kfs.sys.document.validation.event.DocumentSystemSaveEvent;
import org.kuali.kfs.sys.service.FinancialSystemUserService;
import org.kuali.kfs.sys.service.OptionsService;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import edu.cornell.kfs.module.ar.CuArKeyConstants;
import edu.cornell.kfs.module.ar.CuArParameterKeyConstants;
import edu.cornell.kfs.module.ar.service.CuCustomerAddressHelperService;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * This is the default implementation of the ContractsGrantsInvoiceDocumentCreateService interface.
 */
@Transactional
public class ContractsGrantsInvoiceCreateDocumentServiceImpl implements ContractsGrantsInvoiceCreateDocumentService {

    private static final Logger LOG = LogManager.getLogger();

    protected AccountService accountService;
    protected AccountingPeriodService accountingPeriodService;
    protected AccountsReceivableDocumentHeaderService accountsReceivableDocumentHeaderService;
    protected AwardAccountObjectCodeTotalBilledDao awardAccountObjectCodeTotalBilledDao;
    protected BusinessObjectService businessObjectService;
    protected ConfigurationService configurationService;
    protected ContractsGrantsBillingAwardVerificationService contractsGrantsBillingAwardVerificationService;
    protected ContractsGrantsBillingUtilityService contractsGrantsBillingUtilityService;
    protected ContractsAndGrantsModuleBillingService contractsAndGrantsModuleBillingService;
    protected ContractsGrantsInvoiceDocumentService contractsGrantsInvoiceDocumentService;
    protected CostCategoryService costCategoryService;
    protected CustomerService customerService;
    protected DateTimeService dateTimeService;
    protected DataDictionaryService dataDictionaryService;
    protected DocumentService documentService;
    protected FinancialSystemDocumentService financialSystemDocumentService;
    protected KualiModuleService kualiModuleService;
    protected ParameterService parameterService;
    protected VerifyBillingFrequencyService verifyBillingFrequencyService;
    protected WorkflowDocumentService workflowDocumentService;
    protected UniversityDateService universityDateService;
    protected OptionsService optionsService;
    protected FinancialSystemUserService financialSystemUserService;
    private CustomerAddressService customerAddressService;

    /*
     * CU Customization: KFSPTS-26393
     */
    private CuCustomerAddressHelperService cuCustomerAddressHelperService;

    public static final String REPORT_LINE_DIVIDER = "--------------------------------------------------------------------------------------------------------------";

    @Override
    public List<ErrorMessage> createCGInvoiceDocumentsByAwards(
            final Collection<ContractsAndGrantsBillingAward> awards,
            final ContractsAndGrantsInvoiceDocumentCreationProcessType creationProcessType) {
        final List<ErrorMessage> errorMessages = createInvoices(awards, creationProcessType, null, null);

        if (!CollectionUtils.isEmpty(errorMessages)) {
            storeCreationErrors(errorMessages, creationProcessType.getCode());
        }

        return errorMessages;
    }

    @Override
    public List<ErrorMessage> createCGInvoiceDocumentsByAwards(
            final Collection<ContractsAndGrantsBillingAward> awards,
            final List<ContractsGrantsLetterOfCreditReviewDetail> accountDetails, final String locCreationType) {
        final List<ErrorMessage> errorMessages = createInvoices(awards, ContractsAndGrantsInvoiceDocumentCreationProcessType.LOC, accountDetails, locCreationType);

        if (!CollectionUtils.isEmpty(errorMessages)) {
            storeCreationErrors(errorMessages,
                    ContractsAndGrantsInvoiceDocumentCreationProcessType.LOC.getCode());
        }

        return errorMessages;
    }

    /**
     * This method iterates through awards and create cgInvoice documents
     *
     * @param awards          used to create cgInvoice documents
     * @param creationProcessType  creation process type
     * @param accountDetails  letter of credit details if we're creating via loc
     * @param locCreationType letter of credit creation type if we're creating via loc
     * @return List of error messages (if any)
     */
    protected List<ErrorMessage> createInvoices(
            final Collection<ContractsAndGrantsBillingAward> awards,
            final ContractsAndGrantsInvoiceDocumentCreationProcessType creationProcessType,
            final List<ContractsGrantsLetterOfCreditReviewDetail> accountDetails, final String locCreationType) {
        final List<ErrorMessage> errorMessages = new ArrayList<>();

        if (ObjectUtils.isNotNull(awards) && awards.size() > 0) {
            for (final ContractsAndGrantsBillingAward awd : awards) {
                final String invOpt = awd.getInvoicingOptionCode();
                final ContractsAndGrantsOrganization awardOrganization = awd.getPrimaryAwardOrganization();
                if (ObjectUtils.isNull(awardOrganization)) {
                    final ErrorMessage errorMessage = new ErrorMessage(
                            ArKeyConstants.ContractsGrantsInvoiceCreateDocumentConstants.NO_ORGANIZATION_ON_AWARD,
                            awd.getProposalNumber());
                    errorMessages.add(errorMessage);
                } else {
                    switch (invOpt) {
                        case ArConstants.INV_ACCOUNT:
                            createInvoicesByAccounts(awd, errorMessages, creationProcessType, accountDetails, locCreationType);
                            break;
                        case ArConstants.INV_SCHEDULE:
                            createInvoicesBySchedules(awd, errorMessages, creationProcessType, accountDetails, locCreationType);
                            break;
                        case ArConstants.INV_CONTRACT_CONTROL_ACCOUNT:
                            createInvoicesByContractControlAccounts(awd, errorMessages, creationProcessType, accountDetails,
                                    locCreationType);
                            break;
                        case ArConstants.INV_AWARD:
                            createInvoicesByAward(awd, errorMessages, creationProcessType, accountDetails, locCreationType);
                            break;
                        default:
                            break;
                    }
                }
            }
        } else {
            final ErrorMessage errorMessage = new ErrorMessage(
                    ArKeyConstants.ContractsGrantsInvoiceCreateDocumentConstants.NO_AWARD);
            errorMessages.add(errorMessage);
        }
        return errorMessages;
    }

    /**
     * Generates and saves a single Contracts & Grants Invoice Document based on the given award
     *
     * @param awd             the award to generate a Contracts & Grants Invoice Document for
     * @param errorMessages   a holder for error messages
     * @param creationProcessType invoice document creation process type
     * @param accountDetails  letter of credit details if we're creating via loc
     * @param locCreationType letter of credit creation type if we're creating via loc
     */
    protected void createInvoicesByAward(
            final ContractsAndGrantsBillingAward awd, final List<ErrorMessage> errorMessages,
            final ContractsAndGrantsInvoiceDocumentCreationProcessType creationProcessType,
            final List<ContractsGrantsLetterOfCreditReviewDetail> accountDetails, final String locCreationType) {
        // Check if award accounts has the same control account
        final int accountNum = awd.getActiveAwardAccounts().size();
        final Collection<Account> controlAccounts = getContractsGrantsInvoiceDocumentService()
                .getContractControlAccounts(awd);
        if (controlAccounts == null || controlAccounts.size() < accountNum) {
            final ErrorMessage errorMessage = new ErrorMessage(
                    ArKeyConstants.ContractsGrantsInvoiceCreateDocumentConstants.BILL_BY_CONTRACT_VALID_ACCOUNTS,
                    awd.getProposalNumber());
            errorMessages.add(errorMessage);
        } else {
            // check if control accounts of award accounts are the same
            boolean isValid = true;
            if (accountNum != 1) {
                // collect a set of distinct "chart-account" string combinations since the Contract Control Account
                // itself may be a proxy and wouldn't be equal to another non-proxy Account object even if the
                // chart/account are the same, so we could end up with multiple Accounts in the Set causing this
                // validation to fail inadvertently
                final Set<String> distinctAwardAccounts = new HashSet<>();
                for (final ContractsAndGrantsBillingAwardAccount awardAccount : awd.getActiveAwardAccounts()) {
                    final Account account = awardAccount.getAccount();
                    if (ObjectUtils.isNotNull(account)) {
                        distinctAwardAccounts.add(
                                account.getContractControlFinCoaCode()
                                + "-"
                                + account.getContractControlAccountNumber()
                        );
                    }
                }
                if (distinctAwardAccounts.size() > 1) {
                    final ErrorMessage errorMessage = new ErrorMessage(
                            ArKeyConstants.ContractsGrantsInvoiceCreateDocumentConstants.DIFFERING_CONTROL_ACCOUNTS,
                            awd.getProposalNumber());
                    errorMessages.add(errorMessage);
                    isValid = false;
                }
            }

            if (isValid) {
                // To get valid award accounts of amounts > zero$ and pass it to the create invoices method
                if (!getValidAwardAccounts(awd.getActiveAwardAccounts(), awd, creationProcessType)
                        .containsAll(awd.getActiveAwardAccounts())) {
                    final ErrorMessage errorMessage = new ErrorMessage(
                            ArKeyConstants.ContractsGrantsInvoiceCreateDocumentConstants.NOT_ALL_BILLABLE_ACCOUNTS,
                            awd.getProposalNumber());
                    errorMessages.add(errorMessage);
                }
                generateAndSaveContractsAndGrantsInvoiceDocument(awd,
                        getValidAwardAccounts(awd.getActiveAwardAccounts(), awd, creationProcessType), errorMessages,
                        creationProcessType, accountDetails, locCreationType);
            }
        }
    }

    /**
     * Generates and saves Contracts & Grants Invoice Documents based on the given award's contract control accounts
     *
     * @param awd             the award with contract control accounts to build Contracts & Grants Invoice Documents from
     * @param errorMessages   a holder for error messages
     * @param creationProcessType invoice document creation process type
     * @param accountDetails  letter of credit details if we're creating via loc
     * @param locCreationType letter of credit creation type if we're creating via loc
     */
    protected void createInvoicesByContractControlAccounts(
            final ContractsAndGrantsBillingAward awd,
            final List<ErrorMessage> errorMessages, final ContractsAndGrantsInvoiceDocumentCreationProcessType creationProcessType,
            final List<ContractsGrantsLetterOfCreditReviewDetail> accountDetails, final String locCreationType) {
        final List<ContractsAndGrantsBillingAwardAccount> tmpAcctList = new ArrayList<>();
        final List<Account> controlAccounts = getContractsGrantsInvoiceDocumentService().getContractControlAccounts(awd);
        final List<Account> controlAccountsTemp = getContractsGrantsInvoiceDocumentService().getContractControlAccounts(awd);

        // to check if the number of contract control accounts is same as the number of accounts
        if (controlAccounts == null || controlAccounts.size() != awd.getActiveAwardAccounts().size()) {
            final ErrorMessage errorMessage = new ErrorMessage(
                    ArKeyConstants.ContractsGrantsInvoiceCreateDocumentConstants.NO_CONTROL_ACCOUNT,
                    awd.getProposalNumber());
            errorMessages.add(errorMessage);
        } else {
            final Set<Account> controlAccountSet = new HashSet<>();
            for (int i = 0; i < controlAccountsTemp.size(); i++) {
                if (ObjectUtils.isNotNull(controlAccountsTemp.get(i))) {
                    for (int j = i + 1; j < controlAccounts.size(); j++) {
                        if (controlAccountsTemp.get(i).equals(controlAccounts.get(j))) {
                            controlAccounts.set(j, null);
                        }
                    }
                } else {
                    break;
                }
            }
            for (final Account ctrlAcct : controlAccounts) {
                if (ObjectUtils.isNotNull(ctrlAcct)) {
                    controlAccountSet.add(ctrlAcct);
                }
            }
            // control accounts are set correctly for award accounts

            if (controlAccountSet.size() != 0) {
                for (final Account controlAccount : controlAccountSet) {
                    Account tmpCtrlAcct;
                    tmpAcctList.clear();

                    for (final ContractsAndGrantsBillingAwardAccount awardAccount : awd.getActiveAwardAccounts()) {
                        if (!awardAccount.isFinalBilledIndicator()) {
                            tmpCtrlAcct = awardAccount.getAccount().getContractControlAccount();
                            if (tmpCtrlAcct.getChartOfAccountsCode().equals(controlAccount.getChartOfAccountsCode())
                                    && tmpCtrlAcct.getAccountNumber().equals(controlAccount.getAccountNumber())) {
                                tmpAcctList.add(awardAccount);
                            }
                        }
                    }

                    // To get valid award accounts of amounts > zero$ and pass it to the create invoices method
                    if (!getValidAwardAccounts(tmpAcctList, awd, creationProcessType).containsAll(tmpAcctList)) {
                        final ErrorMessage errorMessage = new ErrorMessage(
                                ArKeyConstants.ContractsGrantsInvoiceCreateDocumentConstants.CONTROL_ACCOUNT_NON_BILLABLE,
                                controlAccount.getAccountNumber(), awd.getProposalNumber());
                        errorMessages.add(errorMessage);
                    }
                    generateAndSaveContractsAndGrantsInvoiceDocument(awd, getValidAwardAccounts(tmpAcctList, awd, creationProcessType),
                            errorMessages, creationProcessType, accountDetails, locCreationType);
                }
            } else {
                final ErrorMessage errorMessage = new ErrorMessage(
                        ArKeyConstants.ContractsGrantsInvoiceCreateDocumentConstants.BILL_BY_CONTRACT_LACKS_CONTROL_ACCOUNT,
                        awd.getProposalNumber());
                errorMessages.add(errorMessage);
            }
        }
    }

    /**
     * Generates and saves Contracts & Grants Invoice Documents based on the award accounts of the passed in award
     *
     * @param award           the award to build Contracts & Grants Invoice Documents from the award accounts on
     * @param errorMessages   a holder for error messages
     * @param creationProcessType invoice document creation process type
     * @param accountDetails  letter of credit details if we're creating via loc
     * @param locCreationType letter of credit creation type if we're creating via loc
     */
    protected void createInvoicesByAccounts(
            final ContractsAndGrantsBillingAward award, final List<ErrorMessage> errorMessages,
            final ContractsAndGrantsInvoiceDocumentCreationProcessType creationProcessType,
            final List<ContractsGrantsLetterOfCreditReviewDetail> accountDetails, final String locCreationType) {
        final List<ContractsAndGrantsBillingAwardAccount> tmpAcctList = new ArrayList<>();

        for (final ContractsAndGrantsBillingAwardAccount awardAccount : award.getActiveAwardAccounts()) {
            if (!awardAccount.isFinalBilledIndicator()) {
                tmpAcctList.clear();
                tmpAcctList.add(awardAccount);

                final List<ContractsAndGrantsBillingAwardAccount> validAwardAccounts =
                        getValidAwardAccounts(tmpAcctList, award, creationProcessType);
                if (validAwardAccounts.containsAll(tmpAcctList)) {
                    generateAndSaveContractsAndGrantsInvoiceDocument(award, validAwardAccounts, errorMessages,
                            creationProcessType, accountDetails, locCreationType);
                } else {
                    final ErrorMessage errorMessage = new ErrorMessage(
                            ArKeyConstants.ContractsGrantsInvoiceCreateDocumentConstants.NON_BILLABLE_ACCOUNT_AND_AWARD,
                            awardAccount.getAccountNumber(), award.getProposalNumber());
                    errorMessages.add(errorMessage);
                }

            }
        }
    }

    /**
     * Generates and saves Contracts & Grants Invoice Documents based on the award accounts of the passed in award
     *
     * @param award           the award to build Contracts & Grants Invoice Documents from the award accounts on
     * @param errorMessages   a holder for error messages
     * @param creationProcessType invoice document creation process type
     * @param accountDetails  letter of credit details if we're creating via loc
     * @param locCreationType letter of credit creation type if we're creating via loc
     */
    protected void createInvoicesBySchedules(
            final ContractsAndGrantsBillingAward award, final List<ErrorMessage> errorMessages,
            final ContractsAndGrantsInvoiceDocumentCreationProcessType creationProcessType,
            final List<ContractsGrantsLetterOfCreditReviewDetail> accountDetails, final String locCreationType) {

        for (final ContractsAndGrantsBillingAwardAccount awardAccount : award.getActiveAwardAccounts()) {
            if (!awardAccount.isFinalBilledIndicator() && contractsGrantsBillingAwardVerificationService.isAwardAccountValidToInvoiceBasedOnSchedule(
                        awardAccount)) {
                generateAndSaveContractsAndGrantsInvoiceDocument(award, List.of(awardAccount), errorMessages,
                        creationProcessType, accountDetails, locCreationType);
            }
        }
    }

    /*
     * CU Customization KFSPTS-23690:
     * Set Award transient attribute creationProcessType so value is available for downstream processing.
     */
    /**
     * Generates and then saves a Contracts & Grants Invoice Document
     *
     * @param awd                the award for the document
     * @param validAwardAccounts the award accounts which should appear on the document
     * @param errorMessages      a List of error messages, to be appended to if there are errors in document generation
     * @param accountDetails     letter of credit details if we're creating via loc
     * @param locCreationType    letter of credit creation type if we're creating via loc
     */
    protected void generateAndSaveContractsAndGrantsInvoiceDocument(
            final ContractsAndGrantsBillingAward awd,
            final List<ContractsAndGrantsBillingAwardAccount> validAwardAccounts, final List<ErrorMessage> errorMessages,
            final ContractsAndGrantsInvoiceDocumentCreationProcessType creationProcessType,
            final List<ContractsGrantsLetterOfCreditReviewDetail> accountDetails, final String locCreationType) {
        final ChartOrgHolder chartOrgHolder = financialSystemUserService.getPrimaryOrganization(
                awd.getAwardPrimaryFundManager().getFundManager().getPrincipalId(),
                KFSConstants.OptionalModuleNamespaces.ACCOUNTS_RECEIVABLE);

        /* CU Customization KFSPTS-23690 */
        awd.setCreationProcessType(creationProcessType);

        final ContractsGrantsInvoiceDocument cgInvoiceDocument = createCGInvoiceDocumentByAwardInfo(awd, validAwardAccounts,
                chartOrgHolder.getChartOfAccountsCode(), chartOrgHolder.getOrganizationCode(), errorMessages,
                accountDetails, locCreationType);
        if (ObjectUtils.isNotNull(cgInvoiceDocument)) {
            if (cgInvoiceDocument.getTotalInvoiceAmount().isPositive()
                || getContractsGrantsInvoiceDocumentService().getInvoiceMilestoneTotal(cgInvoiceDocument).isPositive()
                || getContractsGrantsInvoiceDocumentService().getBillAmountTotal(cgInvoiceDocument).isPositive()
                || ArConstants.BillingFrequencyValues.isTimeBased(awd)
                && ContractsAndGrantsInvoiceDocumentCreationProcessType.MANUAL.equals(creationProcessType)) {
                documentService.saveDocument(cgInvoiceDocument, DocumentSystemSaveEvent.class);
            } else {
                final ErrorMessage errorMessage;
                final List<InvoiceAccountDetail> invoiceAccounts = cgInvoiceDocument.getAccountDetails();
                if (!invoiceAccounts.isEmpty()) {
                    errorMessage = new ErrorMessage(
                      ArKeyConstants.ContractsGrantsInvoiceCreateDocumentConstants.NON_BILLABLE_ACCOUNT_AND_AWARD,
                      invoiceAccounts.get(0).getAccountNumber(), awd.getProposalNumber());
                } else {
                    errorMessage = new ErrorMessage(
                            ArKeyConstants.ContractsGrantsInvoiceCreateDocumentConstants.NON_BILLABLE_AWARD,
                            awd.getProposalNumber()
                    );
                }
                errorMessages.add(errorMessage);
            }
        }
    }

    /*
     * CU Customization KFSPTS-23690 NOTE:
     * Currently, this method is only called from within this class from protected method
     * generateAndSaveContractsAndGrantsInvoiceDocument which sets the Award transient
     * attribute createProcessType needed by downstream routines.
     * This method is declared as public and there is the potential for it to generate compile or run time
     * errors when method ContractsGrantsInvoiceDocumentService.updateSuspensionCategoriesOnDocument is
     * called should any new or existing suspensionCategory.shouldSuspend routines rely on the Award
     * transient attribute createProcessType for its downstream processing.
     */
    @Override
    public ContractsGrantsInvoiceDocument createCGInvoiceDocumentByAwardInfo(
            final ContractsAndGrantsBillingAward awd,
            final List<ContractsAndGrantsBillingAwardAccount> accounts, final String chartOfAccountsCode, final String organizationCode,
            final List<ErrorMessage> errorMessages, final List<ContractsGrantsLetterOfCreditReviewDetail> accountDetails,
            final String locCreationType) {
        ContractsGrantsInvoiceDocument cgInvoiceDocument = null;
        if (ObjectUtils.isNotNull(accounts) && !accounts.isEmpty()) {
            if (chartOfAccountsCode != null && organizationCode != null) {
                cgInvoiceDocument = (ContractsGrantsInvoiceDocument) documentService.getNewDocument(
                        ContractsGrantsInvoiceDocument.class);
                // Set description to the document created.
                cgInvoiceDocument.getDocumentHeader().setDocumentDescription(buildDocumentDescription(awd,
                        accounts));
                // setup several Default Values for CGInvoice document which extends from Customer Invoice Document

                // a) set billing org and chart code
                cgInvoiceDocument.setBillByChartOfAccountCode(chartOfAccountsCode);
                cgInvoiceDocument.setBilledByOrganizationCode(organizationCode);

                // b) set processing org and chart code
                final List<String> procCodes = getContractsGrantsInvoiceDocumentService()
                        .getProcessingFromBillingCodes(chartOfAccountsCode, organizationCode);

                final AccountsReceivableDocumentHeader accountsReceivableDocumentHeader =
                        new AccountsReceivableDocumentHeader();
                accountsReceivableDocumentHeader.setDocumentNumber(cgInvoiceDocument.getDocumentNumber());

                // Set processing chart and org codes
                if (procCodes != null) {
                    final int procCodesSize = procCodes.size();

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

                populateInvoiceFromAward(awd, accounts, cgInvoiceDocument, accountDetails, locCreationType);
                contractsGrantsInvoiceDocumentService.createSourceAccountingLines(cgInvoiceDocument, accounts);

                if (ObjectUtils.isNotNull(cgInvoiceDocument.getInvoiceGeneralDetail().getAward())) {
                    contractsGrantsInvoiceDocumentService.updateSuspensionCategoriesOnDocument(cgInvoiceDocument);
                }

                LOG.info("Created Contracts & Grants Invoice Document {}", cgInvoiceDocument::getDocumentNumber);
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

    /**
     * Create document description with attributes
     * 1. proposalNumber
     * 2. contractControlAccount
     * 3. awardPrimaryFundManager.fundManager.principalName
     *
     * Format can be configured by property - contracts.grants.invoice.document.description.format
     *
     * @param award             the award for the document
     * @param accounts          List of award accounts used to create CG Invoice Document
     * @return the formatted document description string
     */
    protected String buildDocumentDescription(final ContractsAndGrantsBillingAward award, final List<ContractsAndGrantsBillingAwardAccount> accounts) {
        final String contractControlAccount = StringUtils.defaultString(accounts.stream().findFirst().get().getAccount().getContractControlAccountNumber());
        final String fundManagerPrincipalName = award.getAwardPrimaryFundManager().getFundManager().getPrincipalName();
        final String description = MessageFormat.format(getConfigurationService().getPropertyValueAsString(ArKeyConstants.CONTRACTS_GRANTS_INVOICE_DOCUMENT_DESCRIPTION_FORMAT),
                award.getProposalNumber(), contractControlAccount, fundManagerPrincipalName);

        final int descriptionMaxLength = dataDictionaryService.getAttributeMaxLength(DocumentHeader.class, KFSPropertyConstants.DOCUMENT_DESCRIPTION);
        return StringUtils.left(description, descriptionMaxLength);
    }

    /**
     * This method takes all the applicable attributes from the associated award object and sets those attributes into
     * their corresponding invoice attributes.
     *
     * @param award           The associated award that the invoice will be linked to.
     * @param awardAccounts
     * @param document
     * @param accountDetails  letter of credit details if we're creating via loc
     * @param locCreationType letter of credit creation type if we're creating via loc
     */
    protected void populateInvoiceFromAward(
            final ContractsAndGrantsBillingAward award,
            final List<ContractsAndGrantsBillingAwardAccount> awardAccounts, final ContractsGrantsInvoiceDocument document,
            final List<ContractsGrantsLetterOfCreditReviewDetail> accountDetails, final String locCreationType) {
        if (ObjectUtils.isNotNull(award)) {
            final InvoiceGeneralDetail invoiceGeneralDetail = new InvoiceGeneralDetail();
            invoiceGeneralDetail.setDocumentNumber(document.getDocumentNumber());
            invoiceGeneralDetail.setProposalNumber(award.getProposalNumber());
            invoiceGeneralDetail.setAward(award);

            final Timestamp ts = new Timestamp(new java.util.Date().getTime());
            final java.sql.Date today = new java.sql.Date(ts.getTime());
            final AccountingPeriod currPeriod = accountingPeriodService.getByDate(today);
            final BillingPeriod billingPeriod = verifyBillingFrequencyService
                    .getStartDateAndEndDateOfPreviousBillingPeriod(award, currPeriod);
            invoiceGeneralDetail.setBillingPeriod(getDateTimeService().toDateString(billingPeriod.getStartDate()) +
                    " to " + getDateTimeService().toDateString(billingPeriod.getEndDate()));
            invoiceGeneralDetail.setLastBilledDate(billingPeriod.getEndDate());

            populateInvoiceDetailFromAward(invoiceGeneralDetail, award);
            document.setInvoiceGeneralDetail(invoiceGeneralDetail);
            // To set Bill by address identifier because it is a required field - set the value to 1 as it is never
            // being used.
            document.setCustomerBillToAddressIdentifier(1);

            // Set Invoice due date to current date as it is required field and never used.
            document.setInvoiceDueDate(dateTimeService.getCurrentSqlDateMidnight());

            document.getInvoiceAddressDetails().clear();

            final ContractsAndGrantsBillingAgency agency = award.getAgency();
            if (ObjectUtils.isNotNull(agency)) {
                final List<InvoiceAddressDetail> invoiceAddressDetails =
                        buildInvoiceAddressDetails(award, document);
                document.getInvoiceAddressDetails().addAll(invoiceAddressDetails);
            }

            if (ArConstants.BillingFrequencyValues.isMilestone(document.getInvoiceGeneralDetail())) {
                final ContractsAndGrantsBillingAwardAccount awardAccount = awardAccounts.get(0);
                final List<Milestone> milestones = getContractsGrantsBillingUtilityService()
                        .getActiveMilestonesForProposalNumber(award.getProposalNumber(),
                                awardAccount.getChartOfAccountsCode(), awardAccount.getAccountNumber());
                if (!CollectionUtils.isEmpty(milestones)) {
                    document.getInvoiceMilestones().clear();
                    document.getInvoiceMilestones().addAll(buildInvoiceMilestones(milestones));
                }
            } else if (ArConstants.BillingFrequencyValues.isPredeterminedBilling(document.getInvoiceGeneralDetail())) {
                final ContractsAndGrantsBillingAwardAccount awardAccount = awardAccounts.get(0);
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
            final List<InvoiceDetailAccountObjectCode> invoiceDetailAccountObjectsCodes = new ArrayList<>();
            final Map<String, KualiDecimal> budgetAmountsByCostCategory = new HashMap<>();

            final Integer currentYear = getUniversityDateService().getCurrentFiscalYear();
            final boolean firstFiscalPeriod = contractsGrantsInvoiceDocumentService.isFirstFiscalPeriod();
            final Integer fiscalYear = firstFiscalPeriod && ArConstants.BillingFrequencyValues
                    .isTimeBased(document.getInvoiceGeneralDetail()) ? currentYear - 1 : currentYear;

            final SystemOptions systemOptions = optionsService.getOptions(fiscalYear);

            final List<String> balanceTypeCodeList = new ArrayList<>();
            balanceTypeCodeList.add(systemOptions.getBudgetCheckingBalanceTypeCd());
            balanceTypeCodeList.add(systemOptions.getActualFinancialBalanceTypeCd());
            for (final ContractsAndGrantsBillingAwardAccount awardAccount : awardAccounts) {
                final InvoiceAccountDetail invoiceAccountDetail =
                        buildInvoiceAccountDetailForAwardAccount(awardAccount, document.getDocumentNumber());
                final ContractsGrantsLetterOfCreditReviewDetail locReviewDetail =
                        retrieveMatchingLetterOfCreditReviewDetail(awardAccount, accountDetails);

                final List<Balance> glBalances = retrieveBalances(fiscalYear, awardAccount.getChartOfAccountsCode(),
                        awardAccount.getAccountNumber(), balanceTypeCodeList);
                KualiDecimal awardAccountBudgetAmount = KualiDecimal.ZERO;
                KualiDecimal awardAccountCumulativeAmount = KualiDecimal.ZERO;
                for (final Balance balance : glBalances) {
                    if (!isBalanceCostShare(balance)) {
                        if (balance.getBalanceTypeCode().equalsIgnoreCase(systemOptions.getBudgetCheckingBalanceTypeCd())) {
                            awardAccountBudgetAmount = addBalanceToAwardAccountBudgetAmount(balance,
                                    awardAccountBudgetAmount, firstFiscalPeriod);
                            updateCategoryBudgetAmountsByBalance(balance, budgetAmountsByCostCategory, firstFiscalPeriod);
                        } else if (balance.getBalanceTypeCode().equalsIgnoreCase(systemOptions
                                .getActualFinancialBalanceTypeCd())) {
                            awardAccountCumulativeAmount = addBalanceToAwardAccountCumulativeAmount(document, balance,
                                    awardAccountCumulativeAmount
                            );
                            updateCategoryActualAmountsByBalance(document, balance, invoiceDetailAccountObjectsCodes);
                        }
                    }
                    invoiceAccountDetail.setTotalBudget(awardAccountBudgetAmount);
                    invoiceAccountDetail.setCumulativeExpenditures(awardAccountCumulativeAmount);
                }
                if (shouldBillAccount(document, locReviewDetail)) {
                    invoiceAccountDetails.add(invoiceAccountDetail);
                }
                if (ObjectUtils.isNotNull(locReviewDetail)
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
                final List<AwardAccountObjectCodeTotalBilled> awardAccountObjectCodeTotalBilleds =
                        getAwardAccountObjectCodeTotalBilledDao()
                                .getAwardAccountObjectCodeTotalBuildByProposalNumberAndAccount(awardAccounts);
                final List<ContractsGrantsInvoiceDetail> invoiceDetails =
                        generateValuesForCategories(document.getDocumentNumber(),
                                document.getInvoiceDetailAccountObjectCodes(), budgetAmountsByCostCategory,
                                awardAccountObjectCodeTotalBilleds);
                document.getInvoiceDetails().addAll(invoiceDetails);
            }
            populateContractsGrantsInvoiceDocument(award, document, accountDetails, locCreationType);
        }
    }

    private boolean shouldBillAccount(
            final ContractsGrantsInvoiceDocument document,
            final ContractsGrantsLetterOfCreditReviewDetail locReviewDetail
    ) {
        // CGB should only bill positive amounts on LOC
        if (ArConstants.BillingFrequencyValues.isLetterOfCredit(document.getInvoiceGeneralDetail())) {
            KualiDecimal amountToDraw = KualiDecimal.ZERO;
            if (ObjectUtils.isNotNull(locReviewDetail)) {
                amountToDraw = locReviewDetail.getAmountToDraw();
            }

            return amountToDraw.isPositive();
        } else {
            return true;
        }
    }

    /**
     * Finds a letter of credit review detail which matches the given award account, or null if one could not be found
     *
     * @param awardAccount   the award account to find a matching contracts grants letter of credit review detail for
     * @param accountDetails a List of contracts grants letter of credit review details
     * @return the matching contracts grants review detail, or null if one could not be found
     */
    protected ContractsGrantsLetterOfCreditReviewDetail retrieveMatchingLetterOfCreditReviewDetail(
            final ContractsAndGrantsBillingAwardAccount awardAccount,
            final List<ContractsGrantsLetterOfCreditReviewDetail> accountDetails) {
        if (CollectionUtils.isEmpty(accountDetails)) {
            return null;
        }
        for (final ContractsGrantsLetterOfCreditReviewDetail reviewDetail : accountDetails) {
            if (reviewDetail.getProposalNumber().equals(awardAccount.getProposalNumber())
                    && StringUtils.equals(reviewDetail.getChartOfAccountsCode(), awardAccount.getChartOfAccountsCode())
                    && StringUtils.equals(reviewDetail.getAccountNumber(), awardAccount.getAccountNumber())) {
                return reviewDetail;
            }
        }
        return null;
    }

    /*
     * CU Customization KFSPTS-27212: UCI local fix KFS-2775 for FINP-9026
     * Fix for Cost Reimbursable CINV where cumulative expendature in invoice
     * detail is incorrect when expenditures contain sub-accounts. 
     */
    /**
     * Updates the appropriate amounts for the InvoiceDetailAccountObjectCode matching the given balance
     *
     * @param document                        the CINV document we're generating
     * @param balance                         the balance to update amounts by
     * @param invoiceDetailAccountObjectCodes the List of invoiceDetailObjectCodes to update one of
     */
    protected void updateCategoryActualAmountsByBalance(
            final ContractsGrantsInvoiceDocument document,
            final Balance balance,
            final List<InvoiceDetailAccountObjectCode> invoiceDetailAccountObjectCodes
    ) {
        final CostCategory category = getCostCategoryService().getCostCategoryForObjectCode(
                balance.getUniversityFiscalYear(), balance.getChartOfAccountsCode(), balance.getObjectCode());
        if (ObjectUtils.isNotNull(category)) {
            final InvoiceGeneralDetail invoiceGeneralDetail = document.getInvoiceGeneralDetail();
            final InvoiceDetailAccountObjectCode invoiceDetailAccountObjectCode =
                    getInvoiceDetailAccountObjectCodeByBalanceAndCategory(invoiceDetailAccountObjectCodes, balance,
                            document.getDocumentNumber(), invoiceGeneralDetail.getProposalNumber(),
                            category);

            if (ArConstants.BillingFrequencyValues.isTimeBased(invoiceGeneralDetail) ||
                ArConstants.BillingFrequencyValues.isLetterOfCredit(invoiceGeneralDetail)) {
                final KualiDecimal balanceAmount =
                        contractsGrantsInvoiceDocumentService.calculateCumulativeBalanceAmount(balance);
                
                /* CU Customization KFSPTS-27212 */    
                //invoiceDetailAccountObjectCode.setCumulativeExpenditures(balanceAmount);
                invoiceDetailAccountObjectCode.setCumulativeExpenditures(invoiceDetailAccountObjectCode.getCumulativeExpenditures()
                        .add(balanceAmount));

            } else {
                // For other billing frequencies
                cleanAmount(balance.getContractsGrantsBeginningBalanceAmount()).add(
                        cleanAmount(balance.getAccountLineAnnualBalanceAmount()));
                invoiceDetailAccountObjectCode.setCumulativeExpenditures(cleanAmount(
                        invoiceDetailAccountObjectCode.getCumulativeExpenditures())
                        .add(cleanAmount(balance.getContractsGrantsBeginningBalanceAmount())
                                .add(cleanAmount(balance.getAccountLineAnnualBalanceAmount()))));
            }
        }
    }

    /**
     * Sums the balance to the given awardAccountCumulativeAmount and returns that summed amount
     *
     * @param document                     the CINV document we're generating
     * @param balance                      the balance to update amounts by
     * @param awardAccountCumulativeAmount the beginning cumulative expense amount for the award account of the balance
     * @return the updated cumulative amount on the award account
     */
    protected KualiDecimal addBalanceToAwardAccountCumulativeAmount(
            final ContractsGrantsInvoiceDocument document, final Balance balance, final KualiDecimal awardAccountCumulativeAmount
    ) {
        final InvoiceGeneralDetail invoiceGeneralDetail = document.getInvoiceGeneralDetail();
        if (ArConstants.BillingFrequencyValues.isTimeBased(invoiceGeneralDetail) ||
            ArConstants.BillingFrequencyValues.isLetterOfCredit(invoiceGeneralDetail)) {
            return awardAccountCumulativeAmount
                    .add(contractsGrantsInvoiceDocumentService.calculateCumulativeBalanceAmount(balance));
        } else {
            // For other billing frequencies
            final KualiDecimal balanceAmount = cleanAmount(balance.getContractsGrantsBeginningBalanceAmount())
                    .add(cleanAmount(balance.getAccountLineAnnualBalanceAmount()));
            return awardAccountCumulativeAmount.add(balanceAmount);
        }
    }

    /**
     * Updates the cost category budget amount (in the given Map, budgetAmountsByCostCategory) by the total amount of
     * the balance
     *
     * @param balance                     the balance to update the budget amounts by
     * @param budgetAmountsByCostCategory the Map of budget amounts sorted by cost category
     * @param firstFiscalPeriod           whether this CINV is being generated in the first fiscal period or not
     * @return the updated award account budget amount
     */
    protected void updateCategoryBudgetAmountsByBalance(
            final Balance balance, final Map<String,
            KualiDecimal> budgetAmountsByCostCategory, final boolean firstFiscalPeriod) {
        final CostCategory category = getCostCategoryService().getCostCategoryForObjectCode(
                balance.getUniversityFiscalYear(), balance.getChartOfAccountsCode(), balance.getObjectCode());
        if (ObjectUtils.isNotNull(category)) {
            final KualiDecimal balanceAmount = getBudgetBalanceAmount(balance, firstFiscalPeriod);
            KualiDecimal categoryBudgetAmount = budgetAmountsByCostCategory.get(category.getCategoryCode());
            if (categoryBudgetAmount == null) {
                categoryBudgetAmount = KualiDecimal.ZERO;
            }
            categoryBudgetAmount = categoryBudgetAmount.add(balanceAmount);
            budgetAmountsByCostCategory.put(category.getCategoryCode(), categoryBudgetAmount);
        } else {
            LOG.warn(
                    "Could not find cost category for balance: {} {} {} {} {} {} {}",
                    balance::getUniversityFiscalYear,
                    balance::getChartOfAccountsCode,
                    balance::getAccountNumber,
                    balance::getSubAccountNumber,
                    balance::getObjectCode,
                    balance::getSubObjectCode,
                    balance::getBalanceTypeCode
            );
        }
    }

    /**
     * Adds the budget balance to the award account budget amount
     *
     * @param balance                  the balance to update the budget amounts by
     * @param awardAccountBudgetAmount the beginning award account budget amount
     * @param firstFiscalPeriod        whether this CINV is being generated in the first fiscal period or not
     * @return the updated award account budget amount
     */
    protected KualiDecimal addBalanceToAwardAccountBudgetAmount(
            final Balance balance,
            final KualiDecimal awardAccountBudgetAmount, final boolean firstFiscalPeriod) {
        final KualiDecimal balanceAmount = getBudgetBalanceAmount(balance, firstFiscalPeriod);
        return awardAccountBudgetAmount.add(balanceAmount);
    }

    /**
     * Determines the balance amount (cg + annual) from the given budget balance
     *
     * @param balance           balance to find amount from
     * @param firstFiscalPeriod whether the CINV is being created in the first fiscal period or not
     * @return the total amount from the balance
     */
    protected KualiDecimal getBudgetBalanceAmount(final Balance balance, final boolean firstFiscalPeriod) {
        KualiDecimal balanceAmount = balance.getContractsGrantsBeginningBalanceAmount()
                .add(balance.getAccountLineAnnualBalanceAmount());
        if (firstFiscalPeriod && !contractsGrantsInvoiceDocumentService.includePeriod13InPeriod01Calculations()) {
            // get rid of period 13 if we should not include in calculations
            balanceAmount = balanceAmount.subtract(balance.getMonth13Amount());
        }
        return balanceAmount;
    }

    /**
     * Builds a new invoice account detail for a given award account
     *
     * @param awardAccount         the award account to build the invoice account detail for
     * @param documentNumber       the number of the document we're currently building
     * @return the built invoice account detail
     */
    protected InvoiceAccountDetail buildInvoiceAccountDetailForAwardAccount(
            final ContractsAndGrantsBillingAwardAccount awardAccount, final String documentNumber) {
        final InvoiceAccountDetail invoiceAccountDetail = new InvoiceAccountDetail();

        invoiceAccountDetail.setDocumentNumber(documentNumber);
        invoiceAccountDetail.setAccountNumber(awardAccount.getAccountNumber());
        invoiceAccountDetail.setChartOfAccountsCode(awardAccount.getChartOfAccountsCode());
        invoiceAccountDetail.setProposalNumber(awardAccount.getProposalNumber());

        return invoiceAccountDetail;
    }

    /**
     * Generates InvoiceBills for each of the given Bills
     *
     * @param bills the bills to associate with a contracts & grants billing invoice
     * @return the List of generated InvoiceBill objects
     */
    protected List<InvoiceBill> buildInvoiceBills(final List<Bill> bills) {
        final List<Bill> billsToInvoice = contractsGrantsBillingAwardVerificationService.determineBillsToInvoice(bills);

        return billsToInvoice.stream()
                .map(InvoiceBill::new)
                .collect(Collectors.toList());
    }

    /**
     * Generates InvoiceMilestones for each of the given milestones
     *
     * @param milestones  the milestones to associate with a contracts & grants billing invoice
     * @return the List of InvoiceMilestones
     */
    protected List<InvoiceMilestone> buildInvoiceMilestones(final List<Milestone> milestones) {
        final List<Milestone> milestonesToInvoice =
                contractsGrantsBillingAwardVerificationService.determineMilestonesToInvoice(milestones);

        return milestonesToInvoice.stream()
                .map(InvoiceMilestone::new)
                .collect(Collectors.toList());
    }

    /**
     * Builds a list of InvoiceAddressDetails based on the customer associated with the Award
     *
     * @param award    the award associated with the proposal we're building a CINV document for
     * @param document the CINV document we're creating
     * @return a List of the generated invoice address details
     */
    protected List<InvoiceAddressDetail> buildInvoiceAddressDetails(
            final ContractsAndGrantsBillingAward award,
            final ContractsGrantsInvoiceDocument document) {
        CustomerAddress customerAddress = null;
        if (ObjectUtils.isNotNull(award.getCustomerAddressIdentifier())) {
            customerAddress = customerAddressService.getByPrimaryKey(award.getCustomerNumber(),
                    award.getCustomerAddressIdentifier());
        }
        if (customerAddress == null) {
            customerAddress = customerAddressService.getPrimaryAddress(award.getCustomerNumber());
        }

        final String documentNumber = document.getDocumentNumber();

        final List<InvoiceAddressDetail> invoiceAddressDetails = new ArrayList<>();
        final AtomicInteger detailNumber = new AtomicInteger(1);
        if (StringUtils.equalsIgnoreCase(ArKeyConstants.CustomerConstants.CUSTOMER_ADDRESS_TYPE_CODE_PRIMARY,
                customerAddress.getCustomerAddressTypeCode())) {
            document.setCustomerBillToAddressOnInvoice(customerAddress);
        }
        final InvoiceAddressDetail invoiceAddressDetail = new InvoiceAddressDetail();
        invoiceAddressDetail.setCustomerNumber(customerAddress.getCustomerNumber());
        invoiceAddressDetail.setDocumentNumber(documentNumber);
        invoiceAddressDetail.setCustomerAddressIdentifier(customerAddress.getCustomerAddressIdentifier());
        invoiceAddressDetail.setDetailNumber(detailNumber.getAndIncrement());
        invoiceAddressDetail.setCustomerAddressTypeCode(customerAddress.getCustomerAddressTypeCode());
        invoiceAddressDetail.setCustomerAddressName(customerAddress.getCustomerAddressName());
        invoiceAddressDetail.setInvoiceTransmissionMethodCode(ArConstants.InvoiceTransmissionMethod.MAIL);
        invoiceAddressDetail.setCustomerLine1StreetAddress(customerAddress.getCustomerLine1StreetAddress());
        invoiceAddressDetail.setCustomerLine2StreetAddress(customerAddress.getCustomerLine2StreetAddress());
        invoiceAddressDetail.setCustomerCityName(customerAddress.getCustomerCityName());
        invoiceAddressDetail.setCustomerStateCode(customerAddress.getCustomerStateCode());
        invoiceAddressDetail.setCustomerZipCode(customerAddress.getCustomerZipCode());
        invoiceAddressDetail.setCustomerCountryCode(customerAddress.getCustomerCountryCode());
        invoiceAddressDetail.setCustomerInternationalMailCode(customerAddress.getCustomerInternationalMailCode());
        invoiceAddressDetail.setCustomerAddressInternationalProvinceName(
                customerAddress.getCustomerAddressInternationalProvinceName());
        if (StringUtils.isNotBlank(customerAddress.getCustomerInvoiceTemplateCode())) {
            invoiceAddressDetail.setCustomerInvoiceTemplateCode(customerAddress.getCustomerInvoiceTemplateCode());
        } else {
            final AccountsReceivableCustomer customer = award.getAgency().getCustomer();
            if (ObjectUtils.isNotNull(customer)
                    && StringUtils.isNotBlank(customer.getCustomerInvoiceTemplateCode())) {
                invoiceAddressDetail.setCustomerInvoiceTemplateCode(customer.getCustomerInvoiceTemplateCode());
            }
        }
        if (ArConstants.InvoiceTransmissionMethod.MAIL.equals(customerAddress.getInvoiceTransmissionMethodCode())) {
            invoiceAddressDetail.setSendIndicator(true);
        }
        invoiceAddressDetails.add(invoiceAddressDetail);

        final CustomerAddress finalCustomerAddress = customerAddress;
        customerAddress.getCustomerAddressEmails().stream()
                .filter(AccountsReceivableCustomerAddressEmail::isActive)
                .forEach(email -> {
                    final InvoiceAddressDetail invoiceAddressDetailWithEmail = new InvoiceAddressDetail();
                    invoiceAddressDetailWithEmail.setCustomerNumber(finalCustomerAddress.getCustomerNumber());
                    invoiceAddressDetailWithEmail.setDocumentNumber(documentNumber);
                    invoiceAddressDetailWithEmail.setCustomerAddressIdentifier(
                            finalCustomerAddress.getCustomerAddressIdentifier());
                    invoiceAddressDetailWithEmail.setDetailNumber(detailNumber.getAndIncrement());
                    invoiceAddressDetailWithEmail.setCustomerEmailAddress(email.getCustomerEmailAddress());
                    invoiceAddressDetailWithEmail.setInvoiceTransmissionMethodCode(
                            ArConstants.InvoiceTransmissionMethod.EMAIL);
                    if (ArConstants.InvoiceTransmissionMethod.EMAIL.equals(
                            finalCustomerAddress.getInvoiceTransmissionMethodCode())) {
                        invoiceAddressDetailWithEmail.setSendIndicator(true);
                    }
                    invoiceAddressDetails.add(invoiceAddressDetailWithEmail);
                });
        return invoiceAddressDetails;
    }

    /**
     * 1. This method is responsible to populate categories column for the ContractsGrantsInvoice Document. 2. The
     * categories are retrieved from the Maintenance document as a collection and then a logic with conditions to
     * handle ranges of Object Codes. 3. Once the object codes are retrieved and categories are set the
     * performAccountingCalculations method of InvoiceDetail BO will do all the accounting calculations.
     *
     * @param documentNumber                     the number of the document we want to add invoice details to
     * @param invoiceDetailAccountObjectCodes    the List of InvoiceDetailAccountObjectCodes containing amounts to
     *                                           sum into our invoice details
     * @param budgetAmountsByCostCategory        the budget amounts, sorted by cost category
     * @param awardAccountObjectCodeTotalBilleds the business objects containing what has been billed from the
     *                                           document's award accounts already
     */
    public List<ContractsGrantsInvoiceDetail> generateValuesForCategories(
            final String documentNumber,
            final List<InvoiceDetailAccountObjectCode> invoiceDetailAccountObjectCodes,
            final Map<String, KualiDecimal> budgetAmountsByCostCategory,
            final List<AwardAccountObjectCodeTotalBilled> awardAccountObjectCodeTotalBilleds) {
        final Collection<CostCategory> costCategories = retrieveAllBillingCategories();
        final List<ContractsGrantsInvoiceDetail> invoiceDetails = new ArrayList<>();
        final Map<String, List<InvoiceDetailAccountObjectCode>> invoiceDetailAccountObjectCodesMap =
                mapInvoiceDetailAccountObjectCodesByCategoryCode(invoiceDetailAccountObjectCodes);
        final Map<String, List<AwardAccountObjectCodeTotalBilled>> billedsMap =
                mapAwardAccountObjectCodeTotalBilledsByCategoryCode(awardAccountObjectCodeTotalBilleds);

        for (final CostCategory category : costCategories) {
            final ContractsGrantsInvoiceDetail invDetail = new ContractsGrantsInvoiceDetail();
            invDetail.setDocumentNumber(documentNumber);
            invDetail.setCategoryCode(category.getCategoryCode());
            invDetail.setCostCategory(category);
            invDetail.setIndirectCostIndicator(category.isIndirectCostIndicator());
            // calculate total billed first
            invDetail.setCumulativeExpenditures(KualiDecimal.ZERO);
            invDetail.setInvoiceAmount(KualiDecimal.ZERO);
            invDetail.setTotalPreviouslyBilled(KualiDecimal.ZERO);

            final List<InvoiceDetailAccountObjectCode> invoiceDetailAccountObjectCodesForCategory =
                    invoiceDetailAccountObjectCodesMap.get(category.getCategoryCode());
            if (!CollectionUtils.isEmpty(invoiceDetailAccountObjectCodesForCategory)) {
                for (final InvoiceDetailAccountObjectCode invoiceDetailAccountObjectCode :
                        invoiceDetailAccountObjectCodesForCategory) {
                    invDetail.setCumulativeExpenditures(invDetail.getCumulativeExpenditures()
                            .add(invoiceDetailAccountObjectCode.getCumulativeExpenditures()));
                    /*
                     * CU Customization back port FINP-10147
                     */
                    //invDetail.setInvoiceAmount(invDetail.getInvoiceAmount()
                      //      .add(invoiceDetailAccountObjectCode.getCurrentExpenditures()));
                }
            }
            final List<AwardAccountObjectCodeTotalBilled> billedForCategory = billedsMap.get(category.getCategoryCode());
            if (!CollectionUtils.isEmpty(billedForCategory)) {
                for (final AwardAccountObjectCodeTotalBilled accountObjectCodeTotalBilled : billedForCategory) {
                    // this adds up all the total billed based on object code into categories; sum for this category.
                    invDetail.setTotalPreviouslyBilled(invDetail.getTotalPreviouslyBilled()
                            .add(accountObjectCodeTotalBilled.getTotalBilled()));
                }
            }
            
            /*
             * CU Customization back port FINP-10147
             */
            invDetail.setInvoiceAmount(invDetail.getCumulativeExpenditures()
                    .subtract(invDetail.getTotalPreviouslyBilled()));
            
            // calculate the rest using billed to date
            if (ObjectUtils.isNotNull(budgetAmountsByCostCategory.get(category.getCategoryCode()))) {
                invDetail.setTotalBudget(budgetAmountsByCostCategory.get(category.getCategoryCode()));
            } else {
                invDetail.setTotalBudget(KualiDecimal.ZERO);
            }
            invoiceDetails.add(invDetail);
        }
        return invoiceDetails;
    }

    /**
     * Converts a List of InvoiceDetailAccountObjectCodes into a map where the key is the category code
     *
     * @param invoiceDetailAccountObjectCodes a List of InvoiceDetailAccountObjectCodes
     * @return that List converted to a Map, keyed by category code
     */
    protected Map<String, List<InvoiceDetailAccountObjectCode>> mapInvoiceDetailAccountObjectCodesByCategoryCode(
            final List<InvoiceDetailAccountObjectCode> invoiceDetailAccountObjectCodes) {
        final Map<String, List<InvoiceDetailAccountObjectCode>> invoiceDetailAccountObjectCodesMap = new HashMap<>();
        for (final InvoiceDetailAccountObjectCode invoiceDetailAccountObjectCode : invoiceDetailAccountObjectCodes) {
            List<InvoiceDetailAccountObjectCode> invoiceDetailAccountObjectCodesForCategory =
                    invoiceDetailAccountObjectCodesMap.get(invoiceDetailAccountObjectCode.getCategoryCode());
            if (invoiceDetailAccountObjectCodesForCategory == null) {
                invoiceDetailAccountObjectCodesForCategory = new ArrayList<>();
            }
            invoiceDetailAccountObjectCodesForCategory.add(invoiceDetailAccountObjectCode);
            invoiceDetailAccountObjectCodesMap.put(invoiceDetailAccountObjectCode.getCategoryCode(),
                    invoiceDetailAccountObjectCodesForCategory);
        }
        return invoiceDetailAccountObjectCodesMap;
    }

    /**
     * Converts a List of AwardAccountObjectCodeTotalBilled into a Map, keyed by the Cost Category which most closely
     * matches them
     *
     * @param awardAccountObjectCodeTotalBilleds the List of AwardAccountObjectCodeTotalBilled business objects to Map
     * @return the Mapped AwardAccountObjectCodeTotalBilled records
     */
    protected Map<String, List<AwardAccountObjectCodeTotalBilled>> mapAwardAccountObjectCodeTotalBilledsByCategoryCode(
            final List<AwardAccountObjectCodeTotalBilled> awardAccountObjectCodeTotalBilleds) {
        final Integer fiscalYear = getUniversityDateService().getCurrentFiscalYear();
        final Map<String, List<AwardAccountObjectCodeTotalBilled>> billedsMap = new HashMap<>();
        for (final AwardAccountObjectCodeTotalBilled billed : awardAccountObjectCodeTotalBilleds) {
            final CostCategory category = getCostCategoryService().getCostCategoryForObjectCode(fiscalYear,
                    billed.getChartOfAccountsCode(), billed.getFinancialObjectCode());
            if (ObjectUtils.isNotNull(category)) {
                List<AwardAccountObjectCodeTotalBilled> billedForCategory = billedsMap.get(category.getCategoryCode());
                if (billedForCategory == null) {
                    billedForCategory = new ArrayList<>();
                }
                billedForCategory.add(billed);
                billedsMap.put(category.getCategoryCode(), billedForCategory);
            } else {
                LOG.warn(
                        "Could not find cost category for AwardAccountObjectCodeTotalBilled, fiscal year = {} {} {}",
                        () -> fiscalYear,
                        billed::getChartOfAccountsCode,
                        billed::getFinancialObjectCode
                );
            }
        }
        return billedsMap;
    }

    /**
     * This method takes all the applicable attributes from the associated award object and sets those attributes
     * into their corresponding invoice attributes.
     *
     * @param invoiceGeneralDetail the invoice detail to populate
     * @param award                The associated award that the invoice will be linked to.
     */
    protected void populateInvoiceDetailFromAward(
            final InvoiceGeneralDetail invoiceGeneralDetail,
            final ContractsAndGrantsBillingAward award) {
        invoiceGeneralDetail.setAwardTotal(award.getAwardTotalAmount());
        invoiceGeneralDetail.setAgencyNumber(award.getAgencyNumber());
        if (ObjectUtils.isNotNull(award.getBillingFrequencyCode())) {
            invoiceGeneralDetail.setBillingFrequencyCode(award.getBillingFrequencyCode());
        }
        if (ObjectUtils.isNotNull(award.getInstrumentTypeCode())) {
            invoiceGeneralDetail.setInstrumentTypeCode(award.getInstrumentTypeCode());
        }
        final String awdDtRange = getDateTimeService().toDateString(award.getAwardBeginningDate()) + " to " +
                                  getDateTimeService().toDateString(award.getAwardEndingDate());
        invoiceGeneralDetail.setAwardDateRange(awdDtRange);

        invoiceGeneralDetail.setTotalPreviouslyBilled(contractsGrantsInvoiceDocumentService
                .getAwardBilledToDateAmount(award.getProposalNumber()));
        final ContractsAndGrantsBillingAgency agency = award.getAgency();
        if (ObjectUtils.isNotNull(agency)) {
            final String customerNumber = agency.getCustomerNumber();
            // default to primary of the customer
            CustomerAddress customerAddress = customerAddressService.getPrimaryAddress(customerNumber);
            // if award has customer address identifier - use it for population
            if (ObjectUtils.isNotNull(award.getCustomerAddressIdentifier())) {
                customerAddress = customerAddressService.getByPrimaryKey(customerNumber, award.getCustomerAddressIdentifier());
            }

            if (StringUtils.isNotBlank(customerAddress.getCustomerInvoiceTemplateCode())) {
                invoiceGeneralDetail.setCustomerInvoiceTemplateCode(customerAddress.getCustomerInvoiceTemplateCode());
            } else {
                final AccountsReceivableCustomer customer = agency.getCustomer();
                if (ObjectUtils.isNotNull(customer) && StringUtils.isNotBlank(customer.getCustomerInvoiceTemplateCode())) {
                    invoiceGeneralDetail.setCustomerInvoiceTemplateCode(customer.getCustomerInvoiceTemplateCode());
                }
            }
            invoiceGeneralDetail.setCustomerNumber(customerNumber);
            invoiceGeneralDetail.setCustomerAddressIdentifier(customerAddress.getCustomerAddressIdentifier());
        }
    }

    /**
     * For letter of credit, this distributes the amount for matching LOC invoice detail account object codes (which
     * is very probably all of invoice detail account object codes in the given list) evenly
     *
     * @param document                         the CINV document we're creating
     * @param awdAcct                          the C&G Award Account
     * @param invoiceDetailAccountObjectsCodes the List of invoice detail account object codes we're attempting to
     *                                         generate
     * @param locReviewDetail                  the contracts grants letter of credit review detail which is related to
     *                                         the given award account
     */
    protected void distributeAmountAmongAllAccountObjectCodes(
            final ContractsGrantsInvoiceDocument document,
            final ContractsAndGrantsBillingAwardAccount awdAcct,
            final List<InvoiceDetailAccountObjectCode> invoiceDetailAccountObjectsCodes,
            final ContractsGrantsLetterOfCreditReviewDetail locReviewDetail) {
        final List<InvoiceDetailAccountObjectCode> locRedistributionInvoiceDetailAccountObjectCodes =
                filterInvoiceAccountObjectCodesByDocumentAndAccount(document, awdAcct,
                        invoiceDetailAccountObjectsCodes);
        final Map<String, List<InvoiceDetailAccountObjectCode>> locRedistributionAccountObjectCodesByCategory =
                mapInvoiceDetailAccountObjectCodesByCategoryCode(locRedistributionInvoiceDetailAccountObjectCodes);
        final Map<String, BigDecimal> percentagesByCategory = calculatePercentagesByCategory(
                locRedistributionAccountObjectCodesByCategory, locReviewDetail.getClaimOnCashBalance().negated());
        final Map<String, KualiDecimal> amountsByCategory = calculateAmountsByCategory(percentagesByCategory,
                locReviewDetail.getAmountToDraw());
        redistributeAmountsToInvoiceAccountCategories(locRedistributionAccountObjectCodesByCategory,
                amountsByCategory);
        takeAPennyLeaveAPennyCGBStyle(locRedistributionInvoiceDetailAccountObjectCodes,
                locReviewDetail.getAmountToDraw());
    }

    /**
     * Filters the given list of invoice detail account object codes by the given document and account
     *
     * @param document                         the document which owns the invoice detail account object codes
     * @param awdAcct                          the award account to find invoice detail account object codes for
     * @param invoiceDetailAccountObjectsCodes the original list of invoice detail account object codes
     * @return a list of invoice detail account object codes associated with the given document and the given account
     */
    protected List<InvoiceDetailAccountObjectCode> filterInvoiceAccountObjectCodesByDocumentAndAccount(
            final ContractsGrantsInvoiceDocument document, final ContractsAndGrantsBillingAwardAccount awdAcct,
            final List<InvoiceDetailAccountObjectCode> invoiceDetailAccountObjectsCodes) {
        final List<InvoiceDetailAccountObjectCode> locRedistributionInvoiceDetailAccountObjectCodes = new ArrayList<>();
        for (final InvoiceDetailAccountObjectCode invoiceDetailAccountObjectCode : invoiceDetailAccountObjectsCodes) {
            if (StringUtils.equals(invoiceDetailAccountObjectCode.getDocumentNumber(), document.getDocumentNumber())
                    && invoiceDetailAccountObjectCode.getProposalNumber()
                        .equals(document.getInvoiceGeneralDetail().getProposalNumber())
                    && StringUtils.equals(invoiceDetailAccountObjectCode.getAccountNumber(), awdAcct.getAccountNumber())
                    && StringUtils.equals(invoiceDetailAccountObjectCode.getChartOfAccountsCode(),
                        awdAcct.getChartOfAccountsCode())) {
                locRedistributionInvoiceDetailAccountObjectCodes.add(invoiceDetailAccountObjectCode);
            }
        }
        return locRedistributionInvoiceDetailAccountObjectCodes;
    }

    /**
     * Sums the current expenditures of the given invoice detail account object codes
     *
     * @param invoiceDetailAccountObjectCodes invoice detail account object codes to total the current expenditures of
     * @return the total of the current expenditures
     */
    protected InvoiceDetailAccountObjectCode sumInvoiceDetailAccountObjectCodes(
            final List<InvoiceDetailAccountObjectCode> invoiceDetailAccountObjectCodes) {
        final InvoiceDetailAccountObjectCode total = new InvoiceDetailAccountObjectCode();
        for (final InvoiceDetailAccountObjectCode invoiceDetailAccountObjectCode : invoiceDetailAccountObjectCodes) {
            total.setCumulativeExpenditures(total.getCumulativeExpenditures()
                    .add(invoiceDetailAccountObjectCode.getCumulativeExpenditures()));
            total.setCurrentExpenditures(total.getCurrentExpenditures()
                    .add(invoiceDetailAccountObjectCode.getCurrentExpenditures()));
            total.setTotalBilled(total.getTotalBilled().add(invoiceDetailAccountObjectCode.getTotalBilled()));
        }
        return total;
    }

    /**
     * Calculates the percentage of the given total each list of invoice detail account object codes represents
     *
     * @param invoiceDetailAccountObjectCodesByCategory a Map of invoice detail account object codes mapped by category
     * @param total                                     the total of all of the invoice detail account object codes
     * @return A Map keyed by category where the value is the percentage of the total that category represents
     */
    protected Map<String, BigDecimal> calculatePercentagesByCategory(
            final Map<String,
            List<InvoiceDetailAccountObjectCode>> invoiceDetailAccountObjectCodesByCategory, final KualiDecimal total) {
        final Map<String, BigDecimal> percentagesByCategory = new HashMap<>();
        for (final String categoryCode : invoiceDetailAccountObjectCodesByCategory.keySet()) {
            if (total.equals(KualiDecimal.ZERO)) {
                percentagesByCategory.put(categoryCode, BigDecimal.ZERO);
            } else {
                percentagesByCategory.put(categoryCode, calculatePercentageByInvoiceDetailAccountObjectCodes(
                        invoiceDetailAccountObjectCodesByCategory.get(categoryCode), total));
            }
        }
        return percentagesByCategory;
    }

    /**
     * Finds the percentage that the given total is of the sum of the current expenditures of the given
     * invoiceDetailAccountObjectCodes
     *
     * @param invoiceDetailAccountObjectCodes a List of invoice detail account object codes to sum
     * @param total                           the total of all of the invoice detail account object codes for that
     *                                        account
     * @return the percentage of the total of the given List of invoice detail account object code current
     *         expenditures are of the given total
     */
    protected BigDecimal calculatePercentageByInvoiceDetailAccountObjectCodes(
            final List<InvoiceDetailAccountObjectCode> invoiceDetailAccountObjectCodes, final KualiDecimal total) {
        final KualiDecimal cumulativeExpenditureTotal = sumInvoiceDetailAccountObjectCodes(
                invoiceDetailAccountObjectCodes).getCumulativeExpenditures();
        return cumulativeExpenditureTotal.bigDecimalValue().divide(total.bigDecimalValue(), 10, RoundingMode.HALF_UP);
    }

    /**
     * Given a Map of category keys mapping percentage values and an amount, find what amount each percentage would be
     *
     * @param percentagesByCategory a map of category code keys mapping percentage values
     * @param amount                the amount to split by percentages
     * @return a Map of amounts keyed by category codes
     */
    protected Map<String, KualiDecimal> calculateAmountsByCategory(
            final Map<String, BigDecimal> percentagesByCategory,
            final KualiDecimal amount) {
        final BigDecimal bigDecimalAmount = amount.bigDecimalValue().setScale(2, RoundingMode.HALF_UP);
        final Map<String, KualiDecimal> amountsByCategory = new HashMap<>();
        for (final String categoryCode : percentagesByCategory.keySet()) {
            amountsByCategory.put(categoryCode, new KualiDecimal(bigDecimalAmount.multiply(
                    percentagesByCategory.get(categoryCode))));
        }
        return amountsByCategory;
    }

    /**
     * Redistributes the given amounts mapped by category to each of the invoice detail account object codes mapped
     * by category code
     *
     * @param redistributionAccountObjectCodesByCategory invoice detail account object codes mapped by category code
     * @param amountsByCategory                          amounts mapped by category code
     */
    protected void redistributeAmountsToInvoiceAccountCategories(
            final Map<String,
            List<InvoiceDetailAccountObjectCode>> redistributionAccountObjectCodesByCategory,
            final Map<String, KualiDecimal> amountsByCategory) {
        for (final String categoryCode : redistributionAccountObjectCodesByCategory.keySet()) {
            final List<InvoiceDetailAccountObjectCode> invoiceDetailAccountObjectCodes =
                    redistributionAccountObjectCodesByCategory.get(categoryCode);
            if (invoiceDetailAccountObjectCodes.size() == 1) {
                invoiceDetailAccountObjectCodes.get(0).setCurrentExpenditures(amountsByCategory.get(categoryCode));
            } else {
                splitOutRedistribution(invoiceDetailAccountObjectCodes, amountsByCategory.get(categoryCode));
            }
        }
    }

    /**
     * If the total of current expenditures within the list of InvoiceDetailAccountObjectCode business objects does
     * not meet the amount to target, steal or give a penny from one of those business objects so that it does
     *
     * @param invoiceDetailAccountObjectCodes a List of InvoiceDetailAccountObjectCode business objects
     * @param amountToTarget                  the amount which the sum of those objects should equal
     */
    protected void takeAPennyLeaveAPennyCGBStyle(
            final List<InvoiceDetailAccountObjectCode> invoiceDetailAccountObjectCodes,
            final KualiDecimal amountToTarget) {
        if (!CollectionUtils.isEmpty(invoiceDetailAccountObjectCodes)) {
            final KualiDecimal currentExpenditureTotal = sumInvoiceDetailAccountObjectCodes(
                    invoiceDetailAccountObjectCodes).getCurrentExpenditures();
            if (!currentExpenditureTotal.equals(amountToTarget)) {
                final KualiDecimal difference = currentExpenditureTotal.subtract(amountToTarget);
                final InvoiceDetailAccountObjectCode invoiceDetailAccountObjectCode =
                        findFirstPositiveCurrentExpenditureInvoiceDetailAccountObjectCode(
                                invoiceDetailAccountObjectCodes);
                if (invoiceDetailAccountObjectCode != null) {
                    invoiceDetailAccountObjectCode.setCurrentExpenditures(
                            invoiceDetailAccountObjectCode.getCurrentExpenditures().subtract(difference));
                }
            }
        }
    }

    /**
     * Given a list of invoice detail account object codes, return the first one with a positive currentExpenditure
     * field
     *
     * @param invoiceDetailAccountObjectCodes the list of InvoiceDetailAccountObjectCodes to find the first one with
     *                                        a positive currentExpenditure
     * @return the first invoice detail account object code with a positive currentExpenditure or null if nothing
     *         could be found
     */
    protected InvoiceDetailAccountObjectCode findFirstPositiveCurrentExpenditureInvoiceDetailAccountObjectCode(
            final List<InvoiceDetailAccountObjectCode> invoiceDetailAccountObjectCodes) {
        for (final InvoiceDetailAccountObjectCode invoiceDetailAccountObjectCode : invoiceDetailAccountObjectCodes) {
            if (ObjectUtils.isNotNull(invoiceDetailAccountObjectCode.getCurrentExpenditures())
                    && invoiceDetailAccountObjectCode.getCurrentExpenditures().isPositive()) {
                return invoiceDetailAccountObjectCode;
            }
        }
        return null;
    }

    /**
     * Splits an amount evenly over the given List of invoice detail account object codes
     *
     * @param invoiceDetailAccountObjectCodes a List of invoice detail account object codes to divvy an amount equally
     *                                        among
     * @param amount                          the amount to divvy
     */
    protected void splitOutRedistribution(
            final List<InvoiceDetailAccountObjectCode> invoiceDetailAccountObjectCodes,
            final KualiDecimal amount) {
        final KualiDecimal amountEach = new KualiDecimal(amount.bigDecimalValue()
                .divide(new BigDecimal(invoiceDetailAccountObjectCodes.size()), 2, RoundingMode.HALF_UP));
        for (final InvoiceDetailAccountObjectCode invoiceDetailAccountObjectCode : invoiceDetailAccountObjectCodes) {
            invoiceDetailAccountObjectCode.setCurrentExpenditures(amountEach);
        }
    }

    /**
     * Updates all of the given invoice detail object codes by the billed amount for the given award account (and
     * updates the current expenditures accordingly)
     *
     * @param awdAcct                          the award account to find billing information for
     * @param invoiceDetailAccountObjectsCodes the List of invoice detail account object code business objects to update
     */
    protected void updateInvoiceDetailAccountObjectCodesByBilledAmount(
            final ContractsAndGrantsBillingAwardAccount awdAcct,
            final List<InvoiceDetailAccountObjectCode> invoiceDetailAccountObjectsCodes) {
        final List<AwardAccountObjectCodeTotalBilled> awardAccountObjectCodeTotalBilledList =
                retrieveBillingInformationForAwardAccount(awdAcct);

        for (final InvoiceDetailAccountObjectCode invoiceDetailAccountObjectCode : invoiceDetailAccountObjectsCodes) {
            // since there may be multiple accounts represented in the Invoice Detail Account Object Codes, only
            // process the ones that match
            if (StringUtils.equals(invoiceDetailAccountObjectCode.getChartOfAccountsCode(),
                        awdAcct.getChartOfAccountsCode())
                    && StringUtils.equals(invoiceDetailAccountObjectCode.getAccountNumber(),
                        awdAcct.getAccountNumber())) {
                if (!CollectionUtils.isEmpty(awardAccountObjectCodeTotalBilledList)) {
                    for (final AwardAccountObjectCodeTotalBilled awardAccountObjectCodeTotalBilled :
                            awardAccountObjectCodeTotalBilledList) {
                        if (invoiceDetailAccountObjectCode.getFinancialObjectCode().equalsIgnoreCase(
                                awardAccountObjectCodeTotalBilled.getFinancialObjectCode())) {
                            invoiceDetailAccountObjectCode.setTotalBilled(
                                    awardAccountObjectCodeTotalBilled.getTotalBilled());
                        }
                    }
                }
                invoiceDetailAccountObjectCode.setCurrentExpenditures(
                        invoiceDetailAccountObjectCode.getCumulativeExpenditures()
                                .subtract(invoiceDetailAccountObjectCode.getTotalBilled()));
            }
        }
    }

    /**
     * Retrieves all of the billing information performed against the given award account
     *
     * @param awdAcct a C&G award account
     * @return the List of billing information
     */
    protected List<AwardAccountObjectCodeTotalBilled> retrieveBillingInformationForAwardAccount(
            final ContractsAndGrantsBillingAwardAccount awdAcct) {
        final Map<String, Object> totalBilledKeys = new HashMap<>();
        totalBilledKeys.put(KFSPropertyConstants.PROPOSAL_NUMBER, awdAcct.getProposalNumber());
        totalBilledKeys.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, awdAcct.getChartOfAccountsCode());
        totalBilledKeys.put(KFSPropertyConstants.ACCOUNT_NUMBER, awdAcct.getAccountNumber());

        return (List<AwardAccountObjectCodeTotalBilled>) businessObjectService.findMatching(
                AwardAccountObjectCodeTotalBilled.class, totalBilledKeys);
    }

    /**
     * @return a Collection of all active Contracts & Grants billing categories
     */
    protected Collection<CostCategory> retrieveAllBillingCategories() {
        final Map<String, Object> criteria = new HashMap<>();
        criteria.put(KFSPropertyConstants.ACTIVE, true);
        return businessObjectService.findMatching(CostCategory.class, criteria);
    }

    /**
     * Looks up or constructs an InvoiceDetailAccountObjectCode based on a given balance and billing category
     *
     * @param invoiceDetailAccountObjectCodes the list of invoice detail account object codes to find a matching
     *                                        Invoice Detail Account Object Code in
     * @param bal                             the balance to get the account object code from
     * @param documentNumber                  the document number of the CINV doc being created
     * @param proposalNumber                  the proposal number associated with the award on the CINV document we're
     *                                        currently building
     * @param category                        the cost category associated with the balance
     * @return the retrieved or constructed (if nothing was found in the database) InvoiceDetailAccountObjectCode
     *         object
     */
    protected InvoiceDetailAccountObjectCode getInvoiceDetailAccountObjectCodeByBalanceAndCategory(
            final List<InvoiceDetailAccountObjectCode> invoiceDetailAccountObjectCodes, final Balance bal, final String documentNumber,
            final String proposalNumber, final CostCategory category) {
        // Check if there is an existing invoice detail account object code existing (if there are more than one
        // fiscal years)
        InvoiceDetailAccountObjectCode invoiceDetailAccountObjectCode = lookupInvoiceDetailAccountObjectCode(
                invoiceDetailAccountObjectCodes, bal, proposalNumber);

        if (ObjectUtils.isNull(invoiceDetailAccountObjectCode)) {
            if (ObjectUtils.isNotNull(category)) {
                invoiceDetailAccountObjectCode = new InvoiceDetailAccountObjectCode();
                invoiceDetailAccountObjectCode.setDocumentNumber(documentNumber);
                invoiceDetailAccountObjectCode.setProposalNumber(proposalNumber);
                invoiceDetailAccountObjectCode.setFinancialObjectCode(bal.getObjectCode());
                invoiceDetailAccountObjectCode.setCategoryCode(category.getCategoryCode());
                invoiceDetailAccountObjectCode.setAccountNumber(bal.getAccountNumber());
                invoiceDetailAccountObjectCode.setChartOfAccountsCode(bal.getChartOfAccountsCode());
                invoiceDetailAccountObjectCodes.add(invoiceDetailAccountObjectCode);
            } else {
                LOG.warn(
                        "Could not find cost category for balance: {} {} {} {} {} {} {}",
                        bal::getUniversityFiscalYear,
                        bal::getChartOfAccountsCode,
                        bal::getAccountNumber,
                        bal::getSubAccountNumber,
                        bal::getObjectCode,
                        bal::getSubObjectCode,
                        bal::getBalanceTypeCode
                );
            }
        }
        return invoiceDetailAccountObjectCode;
    }

    /**
     * Looks for a matching invoice detail account object code in the given list that matches the given balance and
     * proposal number
     *
     * @param invoiceDetailAccountObjectsCodes a List of invoice detail account object codes to look up values from
     * @param bal                              the balance to match
     * @param proposalNumber                   the proposal number to match
     * @return the matching invoice detail account object code record, or null if no matching record can be found
     */
    protected InvoiceDetailAccountObjectCode lookupInvoiceDetailAccountObjectCode(
            final List<InvoiceDetailAccountObjectCode> invoiceDetailAccountObjectsCodes, final Balance bal,
            final String proposalNumber) {
        for (final InvoiceDetailAccountObjectCode invoiceDetailAccountObjectCode : invoiceDetailAccountObjectsCodes) {
            if (StringUtils.equals(bal.getChartOfAccountsCode(), invoiceDetailAccountObjectCode.getChartOfAccountsCode())
                && StringUtils.equals(bal.getAccountNumber(), invoiceDetailAccountObjectCode.getAccountNumber())
                && StringUtils.equals(bal.getObjectCode(), invoiceDetailAccountObjectCode.getFinancialObjectCode())
                && Objects.equals(proposalNumber, invoiceDetailAccountObjectCode.getProposalNumber())) {
                return invoiceDetailAccountObjectCode;
            }
        }
        return null;
    }

    /**
     * Determines if a balance represents a cost share or not
     *
     * @param bal the balance to check
     * @return true if the balance is a cost share, false otherwise
     */
    protected boolean isBalanceCostShare(final Balance bal) {
        return ObjectUtils.isNotNull(bal.getSubAccount())
                && ObjectUtils.isNotNull(bal.getSubAccount().getA21SubAccount())
                && StringUtils.equalsIgnoreCase(bal.getSubAccount().getA21SubAccount().getSubAccountTypeCode(),
                    KFSConstants.SubAccountType.COST_SHARE);
    }

    /**
     * Retrieves balances used to populate amounts for an invoice account detail
     *
     * @param fiscalYear          the fiscal year of the balances to find
     * @param chartOfAccountsCode the chart of accounts code of balances to find
     * @param accountNumber       the account number of balances to find
     * @param balanceTypeCodeList the balance type codes of balances to find
     * @return a List of retrieved balances
     */
    protected List<Balance> retrieveBalances(
            final Integer fiscalYear, final String chartOfAccountsCode, final String accountNumber,
            final List<String> balanceTypeCodeList) {
        final Map<String, Object> balanceKeys = new HashMap<>();
        balanceKeys.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartOfAccountsCode);
        balanceKeys.put(KFSPropertyConstants.ACCOUNT_NUMBER, accountNumber);
        balanceKeys.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, fiscalYear);
        balanceKeys.put(KFSPropertyConstants.OBJECT_TYPE_CODE, retrieveExpenseObjectTypes());
        balanceKeys.put(KFSPropertyConstants.BALANCE_TYPE_CODE, balanceTypeCodeList);
        return (List<Balance>) getBusinessObjectService().findMatching(Balance.class, balanceKeys);
    }

    /**
     * This method helps in setting up basic values for Contracts & Grants Invoice Document
     */
    protected void populateContractsGrantsInvoiceDocument(
            final ContractsAndGrantsBillingAward award,
            final ContractsGrantsInvoiceDocument document, final List<ContractsGrantsLetterOfCreditReviewDetail> locReviewDetails,
            final String locCreationType) {
        if (ObjectUtils.isNotNull(award.getAgency())) {
            if (ObjectUtils.isNotNull(document.getAccountsReceivableDocumentHeader())) {
                document.getAccountsReceivableDocumentHeader().setCustomerNumber(award.getAgency().getCustomerNumber());
            }
            final Customer customer = getCustomerService().getByPrimaryKey(award.getAgency().getCustomerNumber());
            if (ObjectUtils.isNotNull(customer)) {
                document.setCustomerName(customer.getCustomerName());
            }
        }
        // To set open invoice indicator to true to help doing cash control for the invoice
        document.setOpenInvoiceIndicator(true);

        // To set LOC creation type and appropriate values from award.
        if (StringUtils.isNotBlank(locCreationType)) {
            document.getInvoiceGeneralDetail().setLetterOfCreditCreationType(locCreationType);
        }
        // To set up values for Letter of Credit Fund and Fund Group irrespective of the LOC Creation type.
        if (StringUtils.isNotEmpty(award.getLetterOfCreditFundCode())) {
            document.getInvoiceGeneralDetail().setLetterOfCreditFundCode(award.getLetterOfCreditFundCode());
        }
        if (ObjectUtils.isNotNull(award.getLetterOfCreditFund())) {
            if (StringUtils.isNotEmpty(award.getLetterOfCreditFund().getLetterOfCreditFundGroupCode())) {
                document.getInvoiceGeneralDetail().setLetterOfCreditFundGroupCode(award.getLetterOfCreditFund()
                        .getLetterOfCreditFundGroupCode());
            }
        }

        final KualiDecimal totalAmountBilledToDate;
        if (ArConstants.BillingFrequencyValues.isMilestone(award)) {
            totalAmountBilledToDate = getContractsGrantsInvoiceDocumentService().getInvoiceMilestoneTotal(document)
                    .add(document.getInvoiceGeneralDetail().getTotalPreviouslyBilled());
        } else if (ArConstants.BillingFrequencyValues.isPredeterminedBilling(award)) {
            totalAmountBilledToDate = getContractsGrantsInvoiceDocumentService().getBillAmountTotal(document)
                    .add(document.getInvoiceGeneralDetail().getTotalPreviouslyBilled());
        } else {
            totalAmountBilledToDate = calculateTotalExpenditureAmount(document, locReviewDetails)
                    .add(getContractsGrantsInvoiceDocumentService().getOtherTotalBilledForAwardPeriod(document));
        }
        document.getInvoiceGeneralDetail().setTotalAmountBilledToDate(totalAmountBilledToDate);
    }

    protected KualiDecimal calculateTotalExpenditureAmount(
            final ContractsGrantsInvoiceDocument document,
            final List<ContractsGrantsLetterOfCreditReviewDetail> locReviewDetails) {
        
        /*
         * CU Customization backport FINP-10147
         */
        
        /*
        final Map<String, KualiDecimal> totalBilledByAccountNumberMap = new HashMap<>();
        for (final InvoiceDetailAccountObjectCode objectCode: document.getInvoiceDetailAccountObjectCodes()) {
            final String key = objectCode.getChartOfAccountsCode() + "-" + objectCode.getAccountNumber();
            KualiDecimal totalBilled = cleanAmount(totalBilledByAccountNumberMap.get(key));
            totalBilled = totalBilled.add(objectCode.getTotalBilled());
            totalBilledByAccountNumberMap.put(key, totalBilled);
        }
        */

        KualiDecimal totalExpendituredAmount = KualiDecimal.ZERO;
        for (final InvoiceAccountDetail invAcctD : document.getAccountDetails()) {
            final String chartOfAccountsCode = invAcctD.getChartOfAccountsCode();
            final String accountNumber = invAcctD.getAccountNumber();
            
            /*
             * CU Customization backport FINP-10147
             */
            
            /*
            final String key = chartOfAccountsCode + "-" + accountNumber;
            if (ObjectUtils.isNotNull(totalBilledByAccountNumberMap.get(key))) {
                invAcctD.setTotalPreviouslyBilled(totalBilledByAccountNumberMap.get(key));
            } else {
                invAcctD.setTotalPreviouslyBilled(KualiDecimal.ZERO);
                */
            final Map<String, Object> mapKey = new HashMap<>();
            mapKey.put(KFSPropertyConstants.ACCOUNT_NUMBER, accountNumber);
            mapKey.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartOfAccountsCode);
            mapKey.put(KFSPropertyConstants.PROPOSAL_NUMBER, invAcctD.getProposalNumber());
            final AwardAccount awardAccountToFindTotalBilled =
                    businessObjectService.findByPrimaryKey(AwardAccount.class, mapKey);
            KualiDecimal totalBilled = KualiDecimal.ZERO;
            if (ObjectUtils.isNotNull(awardAccountToFindTotalBilled)) {
                final List<AwardAccountObjectCodeTotalBilled> awardAccountObjectCodeTotalBilleds =
                        awardAccountObjectCodeTotalBilledDao
                                .getAwardAccountObjectCodeTotalBuildByProposalNumberAndAccount(
                                        List.of(awardAccountToFindTotalBilled)
                                );

                for (final AwardAccountObjectCodeTotalBilled accountObjectCodeTotalBilled :
                        awardAccountObjectCodeTotalBilleds) {
                    totalBilled = totalBilled.add(accountObjectCodeTotalBilled.getTotalBilled());
                }
            }
            
            invAcctD.setTotalPreviouslyBilled(totalBilled);

            if (invAcctD.getTotalPreviouslyBilled().isZero()) {
                final String proposalNumber = document.getInvoiceGeneralDetail().getProposalNumber();
                KualiDecimal previouslyBilledAmount = KualiDecimal.ZERO;

                previouslyBilledAmount = previouslyBilledAmount.add(
                        contractsGrantsInvoiceDocumentService.getPredeterminedBillingBilledToDateAmount(proposalNumber,
                                chartOfAccountsCode, accountNumber));
                previouslyBilledAmount = previouslyBilledAmount.add(
                        contractsGrantsInvoiceDocumentService.getMilestonesBilledToDateAmount(proposalNumber,
                                chartOfAccountsCode, accountNumber));
                invAcctD.setTotalPreviouslyBilled(previouslyBilledAmount);
            }

            KualiDecimal currentExpenditureAmount = invAcctD.getCumulativeExpenditures()
                    .subtract(invAcctD.getTotalPreviouslyBilled());
            invAcctD.setInvoiceAmount(currentExpenditureAmount);
            // overwrite account detail expenditure amount if locReview Indicator is true and award is LOC Billing
            if (ObjectUtils.isNotNull(document.getInvoiceGeneralDetail())) {
                final ContractsAndGrantsBillingAward award = document.getInvoiceGeneralDetail().getAward();
                if (ObjectUtils.isNotNull(award) && ArConstants.BillingFrequencyValues.isLetterOfCredit(award)
                        && !CollectionUtils.isEmpty(locReviewDetails)) {
                    for (final ContractsAndGrantsBillingAwardAccount awardAccount : award.getActiveAwardAccounts()) {
                        final ContractsGrantsLetterOfCreditReviewDetail locReviewDetail =
                                retrieveMatchingLetterOfCreditReviewDetail(awardAccount, locReviewDetails);
                        if (ObjectUtils.isNotNull(locReviewDetail)
                                && StringUtils.equals(awardAccount.getChartOfAccountsCode(), chartOfAccountsCode)
                                && StringUtils.equals(awardAccount.getAccountNumber(), accountNumber)) {
                            currentExpenditureAmount = locReviewDetail.getAmountToDraw();
                            invAcctD.setInvoiceAmount(currentExpenditureAmount);
                        }
                    }
                }
            }
            totalExpendituredAmount = totalExpendituredAmount.add(currentExpenditureAmount);
        }
        totalExpendituredAmount = totalExpendituredAmount.add(
                document.getInvoiceGeneralDetail().getTotalPreviouslyBilled());

        return totalExpendituredAmount;
    }

    /**
     * Null protects the addition in retrieveAccurateBalanceAmount
     *
     * @param amount the amount to return
     * @return zero if the amount was null, the given amount otherwise
     */
    protected KualiDecimal cleanAmount(final KualiDecimal amount) {
        return amount == null ? KualiDecimal.ZERO : amount;
    }

    @Override
    public Collection<ContractsAndGrantsBillingAward> retrieveAllAwards() {
        final Map<String, Object> map = new HashMap<>();
        map.put(KFSPropertyConstants.ACTIVE, true);
        // It would be nice not to have to manually remove LOC awards, maybe when we convert to KRAD
        // we could leverage the KRAD-DATA Criteria framework to avoid this
        return kualiModuleService.getResponsibleModuleService(ContractsAndGrantsBillingAward.class)
                .getExternalizableBusinessObjectsList(ContractsAndGrantsBillingAward.class, map);
    }

    @Override
    public Collection<ContractsAndGrantsBillingAward> validateAwards(
            final Collection<ContractsAndGrantsBillingAward> awards,
            Collection<ContractsGrantsInvoiceDocumentErrorLog> contractsGrantsInvoiceDocumentErrorLogs,
            final String errOutputFile, final ContractsAndGrantsInvoiceDocumentCreationProcessType creationProcessType) {
        final Map<ContractsAndGrantsBillingAward, List<String>> invalidGroup = new HashMap<>();
        final List<ContractsAndGrantsBillingAward> qualifiedAwards = new ArrayList<>();

        if (ObjectUtils.isNull(contractsGrantsInvoiceDocumentErrorLogs)) {
            contractsGrantsInvoiceDocumentErrorLogs = new ArrayList<>();
        }

        performAwardValidation(awards, invalidGroup, qualifiedAwards, creationProcessType);

        if (!CollectionUtils.isEmpty(invalidGroup)) {
            if (StringUtils.isNotBlank(errOutputFile)) {
                writeErrorToFile(invalidGroup, errOutputFile);
            }
            storeValidationErrors(invalidGroup, contractsGrantsInvoiceDocumentErrorLogs, creationProcessType.getCode());
        }

        return qualifiedAwards;
    }

    /*
     * CU Customization: KFSPTS-23675
     * Allow for using excluded-from-invoicing awards when creating a manual invoice.
     *
     * CU Customization: KFSPTS-23690
     * Set transient attribute creationProcessType on Award so that data is available in the
     * VerifyBillingFrequencyService.validateBillingFrequency method.
     */
    /**
     * Perform all validation checks on the awards passed in to determine if CGB Invoice documents can be
     * created for the given awards.
     * @param awards                  List of awards to be validated
     * @param invalidGroup            Map of errors per award that failed validation
     * @param qualifiedAwards         List of awards that are valid to create CGB Invoice docs from
     * @param creationProcessType     The type of process is creating the awards to be validated
     */
    protected void performAwardValidation(
            final Collection<ContractsAndGrantsBillingAward> awards,
            final Map<ContractsAndGrantsBillingAward, List<String>> invalidGroup,
            final List<ContractsAndGrantsBillingAward> qualifiedAwards, final ContractsAndGrantsInvoiceDocumentCreationProcessType creationProcessType) {
        final Set<ContractsAndGrantsBillingAward> awardsWithDuplicateAccounts = findAwardsWithDuplicateAccounts(awards);

        for (final ContractsAndGrantsBillingAward award : awards) {
            final List<String> errorList = new ArrayList<>();

            // CUMod: KFSPTS-23675 (portion for excluded-from-invoicing handling)
            if (award.isExcludedFromInvoicing()
                    && ContractsAndGrantsInvoiceDocumentCreationProcessType.MANUAL != creationProcessType) {
                errorList.add(configurationService.getPropertyValueAsString(
                        ArKeyConstants.CGINVOICE_CREATION_AWARD_EXCLUDED_FROM_INVOICING));
            } else if (ContractsAndGrantsInvoiceDocumentCreationProcessType.BATCH == creationProcessType
                    && StringUtils.equals(award.getBillingFrequencyCode(), ArConstants.BillingFrequencyValues.MANUAL.getCode())) {
                errorList.add(configurationService.getPropertyValueAsString(ArKeyConstants.CGINVOICE_CREATION_MANUAL_BILLING_FREQUENCY));
            } else {
                if (awardsWithDuplicateAccounts.contains(award)) {
                    errorList.add(configurationService.getPropertyValueAsString(
                            ArKeyConstants.CGINVOICE_CREATION_ACCOUNT_ON_MULTIPLE_AWARDS));
                }
                if (ArConstants.BillingFrequencyValues.isLetterOfCredit(award)
                        && ContractsAndGrantsInvoiceDocumentCreationProcessType.LOC != creationProcessType) {
                    errorList.add(configurationService.getPropertyValueAsString(
                            ArKeyConstants.CGINVOICE_CREATION_AWARD_LOCB_BILLING_FREQUENCY));
                } else {
                    if (award.getAwardBeginningDate() != null) {
                        if (award.getBillingFrequencyCode() != null
                                && getContractsGrantsBillingAwardVerificationService()
                                    .isValueOfBillingFrequencyValid(award)) {
                            final boolean checkGracePeriod = ContractsAndGrantsInvoiceDocumentCreationProcessType.MANUAL != creationProcessType;
                            /*
                             * CU Customization KFSPTS-23690 (portion for billing frequency handling)
                             */
                            award.setCreationProcessType(creationProcessType);
                            if (verifyBillingFrequencyService.validateBillingFrequency(award, checkGracePeriod)) {
                                validateAward(errorList, award, creationProcessType);
                            } else {
                                errorList.add(configurationService.getPropertyValueAsString(
                                        ArKeyConstants.CGINVOICE_CREATION_AWARD_INVALID_BILLING_PERIOD));
                            }
                        } else {
                            errorList.add(configurationService.getPropertyValueAsString(
                                    ArKeyConstants.CGINVOICE_CREATION_BILLING_FREQUENCY_MISSING_ERROR));
                        }
                    } else {
                        errorList.add(configurationService.getPropertyValueAsString(
                                ArKeyConstants.CGINVOICE_CREATION_AWARD_START_DATE_MISSING_ERROR));
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

    /**
     * Determines which awards have accounts that are used in multiple awards.
     *
     * @param awards The list of awards to check.
     * @return The set of awards with duplicate accounts.
     */
    protected Set<ContractsAndGrantsBillingAward> findAwardsWithDuplicateAccounts(
            final Collection<ContractsAndGrantsBillingAward> awards) {
        // Get the list of awards associated with each account
        final Map<String, List<ContractsAndGrantsBillingAward>> accountMap = awards.stream()
                .flatMap(award -> award.getActiveAwardAccounts().stream()
                        .map(awardAccount -> new SimpleEntry<>(awardAccount.getChartOfAccountsCode() + awardAccount.getAccountNumber(), award)))
                .collect(Collectors.groupingBy(Entry::getKey,
                        Collectors.mapping(Entry::getValue, Collectors.toList())));

        // Return the awards that are in groups of more than one
        return accountMap.entrySet().stream().filter(entry -> entry.getValue().size() > 1)
                .flatMap(entry -> entry.getValue().stream()).collect(Collectors.toSet());
    }

    /*
     * CU Customization: KFSPTS-26393
     * Base code method ContractsGrantsBillingAwardVerificationService.owningAgencyHasCustomerRecord only verifies
     * that a record exists for the customer associated to the agency that is associated to the award.
     *
     * Processing after that validation in method buildInvoiceAddressDetails then uses the customer number that is
     * directly associated to the award for its processing. That award customer number is used to look up the customer
     * address in various ways resulting in stacktraces being generated when there is a mismtach between the two
     * customer identifiers (award-agency-customer and award-customer) OR when that downstream method presumes a
     * customer address is obtained even though one is not. Validation checks to report these two conditions for the
     * BATCH creationProcessType have been added so that the batch job will successfully complete and report to the user
     * what data needs to be fixed. These validation checks are NOT performed for any other creationProcessType.
     *
     * Validate that the customer number on the award as part if the customer address composite key matches the
     * customer number associated to the agency associated to the award.
     *
     * Validate that a customer address for invoicing purposes can be obtained in the same manner that method
     * buildInvoiceAddressDetails obtains the customer address.
     */
    /**
     * Perform validation for an award to determine if a CGB Invoice document can be created for the award.
     *
     * @param errorList list of validation errors per award
     * @param award     to perform validation upon
     * @param creationProcessType invoice document creation process type
     */
    protected void validateAward(final List<String> errorList, final ContractsAndGrantsBillingAward award, final ContractsAndGrantsInvoiceDocumentCreationProcessType creationProcessType) {

        if (!award.isActive()) {
            errorList.add(configurationService.getPropertyValueAsString(
                    ArKeyConstants.CGINVOICE_CREATION_AWARD_INACTIVE_ERROR));
        }

        if (StringUtils.isEmpty(award.getInvoicingOptionCode())) {
            errorList.add(configurationService.getPropertyValueAsString(
                    ArKeyConstants.CGINVOICE_CREATION_INVOICING_OPTION_MISSING_ERROR));
        }

        if (CollectionUtils.isEmpty(award.getActiveAwardAccounts())) {
            errorList.add(configurationService.getPropertyValueAsString(
                    ArKeyConstants.CGINVOICE_CREATION_NO_ACTIVE_ACCOUNTS_ASSIGNED_ERROR));
        }

        if (getContractsGrantsBillingAwardVerificationService().isAwardFinalInvoiceAlreadyBuilt(award)) {
            errorList.add(configurationService.getPropertyValueAsString(
                    ArKeyConstants.CGINVOICE_CREATION_AWARD_FINAL_BILLED_ERROR));
        }

        if (ArConstants.BillingFrequencyValues.isMilestone(award)
                && !getContractsGrantsBillingAwardVerificationService().hasMilestonesToInvoice(award)) {
            errorList.add(configurationService.getPropertyValueAsString(
                    ArKeyConstants.CGINVOICE_CREATION_AWARD_NO_VALID_MILESTONES));
        }

        if (ArConstants.BillingFrequencyValues.isPredeterminedBilling(award)
                && !getContractsGrantsBillingAwardVerificationService().hasBillsToInvoice(award)) {
            errorList.add(configurationService.getPropertyValueAsString(
                    ArKeyConstants.CGINVOICE_CREATION_AWARD_NO_VALID_BILLS));
        }

        /*
         * CU Customization: KFSPTS-26393
         */
        if (!getContractsGrantsBillingAwardVerificationService().owningAgencyHasCustomerRecord(award)) {
            errorList.add(configurationService.getPropertyValueAsString(
                    ArKeyConstants.CGINVOICE_CREATION_AWARD_AGENCY_NO_CUSTOMER_RECORD));
        } else if (!getCuCustomerAddressHelperService().agencyCustomerMatchesAwardCustomer(award, creationProcessType)) {
                errorList.add(configurationService.getPropertyValueAsString(
                        CuArKeyConstants.CGINVOICE_CREATION_AGENCY_CUSTOMER_MISMATCH));
        } else if (!getCuCustomerAddressHelperService().invoicingCustomerAddressExists(award, creationProcessType)) {
            errorList.add(configurationService.getPropertyValueAsString(
                    CuArKeyConstants.CGINVOICE_CREATION_CUSTOMER_INVOICING_ADDRESS_MISSING));
            
        }

        if (!hasBillableAccounts(award, creationProcessType)) {
            errorList.add(configurationService.getPropertyValueAsString(
                    ArKeyConstants.CGINVOICE_CREATION_AWARD_NO_VALID_ACCOUNTS));
        }

        final List<String> errorString = contractsGrantsInvoiceDocumentService.checkAwardContractControlAccounts(award);
        if (!CollectionUtils.isEmpty(errorString) && errorString.size() > 1) {
            errorList.add(configurationService.getPropertyValueAsString(errorString.get(0))
                    .replace("{0}", errorString.get(1)));
        }

        if (!getContractsGrantsBillingAwardVerificationService().isChartAndOrgSetupForInvoicing(award)) {
            errorList.add(configurationService.getPropertyValueAsString(
                    ArKeyConstants.CGINVOICE_CREATION_SYS_INFO_OADF_NOT_SETUP));
        }

        if (!getContractsGrantsBillingAwardVerificationService().hasIncomeAndReceivableObjectCodes(award)) {
            errorList.add(configurationService.getPropertyValueAsString(
                    ArKeyConstants.ContractsGrantsInvoiceConstants.ERROR_NO_MATCHING_CONTRACT_GRANTS_INVOICE_OBJECT_CODE)
                    .replace("{0}", award.getProposalNumber()));
        }
    }

    protected void writeErrorToFile(
            final Map<ContractsAndGrantsBillingAward, List<String>> invalidGroup,
            final String errOutputFile) {
        final File errOutPutfile = new File(errOutputFile);
        try (PrintStream outputFileStream = new PrintStream(errOutPutfile, StandardCharsets.UTF_8)) {
            writeReportHeader(outputFileStream);

            for (final ContractsAndGrantsBillingAward award : invalidGroup.keySet()) {
                writeErrorEntryByAward(award, invalidGroup.get(award), outputFileStream);
            }

            outputFileStream.print("\r\n");
        } catch (final IOException ioe) {
            LOG.error(
                    "Could not write errors in Contracts & Grants Invoice Document creation process to file{}",
                    ioe::getMessage
            );
            throw new RuntimeException("Could not write errors in Contracts & Grants Invoice Document creation " +
                    "process to file", ioe);
        }
    }

    /**
     * CU Customization:
     * Fixes NullPointerException that can occur when getting the award total amount.
     * award.getAwardTotalAmount().bigDecimalValue() causes the NullPointerException if the total amount returned null.
     */
    protected void storeValidationErrors(
            final Map<ContractsAndGrantsBillingAward, List<String>> invalidGroup,
            final Collection<ContractsGrantsInvoiceDocumentErrorLog> contractsGrantsInvoiceDocumentErrorLogs,
            final String creationProcessTypeCode) {
        for (final ContractsAndGrantsBillingAward award : invalidGroup.keySet()) {
            KualiDecimal cumulativeExpenses = KualiDecimal.ZERO;
            final ContractsGrantsInvoiceDocumentErrorLog contractsGrantsInvoiceDocumentErrorLog =
                    new ContractsGrantsInvoiceDocumentErrorLog();

            if (ObjectUtils.isNotNull(award)) {
                final Date beginningDate = award.getAwardBeginningDate();
                final Date endingDate = award.getAwardEndingDate();
                final SystemOptions systemOptions = optionsService.getCurrentYearOptions();

                contractsGrantsInvoiceDocumentErrorLog.setProposalNumber(award.getProposalNumber());
                contractsGrantsInvoiceDocumentErrorLog.setAwardBeginningDate(beginningDate);
                contractsGrantsInvoiceDocumentErrorLog.setAwardEndingDate(endingDate);
                /*
                 * CU Customization
                 */
                KualiDecimal awardTotalAmount = award.getAwardTotalAmount() == null ? KualiDecimal.ZERO : award.getAwardTotalAmount();
                contractsGrantsInvoiceDocumentErrorLog.setAwardTotalAmount(awardTotalAmount.bigDecimalValue());

                if (ObjectUtils.isNotNull(award.getAwardPrimaryFundManager())) {
                    contractsGrantsInvoiceDocumentErrorLog.setPrimaryFundManagerPrincipalId(
                            award.getAwardPrimaryFundManager().getPrincipalId());
                }
                if (!CollectionUtils.isEmpty(award.getActiveAwardAccounts())) {
                    boolean firstLineFlag = true;

                    for (final ContractsAndGrantsBillingAwardAccount awardAccount : award.getActiveAwardAccounts()) {

                        cumulativeExpenses = cumulativeExpenses.add(contractsGrantsInvoiceDocumentService
                                .getBudgetAndActualsForAwardAccount(awardAccount,
                                        systemOptions.getActualFinancialBalanceTypeCd()));
                        if (firstLineFlag) {
                            firstLineFlag = false;
                            contractsGrantsInvoiceDocumentErrorLog.setAccounts(awardAccount.getAccountNumber());
                        } else {
                            contractsGrantsInvoiceDocumentErrorLog.setAccounts(
                                    contractsGrantsInvoiceDocumentErrorLog.getAccounts() + ";" +
                                            awardAccount.getAccountNumber());
                        }
                    }
                }
                contractsGrantsInvoiceDocumentErrorLog.setCumulativeExpensesAmount(
                        cumulativeExpenses.bigDecimalValue());
            }

            for (final String vCat : invalidGroup.get(award)) {
                final ContractsGrantsInvoiceDocumentErrorMessage contractsGrantsInvoiceDocumentErrorCategory =
                        new ContractsGrantsInvoiceDocumentErrorMessage();
                contractsGrantsInvoiceDocumentErrorCategory.setErrorMessageText(vCat);
                contractsGrantsInvoiceDocumentErrorLog.getErrorMessages()
                        .add(contractsGrantsInvoiceDocumentErrorCategory);
            }

            final int errorAccountsMax = dataDictionaryService.getAttributeMaxLength(
                    ContractsGrantsInvoiceDocumentErrorLog.class,
                    ContractsGrantsInvoiceDocumentErrorLogLookupFields.ACCOUNTS);
            contractsGrantsInvoiceDocumentErrorLog.setAccounts(
                    StringUtils.left(contractsGrantsInvoiceDocumentErrorLog.getAccounts(), errorAccountsMax));

            contractsGrantsInvoiceDocumentErrorLog.setErrorDate(dateTimeService.getCurrentTimestamp());
            contractsGrantsInvoiceDocumentErrorLog.setCreationProcessTypeCode(creationProcessTypeCode);
            businessObjectService.save(contractsGrantsInvoiceDocumentErrorLog);
            contractsGrantsInvoiceDocumentErrorLogs.add(contractsGrantsInvoiceDocumentErrorLog);
        }
    }

    protected void storeCreationErrors(final List<ErrorMessage> errorMessages, final String creationProcessTypeCode) {
        for (final ErrorMessage errorMessage : errorMessages) {
            final ContractsGrantsInvoiceDocumentErrorLog contractsGrantsInvoiceDocumentErrorLog =
                    new ContractsGrantsInvoiceDocumentErrorLog();

            final ContractsGrantsInvoiceDocumentErrorMessage contractsGrantsInvoiceDocumentErrorCategory =
                    new ContractsGrantsInvoiceDocumentErrorMessage();
            contractsGrantsInvoiceDocumentErrorCategory.setErrorMessageText(MessageFormat.format(
                    configurationService.getPropertyValueAsString(errorMessage.getErrorKey()),
                    errorMessage.getMessageParameters()));
            contractsGrantsInvoiceDocumentErrorLog.getErrorMessages().add(contractsGrantsInvoiceDocumentErrorCategory);

            contractsGrantsInvoiceDocumentErrorLog.setErrorDate(dateTimeService.getCurrentTimestamp());
            contractsGrantsInvoiceDocumentErrorLog.setCreationProcessTypeCode(creationProcessTypeCode);
            businessObjectService.save(contractsGrantsInvoiceDocumentErrorLog);
        }
    }

    /**
     * This method retrieves all the Contracts & Grants Invoice Documents with a status of Saved and routes them to
     * the next step in the routing path.
     *
     * @return True if the routing was performed successfully. A runtime exception will be thrown if any errors occur
     *         while routing.
     */
    @Override
    public void routeContractsGrantsInvoiceDocuments() {
        final String currentUserPrincipalId = GlobalVariables.getUserSession().getPerson().getPrincipalId();
        final List<String> documentIdList = retrieveContractsGrantsInvoiceDocumentsToRoute(DocumentStatus.SAVED,
                currentUserPrincipalId);

        LOG.info("CGinvoice to Route: {}", documentIdList);

        for (final String cgInvoiceDocId : documentIdList) {
            final ContractsGrantsInvoiceDocument cgInvoiceDoc =
                    (ContractsGrantsInvoiceDocument) documentService.getByDocumentHeaderId(cgInvoiceDocId);
            // To route documents only if the user in the session is same as the initiator.
            LOG.info("Routing Contracts & Grants Invoice document # {}.", cgInvoiceDocId);
            documentService.prepareWorkflowDocument(cgInvoiceDoc);

            // calling workflow service to bypass business rule checks
            workflowDocumentService.route(cgInvoiceDoc.getDocumentHeader().getWorkflowDocument(), "", null);
        }
    }

    /**
     * Returns a list of all saved but not yet routed Contracts & Grants Invoice Documents, using the
     * KualiWorkflowInfo service.
     *
     * @return a list of Contracts & Grants Invoice Documents to route
     */
    protected List<String> retrieveContractsGrantsInvoiceDocumentsToRoute(
            final DocumentStatus statusCode,
            final String initiatorPrincipalId) {
        final List<String> documentIds = new ArrayList<>();

        final Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put(KFSPropertyConstants.WORKFLOW_DOCUMENT_TYPE_NAME,
                ArConstants.ArDocumentTypeCodes.CONTRACTS_GRANTS_INVOICE);
        fieldValues.put(KFSPropertyConstants.WORKFLOW_DOCUMENT_STATUS_CODE, statusCode.getCode());
        fieldValues.put(KFSPropertyConstants.INITIATOR_PRINCIPAL_ID, initiatorPrincipalId);
        final Collection<DocumentHeader> docHeaders = businessObjectService.findMatching(
                DocumentHeader.class, fieldValues);

        for (final DocumentHeader docHeader : docHeaders) {
            documentIds.add(docHeader.getDocumentNumber());
        }
        return documentIds;
    }

    protected void writeErrorEntryByAward(
            final ContractsAndGrantsBillingAward award, final List<String> validationCategory,
            final PrintStream printStream) throws IOException {
        // %15s %18s %20s %19s %15s %18s %23s %18s
        if (ObjectUtils.isNotNull(award)) {
            KualiDecimal cumulativeExpenses = KualiDecimal.ZERO;
            final String awardBeginningDate;
            final String awardEndingDate;
            final String awardTotalAmount;

            final String proposalNumber = award.getProposalNumber();
            final Date beginningDate = award.getAwardBeginningDate();
            final Date endingDate = award.getAwardEndingDate();
            final KualiDecimal totalAmount = award.getAwardTotalAmount();

            if (ObjectUtils.isNotNull(beginningDate)) {
                awardBeginningDate = beginningDate.toString();
            } else {
                awardBeginningDate = "null award beginning date";
            }

            if (ObjectUtils.isNotNull(endingDate)) {
                awardEndingDate = endingDate.toString();
            } else {
                awardEndingDate = "null award ending date";
            }

            if (ObjectUtils.isNotNull(totalAmount) && ObjectUtils.isNotNull(totalAmount.bigDecimalValue())) {
                awardTotalAmount = totalAmount.toString();
            } else {
                awardTotalAmount = "null award total amount";
            }

            if (CollectionUtils.isEmpty(award.getActiveAwardAccounts())) {
                writeToReport(proposalNumber, "", awardBeginningDate, awardEndingDate, awardTotalAmount,
                        cumulativeExpenses.toString(), printStream);
            } else {
                final SystemOptions systemOptions = optionsService.getCurrentYearOptions();

                // calculate cumulativeExpenses
                for (final ContractsAndGrantsBillingAwardAccount awardAccount : award.getActiveAwardAccounts()) {
                    cumulativeExpenses = cumulativeExpenses.add(contractsGrantsInvoiceDocumentService
                            .getBudgetAndActualsForAwardAccount(awardAccount,
                                    systemOptions.getActualFinancialBalanceTypeCd()));
                }
                boolean firstLineFlag = true;

                for (final ContractsAndGrantsBillingAwardAccount awardAccount : award.getActiveAwardAccounts()) {
                    if (firstLineFlag) {
                        writeToReport(proposalNumber, awardAccount.getAccountNumber(), awardBeginningDate,
                                awardEndingDate, awardTotalAmount, cumulativeExpenses.toString(), printStream);
                        firstLineFlag = false;
                    } else {
                        writeToReport("", awardAccount.getAccountNumber(), "", "", "", "", printStream);
                    }
                }
            }
        }
        // To print all the errors from the validation category.
        for (final String vCat : validationCategory) {
            printStream.printf("%s", "     " + vCat);
            printStream.print("\r\n");
        }
        printStream.print(REPORT_LINE_DIVIDER);
        printStream.print("\r\n");
    }

    protected void writeToReport(
            final String proposalNumber, final String accountNumber, final String awardBeginningDate,
            final String awardEndingDate, final String awardTotalAmount, final String cumulativeExpenses, final PrintStream printStream)
            throws IOException {
        printStream.printf("%15s", proposalNumber);
        printStream.printf("%18s", accountNumber);
        printStream.printf("%20s", awardBeginningDate);
        printStream.printf("%19s", awardEndingDate);
        printStream.printf("%15s", awardTotalAmount);
        printStream.printf("%23s", cumulativeExpenses);
        printStream.print("\r\n");
    }

    /**
     * @param printStream
     * @throws IOException
     */
    protected void writeReportHeader(final PrintStream printStream) throws IOException {
        printStream.printf("%15s%18s%20s%19s%15s%23s\r\n", "Proposal Number", "Account Number", "Award Start Date",
                "Award Stop Date", "Award Total", "Cumulative Expenses");
        printStream.printf("%23s", "Validation Category");
        printStream.print("\r\n");
        printStream.print(REPORT_LINE_DIVIDER);
        printStream.print("\r\n");
    }

    /**
     * @param award award to check for billable award accounts
     * @param creationProcessType invoice document creation process type
     * @return true award has billable award accounts
     */
    protected boolean hasBillableAccounts(final ContractsAndGrantsBillingAward award, final ContractsAndGrantsInvoiceDocumentCreationProcessType creationProcessType) {
        if (ArConstants.BillingFrequencyValues.isMilestone(award)
                || ArConstants.BillingFrequencyValues.isPredeterminedBilling(award)) {
            return !getContractsGrantsBillingAwardVerificationService().isInvoiceInProgress(award);
        } else {
            return CollectionUtils.isEmpty(award.getActiveAwardAccounts())
                    || !CollectionUtils.isEmpty(getValidAwardAccounts(award.getActiveAwardAccounts(), award, creationProcessType));
        }
    }

    /*
     * CU Customization: KFSPTS-13256
     * Added evaluation of account sub-fund not being an expenditure to existing base code evaluation
     * of billing frequency and invoice document status when determining valid award accounts.
     * Expenditure sub-funds associated to accounts to exclude are defined in CG system parameter
     * CG_INVOICING_EXCLUDE_EXPENSES_SUB_FUNDS.
     *
     *
     * CU Customization KFSPTS-23690
     * Set transient creationProcessType attribute on Award so value is available in called method.
     */
    /**
     * @param awardAccounts
     * @param creationProcessType invoice document creation process type
     * @return the valid award accounts based on evaluation of billing frequency and invoice document status
     */
    protected List<ContractsAndGrantsBillingAwardAccount> getValidAwardAccounts(
            final List<ContractsAndGrantsBillingAwardAccount> awardAccounts, final ContractsAndGrantsBillingAward award,
            final ContractsAndGrantsInvoiceDocumentCreationProcessType creationProcessType) {
        if (!ArConstants.BillingFrequencyValues.isMilestone(award)
                && !ArConstants.BillingFrequencyValues.isPredeterminedBilling(award)) {
            final List<ContractsAndGrantsBillingAwardAccount> validAwardAccounts = new ArrayList<>();
            final Set<Account> invalidAccounts = harvestAccountsFromContractsGrantsInvoices(
                    getInProgressInvoicesForAward(award));

            for (final ContractsAndGrantsBillingAwardAccount awardAccount : awardAccounts) {
                if (!invalidAccounts.contains(awardAccount.getAccount())) {
                    boolean checkGracePeriod = ContractsAndGrantsInvoiceDocumentCreationProcessType.MANUAL != creationProcessType;
                    /* CU Customizations: KFSPTS-13256, KFSPTS-23690 */
                    award.setCreationProcessType(creationProcessType);
                    if (verifyBillingFrequencyService.validateBillingFrequency(award, awardAccount, checkGracePeriod)
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
    
    /*
     * CU Customization: KFSPTS-13256
     */
    protected boolean isNotExpenditureAccount(ContractsAndGrantsBillingAwardAccount billingAwardAccount) {
        Account accountLinkedToAward = getAccountService().getByPrimaryId(billingAwardAccount.getChartOfAccountsCode(), billingAwardAccount.getAccountNumber());
        if (ObjectUtils.isNotNull(accountLinkedToAward)) {
            return !isExpenditureSubFund(accountLinkedToAward.getSubFundGroupCode());
        }
        return true;
    }
    
    /*
     * CU Customization: KFSPTS-13256
     */
    protected boolean isExpenditureSubFund(String subFundGroupCode) {
        if (StringUtils.isNotBlank(subFundGroupCode)) {
            Collection<String> acceptedValuesForExpenditureSubFundCodes = parameterService.getParameterValuesAsString(KFSConstants.OptionalModuleNamespaces.ACCOUNTS_RECEIVABLE,
                    CUKFSParameterKeyConstants.ALL_COMPONENTS, CuArParameterKeyConstants.CG_INVOICING_EXCLUDE_EXPENSES_SUB_FUNDS);
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(acceptedValuesForExpenditureSubFundCodes)) {
                return acceptedValuesForExpenditureSubFundCodes.stream().anyMatch(subFundGroupCode::equalsIgnoreCase);
            }
        }
        return false;
    }

    /**
     * Pulls all the unique accounts from the source accounting lines on the given ContractsGrantsInvoiceDocument
     *
     * @param contractsGrantsInvoices the invoices to pull unique accounts from
     * @return a Set of the unique accounts
     */
    protected Set<Account> harvestAccountsFromContractsGrantsInvoices(
            final Collection<ContractsGrantsInvoiceDocument> contractsGrantsInvoices) {
        final Set<Account> accounts = new HashSet<>();
        for (final ContractsGrantsInvoiceDocument invoice : contractsGrantsInvoices) {
            for (final InvoiceAccountDetail invoiceAccountDetail : invoice.getAccountDetails()) {
                final Account account = getAccountService().getByPrimaryId(
                        invoiceAccountDetail.getChartOfAccountsCode(), invoiceAccountDetail.getAccountNumber());
                if (ObjectUtils.isNotNull(account)) {
                    accounts.add(account);
                }
            }
        }
        return accounts;
    }

    /**
     * Looks up all the in progress contracts & grants invoices for the award
     *
     * @param award the award to look up contracts & grants invoices for
     * @return a Collection matching in progress/pending Contracts & Grants Invoice documents
     */
    protected Collection<ContractsGrantsInvoiceDocument> getInProgressInvoicesForAward(
            final ContractsAndGrantsBillingAward award) {
        final Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put(ArPropertyConstants.ContractsGrantsInvoiceDocumentFields.PROPOSAL_NUMBER,
                award.getProposalNumber());
        fieldValues.put(KFSPropertyConstants.DOCUMENT_HEADER + "." +
                KFSPropertyConstants.WORKFLOW_DOCUMENT_STATUS_CODE,
                financialSystemDocumentService.getPendingDocumentStatuses());

        return businessObjectService.findMatching(ContractsGrantsInvoiceDocument.class, fieldValues);
    }

    /**
     * Retrieve expense object types by the basic accounting category for expenses
     */
    @Override
    public Collection<String> retrieveExpenseObjectTypes() {
        final List<String> objectTypeCodes = new ArrayList<>();

        final Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put(KFSPropertyConstants.BASIC_ACCOUNTING_CATEGORY_CODE,
                KFSConstants.BasicAccountingCategoryCodes.EXPENSES);

        final Collection<ObjectType> objectTypes = getBusinessObjectService().findMatching(ObjectType.class,
                fieldValues);
        for (final ObjectType objectType : objectTypes) {
            objectTypeCodes.add(objectType.getCode());
        }

        return objectTypeCodes;
    }

    public AccountService getAccountService() {
        return accountService;
    }

    public void setAccountService(final AccountService accountService) {
        this.accountService = accountService;
    }

    public void setAccountingPeriodService(final AccountingPeriodService accountingPeriodService) {
        this.accountingPeriodService = accountingPeriodService;
    }

    public void setVerifyBillingFrequencyService(final VerifyBillingFrequencyService verifyBillingFrequencyService) {
        this.verifyBillingFrequencyService = verifyBillingFrequencyService;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setWorkflowDocumentService(final WorkflowDocumentService workflowDocumentService) {
        this.workflowDocumentService = workflowDocumentService;
    }

    public void setDataDictionaryService(final DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public void setDocumentService(final DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setAccountsReceivableDocumentHeaderService(
            final AccountsReceivableDocumentHeaderService accountsReceivableDocumentHeaderService) {
        this.accountsReceivableDocumentHeaderService = accountsReceivableDocumentHeaderService;
    }

    public void setContractsGrantsInvoiceDocumentService(
            final ContractsGrantsInvoiceDocumentService contractsGrantsInvoiceDocumentService) {
        this.contractsGrantsInvoiceDocumentService = contractsGrantsInvoiceDocumentService;
    }

    public ContractsGrantsInvoiceDocumentService getContractsGrantsInvoiceDocumentService() {
        return contractsGrantsInvoiceDocumentService;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(final DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setKualiModuleService(final KualiModuleService kualiModuleService) {
        this.kualiModuleService = kualiModuleService;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(final ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public ContractsGrantsBillingUtilityService getContractsGrantsBillingUtilityService() {
        return contractsGrantsBillingUtilityService;
    }

    public void setContractsGrantsBillingUtilityService(
            final ContractsGrantsBillingUtilityService contractsGrantsBillingUtilityService) {
        this.contractsGrantsBillingUtilityService = contractsGrantsBillingUtilityService;
    }

    public ContractsAndGrantsModuleBillingService getContractsAndGrantsModuleBillingService() {
        return contractsAndGrantsModuleBillingService;
    }

    public void setContractsAndGrantsModuleBillingService(
            final ContractsAndGrantsModuleBillingService contractsAndGrantsModuleBillingService) {
        this.contractsAndGrantsModuleBillingService = contractsAndGrantsModuleBillingService;
    }

    public FinancialSystemDocumentService getFinancialSystemDocumentService() {
        return financialSystemDocumentService;
    }

    public void setFinancialSystemDocumentService(final FinancialSystemDocumentService financialSystemDocumentService) {
        this.financialSystemDocumentService = financialSystemDocumentService;
    }

    public UniversityDateService getUniversityDateService() {
        return universityDateService;
    }

    public void setUniversityDateService(final UniversityDateService universityDateService) {
        this.universityDateService = universityDateService;
    }

    public AwardAccountObjectCodeTotalBilledDao getAwardAccountObjectCodeTotalBilledDao() {
        return awardAccountObjectCodeTotalBilledDao;
    }

    public void setAwardAccountObjectCodeTotalBilledDao(
            final AwardAccountObjectCodeTotalBilledDao awardAccountObjectCodeTotalBilledDao) {
        this.awardAccountObjectCodeTotalBilledDao = awardAccountObjectCodeTotalBilledDao;
    }

    public CustomerService getCustomerService() {
        return customerService;
    }

    public void setCustomerService(final CustomerService customerService) {
        this.customerService = customerService;
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(final ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public ContractsGrantsBillingAwardVerificationService getContractsGrantsBillingAwardVerificationService() {
        return contractsGrantsBillingAwardVerificationService;
    }

    public void setContractsGrantsBillingAwardVerificationService(
            final ContractsGrantsBillingAwardVerificationService contractsGrantsBillingAwardVerificationService) {
        this.contractsGrantsBillingAwardVerificationService = contractsGrantsBillingAwardVerificationService;
    }

    public CostCategoryService getCostCategoryService() {
        return costCategoryService;
    }

    public void setCostCategoryService(final CostCategoryService costCategoryService) {
        this.costCategoryService = costCategoryService;
    }

    public OptionsService getOptionsService() {
        return optionsService;
    }

    public void setOptionsService(final OptionsService optionsService) {
        this.optionsService = optionsService;
    }

    public void setFinancialSystemUserService(final FinancialSystemUserService financialSystemUserService) {
        this.financialSystemUserService = financialSystemUserService;
    }

    public void setCustomerAddressService(final CustomerAddressService customerAddressService) {
        this.customerAddressService = customerAddressService;
    }

    /*
     * CU Customization: KFSPTS-26393
     */
    public CuCustomerAddressHelperService getCuCustomerAddressHelperService() {
        return cuCustomerAddressHelperService;
    }

    /*
     * CU Customization: KFSPTS-26393
     */
    public void setCuCustomerAddressHelperService(CuCustomerAddressHelperService cuCustomerAddressHelperService) {
        this.cuCustomerAddressHelperService = cuCustomerAddressHelperService;
    }
}
