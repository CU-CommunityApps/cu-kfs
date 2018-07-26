package edu.cornell.kfs.fp.document;

import java.sql.Date;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.GeneralLedgerPendingEntryService;
import org.kuali.kfs.sys.service.HomeOriginationService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.fp.businessobject.PreEncumbranceAccountingLineUtil;
import edu.cornell.kfs.fp.businessobject.PreEncumbranceSourceAccountingLine;

public class PreEncumbranceDocument extends org.kuali.kfs.fp.document.PreEncumbranceDocument {
	private static final Logger LOG = LogManager.getLogger(PreEncumbranceDocument.class);
    private static final long serialVersionUID = 1L;
    protected Integer nextPositionSourceLineNumber;
    protected Integer nextPositionTargetLineNumber;
    
    

    @Override
    public void customizeExplicitGeneralLedgerPendingEntry(GeneralLedgerPendingEntrySourceDetail postable, GeneralLedgerPendingEntry explicitEntry) {
        explicitEntry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_PRE_ENCUMBRANCE);
        AccountingLine accountingLine = (AccountingLine) postable;

        // set the reversal date to what was chosen by the user in the interface
        if (ObjectUtils.isNotNull(getReversalDate()) && ObjectUtils.isNull(explicitEntry.getFinancialDocumentReversalDate())) {
            explicitEntry.setFinancialDocumentReversalDate(getReversalDate());
        }
        explicitEntry.setTransactionEntryProcessedTs(null);
        if (accountingLine.isSourceAccountingLine()) {
            explicitEntry.setTransactionEncumbranceUpdateCode(KFSConstants.ENCUMB_UPDT_DOCUMENT_CD);
            explicitEntry.setReferenceFinancialSystemOriginationCode(SpringContext.getBean(HomeOriginationService.class)
                    .getHomeOrigination().getFinSystemHomeOriginationCode());
            explicitEntry.setReferenceFinancialDocumentNumber(accountingLine.getReferenceNumber());
            explicitEntry.setReferenceFinancialDocumentTypeCode(explicitEntry.getFinancialDocumentTypeCode());
            // "PE"
        } else if (accountingLine.isTargetAccountingLine()) {
            explicitEntry.setTransactionEncumbranceUpdateCode(KFSConstants.ENCUMB_UPDT_REFERENCE_DOCUMENT_CD);
            explicitEntry.setReferenceFinancialSystemOriginationCode(SpringContext.getBean(HomeOriginationService.class)
                .getHomeOrigination().getFinSystemHomeOriginationCode());
            explicitEntry.setReferenceFinancialDocumentNumber(accountingLine.getReferenceNumber());
            // "PE"
            explicitEntry.setReferenceFinancialDocumentTypeCode(explicitEntry.getFinancialDocumentTypeCode()); 
            
        }
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
    public boolean generateGeneralLedgerPendingEntries(GeneralLedgerPendingEntrySourceDetail glpeSourceDetail,
         GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {

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
    protected void processExplicitGeneralLedgerPendingEntry(GeneralLedgerPendingEntrySequenceHelper sequenceHelper,
        GeneralLedgerPendingEntrySourceDetail glpeSourceDetail, GeneralLedgerPendingEntry explicitEntry) {
        if (glpeSourceDetail instanceof PreEncumbranceSourceAccountingLine) {
            int rowId = ((AccountingLine) glpeSourceDetail).getSequenceNumber() - 1;
            PreEncumbranceSourceAccountingLine pesal = (PreEncumbranceSourceAccountingLine) glpeSourceDetail;
            if (ObjectUtils.isNotNull(pesal.getAutoDisEncumberType())) {
                if (ObjectUtils.isNull(pesal.getStartDate()) || ObjectUtils.isNull(pesal.getPartialTransactionCount()) 
                    || ObjectUtils.isNull(pesal.getPartialAmount())) {
                    throw new ValidationException("Insufficient information for GLPE generation");
                }
                Date generatedEndDate = PreEncumbranceAccountingLineUtil.generateEndDate(pesal.getStartDate(),
                    Integer.parseInt(pesal.getPartialTransactionCount()), pesal.getAutoDisEncumberType());
                pesal.setEndDate(generatedEndDate);
                
                TreeMap<Date, KualiDecimal> datesAndAmounts = PreEncumbranceAccountingLineUtil.generateDatesAndAmounts(pesal.getAutoDisEncumberType(), 
                                pesal.getStartDate(), pesal.getEndDate(), Integer.parseInt(pesal.getPartialTransactionCount()), 
                                pesal.getAmount(), pesal.getPartialAmount(), rowId);
                Iterator<Date> it = datesAndAmounts.keySet().iterator();
                boolean isErrorCorrection = false;
                Date today = new Date(Calendar.getInstance().getTimeInMillis());
                if (pesal.getAmount().isNegative()) { 
                    // we are doing error correction
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
                    SpringContext.getBean(GeneralLedgerPendingEntryService.class)
                        .populateExplicitGeneralLedgerPendingEntry(this, glpeSourceDetail, sequenceHelper, explicitPartialEntry);
                    explicitPartialEntry.setFinancialDocumentReversalDate(revDate);
                    explicitPartialEntry.setTransactionLedgerEntryAmount(isErrorCorrection ? partialAmount.negated() : partialAmount);
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
        SpringContext.getBean(GeneralLedgerPendingEntryService.class)
            .populateExplicitGeneralLedgerPendingEntry(this, glpeSourceDetail, sequenceHelper, explicitEntry);
        // hook for children documents to implement document specific GLPE field mappings
        customizeExplicitGeneralLedgerPendingEntry(glpeSourceDetail, explicitEntry);
        addPendingEntry(explicitEntry);

        sequenceHelper.increment();
        // handle the offset entry
        GeneralLedgerPendingEntry offsetEntry = new GeneralLedgerPendingEntry(explicitEntry);
        boolean success = processOffsetGeneralLedgerPendingEntry(sequenceHelper, glpeSourceDetail, explicitEntry, offsetEntry);
    }
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
    protected boolean processOffsetGeneralLedgerPendingEntry(GeneralLedgerPendingEntrySequenceHelper sequenceHelper,
        GeneralLedgerPendingEntrySourceDetail postable, GeneralLedgerPendingEntry explicitEntry, GeneralLedgerPendingEntry offsetEntry) {
        // populate the offset entry
        boolean success = SpringContext.getBean(GeneralLedgerPendingEntryService.class)
             .populateOffsetGeneralLedgerPendingEntry(getPostingYear(), explicitEntry, sequenceHelper, offsetEntry);
        // hook for children documents to implement document specific field mappings for the GLPE
        success &= customizeOffsetGeneralLedgerPendingEntry(postable, explicitEntry, offsetEntry);
        addPendingEntry(offsetEntry);
        return success;
    }
    
  
    
}
