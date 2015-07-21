package edu.cornell.kfs.fp.document;

import java.sql.Date;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.coreservice.framework.parameter.ParameterConstants.COMPONENT;
import org.kuali.rice.coreservice.framework.parameter.ParameterConstants.NAMESPACE;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.api.action.ActionTaken;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.krad.util.ObjectUtils;
import org.kuali.kfs.fp.businessobject.CapitalAssetInformation;
import org.kuali.kfs.fp.businessobject.ProcurementCardSourceAccountingLine;
import org.kuali.kfs.fp.businessobject.ProcurementCardTargetAccountingLine;
import org.kuali.kfs.fp.businessobject.ProcurementCardTransactionDetail;

import edu.cornell.kfs.fp.batch.ProcurementCardParameterConstants;
import edu.cornell.kfs.fp.businessobject.CuProcurementCardHolder;

@NAMESPACE(namespace = KFSConstants.CoreModuleNamespaces.FINANCIAL)
@COMPONENT(component = "ProcurementCard")
public class CuProcurementCardDocument extends ProcurementCardDocument {

    private static final long serialVersionUID = 1L;
    private static final String FINAL_ACCOUNTING_PERIOD = "13";
    private static final String HAS_RECONCILER_NODE = "HasReconciler";
    
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

        for (Iterator iter = transactionEntries.iterator(); iter.hasNext();) {
            ProcurementCardTransactionDetail transactionEntry = (ProcurementCardTransactionDetail) iter.next();
            transactionEntry.getSourceAccountingLines().add(line);
        }

        this.nextSourceLineNumber = new Integer(this.getNextSourceLineNumber().intValue() + 1);
    }

    @Override
    public void addTargetAccountingLine(TargetAccountingLine targetLine) {
        ProcurementCardTargetAccountingLine line = (ProcurementCardTargetAccountingLine) targetLine;

        line.setSequenceNumber(this.getNextTargetLineNumber());

        for (Iterator iter = transactionEntries.iterator(); iter.hasNext();) {
            ProcurementCardTransactionDetail transactionEntry = (ProcurementCardTransactionDetail) iter.next();
            transactionEntry.getTargetAccountingLines().add(line);
        }

        this.nextTargetLineNumber = new Integer(this.getNextTargetLineNumber().intValue() + 1);
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
    private Date getProcurementCardTransactionPostingDetailDate() {
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
     * @see org.kuali.rice.kns.document.DocumentBase#getDocumentTitle()
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
    
    @Override
    public void prepareForSave()
    {
      List<CapitalAssetInformation> caiList = getCapitalAssetInformation();

      if (caiList != null)
      {
        for (CapitalAssetInformation cai : caiList)
        {
          cai.setDocumentNumber(this.getDocumentNumber());
        }
      }
      super.prepareForSave();
    }

    /**
     * Answers true when invoice recurrence details are provided by the user
     *
     * @see org.kuali.kfs.sys.document.FinancialSystemTransactionalDocumentBase#answerSplitNodeQuestion(java.lang.String)
     */
    @Override
    public boolean answerSplitNodeQuestion(String nodeName) throws UnsupportedOperationException
    {
      if (HAS_RECONCILER_NODE.equalsIgnoreCase(nodeName))
      {
        return hasReconciler();
      }
          return super.answerSplitNodeQuestion(nodeName);
    }

    /**
     * Determines whether this document has a Recurrence filled out enough to create an INVR doc.
     *
     * @return
     */
    private boolean hasReconciler()
    {
      CuProcurementCardHolder cardHolder = (CuProcurementCardHolder) getProcurementCardHolder();
      return (ObjectUtils.isNotNull(cardHolder)
        && ObjectUtils.isNotNull(cardHolder.getProcurementCardHolderDetail())
        && ObjectUtils.isNotNull(cardHolder.getProcurementCardHolderDetail().getCardGroupId()));
    }

    /**
     * Copied from Rice 1 WorkflowDocumentImpl because functionality was removed from rice.
     *
     * @return
     * @throws WorkflowException
     */
    public Set<Person> getAllPriorApprovers() throws WorkflowException
    {
      PersonService personService = KimApiServiceLocator.getPersonService();
      List<ActionTaken> actionsTaken = getDocumentHeader().getWorkflowDocument().getActionsTaken();
      Set<String> principalIds = new HashSet<String>();
      Set<Person> persons = new HashSet<Person>();

      for (ActionTaken actionTaken : actionsTaken)
      {
        if (KewApiConstants.ACTION_TAKEN_APPROVED_CD.equals(actionTaken.getActionTaken().getCode()))
        {
          String principalId = actionTaken.getPrincipalId();
          if ( ! principalIds.contains(principalId))
          {
            principalIds.add(principalId);
            persons.add(personService.getPerson(principalId));
          }
        }
      }
      return persons;
    }

}
