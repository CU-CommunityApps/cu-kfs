package edu.cornell.kfs.module.purap.document;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kew.framework.postprocessor.ActionTakenEvent;
import org.kuali.kfs.kew.framework.postprocessor.DocumentRouteStatusChange;
import org.kuali.kfs.kns.document.authorization.DocumentAuthorizer;
import org.kuali.kfs.kns.service.DocumentHelperService;
import org.kuali.kfs.krad.rules.rule.event.KualiDocumentEvent;
import org.kuali.kfs.krad.service.SequenceAccessorService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PaymentRequestStatuses;
import org.kuali.kfs.module.purap.PurapWorkflowConstants;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderView;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.service.PurapGeneralLedgerService;
import org.kuali.kfs.module.purap.util.ExpiredOrClosedAccountEntry;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.service.GeneralLedgerPendingEntryService;

import edu.cornell.kfs.fp.businessobject.PaymentMethod;
import edu.cornell.kfs.fp.service.CUPaymentMethodGeneralLedgerPendingEntryService;
import edu.cornell.kfs.module.purap.CUPurapWorkflowConstants;
import edu.cornell.kfs.module.purap.businessobject.CuPaymentRequestItemExtension;
import edu.cornell.kfs.module.purap.businessobject.PaymentRequestWireTransfer;
import edu.cornell.kfs.vnd.businessobject.VendorDetailExtension;

public class CuPaymentRequestDocument extends PaymentRequestDocument {
	private static final Logger LOG = LogManager.getLogger();
    // KFSPTS-1891
    public static String DOCUMENT_TYPE_NON_CHECK = "PRNC";
    public static String DOCUMENT_TYPE_INTERNAL_BILLING = "PRID";
    protected PaymentRequestWireTransfer preqWireTransfer;
    
    // default this value to "C" to preserve baseline behavior
    protected String paymentMethodCode = "P"; //ACH check
    private static CUPaymentMethodGeneralLedgerPendingEntryService paymentMethodGeneralLedgerPendingEntryService;
    private PaymentMethod paymentMethod;
    
    public CuPaymentRequestDocument() {
		super();
		preqWireTransfer = new PaymentRequestWireTransfer();
	}

