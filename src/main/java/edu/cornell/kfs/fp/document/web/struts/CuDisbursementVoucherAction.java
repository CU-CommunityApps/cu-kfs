package edu.cornell.kfs.fp.document.web.struts;



import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.fp.document.web.struts.DisbursementVoucherAction;
import org.kuali.kfs.fp.document.web.struts.DisbursementVoucherForm;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.rice.core.api.util.RiceConstants;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.kns.document.authorization.TransactionalDocumentAuthorizer;
import org.kuali.rice.kns.document.authorization.TransactionalDocumentPresentationController;
import org.kuali.rice.kns.question.ConfirmationQuestion;
import org.kuali.rice.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.ObjectUtils;
import org.kuali.rice.krad.util.UrlFactory;

import edu.cornell.kfs.fp.document.CuDisbursementVoucherConstants;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import edu.cornell.kfs.fp.document.service.CULegacyTravelService;
import edu.cornell.kfs.vnd.businessobject.CuVendorAddress;



public class CuDisbursementVoucherAction extends DisbursementVoucherAction {    
    @SuppressWarnings("deprecation")
    @Override
    public ActionForward disapprove(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Object question = request.getParameter(KRADConstants.QUESTION_INST_ATTRIBUTE_NAME);
        String reason = request.getParameter(KRADConstants.QUESTION_REASON_ATTRIBUTE_NAME);
        Object buttonClicked = request.getParameter(KRADConstants.QUESTION_CLICKED_BUTTON);

        if(ObjectUtils.isNotNull(question) && ConfirmationQuestion.YES.equals(buttonClicked)) {
            KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
            
            // if travel DV, then reopen associated trip
            Boolean tripReOpened = true;
            boolean isTravelDV = false;
            try {
                CULegacyTravelService cuLegacyTravelService = SpringContext.getBean(CULegacyTravelService.class);
                String tripID = cuLegacyTravelService.getLegacyTripID(kualiDocumentFormBase.getDocId());
                if(isTravelDV = StringUtils.isNotEmpty(tripID)) { // This means the DV is a Travel DV
                    tripReOpened &= cuLegacyTravelService.reopenLegacyTrip(kualiDocumentFormBase.getDocId(), reason);
                    System.out.println("Trip successfully reopened : "+tripReOpened);
                } else {
                    LOG.info("DV is not a travel DV");
                }
            } catch (Exception ex) {
                LOG.info("Exception occurred while trying to disapprove a disbursement voucher.");
                ex.printStackTrace();
                tripReOpened=false;
            }
    
            if(!isTravelDV || (isTravelDV && tripReOpened)) {
                return super.disapprove(mapping, form, request, response);
            } else {
                // TODO add message to DV indicating why doc was not canceled.
                return mapping.findForward(RiceConstants.MAPPING_BASIC);
            }
        } else {
            return super.disapprove(mapping, form, request, response);
        }
    }

