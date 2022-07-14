package edu.cornell.kfs.fp.document;

import java.sql.Date;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.kuali.kfs.fp.batch.ProcurementCardLoadStep;
import org.kuali.kfs.fp.document.ProcurementCardDocument;
import org.kuali.kfs.module.purap.PurapRuleConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.TargetAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants.COMPONENT;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants.NAMESPACE;
import org.kuali.kfs.fp.businessobject.ProcurementCardSourceAccountingLine;
import org.kuali.kfs.fp.businessobject.ProcurementCardTargetAccountingLine;
import org.kuali.kfs.fp.businessobject.ProcurementCardTransactionDetail;

import edu.cornell.kfs.fp.batch.ProcurementCardParameterConstants;


@NAMESPACE(namespace = KFSConstants.CoreModuleNamespaces.FINANCIAL)
@COMPONENT(component = "ProcurementCard")
public class CuProcurementCardDocument extends ProcurementCardDocument {
	private static final Logger LOG = LogManager.getLogger(CuProcurementCardDocument.class);

    private static final long serialVersionUID = 1L;
    private static final String FINAL_ACCOUNTING_PERIOD = "13";
    
    /**
     * @return the previous fiscal year used with all GLPE
     */
    public static final Integer getPreviousFiscalYear() {
        int i = SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear().intValue() - 1;
        return new Integer(i);
    }
    
    @Override
    public void addSourceAccountingLine(SourceAccountingLine sourceLine) {
        ProcurementCardSourceAccountingLine line = (ProcurementCardSourceAccountingLine) sourceLine;

        line.setSequenceNumber(this.getNextSourceLineNumber());

        for (Object transactionEntryObj : transactionEntries) {
            ProcurementCardTransactionDetail transactionEntry = (ProcurementCardTransactionDetail) transactionEntryObj;
            transactionEntry.getSourceAccountingLines().add(line);
        }

        this.nextSourceLineNumber = this.getNextSourceLineNumber() + 1;
    }

    @Override
    public void addTargetAccountingLine(TargetAccountingLine targetLine) {
        ProcurementCardTargetAccountingLine line = (ProcurementCardTargetAccountingLine) targetLine;

        line.setSequenceNumber(this.getNextTargetLineNumber());

        for (Object transactionEntryObj : transactionEntries) {
            ProcurementCardTransactionDetail transactionEntry = (ProcurementCardTransactionDetail) transactionEntryObj;
            transactionEntry.getTargetAccountingLines().add(line);
        }

        this.nextTargetLineNumber = this.getNextTargetLineNumber() + 1;
    }
    
    @Override
    public void customizeExplicitGeneralLedgerPendingEntry(GeneralLedgerPendingEntrySourceDetail postable, GeneralLedgerPendingEntry explicitEntry) {
        Date temp = getProcurementCardTransactionPostingDetailDate();
        
        if (temp != null && allowBackpost(temp)) {
            Integer prevFiscYr = getPreviousFiscalYear();
            
            explicitEntry.setUniversityFiscalPeriodCode(FINAL_ACCOUNTING_PERIOD);
            explicitEntry.setUniversityFiscalYear(prevFiscYr);
            
            List<SourceAccountingLine> srcLines = getSourceAccountingLines();
            
            for (SourceAccountingLine src : srcLines) {
                src.setPostingYear(prevFiscYr);
            }

            List<TargetAccountingLine> trgLines = getTargetAccountingLines();
            
            for (TargetAccountingLine trg : trgLines) {
                trg.setPostingYear(prevFiscYr);
            }
        }
    }

    /**
     * Get Transaction Date - CSU assumes there will be only one
     * 
     * @param docNum
     * 
     * @return Date
     */
    public Date getProcurementCardTransactionPostingDetailDate() {
        Date date = null;
        
        for (Object temp : getTransactionEntries()) {
            date = ((ProcurementCardTransactionDetail) temp).getTransactionPostingDate();
        }
        
        return date;
    }
    
    /**
     * Allow Backpost
     * 
     * @param tranDate
     * @return
     */
    public boolean allowBackpost(Date tranDate) {
        ParameterService      parameterService      = SpringContext.getBean(ParameterService.class);
        UniversityDateService universityDateService = SpringContext.getBean(UniversityDateService.class);
       
        int allowBackpost = Integer.parseInt(parameterService.getParameterValueAsString(
                ProcurementCardLoadStep.class, PurapRuleConstants.ALLOW_BACKPOST_DAYS));

        Calendar today = Calendar.getInstance();
        Integer currentFY = universityDateService.getCurrentUniversityDate().getUniversityFiscalYear();
        java.util.Date priorClosingDateTemp = universityDateService.getLastDateOfFiscalYear(currentFY - 1);
        
        Calendar priorClosingDate = Calendar.getInstance();
        priorClosingDate.setTime(priorClosingDateTemp);

        // adding 1 to set the date to midnight the day after backpost is allowed so that preqs allow backpost on the last day
        Calendar allowBackpostDate = Calendar.getInstance();
        allowBackpostDate.setTime(priorClosingDate.getTime());
        allowBackpostDate.add(Calendar.DATE, allowBackpost + 1);

        Calendar tranCal = Calendar.getInstance();
        tranCal.setTime(tranDate);

        // if today is after the closing date but before/equal to the allowed backpost date and the transaction date is for the
        // prior year, set the year to prior year
        if ((today.compareTo(priorClosingDate) > 0) && (today.compareTo(allowBackpostDate) <= 0) && (tranCal.compareTo(priorClosingDate) <= 0)) {
            LOG.debug("allowBackpost() within range to allow backpost; posting entry to period 12 of previous FY");
            return true;
        }

        LOG.debug("allowBackpost() not within range to allow backpost; posting entry to current FY");
        return false;
    }
    
    public String getAccountNumberForSearching() {
        ProcurementCardTransactionDetail transaction = (ProcurementCardTransactionDetail) transactionEntries.get(0);
        TargetAccountingLine tal = (TargetAccountingLine) transaction.getTargetAccountingLines().get(0);
        String acctNbr = tal.getAccountNumber();
        return acctNbr;
    }

    
    /**
     * @see org.kuali.kfs.kns.document.DocumentBase#getDocumentTitle()
     */
    @Override
    public String getDocumentTitle() {
        if (SpringContext.getBean(ParameterService.class).getParameterValueAsBoolean(
                ProcurementCardDocument.class, ProcurementCardParameterConstants.OVERRIDE_PCDO_DOC_TITLE)) {
            return getCustomDocumentTitle();
        }
        
        return super.getDocumentTitle();
    }

    /**
     * Returns a custom document title based on the workflow document title. Depending on what route level the document is currently
     * in, the PCDO amount may be added to the documents title.
     * 
     * @return - Customized document title text dependent upon route level.
     */
    protected String getCustomDocumentTitle() {
       
        // set the workflow document title
        String pcdoAmount = this.getTotalDollarAmount().toString();

        return new StringBuffer(super.getDocumentTitle()).append(" - Amount: ").append(pcdoAmount).toString();

    }
}
