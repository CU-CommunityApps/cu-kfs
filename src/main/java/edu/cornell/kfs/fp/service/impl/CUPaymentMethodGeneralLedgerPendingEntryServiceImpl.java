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
import org.apache.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.OffsetDefinition;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.coa.service.OffsetDefinitionService;
import org.kuali.kfs.gl.businessobject.Entry;

import org.kuali.kfs.module.cab.CabPropertyConstants;
import org.kuali.kfs.module.cab.businessobject.BatchParameters;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.service.PurapGeneralLedgerService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
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
import org.kuali.kfs.sys.service.GeneralLedgerPendingEntryService;
import org.kuali.kfs.sys.service.NonTransactional;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.ObjectUtils;
import org.springframework.cache.annotation.Cacheable;

import edu.cornell.kfs.fp.businessobject.PaymentMethod;
import edu.cornell.kfs.fp.businessobject.PaymentMethodChart;
import edu.cornell.kfs.fp.service.CUPaymentMethodGeneralLedgerPendingEntryService;
import edu.cornell.kfs.module.purap.document.CuPaymentRequestDocument;

@NonTransactional
public class CUPaymentMethodGeneralLedgerPendingEntryServiceImpl implements CUPaymentMethodGeneralLedgerPendingEntryService {
    private static Logger LOG = Logger.getLogger(CUPaymentMethodGeneralLedgerPendingEntryServiceImpl.class);
    protected static final String DEFAULT_PAYMENT_METHOD_IF_MISSING = "A"; // check/ACH

    // not sure why these are not injected ?
    private GeneralLedgerPendingEntryService generalLedgerPendingEntryService;
    private ObjectCodeService objectCodeService;
    private ParameterService parameterService;
    private BusinessObjectService businessObjectService;
    private BankService bankService;
    protected PurapGeneralLedgerService purapGeneralLedgerService; 
    

