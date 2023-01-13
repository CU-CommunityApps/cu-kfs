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
package org.kuali.kfs.pdp.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.businessobject.OffsetDefinition;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.coa.service.OffsetDefinitionService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
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
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiInteger;
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
    public void generatePaymentGeneralLedgerPendingEntry(PaymentGroup paymentGroup) {
        this.populatePaymentGeneralLedgerPendingEntry(paymentGroup, PdpConstants.FDOC_TYP_CD_PROCESS_ACH,
                PdpConstants.FDOC_TYP_CD_PROCESS_CHECK, false, false);
    }

    @Override
    public void generateCancellationGeneralLedgerPendingEntry(PaymentGroup paymentGroup) {
        this.populatePaymentGeneralLedgerPendingEntry(paymentGroup, PdpConstants.FDOC_TYP_CD_CANCEL_ACH,
                PdpConstants.FDOC_TYP_CD_CANCEL_CHECK, true, false);
    }

    @Override
    public void generateCancelReissueGeneralLedgerPendingEntry(PaymentGroup paymentGroup) {
        this.populatePaymentGeneralLedgerPendingEntry(paymentGroup, PdpConstants.FDOC_TYP_CD_CANCEL_REISSUE_ACH,
                PdpConstants.FDOC_TYP_CD_CANCEL_REISSUE_CHECK, true, false);
    }

    @Override
    public void generateReissueGeneralLedgerPendingEntries(PaymentGroup paymentGroup) {
        this.populatePaymentGeneralLedgerPendingEntry(paymentGroup, null, null, false, true);
        this.populatePaymentGeneralLedgerPendingEntry(paymentGroup, null, null, true, true);
    }

    /**
     * Populates and stores a new GLPE for each account detail in the payment group.
     *
     * @param paymentGroup     payment group to generate entries for
     * @param achFdocTypeCode  doc type for ach disbursements
     * @param checkFdocTypeCod doc type for check disbursements
     * @param reversal         boolean indicating if this is a reversal
     * @param expenseOrLiability boolean indicating if these should be expense (reversal=false) or liability
     *                           (reversal=true) entries
     */
    protected void populatePaymentGeneralLedgerPendingEntry(PaymentGroup paymentGroup, String achFdocTypeCode,
            String checkFdocTypeCod, boolean reversal, boolean expenseOrLiability) {
        List<PaymentAccountDetail> accountListings = new ArrayList<>();
        for (PaymentDetail paymentDetail : paymentGroup.getPaymentDetails()) {
            accountListings.addAll(paymentDetail.getAccountDetail());
        }

        GeneralLedgerPendingEntrySequenceHelper sequenceHelper = new GeneralLedgerPendingEntrySequenceHelper();
        for (PaymentAccountDetail paymentAccountDetail : accountListings) {
            GlPendingTransaction glPendingTransaction = new GlPendingTransaction();
            glPendingTransaction.setSequenceNbr(new KualiInteger(sequenceHelper.getSequenceCounter()));

            glPendingTransaction.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);

            Date transactionTimestamp = new Date(dateTimeService.getCurrentDate().getTime());
            glPendingTransaction.setTransactionDt(transactionTimestamp);
            AccountingPeriod fiscalPeriod = accountingPeriodService.getByDate(new java.sql.Date(transactionTimestamp.getTime()));
            glPendingTransaction.setUniversityFiscalYear(fiscalPeriod.getUniversityFiscalYear());
            glPendingTransaction.setUnivFiscalPrdCd(fiscalPeriod.getUniversityFiscalPeriodCode());

            glPendingTransaction.setAccountNumber(paymentAccountDetail.getAccountNbr());
            glPendingTransaction.setSubAccountNumber(paymentAccountDetail.getSubAccountNbr());
            glPendingTransaction.setChartOfAccountsCode(paymentAccountDetail.getFinChartCode());

            glPendingTransaction.setProjectCd(paymentAccountDetail.getProjectCode());
            glPendingTransaction.setDebitCrdtCd(pdpUtilService.isDebit(paymentAccountDetail, reversal) ?
                    KFSConstants.GL_DEBIT_CODE : KFSConstants.GL_CREDIT_CODE);
            glPendingTransaction.setAmount(paymentAccountDetail.getAccountNetAmount().abs());

            //Changes for Research Participant Upload
            String trnDesc = StringUtils.EMPTY;
            CustomerProfile customerProfile = paymentGroup.getBatch().getCustomerProfile();

            if (researchParticipantPaymentValidationService.isResearchParticipantPayment(customerProfile)) {
                BusinessObjectEntry businessObjectEntry = businessObjectDictionaryService.getBusinessObjectEntry(
                        PaymentDetail.class.getName());
                AttributeDefinition attributeDefinition = businessObjectEntry.getAttributeDefinition(
                        "paymentGroup.payeeName");
                AttributeSecurity originalPayeeNameAttributeSecurity = attributeDefinition.getAttributeSecurity();
                //This is a temporary work around for an issue introduced with KFSCNTRB-705.
                if (ObjectUtils.isNotNull(originalPayeeNameAttributeSecurity)) {
                    trnDesc = ((MaskFormatterLiteral) originalPayeeNameAttributeSecurity.getMaskFormatter()).getLiteral();
                }
            } else {
                String payeeName = paymentGroup.getPayeeName();
                if (StringUtils.isNotBlank(payeeName)) {
                    trnDesc = payeeName.length() > 40 ? payeeName.substring(0, 40) : StringUtils.rightPad(payeeName, 40);
                }

                if (reversal) {
                    String poNbr = paymentAccountDetail.getPaymentDetail().getPurchaseOrderNbr();
                    if (StringUtils.isNotBlank(poNbr)) {
                        trnDesc += " " + (poNbr.length() > 9 ? poNbr.substring(0, 9) : StringUtils.rightPad(poNbr, 9));
                    }

                    String invoiceNbr = paymentAccountDetail.getPaymentDetail().getInvoiceNbr();
                    if (StringUtils.isNotBlank(invoiceNbr)) {
                        trnDesc += " " + (invoiceNbr.length() > 14 ? invoiceNbr.substring(0, 14) :
                                StringUtils.rightPad(invoiceNbr, 14));
                    }

                    if (trnDesc.length() > 40) {
                        trnDesc = trnDesc.substring(0, 40);
                    }
                }
            }
            glPendingTransaction.setOrgDocNbr(paymentAccountDetail.getPaymentDetail().getOrganizationDocNbr());
            glPendingTransaction.setOrgReferenceId(paymentAccountDetail.getOrgReferenceId());

            final boolean relieveLiabilities = paymentGroup.getBatch().getCustomerProfile().getRelieveLiabilities();
            final OffsetDefinition offsetDefinition = offsetDefinitionService.getByPrimaryId(
                    glPendingTransaction.getUniversityFiscalYear(), glPendingTransaction.getChartOfAccountsCode(),
                    paymentAccountDetail.getPaymentDetail().getFinancialDocumentTypeCode(),
                    glPendingTransaction.getFinancialBalanceTypeCode());
            if (expenseOrLiability && relieveLiabilities) {
                updateGeneralLedgerPendingEntryAsExpenseOrLiability(reversal, paymentAccountDetail, trnDesc,
                    offsetDefinition, glPendingTransaction);
            } else {
                updateGeneralLedgerPendingEntryAsCheck(relieveLiabilities, paymentGroup.getDisbursementType().getCode(),
                    paymentGroup.getDisbursementNbr(), achFdocTypeCode, checkFdocTypeCod, paymentAccountDetail, trnDesc,
                    offsetDefinition, glPendingTransaction);
            }

            // update the offset account if necessary
            flexibleOffsetAccountService.updateOffset(glPendingTransaction);

            businessObjectService.save(glPendingTransaction);

            sequenceHelper.increment();

            if (bankService.isBankSpecificationEnabled() && !expenseOrLiability) {
                populateBankOffsetEntry(paymentGroup, glPendingTransaction, sequenceHelper);
            }
        }
    }

    /**
     * Helper to update the GLPE as an expense or liability entry. Liability offset entry can be created by scrubber.
     * However, functional requirement is that reference fields are set, the scrubber would not do that. So we create
     * liabilities here.
     *
     * @param reversal boolean indicating if this is a reversal
     * @param paymentAccountDetail current payment detail used to update the GLPE
     * @param trnDesc transaction description used to update the GLPE
     * @param offsetDefinition offset definition used to update the GLPE
     * @param glPendingTransaction to be updated
     */
    private void updateGeneralLedgerPendingEntryAsExpenseOrLiability(boolean reversal,
            PaymentAccountDetail paymentAccountDetail, String trnDesc, OffsetDefinition offsetDefinition,
            GlPendingTransaction glPendingTransaction) {
        if (reversal) {
            glPendingTransaction.setFinObjTypCd(KFSConstants.BasicAccountingCategoryCodes.LIABILITIES);
            glPendingTransaction.setFinancialObjectCode(offsetDefinition != null ?
                    offsetDefinition.getFinancialObjectCode() : paymentAccountDetail.getFinObjectCode());
            glPendingTransaction.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
            glPendingTransaction.setDescription(KFSConstants.GL_PE_OFFSET_STRING);
        } else {
            glPendingTransaction.setFinObjTypCd(KFSConstants.BasicAccountingCategoryCodes.EXPENSES);
            glPendingTransaction.setFinancialObjectCode(paymentAccountDetail.getFinObjectCode());
            glPendingTransaction.setFinancialSubObjectCode(paymentAccountDetail.getFinSubObjectCode());
            glPendingTransaction.setDescription(trnDesc);
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
     * Helper to update the GLPE as a check entry.
     *
     * @param relieveLiabilities indicator from CustomerProfile. Determines how object code and sub object code are set
     * @param disbursementType payment group's disbursement type used to update the GLPE
     * @param disbursementNbr payment group's disbursement number used to update the GLPE
     * @param achFdocTypeCode doc type for ach disbursements used to update the GLPE
     * @param checkFdocTypeCod doc type for check disbursements used to update the GLPE
     * @param paymentAccountDetail current payment detail used to update the GLPE
     * @param trnDesc transaction description used to update the GLPE
     * @param offsetDefinition offset definition used to update the GLPE
     * @param glPendingTransaction to be updated
     */
    private void updateGeneralLedgerPendingEntryAsCheck(boolean relieveLiabilities, String disbursementType,
            KualiInteger disbursementNbr, String achFdocTypeCode, String checkFdocTypeCod,
            PaymentAccountDetail paymentAccountDetail, String trnDesc, OffsetDefinition offsetDefinition,
            GlPendingTransaction glPendingTransaction) {
        if (relieveLiabilities && paymentAccountDetail.getPaymentDetail().getFinancialDocumentTypeCode() != null) {
            glPendingTransaction.setFinancialObjectCode(offsetDefinition != null ?
                    offsetDefinition.getFinancialObjectCode() : paymentAccountDetail.getFinObjectCode());
            glPendingTransaction.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
        } else {
            glPendingTransaction.setFinancialObjectCode(paymentAccountDetail.getFinObjectCode());
            glPendingTransaction.setFinancialSubObjectCode(paymentAccountDetail.getFinSubObjectCode());
        }

        glPendingTransaction.setDescription(trnDesc);

        if (disbursementType.equals(PdpConstants.DisbursementTypeCodes.ACH)) {
            glPendingTransaction.setFinancialDocumentTypeCode(achFdocTypeCode);
        } else if (disbursementType.equals(PdpConstants.DisbursementTypeCodes.CHECK)) {
            glPendingTransaction.setFinancialDocumentTypeCode(checkFdocTypeCod);
        }

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

    /**
     * Generates the bank offset for an entry (when enabled in the system)
     *
     * @param paymentGroup         PaymentGroup for which entries are being generated, contains the Bank
     * @param glPendingTransaction PDP entry created for payment detail
     * @param sequenceHelper       holds current entry sequence value
     */
    @Override
    public void populateBankOffsetEntry(PaymentGroup paymentGroup, GlPendingTransaction glPendingTransaction, GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        GlPendingTransaction bankPendingTransaction = new GlPendingTransaction();

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

        Bank bank = paymentGroup.getBank();
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

        String description = kualiConfigurationService.getPropertyValueAsString(FPKeyConstants.DESCRIPTION_GLPE_BANK_OFFSET);
        bankPendingTransaction.setDescription(description);
        bankPendingTransaction.setOrgDocNbr(glPendingTransaction.getOrgDocNbr());
        bankPendingTransaction.setOrgReferenceId(null);
        bankPendingTransaction.setFdocRefNbr(null);

        this.businessObjectService.save(bankPendingTransaction);

        sequenceHelper.increment();
    }

    @Override
    public void save(GlPendingTransaction tran) {
        LOG.debug("save() started");

        this.businessObjectService.save(tran);
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

    public void setBankService(BankService bankService) {
        this.bankService = bankService;
    }

    public void setGlPendingTransactionDao(PendingTransactionDao glPendingTransactionDao) {
        this.glPendingTransactionDao = glPendingTransactionDao;
    }

    // known user: UCI
    protected AccountingPeriodService getAccountingPeriodService() {
        return accountingPeriodService;
    }

    public void setAccountingPeriodService(AccountingPeriodService accountingPeriodService) {
        this.accountingPeriodService = accountingPeriodService;
    }

    // known user: UCI
    protected DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setConfigurationService(ConfigurationService kualiConfigurationService) {
        this.kualiConfigurationService = kualiConfigurationService;
    }

    // known user: Cornell
    protected BusinessObjectDictionaryService getBusinessObjectDictionaryService() {
        return businessObjectDictionaryService;
    }

    public void setBusinessObjectDictionaryService(
            BusinessObjectDictionaryService businessObjectDictionaryService) {
        this.businessObjectDictionaryService = businessObjectDictionaryService;
    }

    // known user: UCI
    protected BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setKualiConfigurationService(ConfigurationService kualiConfigurationService) {
        this.kualiConfigurationService = kualiConfigurationService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public ResearchParticipantPaymentValidationService getResearchParticipantPaymentValidationService() {
        return researchParticipantPaymentValidationService;
    }

    public void setResearchParticipantPaymentValidationService(
            ResearchParticipantPaymentValidationService researchParticipantPaymentValidationService) {
        this.researchParticipantPaymentValidationService = researchParticipantPaymentValidationService;
    }

    public void setPdpUtilService(PdpUtilService pdpUtilService) {
        this.pdpUtilService = pdpUtilService;
    }

    public void setOffsetDefinitionService(OffsetDefinitionService offsetDefinitionService) {
        this.offsetDefinitionService = offsetDefinitionService;
    }

    public void setFlexibleOffsetAccountService(FlexibleOffsetAccountService flexibleOffsetAccountService) {
        this.flexibleOffsetAccountService = flexibleOffsetAccountService;
    }
}
