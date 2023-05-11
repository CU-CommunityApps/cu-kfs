package edu.cornell.kfs.module.purap.document.web.struts;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kns.document.authorization.TransactionalDocumentAuthorizer;
import org.kuali.kfs.kns.document.authorization.TransactionalDocumentPresentationController;
import org.kuali.kfs.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.exception.AuthorizationException;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.KualiRuleService;
import org.kuali.kfs.krad.service.SessionDocumentService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.RequisitionItem;
import org.kuali.kfs.module.purap.document.PurchasingDocument;
import org.kuali.kfs.module.purap.document.PurchasingDocumentBase;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.document.validation.event.AttributedAddPurchasingAccountsPayableItemEvent;
import org.kuali.kfs.module.purap.document.web.struts.PurchasingFormBase;
import org.kuali.kfs.module.purap.document.web.struts.RequisitionAction;
import org.kuali.kfs.module.purap.document.web.struts.RequisitionForm;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorCommodityCode;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kew.api.exception.WorkflowException;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.document.CuRequisitionDocument;
import edu.cornell.kfs.module.purap.document.IWantDocument;
import edu.cornell.kfs.module.purap.document.service.CuPurapService;
import edu.cornell.kfs.module.purap.document.service.IWantDocumentService;
import edu.cornell.kfs.sys.businessobject.NoteExtendedAttribute;

public class CuRequisitionAction extends RequisitionAction {