    @Cacheable(value=SystemOptions.CACHE_NAME, key="'{isPaymentMethodProcessedUsingPdp}'+#p0")
    public boolean isPaymentMethodProcessedUsingPdp(String paymentMethodCode) {
        if ( StringUtils.isBlank(paymentMethodCode) ) {
            paymentMethodCode = DEFAULT_PAYMENT_METHOD_IF_MISSING;
        }
        PaymentMethod pm = getBusinessObjectService().findBySinglePrimaryKey(PaymentMethod.class, paymentMethodCode);
        if ( pm != null ) {
            return pm.isProcessedUsingPdp();
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
        PaymentMethod pm = getBusinessObjectService().findBySinglePrimaryKey(PaymentMethod.class, paymentMethodCode);
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
     * @param financialDocument submitted financial document
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
        PaymentMethod pm = getBusinessObjectService().findBySinglePrimaryKey(PaymentMethod.class, paymentMethodCode);
        // no payment method? abort.
        if ( pm == null ) {
            return false;
        }
        
        if ( pm.isAssessedFees() ) {
            if ( !feesWaived ) {
                generateFeeAssessmentEntries(pm, document, templatePendingEntry, sequenceHelper, reverseCharge);
            }                        
        }
        
        if ( !PaymentMethod.PM_CODE_INTERNAL_BILLING.equalsIgnoreCase(paymentMethodCode) && pm.isOffsetUsingClearingAccount() ) {
            generateClearingAccountOffsetEntries(pm, document, sequenceHelper, actualTotalsByChart);
        }
        
        if ( !pm.isProcessedUsingPdp() && StringUtils.isNotBlank( bankCode ) ) {
            if(PaymentMethod.PM_CODE_WIRE.equalsIgnoreCase(paymentMethodCode) || PaymentMethod.PM_CODE_FOREIGN_DRAFT.equalsIgnoreCase(paymentMethodCode)){
                //do not create bank offsets unless DM approval
            }
            else{
                generateDocumentBankOffsetEntries(document,bankCode,bankCodePropertyName,templatePendingEntry.getFinancialDocumentTypeCode(), sequenceHelper, bankOffsetAmount );
            }
        }
        
        return true;
    }
    
    /**
     * Generates the GL entries to charge the department for the foreign draft and credit the Wire Charge
     * Fee Account as specified by system parameters.
     * 
     * @param document Document into which to add the generated GL Entries.
     * 
     */
    protected boolean generateFeeAssessmentEntries(PaymentMethod pm, AccountingDocument document, GeneralLedgerPendingEntry templatePendingEntry, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, boolean reverseEntries) {
        LOG.debug("generateForeignDraftChargeEntries started");
        
        PaymentMethodChart pmc = pm.getPaymentMethodChartInfo(templatePendingEntry.getChartOfAccountsCode(), new java.sql.Date( document.getDocumentHeader().getWorkflowDocument().getDateCreated().getMillis() ) );
        if ( pmc == null ) {
            LOG.warn( "No Applicable PaymentMethodChart found for chart: " + templatePendingEntry.getChartOfAccountsCode() + " and date: " + document.getDocumentHeader().getWorkflowDocument().getDateCreated() );
            return false;
        }
        // Get all the parameters which control these entries
        String feeIncomeChartCode = pmc.getFeeIncomeChartOfAccountsCode();
        String feeIncomeAccountNumber = pmc.getFeeIncomeAccountNumber();
        String feeExpenseObjectCode = pmc.getFeeExpenseFinancialObjectCode();
        String feeIncomeObjectCode = pmc.getFeeIncomeFinancialObjectCode();
        KualiDecimal feeAmount = pmc.getFeeAmount();

        // skip creation if the fee has been set to zero
        if ( !KualiDecimal.ZERO.equals(feeAmount) ) {
            // grab the explicit entry for the first accounting line and adjust for the foreign draft fee
            GeneralLedgerPendingEntry chargeEntry = new GeneralLedgerPendingEntry(document.getGeneralLedgerPendingEntry(0));        
            chargeEntry.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
            
            // change the object code (expense to the department)
            chargeEntry.setFinancialObjectCode(feeExpenseObjectCode);
            chargeEntry.setFinancialSubObjectCode(GENERAL_LEDGER_PENDING_ENTRY_CODE.getBlankFinancialSubObjectCode());
            chargeEntry.setTransactionLedgerEntryDescription( StringUtils.left( "Automatic debit for " + pm.getPaymentMethodName() + " fee", 40 ));
            chargeEntry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);
    
            // retrieve object type
            ObjectCode objectCode = getObjectCodeService().getByPrimaryIdForCurrentYear(chargeEntry.getChartOfAccountsCode(), chargeEntry.getFinancialObjectCode());
            if ( objectCode == null ) {
                LOG.fatal("Specified offset object code: " + chargeEntry.getChartOfAccountsCode() + "-" + chargeEntry.getFinancialObjectCode() + " does not exist - failed to generate foreign draft fee entries", new RuntimeException() );
                return false;
            }       
            chargeEntry.setFinancialObjectTypeCode(objectCode.getFinancialObjectTypeCode());
            
            // Set the amount from the parameter
            chargeEntry.setTransactionLedgerEntryAmount(feeAmount);
            chargeEntry.setTransactionDebitCreditCode(reverseEntries?GL_CREDIT_CODE:GL_DEBIT_CODE);
    
            document.addPendingEntry(chargeEntry);
            sequenceHelper.increment();
    
            // handle the offset entry
            GeneralLedgerPendingEntry offsetEntry = new GeneralLedgerPendingEntry(chargeEntry);
            getGeneralLedgerPendingEntryService().populateOffsetGeneralLedgerPendingEntry(document.getPostingYear(), chargeEntry, sequenceHelper, offsetEntry);
    
            document.addPendingEntry(offsetEntry);
            sequenceHelper.increment();
            
            // Now, create the income entry in the AP Foreign draft fee account
            
            GeneralLedgerPendingEntry feeIncomeEntry = new GeneralLedgerPendingEntry(document.getGeneralLedgerPendingEntry(0));
            feeIncomeEntry.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
    
            feeIncomeEntry.setChartOfAccountsCode(feeIncomeChartCode);
            feeIncomeEntry.setAccountNumber(feeIncomeAccountNumber);
            feeIncomeEntry.setFinancialObjectCode(feeIncomeObjectCode);
            feeIncomeEntry.setFinancialSubObjectCode(GENERAL_LEDGER_PENDING_ENTRY_CODE.getBlankFinancialSubObjectCode());
            feeIncomeEntry.setSubAccountNumber(GENERAL_LEDGER_PENDING_ENTRY_CODE.getBlankSubAccountNumber());
            feeIncomeEntry.setProjectCode(GENERAL_LEDGER_PENDING_ENTRY_CODE.getBlankProjectCode());
    
            // retrieve object type
            objectCode = getObjectCodeService().getByPrimaryIdForCurrentYear(feeIncomeChartCode, feeIncomeObjectCode);
            if ( objectCode == null ) {
                LOG.fatal("Specified income object code: " + feeIncomeChartCode + "-" + feeIncomeObjectCode + " does not exist - failed to generate foreign draft income entries", new RuntimeException() );
                return false;
            }
            feeIncomeEntry.setFinancialObjectTypeCode(objectCode.getFinancialObjectTypeCode());       
            feeIncomeEntry.setTransactionLedgerEntryAmount(feeAmount);
            feeIncomeEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
            feeIncomeEntry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);
    
            document.addPendingEntry(feeIncomeEntry);
            sequenceHelper.increment();
            
            // create the offset entry
            offsetEntry = new GeneralLedgerPendingEntry(feeIncomeEntry);
            getGeneralLedgerPendingEntryService().populateOffsetGeneralLedgerPendingEntry(document.getPostingYear(), feeIncomeEntry, sequenceHelper, offsetEntry);
    
            document.addPendingEntry(offsetEntry);
            sequenceHelper.increment();
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
     * When the "A" payment method is used for AP Credit Cards - generate the needed entries in the clearing account.
     * 
     * @param document Document into which to add the generated GL Entries.
     * @param sequenceHelper helper class to keep track of GLPE sequence
     * 
     */
    public boolean generateClearingAccountOffsetEntries(PaymentMethod pm, AccountingDocument document, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, Map<String,KualiDecimal> actualTotalsByChart) {
        if ( actualTotalsByChart == null ) {
            actualTotalsByChart = getNonOffsetActualTotalsByChart(document);
        }

        for ( String chart : actualTotalsByChart.keySet() ) {
            KualiDecimal offsetAmount = actualTotalsByChart.get(chart);
            if ( !KualiDecimal.ZERO.equals(offsetAmount) ) {
                PaymentMethodChart pmc = pm.getPaymentMethodChartInfo(chart, new java.sql.Date( document.getDocumentHeader().getWorkflowDocument().getDateCreated().getMillis() ) );
                if ( pmc == null ) {
                    LOG.warn( "No Applicable PaymentMethodChart found for chart: " + chart + " and date: " + document.getDocumentHeader().getWorkflowDocument().getDateCreated() );
                    // skip this line - still attempt for other charts
                    continue;
                }
                String clearingChartCode = pmc.getClearingChartOfAccountsCode();
                String clearingAccountNumber = pmc.getClearingAccountNumber();
                String clearingObjectCode = pmc.getClearingFinancialObjectCode(); // liability object code
                
                GeneralLedgerPendingEntry apOffsetEntry = new GeneralLedgerPendingEntry(document.getGeneralLedgerPendingEntry(0));
                apOffsetEntry.setTransactionLedgerEntrySequenceNumber(new Integer(sequenceHelper.getSequenceCounter()));
    
                apOffsetEntry.setChartOfAccountsCode(clearingChartCode);
                apOffsetEntry.setAccountNumber(clearingAccountNumber);
                apOffsetEntry.setFinancialObjectCode(clearingObjectCode);
                // if internal billing
                if (StringUtils.equals(PaymentMethod.PM_CODE_INTERNAL_BILLING, pm.getPaymentMethodCode())) {
                    apOffsetEntry.setFinancialSubObjectCode(pmc.getClearingFinancialSubObjectCode());
                    apOffsetEntry.setSubAccountNumber(pmc.getClearingSubAccountNumber());
                } else {
                apOffsetEntry.setFinancialSubObjectCode(GENERAL_LEDGER_PENDING_ENTRY_CODE.getBlankFinancialSubObjectCode());
                apOffsetEntry.setSubAccountNumber(GENERAL_LEDGER_PENDING_ENTRY_CODE.getBlankSubAccountNumber());
                }
                apOffsetEntry.setProjectCode(GENERAL_LEDGER_PENDING_ENTRY_CODE.getBlankProjectCode());
    
                // retrieve object type
                ObjectCode objectCode = getObjectCodeService().getByPrimaryIdForCurrentYear(clearingChartCode, clearingObjectCode);
                if ( objectCode == null ) {
                    LOG.fatal("Specified offset object code: " + clearingChartCode + "-" + clearingObjectCode + " does not exist - failed to generate CC offset entries", new RuntimeException() );
                    return false;
                }
                apOffsetEntry.setFinancialObjectTypeCode(objectCode.getFinancialObjectTypeCode());       
                apOffsetEntry.setTransactionLedgerEntryAmount(offsetAmount.abs());
                apOffsetEntry.setTransactionDebitCreditCode(offsetAmount.isNegative()?KFSConstants.GL_DEBIT_CODE:KFSConstants.GL_CREDIT_CODE);
                apOffsetEntry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);
    
                document.addPendingEntry(apOffsetEntry);
                sequenceHelper.increment();
                
                // handle the offset entry
                GeneralLedgerPendingEntry offsetEntry = new GeneralLedgerPendingEntry(apOffsetEntry);
                getGeneralLedgerPendingEntryService().populateOffsetGeneralLedgerPendingEntry(document.getPostingYear(), apOffsetEntry, sequenceHelper, offsetEntry);
    
                document.addPendingEntry(offsetEntry);
                sequenceHelper.increment();
            }
        }
        
        return true;
    }

