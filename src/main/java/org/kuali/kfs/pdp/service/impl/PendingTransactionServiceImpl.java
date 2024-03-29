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
package org.kuali.kfs.pdp.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.businessobject.OffsetDefinition;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.coa.service.OffsetDefinitionService;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.datadictionary.legacy.BusinessObjectDictionaryService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.fp.FPKeyConstants;
import org.kuali.kfs.kns.datadictionary.BusinessObjectEntry;
import org.kuali.kfs.fp.FPKeyConstants;
import org.kuali.kfs.datadictionary.legacy.BusinessObjectDictionaryService;
import org.kuali.kfs.krad.datadictionary.AttributeDefinition;
import org.kuali.kfs.krad.datadictionary.AttributeSecurity;
import org.kuali.kfs.krad.datadictionary.mask.MaskFormatterLiteral;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.businessobject.CustomerProfile;
import org.kuali.kfs.pdp.businessobject.GlPendingTransaction;
import org.kuali.kfs.pdp.businessobject.PaymentAccountDetail;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.dataaccess.PendingTransactionDao;
import org.kuali.kfs.pdp.service.PdpUtilService;
import org.kuali.kfs.pdp.service.PendingTransactionService;
import org.kuali.kfs.pdp.service.ResearchParticipantPaymentValidationService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.service.BankService;
import org.kuali.kfs.sys.service.FlexibleOffsetAccountService;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Transactional
public class PendingTransactionServiceImpl implements PendingTransactionService {

    private static final Logger LOG = LogManager.getLogger();

    public static final String REF_FDOC_TYP_CD_LIABILITY_CHECK = "PO";

    private PendingTransactionDao glPendingTransactionDao;
    private AccountingPeriodService accountingPeriodService;
    private DateTimeService dateTimeService;
    private ConfigurationService kualiConfigurationService;
    private BusinessObjectDictionaryService businessObjectDictionaryService;
    private BusinessObjectService businessObjectService;
    private BankService bankService;
    protected DataDictionaryService dataDictionaryService;
    protected ParameterService parameterService;
    private ResearchParticipantPaymentValidationService researchParticipantPaymentValidationService;
    // CU Customization: Change pdpUtilService service variable from private to protected.
    protected PdpUtilService pdpUtilService;
    private OffsetDefinitionService offsetDefinitionService;
    private FlexibleOffsetAccountService flexibleOffsetAccountService;

    public PendingTransactionServiceImpl() {
        super();
    }

    @Override
    public void generatePaymentGeneralLedgerPendingEntry(final PaymentGroup paymentGroup) {
        populatePaymentGeneralLedgerPendingEntry(
                paymentGroup,
                GeneratePdpGlpeState.forProcess());
    }

    @Override
    public void generateCancellationGeneralLedgerPendingEntry(final PaymentGroup paymentGroup) {
        populatePaymentGeneralLedgerPendingEntry(
                paymentGroup,
                GeneratePdpGlpeState.forCancel());
    }

    @Override
    public void generateCancelReissueGeneralLedgerPendingEntry(final PaymentGroup paymentGroup) {
        populatePaymentGeneralLedgerPendingEntry(
                paymentGroup,
                GeneratePdpGlpeState.forCancelReissue());
    }

    @Override
    public void generateReissueGeneralLedgerPendingEntries(final PaymentGroup paymentGroup) {
        populatePaymentGeneralLedgerPendingEntry(
                paymentGroup,
                GeneratePdpGlpeState.forReissue());
        populatePaymentGeneralLedgerPendingEntry(
                paymentGroup,
                GeneratePdpGlpeState.forReissueReverse());
    }

