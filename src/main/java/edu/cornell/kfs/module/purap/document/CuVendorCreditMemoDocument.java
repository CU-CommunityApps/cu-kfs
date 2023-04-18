package edu.cornell.kfs.module.purap.document;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.module.purap.PurapWorkflowConstants;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.kns.service.DocumentHelperService;
import org.kuali.kfs.krad.document.DocumentAuthorizer;
import org.kuali.kfs.krad.rules.rule.event.KualiDocumentEvent;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.fp.businessobject.PaymentMethod;
import edu.cornell.kfs.fp.service.CUPaymentMethodGeneralLedgerPendingEntryService;
import edu.cornell.kfs.module.purap.CUPurapWorkflowConstants;
import edu.cornell.kfs.module.purap.businessobject.CreditMemoWireTransfer;

public class CuVendorCreditMemoDocument extends VendorCreditMemoDocument {
	private static final Logger LOG = LogManager.getLogger();
	
    public static String DOCUMENT_TYPE_NON_CHECK = "CMNC";

    protected String paymentMethodCode = "A"; //ACH check
    private static CUPaymentMethodGeneralLedgerPendingEntryService paymentMethodGeneralLedgerPendingEntryService;
    protected CreditMemoWireTransfer cmWireTransfer;
    
    @Override
    public void prepareForSave(KualiDocumentEvent event) {
    	super.prepareForSave(event);
    	
        try {
          	cmWireTransfer.setDocumentNumber(getDocumentNumber());
        } catch (Exception e) {
            LOG.info("cmWireTransfer is null" );
            cmWireTransfer = new CreditMemoWireTransfer();  
            cmWireTransfer.setDocumentNumber(getDocumentNumber());
       	
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
	public void customizeExplicitGeneralLedgerPendingEntry( GeneralLedgerPendingEntrySourceDetail postable, GeneralLedgerPendingEntry explicitEntry) {
    	super.customizeExplicitGeneralLedgerPendingEntry(postable, explicitEntry);
    	
        // KFSPTS-1891
        // if the document is not processed using PDP, then the cash entries need to be created instead of liability
        // so, switch the document type so the offset generation uses a cash offset object code
        if ( !getPaymentMethodGeneralLedgerPendingEntryService().isPaymentMethodProcessedUsingPdp(getPaymentMethodCode())) {
            explicitEntry.setFinancialDocumentTypeCode(DOCUMENT_TYPE_NON_CHECK);
        }
    }
    
    @Override
    public boolean answerSplitNodeQuestion(String nodeName) throws UnsupportedOperationException {
    	   if (nodeName.equals(PurapWorkflowConstants.REQUIRES_IMAGE_ATTACHMENT)) return requiresAccountsPayableReviewRouting();
           // KFSPTS-1891, KFSPTS-2851
           if (nodeName.equals(CUPurapWorkflowConstants.TREASURY_MANAGER))
               return isWireOrForeignDraft();
           throw new UnsupportedOperationException("Cannot answer split question for this node you call \""+nodeName+"\"");

    }
    
    // KFSPTS-1891, KFSPTS-2851
    private boolean isWireOrForeignDraft() {
        return StringUtils.equals(PaymentMethod.PM_CODE_WIRE, this.getPaymentMethodCode()) || StringUtils.equals(PaymentMethod.PM_CODE_FOREIGN_DRAFT, this.getPaymentMethodCode());
    }
    

	public String getPaymentMethodCode() {
		return paymentMethodCode;
	}

	public void setPaymentMethodCode(String paymentMethodCode) {
		this.paymentMethodCode = paymentMethodCode;
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

    protected CUPaymentMethodGeneralLedgerPendingEntryService getPaymentMethodGeneralLedgerPendingEntryService() {
        if ( paymentMethodGeneralLedgerPendingEntryService == null ) {
            paymentMethodGeneralLedgerPendingEntryService = SpringContext.getBean(CUPaymentMethodGeneralLedgerPendingEntryService.class);
        }
        return paymentMethodGeneralLedgerPendingEntryService;
    }

	public CreditMemoWireTransfer getCmWireTransfer() {
		if (ObjectUtils.isNull(cmWireTransfer)) {
			cmWireTransfer = new CreditMemoWireTransfer();
			cmWireTransfer.setDocumentNumber(this.getDocumentNumber());
		}
		return cmWireTransfer;
	}

	public void setCmWireTransfer(CreditMemoWireTransfer cmWireTransfer) {
		this.cmWireTransfer = cmWireTransfer;
	}


}
