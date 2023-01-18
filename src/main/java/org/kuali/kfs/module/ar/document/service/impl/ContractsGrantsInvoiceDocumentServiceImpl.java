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
package org.kuali.kfs.module.ar.document.service.impl;

import com.lowagie.text.DocumentException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.businessobject.ObjectCodeCurrent;
import org.kuali.kfs.coa.businessobject.SubFundGroup;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.coa.service.ObjectTypeService;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.search.SearchOperator;
import org.kuali.kfs.core.api.util.type.AbstractKualiDecimal;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.gl.businessobject.Balance;
import org.kuali.kfs.integration.cg.ContractAndGrantsProposal;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAward;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAwardAccount;
import org.kuali.kfs.integration.cg.ContractsAndGrantsModuleBillingService;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kew.api.document.DocumentStatus;
import org.kuali.kfs.kim.api.identity.IdentityService;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.api.permission.PermissionService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.impl.identity.principal.Principal;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.bo.ModuleConfiguration;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.service.AttachmentService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.KualiModuleService;
import org.kuali.kfs.krad.service.KualiRuleService;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArAuthorizationConstants;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.ArKeyConstants;
import org.kuali.kfs.module.ar.ArParameterConstants;
import org.kuali.kfs.module.ar.ArPropertyConstants;
import org.kuali.kfs.module.ar.batch.ContractsGrantsInvoiceDocumentBatchStep;
import org.kuali.kfs.module.ar.businessobject.AwardAccountObjectCodeTotalBilled;
import org.kuali.kfs.module.ar.businessobject.Bill;
import org.kuali.kfs.module.ar.businessobject.ContractsGrantsInvoiceDetail;
import org.kuali.kfs.module.ar.businessobject.ContractsGrantsInvoiceObjectCode;
import org.kuali.kfs.module.ar.businessobject.CostCategory;
import org.kuali.kfs.module.ar.businessobject.CostCategoryObjectCode;
import org.kuali.kfs.module.ar.businessobject.CostCategoryObjectConsolidation;
import org.kuali.kfs.module.ar.businessobject.CostCategoryObjectLevel;
import org.kuali.kfs.module.ar.businessobject.Customer;
import org.kuali.kfs.module.ar.businessobject.CustomerAddress;
import org.kuali.kfs.module.ar.businessobject.CustomerInvoiceDetail;
import org.kuali.kfs.module.ar.businessobject.InvoiceAccountDetail;
import org.kuali.kfs.module.ar.businessobject.InvoiceAddressDetail;
import org.kuali.kfs.module.ar.businessobject.InvoiceBill;
import org.kuali.kfs.module.ar.businessobject.InvoiceDetailAccountObjectCode;
import org.kuali.kfs.module.ar.businessobject.InvoiceGeneralDetail;
import org.kuali.kfs.module.ar.businessobject.InvoiceMilestone;
import org.kuali.kfs.module.ar.businessobject.InvoiceSuspensionCategory;
import org.kuali.kfs.module.ar.businessobject.InvoiceTemplate;
import org.kuali.kfs.module.ar.businessobject.Milestone;
import org.kuali.kfs.module.ar.businessobject.OrganizationAccountingDefault;
import org.kuali.kfs.module.ar.businessobject.OrganizationOptions;
import org.kuali.kfs.module.ar.businessobject.SystemInformation;
import org.kuali.kfs.module.ar.businessobject.TransmissionDetailStatus;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.document.dataaccess.ContractsGrantsInvoiceDocumentDao;
import org.kuali.kfs.module.ar.document.service.AccountsReceivablePendingEntryService;
import org.kuali.kfs.module.ar.document.service.ContractsGrantsInvoiceDocumentService;
import org.kuali.kfs.module.ar.document.service.CustomerInvoiceDocumentService;
import org.kuali.kfs.module.ar.document.validation.SuspensionCategory;
import org.kuali.kfs.module.ar.identity.ArKimAttributes;
import org.kuali.kfs.module.ar.service.ContractsGrantsBillingUtilityService;
import org.kuali.kfs.module.ar.service.CostCategoryService;
import org.kuali.kfs.sys.FinancialSystemModuleConfiguration;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.PdfFormFillerUtil;
import org.kuali.kfs.sys.batch.Job;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.sys.document.service.FinancialSystemDocumentService;
import org.kuali.kfs.sys.document.validation.event.AttributedRouteDocumentEvent;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Transactional
//CU customization: backport FINP-8642; this file can be removed when we upgrade to the 07/21/2022 version of financials
public class ContractsGrantsInvoiceDocumentServiceImpl implements ContractsGrantsInvoiceDocumentService {

    private static final Logger LOG = LogManager.getLogger();

    protected AccountsReceivablePendingEntryService accountsReceivablePendingEntryService;
    protected AttachmentService attachmentService;
    protected BusinessObjectService businessObjectService;
    protected ConfigurationService configurationService;
    protected ContractsGrantsBillingUtilityService contractsGrantsBillingUtilityService;
    protected ContractsGrantsInvoiceDocumentDao contractsGrantsInvoiceDocumentDao;
    protected ContractsAndGrantsModuleBillingService contractsAndGrantsModuleBillingService;
    protected CostCategoryService costCategoryService;
    protected CustomerInvoiceDocumentService customerInvoiceDocumentService;
    protected DateTimeService dateTimeService;
    protected DocumentService documentService;
    protected FinancialSystemDocumentService financialSystemDocumentService;
    protected IdentityService identityService;
    protected KualiModuleService kualiModuleService;
    protected KualiRuleService kualiRuleService;
    protected NoteService noteService;
    protected ObjectCodeService objectCodeService;
    protected ParameterService parameterService;
    protected PermissionService permissionService;
    protected PersonService personService;
    protected UniversityDateService universityDateService;
    private AccountingPeriodService accountingPeriodService;
    private ObjectTypeService objectTypeService;

    private List<SuspensionCategory> suspensionCategories;

    @Override
    public void createSourceAccountingLines(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument,
            List<ContractsAndGrantsBillingAwardAccount> awardAccounts) {
        // To check if the Source accounting lines are existing. If they are do nothing
        if (CollectionUtils.isEmpty(contractsGrantsInvoiceDocument.getSourceAccountingLines())) {
            ContractsAndGrantsBillingAward award = contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getAward();

            if (ObjectUtils.isNotNull(award)) {
                if (StringUtils.equalsIgnoreCase(award.getInvoicingOptionCode(), ArConstants.INV_ACCOUNT)
                        || StringUtils.equalsIgnoreCase(award.getInvoicingOptionCode(), ArConstants.INV_SCHEDULE)) {
                    // If its bill by Account or Schedule, irrespective of it is by contract control account,
                    // there would be a single source accounting line with award account specified by the user.
                    CustomerInvoiceDetail cide = createSourceAccountingLine(
                            contractsGrantsInvoiceDocument.getDocumentNumber(),
                            awardAccounts.get(0).getChartOfAccountsCode(), awardAccounts.get(0).getAccountNumber(),
                            awardAccounts.get(0).getAccount().getSubFundGroup(),
                            getTotalAmountForInvoice(contractsGrantsInvoiceDocument), 1);
                    contractsGrantsInvoiceDocument.getSourceAccountingLines().add(cide);
                } else if (StringUtils.equalsIgnoreCase(award.getInvoicingOptionCode(),
                        ArConstants.INV_CONTRACT_CONTROL_ACCOUNT)) {
                    // by control account
                    // If its bill by Contract Control Account there would be a single source accounting line.
                    CustomerInvoiceDetail cide = createSourceAccountingLinesByContractControlAccount(
                            contractsGrantsInvoiceDocument);
                    contractsGrantsInvoiceDocument.getSourceAccountingLines().add(cide);
                } else {
                    // by award
                    List<CustomerInvoiceDetail> awardAccountingLines = createSourceAccountingLinesByAward(
                            contractsGrantsInvoiceDocument);
                    contractsGrantsInvoiceDocument.getSourceAccountingLines().addAll(awardAccountingLines);
                }
            }
        }
    }

    @Override
    public ContractsGrantsInvoiceObjectCode contractGrantsInvoiceObjectCodeForSubFundGroup(SubFundGroup subFundGroup,
            String chartOfAccountsCode) {
        ContractsGrantsInvoiceObjectCode contractsGrantsInvoiceObjectCode =
                findContractGrantsInvoiceObjectCode(
                        ArPropertyConstants.ContractsGrantsInvoiceObjectCodeFields.SUB_FUND_GROUP_CODE,
                        subFundGroup.getSubFundGroupCode(), chartOfAccountsCode);

        if (contractsGrantsInvoiceObjectCode == null) {
            contractsGrantsInvoiceObjectCode =
                    findContractGrantsInvoiceObjectCode(
                            ArPropertyConstants.ContractsGrantsInvoiceObjectCodeFields.SUB_FUND_GROUP_TYPE_CODE,
                            subFundGroup.getSubFundGroupTypeCode(), chartOfAccountsCode);
        }

        if (contractsGrantsInvoiceObjectCode == null) {
            contractsGrantsInvoiceObjectCode =
                    findContractGrantsInvoiceObjectCode(
                            ArPropertyConstants.ContractsGrantsInvoiceObjectCodeFields.FUND_GROUP_CODE,
                            subFundGroup.getFundGroupCode(), chartOfAccountsCode);
        }

        return contractsGrantsInvoiceObjectCode;
    }

    private ContractsGrantsInvoiceObjectCode findContractGrantsInvoiceObjectCode(String fieldName, String fieldValue,
            Object chartOfAccountsCode) {
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put(fieldName, fieldValue);
        fieldValues.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartOfAccountsCode);
        fieldValues.put(ArPropertyConstants.ContractsGrantsInvoiceObjectCodeFields.ACTIVE, true);
        List<ContractsGrantsInvoiceObjectCode> contractsGrantsInvoiceObjectCodes =
                (List<ContractsGrantsInvoiceObjectCode>) getBusinessObjectService()
                        .findMatching(ContractsGrantsInvoiceObjectCode.class, fieldValues);
        if (CollectionUtils.isNotEmpty(contractsGrantsInvoiceObjectCodes)) {
            return contractsGrantsInvoiceObjectCodes.get(0);
        }