    /**
     * Populates and stores a new GLPE for each account detail in the payment group.
     *
     * @param paymentGroup     payment group to generate entries for
     */
    private void populatePaymentGeneralLedgerPendingEntry(
            final PaymentGroup paymentGroup,
            final GeneratePdpGlpeState state
    ) {
        final List<PaymentAccountDetail> accountListings = new ArrayList<>();
        for (final PaymentDetail paymentDetail : paymentGroup.getPaymentDetails()) {
            accountListings.addAll(paymentDetail.getAccountDetail());
        }

        final GeneralLedgerPendingEntrySequenceHelper sequenceHelper = new GeneralLedgerPendingEntrySequenceHelper();
        for (final PaymentAccountDetail paymentAccountDetail : accountListings) {
            final GlPendingTransaction glPendingTransaction = new GlPendingTransaction();
            glPendingTransaction.setSequenceNbr(new KualiInteger(sequenceHelper.getSequenceCounter()));

            glPendingTransaction.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);

            final Date transactionTimestamp = new Date(dateTimeService.getCurrentDate().getTime());
            glPendingTransaction.setTransactionDt(transactionTimestamp);
            final AccountingPeriod fiscalPeriod = accountingPeriodService.getByDate(new java.sql.Date(transactionTimestamp.getTime()));
            glPendingTransaction.setUniversityFiscalYear(fiscalPeriod.getUniversityFiscalYear());
            glPendingTransaction.setUnivFiscalPrdCd(fiscalPeriod.getUniversityFiscalPeriodCode());

            glPendingTransaction.setAccountNumber(paymentAccountDetail.getAccountNbr());
            glPendingTransaction.setSubAccountNumber(paymentAccountDetail.getSubAccountNbr());
            glPendingTransaction.setChartOfAccountsCode(paymentAccountDetail.getFinChartCode());

            glPendingTransaction.setProjectCd(paymentAccountDetail.getProjectCode());
            glPendingTransaction.setDebitCrdtCd(pdpUtilService.isDebit(paymentAccountDetail, state.isReversal()) ?
                    KFSConstants.GL_DEBIT_CODE : KFSConstants.GL_CREDIT_CODE);
            glPendingTransaction.setAmount(paymentAccountDetail.getAccountNetAmount().abs());

            //Changes for Research Participant Upload
            String trnDesc = StringUtils.EMPTY;
            final CustomerProfile customerProfile = paymentGroup.getBatch().getCustomerProfile();

            if (researchParticipantPaymentValidationService.isResearchParticipantPayment(customerProfile)) {
                final BusinessObjectEntry businessObjectEntry = businessObjectDictionaryService.getBusinessObjectEntry(
                        PaymentDetail.class.getName());
                final AttributeDefinition attributeDefinition = businessObjectEntry.getAttributeDefinition(
                        "paymentGroup.payeeName");
                final AttributeSecurity originalPayeeNameAttributeSecurity = attributeDefinition.getAttributeSecurity();
                //This is a temporary work around for an issue introduced with KFSCNTRB-705.
                if (ObjectUtils.isNotNull(originalPayeeNameAttributeSecurity)) {
                    trnDesc = ((MaskFormatterLiteral) originalPayeeNameAttributeSecurity.getMaskFormatter()).getLiteral();
                }
            } else {
                final String payeeName = paymentGroup.getPayeeName();
                if (StringUtils.isNotBlank(payeeName)) {
                    trnDesc = payeeName.length() > 40 ? payeeName.substring(0, 40) : StringUtils.rightPad(payeeName, 40);
                }

                if (state.isReversal()) {
                    final String poNbr = paymentAccountDetail.getPaymentDetail().getPurchaseOrderNbr();
                    if (StringUtils.isNotBlank(poNbr)) {
                        trnDesc += " " + (poNbr.length() > 9 ? poNbr.substring(0, 9) : StringUtils.rightPad(poNbr, 9));
                    }

                    final String invoiceNbr = paymentAccountDetail.getPaymentDetail().getInvoiceNbr();
                    if (StringUtils.isNotBlank(invoiceNbr)) {
                        trnDesc += " " + (invoiceNbr.length() > 14 ? invoiceNbr.substring(0, 14) :
                                StringUtils.rightPad(invoiceNbr, 14));
                    }

                    if (trnDesc.length() > 40) {
                        trnDesc = trnDesc.substring(0, 40);
                    }
                }
            }
            state.setTransactionDescription(trnDesc);

            glPendingTransaction.setOrgDocNbr(paymentAccountDetail.getPaymentDetail().getOrganizationDocNbr());
            glPendingTransaction.setOrgReferenceId(paymentAccountDetail.getOrgReferenceId());

            state.setRelieveLiabilities(paymentGroup.getBatch().getCustomerProfile().getRelieveLiabilities());
            state.setOffsetDefinitionObjectCode(offsetDefinitionService.getActiveByPrimaryId(
                        glPendingTransaction.getUniversityFiscalYear(),
                        glPendingTransaction.getChartOfAccountsCode(),
                        paymentAccountDetail.getPaymentDetail().getFinancialDocumentTypeCode(),
                        glPendingTransaction.getFinancialBalanceTypeCode())
                    .map(OffsetDefinition::getFinancialObjectCode)
                    .orElse(paymentAccountDetail.getFinObjectCode()));
            if (state.isExpenseOrLiability() && state.isRelieveLiabilities()) {
                updateGeneralLedgerPendingEntryAsExpenseOrLiability(
                        state,
                        paymentAccountDetail,
                        glPendingTransaction);
            } else {
                updateGeneralLedgerPendingEntryAsPayment(
                        paymentGroup.getDisbursementType().getCode(),
                        paymentGroup.getDisbursementNbr(),
                        state,
                        paymentAccountDetail,
                        glPendingTransaction);
            }

            // update the offset account if necessary
            flexibleOffsetAccountService.updateOffset(glPendingTransaction);

            businessObjectService.save(glPendingTransaction);

            sequenceHelper.increment();

            if (bankService.isBankSpecificationEnabled() && !state.isExpenseOrLiability()) {
                populateBankOffsetEntry(paymentGroup, glPendingTransaction, sequenceHelper);
            }
        }
    }

    /**
     * Helper to update the GLPE as an expense or liability entry. Liability offset entry can be created by scrubber.
     * However, functional requirement is that reference fields are set, the scrubber would not do that. So we create
     * liabilities here.
     *
     * @param paymentAccountDetail current payment detail used to update the GLPE
     * @param glPendingTransaction to be updated
     */
    private void updateGeneralLedgerPendingEntryAsExpenseOrLiability(
            final GeneratePdpGlpeState state,
            final PaymentAccountDetail paymentAccountDetail,
            final GlPendingTransaction glPendingTransaction
    ) {
        if (state.isReversal()) {
            glPendingTransaction.setFinObjTypCd(KFSConstants.BasicAccountingCategoryCodes.LIABILITIES);
            glPendingTransaction.setFinancialObjectCode(state.getOffsetDefinitionObjectCode());
            glPendingTransaction.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
            glPendingTransaction.setDescription(KFSConstants.GL_PE_OFFSET_STRING);
        } else {
            glPendingTransaction.setFinObjTypCd(KFSConstants.BasicAccountingCategoryCodes.EXPENSES);
            glPendingTransaction.setFinancialObjectCode(paymentAccountDetail.getFinObjectCode());
            glPendingTransaction.setFinancialSubObjectCode(paymentAccountDetail.getFinSubObjectCode());
            glPendingTransaction.setDescription(state.getTransactionDescription());
        }

        glPendingTransaction.setFinancialDocumentTypeCode(paymentAccountDetail.getPaymentDetail().getFinancialDocumentTypeCode());

        glPendingTransaction.setFdocNbr(paymentAccountDetail.getPaymentDetail().getCustPaymentDocNbr());
        glPendingTransaction.setFsOriginCd(paymentAccountDetail.getPaymentDetail().getFinancialSystemOriginCode());

        // Will be blank for DVs. Will be populated for CMs even when they are created from a PREQ
        glPendingTransaction.setFdocRefNbr(paymentAccountDetail.getPaymentDetail().getPurchaseOrderNbr());
        if (StringUtils.isNotBlank(paymentAccountDetail.getPaymentDetail().getPurchaseOrderNbr())) {
            glPendingTransaction.setFdocRefTypCd(REF_FDOC_TYP_CD_LIABILITY_CHECK);
            glPendingTransaction.setFsRefOriginCd(paymentAccountDetail.getPaymentDetail().getFinancialSystemOriginCode());
        }
    }

    /**
     * Helper to update the GLPE as a payment entry.
     *
     * @param disbursementType payment group's disbursement type used to update the GLPE
     * @param disbursementNbr payment group's disbursement number used to update the GLPE
     * @param paymentAccountDetail current payment detail used to update the GLPE
     * @param glPendingTransaction to be updated
     */
    private void updateGeneralLedgerPendingEntryAsPayment(
            final String disbursementType,
            final KualiInteger disbursementNbr,
            final GeneratePdpGlpeState state,
            final PaymentAccountDetail paymentAccountDetail,
            final GlPendingTransaction glPendingTransaction
    ) {
        if (state.isRelieveLiabilities()
                && isAchCheckDisbursementType(disbursementType)
                && paymentAccountDetail.getPaymentDetail().getFinancialDocumentTypeCode() != null) {
            glPendingTransaction.setFinancialObjectCode(state.getOffsetDefinitionObjectCode());
            glPendingTransaction.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
        } else {
            glPendingTransaction.setFinancialObjectCode(paymentAccountDetail.getFinObjectCode());
            glPendingTransaction.setFinancialSubObjectCode(paymentAccountDetail.getFinSubObjectCode());
        }

        glPendingTransaction.setDescription(state.getTransactionDescription());

        glPendingTransaction.setFinancialDocumentTypeCode(
                state.documentTypeForDisbursementType(disbursementType));

        glPendingTransaction.setFdocNbr(disbursementNbr.toString());
        glPendingTransaction.setFsOriginCd(PdpConstants.PDP_FDOC_ORIGIN_CODE);

        if (StringUtils.isNotBlank(paymentAccountDetail.getPaymentDetail().getFinancialSystemOriginCode()) && StringUtils.isNotBlank(paymentAccountDetail.getPaymentDetail().getFinancialDocumentTypeCode())) {
            glPendingTransaction.setFdocRefTypCd(paymentAccountDetail.getPaymentDetail().getFinancialDocumentTypeCode());
            glPendingTransaction.setFsRefOriginCd(paymentAccountDetail.getPaymentDetail().getFinancialSystemOriginCode());
        } else {
            glPendingTransaction.setFdocRefTypCd(PdpConstants.PDP_FDOC_TYPE_CODE);
            glPendingTransaction.setFsRefOriginCd(PdpConstants.PDP_FDOC_ORIGIN_CODE);
        }

        glPendingTransaction.setFdocRefNbr(paymentAccountDetail.getPaymentDetail().getCustPaymentDocNbr());
    }

    private static boolean isAchCheckDisbursementType(final String disbursementType) {
        return PdpConstants.DisbursementTypeCodes.ACH.equals(disbursementType)
                || PdpConstants.DisbursementTypeCodes.CHECK.equals(disbursementType);
    }

    /**
     * Generates the bank offset for an entry (when enabled in the system)
     *
     * @param paymentGroup         PaymentGroup for which entries are being generated, contains the Bank
     * @param glPendingTransaction PDP entry created for payment detail
     * @param sequenceHelper       holds current entry sequence value
     */
    @Override
    public void populateBankOffsetEntry(final PaymentGroup paymentGroup, final GlPendingTransaction glPendingTransaction, final GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        final GlPendingTransaction bankPendingTransaction = new GlPendingTransaction();

        bankPendingTransaction.setSequenceNbr(new KualiInteger(sequenceHelper.getSequenceCounter()));
        bankPendingTransaction.setFdocRefTypCd(null);
        bankPendingTransaction.setFsRefOriginCd(null);
        bankPendingTransaction.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);
        bankPendingTransaction.setTransactionDt(glPendingTransaction.getTransactionDt());
        bankPendingTransaction.setUniversityFiscalYear(glPendingTransaction.getUniversityFiscalYear());
        bankPendingTransaction.setUnivFiscalPrdCd(glPendingTransaction.getUnivFiscalPrdCd());
        bankPendingTransaction.setFinancialDocumentTypeCode(glPendingTransaction.getFinancialDocumentTypeCode());
        bankPendingTransaction.setFsOriginCd(glPendingTransaction.getFsOriginCd());
        bankPendingTransaction.setFdocNbr(glPendingTransaction.getFdocNbr());

        final Bank bank = paymentGroup.getBank();
        bankPendingTransaction.setChartOfAccountsCode(bank.getCashOffsetFinancialChartOfAccountCode());
        bankPendingTransaction.setAccountNumber(bank.getCashOffsetAccountNumber());
        if (StringUtils.isBlank(bank.getCashOffsetSubAccountNumber())) {
            bankPendingTransaction.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());
        } else {
            bankPendingTransaction.setSubAccountNumber(bank.getCashOffsetSubAccountNumber());
        }

        bankPendingTransaction.setFinancialObjectCode(bank.getCashOffsetObjectCode());
        if (StringUtils.isBlank(bank.getCashOffsetSubObjectCode())) {
            bankPendingTransaction.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
        } else {
            bankPendingTransaction.setFinancialSubObjectCode(bank.getCashOffsetSubObjectCode());
        }
        bankPendingTransaction.setProjectCd(KFSConstants.getDashProjectCode());

        if (KFSConstants.GL_CREDIT_CODE.equals(glPendingTransaction.getDebitCrdtCd())) {
            bankPendingTransaction.setDebitCrdtCd(KFSConstants.GL_DEBIT_CODE);
        } else {
            bankPendingTransaction.setDebitCrdtCd(KFSConstants.GL_CREDIT_CODE);
        }
        bankPendingTransaction.setAmount(glPendingTransaction.getAmount());

        final String description = kualiConfigurationService.getPropertyValueAsString(FPKeyConstants.DESCRIPTION_GLPE_BANK_OFFSET);
        bankPendingTransaction.setDescription(description);
        bankPendingTransaction.setOrgDocNbr(glPendingTransaction.getOrgDocNbr());
        bankPendingTransaction.setOrgReferenceId(null);
        bankPendingTransaction.setFdocRefNbr(null);

        businessObjectService.save(bankPendingTransaction);

        sequenceHelper.increment();
    }

    @Override
    public void save(final GlPendingTransaction tran) {
        LOG.debug("save() started");

        businessObjectService.save(tran);
    }

    @Override
    public Iterator<GlPendingTransaction> getUnextractedTransactions() {
        LOG.debug("getUnextractedTransactions() started");

        return glPendingTransactionDao.getUnextractedTransactions();
    }

    @Override
    public void clearExtractedTransactions() {
        glPendingTransactionDao.clearExtractedTransactions();
    }

    protected BankService getBankService() {
        return bankService;
    }

    public void setBankService(final BankService bankService) {
        this.bankService = bankService;
    }

    public void setGlPendingTransactionDao(final PendingTransactionDao glPendingTransactionDao) {
        this.glPendingTransactionDao = glPendingTransactionDao;
    }

    // known user: UCI
    protected AccountingPeriodService getAccountingPeriodService() {
        return accountingPeriodService;
    }

    public void setAccountingPeriodService(final AccountingPeriodService accountingPeriodService) {
        this.accountingPeriodService = accountingPeriodService;
    }

    // known user: UCI
    protected DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(final DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setConfigurationService(final ConfigurationService kualiConfigurationService) {
        this.kualiConfigurationService = kualiConfigurationService;
    }

    // known user: Cornell
    protected BusinessObjectDictionaryService getBusinessObjectDictionaryService() {
        return businessObjectDictionaryService;
    }

    public void setBusinessObjectDictionaryService(
            final BusinessObjectDictionaryService businessObjectDictionaryService) {
        this.businessObjectDictionaryService = businessObjectDictionaryService;
    }

    // known user: UCI
    protected BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setKualiConfigurationService(final ConfigurationService kualiConfigurationService) {
        this.kualiConfigurationService = kualiConfigurationService;
    }

    public void setDataDictionaryService(final DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public void setParameterService(final ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public ResearchParticipantPaymentValidationService getResearchParticipantPaymentValidationService() {
        return researchParticipantPaymentValidationService;
    }

    public void setResearchParticipantPaymentValidationService(
            final ResearchParticipantPaymentValidationService researchParticipantPaymentValidationService) {
        this.researchParticipantPaymentValidationService = researchParticipantPaymentValidationService;
    }

    public void setPdpUtilService(final PdpUtilService pdpUtilService) {
        this.pdpUtilService = pdpUtilService;
    }

    public void setOffsetDefinitionService(final OffsetDefinitionService offsetDefinitionService) {
        this.offsetDefinitionService = offsetDefinitionService;
    }

    public void setFlexibleOffsetAccountService(final FlexibleOffsetAccountService flexibleOffsetAccountService) {
        this.flexibleOffsetAccountService = flexibleOffsetAccountService;
    }
}
