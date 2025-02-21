package edu.cornell.kfs.module.purap.document;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kew.framework.postprocessor.DocumentRouteStatusChange;
import org.kuali.kfs.kns.service.DocumentHelperService;
import org.kuali.kfs.krad.document.DocumentAuthorizer;
import org.kuali.kfs.krad.rules.rule.event.KualiDocumentEvent;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapWorkflowConstants;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.businessobject.PaymentSourceWireTransfer;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.PaymentSource;

import edu.cornell.kfs.fp.service.CUPaymentMethodGeneralLedgerPendingEntryService;
import edu.cornell.kfs.module.purap.CUPurapWorkflowConstants;
import edu.cornell.kfs.pdp.service.CuCheckStubService;

public class CuVendorCreditMemoDocument extends VendorCreditMemoDocument implements PaymentSource {
    private static final Logger LOG = LogManager.getLogger();
    
    public static String DOCUMENT_TYPE_NON_CHECK = "CMNC";

    private static CUPaymentMethodGeneralLedgerPendingEntryService paymentMethodGeneralLedgerPendingEntryService;
    private static CuCheckStubService cuCheckStubService;
    protected PaymentSourceWireTransfer wireTransfer;
    
    public CuVendorCreditMemoDocument() {
        super();
        setPaymentMethodCode(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_CHECK);
    }
    
    @Override
    public void prepareForSave(final KualiDocumentEvent event) {
        super.prepareForSave(event);
        
        try {
            wireTransfer.setDocumentNumber(getDocumentNumber());
        } catch (Exception e) {
            LOG.info("wireTransfer is null" );
            wireTransfer = new PaymentSourceWireTransfer();  
            wireTransfer.setDocumentNumber(getDocumentNumber());
        }
    
        // KFSPTS-1981
        // First, only do this if the document is in initiated status - after that, we don't want to 
        // accidentally reset the bank code
        if ( getDocumentHeader().getWorkflowDocument().isInitiated() ||  getDocumentHeader().getWorkflowDocument().isSaved() ) {
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
    public void customizeExplicitGeneralLedgerPendingEntry( final GeneralLedgerPendingEntrySourceDetail postable, final GeneralLedgerPendingEntry explicitEntry) {
        super.customizeExplicitGeneralLedgerPendingEntry(postable, explicitEntry);
        
        // KFSPTS-1891
        // if the document is not processed using PDP, then the cash entries need to be created instead of liability
        // so, switch the document type so the offset generation uses a cash offset object code
        if ( !getPaymentMethodGeneralLedgerPendingEntryService().isPaymentMethodProcessedUsingPdp(getPaymentMethodCode())) {
            explicitEntry.setFinancialDocumentTypeCode(DOCUMENT_TYPE_NON_CHECK);
        }
    }
    
    @Override
    public boolean answerSplitNodeQuestion(final String nodeName) throws UnsupportedOperationException {
        if (nodeName.equals(PurapWorkflowConstants.REQUIRES_IMAGE_ATTACHMENT)) {
            return requiresAccountsPayableReviewRouting();
        }
        if (nodeName.equals(CUPurapWorkflowConstants.TREASURY_MANAGER)) {
            /*
             * CU Customization KFSPTS-34074
             * This fixes canceled and disapproved CM documents that were done before 2/9/2025,
             * which is when the 2023-04-19 version of Kuali Financials was installed into cu-kfs.  
             */
            return false;
        }
        throw new UnsupportedOperationException("Cannot answer split question for this node you call \""+nodeName+"\"");
    }
    
    public void synchronizeBankCodeWithPaymentMethod() {
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

    @Override
    public void doRouteStatusChange(final DocumentRouteStatusChange statusChangeEvent) {
        if (getDocumentHeader().getWorkflowDocument().isProcessed()) {
            if (getCuCheckStubService().doesCheckStubNeedTruncatingForIso20022(this)) {
                getCuCheckStubService().addNoteToDocumentRegardingCheckStubIso20022MaxLength(this);
            }
        }
        super.doRouteStatusChange(statusChangeEvent);
    }

    protected CUPaymentMethodGeneralLedgerPendingEntryService getPaymentMethodGeneralLedgerPendingEntryService() {
        if ( paymentMethodGeneralLedgerPendingEntryService == null ) {
            paymentMethodGeneralLedgerPendingEntryService = SpringContext.getBean(CUPaymentMethodGeneralLedgerPendingEntryService.class);
        }
        return paymentMethodGeneralLedgerPendingEntryService;
    }

    public PaymentSourceWireTransfer getWireTransfer() {
        if (ObjectUtils.isNull(wireTransfer)) {
            wireTransfer = new PaymentSourceWireTransfer();
            wireTransfer.setDocumentNumber(this.getDocumentNumber());
        }
        return wireTransfer;
    }

    public void setWireTransfer(PaymentSourceWireTransfer wireTransfer) {
        this.wireTransfer = wireTransfer;
    }

    protected static CuCheckStubService getCuCheckStubService() {
        if (cuCheckStubService == null) {
            cuCheckStubService = SpringContext.getBean(CuCheckStubService.class);
        }
        return cuCheckStubService;
    }

    @Override
    public boolean hasAttachment() {
        return false;
    }

    @Override
    public String getCampusCode() {
        return getProcessingCampusCode();
    }

}