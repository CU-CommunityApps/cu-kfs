package edu.cornell.kfs.fp.document.web.struts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.fp.document.service.impl.DisbursementVoucherPayeeServiceImpl;
import org.kuali.kfs.fp.document.web.struts.DisbursementVoucherAction;
import org.kuali.kfs.integration.ar.AccountsReceivableCustomer;
import org.kuali.kfs.integration.ar.AccountsReceivableCustomerAddress;
import org.kuali.kfs.integration.ar.AccountsReceivableModuleService;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.document.authorization.TransactionalDocumentAuthorizer;
import org.kuali.kfs.kns.document.authorization.TransactionalDocumentPresentationController;
import org.kuali.kfs.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.rules.rule.event.SaveDocumentEvent;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.KualiRuleService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

import edu.cornell.kfs.fp.document.CuDisbursementVoucherConstants;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import edu.cornell.kfs.module.purap.CUPurapKeyConstants;
import edu.cornell.kfs.module.purap.document.IWantDocument;
import edu.cornell.kfs.module.purap.document.service.IWantDocumentService;
import edu.cornell.kfs.pdp.service.CuCheckStubService;
import edu.cornell.kfs.sys.util.ConfidentialAttachmentUtil;

public class CuDisbursementVoucherAction extends DisbursementVoucherAction {
    private static final Logger LOG = LogManager.getLogger();

    private CuCheckStubService cuCheckStubService;

    @Override
    protected void loadDocument(final KualiDocumentFormBase kualiDocumentFormBase) {
        super.loadDocument(kualiDocumentFormBase);
        getCuCheckStubService().addIso20022CheckStubLengthWarningToDocumentIfNecessary(
                kualiDocumentFormBase.getDocument());
    }

    @Override
    public ActionForward refresh(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        CuDisbursementVoucherForm dvForm = (CuDisbursementVoucherForm) form;

        ActionForward actionAfterPayeeLookup = this.refreshAfterPayeeSelection(mapping, dvForm, request);
        if (actionAfterPayeeLookup != null) {
            return actionAfterPayeeLookup;
        }
        return super.refresh(mapping, form, request, response);
    }
    
