//UPGRADE-911 commenting out wire stuff
package edu.cornell.kfs.module.purap.document.web.struts;

import java.text.MessageFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.kns.service.DocumentHelperService;
import org.kuali.kfs.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.kfs.krad.document.DocumentAuthorizer;
import org.kuali.kfs.krad.util.GlobalVariables;
//import org.kuali.kfs.fp.businessobject.WireCharge;
import org.kuali.kfs.module.purap.document.web.struts.VendorCreditMemoAction;
import org.kuali.kfs.module.purap.document.web.struts.VendorCreditMemoForm;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.module.purap.document.CuVendorCreditMemoDocument;
import edu.cornell.kfs.pdp.service.CuCheckStubService;

public class CuVendorCreditMemoAction extends VendorCreditMemoAction {

    private CuCheckStubService cuCheckStubService;

    @Override
    protected void loadDocument(final KualiDocumentFormBase kualiDocumentFormBase) {
        super.loadDocument(kualiDocumentFormBase);
        getCuCheckStubService().addIso20022CheckStubLengthWarningToDocumentIfNecessary(
                kualiDocumentFormBase.getDocument());
    }

	@Override
	protected void createDocument(final KualiDocumentFormBase kualiDocumentFormBase) {
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
    public ActionForward calculate(
            final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final VendorCreditMemoForm cmForm = (VendorCreditMemoForm) form;
        final CuVendorCreditMemoDocument creditMemoDocument = (CuVendorCreditMemoDocument) cmForm.getDocument();
        if ( creditMemoDocument.getDocumentHeader().getWorkflowDocument().isInitiated() 
                || creditMemoDocument.getDocumentHeader().getWorkflowDocument().isSaved() ) {
            // need to check whether the user has the permission to edit the bank code
            // if so, don't synchronize since we can't tell whether the value coming in
            // was entered by the user or not.
            final DocumentAuthorizer docAuth = SpringContext.getBean(DocumentHelperService.class).getDocumentAuthorizer(creditMemoDocument);
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
