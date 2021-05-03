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
import org.kuali.kfs.module.ar.ArConstants.ContractsAndGrantsInvoiceDocumentCreationProcessType;
import org.kuali.kfs.module.ar.ArPropertyConstants.ContractsGrantsInvoiceDocumentErrorLogLookupFields;
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
import edu.cornell.kfs.module.ar.document.service.CuContractsGrantsInvoiceDocumentService;
import edu.cornell.kfs.module.ar.service.CuContractsGrantsInvoiceCreateDocumentService;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

public class CuContractsGrantsInvoiceCreateDocumentServiceImpl extends ContractsGrantsInvoiceCreateDocumentServiceImpl implements CuContractsGrantsInvoiceCreateDocumentService {
    
    private static final Logger LOG = LogManager.getLogger();
    
    protected CuContractsGrantsInvoiceDocumentService cuContractsGrantsInvoiceDocumentService;
    
    /**
     * Fixes NullPointerException that can occur when getting the award total amount.
     * award.getAwardTotalAmount().bigDecimalValue() causes the NullPointerException if the total amount returned null.
     */
    @Override
    protected void storeValidationErrors(Map<ContractsAndGrantsBillingAward, List<String>> invalidGroup,
            Collection<ContractsGrantsInvoiceDocumentErrorLog> contractsGrantsInvoiceDocumentErrorLogs,
            String creationProcessTypeCode) {
        for (ContractsAndGrantsBillingAward award : invalidGroup.keySet()) {
            KualiDecimal cumulativeExpenses = KualiDecimal.ZERO;
            ContractsGrantsInvoiceDocumentErrorLog contractsGrantsInvoiceDocumentErrorLog =
                    new ContractsGrantsInvoiceDocumentErrorLog();

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
                    contractsGrantsInvoiceDocumentErrorLog.setPrimaryFundManagerPrincipalId(
                            award.getAwardPrimaryFundManager().getPrincipalId());
                }
                if (!CollectionUtils.isEmpty(award.getActiveAwardAccounts())) {
                    boolean firstLineFlag = true;

                    for (ContractsAndGrantsBillingAwardAccount awardAccount : award.getActiveAwardAccounts()) {

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

            for (String vCat : invalidGroup.get(award)) {
                ContractsGrantsInvoiceDocumentErrorMessage contractsGrantsInvoiceDocumentErrorCategory =
                        new ContractsGrantsInvoiceDocumentErrorMessage();
                contractsGrantsInvoiceDocumentErrorCategory.setErrorMessageText(vCat);
                contractsGrantsInvoiceDocumentErrorLog.getErrorMessages()
                        .add(contractsGrantsInvoiceDocumentErrorCategory);
            }

            int errorAccountsMax = dataDictionaryService.getAttributeMaxLength(
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

    /*
     * CUMod: KFSPTS-12866 
     */
    @Override
    public void populateDocumentDescription(ContractsGrantsInvoiceDocument cgInvoiceDocument) {
        if (ObjectUtils.isNotNull(cgInvoiceDocument) && ObjectUtils.isNotNull(cgInvoiceDocument.getInvoiceGeneralDetail())) {
            String proposalNumber = cgInvoiceDocument.getInvoiceGeneralDetail().getProposalNumber();
            if (StringUtils.isNotBlank(proposalNumber)) {
                String contractControlAccount = findContractControlAccountNumber(cgInvoiceDocument.getAccountDetails());
                String newTitle =  MessageFormat.format(findTitleFormatString(), proposalNumber, contractControlAccount);
                cgInvoiceDocument.getDocumentHeader().setDocumentDescription(newTitle);
            } else {
                LOG.error("populateDocumentDescription, unable to set the document description due to the proposal number being null");
            }
        } else {
            LOG.error("populateDocumentDescription, unable to set the document description due to the document or invoice general details being null");
        }
    }
    
    /*
     * CUMod: KFSPTS-12866, KFSPTS-14929
     */
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
    
    /*
     * CUMod: KFSPTS-12866
     */
    protected String findTitleFormatString() {
        return getConfigurationService().getPropertyValueAsString(CuArParameterConstants.CONTRACTS_GRANTS_INVOICE_DOCUMENT_TITLE_FORMAT);
    }
    
    /*
     * CUMod: KFSPTS-13256
     * Added evaluation of account sub-fund not being an expenditure to existing base code evaluation
     * of billing frequency and invoice document status when determining valid award accounts.
     * Expenditure sub-funds associated to accounts to exclude are defined in CG system parameter
     * CG_INVOICING_EXCLUDE_EXPENSES_SUB_FUNDS.
     */
    @Override
    protected List<ContractsAndGrantsBillingAwardAccount> getValidAwardAccounts(
            List<ContractsAndGrantsBillingAwardAccount> awardAccounts, ContractsAndGrantsBillingAward award,
            ContractsAndGrantsInvoiceDocumentCreationProcessType creationProcessType) {
        LOG.info("getValidAwardAccounts: CU Customization invoked with creationProcessTypeCode = " + ArConstants.ContractsAndGrantsInvoiceDocumentCreationProcessType.getName(creationProcessType.getCode()));
        if (!ArConstants.BillingFrequencyValues.isMilestone(award)
                && !ArConstants.BillingFrequencyValues.isPredeterminedBilling(award)) {
            List<ContractsAndGrantsBillingAwardAccount> validAwardAccounts = new ArrayList<>();
            Set<Account> invalidAccounts = harvestAccountsFromContractsGrantsInvoices(
                    getInProgressInvoicesForAward(award));

            for (ContractsAndGrantsBillingAwardAccount awardAccount : awardAccounts) {
                if (!invalidAccounts.contains(awardAccount.getAccount())) {
                    boolean checkGracePeriod = ContractsAndGrantsInvoiceDocumentCreationProcessType.MANUAL != creationProcessType;
                    //CUMod KFSPTS-13256
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
     * CUMod: KFSPTS-13256
     */
    protected boolean isNotExpenditureAccount(ContractsAndGrantsBillingAwardAccount billingAwardAccount) {
        Account accountLinkedToAward = getAccountService().getByPrimaryId(billingAwardAccount.getChartOfAccountsCode(), billingAwardAccount.getAccountNumber());
        if (ObjectUtils.isNotNull(accountLinkedToAward)) {
            return !isExpenditureSubFund(accountLinkedToAward.getSubFundGroupCode());
        }
        return true;
    }
    
    /*
     * CUMod: KFSPTS-13256
     */
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
    public ContractsGrantsInvoiceDocument createCGInvoiceDocumentByAwardInfo(ContractsAndGrantsBillingAward awd,
            List<ContractsAndGrantsBillingAwardAccount> accounts, String chartOfAccountsCode, String organizationCode,
            List<ErrorMessage> errorMessages, List<ContractsGrantsLetterOfCreditReviewDetail> accountDetails,
            String locCreationType) {
        ContractsGrantsInvoiceDocument cgInvoiceDocument = super.createCGInvoiceDocumentByAwardInfo(awd, accounts, chartOfAccountsCode, 
                organizationCode, errorMessages, accountDetails, locCreationType);
        //CUMod: KFSPTS-12866
        if (CollectionUtils.isEmpty(errorMessages)) {
            populateDocumentDescription(cgInvoiceDocument);
        }
        return cgInvoiceDocument;
    }
    
    /*
     * CUMod: KFSPTS-14929
     */
    public CuContractsGrantsInvoiceDocumentService getCuContractsGrantsInvoiceDocumentService() {
        return cuContractsGrantsInvoiceDocumentService;
    }
    
    /*
     * CUMod: KFSPTS-14929
     */
    public void setCuContractsGrantsInvoiceDocumentService(
            CuContractsGrantsInvoiceDocumentService cuContractsGrantsInvoiceDocumentService) {
        this.cuContractsGrantsInvoiceDocumentService = cuContractsGrantsInvoiceDocumentService;
    }

}