    protected ActionForward refreshAfterPayeeSelection(ActionMapping mapping, CuDisbursementVoucherForm dvForm,
            HttpServletRequest request) {
        String refreshCaller = dvForm.getRefreshCaller();
        
        CuDisbursementVoucherDocument document = (CuDisbursementVoucherDocument) dvForm.getDocument();

        boolean isPayeeLookupable = KFSConstants.KUALI_DISBURSEMENT_PAYEE_LOOKUPABLE_IMPL.equals(refreshCaller);
        boolean isAddressLookupable = KFSConstants.KUALI_VENDOR_ADDRESS_LOOKUPABLE_IMPL.equals(refreshCaller);
        boolean isKualiLookupable = KFSConstants.KUALI_LOOKUPABLE_IMPL.equals(refreshCaller);
        
        // if a cancel occurred on address lookup we need to reset the payee id and type, rest of fields will still have
        // correct information
        if (refreshCaller == null && hasFullEdit(document)) {
            dvForm.setPayeeIdNumber(dvForm.getTempPayeeIdNumber());
            dvForm.setHasMultipleAddresses(false);
            document.getDvPayeeDetail().setDisbVchrPayeeIdNumber(dvForm.getTempPayeeIdNumber());
            document.getDvPayeeDetail().setDisbursementVoucherPayeeTypeCode(dvForm.getOldPayeeType());
            
            return null;
        }
        
        // do not execute the further refreshing logic if the refresh caller is not a lookupable
        if (!isPayeeLookupable && !isAddressLookupable && !isKualiLookupable) {
            return null;
        }

        // do not execute the further refreshing logic if a payee is not selected
        String payeeIdNumber = document.getDvPayeeDetail().getDisbVchrPayeeIdNumber();
        if (payeeIdNumber == null) {
            return null;
        }

        dvForm.setPayeeIdNumber(payeeIdNumber);
        dvForm.setHasMultipleAddresses(false);

        // determine whether the selected vendor has multiple addresses. If so, redirect to the address selection screen
        if (isPayeeLookupable) {
            if (dvForm.isVendor()) {
                VendorDetail refreshVendorDetail = new VendorDetail();
                refreshVendorDetail.setVendorNumber(payeeIdNumber);
                refreshVendorDetail = (VendorDetail) SpringContext.getBean(BusinessObjectService.class).retrieve(refreshVendorDetail);
    
                VendorAddress defaultVendorAddress = null;
                if (refreshVendorDetail != null) {
                    List<VendorAddress> vendorAddresses = refreshVendorDetail.getVendorAddresses();
                    boolean hasMultipleAddresses = vendorAddresses != null && vendorAddresses.size() > 1;
                    dvForm.setHasMultipleAddresses(hasMultipleAddresses);
    
                    if (vendorAddresses != null && !vendorAddresses.isEmpty()) {
                        defaultVendorAddress = vendorAddresses.get(0);
                    }
                }
    
                if (dvForm.hasMultipleAddresses()) {
                    return renderVendorAddressSelection(mapping, request, dvForm);
                }
                else if (defaultVendorAddress != null) {
                    setupPayeeAsVendor(dvForm, payeeIdNumber, defaultVendorAddress.getVendorAddressGeneratedIdentifier().toString());
                }
    
                return null;
            }
            else if (dvForm.isEmployee()) {
                this.setupPayeeAsEmployee(dvForm, payeeIdNumber);
            }
            else if (dvForm.isStudent()) {
                this.setupPayeeAsStudent(dvForm, payeeIdNumber);
            }
            else if (dvForm.isAlumni()) {
                this.setupPayeeAsAlumni(dvForm, payeeIdNumber);
            }
        }
        
        String payeeAddressIdentifier = request.getParameter(KFSPropertyConstants.VENDOR_ADDRESS_GENERATED_ID);
        if (isAddressLookupable && StringUtils.isNotBlank(payeeAddressIdentifier)) {
            setupPayeeAsVendor(dvForm, payeeIdNumber, payeeAddressIdentifier);
        }
        
        // check for multiple custom addresses
        if (isPayeeLookupable && dvForm.isCustomer()) {
            AccountsReceivableCustomer customer = SpringContext.getBean(AccountsReceivableModuleService.class).findCustomer(payeeIdNumber);

            AccountsReceivableCustomerAddress defaultCustomerAddress = null;
            if (customer != null) {
                defaultCustomerAddress = customer.getPrimaryAddress();

                Map<String, String> addressSearch = new HashMap<>();
                addressSearch.put(KFSPropertyConstants.CUSTOMER_NUMBER, payeeIdNumber);

                List<AccountsReceivableCustomerAddress> customerAddresses = (List<AccountsReceivableCustomerAddress>)
                                             SpringContext.getBean(AccountsReceivableModuleService.class).searchForCustomerAddresses(addressSearch);
                if (customerAddresses != null && !customerAddresses.isEmpty()) {
                    if (customerAddresses.size() > 1) {
                        dvForm.setHasMultipleAddresses(true);
                    }
                    else if (defaultCustomerAddress == null) {
                        defaultCustomerAddress = customerAddresses.get(0);
                    }
                }
            }

            if (dvForm.hasMultipleAddresses()) {
                return renderCustomerAddressSelection(mapping, request, dvForm);
            }
            else if (defaultCustomerAddress != null) {
                setupPayeeAsCustomer(dvForm, payeeIdNumber, defaultCustomerAddress.getCustomerAddressIdentifier().toString());
            }
        }

        String customerAddressIdentifier = request.getParameter(KFSPropertyConstants.CUSTOMER_ADDRESS_IDENTIFIER);
        if (isKualiLookupable && StringUtils.isNotBlank(customerAddressIdentifier)) {
            setupPayeeAsCustomer(dvForm, payeeIdNumber, customerAddressIdentifier);
        }
        
        String paymentReasonCode = document.getDvPayeeDetail().getDisbVchrPaymentReasonCode();
        addPaymentCodeWarningMessage(dvForm, paymentReasonCode);

        return null;
    }
    
