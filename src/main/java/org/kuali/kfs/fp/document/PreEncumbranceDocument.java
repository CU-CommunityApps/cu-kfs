/*
 * Copyright 2005-2006 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.fp.document;
import static org.kuali.kfs.sys.KFSConstants.BALANCE_TYPE_PRE_ENCUMBRANCE;
import static org.kuali.rice.kns.util.AssertionUtils.assertThat;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.businessobject.SufficientFundsItem;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocumentBase;
import org.kuali.kfs.sys.document.AmountTotaling;
import org.kuali.kfs.sys.document.service.DebitDeterminerService;
import org.kuali.kfs.sys.service.GeneralLedgerPendingEntryService;
import org.kuali.kfs.sys.service.HomeOriginationService;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kns.document.Copyable;
import org.kuali.rice.kns.exception.ValidationException;
import org.kuali.rice.kns.service.DateTimeService;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.ObjectUtils;

import edu.cornell.kfs.fp.businessobject.PreEncumbranceAccountingLineUtil;
import edu.cornell.kfs.fp.businessobject.PreEncumbranceSourceAccountingLine;
/**
 * The Pre-Encumbrance document provides the capability to record encumbrances independently of purchase orders, travel, or Physical
 * Plant work orders. These transactions are for the use of the account manager to earmark funds for which unofficial commitments
 * have already been made.
 */
