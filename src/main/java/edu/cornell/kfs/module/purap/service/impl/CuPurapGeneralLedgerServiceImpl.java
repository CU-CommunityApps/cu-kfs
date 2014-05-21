package edu.cornell.kfs.module.purap.service.impl;

import static org.kuali.kfs.module.purap.PurapConstants.PURAP_ORIGIN_CODE;
import static org.kuali.kfs.sys.KFSConstants.GL_CREDIT_CODE;
import static org.kuali.kfs.sys.KFSConstants.GL_DEBIT_CODE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapConstants.PurapDocTypeCodes;
import org.kuali.kfs.module.purap.businessobject.PurApItemUseTax;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.service.PurapAccountRevisionService;
import org.kuali.kfs.module.purap.service.PurapAccountingService;
import org.kuali.kfs.module.purap.service.impl.PurapGeneralLedgerServiceImpl;
import org.kuali.kfs.module.purap.util.SummaryAccount;
import org.kuali.kfs.module.purap.util.UseTaxContainer;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.fp.service.CUPaymentMethodGeneralLedgerPendingEntryService;
import edu.cornell.kfs.module.purap.document.CuPaymentRequestDocument;

public class CuPurapGeneralLedgerServiceImpl extends PurapGeneralLedgerServiceImpl {
	
	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuPurapGeneralLedgerServiceImpl.class);

    // KFSPTS-1891
    protected CUPaymentMethodGeneralLedgerPendingEntryService paymentMethodGeneralLedgerPendingEntryService;
    // END MOD
    
    protected int getNextAvailableSequence(String documentNumber) {
        LOG.debug("getNextAvailableSequence() started");
        Map fieldValues = new HashMap();
        fieldValues.put("financialSystemOriginationCode", PURAP_ORIGIN_CODE);
        fieldValues.put("documentNumber", documentNumber);
        List<GeneralLedgerPendingEntry> glpes = (List <GeneralLedgerPendingEntry>)SpringContext.getBean(org.kuali.rice.krad.service.BusinessObjectService.class).findMatching(GeneralLedgerPendingEntry.class, fieldValues);
//      KFSPTS-2632 : Bankoffset will not be posted by nightly batch job because its status is 'N'.
        // so, we need to find the highest transactionsequence.  Otherwise it may cause OLE
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
    
    protected boolean generateEntriesPaymentRequest(PaymentRequestDocument preq, List encumbrances, List summaryAccounts, String processType) {
        LOG.debug("generateEntriesPaymentRequest() started");
        boolean success = true;
        preq.setGeneralLedgerPendingEntries(new ArrayList());

        /*
         * Can't let generalLedgerPendingEntryService just create all the entries because we need the sequenceHelper to carry over
         * from the encumbrances to the actuals and also because we need to tell the PaymentRequestDocumentRule customize entry
         * method how to customize differently based on if creating an encumbrance or actual.
         */
        GeneralLedgerPendingEntrySequenceHelper sequenceHelper = new GeneralLedgerPendingEntrySequenceHelper(getNextAvailableSequence(preq.getDocumentNumber()));

        // when cancelling a PREQ, do not book encumbrances if PO is CLOSED
        if (encumbrances != null && !(CANCEL_PAYMENT_REQUEST.equals(processType) && PurapConstants.PurchaseOrderStatuses.APPDOC_CLOSED.equals(preq.getPurchaseOrderDocument().getApplicationDocumentStatus()))) {
            LOG.debug("generateEntriesPaymentRequest() generate encumbrance entries");
            if (CREATE_PAYMENT_REQUEST.equals(processType)) {
                // on create, use CREDIT code for encumbrances
                preq.setDebitCreditCodeForGLEntries(GL_CREDIT_CODE);
            }
            else if (CANCEL_PAYMENT_REQUEST.equals(processType)) {
                // on cancel, use DEBIT code
                preq.setDebitCreditCodeForGLEntries(GL_DEBIT_CODE);
            }
            else if (MODIFY_PAYMENT_REQUEST.equals(processType)) {
                // no encumbrances for modify
            }

            preq.setGenerateEncumbranceEntries(true);
            for (Iterator iter = encumbrances.iterator(); iter.hasNext();) {
                AccountingLine accountingLine = (AccountingLine) iter.next();
                preq.generateGeneralLedgerPendingEntries(accountingLine, sequenceHelper);
                sequenceHelper.increment(); // increment for the next line
            }
        }

        if (ObjectUtils.isNotNull(summaryAccounts) && !summaryAccounts.isEmpty()) {
            LOG.debug("generateEntriesPaymentRequest() now book the actuals");
            preq.setGenerateEncumbranceEntries(false);

            if (CREATE_PAYMENT_REQUEST.equals(processType) || MODIFY_PAYMENT_REQUEST.equals(processType)) {
                // on create and modify, use DEBIT code
                preq.setDebitCreditCodeForGLEntries(GL_DEBIT_CODE);
            }
            else if (CANCEL_PAYMENT_REQUEST.equals(processType)) {
                // on cancel, use CREDIT code
                preq.setDebitCreditCodeForGLEntries(GL_CREDIT_CODE);
            }

            for (Iterator iter = summaryAccounts.iterator(); iter.hasNext();) {
                SummaryAccount summaryAccount = (SummaryAccount) iter.next();
                preq.generateGeneralLedgerPendingEntries(summaryAccount.getAccount(), sequenceHelper);
                sequenceHelper.increment(); // increment for the next line
            }

            // generate offset accounts for use tax if it exists (useTaxContainers will be empty if not a use tax document)
            List<UseTaxContainer> useTaxContainers = SpringContext.getBean(PurapAccountingService.class).generateUseTaxAccount(preq);
            for (UseTaxContainer useTaxContainer : useTaxContainers) {
                PurApItemUseTax offset = useTaxContainer.getUseTax();
                List<SourceAccountingLine> accounts = useTaxContainer.getAccounts();
                for (SourceAccountingLine sourceAccountingLine : accounts) {
                    preq.generateGeneralLedgerPendingEntries(sourceAccountingLine, sequenceHelper, useTaxContainer.getUseTax());
                    sequenceHelper.increment(); // increment for the next line
                }

            }

            // Manually save preq summary accounts
            if (MODIFY_PAYMENT_REQUEST.equals(processType)) {
                //for modify, regenerate the summary from the doc
                List<SummaryAccount> summaryAccountsForModify = SpringContext.getBean(PurapAccountingService.class).generateSummaryAccountsWithNoZeroTotalsNoUseTax(preq);
                saveAccountsPayableSummaryAccounts(summaryAccountsForModify, preq.getPurapDocumentIdentifier(), PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT);
            }
            else {
                //for create and cancel, use the summary accounts
                saveAccountsPayableSummaryAccounts(summaryAccounts, preq.getPurapDocumentIdentifier(), PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT);
            }

            // manually save cm account change tables (CAMS needs this)
            if (CREATE_PAYMENT_REQUEST.equals(processType) || MODIFY_PAYMENT_REQUEST.equals(processType)) {
                SpringContext.getBean(PurapAccountRevisionService.class).savePaymentRequestAccountRevisions(preq.getItems(), preq.getPostingYearFromPendingGLEntries(), preq.getPostingPeriodCodeFromPendingGLEntries());
            }
            else if (CANCEL_PAYMENT_REQUEST.equals(processType)) {
                SpringContext.getBean(PurapAccountRevisionService.class).cancelPaymentRequestAccountRevisions(preq.getItems(), preq.getPostingYearFromPendingGLEntries(), preq.getPostingPeriodCodeFromPendingGLEntries());
            }
        
        // KFSPTS-1891
        // generate any document level GL entries (offsets or fee charges)
        // we would only want to do this when booking the actuals (not the encumbrances)
        if (preq.getGeneralLedgerPendingEntries() == null || preq.getGeneralLedgerPendingEntries().size() < 2) {
            LOG.warn("No gl entries for accounting lines.");
        } else {
            // Upon a modify, we need to skip re-assessing any fees
            // in fact, we need to skip making any of these entries since there could be a combination
            // of debits and credit entries in the entry list - this will cause problems if the first is a
            // credit since it uses that to determine the sign of all the other transactions
            
            // upon create, build the entries normally
            if ( CREATE_PAYMENT_REQUEST.equals(processType) ) {
                getPaymentMethodGeneralLedgerPendingEntryService().generatePaymentMethodSpecificDocumentGeneralLedgerPendingEntries(
                        preq,((CuPaymentRequestDocument)preq).getPaymentMethodCode(),preq.getBankCode(), KRADConstants.DOCUMENT_PROPERTY_NAME + "." + "bankCode", preq.getGeneralLedgerPendingEntry(0), false, false, sequenceHelper);
            } else if ( MODIFY_PAYMENT_REQUEST.equals(processType) ) {
                // upon modify, we need to calculate the deltas here and pass them in so the appropriate adjustments are created
                KualiDecimal bankOffsetAmount = KualiDecimal.ZERO;
                Map<String,KualiDecimal> changesByChart = new HashMap<String, KualiDecimal>();
                if (ObjectUtils.isNotNull(summaryAccounts) && !summaryAccounts.isEmpty()) {
                    for ( SummaryAccount a : (List<SummaryAccount>)summaryAccounts ) {
                        bankOffsetAmount = bankOffsetAmount.add(a.getAccount().getAmount());
                        if ( changesByChart.get( a.getAccount().getChartOfAccountsCode() ) == null ) {
                            changesByChart.put( a.getAccount().getChartOfAccountsCode(), a.getAccount().getAmount() );
                        } else {
                            changesByChart.put( a.getAccount().getChartOfAccountsCode(), changesByChart.get( a.getAccount().getChartOfAccountsCode() ).add( a.getAccount().getAmount() ) );
                        }
                    }
                }
                
                getPaymentMethodGeneralLedgerPendingEntryService().generatePaymentMethodSpecificDocumentGeneralLedgerPendingEntries(
                        preq,((CuPaymentRequestDocument)preq).getPaymentMethodCode(),preq.getBankCode(), KRADConstants.DOCUMENT_PROPERTY_NAME + "." + "bankCode", preq.getGeneralLedgerPendingEntry(0), true, false, sequenceHelper, bankOffsetAmount, changesByChart );
            }
        }
        preq.generateDocumentGeneralLedgerPendingEntries(sequenceHelper);
        // END MOD
    }


        // Manually save GL entries for Payment Request and encumbrances
        saveGLEntries(preq.getGeneralLedgerPendingEntries());

        return success;
    }

		public CUPaymentMethodGeneralLedgerPendingEntryService getPaymentMethodGeneralLedgerPendingEntryService() {
			return paymentMethodGeneralLedgerPendingEntryService;
		}

		public void setPaymentMethodGeneralLedgerPendingEntryService(
				CUPaymentMethodGeneralLedgerPendingEntryService paymentMethodGeneralLedgerPendingEntryService) {
			this.paymentMethodGeneralLedgerPendingEntryService = paymentMethodGeneralLedgerPendingEntryService;
		}
}