    /**
     * If bank specification is enabled generates bank offsetting entries for the document amount
     * 
     */
    public boolean generateDocumentBankOffsetEntries(AccountingDocument document, String bankCode, String bankCodePropertyName, String documentTypeCode, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, KualiDecimal bankOffsetAmount ) {
        boolean success = true;

        if (!getBankService().isBankSpecificationEnabled()) {
            return success;
        }
        Bank bank = getBankService().getByPrimaryId(bankCode);

        if ( bankOffsetAmount == null ) {
            bankOffsetAmount = getGeneralLedgerPendingEntryService().getOffsetToCashAmount(document).negated();
        }
        if ( !KualiDecimal.ZERO.equals(bankOffsetAmount) ) {
            GeneralLedgerPendingEntry bankOffsetEntry = new GeneralLedgerPendingEntry();
            success &= getGeneralLedgerPendingEntryService()
                    .populateBankOffsetGeneralLedgerPendingEntry(bank, bankOffsetAmount, document, 
                            document.getPostingYear(), sequenceHelper, bankOffsetEntry, bankCodePropertyName);
    
            if (success) {
                AccountingDocumentRuleHelperService accountingDocumentRuleUtil = SpringContext.getBean(AccountingDocumentRuleHelperService.class);
                bankOffsetEntry.setTransactionLedgerEntryDescription(accountingDocumentRuleUtil.formatProperty(KFSKeyConstants.Bank.DESCRIPTION_GLPE_BANK_OFFSET));
                bankOffsetEntry.setFinancialDocumentTypeCode(documentTypeCode);
                document.addPendingEntry(bankOffsetEntry);
                sequenceHelper.increment();
    
                GeneralLedgerPendingEntry offsetEntry = new GeneralLedgerPendingEntry(bankOffsetEntry);
                success &= getGeneralLedgerPendingEntryService().populateOffsetGeneralLedgerPendingEntry(document.getPostingYear(), bankOffsetEntry, sequenceHelper, offsetEntry);
                bankOffsetEntry.setFinancialDocumentTypeCode(documentTypeCode);

                document.addPendingEntry(offsetEntry);
                sequenceHelper.increment();
            }
        }

        return success;
    }
    