    public void prepareForSave(KualiDocumentEvent event) {
    	
        // KFSPTS-1891.  purchasingPreDisbursementExtractJob has NPE issue.  need this null check
       // if (preqWireTransfer != null && !StringUtils.equals(preqWireTransfer.getDocumentNumber(),getDocumentNumber())) {
//        LOG.info("preqWireTransfer " + preqWireTransfer != null);
        try {
//        if (preqWireTransfer != null) {
//            if (!StringUtils.equals(preqWireTransfer.getDocumentNumber(),getDocumentNumber())) {
        	preqWireTransfer.setDocumentNumber(getDocumentNumber());
//            }
//        }
        } catch (Exception e) {
          LOG.info("preqWireTransfer is null" );
          preqWireTransfer = new PaymentRequestWireTransfer();  
      	  preqWireTransfer.setDocumentNumber(getDocumentNumber());
     	
        }
        
    	super.prepareForSave(event);
        for (PaymentRequestItem item : (List<PaymentRequestItem>) getItems()) {
            if (item.getItemIdentifier() == null) {
                Integer generatedItemId = SpringContext.getBean(SequenceAccessorService.class).getNextAvailableSequenceNumber("PMT_RQST_ITM_ID").intValue();
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
        if ( getDocumentHeader().getWorkflowDocument().isInitiated() || getDocumentHeader().getWorkflowDocument().isSaved()  ) {
            // need to check whether the user has the permission to edit the bank code
            // if so, don't synchronize since we can't tell whether the value coming in
            // was entered by the user or not.
            DocumentAuthorizer docAuth = SpringContext.getBean(DocumentHelperService.class).getDocumentAuthorizer(this);
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
    
    @Override
    public boolean answerSplitNodeQuestion(String nodeName) throws UnsupportedOperationException {
        if (nodeName.equals(PurapWorkflowConstants.REQUIRES_IMAGE_ATTACHMENT)) {
            return requiresAccountsPayableReviewRouting();
        }
        if (nodeName.equals(PurapWorkflowConstants.PURCHASE_WAS_RECEIVED)) {
            return shouldWaitForReceiving();
        }
        if (nodeName.equals(PurapWorkflowConstants.VENDOR_IS_EMPLOYEE_OR_NONRESIDENT) ||
                "VendorIsEmployeeOrNonResidentAlien".equals(nodeName)) {
            return isVendorEmployeeOrNonresident();
        }
        // KFSPTS-1891
        if (nodeName.equals(CUPurapWorkflowConstants.TREASURY_MANAGER)) {
            return isWireOrForeignDraft();
        }
        if (nodeName.equals(PurapWorkflowConstants.IS_DOCUMENT_AUTO_APPROVED)) {
            return isAutoApprovedIndicator();
        }
        throw new UnsupportedOperationException("Cannot answer split question for this node you call \"" + nodeName + "\"");
    }
    
    // KFSPTS-1891
    private boolean isWireOrForeignDraft() {
        return StringUtils.equals(PaymentMethod.PM_CODE_WIRE, this.getPaymentMethodCode()) || StringUtils.equals(PaymentMethod.PM_CODE_FOREIGN_DRAFT, this.getPaymentMethodCode());
    }
    
    /**
     * @see org.kuali.kfs.module.purap.document.PaymentRequestDocument#populatePaymentRequestFromPurchaseOrder(org.kuali.kfs.module.purap.document.PurchaseOrderDocument, java.util.HashMap)
     */
    @Override
    public void populatePaymentRequestFromPurchaseOrder(PurchaseOrderDocument po, HashMap<String, ExpiredOrClosedAccountEntry> expiredOrClosedAccountList) {
    	super.populatePaymentRequestFromPurchaseOrder(po, expiredOrClosedAccountList);
    	
    	 // KFSPTS-1891
        if ( ObjectUtils.isNotNull(po.getVendorDetail())
                 && ObjectUtils.isNotNull(po.getVendorDetail().getExtension()) ) {
             if ( po.getVendorDetail().getExtension() instanceof VendorDetailExtension
                     && StringUtils.isNotBlank( ((VendorDetailExtension)po.getVendorDetail().getExtension()).getDefaultB2BPaymentMethodCode() ) ) {
                 setPaymentMethodCode(
                         ((VendorDetailExtension)po.getVendorDetail().getExtension()).getDefaultB2BPaymentMethodCode() );
             }
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
    public void doRouteStatusChange(DocumentRouteStatusChange statusChangeEvent) {
        LOG.debug("doRouteStatusChange() started");
        
        if (this.getDocumentHeader().getWorkflowDocument().isProcessed()) {
        	// KFSPTS-1891
        	if (CollectionUtils.isEmpty(generalLedgerPendingEntries)) {
        		this.refreshReferenceObject("generalLedgerPendingEntries");
        	}
        }

        super.doRouteStatusChange(statusChangeEvent);
    }
    
    @Override
    public void populateDocumentForRouting() {
    	super.populateDocumentForRouting();
    	// KFSPTS-1891
    	if (this.getFinancialSystemDocumentHeader().getWorkflowDocument().isProcessed() && !PaymentRequestStatuses.APPDOC_AUTO_APPROVED.equals(getApplicationDocumentStatus())) {

    		//generate bank offsets for payment method wire or foreign draft, reverse 2900 to 1000
    		String paymentMethodCode = getPaymentMethodCode();
    		if(PaymentMethod.PM_CODE_FOREIGN_DRAFT.equalsIgnoreCase(paymentMethodCode) || PaymentMethod.PM_CODE_WIRE.equalsIgnoreCase(paymentMethodCode) || PaymentMethod.PM_CODE_INTERNAL_BILLING.equalsIgnoreCase(paymentMethodCode)){
    			getPaymentMethodGeneralLedgerPendingEntryService().generateFinalEntriesForPRNC(this);
    		}

    		// KFSPTS-2581 : GLPE need to be saved separately because not in ojb config
    		// All GLPE approve cd has been set to 'A'
    		saveGeneralLedgerPendingEntries();
    	}
    }
    
    protected void saveGeneralLedgerPendingEntries() {
    	// All the approve cd is set to 'A' by glpepostingdocument
        for (GeneralLedgerPendingEntry glpe : getGeneralLedgerPendingEntries()) {
        	
            SpringContext.getBean(GeneralLedgerPendingEntryService.class).save(glpe);
        }
    }
    
    protected void synchronizeBankCodeWithPaymentMethod() {
        Bank bank = getPaymentMethodGeneralLedgerPendingEntryService().getBankForPaymentMethod( getPaymentMethodCode() );
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

    public boolean generateDocumentGeneralLedgerPendingEntries(GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
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
    public void customizeExplicitGeneralLedgerPendingEntry( GeneralLedgerPendingEntrySourceDetail postable, GeneralLedgerPendingEntry explicitEntry) {
    	super.customizeExplicitGeneralLedgerPendingEntry(postable, explicitEntry);
        // KFSPTS-1891
        // if the document is not processed using PDP, then the cash entries need to be created instead of liability
        // so, switch the document type so the offset generation uses a cash offset object code
        if ( !getPaymentMethodGeneralLedgerPendingEntryService().isPaymentMethodProcessedUsingPdp(getPaymentMethodCode())) {
        	
        	if (PaymentMethod.PM_CODE_INTERNAL_BILLING.equalsIgnoreCase(getPaymentMethodCode())){
        		 explicitEntry.setFinancialDocumentTypeCode(DOCUMENT_TYPE_INTERNAL_BILLING);
            }
        	else{
        		explicitEntry.setFinancialDocumentTypeCode(DOCUMENT_TYPE_NON_CHECK);
        	}
        }
    }
    
    public void doActionTaken(ActionTakenEvent event) {
        super.doActionTaken(event);
        WorkflowDocument workflowDocument = getDocumentHeader().getWorkflowDocument();
        String currentNode = null;
        Set<String> currentNodes = workflowDocument.getCurrentNodeNames();
        if (CollectionUtils.isNotEmpty(currentNodes)) {
            Object[] names = currentNodes.toArray();
            if (names.length > 0) {
                currentNode = (String)names[0];
            }
        }

        // everything in the below list requires correcting entries to be written to the GL
            if (PaymentRequestStatuses.getNodesRequiringCorrectingGeneralLedgerEntries().contains(currentNode)) {
            	// KFSPTS-2598 : Treasury also can 'calculate'
                if (PaymentRequestStatuses.NODE_ACCOUNT_REVIEW.equals(currentNode) || PaymentRequestStatuses.NODE_VENDOR_TAX_REVIEW.equals(currentNode)
                		|| PaymentRequestStatuses.NODE_PAYMENT_METHOD_REVIEW.equals(currentNode)) {
                    SpringContext.getBean(PurapGeneralLedgerService.class).generateEntriesModifyPaymentRequest(this);
                }
            }
        }

    @Override
    public List<String> getWorkflowEngineDocumentIdsToLock() {
        Stream<String> otherDocIdsToLock = Stream.of(super.getWorkflowEngineDocumentIdsToLock())
                .flatMap(idList -> (idList != null) ? idList.stream() : Stream.empty());
        
        Stream<String> poDocIdsToLock = getRelatedViews().getRelatedPurchaseOrderViews().stream()
                .filter(PurchaseOrderView::isPurchaseOrderCurrentIndicator)
                .map(PurchaseOrderView::getDocumentNumber);
        
        return Stream.concat(otherDocIdsToLock, poDocIdsToLock)
                .collect(Collectors.toUnmodifiableList());
    }

	public String getPaymentMethodCode() {
		return paymentMethodCode;
	}

	public void setPaymentMethodCode(String paymentMethodCode) {
		this.paymentMethodCode = paymentMethodCode;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public PaymentRequestWireTransfer getPreqWireTransfer() {
		if (ObjectUtils.isNull(preqWireTransfer)) {
			preqWireTransfer = new PaymentRequestWireTransfer();
			preqWireTransfer.setDocumentNumber(this.getDocumentNumber());
		}
		return preqWireTransfer;
	}

	public void setPreqWireTransfer(PaymentRequestWireTransfer preqWireTransfer) {
		this.preqWireTransfer = preqWireTransfer;
	}
}
