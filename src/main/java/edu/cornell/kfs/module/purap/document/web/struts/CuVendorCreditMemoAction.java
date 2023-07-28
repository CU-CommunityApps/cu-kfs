//UPGRADE-911 commenting out wire stuff
package edu.cornell.kfs.module.purap.document.web.struts;

import java.text.MessageFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kns.service.DocumentHelperService;
import org.kuali.kfs.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.kfs.krad.document.DocumentAuthorizer;
import org.kuali.kfs.krad.util.GlobalVariables;
//import org.kuali.kfs.fp.businessobject.WireCharge;
import org.kuali.kfs.module.purap.document.web.struts.VendorCreditMemoAction;
import org.kuali.kfs.module.purap.document.web.struts.VendorCreditMemoForm;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.module.purap.document.CuVendorCreditMemoDocument;
import edu.cornell.kfs.pdp.service.CuCheckStubService;

public class CuVendorCreditMemoAction extends VendorCreditMemoAction {

    private CuCheckStubService cuCheckStubService;

    @Override
    protected void loadDocument(KualiDocumentFormBase kualiDocumentFormBase) {
        super.loadDocument(kualiDocumentFormBase);
        
        CuVendorCreditMemoDocument document = (CuVendorCreditMemoDocument) kualiDocumentFormBase.getDocument();
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        
        if (workflowDocument.isInitiated() || workflowDocument.isSaved() || workflowDocument.isEnroute()
                || workflowDocument.isException()) {
            if (getCuCheckStubService().doesCheckStubNeedTruncatingForIso20022(document)) {
                String warningMessage = getCuCheckStubService()
                        .createWarningMessageForCheckStubIso20022MaxLength(document);
                GlobalVariables.getMessageMap().putWarning(
                        KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_CUSTOM, warningMessage);
            }
        }
    }

	@Override
	protected void createDocument(KualiDocumentFormBase kualiDocumentFormBase) {
		super.createDocument(kualiDocumentFormBase);
        // set wire charge message in form
        ((CuVendorCreditMemoForm) kualiDocumentFormBase).setWireChargeMessage(retrieveWireChargeMessage());
	}
	
    protected String retrieveWireChargeMessage() {
        String message = "";/*SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(KFSKeyConstants.MESSAGE_DV_WIRE_CHARGE);
        WireCharge wireCharge = new WireCharge();
        wireCharge.setUniversityFiscalYear(SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear());

        wireCharge = (WireCharge) SpringContext.getBean(BusinessObjectService.class).retrieve(wireCharge);
        Object[] args = { wireCharge.getDomesticChargeAmt(), wireCharge.getForeignChargeAmt() };*/

        return MessageFormat.format(message, "");
    }

	@Override
	public ActionForward calculate(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
        VendorCreditMemoForm cmForm = (VendorCreditMemoForm) form;
        CuVendorCreditMemoDocument creditMemoDocument = (CuVendorCreditMemoDocument) cmForm.getDocument();
        if ( creditMemoDocument.getDocumentHeader().getWorkflowDocument().isInitiated() 
                || creditMemoDocument.getDocumentHeader().getWorkflowDocument().isSaved() ) {
            // need to check whether the user has the permission to edit the bank code
            // if so, don't synchronize since we can't tell whether the value coming in
            // was entered by the user or not.
            DocumentAuthorizer docAuth = SpringContext.getBean(DocumentHelperService.class).getDocumentAuthorizer(creditMemoDocument);
            if ( !docAuth.isAuthorizedByTemplate(creditMemoDocument, 
                    KFSConstants.CoreModuleNamespaces.KFS, 
                    KFSConstants.PermissionTemplate.EDIT_BANK_CODE.name, 
                    GlobalVariables.getUserSession().getPrincipalId()  ) ) {
            	creditMemoDocument.synchronizeBankCodeWithPaymentMethod();        
            } else {
                // ensure that the name is updated properly
            	creditMemoDocument.refreshReferenceObject( "bank" );
            }
        }        
		return super.calculate(mapping, form, request, response);
	}

    private  CuCheckStubService getCuCheckStubService() {
        if (cuCheckStubService == null) {
            cuCheckStubService = SpringContext.getBean(CuCheckStubService.class);
        }
        return cuCheckStubService;
    }
}
