package edu.cornell.kfs.module.purap.document;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kew.framework.postprocessor.DocumentRouteStatusChange;
import org.kuali.kfs.kns.document.authorization.DocumentAuthorizer;
import org.kuali.kfs.kns.service.DocumentHelperService;
import org.kuali.kfs.krad.rules.rule.event.KualiDocumentEvent;
import org.kuali.kfs.krad.service.SequenceAccessorService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PaymentRequestStatuses;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderView;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.util.ExpiredOrClosedAccountEntry;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.service.GeneralLedgerPendingEntryService;

import edu.cornell.kfs.fp.service.CUPaymentMethodGeneralLedgerPendingEntryService;
import edu.cornell.kfs.module.purap.CUPurapWorkflowConstants;
import edu.cornell.kfs.module.purap.businessobject.CuPaymentRequestItemExtension;
import edu.cornell.kfs.pdp.service.CuCheckStubService;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CuPaymentRequestDocument extends PaymentRequestDocument {
	private static final Logger LOG = LogManager.getLogger();
    // KFSPTS-1891
    public static String DOCUMENT_TYPE_NON_CHECK = "PRNC";
    public static String DOCUMENT_TYPE_INTERNAL_BILLING = "PRID";
    
    private static CUPaymentMethodGeneralLedgerPendingEntryService paymentMethodGeneralLedgerPendingEntryService;
    private static CuCheckStubService cuCheckStubService;
    
    public CuPaymentRequestDocument() {
        super();
        setPaymentMethodCode(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_CHECK);
    }

    @Override
    public void prepareForSave(final KualiDocumentEvent event) {
    	super.prepareForSave(event);
        for (final PaymentRequestItem item : (List<PaymentRequestItem>) getItems()) {
            if (item.getItemIdentifier() == null) {
                final Integer generatedItemId = SpringContext.getBean(SequenceAccessorService.class).getNextAvailableSequenceNumber("PMT_RQST_ITM_ID").intValue();
                item.setItemIdentifier(generatedItemId);
                if (item.getExtension() == null) {
                    item.setExtension(new CuPaymentRequestItemExtension());
                }
                ((CuPaymentRequestItemExtension)item.getExtension()).setItemIdentifier(generatedItemId);
            }
        }
        
        // First, only do this if the document is in initiated status - after that, we don't want to 
        // accidentally reset the bank code
        // KFSPTS-1891
        if (getDocumentHeader().getWorkflowDocument().isInitiated() || getDocumentHeader().getWorkflowDocument().isSaved()  ) {
            // need to check whether the user has the permission to edit the bank code
            // if so, don't synchronize since we can't tell whether the value coming in
            // was entered by the user or not.
            final DocumentAuthorizer docAuth = SpringContext.getBean(DocumentHelperService.class).getDocumentAuthorizer(this);
            if ( !docAuth.isAuthorizedByTemplate(this, 
                    KFSConstants.CoreModuleNamespaces.KFS, 
                    KFSConstants.PermissionTemplate.EDIT_BANK_CODE.name, 
                    GlobalVariables.getUserSession().getPrincipalId()  ) ) {
                synchronizeBankCodeWithPaymentMethod();        
            } else {
                // ensure that the name is updated properly
                refreshReferenceObject( "bank" );
            }
        }    
    }
    
    /**
     * @see org.kuali.kfs.module.purap.document.PaymentRequestDocument#populatePaymentRequestFromPurchaseOrder(org.kuali.kfs.module.purap.document.PurchaseOrderDocument, java.util.HashMap)
     */
    @Override
    public void populatePaymentRequestFromPurchaseOrder(
            final PurchaseOrderDocument po, final HashMap<String,
            ExpiredOrClosedAccountEntry> expiredOrClosedAccountList) {
        super.populatePaymentRequestFromPurchaseOrder(po, expiredOrClosedAccountList);

        if (ObjectUtils.isNotNull(po.getVendorDetail()) 
                && StringUtils.isNotBlank((po.getVendorDetail()).getDefaultPaymentMethodCode())) {
             setPaymentMethodCode((po.getVendorDetail()).getDefaultPaymentMethodCode());
         }
        
        // Copy PO explanation to PREQ.
        if (StringUtils.isNotBlank(po.getDocumentHeader().getExplanation())) {
            this.getDocumentHeader().setExplanation(po.getDocumentHeader().getExplanation());
        }
    }
    
    /**
     * @see org.kuali.kfs.krad.document.DocumentBase#doRouteStatusChange()
     */
    @Override
    public void doRouteStatusChange(final DocumentRouteStatusChange statusChangeEvent) {
        LOG.debug("doRouteStatusChange() started");
        
        if (getDocumentHeader().getWorkflowDocument().isProcessed()) {
        	// KFSPTS-1891
        	if (CollectionUtils.isEmpty(generalLedgerPendingEntries)) {
        		refreshReferenceObject("generalLedgerPendingEntries");
        	}
        	
        	if (getCuCheckStubService().doesCheckStubNeedTruncatingForIso20022(this)) {
        		getCuCheckStubService().addNoteToDocumentRegardingCheckStubIso20022MaxLength(this);
        	}
        }

        super.doRouteStatusChange(statusChangeEvent);
    }
    
    @Override
    public void populateDocumentForRouting() {
    	super.populateDocumentForRouting();
    	// KFSPTS-1891
    	if (this.getDocumentHeader().getWorkflowDocument().isProcessed() 
    	        && !PaymentRequestStatuses.APPDOC_AUTO_APPROVED.equals(getApplicationDocumentStatus())
    	        && !paymentHasBeenExtractedOrPaid()) {

    		//generate bank offsets for payment method wire or foreign draft, reverse 2900 to 1000
    		final String paymentMethodCode = getPaymentMethodCode();
    		if(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_DRAFT.equalsIgnoreCase(paymentMethodCode) || KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_WIRE.equalsIgnoreCase(paymentMethodCode) || CUKFSConstants.CuPaymentSourceConstants.PAYMENT_METHOD_INTERNAL_BILLING.equalsIgnoreCase(paymentMethodCode)){
    			getPaymentMethodGeneralLedgerPendingEntryService().generateFinalEntriesForPRNC(this);
    		}

    		// KFSPTS-2581 : GLPE need to be saved separately because not in ojb config
    		// All GLPE approve cd has been set to 'A'
    		saveGeneralLedgerPendingEntries();
    	}
    }
    
    /* Cornell Customization : KFSPTS-34015
     * We needed local fixes due to changes introduced by the KualiCo 2023-04-19 Upgrade patch changes.
     * 
     * In that patch, method org.kuali.kfs.module.purap.document.PaymentRequestDocument.doRouteStatusChange 
     * introduced the following service call
     *       getPdpExtractService().extractPaymentRequestDocument(this);
     * which saves the paymentRequestDocument to record the extracted and paid timestamps resulting in 
     * cash offset GL entries being duplicated.
     * This GL duplicate entry issue was because method populateDocumentForRouting is invoked after a 
     * docuemnt is saved. This check was added to the method so that populateDocumentForRouting prevented
     * from executing after the wire extraction process was run.
     */
    protected boolean paymentHasBeenExtractedOrPaid() {
        if (ObjectUtils.isNotNull(getExtractedTimestamp())
                    || ObjectUtils.isNotNull(getPaymentPaidTimestamp())) {
            return true;
        }
        return false;
    }
    
    protected void saveGeneralLedgerPendingEntries() {
    	// All the approve cd is set to 'A' by glpepostingdocument
        for (final GeneralLedgerPendingEntry glpe : getGeneralLedgerPendingEntries()) {
        	
            SpringContext.getBean(GeneralLedgerPendingEntryService.class).save(glpe);
        }
    }
    
    protected void synchronizeBankCodeWithPaymentMethod() {
        final Bank bank = getPaymentMethodGeneralLedgerPendingEntryService().getBankForPaymentMethod( getPaymentMethodCode() );
        if ( bank != null ) {
            setBankCode(bank.getBankCode());
            setBank(bank);
        } else {
            // no bank code, no bank needed
            setBankCode(null);
            setBank(null);
        }
    }

    protected CUPaymentMethodGeneralLedgerPendingEntryService getPaymentMethodGeneralLedgerPendingEntryService() {
        if ( paymentMethodGeneralLedgerPendingEntryService == null ) {
            paymentMethodGeneralLedgerPendingEntryService = SpringContext.getBean(CUPaymentMethodGeneralLedgerPendingEntryService.class);
        }
        return paymentMethodGeneralLedgerPendingEntryService;
    }
    


    public boolean generateDocumentGeneralLedgerPendingEntries(final GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        if (getGeneralLedgerPendingEntries() == null || getGeneralLedgerPendingEntries().size() < 2) {
            LOG.warn("No gl entries for accounting lines.");
            return true;
        }
//        LOG.debug("generateDocumentGeneralLedgerPendingEntries()");
        getPaymentMethodGeneralLedgerPendingEntryService().generatePaymentMethodSpecificDocumentGeneralLedgerPendingEntries(
                (AccountingDocument)this,getPaymentMethodCode(),getBankCode(), KRADConstants.DOCUMENT_PROPERTY_NAME + "." + "bankCode", getGeneralLedgerPendingEntry(0), false, false, sequenceHelper);
        
        return true;
    }
    
    @Override
    public void customizeExplicitGeneralLedgerPendingEntry(
            final GeneralLedgerPendingEntrySourceDetail postable, final GeneralLedgerPendingEntry explicitEntry) {
    	super.customizeExplicitGeneralLedgerPendingEntry(postable, explicitEntry);
        // KFSPTS-1891
        // if the document is not processed using PDP, then the cash entries need to be created instead of liability
        // so, switch the document type so the offset generation uses a cash offset object code
        if ( !getPaymentMethodGeneralLedgerPendingEntryService().isPaymentMethodProcessedUsingPdp(getPaymentMethodCode())) {
        	
        	if (CUKFSConstants.CuPaymentSourceConstants.PAYMENT_METHOD_INTERNAL_BILLING.equalsIgnoreCase(getPaymentMethodCode())){
        		 explicitEntry.setFinancialDocumentTypeCode(DOCUMENT_TYPE_INTERNAL_BILLING);
            }
        	else{
        		explicitEntry.setFinancialDocumentTypeCode(DOCUMENT_TYPE_NON_CHECK);
        	}
        }
    }
    
    @Override
    public List<String> getWorkflowEngineDocumentIdsToLock() {
        final Stream<String> otherDocIdsToLock = Stream.of(super.getWorkflowEngineDocumentIdsToLock())
                .flatMap(idList -> (idList != null) ? idList.stream() : Stream.empty());
        
        final Stream<String> poDocIdsToLock = getRelatedViews().getRelatedPurchaseOrderViews().stream()
                .filter(PurchaseOrderView::isPurchaseOrderCurrentIndicator)
                .map(PurchaseOrderView::getDocumentNumber);
        
        return Stream.concat(otherDocIdsToLock, poDocIdsToLock)
                .collect(Collectors.toUnmodifiableList());
    }
    
    @Override
    public boolean answerSplitNodeQuestion(final String nodeName) throws UnsupportedOperationException {
        if (nodeName.equals(CUPurapWorkflowConstants.TREASURY_MANAGER)) {
            /*
             * CU Customization KFSPTS-34074
             * This fixes canceled and disapproved PREQ documents that were done before 2/9/2025,
             * which is when the 2023-04-19 version of Kuali Financials was installed into cu-kfs.  
             */
            return false;
        }
        
        return super.answerSplitNodeQuestion(nodeName);
    }
    
    protected static CuCheckStubService getCuCheckStubService() {
        if (cuCheckStubService == null) {
            cuCheckStubService = SpringContext.getBean(CuCheckStubService.class);
        }
        return cuCheckStubService;
    }
}