    protected void setupPayeeAsEmployee(CuDisbursementVoucherForm dvForm, String payeeIdNumber) {
        Person person = (Person) SpringContext.getBean(PersonService.class).getPersonByEmployeeId(payeeIdNumber);
        if (person == null ) {
            person = (Person) SpringContext.getBean(PersonService.class).getPerson(payeeIdNumber);
        }
        if (person != null) {
            ((CuDisbursementVoucherDocument) dvForm.getDocument()).templateEmployee(person);
            dvForm.setTempPayeeIdNumber(payeeIdNumber);
            dvForm.setOldPayeeType(KFSConstants.PaymentPayeeTypes.EMPLOYEE);

        }
        else {
            LOG.error("Exception while attempting to retrieve universal user by universal user id {}", payeeIdNumber);
        }
    }
    
    /**
     * setup the payee as an student with the given id number
     */
    protected void setupPayeeAsStudent(CuDisbursementVoucherForm dvForm, String payeeIdNumber) {
        Person person = (Person) SpringContext.getBean(PersonService.class).getPerson(payeeIdNumber);
        if (person != null) {
            ((CuDisbursementVoucherDocument) dvForm.getDocument()).templateStudent(person);
            dvForm.setTempPayeeIdNumber(payeeIdNumber);
            dvForm.setOldPayeeType(CuDisbursementVoucherConstants.DV_PAYEE_TYPE_STUDENT);

        }
        else {
            LOG.error("Exception while attempting to retrieve universal user by universal user id " + payeeIdNumber);
        }
    }

    /**
     * setup the payee as an alumni with the given id number
     */
    protected void setupPayeeAsAlumni(CuDisbursementVoucherForm dvForm, String payeeIdNumber) {
        Person person = (Person) SpringContext.getBean(PersonService.class).getPerson(payeeIdNumber);
        if (person != null) {
            ((CuDisbursementVoucherDocument) dvForm.getDocument()).templateAlumni(person);
            dvForm.setTempPayeeIdNumber(payeeIdNumber);
            dvForm.setOldPayeeType(CuDisbursementVoucherConstants.DV_PAYEE_TYPE_ALUMNI);

        }
        else {
            LOG.error("Exception while attempting to retrieve universal user by universal user id " + payeeIdNumber);
        }
    }
    
    protected boolean hasFullEdit(CuDisbursementVoucherDocument document) {
        final Person user = GlobalVariables.getUserSession().getPerson();
        final TransactionalDocumentPresentationController documentPresentationController = (TransactionalDocumentPresentationController) getDocumentHelperService()
                .getDocumentPresentationController(document);
        final TransactionalDocumentAuthorizer documentAuthorizer = (TransactionalDocumentAuthorizer) getDocumentHelperService()
                .getDocumentAuthorizer(document);
        Set<String> documentActions =  documentPresentationController.getDocumentActions(document);
        documentActions = documentAuthorizer.getDocumentActions(document, user, documentActions);

        Set<String> editModes = documentPresentationController.getEditModes(document);
        editModes = documentAuthorizer.getEditModes(document, user, editModes);
        
        return documentActions.contains(KRADConstants.KUALI_ACTION_CAN_EDIT) && editModes.contains("fullEntry");
    }
    
    
    