    protected GeneralLedgerPendingEntryService getGeneralLedgerPendingEntryService() {
        if ( generalLedgerPendingEntryService == null ) {
            generalLedgerPendingEntryService = SpringContext.getBean(GeneralLedgerPendingEntryService.class);
        }
        return generalLedgerPendingEntryService;
    }
    
    protected ObjectCodeService getObjectCodeService() {
        if ( objectCodeService == null ) {
            objectCodeService = SpringContext.getBean(ObjectCodeService.class);
        }
        return objectCodeService;
    }
    
    protected ParameterService getParameterService() {
        if ( parameterService == null ) {
            parameterService = SpringContext.getBean(ParameterService.class);
        }
        return parameterService;
    }

    protected BusinessObjectService getBusinessObjectService() {
        if ( businessObjectService == null ) {
            businessObjectService = SpringContext.getBean(BusinessObjectService.class);
        }
        return businessObjectService;
    }
    
    protected BankService getBankService() {
        if ( bankService == null ) {
            bankService = SpringContext.getBean(BankService.class);
        }
        return bankService;
    }

    /**
     * Creates final entries for PRNC doc: Reverse all usage 2900 object codes Replaces with 1000 offset object code Generate
     * Bank Offsets for total amounts
     * 
     * @see edu.cornell.kfs.fp.service.CUPaymentMethodGeneralLedgerPendingEntryService#generateFinalEntriesForPRNC(org.kuali.kfs.module.purap.document.PaymentRequestDocument)
     */
    public void generateFinalEntriesForPRNC(PaymentRequestDocument document) {

        GeneralLedgerPendingEntrySequenceHelper sequenceHelper = new GeneralLedgerPendingEntrySequenceHelper(getNextAvailableSequence(document.getDocumentNumber()));

        // generate bank offset
        if (PaymentMethod.PM_CODE_FOREIGN_DRAFT.equalsIgnoreCase(((CuPaymentRequestDocument)document).getPaymentMethodCode()) || PaymentMethod.PM_CODE_WIRE.equalsIgnoreCase(((CuPaymentRequestDocument)document).getPaymentMethodCode())) {
            generateDocumentBankOffsetEntries((AccountingDocument) document, document.getBankCode(), KRADConstants.DOCUMENT_PROPERTY_NAME + "." + "bankCode", ((CuPaymentRequestDocument)document).DOCUMENT_TYPE_NON_CHECK, sequenceHelper, document.getTotalDollarAmount().negated());
        }
        
        // KFPTS-3046
        // for internal billing generate entries to clearing accounts
        if(PaymentMethod.PM_CODE_INTERNAL_BILLING.equalsIgnoreCase(((CuPaymentRequestDocument)document).getPaymentMethodCode())){
            PaymentMethod pm = getBusinessObjectService().findBySinglePrimaryKey(PaymentMethod.class, ((CuPaymentRequestDocument)document).getPaymentMethodCode());
            generateClearingAccountOffsetEntries(pm, document, sequenceHelper, null);
        }

        // check for pending entries and replace object code with chart cash object code
        List<GeneralLedgerPendingEntry> glpes = document.getGeneralLedgerPendingEntries();

        for (GeneralLedgerPendingEntry glpe : glpes) {
            OffsetDefinition offsetDefinition = SpringContext.getBean(OffsetDefinitionService.class).getByPrimaryId(glpe.getUniversityFiscalYear(), glpe.getChartOfAccountsCode(), glpe.getFinancialDocumentTypeCode(), glpe.getFinancialBalanceTypeCode());
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
                    OffsetDefinition offsetDefinition = SpringContext.getBean(OffsetDefinitionService.class).getByPrimaryId(accountingLine.getPostingYear(), accountingLine.getChartOfAccountsCode(), ((CuPaymentRequestDocument)document).DOCUMENT_TYPE_NON_CHECK, KFSConstants.BALANCE_TYPE_ACTUAL);
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
                    glpe = getGeneralLedgerPendingEntryService().buildGeneralLedgerPendingEntry((GeneralLedgerPostingDocument) document, entry.getAccount(), entry.getFinancialObject(), entry.getSubAccountNumber(), entry.getFinancialSubObjectCode(), entry.getOrganizationReferenceId(), entry.getProjectCode(), entry.getReferenceFinancialDocumentNumber(), entry.getReferenceFinancialDocumentTypeCode(), entry.getReferenceFinancialSystemOriginationCode(), entry.getTransactionLedgerEntryDescription(), debit, entry.getTransactionLedgerEntryAmount(), sequenceHelper);
                    glpe.setFinancialDocumentTypeCode(((CuPaymentRequestDocument)document).DOCUMENT_TYPE_NON_CHECK);
                    document.addPendingEntry(glpe);
                    sequenceHelper.increment();
                    // create cash entry
                    GeneralLedgerPendingEntry cashGlpe = new GeneralLedgerPendingEntry();
                    cashGlpe = getGeneralLedgerPendingEntryService().buildGeneralLedgerPendingEntry((GeneralLedgerPostingDocument) document, entry.getAccount(), entry.getChart().getFinancialCashObject(), entry.getSubAccountNumber(), entry.getFinancialSubObjectCode(), entry.getOrganizationReferenceId(), entry.getProjectCode(), entry.getReferenceFinancialDocumentNumber(), entry.getReferenceFinancialDocumentTypeCode(), entry.getReferenceFinancialSystemOriginationCode(), entry.getTransactionLedgerEntryDescription(), !debit, entry.getTransactionLedgerEntryAmount(), sequenceHelper);
                    cashGlpe.setFinancialDocumentTypeCode(((CuPaymentRequestDocument)document).DOCUMENT_TYPE_NON_CHECK);
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

}