    /**
     * Calls the document service to cancel the document
     *
     * @see org.kuali.rice.kns.web.struts.action.KualiDocumentActionBase#cancel()
     */
    @Override
    public ActionForward cancel(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Object question = request.getParameter(KRADConstants.QUESTION_INST_ATTRIBUTE_NAME);
        // this should probably be moved into a private instance variable
        // logic for cancel question
        if (question == null) {
            // ask question if not already asked
            return this.performQuestionWithoutInput(mapping, form, request, response, KRADConstants.DOCUMENT_CANCEL_QUESTION, getKualiConfigurationService().getPropertyValueAsString("document.question.cancel.text"), KRADConstants.CONFIRMATION_QUESTION, KRADConstants.MAPPING_CANCEL, "");
        }
        else {
            Object buttonClicked = request.getParameter(KRADConstants.QUESTION_CLICKED_BUTTON);
            if ((KRADConstants.DOCUMENT_CANCEL_QUESTION.equals(question)) && ConfirmationQuestion.NO.equals(buttonClicked)) {
                // if no button clicked just reload the doc
                return mapping.findForward(RiceConstants.MAPPING_BASIC);
            }
            // else go to cancel logic below
        }

        KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        
        // if travel DV, then reopen associated trip
        Boolean tripReOpened = true;
        boolean isTravelDV = false;
        try {
            CULegacyTravelService cuLegacyTravelService = SpringContext.getBean(CULegacyTravelService.class);
            String tripID = cuLegacyTravelService.getLegacyTripID(kualiDocumentFormBase.getDocId());
            if(isTravelDV = StringUtils.isNotEmpty(tripID)) { // This means the DV is a Travel DV
                tripReOpened &= cuLegacyTravelService.reopenLegacyTrip(kualiDocumentFormBase.getDocId());
                System.out.println("Trip successfully reopened : "+tripReOpened);
            } else {
                LOG.info("DV is not a travel DV");
            }
        } catch (Exception ex) {
            LOG.info("Exception occurred while trying to cancel a trip.");
            ex.printStackTrace();
            tripReOpened=false;
        }

        if(!isTravelDV || (isTravelDV && tripReOpened)) {
            doProcessingAfterPost( kualiDocumentFormBase, request );
            getDocumentService().cancelDocument(kualiDocumentFormBase.getDocument(), kualiDocumentFormBase.getAnnotation());
    
            return returnToSender(request, mapping, kualiDocumentFormBase);
        } else {
            // TODO add message to DV indicating why doc was not canceled.
            return mapping.findForward(RiceConstants.MAPPING_BASIC);
        }
    }
    
    @Override
    public ActionForward refresh(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        CuDisbursementVoucherForm dvForm = (CuDisbursementVoucherForm) form;

        ActionForward actionAfterPayeeLookup = this.refreshAfterPayeeSelection(mapping, dvForm, request);
        if (actionAfterPayeeLookup != null) {
            return actionAfterPayeeLookup;
        }

        return super.refresh(mapping, form, request, response);
    }
    