public class PreEncumbranceDocument extends AccountingDocumentBase implements Copyable, AmountTotaling {
    protected static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PreEncumbranceDocument.class);
    protected java.sql.Date reversalDate;
    protected Integer nextPositionSourceLineNumber;
    protected Integer nextPositionTargetLineNumber;
    
    /**
     * Initializes the array lists and some basic info.
     */
    public PreEncumbranceDocument() {
        super();
    }
    /**
     * @see org.kuali.kfs.sys.document.AccountingDocumentBase#checkSufficientFunds()
     */
    @Override
    public List<SufficientFundsItem> checkSufficientFunds() {
        LOG.debug("checkSufficientFunds() started");
        // This document does not do sufficient funds checking
        return new ArrayList<SufficientFundsItem>();
    }
    /**
     * @return Timestamp
     */
    public java.sql.Date getReversalDate() {
        return reversalDate;
    }
    /**
     * @param reversalDate
     */
    public void setReversalDate(java.sql.Date reversalDate) {
        this.reversalDate = reversalDate;
    }
    /**
     * Overrides the base implementation to return "Encumbrance".
     * 
     * @see org.kuali.kfs.sys.document.AccountingDocument#getSourceAccountingLinesSectionTitle()
     */
    @Override
    public String getSourceAccountingLinesSectionTitle() {
        return KFSConstants.ENCUMBRANCE;
    }
    /**
     * Overrides the base implementation to return "Disencumbrance".
     * 
     * @see org.kuali.kfs.sys.document.AccountingDocument#getTargetAccountingLinesSectionTitle()
     */
    @Override
    public String getTargetAccountingLinesSectionTitle() {
        return KFSConstants.DISENCUMBRANCE;
    }
    /**
     * This method limits valid debits to only expense object type codes.  Additionally, an 
     * IllegalStateException will be thrown if the accounting line passed in is not an expense, 
     * is an error correction with a positive dollar amount or is not an error correction and 
     * has a negative amount. 
     * 
     * @param transactionalDocument The document the accounting line being checked is located in.
     * @param accountingLine The accounting line being analyzed.
     * @return True if the accounting line given is a debit accounting line, false otherwise.
     * 
     * @see IsDebitUtils#isDebitConsideringSection(FinancialDocumentRuleBase, FinancialDocument, AccountingLine)
     * @see org.kuali.rice.kns.rule.AccountingLineRule#isDebit(org.kuali.rice.kns.document.FinancialDocument,
     *      org.kuali.rice.kns.bo.AccountingLine)
     */
    public boolean isDebit(GeneralLedgerPendingEntrySourceDetail postable) {
        AccountingLine accountingLine = (AccountingLine)postable;
        // if not expense, or positive amount on an error-correction, or negative amount on a non-error-correction, throw exception
        DebitDeterminerService isDebitUtils = SpringContext.getBean(DebitDeterminerService.class);
        if (!isDebitUtils.isExpense(accountingLine) || (isDebitUtils.isErrorCorrection(this) == accountingLine.getAmount().isPositive())) {
            throw new IllegalStateException(isDebitUtils.getDebitCalculationIllegalStateExceptionMessage());
        }
        return !isDebitUtils.isDebitConsideringSection(this, accountingLine);
    }
    
    /**
     * This method contains PreEncumbrance document specific general ledger pending entry explicit entry 
     * attribute assignments.  These attributes include financial balance type code, reversal date and 
     * transaction encumbrance update code.
     * 
     * @param financialDocument The document which contains the explicit entry.
     * @param accountingLine The accounting line the explicit entry is generated from.
     * @param explicitEntry The explicit entry being updated.
     * 
     * @see org.kuali.module.financial.rules.FinancialDocumentRuleBase#customizeExplicitGeneralLedgerPendingEntry(org.kuali.rice.kns.document.FinancialDocument,
     *      org.kuali.rice.kns.bo.AccountingLine, org.kuali.module.gl.bo.GeneralLedgerPendingEntry)
     */
    @Override
    public void customizeExplicitGeneralLedgerPendingEntry(GeneralLedgerPendingEntrySourceDetail postable, GeneralLedgerPendingEntry explicitEntry) {
        explicitEntry.setFinancialBalanceTypeCode(BALANCE_TYPE_PRE_ENCUMBRANCE);
        AccountingLine accountingLine = (AccountingLine)postable;
        // set the reversal date to what was chosen by the user in the interface
        if (ObjectUtils.isNotNull(getReversalDate()) && ObjectUtils.isNull(explicitEntry.getFinancialDocumentReversalDate())) {
            explicitEntry.setFinancialDocumentReversalDate(getReversalDate());
        }
        explicitEntry.setTransactionEntryProcessedTs(null);
        if (accountingLine.isSourceAccountingLine()) {
            explicitEntry.setTransactionEncumbranceUpdateCode(KFSConstants.ENCUMB_UPDT_DOCUMENT_CD);
            explicitEntry.setReferenceFinancialSystemOriginationCode(SpringContext.getBean(HomeOriginationService.class).getHomeOrigination().getFinSystemHomeOriginationCode());
            explicitEntry.setReferenceFinancialDocumentNumber(accountingLine.getReferenceNumber());
            explicitEntry.setReferenceFinancialDocumentTypeCode(explicitEntry.getFinancialDocumentTypeCode()); // "PE"
        }
        else {
            assertThat(accountingLine.isTargetAccountingLine(), accountingLine);
            explicitEntry.setTransactionEncumbranceUpdateCode(KFSConstants.ENCUMB_UPDT_REFERENCE_DOCUMENT_CD);
            explicitEntry.setReferenceFinancialSystemOriginationCode(SpringContext.getBean(HomeOriginationService.class).getHomeOrigination().getFinSystemHomeOriginationCode());
            explicitEntry.setReferenceFinancialDocumentNumber(accountingLine.getReferenceNumber());
            explicitEntry.setReferenceFinancialDocumentTypeCode(explicitEntry.getFinancialDocumentTypeCode()); // "PE"
        }
    }
    /**
     * @see org.kuali.kfs.sys.document.AccountingDocumentBase#toCopy()
     */
    @Override
    public void toCopy() throws WorkflowException {
        super.toCopy();
        refreshReversalDate();
    }
    
    /**
     * If the reversal date on this document is in need of refreshing, refreshes the reveral date.  THIS METHOD MAY CHANGE DOCUMENT STATE!
     * @return true if the reversal date ended up getting refreshed, false otherwise
     */
    protected boolean refreshReversalDate() {
        boolean refreshed = false;
        if (getReversalDate() != null) {
            java.sql.Date today = SpringContext.getBean(DateTimeService.class).getCurrentSqlDateMidnight();
            if (getReversalDate().before(today)) {
                // set the reversal date on the document
                setReversalDate(today);
                refreshed = true;
            }
        }
        return refreshed;
    }
        public Integer getNextPositionSourceLineNumber() {
                return nextPositionSourceLineNumber;
        }
        public void setNextPositionSourceLineNumber(Integer nextPositionSourceLineNumber) {
                this.nextPositionSourceLineNumber = nextPositionSourceLineNumber;
        }
        public Integer getNextPositionTargetLineNumber() {
                return nextPositionTargetLineNumber;
        }
        public void setNextPositionTargetLineNumber(Integer nextPositionTargetLineNumber) {
                this.nextPositionTargetLineNumber = nextPositionTargetLineNumber;
        }
    
    @Override
    public boolean generateGeneralLedgerPendingEntries(GeneralLedgerPendingEntrySourceDetail glpeSourceDetail, GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        
        // handle the explicit entry
        // create a reference to the explicitEntry to be populated, so we can pass to the offset method later
        GeneralLedgerPendingEntry explicitEntry = new GeneralLedgerPendingEntry();
        processExplicitGeneralLedgerPendingEntry(sequenceHelper, glpeSourceDetail, explicitEntry);
        // increment the sequence counter
        sequenceHelper.increment();
        // handle the offset entry
        
        return true;
    }
    
    
        
    /**
     * This method processes all necessary information to build an explicit general ledger entry, and then adds that to the
     * document.
     * 
     * @param accountingDocument
     * @param sequenceHelper
     * @param accountingLine
     * @param explicitEntry
     * @return boolean True if the explicit entry generation was successful, false otherwise.
     */
    @Override
    protected void processExplicitGeneralLedgerPendingEntry(GeneralLedgerPendingEntrySequenceHelper sequenceHelper, GeneralLedgerPendingEntrySourceDetail glpeSourceDetail, GeneralLedgerPendingEntry explicitEntry) {
        if (glpeSourceDetail instanceof PreEncumbranceSourceAccountingLine) {
        	int rowId = ((AccountingLine) glpeSourceDetail).getSequenceNumber()-1;
        		PreEncumbranceSourceAccountingLine pesal = (PreEncumbranceSourceAccountingLine) glpeSourceDetail;
                if (ObjectUtils.isNotNull(pesal.getAutoDisEncumberType())) {
                	if (ObjectUtils.isNull(pesal.getStartDate()) || ObjectUtils.isNull(pesal.getPartialTransactionCount()) || ObjectUtils.isNull(pesal.getPartialAmount())) {
                		throw new ValidationException("Insufficient information for GLPE generation");
                	}
                	
                       Date generatedEndDate = PreEncumbranceAccountingLineUtil.generateEndDate(pesal.getStartDate(), Integer.parseInt(pesal.getPartialTransactionCount()), pesal.getAutoDisEncumberType());
                       pesal.setEndDate(generatedEndDate);
                        
                        TreeMap<Date, KualiDecimal> datesAndAmounts = PreEncumbranceAccountingLineUtil.generateDatesAndAmounts(pesal.getAutoDisEncumberType(), 
                                        pesal.getStartDate(), pesal.getEndDate(), Integer.parseInt(pesal.getPartialTransactionCount()), 
                                        pesal.getAmount(), pesal.getPartialAmount(), rowId);
                        Iterator<Date> it = datesAndAmounts.keySet().iterator();
                        boolean isErrorCorrection = false;
                        Date today = new Date(Calendar.getInstance().getTimeInMillis());
                        if (pesal.getAmount().isNegative()) { // we are doing error correction
                                LOG.info("Error correction!");
                                isErrorCorrection = true;
                        }
                        while (it.hasNext()) {
                                Date revDate = it.next();
                                if (isErrorCorrection && revDate.before(today)) {
                                        break;
                                }
                                KualiDecimal partialAmount = datesAndAmounts.get(revDate);
                                GeneralLedgerPendingEntry explicitPartialEntry = new GeneralLedgerPendingEntry();
                                SpringContext.getBean(GeneralLedgerPendingEntryService.class).populateExplicitGeneralLedgerPendingEntry(this, glpeSourceDetail, sequenceHelper, explicitPartialEntry);
                                explicitPartialEntry.setFinancialDocumentReversalDate(revDate);
                                explicitPartialEntry.setTransactionLedgerEntryAmount(isErrorCorrection?partialAmount.negated():partialAmount);
                                customizeExplicitGeneralLedgerPendingEntry(glpeSourceDetail, explicitPartialEntry);
                                addPendingEntry(explicitPartialEntry);
                                sequenceHelper.increment();
                        GeneralLedgerPendingEntry offsetEntry = new GeneralLedgerPendingEntry(explicitPartialEntry);
                        processOffsetGeneralLedgerPendingEntry(sequenceHelper, glpeSourceDetail, explicitPartialEntry, offsetEntry);
                        sequenceHelper.increment();
                        }
                        
                        // no need to do the following stuff, as we're generating a bunch of custom GL pending entries above
                        return;
                }
        }
        // populate the explicit entry
        SpringContext.getBean(GeneralLedgerPendingEntryService.class).populateExplicitGeneralLedgerPendingEntry(this, glpeSourceDetail, sequenceHelper, explicitEntry);
        // hook for children documents to implement document specific GLPE field mappings
        customizeExplicitGeneralLedgerPendingEntry(glpeSourceDetail, explicitEntry);
        addPendingEntry(explicitEntry);
        
        sequenceHelper.increment();
        // handle the offset entry
        GeneralLedgerPendingEntry offsetEntry = new GeneralLedgerPendingEntry(explicitEntry);
        boolean success = processOffsetGeneralLedgerPendingEntry(sequenceHelper, glpeSourceDetail, explicitEntry, offsetEntry);
    }