    @SuppressWarnings("unchecked")
    @Override
    public ActionForward addItem(ActionMapping mapping, ActionForm form, HttpServletRequest request, 
            HttpServletResponse response) throws Exception {
        PurchasingFormBase purchasingForm = (PurchasingFormBase) form;
        PurApItem item = purchasingForm.getNewPurchasingItemLine();
        RequisitionItem requisitionItem = (RequisitionItem) item;
        PurchasingDocument purDocument = (PurchasingDocument) purchasingForm.getDocument();
        
        if (StringUtils.isBlank(requisitionItem.getPurchasingCommodityCode())) {
            boolean commCodeParam = SpringContext.getBean(ParameterService.class).getParameterValueAsBoolean(
                    CuRequisitionDocument.class, PurapParameterConstants.ENABLE_DEFAULT_VENDOR_COMMODITY_CODE_IND);

            if (commCodeParam && purchasingForm instanceof RequisitionForm) {
                CuRequisitionDocument reqs = (CuRequisitionDocument) purchasingForm.getDocument();
                VendorDetail dtl = reqs.getVendorDetail();
                if (ObjectUtils.isNotNull(dtl)) {
                    List<VendorCommodityCode> vcc = dtl.getVendorCommodities();
                    for (VendorCommodityCode commodity : vcc) {
                        if (commodity.isCommodityDefaultIndicator()) {
                            requisitionItem.setPurchasingCommodityCode(commodity.getPurchasingCommodityCode());
                        }
                    }
                }
            }
        }
        
        boolean rulePassed = SpringContext.getBean(KualiRuleService.class)
                .applyRules(new AttributedAddPurchasingAccountsPayableItemEvent("", purDocument, item));

        if (rulePassed) {
            item = purchasingForm.getAndResetNewPurchasingItemLine();
            purDocument.addItem(item);
            // KFSPTS-985
            if (((PurchasingDocumentBase)(purDocument)).isIntegratedWithFavoriteAccount()) {
                populatePrimaryFavoriteAccount(item.getSourceAccountingLines(), getAccountClassFromNewPurApAccountingLine(purchasingForm));
            }
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }
    
    public ActionForward clearVendor(ActionMapping mapping, ActionForm form, HttpServletRequest request, 
            HttpServletResponse response) throws Exception {
        PurchasingFormBase baseForm = (PurchasingFormBase) form;
        CuRequisitionDocument document = (CuRequisitionDocument) baseForm.getDocument();

        document.setVendorHeaderGeneratedIdentifier(null);
        document.setVendorDetailAssignedIdentifier(null);
        document.setVendorDetail(null);
        document.setVendorName("");
        document.setVendorLine1Address("");
        document.setVendorLine2Address("");
        document.setVendorAddressInternationalProvinceName("");
        document.setVendorCityName("");
        document.setVendorStateCode("");
        document.setVendorPostalCode("");
        document.setVendorCountryCode("");
        document.setVendorContractGeneratedIdentifier(null);
        document.setVendorContract(null);
        document.setVendorFaxNumber("");
        document.setVendorCustomerNumber("");
        document.setVendorAttentionName("");
        document.setVendorAddressGeneratedIdentifier(null);  
        //clearing value that was set in PurchasingDocumentBase.templateVendorAction
        document.setVendorAddressGeneratedIdentifier(null);  
        //clearing value that was set in PurchasingDocumentBase.templateVendorAction
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }
    
    /**
     * Overridden to guarantee that form of copied document is set to whatever the entry mode of the document is
     * @see org.kuali.kfs.kns.web.struts.action.KualiTransactionalDocumentActionBase#copy
     * (org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, 
     * javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward copy(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ActionForward forward = null;
        String docID = "docId";
        if (request.getParameter(docID) == null) {
            forward = super.copy(mapping, form, request, response);
        } else {
            // this is copy document from Procurement Gateway:
            // use this url to call: http://localhost:8080/kfs-dev/purapRequisition.do?methodToCall=copy&docId=xxxx
            String docId = request.getParameter(docID);
            KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
               
            CuRequisitionDocument document = null;
            document = (CuRequisitionDocument)getDocumentService().getByDocumentHeaderId(docId);
            document.toCopyFromGateway();
           
            kualiDocumentFormBase.setDocument(document);
            WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
            kualiDocumentFormBase.setDocTypeName(workflowDocument.getDocumentTypeName());
            SpringContext.getBean(SessionDocumentService.class).addDocumentToUserSession(GlobalVariables.getUserSession(), workflowDocument);
                     
            forward = mapping.findForward(KFSConstants.MAPPING_BASIC);   
        }
        return forward;
    }
    
    /**
    * Creates a requisition document based on information from an I Want document.
    *
    * @param mapping
    * @param form
    * @param request
    * @param response
    * @return
    * @throws Exception
    */
    @SuppressWarnings("deprecation")
    public ActionForward createReqFromIWantDoc(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        String iWantDocumentNumber = request.getParameter(KRADConstants.PARAMETER_DOC_ID);
        RequisitionForm requisitionForm = (RequisitionForm) form;

        IWantDocument iWantDocument = (IWantDocument) getDocumentService().getByDocumentHeaderId(iWantDocumentNumber);
        if (iWantDocument == null) {
            throw new WorkflowException("Could not find IWantDocument with ID '" + iWantDocumentNumber + "'");
        }

        // Make sure the user is authorized to create the req in this manner.
        TransactionalDocumentPresentationController pControl =
                (TransactionalDocumentPresentationController) getDocumentHelperService().getDocumentPresentationController(iWantDocument);
        TransactionalDocumentAuthorizer authorizer = (TransactionalDocumentAuthorizer) getDocumentHelperService().getDocumentAuthorizer(iWantDocument);
        Set<String> iwntEditModes = authorizer.getEditModes(iWantDocument, GlobalVariables.getUserSession().getPerson(), pControl.getEditModes(iWantDocument));
        if (!iwntEditModes.contains(CUPurapConstants.IWNT_DOC_CREATE_REQ)) {
            throw new AuthorizationException(GlobalVariables.getUserSession().getPrincipalId(), CUPurapConstants.IWNT_DOC_CREATE_REQ,
                    CuRequisitionDocument.class.getSimpleName(), "user is not authorized to create requisitions from IWantDocument '"
                    + iWantDocumentNumber + "'", Collections.<String,Object>emptyMap());
        }

        // Do not allow the req to be created if the IWNT doc is already associated with another req.
        if (iWantDocument != null && (StringUtils.isNotBlank(iWantDocument.getReqsDocId()) || StringUtils.isNotBlank(iWantDocument.getDvDocId()))) {
            throw new WorkflowException("Cannot create requisition from IWantDocument '" + iWantDocumentNumber +
                    "' because a DV or Requisition has already been created from that document");
        }

        IWantDocumentService iWantDocumentService = SpringContext.getBean(IWantDocumentService.class);

        createDocument(requisitionForm);

        RequisitionDocument requisitionDocument = requisitionForm.getRequisitionDocument();

        iWantDocumentService.setUpRequisitionDetailsFromIWantDoc(iWantDocument, requisitionDocument, requisitionForm);

        // Set the requisition doc ID reference on the IWantDocument.
        iWantDocumentService.updateIWantDocumentWithRequisitionReference(
                iWantDocument, requisitionDocument.getDocumentNumber());

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    @Override
    public ActionForward insertBONote(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        Note newNote = kualiDocumentFormBase.getNewNote();
        NoteExtendedAttribute extendedAttribute = (NoteExtendedAttribute) newNote.getExtension();

        ActionForward forward = super.insertBONote(mapping, form, request, response);

        if (newNote != kualiDocumentFormBase.getNewNote()) {
            Note addedNote = kualiDocumentFormBase.getDocument().getNotes().get(kualiDocumentFormBase.getDocument().getNotes().size() - 1);
            extendedAttribute.setNoteIdentifier(addedNote.getNoteIdentifier());
            addedNote.setExtension(extendedAttribute);
            SpringContext.getBean(BusinessObjectService.class).save(extendedAttribute);
            addedNote.refreshReferenceObject("extension");
        }

        return forward;
    }

    @Override
    public ActionForward refresh(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ActionForward forward = super.refresh(mapping, form, request, response);
        
        RequisitionForm requisitionForm = (RequisitionForm) form;
        RequisitionDocument document = requisitionForm.getRequisitionDocument();
        document.setOrganizationAutomaticPurchaseOrderLimit(getPurapService().getApoLimit(document));
        
        return forward;
    }

    protected CuPurapService getPurapService() {
        return SpringContext.getBean(CuPurapService.class);
    }

}