    protected ActionForward refreshAfterPayeeSelection(ActionMapping mapping, CuDisbursementVoucherForm dvForm, HttpServletRequest request) {
        String refreshCaller = dvForm.getRefreshCaller();
        
        CuDisbursementVoucherDocument document = (CuDisbursementVoucherDocument) dvForm.getDocument();

        boolean isPayeeLookupable = KFSConstants.KUALI_DISBURSEMENT_PAYEE_LOOKUPABLE_IMPL.equals(refreshCaller);
        boolean isAddressLookupable = KFSConstants.KUALI_VENDOR_ADDRESS_LOOKUPABLE_IMPL.equals(refreshCaller);

        // if a cancel occurred on address lookup we need to reset the payee id and type, rest of fields will still have correct information
        if (refreshCaller == null && hasFullEdit(document)) {
            dvForm.setPayeeIdNumber(dvForm.getTempPayeeIdNumber());
            dvForm.setHasMultipleAddresses(false);
            document.getDvPayeeDetail().setDisbVchrPayeeIdNumber(dvForm.getTempPayeeIdNumber());
            document.getDvPayeeDetail().setDisbursementVoucherPayeeTypeCode(dvForm.getOldPayeeType());
            
            return null;
        }
        
        // do not execute the further refreshing logic if the refresh caller is not a lookupable
        if (!isPayeeLookupable && !isAddressLookupable) {
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
            dvForm.setOldPayeeType(CuDisbursementVoucherConstants.DV_PAYEE_TYPE_EMPLOYEE);

        }
        else {
            LOG.error("Exception while attempting to retrieve universal user by universal user id " + payeeIdNumber);
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
        final TransactionalDocumentPresentationController documentPresentationController = (TransactionalDocumentPresentationController)getDocumentHelperService().getDocumentPresentationController(document);
        final TransactionalDocumentAuthorizer documentAuthorizer = (TransactionalDocumentAuthorizer)getDocumentHelperService().getDocumentAuthorizer(document);
        Set<String> documentActions =  documentPresentationController.getDocumentActions(document);
        documentActions = documentAuthorizer.getDocumentActions(document, user, documentActions);
        if (getDataDictionaryService().getDataDictionary().getDocumentEntry(document.getClass().getName()).getUsePessimisticLocking()) {
            documentActions = getPessimisticLockService().getDocumentActions(document, user, documentActions);
        }
        
        Set<String> editModes = documentPresentationController.getEditModes(document);
        editModes = documentAuthorizer.getEditModes(document, user, editModes);
        
        return documentActions.contains(KRADConstants.KUALI_ACTION_CAN_EDIT) && editModes.contains("fullEntry");
    }

    protected ActionForward renderVendorAddressSelection(ActionMapping mapping, HttpServletRequest request, DisbursementVoucherForm dvForm) {
        Properties props = new Properties();

        props.put(KRADConstants.SUPPRESS_ACTIONS, Boolean.toString(true));
        // upgrade-277 try to resolve payeelookup return exception 
        props.put(KRADConstants.BUSINESS_OBJECT_CLASS_ATTRIBUTE, CuVendorAddress.class.getName());
        props.put(KRADConstants.LOOKUP_ANCHOR, KRADConstants.ANCHOR_TOP_OF_FORM);
        props.put(KRADConstants.LOOKED_UP_COLLECTION_NAME, KFSPropertyConstants.VENDOR_ADDRESSES);

        String conversionPatttern = "{0}" + KFSConstants.FIELD_CONVERSION_PAIR_SEPERATOR + "{0}";
        StringBuilder filedConversion = new StringBuilder();
        filedConversion.append(MessageFormat.format(conversionPatttern, KFSPropertyConstants.VENDOR_ADDRESS_GENERATED_ID)).append(KFSConstants.FIELD_CONVERSIONS_SEPERATOR);
        filedConversion.append(MessageFormat.format(conversionPatttern, KFSPropertyConstants.VENDOR_HEADER_GENERATED_ID)).append(KFSConstants.FIELD_CONVERSIONS_SEPERATOR);
        filedConversion.append(MessageFormat.format(conversionPatttern, KFSPropertyConstants.VENDOR_DETAIL_ASSIGNED_ID));
        props.put(KRADConstants.CONVERSION_FIELDS_PARAMETER, filedConversion);

        props.put(KFSPropertyConstants.VENDOR_HEADER_GENERATED_ID, dvForm.getVendorHeaderGeneratedIdentifier());
        props.put(KFSPropertyConstants.VENDOR_DETAIL_ASSIGNED_ID, dvForm.getVendorDetailAssignedIdentifier());
        props.put(KFSPropertyConstants.ACTIVE, KFSConstants.ACTIVE_INDICATOR);

        props.put(KRADConstants.RETURN_LOCATION_PARAMETER, this.getReturnLocation(request, mapping));
        props.put(KRADConstants.BACK_LOCATION, this.getReturnLocation(request, mapping));

        props.put(KRADConstants.LOOKUP_AUTO_SEARCH, "Yes");
        props.put(KRADConstants.DISPATCH_REQUEST_PARAMETER, KFSConstants.SEARCH_METHOD);

        props.put(KRADConstants.DOC_FORM_KEY, GlobalVariables.getUserSession().addObjectWithGeneratedKey(dvForm));
        props.put(KRADConstants.DOC_NUM, dvForm.getDocument().getDocumentNumber());

        // TODO: how should this forward be handled
        String url = UrlFactory.parameterizeUrl(getApplicationBaseUrl() + "/kr/" + KRADConstants.LOOKUP_ACTION, props);

        dvForm.registerEditableProperty("methodToCall");

        return new ActionForward(url, true);
    }

}
