/*
 * Copyright 2010 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cornell.kfs.fp.service.impl;

import static org.kuali.kfs.module.purap.PurapConstants.PURAP_ORIGIN_CODE;
import static org.kuali.kfs.sys.KFSConstants.GL_CREDIT_CODE;
import static org.kuali.kfs.sys.KFSConstants.GL_DEBIT_CODE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.OffsetDefinition;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.coa.service.OffsetDefinitionService;
import org.kuali.kfs.fp.FPKeyConstants;
import org.kuali.kfs.gl.businessobject.Entry;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.GeneralLedgerPostingDocument;
import org.kuali.kfs.sys.document.service.AccountingDocumentRuleHelperService;
import org.kuali.kfs.sys.document.validation.impl.AccountingDocumentRuleBaseConstants.GENERAL_LEDGER_PENDING_ENTRY_CODE;
import org.kuali.kfs.sys.service.BankService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.springframework.cache.annotation.Cacheable;

import org.kuali.kfs.sys.businessobject.PaymentMethod;
import edu.cornell.kfs.fp.service.CUPaymentMethodGeneralLedgerPendingEntryService;
import edu.cornell.kfs.module.purap.document.CuPaymentRequestDocument;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.businessobject.PaymentMethodExtendedAttribute;
import edu.cornell.kfs.sys.service.CuGeneralLedgerPendingEntryService;

public class CUPaymentMethodGeneralLedgerPendingEntryServiceImpl implements CUPaymentMethodGeneralLedgerPendingEntryService {
	private static final Logger LOG = LogManager.getLogger(CUPaymentMethodGeneralLedgerPendingEntryServiceImpl.class);
    protected static final String DEFAULT_PAYMENT_METHOD_IF_MISSING = "A"; // check/ACH

    protected CuGeneralLedgerPendingEntryService generalLedgerPendingEntryService;
    protected ObjectCodeService objectCodeService;
    protected BusinessObjectService businessObjectService;
    protected BankService bankService;

    @Cacheable(value=SystemOptions.CACHE_NAME, key="'{isPaymentMethodProcessedUsingPdp}'+#p0")
    public boolean isPaymentMethodProcessedUsingPdp(String paymentMethodCode) {
        if ( StringUtils.isBlank(paymentMethodCode) ) {
            paymentMethodCode = DEFAULT_PAYMENT_METHOD_IF_MISSING;
        }
        PaymentMethod pm = businessObjectService.findBySinglePrimaryKey(PaymentMethod.class, paymentMethodCode);
        if ( pm != null ) {
            PaymentMethodExtendedAttribute extendedAttribute = (PaymentMethodExtendedAttribute)pm.getExtension();
            return extendedAttribute.isProcessedUsingPdp();
        }
        return false;
    }
    
    /**
     * This implementation will also return null if the bank code on the payment method record does not exist in the bank table.
     * 
     */
    public Bank getBankForPaymentMethod(String paymentMethodCode) {
        if ( StringUtils.isBlank(paymentMethodCode) ) {
            paymentMethodCode = DEFAULT_PAYMENT_METHOD_IF_MISSING;
        }
        PaymentMethod pm = businessObjectService.findBySinglePrimaryKey(PaymentMethod.class, paymentMethodCode);
        if ( pm != null ) {
            // if no bank code, short circuit and return null
            if ( pm.getBankCode() != null ) {
                return pm.getBank();
            }
        }
        return null;
    }
    
    /**
     * Generates additional document-level GL entries for the DV, depending on the payment method code. 
     * 
     * Return true if GLPE's are generated successfully (i.e. there are either 0 GLPE's or 1 GLPE in disbursement voucher document)
     * 
     * @param document submitted financial document
     * @param paymentMethodCode
     * @param bankCode
     * @param bankCodePropertyName
     * @param templatePendingEntry
     * @param feesWaived
     * @param reverseCharge
     * @param sequenceHelper helper class to keep track of GLPE sequence
     * @return true if GLPE's are generated successfully
     */
    public boolean generatePaymentMethodSpecificDocumentGeneralLedgerPendingEntries(
            AccountingDocument document, String paymentMethodCode, String bankCode, String bankCodePropertyName, 
            GeneralLedgerPendingEntry templatePendingEntry, 
            boolean feesWaived, boolean reverseCharge, GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        return generatePaymentMethodSpecificDocumentGeneralLedgerPendingEntries(document, paymentMethodCode, bankCode, bankCodePropertyName, templatePendingEntry, feesWaived, reverseCharge, sequenceHelper, null, null);
    }

    public boolean generatePaymentMethodSpecificDocumentGeneralLedgerPendingEntries(AccountingDocument document, 
            String paymentMethodCode, 
            String bankCode, 
            String bankCodePropertyName, 
            GeneralLedgerPendingEntry templatePendingEntry, 
            boolean feesWaived, 
            boolean reverseCharge, 
            GeneralLedgerPendingEntrySequenceHelper sequenceHelper, 
            KualiDecimal bankOffsetAmount, 
            Map<String, KualiDecimal> actualTotalsByChart) {

        if ( StringUtils.isBlank(paymentMethodCode) ) {
            paymentMethodCode = DEFAULT_PAYMENT_METHOD_IF_MISSING;
        }
        PaymentMethod pm = businessObjectService.findBySinglePrimaryKey(PaymentMethod.class, paymentMethodCode);
        // no payment method? abort.
        if ( pm == null ) {
            return false;
        }
        
        PaymentMethodExtendedAttribute extendedAttribute = (PaymentMethodExtendedAttribute)pm.getExtension();
        
        if ( !extendedAttribute.isProcessedUsingPdp() && StringUtils.isNotBlank( bankCode ) ) {
            if(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_WIRE.equalsIgnoreCase(paymentMethodCode) || KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_DRAFT.equalsIgnoreCase(paymentMethodCode)){
                //do not create bank offsets unless DM approval
            }
            else{
                generateDocumentBankOffsetEntries(document,bankCode,bankCodePropertyName,templatePendingEntry.getFinancialDocumentTypeCode(), sequenceHelper, bankOffsetAmount );
            }
        }
        
        return true;
    }

    
    /**
     * Adds up the amounts of all cash to offset GeneralLedgerPendingEntry records on the given AccountingDocument
     * 
     * MOD-PA2000-01 : Copied from the GL Pending entry service since that one does not make any distinction between
     * expense and encumbrance balance types
     * 
     * @author jonathan
     * 
     * @param glPostingDocument the accounting document total the offset to cash amount for
     * @return the offset to cash amount, where debited values have been subtracted and credited values have been added
     */
    protected Map<String,KualiDecimal> getNonOffsetActualTotalsByChart(GeneralLedgerPostingDocument glPostingDocument) {
        Map<String,KualiDecimal> totals = new HashMap<String, KualiDecimal>();
        for (GeneralLedgerPendingEntry glpe : glPostingDocument.getGeneralLedgerPendingEntries()) {
            if ( KFSConstants.BALANCE_TYPE_ACTUAL.equals(glpe.getFinancialBalanceTypeCode()) ) {
                if ( !glpe.isTransactionEntryOffsetIndicator() ) {
                    if ( !totals.containsKey(glpe.getChartOfAccountsCode() ) ) {
                        totals.put(glpe.getChartOfAccountsCode(), KualiDecimal.ZERO);
                    }
                    if (glpe.getTransactionDebitCreditCode().equals(KFSConstants.GL_DEBIT_CODE)) {
                        totals.put(glpe.getChartOfAccountsCode(),totals.get(glpe.getChartOfAccountsCode()).add(glpe.getTransactionLedgerEntryAmount()));
                    } else if (glpe.getTransactionDebitCreditCode().equals(KFSConstants.GL_CREDIT_CODE)) {
                        totals.put(glpe.getChartOfAccountsCode(),totals.get(glpe.getChartOfAccountsCode()).subtract(glpe.getTransactionLedgerEntryAmount()));
                    }
                }
            }
        }
        return totals;
    }    

    /**
     * If bank specification is enabled generates bank offsetting entries for the document amount
     * 
     */
    public boolean generateDocumentBankOffsetEntries(AccountingDocument document, String bankCode, String bankCodePropertyName, String documentTypeCode, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, KualiDecimal bankOffsetAmount ) {
        boolean success = true;

        if (!bankService.isBankSpecificationEnabled()) {
            return success;
        }
        Bank bank = bankService.getByPrimaryId(bankCode);

        if ( bankOffsetAmount == null ) {
            bankOffsetAmount = generalLedgerPendingEntryService.getOffsetToCashAmount(document).negated();
        }
        if ( !KualiDecimal.ZERO.equals(bankOffsetAmount) ) {
            GeneralLedgerPendingEntry bankOffsetEntry = new GeneralLedgerPendingEntry();
            success &= generalLedgerPendingEntryService
                    .populateBankOffsetGeneralLedgerPendingEntry(bank, bankOffsetAmount, document, 
                            document.getPostingYear(), sequenceHelper, bankOffsetEntry, bankCodePropertyName);
    
            if (success) {
                AccountingDocumentRuleHelperService accountingDocumentRuleUtil = SpringContext.getBean(AccountingDocumentRuleHelperService.class);
                bankOffsetEntry.setTransactionLedgerEntryDescription(accountingDocumentRuleUtil.formatProperty(FPKeyConstants.DESCRIPTION_GLPE_BANK_OFFSET));
                bankOffsetEntry.setFinancialDocumentTypeCode(documentTypeCode);
                document.addPendingEntry(bankOffsetEntry);
                sequenceHelper.increment();
    
                GeneralLedgerPendingEntry offsetEntry = new GeneralLedgerPendingEntry(bankOffsetEntry);
                success &= generalLedgerPendingEntryService.populateOffsetGeneralLedgerPendingEntry(document.getPostingYear(), bankOffsetEntry, sequenceHelper, offsetEntry);
                bankOffsetEntry.setFinancialDocumentTypeCode(documentTypeCode);

                document.addPendingEntry(offsetEntry);
                sequenceHelper.increment();
            }
        }

        return success;
    }

    /**
     * Creates final entries for PRNC doc: Reverse all usage 2900 object codes Replaces with 1000 offset object code Generate
     * Bank Offsets for total amounts
     * 
     * @see edu.cornell.kfs.fp.service.CUPaymentMethodGeneralLedgerPendingEntryService#generateFinalEntriesForPRNC(org.kuali.kfs.module.purap.document.PaymentRequestDocument)
     */
    public void generateFinalEntriesForPRNC(PaymentRequestDocument document) {

        GeneralLedgerPendingEntrySequenceHelper sequenceHelper = new GeneralLedgerPendingEntrySequenceHelper(getNextAvailableSequence(document.getDocumentNumber()));
        String documentType = CuPaymentRequestDocument.DOCUMENT_TYPE_NON_CHECK;
        if (CUKFSConstants.CuPaymentSourceConstants.PAYMENT_METHOD_INTERNAL_BILLING.equalsIgnoreCase(((CuPaymentRequestDocument)document).getPaymentMethodCode())){
        	documentType = CuPaymentRequestDocument.DOCUMENT_TYPE_INTERNAL_BILLING;
        }
        

        // generate bank offset
        if (KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_DRAFT.equalsIgnoreCase(((CuPaymentRequestDocument)document).getPaymentMethodCode()) || KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_WIRE.equalsIgnoreCase(((CuPaymentRequestDocument)document).getPaymentMethodCode())) {
            generateDocumentBankOffsetEntries((AccountingDocument) document, document.getBankCode(), KRADConstants.DOCUMENT_PROPERTY_NAME + "." + "bankCode", documentType, sequenceHelper, document.getTotalDollarAmount().negated());
        }

        // check for balance type Actual offset pending entries and replace the object code with chart cash object code (currently replacing object code 2900 with 1000)
        List<GeneralLedgerPendingEntry> glpes = document.getGeneralLedgerPendingEntries();

        for (GeneralLedgerPendingEntry glpe : glpes) {
            OffsetDefinition offsetDefinition = SpringContext.getBean(OffsetDefinitionService.class).getByPrimaryId(glpe.getUniversityFiscalYear(), glpe.getChartOfAccountsCode(), documentType, KFSConstants.BALANCE_TYPE_ACTUAL);
            if (glpe.getFinancialObjectCode().equalsIgnoreCase(offsetDefinition.getFinancialObjectCode())) {
                if (ObjectUtils.isNull(glpe.getChart())) {
                    glpe.refreshReferenceObject(KFSPropertyConstants.CHART);
                }
                glpe.setFinancialObjectCode(glpe.getChart().getFinancialCashObjectCode());
                glpe.refreshReferenceObject(KFSPropertyConstants.FINANCIAL_OBJECT);
                glpe.setFinancialObjectTypeCode(glpe.getFinancialObject().getFinancialObjectTypeCode());
            }
        }

        // reverse 2900
        // check for posted entries and create reverse pending entries
        // get all charts on document
        List<AccountingLine> accountingLines = new ArrayList<AccountingLine>();
        if (ObjectUtils.isNotNull(document.getSourceAccountingLines())) {
            accountingLines.addAll(document.getSourceAccountingLines());
        }
        if (ObjectUtils.isNotNull(document.getTargetAccountingLines())) {
            accountingLines.addAll(document.getTargetAccountingLines());
        }
        Map<String, String> chartOffsets = new HashMap<String, String>();
        if (accountingLines.size() > 0) {
            for (AccountingLine accountingLine : accountingLines) {
                if (!chartOffsets.containsKey(accountingLine.getChartOfAccountsCode())) {
                    OffsetDefinition offsetDefinition = SpringContext.getBean(OffsetDefinitionService.class).getByPrimaryId(accountingLine.getPostingYear(), accountingLine.getChartOfAccountsCode(), documentType, KFSConstants.BALANCE_TYPE_ACTUAL);
                    chartOffsets.put(accountingLine.getChartOfAccountsCode(), offsetDefinition.getFinancialObjectCode());

                }
            }
        }

        for (String offsetObjectCode : chartOffsets.values()) {
            Collection<Entry> glEntries = findMatchingGLEntries(document, offsetObjectCode);
            if (glEntries != null && glEntries.size() > 0) {
                for (Entry entry : glEntries) {
                    // create reversal
                    GeneralLedgerPendingEntry glpe = new GeneralLedgerPendingEntry();
                    boolean debit = KFSConstants.GL_CREDIT_CODE.equalsIgnoreCase(entry.getTransactionDebitCreditCode());
                    glpe = generalLedgerPendingEntryService.buildGeneralLedgerPendingEntry((GeneralLedgerPostingDocument) document, entry.getAccount(), entry.getFinancialObject(), entry.getSubAccountNumber(), entry.getFinancialSubObjectCode(), entry.getOrganizationReferenceId(), entry.getProjectCode(), entry.getReferenceFinancialDocumentNumber(), entry.getReferenceFinancialDocumentTypeCode(), entry.getReferenceFinancialSystemOriginationCode(), entry.getTransactionLedgerEntryDescription(), debit, entry.getTransactionLedgerEntryAmount(), sequenceHelper);
                    glpe.setFinancialDocumentTypeCode(documentType);
                    document.addPendingEntry(glpe);
                    sequenceHelper.increment();
                    // create cash entry
                    GeneralLedgerPendingEntry cashGlpe = new GeneralLedgerPendingEntry();
                    cashGlpe = generalLedgerPendingEntryService.buildGeneralLedgerPendingEntry((GeneralLedgerPostingDocument) document, entry.getAccount(), entry.getChart().getFinancialCashObject(), entry.getSubAccountNumber(), entry.getFinancialSubObjectCode(), entry.getOrganizationReferenceId(), entry.getProjectCode(), entry.getReferenceFinancialDocumentNumber(), entry.getReferenceFinancialDocumentTypeCode(), entry.getReferenceFinancialSystemOriginationCode(), entry.getTransactionLedgerEntryDescription(), !debit, entry.getTransactionLedgerEntryAmount(), sequenceHelper);
                    cashGlpe.setFinancialDocumentTypeCode(documentType);
                    document.addPendingEntry(cashGlpe);
                    sequenceHelper.increment();
                }
            }
        }
        
        if (document.getGeneralLedgerPendingEntries() != null && document.getGeneralLedgerPendingEntries().size() > 0) {
            for (GeneralLedgerPendingEntry glpe : document.getGeneralLedgerPendingEntries()) {
                glpe.setFinancialDocumentApprovedCode(KFSConstants.PENDING_ENTRY_APPROVED_STATUS_CODE.APPROVED);
            }
        }

    }

    /**
     * Retrieves the next available sequence number from the general ledger pending entry table for this document
     * 
     * @param documentNumber
     *            Document number to find next sequence number
     * @return Next available sequence number
     */
    protected int getNextAvailableSequence(String documentNumber) {
        LOG.debug("getNextAvailableSequence() started");
        Map fieldValues = new HashMap();
        fieldValues.put("financialSystemOriginationCode", PURAP_ORIGIN_CODE);
        fieldValues.put("documentNumber", documentNumber);
        List<GeneralLedgerPendingEntry> glpes = (List<GeneralLedgerPendingEntry>) (SpringContext.getBean(BusinessObjectService.class)).findMatching(GeneralLedgerPendingEntry.class, fieldValues);

        int count = 0;
        if (CollectionUtils.isNotEmpty(glpes)) {
            for (GeneralLedgerPendingEntry glpe : glpes) {
                if (glpe.getTransactionLedgerEntrySequenceNumber() > count) {
                    count = glpe.getTransactionLedgerEntrySequenceNumber();
                }
            }
        }
        return count + 1;
    }
    
    public Collection<Entry> findMatchingGLEntries(PaymentRequestDocument document, String offsetObjectCode) {
        Map<String, String> keyValues = new HashMap<String, String>();
        keyValues.put(KFSPropertyConstants.DOCUMENT_NUMBER, document.getDocumentNumber());
        keyValues.put(KFSPropertyConstants.FINANCIAL_OBJECT_CODE, offsetObjectCode);
        
        return businessObjectService.findMatching(Entry.class, keyValues);
    }

    public void setGeneralLedgerPendingEntryService(CuGeneralLedgerPendingEntryService generalLedgerPendingEntryService) {
        this.generalLedgerPendingEntryService = generalLedgerPendingEntryService;
    }

    public void setObjectCodeService(ObjectCodeService objectCodeService) {
        this.objectCodeService = objectCodeService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setBankService(BankService bankService) {
        this.bankService = bankService;
    }

}