        return null;
    }

    @Override
    public KualiDecimal getTotalAmountForInvoice(ContractsGrantsInvoiceDocument invoice) {
        if (ArConstants.BillingFrequencyValues.isPredeterminedBilling(invoice.getInvoiceGeneralDetail())) {
            return getBillAmountTotal(invoice);
        } else if (ArConstants.BillingFrequencyValues.isMilestone(invoice.getInvoiceGeneralDetail())) {
            return getInvoiceMilestoneTotal(invoice);
        } else {
            calculatePreviouslyBilledAmounts(invoice);
            return invoice.getTotalInvoiceInvoiceAmount();
        }
    }

    /**
     * Generates the source accounting lines for a Contracts & Grants Invoice from the award accounts associated with
     * an award (in the form of the pre-generated invoice account details)
     *
     * @param contractsGrantsInvoiceDocument the Contracts & Grants Invoice to create invoice details for
     * @return a List of generated accounting lines
     */
    protected List<CustomerInvoiceDetail> createSourceAccountingLinesByAward(
            ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument) {
        List<CustomerInvoiceDetail> awardAccountingLines = new ArrayList<>();
        if (!CollectionUtils.isEmpty(contractsGrantsInvoiceDocument.getAccountDetails())) {
            final Map<String, KualiDecimal> accountExpenditureAmounts = getCategoryExpenditureAmountsForInvoiceAccountDetail(
                    contractsGrantsInvoiceDocument);
            final Map<String, KualiDecimal> accountTotalBilledAmounts = getCategoryTotalBilledAmountsForInvoiceAccountDetail(
                    contractsGrantsInvoiceDocument);
            for (InvoiceAccountDetail invAcctD : contractsGrantsInvoiceDocument.getAccountDetails()) {
                final String proposalNumber = invAcctD.getProposalNumber();
                final String chartOfAccountsCode = invAcctD.getChartOfAccountsCode();
                final String accountNumber = invAcctD.getAccountNumber();
                if (invAcctD.getAccount() == null) {
                    invAcctD.refreshReferenceObject(KFSPropertyConstants.ACCOUNT);
                }
                final SubFundGroup subFundGroup = invAcctD.getAccount().getSubFundGroup();
                final Integer sequenceNumber = contractsGrantsInvoiceDocument.getAccountDetails().indexOf(invAcctD) + 1;

                final String accountKey = StringUtils.join(new String[]{chartOfAccountsCode, accountNumber}, "-");
                KualiDecimal totalAmount = accountExpenditureAmounts.getOrDefault(accountKey, KualiDecimal.ZERO);

                if (invAcctD.getTotalPreviouslyBilled().isZero()) {
                    KualiDecimal previouslyBilledAmount = getPredeterminedBillingBilledToDateAmount(proposalNumber,
                            chartOfAccountsCode, accountNumber);
                    previouslyBilledAmount = previouslyBilledAmount.add(
                            getMilestonesBilledToDateAmount(proposalNumber, chartOfAccountsCode, accountNumber));

                    KualiDecimal totalBilledAmount = accountTotalBilledAmounts.getOrDefault(accountKey,
                            KualiDecimal.ZERO);

                    previouslyBilledAmount = previouslyBilledAmount.subtract(totalBilledAmount);
                    if (previouslyBilledAmount.isGreaterThan(KualiDecimal.ZERO)) {
                        totalAmount = totalAmount.subtract(previouslyBilledAmount);
                    }
                }

                CustomerInvoiceDetail cide = createSourceAccountingLine(
                        contractsGrantsInvoiceDocument.getDocumentNumber(), chartOfAccountsCode, accountNumber,
                        subFundGroup, totalAmount, sequenceNumber);
                awardAccountingLines.add(cide);
            }
        }
        return awardAccountingLines;
    }

    /**
     * Totals the current expenditure amounts of any invoice detail account object codes on the document which have set
     * categories by account numbers
     *
     * @param contractsGrantsInvoiceDocument the document holding invoice detail account object codes
     * @return a Map where the key is the concatenation of chartOfAccountsCode-accountNumber and the value is the
     * expenditure amount on that account
     */
    protected Map<String, KualiDecimal> getCategoryExpenditureAmountsForInvoiceAccountDetail(
            ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument) {
        Map<String, KualiDecimal> expenditureAmounts = new HashMap<>();
        for (InvoiceDetailAccountObjectCode invoiceDetailAccountObjectCode : contractsGrantsInvoiceDocument
                .getInvoiceDetailAccountObjectCodes()) {
            final String accountKey = StringUtils.join(
                    new String[]{invoiceDetailAccountObjectCode.getChartOfAccountsCode(),
                            invoiceDetailAccountObjectCode.getAccountNumber()}, "-");
            if (StringUtils.isNotBlank(invoiceDetailAccountObjectCode.getCategoryCode())) {
                KualiDecimal total = expenditureAmounts.get(accountKey);
                if (ObjectUtils.isNull(total)) {
                    total = KualiDecimal.ZERO;
                }
                expenditureAmounts.put(accountKey, total.add(invoiceDetailAccountObjectCode.getCurrentExpenditures()));
            }
        }
        return expenditureAmounts;
    }

    /**
     * Totals the total billed amounts of any invoice detail account object codes on the document which have set
     * categories by account numbers
     *
     * @param contractsGrantsInvoiceDocument the document holding invoice detail account object codes
     * @return a Map where the key is the concatenation of chartOfAccountsCode-accountNumber and the value is the
     * total billed amount on that account
     */
    protected Map<String, KualiDecimal> getCategoryTotalBilledAmountsForInvoiceAccountDetail(
            ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument) {
        Map<String, KualiDecimal> totalBilledAmounts = new HashMap<>();
        for (InvoiceDetailAccountObjectCode invoiceDetailAccountObjectCode : contractsGrantsInvoiceDocument
                .getInvoiceDetailAccountObjectCodes()) {
            final String accountKey = StringUtils.join(
                    new String[]{invoiceDetailAccountObjectCode.getChartOfAccountsCode(),
                            invoiceDetailAccountObjectCode.getAccountNumber()}, "-");
            if (StringUtils.isNotBlank(invoiceDetailAccountObjectCode.getCategoryCode())) {
                KualiDecimal total = totalBilledAmounts.get(accountKey);
                if (ObjectUtils.isNull(total)) {
                    total = KualiDecimal.ZERO;
                }
                totalBilledAmounts.put(accountKey, total.add(invoiceDetailAccountObjectCode.getTotalBilled()));
            }
        }
        return totalBilledAmounts;
    }

    /**
     * Creates source accounting lines using the contract control account to populate
     *
     * @param contractsGrantsInvoiceDocument the Contracts & Grants Invoice to add lines to
     */
    protected CustomerInvoiceDetail createSourceAccountingLinesByContractControlAccount(
            ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument) {
        String coaCode = null;
        String accountNumber = null;
        SubFundGroup subFundGroup = null;

        List<InvoiceAccountDetail> accountDetails = contractsGrantsInvoiceDocument.getAccountDetails();
        if (CollectionUtils.isNotEmpty(accountDetails)) {
            InvoiceAccountDetail invoiceAccountDetail = accountDetails.get(0);
            Account account = invoiceAccountDetail.getAccount();
            if (ObjectUtils.isNull(account)) {
                invoiceAccountDetail.refreshReferenceObject(KFSPropertyConstants.ACCOUNT);
                account = invoiceAccountDetail.getAccount();
            }
            if (ObjectUtils.isNotNull(account)) {
                Account contractControlAccount = account.getContractControlAccount();
                if (ObjectUtils.isNotNull(contractControlAccount)) {
                    coaCode = contractControlAccount.getChartOfAccountsCode();
                    accountNumber = contractControlAccount.getAccountNumber();
                    subFundGroup = contractControlAccount.getSubFundGroup();
                }
            }
        }

        return createSourceAccountingLine(contractsGrantsInvoiceDocument.getDocumentNumber(),
                coaCode, accountNumber, subFundGroup, getTotalAmountForInvoice(contractsGrantsInvoiceDocument), 1);
    }

    @Override
    public KualiDecimal getBillAmountTotal(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument) {
        KualiDecimal totalBillAmount = KualiDecimal.ZERO;
        // To calculate the total bill amount.
        if (ArConstants.BillingFrequencyValues.isPredeterminedBilling(
                contractsGrantsInvoiceDocument.getInvoiceGeneralDetail()) && !CollectionUtils.isEmpty(
                contractsGrantsInvoiceDocument.getInvoiceBills())) {
            for (InvoiceBill bill : contractsGrantsInvoiceDocument.getInvoiceBills()) {
                if (bill.getEstimatedAmount() != null) {
                    totalBillAmount = totalBillAmount.add(bill.getEstimatedAmount());
                }
            }
        }
        return totalBillAmount;
    }

    @Override
    public KualiDecimal getInvoiceMilestoneTotal(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument) {
        KualiDecimal totalMilestoneAmount = KualiDecimal.ZERO;
        // To calculate the total milestone amount.
        if (ArConstants.BillingFrequencyValues.isMilestone(
                contractsGrantsInvoiceDocument.getInvoiceGeneralDetail()) && !CollectionUtils.isEmpty(
                contractsGrantsInvoiceDocument.getInvoiceMilestones())) {
            for (InvoiceMilestone milestone : contractsGrantsInvoiceDocument.getInvoiceMilestones()) {
                if (milestone.getMilestoneAmount() != null) {
                    totalMilestoneAmount = totalMilestoneAmount.add(milestone.getMilestoneAmount());
                }
            }
        }
        return totalMilestoneAmount;
    }

    /**
     * Retrieves the OrganizationAccountingDefault record for the given billing organization
     *
     * @param billByChartOfAccountsCode the chart of the billing organization
     * @param billByOrganizationCode    the organization code of the billing organization
     * @return the OrganizationAccountingDefault for the given chart and organization code
     */
    protected OrganizationAccountingDefault retrieveBillingOrganizationAccountingDefault(
            final String billByChartOfAccountsCode, final String billByOrganizationCode) {
        Map<String, Object> criteria = new HashMap<>();
        Integer currentYear = universityDateService.getCurrentFiscalYear();
        criteria.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, currentYear);
        criteria.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, billByChartOfAccountsCode);
        criteria.put(KFSPropertyConstants.ORGANIZATION_CODE, billByOrganizationCode);
        // Need to avoid hitting database in the loop. option would be to set the financial object code when the form
        // loads and save it somewhere.
        return businessObjectService.findByPrimaryKey(OrganizationAccountingDefault.class, criteria);
    }

    protected CustomerInvoiceDetail createSourceAccountingLine(String docNum, String coaCode, String acctNum,
            SubFundGroup subFundGroup, KualiDecimal totalAmount, Integer seqNum) {
        CustomerInvoiceDetail cid = new CustomerInvoiceDetail();
        cid.setDocumentNumber(docNum);

        cid.setAccountNumber(acctNum);
        cid.setChartOfAccountsCode(coaCode);

        ContractsGrantsInvoiceObjectCode cgbiObjectCode = contractGrantsInvoiceObjectCodeForSubFundGroup(subFundGroup,
                coaCode);
        if (cgbiObjectCode != null) {
            cid.setFinancialObjectCode(cgbiObjectCode.getIncomeFinancialObjectCode());
            // To get AR Object codes for the GLPEs .... as it is not being called implicitly.
            cid.setAccountsReceivableObjectCode(cgbiObjectCode.getReceivableFinancialObjectCode());
        }

        cid.setSequenceNumber(seqNum);
        cid.setInvoiceItemQuantity(BigDecimal.ONE);
        cid.setInvoiceItemUnitOfMeasureCode(ArConstants.CUSTOMER_INVOICE_DETAIL_UOM_DEFAULT);

        cid.setInvoiceItemUnitPrice(totalAmount);
        cid.setAmount(totalAmount);
        if (totalAmount.isNegative()) {
            cid.setInvoiceItemDiscountLineNumber(seqNum);
        }
        return cid;
    }

    @Override
    public void recalculateTotalAmountBilledToDate(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument) {
        ContractsGrantsInvoiceDetail totalCostInvoiceDetail = contractsGrantsInvoiceDocument
                .getTotalCostInvoiceDetail();

        // To verify the expenditure amounts have been changed and
        // update the invoiceDetailObjectCode
        boolean expenditureValueChanged = adjustObjectCodeAmountsIfChanged(contractsGrantsInvoiceDocument);

        if (expenditureValueChanged) {
            // update Total Direct Cost in the Invoice Detail Tab
            KualiDecimal totalDirectCostExpenditures = getInvoiceDetailExpenditureSum(
                    contractsGrantsInvoiceDocument.getDirectCostInvoiceDetails());

            // Set expenditures to Direct Cost invoice Details
            ContractsGrantsInvoiceDetail totalDirectCostInvoiceDetail = contractsGrantsInvoiceDocument
                    .getTotalDirectCostInvoiceDetail();
            if (ObjectUtils.isNotNull(totalDirectCostInvoiceDetail)) {
                totalDirectCostInvoiceDetail.setInvoiceAmount(totalDirectCostExpenditures);
            }

            // update Total Indirect Cost in the Invoice Detail Tab
            KualiDecimal totalInDirectCostExpenditures = getInvoiceDetailExpenditureSum(
                    contractsGrantsInvoiceDocument.getIndirectCostInvoiceDetails());

            // Set expenditures to Indirect Cost invoice Details
            ContractsGrantsInvoiceDetail totalInDirectCostInvoiceDetail = contractsGrantsInvoiceDocument
                    .getTotalIndirectCostInvoiceDetail();
            if (ObjectUtils.isNotNull(totalInDirectCostInvoiceDetail)) {
                totalInDirectCostInvoiceDetail.setInvoiceAmount(totalInDirectCostExpenditures);
            }

            // Set the total for Total Cost Invoice Details section.
            if (ObjectUtils.isNotNull(totalCostInvoiceDetail)) {
                totalCostInvoiceDetail.setInvoiceAmount(
                        totalDirectCostInvoiceDetail.getInvoiceAmount().add(totalInDirectCostExpenditures));
            }
            recalculateAccountDetails(contractsGrantsInvoiceDocument.getAccountDetails(),
                    contractsGrantsInvoiceDocument.getInvoiceDetailAccountObjectCodes());

            // update source accounting lines
            updateInvoiceSourceAccountingLines(contractsGrantsInvoiceDocument.getAccountDetails(),
                    contractsGrantsInvoiceDocument.getSourceAccountingLines());
        }

        contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().setTotalPreviouslyBilled(
                getAwardBilledToDateAmount(
                        contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getProposalNumber()));

        KualiDecimal newTotalBilled = contractsGrantsInvoiceDocument.getTotalInvoiceAmount().add(
                contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getTotalPreviouslyBilled());
        newTotalBilled = newTotalBilled.add(getOtherTotalBilledForAwardPeriod(contractsGrantsInvoiceDocument));

        contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().setTotalAmountBilledToDate(newTotalBilled);

        calculatePreviouslyBilledAmounts(contractsGrantsInvoiceDocument);
    }

    @Override
    public void calculatePreviouslyBilledAmounts(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument) {
        KualiDecimal previouslyBilledAmount = KualiDecimal.ZERO;

        final String proposalNumber = contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getProposalNumber();

        for (InvoiceAccountDetail invoiceAccountDetail: contractsGrantsInvoiceDocument.getAccountDetails()) {
            String chartOfAccountsCode = invoiceAccountDetail.getChartOfAccountsCode();
            String accountNumber = invoiceAccountDetail.getAccountNumber();

            previouslyBilledAmount = previouslyBilledAmount.add(getPredeterminedBillingBilledToDateAmount(proposalNumber, chartOfAccountsCode, accountNumber));
            previouslyBilledAmount = previouslyBilledAmount.add(getMilestonesBilledToDateAmount(proposalNumber, chartOfAccountsCode, accountNumber));
        }

        contractsGrantsInvoiceDocument.setPreviouslyBilledTotal(previouslyBilledAmount);

        KualiDecimal previouslyBilledInvoiceAmount;
        if (contractsGrantsInvoiceDocument.isCorrectionDocument()) {
            previouslyBilledInvoiceAmount = previouslyBilledAmount.negated();
        } else {
            final KualiDecimal totalPreviouslyBilled = contractsGrantsInvoiceDocument.getTotalCostInvoiceDetail().getTotalPreviouslyBilled();
            previouslyBilledInvoiceAmount = previouslyBilledAmount.subtract(totalPreviouslyBilled);
            if (previouslyBilledInvoiceAmount.isLessThan(KualiDecimal.ZERO)) {
                previouslyBilledInvoiceAmount = KualiDecimal.ZERO;
            }
        }

        final KualiDecimal invoiceAmount = contractsGrantsInvoiceDocument.getTotalCostInvoiceDetail().getInvoiceAmount();
        contractsGrantsInvoiceDocument.setPreviouslyBilledInvoiceAmount(previouslyBilledInvoiceAmount);
        contractsGrantsInvoiceDocument.setTotalInvoiceInvoiceAmount(invoiceAmount.subtract(previouslyBilledInvoiceAmount));
    }

    @Override
    public KualiDecimal getOtherTotalBilledForAwardPeriod(
            ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument) {
        KualiDecimal newTotalBilled = KualiDecimal.ZERO;

        Map<String, String> fieldValuesForInvoice = new HashMap<>();
        fieldValuesForInvoice.put(ArPropertyConstants.ContractsGrantsInvoiceDocumentFields.PROPOSAL_NUMBER,
                contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getProposalNumber());
        fieldValuesForInvoice.put(ArPropertyConstants.INVOICE_GENERAL_DETAIL + "." + ArPropertyConstants.BILLING_PERIOD,
                contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getBillingPeriod());
        String docNumberCriteriaString = SearchOperator.NOT + contractsGrantsInvoiceDocument.getDocumentNumber();
        if (ObjectUtils.isNotNull(contractsGrantsInvoiceDocument.getDocumentHeader()) && StringUtils
                .isNotBlank(contractsGrantsInvoiceDocument.getDocumentHeader()
                        .getFinancialDocumentInErrorNumber())) {
            docNumberCriteriaString += SearchOperator.NOT + contractsGrantsInvoiceDocument
                    .getDocumentHeader().getFinancialDocumentInErrorNumber();
        }
        fieldValuesForInvoice.put(KFSPropertyConstants.DOCUMENT_NUMBER, docNumberCriteriaString);
        fieldValuesForInvoice.put(ArPropertyConstants.DOCUMENT_STATUS_CODE,
                SearchOperator.NOT + KFSConstants.DocumentStatusCodes.PROCESSED + SearchOperator.NOT
                        + KFSConstants.DocumentStatusCodes.APPROVED);

        Collection<ContractsGrantsInvoiceDocument> cgInvoiceDocuments = retrieveAllCGInvoicesByCriteria(
                fieldValuesForInvoice);
        for (ContractsGrantsInvoiceDocument cgInvoiceDocument : cgInvoiceDocuments) {
            for (InvoiceAccountDetail invAcctD : cgInvoiceDocument.getAccountDetails()) {
                newTotalBilled = newTotalBilled.add(invAcctD.getInvoiceAmount());
            }
        }

        return newTotalBilled;
    }

    public KualiDecimal getInvoiceDetailExpenditureSum(List<ContractsGrantsInvoiceDetail> invoiceDetails) {
        KualiDecimal totalExpenditures = KualiDecimal.ZERO;
        for (ContractsGrantsInvoiceDetail invoiceDetail : invoiceDetails) {
            totalExpenditures = totalExpenditures.add(invoiceDetail.getInvoiceAmount());
        }
        return totalExpenditures;
    }

    protected void updateInvoiceSourceAccountingLines(List<InvoiceAccountDetail> invoiceAccountDetails,
            List sourceAccountingLines) {
        if (sourceAccountingLines.size() > 1) {
            // Invoice By Award
            for (CustomerInvoiceDetail cide : (List<CustomerInvoiceDetail>) sourceAccountingLines) {
                for (InvoiceAccountDetail invoiceAccountDetail : invoiceAccountDetails) {
                    if (cide.getAccountNumber().equals(invoiceAccountDetail.getAccountNumber())) {
                        cide.setInvoiceItemUnitPrice(invoiceAccountDetail.getInvoiceAmount());
                        cide.setAmount(invoiceAccountDetail.getInvoiceAmount());
                    }
                }
            }
        } else if (sourceAccountingLines.size() == 1) {
            // This would be a case where the invoice is generated by Contract Control Account or Invoice By Account.
            KualiDecimal totalExpenditureAmount = KualiDecimal.ZERO;
            if (invoiceAccountDetails.size() == 1) {
                // Invoice By Account
                // update source accounting lines
                CustomerInvoiceDetail cide = (CustomerInvoiceDetail) sourceAccountingLines.get(0);
                cide.setInvoiceItemUnitPrice(invoiceAccountDetails.get(0).getInvoiceAmount());
                cide.setAmount(invoiceAccountDetails.get(0).getInvoiceAmount());
            } else {
                // Invoice By Contract Control Account
                for (InvoiceAccountDetail invoiceAccountDetail : invoiceAccountDetails) {
                    totalExpenditureAmount = totalExpenditureAmount.add(invoiceAccountDetail.getInvoiceAmount());
                }
                // update source accounting lines
                CustomerInvoiceDetail cide = (CustomerInvoiceDetail) sourceAccountingLines.get(0);
                cide.setInvoiceItemUnitPrice(totalExpenditureAmount);
                cide.setAmount(totalExpenditureAmount);
            }
        }
    }

    @Override
    public void prorateBill(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument) {
        // Amount to be billed on this invoice
        KualiDecimal totalCost = new KualiDecimal(0);
        // must iterate through the invoice details because the user might have manually changed the value
        for (ContractsGrantsInvoiceDetail invD : contractsGrantsInvoiceDocument.getInvoiceDetails()) {
            totalCost = totalCost.add(invD.getInvoiceAmount());
        }
        // Total Billed so far
        KualiDecimal billedTotalCost = contractsGrantsInvoiceDocument.getInvoiceGeneralDetail()
                .getTotalPreviouslyBilled();
        // AwardTotal
        KualiDecimal accountAwardTotal = contractsGrantsInvoiceDocument.getInvoiceGeneralDetail()
                .getAwardTotal();

        if (accountAwardTotal.subtract(billedTotalCost).isGreaterEqual(new KualiDecimal(0))) {
            KualiDecimal amountEligibleForBilling = accountAwardTotal.subtract(billedTotalCost);
            // only recalculate if the current invoice is over what's billable.

            if (totalCost.isGreaterThan(amountEligibleForBilling)) {
                // use BigDecimal because percentage should not have only a scale of 2, we need more for accuracy
                BigDecimal percentage = amountEligibleForBilling.bigDecimalValue().divide(totalCost.bigDecimalValue(),
                        10, RoundingMode.HALF_DOWN);
                // use to check if rounding has left a few cents off
                KualiDecimal amountToBill = new KualiDecimal(0);

                ContractsGrantsInvoiceDetail largestCostCategory = null;
                BigDecimal largestAmount = BigDecimal.ZERO;
                for (ContractsGrantsInvoiceDetail invD : contractsGrantsInvoiceDocument.getInvoiceDetails()) {
                    BigDecimal newValue = invD.getInvoiceAmount().bigDecimalValue().multiply(percentage);
                    KualiDecimal newKualiDecimalValue = new KualiDecimal(newValue.setScale(2,
                            RoundingMode.DOWN));
                    invD.setInvoiceAmount(newKualiDecimalValue);
                    amountToBill = amountToBill.add(newKualiDecimalValue);
                    if (newValue.compareTo(largestAmount) > 0) {
                        largestAmount = newKualiDecimalValue.bigDecimalValue();
                        largestCostCategory = invD;
                    }
                }
                if (!amountToBill.equals(amountEligibleForBilling)) {
                    KualiDecimal remaining = amountEligibleForBilling.subtract(amountToBill);
                    if (ObjectUtils.isNull(largestCostCategory) && CollectionUtils.isNotEmpty(
                            contractsGrantsInvoiceDocument.getInvoiceDetails())) {
                        largestCostCategory = contractsGrantsInvoiceDocument.getInvoiceDetails().get(0);
                    }
                    if (ObjectUtils.isNotNull(largestCostCategory)) {
                        largestCostCategory.setInvoiceAmount(largestCostCategory.getInvoiceAmount().add(remaining));
                    }
                }
                recalculateTotalAmountBilledToDate(contractsGrantsInvoiceDocument);
            }
        }
    }

    @Override
    public void addToAccountObjectCodeBilledTotal(
            List<InvoiceDetailAccountObjectCode> invoiceDetailAccountObjectCodes) {
        for (InvoiceDetailAccountObjectCode invoiceDetailAccountObjectCode : invoiceDetailAccountObjectCodes) {
            Map<String, Object> totalBilledKeys = new HashMap<>();
            totalBilledKeys.put(KFSPropertyConstants.PROPOSAL_NUMBER,
                    invoiceDetailAccountObjectCode.getProposalNumber());
            totalBilledKeys.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE,
                    invoiceDetailAccountObjectCode.getChartOfAccountsCode());
            totalBilledKeys.put(KFSPropertyConstants.ACCOUNT_NUMBER, invoiceDetailAccountObjectCode.getAccountNumber());
            totalBilledKeys.put(KFSPropertyConstants.FINANCIAL_OBJECT_CODE,
                    invoiceDetailAccountObjectCode.getFinancialObjectCode());

            List<AwardAccountObjectCodeTotalBilled> awardAccountObjectCodeTotalBilledList =
                    (List<AwardAccountObjectCodeTotalBilled>) businessObjectService
                    .findMatching(AwardAccountObjectCodeTotalBilled.class, totalBilledKeys);
            AwardAccountObjectCodeTotalBilled awardAccountObjectCodeTotalBilled = new AwardAccountObjectCodeTotalBilled();
            if (awardAccountObjectCodeTotalBilledList != null && !awardAccountObjectCodeTotalBilledList.isEmpty()) {
                awardAccountObjectCodeTotalBilled = awardAccountObjectCodeTotalBilledList.get(0);
                awardAccountObjectCodeTotalBilled.setTotalBilled(awardAccountObjectCodeTotalBilled.getTotalBilled()
                        .add(invoiceDetailAccountObjectCode.getCurrentExpenditures()));
            } else {
                awardAccountObjectCodeTotalBilled.setProposalNumber(invoiceDetailAccountObjectCode.getProposalNumber());
                awardAccountObjectCodeTotalBilled.setChartOfAccountsCode(
                        invoiceDetailAccountObjectCode.getChartOfAccountsCode());
                awardAccountObjectCodeTotalBilled.setAccountNumber(invoiceDetailAccountObjectCode.getAccountNumber());
                awardAccountObjectCodeTotalBilled.setFinancialObjectCode(
                        invoiceDetailAccountObjectCode.getFinancialObjectCode());
                awardAccountObjectCodeTotalBilled.setTotalBilled(
                        invoiceDetailAccountObjectCode.getCurrentExpenditures());
            }
            getBusinessObjectService().save(awardAccountObjectCodeTotalBilled);
        }
    }

    /**
     * If any of the current expenditures for the cost categories on the Contracts & Grants Invoice Document have
     * changed, recalculate the Object Code amounts.
     *
     * @param contractsGrantsInvoiceDocument document containing cost categories to review
     * @return true if expenditure value changed, false otherwise
     */
    protected boolean adjustObjectCodeAmountsIfChanged(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument) {
        boolean isExpenditureValueChanged = false;

        // put the invoiceDetailAccountObjectCode into a map based on category
        List<InvoiceDetailAccountObjectCode> invoiceDetailAccountObjectCodes = contractsGrantsInvoiceDocument
                .getInvoiceDetailAccountObjectCodes();
        Map<String, List<InvoiceDetailAccountObjectCode>> invoiceDetailAccountObjectCodeMap = new HashMap<>();
        for (InvoiceDetailAccountObjectCode invoiceDetailAccountObjectCode : invoiceDetailAccountObjectCodes) {
            String categoryCode = invoiceDetailAccountObjectCode.getCategoryCode();
            List<InvoiceDetailAccountObjectCode> invoiceDetailAccountObjectCodeList = invoiceDetailAccountObjectCodeMap
                    .get(categoryCode);
            // if new category, create new list to put into map
            if (invoiceDetailAccountObjectCodeList == null) {
                List<InvoiceDetailAccountObjectCode> newInvoiceDetailAccountObjectCodeList = new ArrayList<>();
                newInvoiceDetailAccountObjectCodeList.add(invoiceDetailAccountObjectCode);
                invoiceDetailAccountObjectCodeMap.put(categoryCode, newInvoiceDetailAccountObjectCodeList);
            } else {
                // else, if list is found, add it to existing list
                invoiceDetailAccountObjectCodeMap.get(categoryCode).add(invoiceDetailAccountObjectCode);
            }
        }

        // figure out if any of the current expenditures for the category has been changed. If yes, then update the
        // invoiceDetailObjectCode and update account details
        for (ContractsGrantsInvoiceDetail invoiceDetail : contractsGrantsInvoiceDocument.getInvoiceDetails()) {
            KualiDecimal total = getSumOfExpendituresOfCategory(
                    invoiceDetailAccountObjectCodeMap.get(invoiceDetail.getCategoryCode()));
            // To set expenditures to zero if its blank - to avoid exceptions.
            if (ObjectUtils.isNull(invoiceDetail.getInvoiceAmount())) {
                invoiceDetail.setInvoiceAmount(KualiDecimal.ZERO);
            }

            if (invoiceDetail.getInvoiceAmount().compareTo(total) != 0) {
                recalculateObjectCodeByCategory(contractsGrantsInvoiceDocument, invoiceDetail, total,
                        invoiceDetailAccountObjectCodeMap.get(invoiceDetail.getCategoryCode()));
                isExpenditureValueChanged = true;
            }
        }
        return isExpenditureValueChanged;
    }

    protected KualiDecimal getSumOfExpendituresOfCategory(
            List<InvoiceDetailAccountObjectCode> invoiceDetailAccountObjectCodes) {
        KualiDecimal total = KualiDecimal.ZERO;
        // null can occur if this category has no invoice detail objectcode amounts
        if (ObjectUtils.isNotNull(invoiceDetailAccountObjectCodes)) {
            for (InvoiceDetailAccountObjectCode invoiceDetailAccountObjectCode : invoiceDetailAccountObjectCodes) {
                total = total.add(invoiceDetailAccountObjectCode.getCurrentExpenditures());
            }
        }
        return total;
    }

    /**
     * This method recalculates the invoiceDetailAccountObjectCode in one category that sits behind the scenes of the
     * invoice document.
     *
     * @param contractsGrantsInvoiceDocument
     * @param invoiceDetail
     * @param total                           is the sum of the current expenditures from all the object codes in
     *                                        that category
     * @param invoiceDetailAccountObjectCodes
     */
    protected void recalculateObjectCodeByCategory(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument,
            ContractsGrantsInvoiceDetail invoiceDetail, KualiDecimal total,
            List<InvoiceDetailAccountObjectCode> invoiceDetailAccountObjectCodes) {
        KualiDecimal currentExpenditure = invoiceDetail.getInvoiceAmount();
        KualiDecimal newTotalAmount = KualiDecimal.ZERO;

        // if the sum of the object codes is 0, then distribute the expenditure change evenly to all object codes in
        // the category
        if (total.compareTo(KualiDecimal.ZERO) == 0) {
            if (invoiceDetailAccountObjectCodes != null) {
                int numberOfObjectCodes = invoiceDetailAccountObjectCodes.size();
                if (numberOfObjectCodes != 0) {
                    KualiDecimal newAmount = new KualiDecimal(currentExpenditure.bigDecimalValue()
                            .divide(new BigDecimal(numberOfObjectCodes), 10, RoundingMode.HALF_DOWN));
                    for (InvoiceDetailAccountObjectCode invoiceDetailAccountObjectCode : invoiceDetailAccountObjectCodes) {
                        invoiceDetailAccountObjectCode.setCurrentExpenditures(newAmount);
                        newTotalAmount = newTotalAmount.add(newAmount);
                    }
                }
            } else {
                // if the list is null, then there are no account/object code in the gl_balance_t. So assign the
                // amount to the first object code in the category
                assignCurrentExpenditureToNonExistingAccountObjectCode(contractsGrantsInvoiceDocument, invoiceDetail);
            }
        } else {
            for (InvoiceDetailAccountObjectCode invoiceDetailAccountObjectCode : invoiceDetailAccountObjectCodes) {
                // this may rarely happen
                // if the initial total is 0, that means none of the object codes in this category is set to bill.
                // If this amount is changed, then just divide evenly among all object codes.
                KualiDecimal newAmount = new KualiDecimal(
                        invoiceDetailAccountObjectCode.getCurrentExpenditures().bigDecimalValue()
                                .divide(total.bigDecimalValue(), 10, RoundingMode.HALF_DOWN)
                                .multiply(currentExpenditure.bigDecimalValue()));
                invoiceDetailAccountObjectCode.setCurrentExpenditures(newAmount);
                newTotalAmount = newTotalAmount.add(newAmount);
            }

            int remainderFromRounding = currentExpenditure.subtract(newTotalAmount)
                    .multiply(new KualiDecimal(100)).intValue();

            // add remainder from rounding
            KualiDecimal addAmount = new KualiDecimal(0.01);
            if (remainderFromRounding < 0) {
                addAmount = new KualiDecimal(-0.01);
                remainderFromRounding = Math.abs(remainderFromRounding);
            }

            int objectCodeIndex = 0;
            for (int i = 0; i < remainderFromRounding; i++) {
                InvoiceDetailAccountObjectCode invoiceDetailAccountObjectCode = invoiceDetailAccountObjectCodes.get(
                        objectCodeIndex);
                invoiceDetailAccountObjectCode.setCurrentExpenditures(invoiceDetailAccountObjectCode
                        .getCurrentExpenditures().add(addAmount));
                objectCodeIndex++;
                if (objectCodeIndex >= invoiceDetailAccountObjectCodes.size()) {
                    objectCodeIndex = 0;
                }
            }
        }
    }

    protected void assignCurrentExpenditureToNonExistingAccountObjectCode(
            ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument, ContractsGrantsInvoiceDetail invoiceDetail) {
        String categoryCode = invoiceDetail.getCategoryCode();
        if (StringUtils.isBlank(categoryCode)) {
            throw new IllegalArgumentException(
                    "Category Code can not be null during recalculation of account object code for Contracts & Grants Invoice Document.");
        }
        // get the category that matches this category code.
        final CostCategory category = businessObjectService.findBySinglePrimaryKey(CostCategory.class, categoryCode);

        // got the category now.
        if (ObjectUtils.isNotNull(category)) {
            final KualiDecimal oneCent = new KualiDecimal(0.01);

            int size = contractsGrantsInvoiceDocument.getAccountDetails().size();
            KualiDecimal amount = new KualiDecimal(invoiceDetail.getInvoiceAmount().bigDecimalValue()
                    .divide(new BigDecimal(size), 2, RoundingMode.HALF_UP));
            KualiDecimal remainder = invoiceDetail.getInvoiceAmount().subtract(amount.multiply(new KualiDecimal(size)));

            for (InvoiceAccountDetail invoiceAccountDetail : contractsGrantsInvoiceDocument.getAccountDetails()) {
                InvoiceDetailAccountObjectCode invoiceDetailAccountObjectCode = new InvoiceDetailAccountObjectCode();
                invoiceDetailAccountObjectCode.setDocumentNumber(contractsGrantsInvoiceDocument.getDocumentNumber());
                invoiceDetailAccountObjectCode.setProposalNumber(
                        contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getProposalNumber());
                invoiceDetailAccountObjectCode.setCategoryCode(categoryCode);
                invoiceDetailAccountObjectCode.setAccountNumber(invoiceAccountDetail.getAccountNumber());
                invoiceDetailAccountObjectCode.setChartOfAccountsCode(invoiceAccountDetail.getChartOfAccountsCode());
                // it's 0.00 that's why we are in this section to begin with.
                invoiceDetailAccountObjectCode.setCumulativeExpenditures(
                        KualiDecimal.ZERO);
                // this is also 0.00 because it has never been billed before
                invoiceDetailAccountObjectCode.setTotalBilled(
                        KualiDecimal.ZERO);
                final ObjectCodeCurrent objectCode = getCostCategoryService().findObjectCodeForChartAndCategory(
                        invoiceAccountDetail.getChartOfAccountsCode(), categoryCode);
                if (ObjectUtils.isNotNull(objectCode)) {
                    invoiceDetailAccountObjectCode.setFinancialObjectCode(objectCode.getFinancialObjectCode());
                }

                // tack on or remove one penny until the remainder is 0 - take a penny, leave a penny!
                if (remainder.isGreaterThan(KualiDecimal.ZERO)) {
                    amount = amount.add(oneCent);
                    remainder = remainder.subtract(oneCent);
                } else if (remainder.isLessThan(KualiDecimal.ZERO)) {
                    amount = amount.subtract(oneCent);
                    remainder = remainder.add(oneCent);
                }
                invoiceDetailAccountObjectCode.setCurrentExpenditures(amount);

                List<InvoiceDetailAccountObjectCode> invoiceDetailAccountObjectCodes = contractsGrantsInvoiceDocument
                        .getInvoiceDetailAccountObjectCodes();
                if (invoiceDetailAccountObjectCodes.contains(invoiceDetailAccountObjectCode)) {
                    // update existing code
                    InvoiceDetailAccountObjectCode original = invoiceDetailAccountObjectCodes.get(
                            invoiceDetailAccountObjectCodes.indexOf(invoiceDetailAccountObjectCode));
                    original.setCurrentExpenditures(amount);
                    original.setCategoryCode(categoryCode);
                } else {
                    // add this single account object code item to the list in the Map
                    contractsGrantsInvoiceDocument.getInvoiceDetailAccountObjectCodes().add(
                            invoiceDetailAccountObjectCode);
                }
            }
        } else {
            LOG.error("Category Code cannot be found from the category list during recalculation of account object "
                    + "code for Contracts & Grants Invoice Document.");
        }
    }

    public void recalculateAccountDetails(List<InvoiceAccountDetail> invoiceAccountDetails,
            List<InvoiceDetailAccountObjectCode> invoiceDetailAccountObjectCodes) {
        Map<String, KualiDecimal> currentExpenditureByAccountNumberMap = new HashMap<>();
        for (InvoiceDetailAccountObjectCode invoiceDetailAccountObjectCode : invoiceDetailAccountObjectCodes) {
            String accountNumber = invoiceDetailAccountObjectCode.getAccountNumber();
            KualiDecimal expenditureSum = currentExpenditureByAccountNumberMap.get(accountNumber);
            // if account number not found in map, then create new total, 0
            if (expenditureSum == null) {
                expenditureSum = KualiDecimal.ZERO;
            }
            expenditureSum = expenditureSum.add(invoiceDetailAccountObjectCode.getCurrentExpenditures());
            currentExpenditureByAccountNumberMap.put(accountNumber, expenditureSum);
        }

        for (InvoiceAccountDetail invoiceAccountDetail : invoiceAccountDetails) {
            final KualiDecimal expenditureAmount = ObjectUtils.isNull(
                    currentExpenditureByAccountNumberMap.get(invoiceAccountDetail.getAccountNumber()))
                    ? KualiDecimal.ZERO
                    : currentExpenditureByAccountNumberMap.get(invoiceAccountDetail.getAccountNumber());
            invoiceAccountDetail.setInvoiceAmount(expenditureAmount);
        }
    }

    @Override
    public KualiDecimal getAwardBilledToDateAmount(String proposalNumber) {
        Map<String, String> fieldValues = new HashMap<>();
        fieldValues.put(ArPropertyConstants.ContractsGrantsInvoiceDocumentFields.PROPOSAL_NUMBER, proposalNumber);
        fieldValues.put(KFSPropertyConstants.DOCUMENT_HEADER + "." + KFSPropertyConstants.WORKFLOW_DOCUMENT_STATUS_CODE,
                StringUtils.join(financialSystemDocumentService.getSuccessfulDocumentStatuses(), "|"));

        Collection<ContractsGrantsInvoiceDocument> invoiceDocuments = retrieveAllCGInvoicesByCriteria(fieldValues);

        final KualiDecimal milestoneTotal = getMilestonesBilledToDateAmount(proposalNumber);
        final KualiDecimal billTotal = getPredeterminedBillingBilledToDateAmount(proposalNumber);
        final KualiDecimal nonScheduledTotal = calculateTotalInvoiceAmount(invoiceDocuments);

        return milestoneTotal.add(billTotal).add(nonScheduledTotal);
    }

    private KualiDecimal calculateTotalInvoiceAmount(Collection<ContractsGrantsInvoiceDocument> invoiceDocuments) {
        return invoiceDocuments.stream()
                .filter(invoice -> !ArConstants.BillingFrequencyValues.isMilestone(invoice.getInvoiceGeneralDetail()))
                .filter(invoice -> !ArConstants.BillingFrequencyValues.isPredeterminedBilling(invoice.getInvoiceGeneralDetail()))
                .filter(invoice -> ObjectUtils.isNotNull(invoice.getTotalInvoiceAmount()))
                .reduce(KualiDecimal.ZERO, (sum, invoice) ->
                        invoice.getTotalInvoiceAmount().add(sum), AbstractKualiDecimal::add);
    }

    @Override
    public KualiDecimal getAwardBilledToDateAmountExcludingDocument(String proposalNumber, String documentNumber) {
        Map<String, String> fieldValues = new HashMap<>();
        fieldValues.put(ArPropertyConstants.ContractsGrantsInvoiceDocumentFields.PROPOSAL_NUMBER, proposalNumber);
        fieldValues.put(KFSPropertyConstants.DOCUMENT_HEADER + "." + KFSPropertyConstants.WORKFLOW_DOCUMENT_STATUS_CODE,
                StringUtils.join(financialSystemDocumentService.getSuccessfulDocumentStatuses(), "|"));
        fieldValues.put(KFSPropertyConstants.DOCUMENT_NUMBER, SearchOperator.NOT + documentNumber);

        Collection<ContractsGrantsInvoiceDocument> invoiceDocuments = retrieveAllCGInvoicesByCriteria(fieldValues);

        final KualiDecimal milestoneTotal = getMilestonesBilledToDateAmount(proposalNumber);
        final KualiDecimal billTotal = getPredeterminedBillingBilledToDateAmount(proposalNumber);
        final KualiDecimal nonScheduledTotal = calculateTotalInvoiceAmount(invoiceDocuments);

        return milestoneTotal.add(billTotal).add(nonScheduledTotal);
    }

    @Override
    public Collection<ContractsGrantsInvoiceDocument> retrieveAllCGInvoicesByCriteria(Map fieldValues) {
        return contractsGrantsInvoiceDocumentDao.getMatchingInvoicesByCollection(fieldValues);
    }

    @Override
    public KualiDecimal getBudgetAndActualsForAwardAccount(ContractsAndGrantsBillingAwardAccount awardAccount,
            String balanceTypeCode) {

        KualiDecimal balanceAmount = KualiDecimal.ZERO;
        Integer currentFiscalYear = universityDateService.getCurrentFiscalYear();
        final List<String> basicExpenseObjectTypes = objectTypeService.getBasicExpenseObjectTypes(currentFiscalYear);
        final String chartOfAccountsCode = awardAccount.getChartOfAccountsCode();
        final String accountNumber = awardAccount.getAccountNumber();

        Map<String, Object> balanceKeys = new HashMap<>();
        balanceKeys.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartOfAccountsCode);
        balanceKeys.put(KFSPropertyConstants.ACCOUNT_NUMBER, accountNumber);
        balanceKeys.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, currentFiscalYear);
        balanceKeys.put(KFSPropertyConstants.BALANCE_TYPE_CODE, balanceTypeCode);
        balanceKeys.put(KFSPropertyConstants.OBJECT_TYPE_CODE, basicExpenseObjectTypes);

        for (Balance balance : businessObjectService.findMatching(Balance.class, balanceKeys)) {
            if (ObjectUtils.isNull(balance.getSubAccount())
                    || ObjectUtils.isNull(balance.getSubAccount().getA21SubAccount())
                    || !StringUtils.equalsIgnoreCase(balance.getSubAccount().getA21SubAccount().getSubAccountTypeCode(),
                    KFSConstants.SubAccountType.COST_SHARE)) {
                balanceAmount = balanceAmount.add(calculateCumulativeBalanceAmount(balance));
            }
        }

        return balanceAmount;
    }

    @Override
    public KualiDecimal calculateCumulativeBalanceAmount(Balance balance) {
        KualiDecimal balanceAmount = KualiDecimal.ZERO;
        if (isFirstFiscalPeriod()) {
            balanceAmount = balanceAmount
                    .add(balance.getContractsGrantsBeginningBalanceAmount())
                    .add(balance.getAccountLineAnnualBalanceAmount());
            if (!includePeriod13InPeriod01Calculations()) {
                balanceAmount = balanceAmount.subtract(balance.getMonth13Amount());
            }
        } else {
            balanceAmount = balanceAmount.add(calculateBalanceAmountWithoutLastBilledPeriod(balance));
        }
        return balanceAmount;
    }

    @Override
    public boolean isFirstFiscalPeriod() {
        final AccountingPeriod currentPeriod = accountingPeriodService.getByDate(getDateTimeService()
                .getCurrentSqlDate());
        return StringUtils.equals(currentPeriod.getUniversityFiscalPeriodCode(), KFSConstants.MONTH1);
    }

    @Override
    public boolean includePeriod13InPeriod01Calculations() {
        return getParameterService().getParameterValueAsBoolean(ContractsGrantsInvoiceDocument.class,
                ArParameterConstants.INCLUDE_PERIOD_13_IND, Boolean.FALSE);
    }

    /**
     * This method would make sure the amounts of the current period are not included. So it calculates the cumulative
     * and subtracts the current period values. This would be done for Billing Frequencies - Monthly, Quarterly,
     * Semi-Annual and Annual.
     *
     * @param glBalance
     * @return balanceAmount
     */
    private KualiDecimal calculateBalanceAmountWithoutLastBilledPeriod(Balance glBalance) {
        Timestamp ts = new Timestamp(new java.util.Date().getTime());
        java.sql.Date today = new java.sql.Date(ts.getTime());
        AccountingPeriod currPeriod = accountingPeriodService.getByDate(today);
        String currentPeriodCode = currPeriod.getUniversityFiscalPeriodCode();

        KualiDecimal currentBalanceAmount = KualiDecimal.ZERO;
        switch (currentPeriodCode) {
            case KFSConstants.MONTH13:
                currentBalanceAmount = currentBalanceAmount.add(glBalance.getMonth12Amount());
                // notice - no break!!!! we want to fall through to pick up all the prior months amounts
            case KFSConstants.MONTH12:
                currentBalanceAmount = currentBalanceAmount.add(glBalance.getMonth11Amount());
                // fall through
            case KFSConstants.MONTH11:
                currentBalanceAmount = currentBalanceAmount.add(glBalance.getMonth10Amount());
                // fall through
            case KFSConstants.MONTH10:
                currentBalanceAmount = currentBalanceAmount.add(glBalance.getMonth9Amount());
                // fall through
            case KFSConstants.MONTH9:
                currentBalanceAmount = currentBalanceAmount.add(glBalance.getMonth8Amount());
                // fall through
            case KFSConstants.MONTH8:
                currentBalanceAmount = currentBalanceAmount.add(glBalance.getMonth7Amount());
                // fall through
            case KFSConstants.MONTH7:
                currentBalanceAmount = currentBalanceAmount.add(glBalance.getMonth6Amount());
                // fall through
            case KFSConstants.MONTH6:
                currentBalanceAmount = currentBalanceAmount.add(glBalance.getMonth5Amount());
                // fall through
            case KFSConstants.MONTH5:
                currentBalanceAmount = currentBalanceAmount.add(glBalance.getMonth4Amount());
                // fall through
            case KFSConstants.MONTH4:
                currentBalanceAmount = currentBalanceAmount.add(glBalance.getMonth3Amount());
                // fall through
            case KFSConstants.MONTH3:
                currentBalanceAmount = currentBalanceAmount.add(glBalance.getMonth2Amount());
                // fall through
            case KFSConstants.MONTH2:
                currentBalanceAmount = currentBalanceAmount.add(glBalance.getMonth1Amount());
                break;
            default:
                break;
        }

        return glBalance.getContractsGrantsBeginningBalanceAmount().add(currentBalanceAmount);
    }

    @Override
    public void updateSuspensionCategoriesOnDocument(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument) {
        if (!contractsGrantsInvoiceDocument.isCorrectionDocument()) {
            String documentNumber = contractsGrantsInvoiceDocument.getDocumentNumber();

            if (ObjectUtils.isNotNull(suspensionCategories)) {
                for (SuspensionCategory suspensionCategory : suspensionCategories) {
                    InvoiceSuspensionCategory invoiceSuspensionCategory = new InvoiceSuspensionCategory(documentNumber,
                            suspensionCategory.getCode());
                    if (suspensionCategory.shouldSuspend(contractsGrantsInvoiceDocument)) {
                        if (!contractsGrantsInvoiceDocument.getInvoiceSuspensionCategories().contains(
                                invoiceSuspensionCategory)) {
                            contractsGrantsInvoiceDocument.getInvoiceSuspensionCategories().add(
                                    invoiceSuspensionCategory);
                        }
                    } else {
                        contractsGrantsInvoiceDocument.getInvoiceSuspensionCategories().remove(
                                invoiceSuspensionCategory);
                    }
                }
            }
        }
    }

    @Override
    public KualiDecimal calculateTotalPaymentsToDateByAward(ContractsAndGrantsBillingAward award) {
        KualiDecimal totalPayments = KualiDecimal.ZERO;

        Map<String, Object> criteria = new HashMap<>();
        criteria.put(ArPropertyConstants.ContractsGrantsInvoiceDocumentFields.PROPOSAL_NUMBER,
                award.getProposalNumber());
        Collection<ContractsGrantsInvoiceDocument> cgInvoiceDocs = businessObjectService.findMatching(
                ContractsGrantsInvoiceDocument.class, criteria);

        for (ContractsGrantsInvoiceDocument cgInvoiceDoc : cgInvoiceDocs) {
            totalPayments = totalPayments.add(
                    getCustomerInvoiceDocumentService().calculateAppliedPaymentAmount(cgInvoiceDoc));
        }
        return totalPayments;
    }

    public KualiDecimal getMilestonesBilledToDateAmount(String proposalNumber) {
        Map<String, Object> totalBilledKeys = new HashMap<>();
        totalBilledKeys.put(KFSPropertyConstants.PROPOSAL_NUMBER, proposalNumber);
        List<Milestone> milestones = (List<Milestone>) businessObjectService.findMatching(Milestone.class,
                totalBilledKeys);

        return calculateBilledToDateAmountForMilestones(milestones);
    }

    @Override
    public KualiDecimal getMilestonesBilledToDateAmount(String proposalNumber, String chartOfAccountsCode,
            String accountNumber) {
        Map<String, Object> totalBilledKeys = new HashMap<>();
        totalBilledKeys.put(KFSPropertyConstants.PROPOSAL_NUMBER, proposalNumber);
        totalBilledKeys.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartOfAccountsCode);
        totalBilledKeys.put(KFSPropertyConstants.ACCOUNT_NUMBER, accountNumber);
        List<Milestone> milestones = (List<Milestone>) businessObjectService.findMatching(Milestone.class,
                totalBilledKeys);

        return calculateBilledToDateAmountForMilestones(milestones);
    }

    private KualiDecimal calculateBilledToDateAmountForMilestones(List<Milestone> milestones) {
        return milestones.stream()
                .filter(Milestone::isBilled)
                .filter(milestone -> ObjectUtils.isNotNull(milestone.getMilestoneAmount()))
                .reduce(KualiDecimal.ZERO, (sum, milestone) ->
                        milestone.getMilestoneAmount().add(sum), AbstractKualiDecimal::add);
    }

    public KualiDecimal getPredeterminedBillingBilledToDateAmount(String proposalNumber) {
        Map<String, Object> totalBilledKeys = new HashMap<>();
        totalBilledKeys.put(KFSPropertyConstants.PROPOSAL_NUMBER, proposalNumber);
        List<Bill> bills = (List<Bill>) businessObjectService.findMatching(Bill.class, totalBilledKeys);

        return calculateBilledToDateAmountForBills(bills);
    }

    @Override
    public KualiDecimal getPredeterminedBillingBilledToDateAmount(String proposalNumber, String chartOfAccountsCode,
            String accountNumber) {
        Map<String, Object> totalBilledKeys = new HashMap<>();
        totalBilledKeys.put(KFSPropertyConstants.PROPOSAL_NUMBER, proposalNumber);
        totalBilledKeys.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartOfAccountsCode);
        totalBilledKeys.put(KFSPropertyConstants.ACCOUNT_NUMBER, accountNumber);
        List<Bill> bills = (List<Bill>) businessObjectService.findMatching(Bill.class, totalBilledKeys);

        return calculateBilledToDateAmountForBills(bills);
    }

    private KualiDecimal calculateBilledToDateAmountForBills(List<Bill> bills) {
        return bills.stream()
                .filter(Bill::isBilled)
                .filter(bill -> ObjectUtils.isNotNull(bill.getEstimatedAmount()))
                .reduce(KualiDecimal.ZERO, (sum, bill) ->
                        bill.getEstimatedAmount().add(sum), AbstractKualiDecimal::add);
    }

    @Override
    public List<Account> getContractControlAccounts(ContractsAndGrantsBillingAward award) {

        if (!CollectionUtils.isEmpty(award.getActiveAwardAccounts())) {
            List<Account> controlAccounts = new ArrayList<>();
            for (ContractsAndGrantsBillingAwardAccount awardAccount : award.getActiveAwardAccounts()) {
                if (ObjectUtils.isNotNull(awardAccount.getAccount())
                    && ObjectUtils.isNotNull(awardAccount.getAccount().getContractControlAccount())) {
                    controlAccounts.add(awardAccount.getAccount().getContractControlAccount());
                }
            }
            if (CollectionUtils.isNotEmpty(controlAccounts)) {
                return controlAccounts;
            }
        }
        return null;
    }

    @Override
    public List<String> getProcessingFromBillingCodes(String billingChartCode, String billingOrgCode) {
        List<String> procCodes = new ArrayList<>();
        // To access Organization Options to find the billing values based on processing codes
        Map<String, String> criteria = new HashMap<>();
        criteria.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, billingChartCode);
        criteria.put(KFSPropertyConstants.ORGANIZATION_CODE, billingOrgCode);
        OrganizationOptions organizationOptions = businessObjectService.findByPrimaryKey(OrganizationOptions.class,
                criteria);

        if (ObjectUtils.isNotNull(organizationOptions)) {
            procCodes.add(0, organizationOptions.getProcessingChartOfAccountCode());
            procCodes.add(1, organizationOptions.getProcessingOrganizationCode());
        }

        return procCodes;
    }

    @Override
    public boolean canViewInvoice(ContractsGrantsInvoiceDocument invoice, String collectorPrincipalId) {
        Map<String, String> qualification = new HashMap<>(3);
        qualification.put(ArKimAttributes.BILLING_CHART_OF_ACCOUNTS_CODE, invoice.getBillByChartOfAccountCode());
        qualification.put(ArKimAttributes.BILLING_ORGANIZATION_CODE, invoice.getBilledByOrganizationCode());
        qualification.put(ArKimAttributes.PROCESSING_CHART_OF_ACCOUNTS_CODE,
                invoice.getAccountsReceivableDocumentHeader().getProcessingChartOfAccountCode());
        qualification.put(ArKimAttributes.PROCESSING_ORGANIZATION_CODE,
                invoice.getAccountsReceivableDocumentHeader().getProcessingOrganizationCode());

        String customerName = invoice.getCustomerName();
        if (StringUtils.isNotBlank(customerName)) {
            qualification.put(ArKimAttributes.CUSTOMER_NAME, customerName);
        }

        return getPermissionService().isAuthorized(collectorPrincipalId, ArConstants.AR_NAMESPACE_CODE,
                ArAuthorizationConstants.VIEW_CONTRACTS_GRANTS_INVOICE_IN_BILLING_REPORTS_PERMISSION, qualification);
    }

    @Override
    public void generateInvoicesForInvoiceAddresses(ContractsGrantsInvoiceDocument document) {
        InvoiceTemplate invoiceTemplate;
        byte[] reportStream;
        InvoiceGeneralDetail invoiceGeneralDetail = document.getInvoiceGeneralDetail();
        if (ObjectUtils.isNotNull(invoiceGeneralDetail.getCustomerInvoiceTemplateCode())) {
            CustomerAddress customerAddress = invoiceGeneralDetail.getCustomerAddress();
            String customerAddressName = customerAddress.getCustomerAddressName();
            invoiceTemplate = businessObjectService.findBySinglePrimaryKey(InvoiceTemplate.class,
                    invoiceGeneralDetail.getCustomerInvoiceTemplateCode());

            if (ObjectUtils.isNotNull(invoiceTemplate) && invoiceTemplate.isActive()
                    && StringUtils.isNotBlank(invoiceTemplate.getFilename())) {
                ModuleConfiguration systemConfiguration = kualiModuleService.getModuleServiceByNamespaceCode(
                        KFSConstants.OptionalModuleNamespaces.ACCOUNTS_RECEIVABLE).getModuleConfiguration();
                String templateFolderPath = ((FinancialSystemModuleConfiguration) systemConfiguration)
                        .getTemplateFileDirectories().get(KFSConstants.TEMPLATES_DIRECTORY_KEY);
                String templateFilePath = templateFolderPath + File.separator + invoiceTemplate.getFilename();
                File templateFile = new File(templateFilePath);
                String outputFileName;
                try {
                    Map<String, String> replacementList = getTemplateParameterList(document);
                    reportStream = PdfFormFillerUtil.populateTemplate(templateFile, replacementList);
                    outputFileName = buildFilenamePrefix(document, customerAddressName) +
                            ArConstants.TemplateUploadSystem.EXTENSION;
                    String watermarkText = null;
                    if (ObjectUtils.isNotNull(document.getInvoiceGeneralDetail()) && document
                            .getInvoiceGeneralDetail().isFinalBillIndicator()) {
                        watermarkText = getConfigurationService().getPropertyValueAsString(
                                ArKeyConstants.INVOICE_ADDRESS_PDF_WATERMARK_FINAL);
                    }
                    Long noteId = buildAndAddInvoiceNote(document, reportStream, customerAddressName, outputFileName,
                            ArKeyConstants.INVOICE_ADDRESS_PDF_FINAL_NOTE, watermarkText);
                    document.getInvoiceGeneralDetail().setInvoiceNoteId(noteId);

                    documentService.updateDocument(document);
                } catch (IOException ex) {
                    addNoteForInvoiceReportFail(document);
                }
            } else {
                addNoteForInvoiceReportFail(document);
            }
        } else {
            addNoteForInvoiceReportFail(document);
        }
    }

    private String buildFilenamePrefix(ContractsGrantsInvoiceDocument document, String customerAddressName) {
        return document.getDocumentNumber() + "_" + customerAddressName + getDateTimeService()
                .toDateStringForFilename(getDateTimeService().getCurrentDate());
    }

    private Long buildAndAddInvoiceNote(ContractsGrantsInvoiceDocument document, byte[] reportStream,
            String customerAddressName, String outputFileName, String invoiceAddressPdfFinalNote,
            String watermarkText) throws IOException {
        Note note = new Note();
        note.setNotePostedTimestampToCurrent();
        final String finalNotePattern = getConfigurationService().getPropertyValueAsString(invoiceAddressPdfFinalNote);
        note.setNoteText(MessageFormat.format(finalNotePattern, document.getDocumentNumber(), customerAddressName));
        note.setNoteTypeCode(KFSConstants.NoteTypeEnum.BUSINESS_OBJECT_NOTE_TYPE.getCode());
        Person systemUser = personService.getPersonByPrincipalName(KFSConstants.SYSTEM_USER);
        note = noteService.createNote(note, document.getNoteTarget(), systemUser.getPrincipalId());
        if (StringUtils.isNotBlank(watermarkText)) {
            try {
                reportStream = PdfFormFillerUtil.createWatermarkOnFile(reportStream, watermarkText);
            } catch (DocumentException e) {
                addNoteForInvoiceReportFail(document);
            }
        }
        Attachment attachment = attachmentService.createAttachment(note, outputFileName,
                ArConstants.TemplateUploadSystem.TEMPLATE_MIME_TYPE, reportStream.length,
                new ByteArrayInputStream(reportStream), KFSConstants.EMPTY_STRING);
        note.setAttachment(attachment);
        noteService.save(note);
        attachment.setNoteIdentifier(note.getNoteIdentifier());
        businessObjectService.save(attachment);
        document.addNote(note);

        return note.getNoteIdentifier();
    }

    /**
     * This method generates the template parameter list to populate the pdf invoices that are attached to the Document.
     *
     * @param document the ContractsGrantsInvoiceDocument to convert into a Map form
     * @return a Map with fields from the ContractsGrantsInvoiceDocument to be used to populate pdf invoices.
     */
    protected Map<String, String> getTemplateParameterList(ContractsGrantsInvoiceDocument document) {
        Map<String, Object> parameterMap = new HashMap<>();

        parameterMap.putAll(buildAccountDetailFieldsMap(document));
        parameterMap.putAll(buildCustomerFieldsMap(document));
        parameterMap.putAll(buildDocumentFieldsMap(document));
        parameterMap.putAll(buildDirectCostInvoiceDetailFieldsMap(document));
        parameterMap.putAll(buildTotalDirectCostInvoiceDetailFieldsMap(document));
        parameterMap.putAll(buildTotalIndirectCostInvoiceDetailFieldsMap(document));
        parameterMap.putAll(buildTotalCostInvoiceDetailFieldsMap(document));
        parameterMap.putAll(buildInvoiceGeneralDetailFieldsMap(document));
        parameterMap.putAll(buildInvoiceBillFieldsMap(document));
        parameterMap.putAll(buildInvoiceMilestoneFieldsMap(document));
        parameterMap.putAll(buildPayeeFieldsMap(document));
        parameterMap.putAll(buildSystemInformationMap(document));

        return parameterMap.keySet().stream()
                .collect(Collectors.toMap(key -> key, key -> stringifyValue(parameterMap.get(key)), (a, b) -> b));
    }

    private Map<String, Object> buildAccountDetailFieldsMap(ContractsGrantsInvoiceDocument document) {
        Map<String, Object> accountDetailFieldsMap = new HashMap<>();

        List<InvoiceAccountDetail> accountDetails = document.getAccountDetails();
        if (CollectionUtils.isNotEmpty(accountDetails)) {
            accountDetailFieldsMap.put(ArPropertyConstants.ACCOUNT_DETAILS + "." +
                            KFSPropertyConstants.CONTRACT_CONTROL_ACCOUNT_NUMBER,
                    determineContractControlAccountNumber(document));

            for (int i = 0; i < accountDetails.size(); i++) {
                final String prefix = ArPropertyConstants.ACCOUNT_DETAILS + "[" + i + "].";
                accountDetailFieldsMap.put(prefix + KFSPropertyConstants.ACCOUNT_NUMBER,
                        accountDetails.get(i).getAccountNumber());
                accountDetailFieldsMap.put(prefix + KFSPropertyConstants.PROPOSAL_NUMBER,
                        accountDetails.get(i).getProposalNumber());
                accountDetailFieldsMap.put(prefix + KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE,
                        accountDetails.get(i).getChartOfAccountsCode());
                accountDetailFieldsMap.put(prefix + ArPropertyConstants.TOTAL_BUDGET,
                        accountDetails.get(i).getTotalBudget());
                accountDetailFieldsMap.put(prefix + ArPropertyConstants.INVOICE_AMOUNT,
                        accountDetails.get(i).getInvoiceAmount());
                accountDetailFieldsMap.put(prefix + ArPropertyConstants.BUDGET_REMAINING,
                        accountDetails.get(i).getBudgetRemaining());
            }
        }
        return accountDetailFieldsMap;
    }

    // access relaxed for Cornell
    protected String determineContractControlAccountNumber(ContractsGrantsInvoiceDocument document) {
        final InvoiceGeneralDetail invoiceGeneralDetail = document.getInvoiceGeneralDetail();
        if (ObjectUtils.isNotNull(invoiceGeneralDetail)) {
            final ContractsAndGrantsBillingAward award = invoiceGeneralDetail.getAward();
            if (ObjectUtils.isNotNull(award)) {
                final List<Account> contractControlAccounts = getContractControlAccounts(award);
                if (CollectionUtils.isNotEmpty(contractControlAccounts)) {
                    return contractControlAccounts.get(0).getAccountNumber();
                }
            }
        }

        final List<InvoiceAccountDetail> accountDetails = document.getAccountDetails();
        if (CollectionUtils.isNotEmpty(accountDetails)) {
            return accountDetails.get(0).getAccountNumber();
        }

        return null;
    }

    private Map<String, Object> buildCustomerFieldsMap(ContractsGrantsInvoiceDocument document) {
        Map<String, Object> customerFieldsMap = new HashMap<>();

        Customer customer = document.getCustomer();
        if (ObjectUtils.isNotNull(customer)) {
            customerFieldsMap.put(ArPropertyConstants.CustomerInvoiceDocumentFields.CUSTOMER + "." +
                    KFSPropertyConstants.CUSTOMER_NAME, customer.getCustomerName());
        }

        return customerFieldsMap;
    }

    private Map<String, Object> buildDocumentFieldsMap(ContractsGrantsInvoiceDocument document) {
        Map<String, Object> documentFieldsMap = new HashMap<>();

        documentFieldsMap.put(KFSPropertyConstants.DOCUMENT_NUMBER, document.getDocumentNumber());
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        if (ObjectUtils.isNotNull(workflowDocument.getDateCreated())) {
            documentFieldsMap.put(KFSPropertyConstants.DATE, workflowDocument.getDateCreated().toDate());
        }
        if (ObjectUtils.isNotNull(workflowDocument.getDateApproved())) {
            documentFieldsMap.put(ArPropertyConstants.FINAL_STATUS_DATE, workflowDocument.getDateApproved().toDate());
        }

        return documentFieldsMap;
    }

    private Map<String, Object> buildDirectCostInvoiceDetailFieldsMap(ContractsGrantsInvoiceDocument document) {
        Map<String, Object> directCostInvoiceDetailFieldsMap = new HashMap<>();

        if (CollectionUtils.isNotEmpty(document.getDirectCostInvoiceDetails())) {
            ContractsGrantsInvoiceDetail firstInvoiceDetail = document.getDirectCostInvoiceDetails().get(0);

            for (int i = 0; i < document.getDirectCostInvoiceDetails().size(); i++) {
                final String prefix = ArPropertyConstants.INVOICE_DETAIL + "[" + i + "].";
                directCostInvoiceDetailFieldsMap.put(prefix + ArPropertyConstants.CATEGORY,
                        document.getDirectCostInvoiceDetails().get(i).getCostCategory().getCategoryName());
                directCostInvoiceDetailFieldsMap.putAll(buildInvoiceDetailFieldMap(prefix,
                        document.getDirectCostInvoiceDetails().get(i)));
                directCostInvoiceDetailFieldsMap.put(prefix + ArPropertyConstants.AMOUNT_REMAINING_TO_BILL,
                        firstInvoiceDetail.getAmountRemainingToBill());
            }
        }

        return directCostInvoiceDetailFieldsMap;
    }

    private Map<String, Object> buildInvoiceDetailFieldMap(String prefix,
            ContractsGrantsInvoiceDetail contractsGrantsInvoiceDetail) {
        Map<String, Object> invoiceDetailFieldMap = new HashMap<>();

        invoiceDetailFieldMap.put(prefix + ArPropertyConstants.TOTAL_BUDGET,
                contractsGrantsInvoiceDetail.getTotalBudget());
        invoiceDetailFieldMap.put(prefix + ArPropertyConstants.INVOICE_AMOUNT,
                contractsGrantsInvoiceDetail.getInvoiceAmount());
        invoiceDetailFieldMap.put(prefix + ArPropertyConstants.CUMULATIVE_EXPENDITURES,
                contractsGrantsInvoiceDetail.getCumulativeExpenditures());
        invoiceDetailFieldMap.put(prefix + ArPropertyConstants.BUDGET_REMAINING,
                contractsGrantsInvoiceDetail.getBudgetRemaining());
        invoiceDetailFieldMap.put(prefix + ArPropertyConstants.TOTAL_PREVIOUSLY_BILLED,
                contractsGrantsInvoiceDetail.getTotalPreviouslyBilled());
        invoiceDetailFieldMap.put(prefix + ArPropertyConstants.TOTAL_AMOUNT_BILLED_TO_DATE,
                contractsGrantsInvoiceDetail.getTotalAmountBilledToDate());
        invoiceDetailFieldMap.put(prefix + ArPropertyConstants.AMOUNT_REMAINING_TO_BILL,
                contractsGrantsInvoiceDetail.getAmountRemainingToBill());

        return invoiceDetailFieldMap;
    }

    private Map<String, Object> buildTotalDirectCostInvoiceDetailFieldsMap(ContractsGrantsInvoiceDocument document) {
        Map<String, Object> totalDirectCostInvoiceDetailFieldsMap = new HashMap<>();

        ContractsGrantsInvoiceDetail totalDirectCostInvoiceDetail = document.getTotalDirectCostInvoiceDetail();
        if (ObjectUtils.isNotNull(totalDirectCostInvoiceDetail)) {
            final String prefix = ArPropertyConstants.DIRECT_COST_INVOICE_DETAIL + ".";
            totalDirectCostInvoiceDetailFieldsMap.putAll(buildInvoiceDetailFieldMap(prefix,
                    totalDirectCostInvoiceDetail));
        }

        return totalDirectCostInvoiceDetailFieldsMap;
    }

    private Map<String, Object> buildTotalIndirectCostInvoiceDetailFieldsMap(ContractsGrantsInvoiceDocument document) {
        Map<String, Object> totalIndirectCostInvoiceDetailFieldsMap = new HashMap<>();

        ContractsGrantsInvoiceDetail totalInDirectCostInvoiceDetail = document.getTotalIndirectCostInvoiceDetail();
        if (ObjectUtils.isNotNull(totalInDirectCostInvoiceDetail)) {
            final String prefix = ArPropertyConstants.IN_DIRECT_COST_INVOICE_DETAIL + ".";
            totalIndirectCostInvoiceDetailFieldsMap.putAll(buildInvoiceDetailFieldMap(prefix,
                    totalInDirectCostInvoiceDetail));
        }

        return totalIndirectCostInvoiceDetailFieldsMap;
    }

    private Map<String, Object> buildTotalCostInvoiceDetailFieldsMap(ContractsGrantsInvoiceDocument document) {
        Map<String, Object> totalCostInvoiceDetailFieldsMap = new HashMap<>();

        ContractsGrantsInvoiceDetail totalCostInvoiceDetail = document.getTotalCostInvoiceDetail();
        if (ObjectUtils.isNotNull(totalCostInvoiceDetail)) {
            final String prefix = ArPropertyConstants.TOTAL_INVOICE_DETAIL + ".";
            totalCostInvoiceDetailFieldsMap.putAll(buildInvoiceDetailFieldMap(prefix, totalCostInvoiceDetail));
        }

        return totalCostInvoiceDetailFieldsMap;
    }

    private Map<String, Object> buildInvoiceGeneralDetailFieldsMap(ContractsGrantsInvoiceDocument document) {
        Map<String, Object> invoiceGeneralDetailFieldsMap = new HashMap<>();

        InvoiceGeneralDetail invoiceGeneralDetail = document.getInvoiceGeneralDetail();
        if (ObjectUtils.isNotNull(invoiceGeneralDetail)) {
            invoiceGeneralDetailFieldsMap.put(ArPropertyConstants.ADVANCE_FLAG,
                    ArConstants.BillingFrequencyValues.isPredeterminedBilling(invoiceGeneralDetail));
            invoiceGeneralDetailFieldsMap.put(ArPropertyConstants.REIMBURSEMENT_FLAG,
                    !ArConstants.BillingFrequencyValues.isPredeterminedBilling(invoiceGeneralDetail));
            invoiceGeneralDetailFieldsMap.put(KFSPropertyConstants.PROPOSAL_NUMBER,
                    invoiceGeneralDetail.getProposalNumber());
            final String prefix = ArPropertyConstants.INVOICE_GENERAL_DETAIL + ".";
            invoiceGeneralDetailFieldsMap.put(prefix + ArPropertyConstants.AWARD_DATE_RANGE,
                    invoiceGeneralDetail.getAwardDateRange());
            invoiceGeneralDetailFieldsMap.put(prefix + ArPropertyConstants.BILLING_FREQUENCY_CODE,
                    invoiceGeneralDetail.getBillingFrequencyCode());
            invoiceGeneralDetailFieldsMap.put(ArPropertyConstants.ContractsGrantsInvoiceDocumentFields.FINAL_BILL,
                    invoiceGeneralDetail.isFinalBillIndicator());
            invoiceGeneralDetailFieldsMap.put(prefix + ArPropertyConstants.BILLING_PERIOD,
                    invoiceGeneralDetail.getBillingPeriod());
            invoiceGeneralDetailFieldsMap.put(prefix + ArPropertyConstants.INSTRUMENT_TYPE_CODE,
                    invoiceGeneralDetail.getInstrumentTypeCode());
            invoiceGeneralDetailFieldsMap.put(prefix + ArConstants.AWARD_TOTAL, invoiceGeneralDetail.getAwardTotal());
            invoiceGeneralDetailFieldsMap.put(prefix + ArPropertyConstants.AMOUNT_REMAINING_TO_BILL,
                    invoiceGeneralDetail.getAmountRemainingToBill());
            invoiceGeneralDetailFieldsMap.put(prefix + ArPropertyConstants.COST_SHARE_AMOUNT,
                    invoiceGeneralDetail.getCostShareAmount());
            invoiceGeneralDetailFieldsMap.put(prefix + ArPropertyConstants.LAST_BILLED_DATE,
                    invoiceGeneralDetail.getLastBilledDate());
            invoiceGeneralDetailFieldsMap.put(prefix + ArPropertyConstants.TOTAL_PREVIOUSLY_BILLED,
                    invoiceGeneralDetail.getTotalPreviouslyBilled());
            invoiceGeneralDetailFieldsMap.put(ArPropertyConstants.TOTAL_AMOUNT_DUE, getTotalAmountForInvoice(document));
            invoiceGeneralDetailFieldsMap.put(ArPropertyConstants.CustomerInvoiceDocumentFields.CUSTOMER + "." +
                            ArPropertyConstants.FULL_ADDRESS,
                    contractsGrantsBillingUtilityService.buildFullAddress(invoiceGeneralDetail.getCustomerAddress()));

            ContractsAndGrantsBillingAward award = invoiceGeneralDetail.getAward();
            if (ObjectUtils.isNotNull(award)) {
                invoiceGeneralDetailFieldsMap.putAll(buildAwardFieldsMap(award));
            }
        }
        return invoiceGeneralDetailFieldsMap;
    }

    private Map<String, Object> buildAwardFieldsMap(ContractsAndGrantsBillingAward award) {
        Map<String, Object> awardFieldsMap = new HashMap<>();

        KualiDecimal billing = getAwardBilledToDateAmount(award.getProposalNumber());
        KualiDecimal payments = calculateTotalPaymentsToDateByAward(award);
        KualiDecimal receivable = billing.subtract(payments);
        awardFieldsMap.put(KFSPropertyConstants.AWARD + "." + ArPropertyConstants.BILLINGS, billing);
        awardFieldsMap.put(KFSPropertyConstants.AWARD + "." + ArPropertyConstants.PAYMENTS, payments);
        awardFieldsMap.put(KFSPropertyConstants.AWARD + "." + ArPropertyConstants.RECEIVABLES, receivable);
        awardFieldsMap.put(KFSPropertyConstants.AWARD + "." + KFSPropertyConstants.PROPOSAL_NUMBER,
                award.getProposalNumber());
        if (ObjectUtils.isNotNull(award.getAwardBeginningDate())) {
            awardFieldsMap.put(KFSPropertyConstants.AWARD + "." +
                    KFSPropertyConstants.AWARD_BEGINNING_DATE, award.getAwardBeginningDate());
        }
        if (ObjectUtils.isNotNull(award.getAwardEndingDate())) {
            awardFieldsMap.put(KFSPropertyConstants.AWARD + "." + KFSPropertyConstants.AWARD_ENDING_DATE,
                    award.getAwardEndingDate());
        }
        awardFieldsMap.put(KFSPropertyConstants.AWARD + "." + KFSPropertyConstants.AWARD_TOTAL_AMOUNT,
                award.getAwardTotalAmount());
        awardFieldsMap.put(KFSPropertyConstants.AWARD + "." +
                        ArPropertyConstants.ContractsAndGrantsBillingAwardFields.AWARD_ADDENDUM_NUMBER,
                award.getAwardAddendumNumber());
        awardFieldsMap.put(KFSPropertyConstants.AWARD + "." + ArPropertyConstants.ContractsAndGrantsBillingAwardFields
                        .AWARD_ALLOCATED_UNIVERSITY_COMPUTING_SERVICES_AMOUNT,
                award.getAwardAllocatedUniversityComputingServicesAmount());
        awardFieldsMap.put(KFSPropertyConstants.AWARD + "." + KFSPropertyConstants.FEDERAL_PASS_THROUGH_FUNDED_AMOUNT,
                award.getFederalPassThroughFundedAmount());
        if (ObjectUtils.isNotNull(award.getAwardEntryDate())) {
            awardFieldsMap.put(KFSPropertyConstants.AWARD + "." + KFSPropertyConstants.AWARD_ENTRY_DATE,
                    award.getAwardEntryDate());
        }
        awardFieldsMap.put(KFSPropertyConstants.AWARD + "." +
                        ArPropertyConstants.ContractsAndGrantsBillingAwardFields.AGENCY_FUTURE_1_AMOUNT,
                award.getAgencyFuture1Amount());
        awardFieldsMap.put(KFSPropertyConstants.AWARD + "." +
                        ArPropertyConstants.ContractsAndGrantsBillingAwardFields.AGENCY_FUTURE_2_AMOUNT,
                award.getAgencyFuture2Amount());
        awardFieldsMap.put(KFSPropertyConstants.AWARD + "." +
                        ArPropertyConstants.ContractsAndGrantsBillingAwardFields.AGENCY_FUTURE_3_AMOUNT,
                award.getAgencyFuture3Amount());
        awardFieldsMap.put(KFSPropertyConstants.AWARD + "." + KFSPropertyConstants.AWARD_DOCUMENT_NUMBER,
                award.getAwardDocumentNumber());
        if (ObjectUtils.isNotNull(award.getAwardLastUpdateDate())) {
            awardFieldsMap.put(KFSPropertyConstants.AWARD + "." +
                            ArPropertyConstants.ContractsAndGrantsBillingAwardFields.AWARD_LAST_UPDATE_DATE,
                    award.getAwardLastUpdateDate());
        }
        awardFieldsMap.put(KFSPropertyConstants.AWARD + "." + KFSPropertyConstants.FEDERAL_PASS_THROUGH_INDICATOR,
                award.getFederalPassThroughIndicator());
        awardFieldsMap.put(KFSPropertyConstants.AWARD + "." +
                        ArPropertyConstants.ContractsAndGrantsBillingAwardFields.AWARD_DIRECT_COST_AMOUNT,
                award.getAwardDirectCostAmount());
        awardFieldsMap.put(KFSPropertyConstants.AWARD + "." +
                        ArPropertyConstants.ContractsAndGrantsBillingAwardFields.AWARD_INDIRECT_COST_AMOUNT,
                award.getAwardIndirectCostAmount());
        awardFieldsMap.put(KFSPropertyConstants.AWARD + "." + KFSPropertyConstants.GRANT_DESCRIPTION_CODE,
                award.getGrantDescriptionCode());
        final ContractAndGrantsProposal proposal = award.getProposal();
        if (ObjectUtils.isNotNull(proposal)) {
            awardFieldsMap.put(KFSPropertyConstants.AWARD + "." +
                            ArPropertyConstants.ContractsAndGrantsBillingAwardFields.GRANT_NUMBER,
                    proposal.getGrantNumber());
        }
        awardFieldsMap.put(KFSPropertyConstants.AGENCY_NUMBER, award.getAgencyNumber());
        awardFieldsMap.put(KFSPropertyConstants.AGENCY + "." + KFSPropertyConstants.FULL_NAME,
                award.getAgency().getFullName());
        awardFieldsMap.put(KFSPropertyConstants.AWARD + "." + KFSPropertyConstants.FEDERAL_PASS_THROUGH_AGENCY_NUMBER,
                award.getFederalPassThroughAgencyNumber());
        awardFieldsMap.put(KFSPropertyConstants.AWARD + "." +
                        ArPropertyConstants.ContractsAndGrantsBillingAwardFields.AGENCY_ANALYST_NAME,
                award.getAgencyAnalystName());
        awardFieldsMap.put(KFSPropertyConstants.AWARD + "." +
                        ArPropertyConstants.ContractsAndGrantsBillingAwardFields.ANALYST_TELEPHONE_NUMBER,
                award.getAnalystTelephoneNumber());
        awardFieldsMap.put(KFSPropertyConstants.AWARD + "." + ArPropertyConstants.BILLING_FREQUENCY_CODE,
                award.getBillingFrequencyCode());
        awardFieldsMap.put(KFSPropertyConstants.AWARD + "." + KFSPropertyConstants.AWARD_PROJECT_TITLE,
                award.getAwardProjectTitle());
        awardFieldsMap.put(KFSPropertyConstants.AWARD + "." +
                        ArPropertyConstants.ContractsAndGrantsBillingAwardFields.AWARD_PURPOSE_CODE,
                award.getAwardPurposeCode());
        awardFieldsMap.put(KFSPropertyConstants.AWARD + "." + KFSPropertyConstants.ACTIVE,
                award.isActive());
        awardFieldsMap.put(KFSPropertyConstants.AWARD + "." +
                        ArPropertyConstants.ContractsAndGrantsBillingAwardFields.EXCLUDED_FROM_INVOICING,
                award.isExcludedFromInvoicing());
        awardFieldsMap.put(KFSPropertyConstants.AWARD + "." +
                        ArPropertyConstants.ContractsAndGrantsBillingAwardFields.ADDITIONAL_FORMS_REQUIRED,
                award.isAdditionalFormsRequiredIndicator());
        awardFieldsMap.put(KFSPropertyConstants.AWARD + "." +
                        ArPropertyConstants.ContractsAndGrantsBillingAwardFields.ADDITIONAL_FORMS_DESCRIPTION,
                award.getAdditionalFormsDescription());
        awardFieldsMap.put(KFSPropertyConstants.AWARD + "." + ArPropertyConstants.INSTRUMENT_TYPE_CODE,
                award.getInstrumentTypeCode());
        awardFieldsMap.put(KFSPropertyConstants.AWARD + "." +
                        ArPropertyConstants.ContractsAndGrantsBillingAwardFields.MIN_INVOICE_AMOUNT,
                award.getMinInvoiceAmount());
        awardFieldsMap.put(KFSPropertyConstants.AWARD + "." +
                        ArPropertyConstants.ContractsAndGrantsBillingAwardFields.AUTO_APPROVE,
                award.getAutoApproveIndicator());
        awardFieldsMap.put(KFSPropertyConstants.AWARD + "." +
                        ArPropertyConstants.ContractsAndGrantsBillingAwardFields.FUNDING_EXPIRATION_DATE,
                award.getFundingExpirationDate());
        if (ObjectUtils.isNotNull(award.getAwardPrimaryProjectDirector())) {
            awardFieldsMap.put(KFSPropertyConstants.AWARD + "." + ArConstants.AWARD_PROJECT_DIRECTOR + "." +
                            KFSPropertyConstants.NAME,
                    award.getAwardPrimaryProjectDirector().getProjectDirector().getName());
        }
        if (ObjectUtils.isNotNull(award.getAwardPrimaryFundManager())) {
            final Person fundManager = award.getAwardPrimaryFundManager().getFundManager();
            awardFieldsMap.put(KFSPropertyConstants.AWARD + "." +
                    ArPropertyConstants.ContractsAndGrantsBillingAwardFields.PRIMARY_FUND_MANAGER + "." +
                    KFSPropertyConstants.NAME, fundManager.getName());
            awardFieldsMap.put(KFSPropertyConstants.AWARD + "." +
                            ArPropertyConstants.ContractsAndGrantsBillingAwardFields.PRIMARY_FUND_MANAGER + "." +
                            ArPropertyConstants.ContractsAndGrantsBillingAwardFields.EMAIL,
                    fundManager.getEmailAddress());
            awardFieldsMap.put(KFSPropertyConstants.AWARD + "." +
                            ArPropertyConstants.ContractsAndGrantsBillingAwardFields.PRIMARY_FUND_MANAGER + "." +
                            ArPropertyConstants.ContractsAndGrantsBillingAwardFields.PHONE,
                    fundManager.getPhoneNumber());
        }

        return awardFieldsMap;
    }

    private Map<String, Object> buildInvoiceBillFieldsMap(ContractsGrantsInvoiceDocument document) {
        Map<String, Object> invoiceBillFieldsMap = new HashMap<>();

        if (CollectionUtils.isNotEmpty(document.getInvoiceBills())) {
            for (int i = 0; i < document.getInvoiceBills().size(); i++) {
                final String prefix = ArPropertyConstants.INVOICE_BILLS + "[" + i + "].";
                invoiceBillFieldsMap.put(prefix + ArPropertyConstants.BillFields.BILL_NUMBER,
                        document.getInvoiceBills().get(i).getBillNumber());
                invoiceBillFieldsMap.put(prefix + ArPropertyConstants.BillFields.BILL_DESCRIPTION,
                        document.getInvoiceBills().get(i).getBillDescription());
                invoiceBillFieldsMap.put(prefix + ArPropertyConstants.BillFields.BILL_DATE,
                        document.getInvoiceBills().get(i).getBillDate());
                invoiceBillFieldsMap.put(prefix + ArPropertyConstants.BillFields.ESTIMATED_AMOUNT,
                        document.getInvoiceBills().get(i).getEstimatedAmount());
            }
        }

        return invoiceBillFieldsMap;
    }

    private Map<String, Object> buildInvoiceMilestoneFieldsMap(ContractsGrantsInvoiceDocument document) {
        Map<String, Object> invoiceMilestoneFieldsMap = new HashMap<>();

        if (CollectionUtils.isNotEmpty(document.getInvoiceMilestones())) {
            for (int i = 0; i < document.getInvoiceMilestones().size(); i++) {
                final String prefix = ArPropertyConstants.INVOICE_MILESTONES + "[" + i + "].";
                invoiceMilestoneFieldsMap.put(prefix + ArPropertyConstants.MilestoneFields.MILESTONE_NUMBER,
                        document.getInvoiceMilestones().get(i).getMilestoneNumber());
                invoiceMilestoneFieldsMap.put(prefix + ArPropertyConstants.MilestoneFields.MILESTONE_DESCRIPTION,
                        document.getInvoiceMilestones().get(i).getMilestoneDescription());
                invoiceMilestoneFieldsMap.put(prefix + ArPropertyConstants.MilestoneFields.MILESTONE_AMOUNT,
                        document.getInvoiceMilestones().get(i).getMilestoneAmount());
                invoiceMilestoneFieldsMap.put(prefix +
                                ArPropertyConstants.MilestoneFields.MILESTONE_ACTUAL_COMPLETION_DATE,
                        document.getInvoiceMilestones().get(i).getMilestoneActualCompletionDate());
            }
        }

        return invoiceMilestoneFieldsMap;
    }

    private Map<String, Object> buildPayeeFieldsMap(ContractsGrantsInvoiceDocument document) {
        Map<String, Object> parameterMap = new HashMap<>();

        parameterMap.put(KFSPropertyConstants.PAYEE + "." + KFSPropertyConstants.NAME,
                document.getBillingAddressName());
        parameterMap.put(KFSPropertyConstants.PAYEE + "." + KFSPropertyConstants.ADDRESS_LINE1,
                document.getBillingLine1StreetAddress());
        parameterMap.put(KFSPropertyConstants.PAYEE + "." + KFSPropertyConstants.ADDRESS_LINE2,
                document.getBillingLine2StreetAddress());
        parameterMap.put(KFSPropertyConstants.PAYEE + "." + KFSPropertyConstants.CITY,
                document.getBillingCityName());
        parameterMap.put(KFSPropertyConstants.PAYEE + "." + KFSPropertyConstants.STATE,
                document.getBillingStateCode());
        parameterMap.put(KFSPropertyConstants.PAYEE + "." + KFSPropertyConstants.ZIPCODE,
                document.getBillingZipCode());

        return parameterMap;
    }

    private Map<String, Object> buildSystemInformationMap(ContractsGrantsInvoiceDocument document) {
        Map<String, Object> primaryKeys = new HashMap<>();
        primaryKeys.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR,
                document.getAccountingPeriod().getUniversityFiscalYear());
        primaryKeys.put(KFSPropertyConstants.PROCESSING_CHART_OF_ACCT_CD,
                document.getAccountsReceivableDocumentHeader().getProcessingChartOfAccountCode());
        primaryKeys.put(KFSPropertyConstants.PROCESSING_ORGANIZATION_CODE,
                document.getAccountsReceivableDocumentHeader().getProcessingOrganizationCode());
        SystemInformation sysInfo = businessObjectService.findByPrimaryKey(SystemInformation.class, primaryKeys);

        Map<String, Object> parameterMap = new HashMap<>();
        if (ObjectUtils.isNotNull(sysInfo)) {
            parameterMap.put(ArPropertyConstants.SYSTEM_INFORMATION + "." +
                            ArPropertyConstants.SystemInformationFields.FEIN_NUMBER,
                    sysInfo.getUniversityFederalEmployerIdentificationNumber());
            parameterMap.put(ArPropertyConstants.SYSTEM_INFORMATION + "." +
                            KFSPropertyConstants.NAME, sysInfo.getOrganizationRemitToAddressName());
            parameterMap.put(ArPropertyConstants.SYSTEM_INFORMATION + "." +
                            KFSPropertyConstants.ADDRESS_LINE1, sysInfo.getOrganizationRemitToLine1StreetAddress());
            parameterMap.put(ArPropertyConstants.SYSTEM_INFORMATION + "." +
                            KFSPropertyConstants.ADDRESS_LINE2, sysInfo.getOrganizationRemitToLine2StreetAddress());
            parameterMap.put(ArPropertyConstants.SYSTEM_INFORMATION + "." +
                            KFSPropertyConstants.CITY, sysInfo.getOrganizationRemitToCityName());
            parameterMap.put(ArPropertyConstants.SYSTEM_INFORMATION + "." +
                            KFSPropertyConstants.STATE, sysInfo.getOrganizationRemitToStateCode());
            parameterMap.put(ArPropertyConstants.SYSTEM_INFORMATION + "." +
                            KFSPropertyConstants.ZIPCODE, sysInfo.getOrganizationRemitToZipCode());
        }
        return parameterMap;
    }

    protected String stringifyValue(Object value) {
        if (ObjectUtils.isNull(value)) {
            return KFSConstants.EMPTY_STRING;
        } else if (value instanceof String) {
            return (String) value;
        } else if (value instanceof java.util.Date) {
            return getDateTimeService().toDateString((java.util.Date) value);
        } else if (value instanceof Boolean) {
            return stringifyBooleanForContractsGrantsInvoiceTemplate((Boolean) value);
        } else if (value instanceof KualiDecimal) {
            return contractsGrantsBillingUtilityService.formatForCurrency((KualiDecimal) value);
        }
        return value.toString();
    }

    /**
     * Converts boolean to a String to display on pdf report
     *
     * @param bool a boolean value
     * @return a String for the pdf based on the given boolean value
     */
    protected String stringifyBooleanForContractsGrantsInvoiceTemplate(boolean bool) {
        return bool ? "Yes" : "No";
    }

    @Override
    public void updateLastBilledDate(ContractsGrantsInvoiceDocument document) {
        boolean isFinalBill = document.getInvoiceGeneralDetail().isFinalBillIndicator();

        // To calculate and update Last Billed Date based on the status of the invoice. Final or Corrected.
        // 1. Set last Billed Date to Award Accounts

        for (InvoiceAccountDetail id : document.getAccountDetails()) {
            if (isFinalBill) {
                setAwardAccountFinalBilledValueAndLastBilledDate(id, true,
                        document.getInvoiceGeneralDetail().getProposalNumber(), document.isInvoiceReversal(),
                        document.getInvoiceGeneralDetail().getLastBilledDate());
            } else {
                calculateAwardAccountLastBilledDate(id, document.isInvoiceReversal(),
                        document.getInvoiceGeneralDetail().getLastBilledDate(),
                        document.getInvoiceGeneralDetail().getProposalNumber());
            }
        }

        // 2. Set last Billed to Award = least of last billed date of award account.
        String proposalNumber = document.getInvoiceGeneralDetail().getProposalNumber();
        Map<String, Object> map = new HashMap<>();
        map.put(KFSPropertyConstants.PROPOSAL_NUMBER, proposalNumber);
        ContractsAndGrantsBillingAward award = kualiModuleService.getResponsibleModuleService(
                ContractsAndGrantsBillingAward.class).getExternalizableBusinessObject(
                ContractsAndGrantsBillingAward.class, map);

        if (CollectionUtils.isNotEmpty(award.getActiveAwardAccounts())) {
            // To set last billed Date to award.
            contractsAndGrantsModuleBillingService.setLastBilledDateToAward(proposalNumber, getLastBilledDate(award));
        }
    }

    @Override
    public java.sql.Date getLastBilledDate(ContractsAndGrantsBillingAward award) {
        java.sql.Date awdLastBilledDate = null;
        //get latest non-null current billed date
        if (ObjectUtils.isNotNull(award)) {
            awdLastBilledDate = award.getActiveAwardAccounts().stream()
                    .filter(awardAccount -> ObjectUtils.isNotNull(awardAccount.getCurrentLastBilledDate()))
                    .sorted(Comparator.comparing(ContractsAndGrantsBillingAwardAccount::getCurrentLastBilledDate, Comparator.nullsLast(Comparator.reverseOrder())))
                    //convert to list of Dates
                    .map(awardAccount -> awardAccount.getCurrentLastBilledDate())
                    .findFirst().orElse(null);
        }
        return awdLastBilledDate;
    }

    /**
     * This method updates the AwardAccount object's last billed Variable with the value provided
     *
     * @param id
     * @param invoiceReversal
     * @param lastBilledDate
     * @param proposalNumber
     */
    protected void calculateAwardAccountLastBilledDate(InvoiceAccountDetail id, boolean invoiceReversal,
            java.sql.Date lastBilledDate, String proposalNumber) {
        Map<String, Object> mapKey = new HashMap<>();
        mapKey.put(KFSPropertyConstants.ACCOUNT_NUMBER, id.getAccountNumber());
        mapKey.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, id.getChartOfAccountsCode());
        mapKey.put(KFSPropertyConstants.PROPOSAL_NUMBER, proposalNumber);
        // To set previous and current last Billed Date for award account .
        contractsAndGrantsModuleBillingService.setLastBilledDateToAwardAccount(mapKey, invoiceReversal, lastBilledDate);
    }

    @Override
    public void updateBillsAndMilestones(boolean billed, List<InvoiceMilestone> invoiceMilestones,
            List<InvoiceBill> invoiceBills) {
        updateMilestonesBilledIndicator(billed, invoiceMilestones);
        updateBillsBilledIndicator(billed, invoiceBills);
    }

    @Override
    public void updateMilestonesBilledIndicator(boolean billed, List<InvoiceMilestone> invoiceMilestones) {
        if (CollectionUtils.isNotEmpty(invoiceMilestones)) {
            List<Long> milestoneIds = new ArrayList<>();
            for (InvoiceMilestone invoiceMilestone : invoiceMilestones) {
                milestoneIds.add(invoiceMilestone.getMilestoneIdentifier());
            }

            if (CollectionUtils.isNotEmpty(milestoneIds)) {
                setMilestonesBilled(milestoneIds, billed);
            }
        }
    }

    /**
     * This method updates value of billed in Milestone BO to the value of the billed parameter
     *
     * @param milestoneIds
     * @param billed
     */
    protected void setMilestonesBilled(List<Long> milestoneIds, boolean billed) {
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put(ArPropertyConstants.MilestoneFields.MILESTONE_IDENTIFIER, milestoneIds);
        List<Milestone> milestones = (List<Milestone>) getBusinessObjectService().findMatching(Milestone.class,
                fieldValues);

        if (ObjectUtils.isNotNull(milestones)) {
            for (Milestone milestone : milestones) {
                milestone.setBilled(billed);
            }
            getBusinessObjectService().save(milestones);
        }
    }

    @Override
    public void updateBillsBilledIndicator(boolean billed, List<InvoiceBill> invoiceBills) {
        if (CollectionUtils.isNotEmpty(invoiceBills)) {
            List<Long> billIds = new ArrayList<>();
            for (InvoiceBill invoiceBill : invoiceBills) {
                billIds.add(invoiceBill.getBillIdentifier());
            }

            if (CollectionUtils.isNotEmpty(invoiceBills)) {
                setBillsBilled(billIds, billed);
            }
        }
    }

    /**
     * This method updates value of billed in Bill BO to billed
     *
     * @param billIds
     * @param billed
     */
    protected void setBillsBilled(List<Long> billIds, boolean billed) {
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put(ArPropertyConstants.BillFields.BILL_IDENTIFIER, billIds);
        List<Bill> bills = (List<Bill>) getBusinessObjectService().findMatching(Bill.class, fieldValues);

        if (ObjectUtils.isNotNull(bills)) {
            for (Bill bill : bills) {
                bill.setBilled(billed);
            }
            getBusinessObjectService().save(bills);
        }
    }

    /**
     * This method updates the ContractsAndGrantsBillingAwardAccount object's FinalBilled Variable with the value
     * provided
     *
     * @param id
     * @param value
     * @param proposalNumber
     */
    protected void setAwardAccountFinalBilledValue(InvoiceAccountDetail id, boolean value, String proposalNumber) {
        Map<String, Object> mapKey = new HashMap<>();
        mapKey.put(KFSPropertyConstants.ACCOUNT_NUMBER, id.getAccountNumber());
        mapKey.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, id.getChartOfAccountsCode());
        mapKey.put(KFSPropertyConstants.PROPOSAL_NUMBER, proposalNumber);

        // To set final Billed to award Account
        contractsAndGrantsModuleBillingService.setFinalBilledToAwardAccount(mapKey, value);
    }

    /**
     * This method updates the ContractsAndGrantsBillingAwardAccount object's FinalBilled Variable with the value
     * provided and also sets the last billed date and invoice status.
     *
     * @param id
     * @param finalBilled
     * @param proposalNumber
     * @param invoiceReversal
     * @param lastBilledDate
     */
    protected void setAwardAccountFinalBilledValueAndLastBilledDate(InvoiceAccountDetail id, boolean finalBilled,
            String proposalNumber, boolean invoiceReversal, java.sql.Date lastBilledDate) {
        Map<String, Object> mapKey = new HashMap<>();
        mapKey.put(KFSPropertyConstants.ACCOUNT_NUMBER, id.getAccountNumber());
        mapKey.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, id.getChartOfAccountsCode());
        mapKey.put(KFSPropertyConstants.PROPOSAL_NUMBER, proposalNumber);

        // To set previous and current last Billed Date for award account .
        contractsAndGrantsModuleBillingService.setFinalBilledAndLastBilledDateToAwardAccount(mapKey, finalBilled,
                invoiceReversal, lastBilledDate);
    }

    @Override
    public void updateUnfinalizationToAwardAccount(List<InvoiceAccountDetail> accountDetails, String proposalNumber) {
        for (Object entry : accountDetails) {
            InvoiceAccountDetail id = (InvoiceAccountDetail) entry;
            setAwardAccountFinalBilledValue(id, false, proposalNumber);
        }
    }

    @Override
    public void correctContractsGrantsInvoiceDocument(ContractsGrantsInvoiceDocument document) {
        // correct Direct Cost Invoice Details.
        for (ContractsGrantsInvoiceDetail id : document.getDirectCostInvoiceDetails()) {
            correctInvoiceDetail(id);
        }

        // correct Indirect Cost Invoice Details.
        for (ContractsGrantsInvoiceDetail id : document.getIndirectCostInvoiceDetails()) {
            correctInvoiceDetail(id);
        }

        // update correction to the InvoiceAccountDetail objects
        for (InvoiceAccountDetail id : document.getAccountDetails()) {
            correctInvoiceAccountDetail(id);
        }

        // correct invoiceDetailAccountObjectCode.
        for (InvoiceDetailAccountObjectCode invoiceDetailAccountObjectCode : document
                .getInvoiceDetailAccountObjectCodes()) {
            invoiceDetailAccountObjectCode.correctInvoiceDetailAccountObjectCodeExpenditureAmount();
        }

        // correct Bills
        KualiDecimal totalBillingAmount = KualiDecimal.ZERO;
        for (InvoiceBill bill : document.getInvoiceBills()) {
            bill.setEstimatedAmount(bill.getEstimatedAmount().negated());
            totalBillingAmount = totalBillingAmount.add(bill.getEstimatedAmount());
        }

        // correct Milestones
        KualiDecimal totalMilestonesAmount = KualiDecimal.ZERO;
        for (InvoiceMilestone milestone : document.getInvoiceMilestones()) {
            milestone.setMilestoneAmount(milestone.getMilestoneAmount().negated());
            totalMilestonesAmount = totalMilestonesAmount.add(milestone.getMilestoneAmount());
        }

        document.getInvoiceGeneralDetail().setTotalPreviouslyBilled(
                getAwardBilledToDateAmountExcludingDocument(document.getInvoiceGeneralDetail().getProposalNumber(),
                        document.getDocumentNumber()));

        if (ArConstants.BillingFrequencyValues.isMilestone(document.getInvoiceGeneralDetail()) && CollectionUtils
                .isNotEmpty(document.getInvoiceMilestones())) {
            document.getInvoiceGeneralDetail().setTotalAmountBilledToDate(
                    document.getInvoiceGeneralDetail().getTotalAmountBilledToDate().add(totalMilestonesAmount));
        } else if (ArConstants.BillingFrequencyValues.isPredeterminedBilling(
                document.getInvoiceGeneralDetail()) && CollectionUtils.isNotEmpty(document.getInvoiceBills())) {
            document.getInvoiceGeneralDetail().setTotalAmountBilledToDate(
                    document.getInvoiceGeneralDetail().getTotalAmountBilledToDate().add(totalBillingAmount));
        } else {
            KualiDecimal newTotalBilled = document.getTotalCostInvoiceDetail().getInvoiceAmount().add(
                    document.getInvoiceGeneralDetail().getTotalPreviouslyBilled());
            newTotalBilled = newTotalBilled.add(getOtherTotalBilledForAwardPeriod(document));
            document.getInvoiceGeneralDetail().setTotalAmountBilledToDate(newTotalBilled);
            calculatePreviouslyBilledAmounts(document);
        }

        for (InvoiceAddressDetail invoiceAddressDetail : document.getInvoiceAddressDetails()) {
            invoiceAddressDetail.setInitialTransmissionDate(null);
            invoiceAddressDetail.setTransmittedByPrincipalId("");
            invoiceAddressDetail.setTransmissionDate(null);
            invoiceAddressDetail.setTransmissionStatusCode("");
            invoiceAddressDetail.setTransmissionCount(0);
        }
    }

    /**
     * Error corrects an invoice detail
     *
     * @param invoiceDetail the invoice detail to error correct
     */
    protected void correctInvoiceDetail(ContractsGrantsInvoiceDetail invoiceDetail) {
        invoiceDetail.setTotalPreviouslyBilled(
                invoiceDetail.getTotalPreviouslyBilled().add(invoiceDetail.getInvoiceAmount()));
        invoiceDetail.setCumulativeExpenditures(
                invoiceDetail.getCumulativeExpenditures().subtract(invoiceDetail.getInvoiceAmount()));
        invoiceDetail.setInvoiceAmount(invoiceDetail.getInvoiceAmount().negated());
        invoiceDetail.setInvoiceDocument(null);
    }

    /**
     * Error corrects an invoice account detail
     *
     * @param invoiceAccountDetail the invoice account detail to error correct
     */
    protected void correctInvoiceAccountDetail(InvoiceAccountDetail invoiceAccountDetail) {
        invoiceAccountDetail.setTotalPreviouslyBilled(
                invoiceAccountDetail.getTotalPreviouslyBilled().add(invoiceAccountDetail.getInvoiceAmount()));
        invoiceAccountDetail.setCumulativeExpenditures(
                invoiceAccountDetail.getCumulativeExpenditures().subtract(invoiceAccountDetail.getInvoiceAmount()));
        invoiceAccountDetail.setInvoiceAmount(invoiceAccountDetail.getInvoiceAmount().negated());
        invoiceAccountDetail.setInvoiceDocument(null);
    }

    protected void addNoteForInvoiceReportFail(ContractsGrantsInvoiceDocument document) {
        Note note = new Note();
        note.setNotePostedTimestampToCurrent();
        note.setNoteText(configurationService
                .getPropertyValueAsString(ArKeyConstants.ERROR_FILE_UPLOAD_NO_PDF_FILE_SELECTED_FOR_SAVE));
        note.setNoteTypeCode(KFSConstants.NoteTypeEnum.BUSINESS_OBJECT_NOTE_TYPE.getCode());
        Person systemUser = personService.getPersonByPrincipalName(KFSConstants.SYSTEM_USER);
        note = noteService.createNote(note, document.getNoteTarget(), systemUser.getPrincipalId());
        noteService.save(note);
        document.addNote(note);
    }

    @Override
    public List<String> checkAwardContractControlAccounts(ContractsAndGrantsBillingAward award) {
        List<String> errorString = new ArrayList<>();
        boolean isValid = true;
        int accountNum = award.getActiveAwardAccounts().size();
        // To check if invoicing options exist on the award
        if (ObjectUtils.isNotNull(award.getInvoicingOptionCode())) {

            // To check if the award account is associated with a contract control account.
            for (ContractsAndGrantsBillingAwardAccount awardAccount : award.getActiveAwardAccounts()) {
                if (ObjectUtils.isNull(awardAccount.getAccount())
                        || ObjectUtils.isNull(awardAccount.getAccount().getContractControlAccount())) {
                    isValid = false;
                    break;
                }
            }

            // if the Invoicing option is "By Contract Control Account" and there are no contract control accounts for
            // one / all award accounts, then throw error.
            if (award.getInvoicingOptionCode().equalsIgnoreCase(ArConstants.INV_CONTRACT_CONTROL_ACCOUNT)) {
                if (!isValid) {
                    errorString.add(ArKeyConstants.AwardConstants.ERROR_NO_CTRL_ACCT);
                    errorString.add(award.getInvoicingOptionDescription());
                }
            } else if (award.getInvoicingOptionCode().equalsIgnoreCase(ArConstants.INV_AWARD)) {
                // if the Invoicing option is "By Award" and there are no contract control accounts for one / all award
                // accounts, then throw error.
                if (!isValid) {
                    errorString.add(ArKeyConstants.AwardConstants.ERROR_NO_CTRL_ACCT);
                    errorString.add(award.getInvoicingOptionDescription());
                } else {
                    if (accountNum != 1) {
                        Account tmpAcct1;
                        Account tmpAcct2;

                        Object[] awardAccounts = award.getActiveAwardAccounts().toArray();
                        for (int i = 0; i < awardAccounts.length - 1; i++) {
                            tmpAcct1 = ((ContractsAndGrantsBillingAwardAccount) awardAccounts[i]).getAccount()
                                    .getContractControlAccount();
                            tmpAcct2 = ((ContractsAndGrantsBillingAwardAccount) awardAccounts[i + 1]).getAccount()
                                    .getContractControlAccount();
                            // if the Invoicing option is "By Award" and there are more than one contract control
                            // account assigned for the award, then throw error.
                            if (ObjectUtils.isNull(tmpAcct1) || !tmpAcct1.equals(tmpAcct2)) {
                                errorString.add(ArKeyConstants.AwardConstants.ERROR_MULTIPLE_CTRL_ACCT);
                                errorString.add(award.getInvoicingOptionDescription());
                            }
                        }
                    }
                }
            }
        }
        return errorString;
    }

    @Override
    public boolean isInvoiceDocumentEffective(String documentNumber) {
        final DocumentHeader invoiceDocHeader = getBusinessObjectService().findBySinglePrimaryKey(
                DocumentHeader.class, documentNumber);
        final String documentStatus = invoiceDocHeader.getWorkflowDocumentStatusCode();

        // skip error correcting CINVs, as they should be taken care of by the error correcting code
        if (StringUtils.isBlank(invoiceDocHeader.getFinancialDocumentInErrorNumber()) && !StringUtils.equals(
                documentStatus, DocumentStatus.CANCELED.getCode()) && !StringUtils.equals(documentStatus,
                DocumentStatus.DISAPPROVED
                        .getCode())) {
            final DocumentHeader correctingDocumentHeader = getFinancialSystemDocumentService()
                    .getCorrectingDocumentHeader(documentNumber);
            return ObjectUtils.isNull(correctingDocumentHeader) || isCorrectedInvoiceDocumentEffective(
                    correctingDocumentHeader.getDocumentNumber());
        }
        return false;
    }

    /**
     * Determines if an error correction is "effective" - ie, currently locking resources like milestones and
     * pre-determined billing from the original CINV
     *
     * @param errorCorrectionDocumentNumber the document number to check for the effectiveness of
     * @return true if the document is effectively locking resources, false otherwise
     */
    protected boolean isCorrectedInvoiceDocumentEffective(String errorCorrectionDocumentNumber) {
        final DocumentHeader invoiceDocHeader = getBusinessObjectService().findBySinglePrimaryKey(
                DocumentHeader.class, errorCorrectionDocumentNumber);
        final String documentStatus = invoiceDocHeader.getWorkflowDocumentStatusCode();
        if (getFinancialSystemDocumentService().getPendingDocumentStatuses().contains(documentStatus)) {
            // the error correction document is currently pending, then it has not yet freed the milestones
            // and pre-billings on the original CINV, so it's effective
            return true;
        }
        final DocumentHeader correctingDocumentHeader = getFinancialSystemDocumentService().getCorrectingDocumentHeader(
                errorCorrectionDocumentNumber);
        // rules on the newer error corrector to see if this document is effective or not
        // is the error correction currently undergoing error correction itself?  Then recheck the the error
        // correction document is not effective and has freed resources
        return ObjectUtils.isNotNull(correctingDocumentHeader) && isCorrectedInvoiceDocumentEffective(
                correctingDocumentHeader.getDocumentNumber());
    }

    @Override
    public boolean isTemplateValidForContractsGrantsInvoiceDocument(InvoiceTemplate invoiceTemplate,
            ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument) {
        if (ObjectUtils.isNotNull(contractsGrantsInvoiceDocument)) {
            return StringUtils.equals(invoiceTemplate.getBillByChartOfAccountCode(),
                    contractsGrantsInvoiceDocument.getBillByChartOfAccountCode()) && StringUtils.equals(
                    invoiceTemplate.getBilledByOrganizationCode(),
                    contractsGrantsInvoiceDocument.getBilledByOrganizationCode());
        }
        return true;
    }

    @Override
    public boolean doesCostCategoryContainObjectCode(CostCategory category, String chartOfAccountsCode,
            String objectCode) {
        if (!CollectionUtils.isEmpty(category.getObjectCodes())) {
            for (CostCategoryObjectCode categoryObjectCode : category.getObjectCodes()) {
                if (StringUtils.equals(categoryObjectCode.getChartOfAccountsCode(), chartOfAccountsCode)
                        && StringUtils.equals(categoryObjectCode.getFinancialObjectCode(), objectCode)) {
                    return true;
                }
            }
        }

        if (!CollectionUtils.isEmpty(category.getObjectLevels())) {
            for (CostCategoryObjectLevel categoryObjectLevel : category.getObjectLevels()) {
                if (getObjectCodeService().doesObjectLevelContainObjectCode(
                        categoryObjectLevel.getChartOfAccountsCode(), categoryObjectLevel.getFinancialObjectLevelCode(),
                        chartOfAccountsCode, objectCode)) {
                    return true;
                }
            }
        }

        if (!CollectionUtils.isEmpty(category.getObjectConsolidations())) {
            for (CostCategoryObjectConsolidation categoryObjectConsolidation : category.getObjectConsolidations()) {
                if (getObjectCodeService().doesObjectConsolidationContainObjectCode(
                        categoryObjectConsolidation.getChartOfAccountsCode(),
                        categoryObjectConsolidation.getFinConsolidationObjectCode(), chartOfAccountsCode, objectCode)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public Map<String, List<ContractsGrantsInvoiceDocument>> getInvoicesByAward(
            Collection<ContractsGrantsInvoiceDocument> invoices) {
        Map<String, List<ContractsGrantsInvoiceDocument>> invoicesByAward = new HashMap<>();
        for (ContractsGrantsInvoiceDocument invoice : invoices) {
            String proposalNumber = invoice.getInvoiceGeneralDetail().getProposalNumber();
            if (invoicesByAward.containsKey(proposalNumber)) {
                invoicesByAward.get(proposalNumber).add(invoice);
            } else {
                List<ContractsGrantsInvoiceDocument> invoicesByProposalNumber = new ArrayList<>();
                invoicesByProposalNumber.add(invoice);
                invoicesByAward.put(proposalNumber, invoicesByProposalNumber);
            }
        }
        return invoicesByAward;
    }

    /**
     * If the document has only one CustomerInvoiceDetail, update the amount with the total expenditures from the
     * cost categories; otherwise, split the amounts by account.
     */
    @Override
    public void recalculateSourceAccountingLineTotals(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument) {
        if (!CollectionUtils.isEmpty(contractsGrantsInvoiceDocument.getSourceAccountingLines())) {
            if (contractsGrantsInvoiceDocument.getSourceAccountingLines().size() == 1) {
                final CustomerInvoiceDetail customerInvoiceDetail = (CustomerInvoiceDetail) contractsGrantsInvoiceDocument
                        .getSourceAccountingLine(0);
                customerInvoiceDetail.setAmount(getTotalAmountForInvoice(contractsGrantsInvoiceDocument));
            } else {
                final Map<String, KualiDecimal> accountExpenditureAmounts =
                        getCategoryExpenditureAmountsForInvoiceAccountDetail(contractsGrantsInvoiceDocument);
                for (Object al : contractsGrantsInvoiceDocument.getSourceAccountingLines()) {
                    final CustomerInvoiceDetail customerInvoiceDetail = (CustomerInvoiceDetail) al;
                    final String accountKey = StringUtils.join(
                            new String[]{customerInvoiceDetail.getChartOfAccountsCode(),
                                    customerInvoiceDetail.getAccountNumber()}, "-");
                    customerInvoiceDetail.setAmount(accountExpenditureAmounts.getOrDefault(accountKey,
                            KualiDecimal.ZERO));
                }
            }
        }
    }

    /**
     * Checks the KFS-AR / ContractsGrantsInvoiceDocumentBatchStep / User parameter, or, if no value is found for that
     * parameter, the KFS system user to see if they are the initiator of the document.  If the
     * KFS-AR / ContractsGrantsInvoiceDocumentBatchStep / User parameter is set to a value of a principal who creates
     * CINVs outside of batch - well, okay, that's an interesting business process, that also means that the results
     * of this method may be incorrect.  But you can always override, right?
     */
    @Override
    public boolean isDocumentBatchCreated(ContractsGrantsInvoiceDocument document) {
        if (document.getInvoiceGeneralDetail().getAward().getAutoApproveIndicator()) {
            final Principal batchJobInitiatorPrincipal = getContractsGrantsInvoiceBatchCreationUserPrincipal();
            return StringUtils.equalsIgnoreCase(document.getDocumentHeader().getInitiatorPrincipalId(),
                    batchJobInitiatorPrincipal.getPrincipalId());
        }
        return false;
    }

    @Override
    public boolean doesInvoicePassValidation(final ContractsGrantsInvoiceDocument document) {
        try {
            return GlobalVariables.doInNewGlobalVariables(
                    new UserSession(getContractsGrantsInvoiceBatchCreationUserPrincipal().getPrincipalName()),
                    new Callable<>() {
                        /**
                         * Checks if the given document passes rule validation with no errors
                         *
                         * @see Callable#call()
                         */
                        @Override
                        public Boolean call() {
                            final AttributedRouteDocumentEvent routeEvent = new AttributedRouteDocumentEvent(document);
                            getKualiRuleService().applyRules(routeEvent);
                            return !GlobalVariables.getMessageMap().hasErrors();
                        }

                    });
        } catch (Exception e) {
            // validation actually caused an exception?  Um, let's log that, and maybe we'll say we didn't pass
            // validation here, okay?
            LOG.error(
                    "Running validation on Contracts & Grants Invoice {} caused an exception",
                    document::getDocumentNumber,
                    () -> e
            );
            return false;
        }
    }

    /**
     * Determines the user who creates CINV documents via the batch job
     *
     * @return the principal for the user who creates CINV documents via the batch job
     */
    protected Principal getContractsGrantsInvoiceBatchCreationUserPrincipal() {
        final String batchJobInitiatorPrincipalName = getParameterService().getParameterValueAsString(
                ContractsGrantsInvoiceDocumentBatchStep.class, Job.STEP_USER_PARM_NM, KFSConstants.SYSTEM_USER);
        final Principal batchJobInitiatorPrincipal = getIdentityService().getPrincipalByPrincipalName(
                batchJobInitiatorPrincipalName);
        return ObjectUtils.isNull(batchJobInitiatorPrincipal)
                ? getIdentityService().getPrincipalByPrincipalName(KFSConstants.SYSTEM_USER)
                : batchJobInitiatorPrincipal;
    }

    @Override
    public void markManuallySent(final ContractsGrantsInvoiceDocument document) {
        document.getInvoiceAddressDetails().stream().filter(InvoiceAddressDetail::isSendIndicator)
                .forEach(detail -> {
                    detail.markSent();
                    detail.setSendIndicator(false);
                    String transmissionNotePattern = getConfigurationService()
                            .getPropertyValueAsString(ArKeyConstants.INVOICE_ADDRESS_MANUAL_TRANSMISSION_NOTE);
                    String noteText = MessageFormat.format(transmissionNotePattern,
                            GlobalVariables.getUserSession().getUserToLog(),
                            detail.getInvoiceTransmissionMethodCode().toLowerCase(Locale.US));
                    addNoteToInvoiceDocument(document, noteText);
                });
        documentService.updateDocument(document);
    }

    @Override
    public void queueInvoiceTransmissions(final ContractsGrantsInvoiceDocument document) {
        document.getInvoiceAddressDetails().stream().filter(InvoiceAddressDetail::isSendIndicator)
                .forEach(detail -> {
                    detail.setTransmissionStatusCode(TransmissionDetailStatus.Queued.getCode());
                    detail.setSendIndicator(false);
                });
        documentService.updateDocument(document);
    }
    
    @Override
    //CU customization: backport FINP-8642
    public void updateFinalBillIndicator(final ContractsGrantsInvoiceDocument document) {
        final InvoiceGeneralDetail invoiceGeneralDetail = document.getInvoiceGeneralDetail();
        final boolean newFinalBill = !invoiceGeneralDetail.isFinalBillIndicator();

        invoiceGeneralDetail.setFinalBillIndicator(newFinalBill);
        String noteText = GlobalVariables.getUserSession().getPerson().getName() + " updated Final Bill to "
                + (invoiceGeneralDetail.isFinalBillIndicator() ? "Yes" : "No");
        addNoteToInvoiceDocument(document, noteText);
        documentService.updateDocument(document);

        // update award account final bill indicator
        ContractsAndGrantsBillingAward award = invoiceGeneralDetail.getAward();
        for (ContractsAndGrantsBillingAwardAccount cgAwardAccount : award.getActiveAwardAccounts()) {
            contractsAndGrantsModuleBillingService.setFinalBilledToAwardAccount(
                    Map.of(KFSPropertyConstants.ACCOUNT_NUMBER, cgAwardAccount.getAccountNumber(),
                            KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, cgAwardAccount.getChartOfAccountsCode(),
                            KFSPropertyConstants.PROPOSAL_NUMBER, cgAwardAccount.getAward().getProposalNumber()),
                    newFinalBill);
        }
        documentService.updateDocument(document);
    }

    @Override
    public void addInvoiceTransmissionNote(ContractsGrantsInvoiceDocument document, String invoiceTransmissionMethod) {
        String transmissionNotePattern = getConfigurationService()
                .getPropertyValueAsString(ArKeyConstants.INVOICE_ADDRESS_TRANSMISSION_NOTE);
        String noteText = MessageFormat.format(transmissionNotePattern,
                GlobalVariables.getUserSession().getUserToLog(), invoiceTransmissionMethod.toLowerCase(Locale.US));
        addNoteToInvoiceDocument(document, noteText);
    }

    protected void addNoteToInvoiceDocument(ContractsGrantsInvoiceDocument document, String noteText) {
        Note note = new Note();
        note.setNotePostedTimestampToCurrent();
        note.setNoteText(noteText);
        note.setNoteTypeCode(document.getNoteType().getCode());
        Person systemUser = personService.getPersonByPrincipalName(KFSConstants.SYSTEM_USER);
        note = noteService.createNote(note, document.getNoteTarget(), systemUser.getPrincipalId());
        noteService.save(note);
        document.addNote(note);
    }

    public ContractsAndGrantsModuleBillingService getContractsAndGrantsModuleBillingService() {
        return contractsAndGrantsModuleBillingService;
    }

    public void setContractsAndGrantsModuleBillingService(
            ContractsAndGrantsModuleBillingService contractsAndGrantsModuleBillingService) {
        this.contractsAndGrantsModuleBillingService = contractsAndGrantsModuleBillingService;
    }

    public ObjectCodeService getObjectCodeService() {
        return objectCodeService;
    }

    public void setObjectCodeService(ObjectCodeService objectCodeService) {
        this.objectCodeService = objectCodeService;
    }

    public void setAttachmentService(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    public void setNoteService(NoteService noteService) {
        this.noteService = noteService;
    }

    public void setKualiModuleService(KualiModuleService kualiModuleService) {
        this.kualiModuleService = kualiModuleService;
    }

    public void setContractsGrantsInvoiceDocumentDao(
            ContractsGrantsInvoiceDocumentDao contractsGrantsInvoiceDocumentDao) {
        this.contractsGrantsInvoiceDocumentDao = contractsGrantsInvoiceDocumentDao;
    }

    public void setContractsGrantsBillingUtilityService(
            ContractsGrantsBillingUtilityService contractsGrantsBillingUtilityService) {
        this.contractsGrantsBillingUtilityService = contractsGrantsBillingUtilityService;
    }

    public FinancialSystemDocumentService getFinancialSystemDocumentService() {
        return financialSystemDocumentService;
    }

    public void setFinancialSystemDocumentService(FinancialSystemDocumentService financialSystemDocumentService) {
        this.financialSystemDocumentService = financialSystemDocumentService;
    }

    @Override
    public List<SuspensionCategory> getSuspensionCategories() {
        return suspensionCategories;
    }

    public void setSuspensionCategories(List<SuspensionCategory> suspensionCategories) {
        this.suspensionCategories = suspensionCategories;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setUniversityDateService(UniversityDateService universityDateService) {
        this.universityDateService = universityDateService;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public AccountsReceivablePendingEntryService getAccountsReceivablePendingEntryService() {
        return accountsReceivablePendingEntryService;
    }

    public void setAccountsReceivablePendingEntryService(
            AccountsReceivablePendingEntryService accountsReceivablePendingEntryService) {
        this.accountsReceivablePendingEntryService = accountsReceivablePendingEntryService;
    }

    public KualiRuleService getKualiRuleService() {
        return kualiRuleService;
    }

    public void setKualiRuleService(KualiRuleService kualiRuleService) {
        this.kualiRuleService = kualiRuleService;
    }

    public IdentityService getIdentityService() {
        return identityService;
    }

    public void setIdentityService(IdentityService identityService) {
        this.identityService = identityService;
    }

    public CostCategoryService getCostCategoryService() {
        return costCategoryService;
    }

    public void setCostCategoryService(CostCategoryService costCategoryService) {
        this.costCategoryService = costCategoryService;
    }

    public PermissionService getPermissionService() {
        return permissionService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public CustomerInvoiceDocumentService getCustomerInvoiceDocumentService() {
        return customerInvoiceDocumentService;
    }

    public void setCustomerInvoiceDocumentService(CustomerInvoiceDocumentService customerInvoiceDocumentService) {
        this.customerInvoiceDocumentService = customerInvoiceDocumentService;
    }

    public void setAccountingPeriodService(final AccountingPeriodService accountingPeriodService) {
        this.accountingPeriodService = accountingPeriodService;
    }

    public void setObjectTypeService(final ObjectTypeService objectTypeService) {
        this.objectTypeService = objectTypeService;
    }
}