//    @Override
//    public void toErrorCorrection() throws WorkflowException {
//      super.toErrorCorrection();
//
//      if (ObjectUtils.isNotNull(this.sourceAccountingLines)) {
//              for (Iterator iter = this.getSourceAccountingLines().iterator(); iter.hasNext();) {
//                      PreEncumbranceSourceAccountingLine sourceLine = (PreEncumbranceSourceAccountingLine) iter.next();
////                            sourceLine.setAmount(sourceLine.getAmount().negated());
////                            sourceLine.setPartialAmount(sourceLine.getPartialAmount().negated());
//              }
//      }
//      
//    }
    
    /**
     * This method processes an accounting line's information to build an offset entry, and then adds that to the document.
     * 
     * @param accountingDocument
     * @param sequenceHelper
     * @param accountingLine
     * @param explicitEntry
     * @param offsetEntry
     * @return boolean True if the offset generation is successful.
     */
    @Override
    protected boolean processOffsetGeneralLedgerPendingEntry(GeneralLedgerPendingEntrySequenceHelper sequenceHelper, GeneralLedgerPendingEntrySourceDetail postable, GeneralLedgerPendingEntry explicitEntry, GeneralLedgerPendingEntry offsetEntry) {
        // populate the offset entry
        boolean success = SpringContext.getBean(GeneralLedgerPendingEntryService.class).populateOffsetGeneralLedgerPendingEntry(getPostingYear(), explicitEntry, sequenceHelper, offsetEntry);
        // hook for children documents to implement document specific field mappings for the GLPE
        success &= customizeOffsetGeneralLedgerPendingEntry(postable, explicitEntry, offsetEntry);
        addPendingEntry(offsetEntry);
        return success;
    }
}