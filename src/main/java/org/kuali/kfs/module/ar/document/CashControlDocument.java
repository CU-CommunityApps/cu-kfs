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
package org.kuali.kfs.module.ar.document;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.web.format.CurrencyFormatter;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.datadictionary.legacy.DocumentDictionaryService;
import org.kuali.kfs.gl.service.EntryService;
import org.kuali.kfs.kns.datadictionary.DocumentEntry;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.businessobject.AccountsReceivableDocumentHeader;
import org.kuali.kfs.module.ar.businessobject.CashControlDetail;
import org.kuali.kfs.module.ar.businessobject.PaymentMedium;
import org.kuali.kfs.module.ar.document.service.CashControlDocumentService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSParameterKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.businessobject.ElectronicPaymentClaim;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AmountTotaling;
import org.kuali.kfs.sys.document.ElectronicPaymentClaiming;
import org.kuali.kfs.sys.document.GeneralLedgerPendingEntrySource;
import org.kuali.kfs.sys.document.GeneralLedgerPostingDocument;
import org.kuali.kfs.sys.document.GeneralLedgerPostingDocumentBase;
import org.kuali.kfs.sys.document.validation.impl.AccountingDocumentRuleBaseConstants.GENERAL_LEDGER_PENDING_ENTRY_CODE;
import org.kuali.kfs.sys.service.BankService;
import org.kuali.kfs.sys.service.ElectronicPaymentClaimingService;
import org.kuali.kfs.sys.service.UniversityDateService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// CU customization: backport FINP-8337: removed method removeGeneralLedgerPendingEntries; this can be removed when we upgrade to the
// 2022.04.06 version of financials
public class CashControlDocument extends GeneralLedgerPostingDocumentBase implements AmountTotaling,
        GeneralLedgerPendingEntrySource, ElectronicPaymentClaiming, GeneralLedgerPostingDocument {

    protected static final String NODE_ASSOCIATED_WITH_ELECTRONIC_PAYMENT = "AssociatedWithElectronicPayment";

    private static final Logger LOG = LogManager.getLogger();

    protected String referenceFinancialDocumentNumber;
    protected String proposalNumber;
    protected Integer universityFiscalYear;
    protected String universityFiscalPeriodCode;
    protected String customerPaymentMediumCode;
    protected KualiDecimal cashControlTotalAmount = KualiDecimal.ZERO;
    protected String lockboxNumber;
    protected String bankCode;

    protected Bank bank;
    protected PaymentMedium customerPaymentMedium;
    protected AccountingPeriod universityFiscalPeriod;
    protected AccountsReceivableDocumentHeader accountsReceivableDocumentHeader;

    protected List<CashControlDetail> cashControlDetails;
    protected List<ElectronicPaymentClaim> electronicPaymentClaims;

    private transient CashControlDocumentService cashControlDocumentService;

    public CashControlDocument() {
        super();
        accountsReceivableDocumentHeader = new AccountsReceivableDocumentHeader();
        customerPaymentMedium = new PaymentMedium();

        // Set the university fiscal year to the current values
        UniversityDateService universityDateService = SpringContext.getBean(UniversityDateService.class);
        universityFiscalYear = universityDateService.getCurrentUniversityDate().getUniversityFiscalYear();
        universityFiscalPeriod = universityDateService.getCurrentUniversityDate().getAccountingPeriod();
        universityFiscalPeriodCode = universityDateService.getCurrentUniversityDate().getUniversityFiscalAccountingPeriod();

        cashControlDetails = new ArrayList<>();
        electronicPaymentClaims = new ArrayList<>();
        // retrieve value from param table and set to default
        try {
            DocumentDictionaryService docDictionaryService = SpringContext.getBean(DocumentDictionaryService.class);
            DataDictionaryService ddService = SpringContext.getBean(DataDictionaryService.class);
            DocumentEntry docEntry = docDictionaryService.getDocumentEntry(
                    ddService.getValidDocumentClassByTypeName(ArConstants.ArDocumentTypeCodes.CASH_CONTROL)
                            .getCanonicalName());
            String documentTypeCode = docEntry.getDocumentTypeName();
            if (SpringContext.getBean(BankService.class).isBankSpecificationEnabled()) {
                bankCode = SpringContext.getBean(ParameterService.class).getSubParameterValueAsString(Bank.class,
                        KFSParameterKeyConstants.DEFAULT_BANK_BY_DOCUMENT_TYPE, documentTypeCode);
            }
        } catch (Exception x) {
            LOG.error("Problem occurred setting default bank code for cash control document", x);
        }
    }

    @Override
    public String getDocumentNumber() {
        return documentNumber;
    }

    @Override
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getReferenceFinancialDocumentNumber() {
        return referenceFinancialDocumentNumber;
    }

    public void setReferenceFinancialDocumentNumber(String referenceFinancialDocumentNumber) {
        this.referenceFinancialDocumentNumber = referenceFinancialDocumentNumber;
    }

    public Integer getUniversityFiscalYear() {
        return universityFiscalYear;
    }

    public void setUniversityFiscalYear(Integer universityFiscalYear) {
        this.universityFiscalYear = universityFiscalYear;
    }

    public String getUniversityFiscalPeriodCode() {
        return universityFiscalPeriodCode;
    }

    public void setUniversityFiscalPeriodCode(String universityFiscalPeriodCode) {
        this.universityFiscalPeriodCode = universityFiscalPeriodCode;
    }

    public String getCustomerPaymentMediumCode() {
        return customerPaymentMediumCode;
    }

    public void setCustomerPaymentMediumCode(String customerPaymentMediumCode) {
        this.customerPaymentMediumCode = customerPaymentMediumCode;
    }

    public KualiDecimal getCashControlTotalAmount() {
        return cashControlTotalAmount;
    }

    public void setCashControlTotalAmount(KualiDecimal cashControlTotalAmount) {
        this.cashControlTotalAmount = cashControlTotalAmount;
    }

    public AccountingPeriod getUniversityFiscalPeriod() {
        return universityFiscalPeriod;
    }

    @Deprecated
    public void setUniversityFiscalPeriod(AccountingPeriod universityFiscalPeriod) {
        this.universityFiscalPeriod = universityFiscalPeriod;
    }

    public AccountsReceivableDocumentHeader getAccountsReceivableDocumentHeader() {
        return accountsReceivableDocumentHeader;
    }

    public void setAccountsReceivableDocumentHeader(AccountsReceivableDocumentHeader accountsReceivableDocumentHeader) {
        this.accountsReceivableDocumentHeader = accountsReceivableDocumentHeader;
    }

    public List<CashControlDetail> getCashControlDetails() {
        return cashControlDetails;
    }

    public void setCashControlDetails(List<CashControlDetail> cashControlDetails) {
        this.cashControlDetails = cashControlDetails;
    }

    /**
     * This method adds a new cash control detail to the list.
     *
     * @param cashControlDetail {@link CashControlDetail} to add.
     */
    public void addCashControlDetail(CashControlDetail cashControlDetail) {
        prepareCashControlDetail(cashControlDetail);
        if (cashControlDetail.getFinancialDocumentLineAmount() != null) {
            this.cashControlTotalAmount = this.cashControlTotalAmount.add(
                    cashControlDetail.getFinancialDocumentLineAmount());
        }
        cashControlDetails.add(cashControlDetail);
    }

    /**
     * This is a helper method that automatically populates document specific information into the cash control detail
     * deposit {@link CashControlDetail} instance.
     */
    protected void prepareCashControlDetail(CashControlDetail cashControlDetail) {
        cashControlDetail.setDocumentNumber(this.getDocumentNumber());
    }

    public PaymentMedium getCustomerPaymentMedium() {
        return customerPaymentMedium;
    }

    public void setCustomerPaymentMedium(PaymentMedium customerPaymentMedium) {
        this.customerPaymentMedium = customerPaymentMedium;
    }

    @Override
    public KualiDecimal getTotalDollarAmount() {
        return getCashControlTotalAmount();
    }

    /**
     * @return the advance deposit total amount as a currency formatted string.
     */
    public String getCurrencyFormattedTotalCashControlAmount() {
        return (String) new CurrencyFormatter().format(getCashControlTotalAmount());
    }

    /**
     * @param index the index of the cash control details to retrieve the cash control detail from
     * @return a specific CashControlDetail from the list, by array index.
     */
    public CashControlDetail getCashControlDetail(int index) {
        if (index >= cashControlDetails.size()) {
            for (int i = cashControlDetails.size(); i <= index; i++) {
                cashControlDetails.add(new CashControlDetail());
            }
        }
        return cashControlDetails.get(index);
    }

    public void customizeExplicitGeneralLedgerPendingEntry(GeneralLedgerPendingEntrySourceDetail postable,
            GeneralLedgerPendingEntry explicitEntry) {
        if (explicitEntry.getFinancialDocumentTypeCode().equalsIgnoreCase(
                KFSConstants.FinancialDocumentTypeCodes.GENERAL_ERROR_CORRECTION)) {
            explicitEntry.setTransactionLedgerEntryDescription(
                    buildTransactionLedgerEntryDescriptionUsingRefOriginAndRefDocNumber(postable));

            // Clearing fields that are already handled by the parent algorithm - we don't actually want these to copy
            // over from the accounting lines b/c they don't belong in the GLPEs if the aren't nulled, then GECs fail to
            // post
            explicitEntry.setReferenceFinancialDocumentNumber(null);
            explicitEntry.setReferenceFinancialSystemOriginationCode(null);
            explicitEntry.setReferenceFinancialDocumentTypeCode(null);
        }
    }

    /**
     * Builds an appropriately formatted string to be used for the {@code transactionLedgerEntryDescription}. It is
     * built using information from the {@link AccountingLine}. Format is "01-12345: blah blah blah".
     *
     * @param line accounting line
     * @return String formatted string to be used for transaction ledger entry description
     */
    protected String buildTransactionLedgerEntryDescriptionUsingRefOriginAndRefDocNumber(
            GeneralLedgerPendingEntrySourceDetail line) {
        String description = line.getReferenceOriginCode() + "-" + line.getReferenceNumber();

        if (StringUtils.isNotBlank(line.getFinancialDocumentLineDescription())) {
            description += ": " + line.getFinancialDocumentLineDescription();
        } else {
            description += ": " + getDocumentHeader().getDocumentDescription();
        }

        if (description.length() > GENERAL_LEDGER_PENDING_ENTRY_CODE.GLPE_DESCRIPTION_MAX_LENGTH) {
            description = description.substring(0, GENERAL_LEDGER_PENDING_ENTRY_CODE.GLPE_DESCRIPTION_MAX_LENGTH - 3) +
                    "...";
        }

        return description;
    }

    public boolean customizeOffsetGeneralLedgerPendingEntry(GeneralLedgerPendingEntrySourceDetail accountingLine,
            GeneralLedgerPendingEntry explicitEntry, GeneralLedgerPendingEntry offsetEntry) {
        return false;
    }

    @Override
    public boolean generateDocumentGeneralLedgerPendingEntries(GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        boolean success = true;
        final String customerPaymentMediumCode = getCustomerPaymentMediumCode();
        if (StringUtils.isNotBlank(customerPaymentMediumCode)) {

            if (customerPaymentMediumCode.equalsIgnoreCase(ArConstants.PaymentMediumCode.CHECK)) {
                success = getCashControlDocumentService().createCashReceiptGLPEs(this, sequenceHelper);
                success &= getCashControlDocumentService().createBankOffsetGLPEs(this, sequenceHelper);
            } else if (customerPaymentMediumCode.equalsIgnoreCase(ArConstants.PaymentMediumCode.WIRE_TRANSFER)) {
                success = getCashControlDocumentService().createDistributionOfIncomeAndExpenseGLPEs(this, sequenceHelper);
                success &= getCashControlDocumentService().createBankOffsetGLPEs(this, sequenceHelper);
            } else if (customerPaymentMediumCode.equalsIgnoreCase(ArConstants.PaymentMediumCode.CREDIT_CARD)) {
                success = getCashControlDocumentService().createGeneralErrorCorrectionGLPEs(this, sequenceHelper);
            }
        }

        return success;
    }

    @Override
    public KualiDecimal getGeneralLedgerPendingEntryAmountForDetail(GeneralLedgerPendingEntrySourceDetail postable) {
        return postable.getAmount().abs();
    }

    @Override
    public List<GeneralLedgerPendingEntrySourceDetail> getGeneralLedgerPendingEntrySourceDetails() {
        return new ArrayList<>();
    }

    /**
     * The Cash Control document doesn't generate general ledger pending entries based off of the accounting lines on
     * the document.
     */
    @Override
    public boolean generateGeneralLedgerPendingEntries(GeneralLedgerPendingEntrySourceDetail glpeSourceDetail,
            GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        return true;
    }

    @Override
    public Integer getPostingYear() {
        return SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear();
    }

    @Override
    public boolean isDebit(GeneralLedgerPendingEntrySourceDetail postable) {
        AccountingLine accountingLine = (AccountingLine) postable;
        return accountingLine.getDebitCreditCode().equalsIgnoreCase(KFSConstants.GL_DEBIT_CODE);
    }

    /**
     * This method sets glpes status to approved.
     */
    @Override
    public void changeGeneralLedgerPendingEntriesApprovedStatusCode() {
        for (GeneralLedgerPendingEntry glpe : getGeneralLedgerPendingEntries()) {
            glpe.setFinancialDocumentApprovedCode(KFSConstants.DocumentStatusCodes.APPROVED);
        }
    }

    public String getLockboxNumber() {
        this.lockboxNumber = getCashControlDocumentService().getLockboxNumber(this);
        return lockboxNumber;
    }

    @Override
    public void populateDocumentForRouting() {
        this.lockboxNumber = getCashControlDocumentService().getLockboxNumber(this);
        super.populateDocumentForRouting();
    }

    @Override
    public void declaimElectronicPaymentClaims() {
        SpringContext.getBean(ElectronicPaymentClaimingService.class).declaimElectronicPaymentClaimsForDocument(this);
    }

    public List<ElectronicPaymentClaim> getElectronicPaymentClaims() {
        return electronicPaymentClaims;
    }

    @Deprecated
    public void setElectronicPaymentClaims(List<ElectronicPaymentClaim> electronicPaymentClaims) {
        this.electronicPaymentClaims = electronicPaymentClaims;
    }

    public Document getReferenceFinancialDocument() {
        DocumentService documentService = SpringContext.getBean(DocumentService.class);
        return documentService.getByDocumentHeaderId(getReferenceFinancialDocumentNumber());
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    /**
     * Answers true when document payment medium is WIRE transfer.
     */
    @Override
    public boolean answerSplitNodeQuestion(String nodeName) throws UnsupportedOperationException {
        if (NODE_ASSOCIATED_WITH_ELECTRONIC_PAYMENT.equals(nodeName)) {
            return ArConstants.PaymentMediumCode.WIRE_TRANSFER.equals(getCustomerPaymentMediumCode());
        }
        return super.answerSplitNodeQuestion(nodeName);
    }

    /**
     * This is a helper method added to support workflow attribute configuration. This method helps to avoid attribute
     * name mismatch between ProcessingChartOfAccountCode and chartOfAccountsCode.
     *
     * @return ProcessingChartOfAccountCode
     */
    public String getChartOfAccountsCode() {
        if (getAccountsReceivableDocumentHeader() != null) {
            return getAccountsReceivableDocumentHeader().getProcessingChartOfAccountCode();
        }
        return null;
    }

    /**
     * This is a helper method added to support workflow attribute configuration. This method helps to avoid attribute
     * name mismatch between ProcessingOrganizationCode and organizationCode.
     *
     * @return ProcessingOrganizationCode
     */
    public String getOrganizationCode() {
        if (getAccountsReceivableDocumentHeader() != null) {
            return getAccountsReceivableDocumentHeader().getProcessingOrganizationCode();
        }
        return null;
    }

    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    public void recalculateTotals() {
        KualiDecimal total = KualiDecimal.ZERO;
        for (CashControlDetail cashControlDetail : getCashControlDetails()) {
            total = total.add(cashControlDetail.getFinancialDocumentLineAmount());
        }
        cashControlTotalAmount = total;
        getDocumentHeader().setFinancialDocumentTotalAmount(total);

        // We only want to recalculate GLPEs for pending statuses, otherwise the GLPE approved indicator can get reset
        // from 'A' to 'N' and GLPEs won't be correctly processed into G/L entries / cleared.
        if (getDocumentHeader().getWorkflowDocument().isInitiated()
                || getDocumentHeader().getWorkflowDocument().isSaved()
                || getDocumentHeader().getWorkflowDocument().isEnroute()
                || getDocumentHeader().getWorkflowDocument().isException()) {
            recalculateGeneralLedgerPendingEntries();
        }
    }

    private void recalculateGeneralLedgerPendingEntries() {
        if (!getGlpeService().generateGeneralLedgerPendingEntries(this)) {
            logErrors();
            throw new ValidationException("general ledger GLPE generation failed");
        }

        getCashControlDocumentService().saveGLPEs(this);
    }

    @Override
    public void prepareForSave() {
        captureWorkflowHeaderInformation();

        // remove all the cash control detail records from the db in prep for the save, where they'll get re-persisted.
        // This is necessary to make sure that details deleted on the form are actually deleted, as OJB does a terrible
        // job at this by itself.
        deleteCashControlDetailsFromDB();

        recalculateTotals();
    }

    protected void deleteCashControlDetailsFromDB() {
        BusinessObjectService boService = SpringContext.getBean(BusinessObjectService.class);
        Map<String, String> pkMap = new HashMap<>();
        pkMap.put(KFSPropertyConstants.DOCUMENT_NUMBER, getDocumentNumber());
        boService.deleteMatching(CashControlDetail.class, pkMap);
    }

    /**
     * This is a method to check the count of gl entries according to the input fields and values.
     *
     * @return totalGLRecordsCreated returns the count of the gl entries
     */
    public Integer getGeneralLedgerEntriesPostedCount() {
        Map<String, Object> pkMap = new HashMap<>();
        pkMap.put(KFSPropertyConstants.DOCUMENT_NUMBER, this.getDocumentNumber());
        pkMap.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, this.getPostingYear().toString());
        pkMap.put(KFSPropertyConstants.UNIVERSITY_FISCAL_PERIOD_CODE, this.getPostingPeriodCode());
        pkMap.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, this.getChartOfAccountsCode());
        return SpringContext.getBean(EntryService.class).getEntryRecordCount(pkMap);
    }

    public String getProposalNumber() {
        return proposalNumber;
    }

    public void setProposalNumber(String proposalNumber) {
        this.proposalNumber = proposalNumber;
    }

    public CashControlDocumentService getCashControlDocumentService() {
        if (cashControlDocumentService == null) {
            cashControlDocumentService = SpringContext.getBean(CashControlDocumentService.class);
        }
        return cashControlDocumentService;
    }
}