    /**
     * Creates a DV document based on information on an I Want document.
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward createDVFromIWantDoc(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        String iWantDocumentNumber = request.getParameter("docId");
        CuDisbursementVoucherForm disbursementVoucherForm = (CuDisbursementVoucherForm) form;

        IWantDocument iWantDocument = (IWantDocument) getDocumentService().getByDocumentHeaderId(iWantDocumentNumber);
        
        // Do not allow the DV to be created if the IWNT doc is already associated with another DV.
        if (iWantDocument != null && (StringUtils.isNotBlank(iWantDocument.getReqsDocId()) || StringUtils.isNotBlank(iWantDocument.getDvDocId())) ) {
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, CUPurapKeyConstants.ERROR_DV_OR_REQ_ALREADY_CREATED_FROM_IWNT, iWantDocumentNumber);
            return mapping.findForward("error");
        }
        
        IWantDocumentService iWantDocumentService = SpringContext.getBean(IWantDocumentService.class);

        createDocument(disbursementVoucherForm);
        
        CuDisbursementVoucherDocument disbursementVoucherDocument = (CuDisbursementVoucherDocument)disbursementVoucherForm.getDocument();

        iWantDocumentService.setUpDVDetailsFromIWantDoc(iWantDocument, disbursementVoucherDocument, disbursementVoucherForm);
        
        // Set the DV doc ID reference on the IWantDocument.
        iWantDocumentService.updateIWantDocumentWithDisbursementVoucherReference(
                iWantDocument, disbursementVoucherDocument.getDocumentNumber());

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Overridden to treat "Confidential" add-attachment authorization failures as validation errors, rather than throwing an authorization exception.
     * 
     * @see org.kuali.kfs.kns.web.struts.action.KualiDocumentActionBase#insertBONote()
     */
    @SuppressWarnings("deprecation")
    @Override
    public ActionForward insertBONote(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        CuDisbursementVoucherForm dvForm = (CuDisbursementVoucherForm) form;
        Note newNote = dvForm.getNewNote();
        
        // If trying to add a conf attachment without authorization or not properly flagging a potentially-conf attachment, then treat as a validation failure.
        if (!ConfidentialAttachmentUtil.attachmentIsNonConfidentialOrCanAddConfAttachment(newNote, dvForm.getDocument(), dvForm.getAttachmentFile(),
                getDocumentHelperService().getDocumentAuthorizer(dvForm.getDocument()))) {
            // Just return without adding the note/attachment. The ConfidentialAttachmentUtil method will handle updating the message map accordingly.
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }
        
        // If not "Confidential" or if authorized to add such attachments, then proceed with the superclass processing.
        return super.insertBONote(mapping, form, request, response);
    }

    /**
     * Overridden to also perform address change notifications if the document is enroute.
     * 
     * @see org.kuali.kfs.sys.web.struts.KualiAccountingDocumentActionBase#save()
     */
    @SuppressWarnings("deprecation")
    @Override
    public ActionForward save(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final CuDisbursementVoucherForm dvForm = (CuDisbursementVoucherForm) form;
        ActionForward forward;
        
        // If the document is ENROUTE, then also send address change notifications as needed.
        if (dvForm.getFinancialDocument().getDocumentHeader().getWorkflowDocument().isEnroute()) {
            final boolean passed = SpringContext.getBean(KualiRuleService.class).applyRules(new SaveDocumentEvent(dvForm.getFinancialDocument()));
            if (passed) {
                SpringContext.getBean(DisbursementVoucherPayeeServiceImpl.class).checkPayeeAddressForChanges(
                        (CuDisbursementVoucherDocument) dvForm.getFinancialDocument());
            }
            
            forward = super.save(mapping, form, request, response);
            
            if (passed && CollectionUtils.isNotEmpty(dvForm.getFinancialDocument().getAdHocRoutePersons())
                    && GlobalVariables.getMessageMap().hasNoErrors()) {
                sendAdHocRequests(mapping, form, request, response);
            }
        } else {
            // If the document is not ENROUTE, then just proceed as in the superclass.
            forward = super.save(mapping, form, request, response);
        }
        
        return forward;
    }

    private CuCheckStubService getCuCheckStubService() {
        if (cuCheckStubService == null) {
            cuCheckStubService = SpringContext.getBean(CuCheckStubService.class);
        }
        return cuCheckStubService;
    }

}
