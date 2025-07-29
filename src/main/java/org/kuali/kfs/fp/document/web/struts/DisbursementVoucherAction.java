/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.fp.document.web.struts;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.fp.FPKeyConstants;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherNonEmployeeExpense;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherNonEmployeeTravel;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPreConferenceRegistrant;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants.TabByReasonCode;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.service.DisbursementVoucherCoverSheetService;
import org.kuali.kfs.fp.document.service.DisbursementVoucherPayeeService;
import org.kuali.kfs.fp.document.service.DisbursementVoucherTaxService;
import org.kuali.kfs.fp.document.service.DisbursementVoucherTravelService;
import org.kuali.kfs.fp.document.service.DisbursementVoucherValidationService;
import org.kuali.kfs.kew.api.document.DocumentStatus;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.document.authorization.TransactionalDocumentAuthorizer;
import org.kuali.kfs.kns.document.authorization.TransactionalDocumentPresentationController;
import org.kuali.kfs.kns.service.DictionaryValidationService;
import org.kuali.kfs.kns.util.WebUtils;
import org.kuali.kfs.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.kfs.module.ar.businessobject.Customer;
import org.kuali.kfs.module.ar.businessobject.CustomerAddress;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.PaymentMethodAdditionalDocumentData;
import org.kuali.kfs.sys.batch.service.PaymentSourceExtractionService;
import org.kuali.kfs.sys.businessobject.PaymentMethod;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.service.PaymentSourceHelperService;
import org.kuali.kfs.sys.service.BankService;
import org.kuali.kfs.sys.util.DuplicatePaymentCheckUtils;
import org.kuali.kfs.sys.web.struts.KualiAccountingDocumentActionBase;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class handles Actions for the DisbursementVoucher.
 */
public class DisbursementVoucherAction extends KualiAccountingDocumentActionBase {

    private static final Logger LOG = LogManager.getLogger();
    private static final String PAYMENT_METHOD_PROPERTY_ADDITIONAL_DISBURSEMENT_VOUCHER_DATA_CODE =
            "additionalDisbursementVoucherDataCode";
    private static final String UPDATE_BANK_BASED_ON_PAYMENT_METHOD = "updateBankBasedOnPaymentMethod";

    private static final String DV_ADHOC_NODE = "AdHoc"; // ==== CU Customization ====

    protected DisbursementVoucherPayeeService disbursementVoucherPayeeService;
    protected DisbursementVoucherValidationService disbursementVoucherValidationService;
    private BankService bankService;
    private PaymentSourceHelperService paymentSourceHelperService;

    @Override
    protected void loadDocument(final KualiDocumentFormBase kualiDocumentFormBase) {
        super.loadDocument(kualiDocumentFormBase);

        final DisbursementVoucherForm dvForm = (DisbursementVoucherForm) kualiDocumentFormBase;
        final DisbursementVoucherDocument dvDoc = (DisbursementVoucherDocument) dvForm.getDocument();

        // do not execute the further refreshing logic if a payee is not selected
        final String payeeIdNumber = dvDoc.getDvPayeeDetail().getDisbVchrPayeeIdNumber();
        // KFSCNTRB-1735: no need to check for identity and issue a message per KFSMI-8935 if there's no payeeId and the
        // document is saved. On other statuses (e.g. enroute) throw exception if there's no payee
        // ==== CU Customization: Updated condition to prevent errors when opening no-payee, non-SAVED DV docs that are at the initial AdHoc node. ====
        if (payeeIdNumber != null && !payeeIdNumber.isEmpty()
            || !dvDoc.getDocumentHeader().getWorkflowDocument().checkStatus(DocumentStatus.SAVED)
                && !dvDoc.getDocumentHeader().getWorkflowDocument().getCurrentNodeNames().contains(DV_ADHOC_NODE)) {
            final Person person = getPersonService().getPersonByEmployeeId(payeeIdNumber);

            //KFSMI-8935: When an employee is inactive, the Payment Type field on DV documents should display the
            // message "Is this payee an employee" = No
            if (person != null && person.isActive()) {
                dvDoc.getDvPayeeDetail().setDisbVchrPayeeEmployeeCode(true);
            } else {
                dvDoc.getDvPayeeDetail().setDisbVchrPayeeEmployeeCode(false);
            }
        }
        
        if (dvDoc.getDocumentHeader().getWorkflowDocument().checkStatus(DocumentStatus.SAVED) ||
        		dvDoc.getDocumentHeader().getWorkflowDocument().checkStatus(DocumentStatus.ENROUTE)) {
        		checkForDuplicatePayments(dvDoc);		
        }
    }
    
    @Override
    public ActionForward save(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final ActionForward actionForward = super.save(mapping, form, request, response);

        final DisbursementVoucherForm dvForm = (DisbursementVoucherForm) form;
        final DisbursementVoucherDocument dvDoc = (DisbursementVoucherDocument) dvForm.getDocument();

        checkForDuplicatePayments(dvDoc);

        return actionForward;
    }

    protected void checkForDuplicatePayments(final DisbursementVoucherDocument dvDoc) {
        final Map<String, String> duplicateMessages = new HashMap<>();
        duplicateMessages.putAll(getDisbursementVoucherValidationService().checkForDuplicatePaymentRequests(dvDoc, false));
        duplicateMessages.putAll(getDisbursementVoucherValidationService().checkForDuplicateDisbursementVouchers(dvDoc, false));

        if (duplicateMessages.size() > 0) {
            final String[] splitMessage = DuplicatePaymentCheckUtils.buildQuestionText(duplicateMessages).split(DuplicatePaymentCheckUtils.ESCAPED_NEWLINE);
            Arrays.stream(splitMessage).forEach(message -> GlobalVariables.getMessageMap().putWarning(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_CUSTOM, message));
        }
    }

    @Override
    public ActionForward execute(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final DisbursementVoucherForm dvForm = (DisbursementVoucherForm) form;
        populatePaymentMethodCodesRequiringAdditionalDvData(dvForm);

        final ActionForward dest;
        if (documentIsAvailable(dvForm) && shouldUpdateBank(dvForm)) {
            dest = dispatchMethod(mapping, form, request, response, UPDATE_BANK_BASED_ON_PAYMENT_METHOD);
        } else {
            dest = super.execute(mapping, form, request, response);
        }

        final DisbursementVoucherDocument dvDoc = (DisbursementVoucherDocument) dvForm.getDocument();

        if (dvDoc != null) {
            final DisbursementVoucherNonEmployeeTravel dvNet = dvDoc.getDvNonEmployeeTravel();
            if (dvNet != null) {
                // clear values derived from travelMileageAmount if that amount has been (manually) cleared
                final Integer amount = dvNet.getDvPersonalCarMileageAmount();
                if (amount == null || amount == 0) {
                    clearTravelMileageAmount(dvNet);
                }

                // clear values derived from perDiemRate if that amount has been (manually) cleared
                final KualiDecimal rate = dvNet.getDisbVchrPerdiemRate();
                if (rate == null || rate.isZero()) {
                    clearTravelPerDiem(dvNet);
                }
            }

            dvDoc.setAchSignUpStatusFlag(getDisbursementVoucherPayeeService().isPayeeSignedUpForACH(dvDoc.getDvPayeeDetail()));
            dvForm.setOriginalPaymentMethodCode(Objects.requireNonNullElse(dvDoc.getPaymentMethodCode(), ""));
        }

        // set wire charge message in form
        dvForm.setWireChargeMessage(getPaymentSourceHelperService().retrieveWireChargeMessage());

        return dest;
    }
    
    private void populatePaymentMethodCodesRequiringAdditionalDvData(final DisbursementVoucherForm dvForm) {
        if (dvForm.getPaymentMethodCodesRequiringAdditionalData() != null) {
            return;
        }
        final Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put(KFSPropertyConstants.ACTIVE, true);
        fieldValues.put(
                PAYMENT_METHOD_PROPERTY_ADDITIONAL_DISBURSEMENT_VOUCHER_DATA_CODE,
                PaymentMethodAdditionalDocumentData.REQUIRED.getCode());
        final Set<String> paymentMethodsRequiringAdditionalData = getBusinessObjectService()
                .findMatching(PaymentMethod.class, fieldValues)
                .stream()
                .map(PaymentMethod::getPaymentMethodCode)
                .collect(Collectors.toSet());
        dvForm.setPaymentMethodCodesRequiringAdditionalData(paymentMethodsRequiringAdditionalData);
        LOG.debug("Payment methods requiring additional data: {}", paymentMethodsRequiringAdditionalData);
    }
    
    private static boolean documentIsAvailable(final DisbursementVoucherForm dvForm) {
        return dvForm.getDocument() != null && StringUtils.isNotBlank(dvForm.getDocument().getDocumentNumber());
    }

    private static boolean shouldUpdateBank(final DisbursementVoucherForm dvForm) {
        final DisbursementVoucherDocument dvDoc = (DisbursementVoucherDocument) dvForm.getDocument();
        return dvDoc != null
               && StringUtils.isNotBlank(dvDoc.getPaymentMethodCode())
               && !dvDoc.getPaymentMethodCode().equals(dvForm.getOriginalPaymentMethodCode());
    }

    public ActionForward updateBankBasedOnPaymentMethod(
            final ActionMapping mapping,
            final ActionForm form,
            final HttpServletRequest request,
            final HttpServletResponse response) {
        final DisbursementVoucherForm dvForm = (DisbursementVoucherForm) form;
        final DisbursementVoucherDocument dvDoc = dvForm.getDisbursementVoucherDocument();
        dvDoc.updateBankBasedOnPaymentMethodCode();
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    @Override
    public ActionForward approve(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, 
            final HttpServletResponse response) throws Exception {
        final DisbursementVoucherForm dvForm = (DisbursementVoucherForm) form;
        SpringContext.getBean(DisbursementVoucherPayeeService.class).checkPayeeAddressForChanges((DisbursementVoucherDocument) dvForm.getDocument());

        return super.approve(mapping, form, request, response);
    }

    /**
     * Do initialization for a new disbursement voucher
     */
    @Override
    protected void createDocument(final KualiDocumentFormBase kualiDocumentFormBase) {
        super.createDocument(kualiDocumentFormBase);
        ((DisbursementVoucherDocument) kualiDocumentFormBase.getDocument()).initiateDocument();
    }

    /**
     * Calls service to generate the disbursement voucher cover sheet as a pdf.
     */
    public ActionForward printDisbursementVoucherCoverSheet(
            final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final DisbursementVoucherForm dvForm = (DisbursementVoucherForm) form;

        final DisbursementVoucherDocument document = (DisbursementVoucherDocument) SpringContext.getBean(
                DocumentService.class).getByDocumentHeaderId(
                request.getParameter(KFSPropertyConstants.DOCUMENT_NUMBER));
        
        // set document back into form to prevent "java.lang.IllegalArgumentException: documentId was null or blank"
        // error when checking permissions since we are bypassing form submit and just linking directly to the action
        dvForm.setDocument(document);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final DisbursementVoucherCoverSheetService coverSheetService = SpringContext.getBean(DisbursementVoucherCoverSheetService.class);

        coverSheetService.generateDisbursementVoucherCoverSheet(document, baos);
        final String fileName = document.getDocumentNumber() + "_cover_sheet.pdf";
        WebUtils.saveMimeOutputStreamAsFile(response, "application/pdf", baos, fileName);

        return null;
    }


    /**
     * Calculates the travel per diem amount.
     */
    public ActionForward calculateTravelPerDiem(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final DisbursementVoucherForm dvForm = (DisbursementVoucherForm) form;

        try {
            // call service to calculate per diem
            final DisbursementVoucherDocument dvDocument = (DisbursementVoucherDocument) dvForm.getDocument();
            final KualiDecimal perDiemAmount = SpringContext.getBean(DisbursementVoucherTravelService.class)
                    .calculatePerDiemAmount(dvDocument.getDvNonEmployeeTravel().getDvPerdiemStartDttmStamp(),
                            dvDocument.getDvNonEmployeeTravel().getDvPerdiemEndDttmStamp(),
                            dvDocument.getDvNonEmployeeTravel().getDisbVchrPerdiemRate());

            dvDocument.getDvNonEmployeeTravel().setDisbVchrPerdiemCalculatedAmt(perDiemAmount);
            dvDocument.getDvNonEmployeeTravel().setDisbVchrPerdiemActualAmount(perDiemAmount);
        } catch (final RuntimeException e) {
            String errorMessage = e.getMessage();

            if (StringUtils.isBlank(errorMessage)) {
                errorMessage = "The per diem amount could not be calculated.  Please ensure all required per diem " +
                        "fields are filled in before attempting to calculate the per diem amount.";
            }

            LOG.error("Error in calculating travel per diem: {}", errorMessage);
            GlobalVariables.getMessageMap().putError("DVNonEmployeeTravelErrors",
                    KFSKeyConstants.ERROR_CUSTOM, errorMessage);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Clears the travel per diem amount
     */
    public ActionForward clearTravelPerDiem(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final DisbursementVoucherForm dvForm = (DisbursementVoucherForm) form;
        final DisbursementVoucherDocument dvDocument = (DisbursementVoucherDocument) dvForm.getDocument();

        final DisbursementVoucherNonEmployeeTravel dvNet = dvDocument.getDvNonEmployeeTravel();
        if (dvNet != null) {
            clearTravelPerDiem(dvNet);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * clear travel perdiem amounts
     */
    protected void clearTravelPerDiem(final DisbursementVoucherNonEmployeeTravel dvNet) {
        dvNet.setDisbVchrPerdiemCalculatedAmt(null);
        dvNet.setDisbVchrPerdiemActualAmount(null);
    }

    /**
     * Calculates the travel mileage amount.
     */
    public ActionForward calculateTravelMileageAmount(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final DisbursementVoucherForm dvForm = (DisbursementVoucherForm) form;
        final DisbursementVoucherDocument dvDocument = (DisbursementVoucherDocument) dvForm.getDocument();

        if (dvDocument.getDvNonEmployeeTravel().getDvPersonalCarMileageAmount() == null) {
            LOG.error("Total Mileage must be given");
            GlobalVariables.getMessageMap().putError("DVNonEmployeeTravelErrors", KFSKeyConstants.ERROR_REQUIRED,
                    "Total Mileage");
        }

        if (dvDocument.getDvNonEmployeeTravel().getDvPerdiemStartDttmStamp() == null) {
            LOG.error("Travel Start Date must be given");
            GlobalVariables.getMessageMap().putError("DVNonEmployeeTravelErrors", KFSKeyConstants.ERROR_REQUIRED,
                    "Travel Start Date");
        }

        if (!GlobalVariables.getMessageMap().hasErrors()) {
            // call service to calculate mileage amount
            final KualiDecimal mileageAmount = SpringContext.getBean(DisbursementVoucherTravelService.class)
                    .calculateMileageAmount(dvDocument.getDvNonEmployeeTravel().getDvPersonalCarMileageAmount(),
                            dvDocument.getDvNonEmployeeTravel().getDvPerdiemStartDttmStamp());

            dvDocument.getDvNonEmployeeTravel().setDisbVchrMileageCalculatedAmt(mileageAmount);
            dvDocument.getDvNonEmployeeTravel().setDisbVchrPersonalCarAmount(mileageAmount);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Clears the travel mileage amount
     */
    public ActionForward clearTravelMileageAmount(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final DisbursementVoucherForm dvForm = (DisbursementVoucherForm) form;
        final DisbursementVoucherDocument dvDocument = (DisbursementVoucherDocument) dvForm.getDocument();

        final DisbursementVoucherNonEmployeeTravel dvNet = dvDocument.getDvNonEmployeeTravel();
        if (dvNet != null) {
            clearTravelMileageAmount(dvNet);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * reset the travel mileage amount as null
     */
    protected void clearTravelMileageAmount(final DisbursementVoucherNonEmployeeTravel dvNet) {
        dvNet.setDisbVchrMileageCalculatedAmt(null);
        dvNet.setDisbVchrPersonalCarAmount(null);
    }

    /**
     * Adds a new employee travel expense line.
     */
    public ActionForward addNonEmployeeExpenseLine(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final DisbursementVoucherForm dvForm = (DisbursementVoucherForm) form;
        final DisbursementVoucherDocument dvDocument = (DisbursementVoucherDocument) dvForm.getDocument();

        final DisbursementVoucherNonEmployeeExpense newExpenseLine = dvForm.getNewNonEmployeeExpenseLine();

        // validate line
        GlobalVariables.getMessageMap().addToErrorPath(KFSPropertyConstants.NEW_NONEMPLOYEE_EXPENSE_LINE);
        SpringContext.getBean(DictionaryValidationService.class).validateBusinessObject(newExpenseLine);

        // Ensure all fields are filled in before attempting to add a new expense line
        if (StringUtils.isBlank(newExpenseLine.getDisbVchrPrePaidExpenseCode())) {
            GlobalVariables.getMessageMap().putError(KFSPropertyConstants.DISB_VCHR_EXPENSE_CODE,
                    FPKeyConstants.ERROR_DV_EXPENSE_CODE);
        }
        if (StringUtils.isBlank(newExpenseLine.getDisbVchrPrePaidExpenseCompanyName())) {
            GlobalVariables.getMessageMap().putError(KFSPropertyConstants.DISB_VCHR_EXPENSE_COMPANY_NAME,
                    FPKeyConstants.ERROR_DV_EXPENSE_COMPANY_NAME);
        }
        if (ObjectUtils.isNull(newExpenseLine.getDisbVchrExpenseAmount())) {
            GlobalVariables.getMessageMap().putError(KFSPropertyConstants.DISB_VCHR_EXPENSE_AMOUNT,
                    FPKeyConstants.ERROR_DV_EXPENSE_AMOUNT);
        }

        GlobalVariables.getMessageMap().removeFromErrorPath(KFSPropertyConstants.NEW_NONEMPLOYEE_EXPENSE_LINE);

        //KFSMI-9523
        //no errors so go ahead and add the record to the list.  Need to set the document number
        //and recalculate the next line number for the new record that is created after adding the current one.
        if (!GlobalVariables.getMessageMap().hasErrors()) {
            newExpenseLine.setDocumentNumber(dvDocument.getDocumentNumber());
            dvDocument.getDvNonEmployeeTravel().addDvNonEmployeeExpenseLine(newExpenseLine);
            final DisbursementVoucherNonEmployeeExpense newNewNonEmployeeExpenseLine = new DisbursementVoucherNonEmployeeExpense();
            newNewNonEmployeeExpenseLine.setFinancialDocumentLineNumber(newExpenseLine.getFinancialDocumentLineNumber() + 1);
            dvForm.setNewNonEmployeeExpenseLine(newNewNonEmployeeExpenseLine);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Adds a new employee pre paid travel expense line.
     */
    public ActionForward addPrePaidNonEmployeeExpenseLine(
            final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final DisbursementVoucherForm dvForm = (DisbursementVoucherForm) form;
        final DisbursementVoucherDocument dvDocument = (DisbursementVoucherDocument) dvForm.getDocument();

        final DisbursementVoucherNonEmployeeExpense newExpenseLine = dvForm.getNewPrePaidNonEmployeeExpenseLine();

        // validate line
        GlobalVariables.getMessageMap().addToErrorPath(KFSPropertyConstants.NEW_PREPAID_EXPENSE_LINE);
        SpringContext.getBean(DictionaryValidationService.class).validateBusinessObject(newExpenseLine);

        // Ensure all fields are filled in before attempting to add a new expense line
        if (StringUtils.isBlank(newExpenseLine.getDisbVchrPrePaidExpenseCode())) {
            GlobalVariables.getMessageMap().putError(KFSPropertyConstants.DISB_VCHR_PRE_PAID_EXPENSE_CODE,
                    FPKeyConstants.ERROR_DV_PREPAID_EXPENSE_CODE);
        }
        if (StringUtils.isBlank(newExpenseLine.getDisbVchrPrePaidExpenseCompanyName())) {
            GlobalVariables.getMessageMap().putError(KFSPropertyConstants.DISB_VCHR_PRE_PAID_EXPENSE_COMPANY_NAME,
                    FPKeyConstants.ERROR_DV_PREPAID_EXPENSE_COMPANY_NAME);
        }
        if (ObjectUtils.isNull(newExpenseLine.getDisbVchrExpenseAmount())) {
            GlobalVariables.getMessageMap().putError(KFSPropertyConstants.DISB_VCHR_EXPENSE_AMOUNT,
                    FPKeyConstants.ERROR_DV_PREPAID_EXPENSE_AMOUNT);
        }
        GlobalVariables.getMessageMap().removeFromErrorPath(KFSPropertyConstants.NEW_PREPAID_EXPENSE_LINE);

        //KFSMI-9523
        //no errors so go ahead and add the record to the list.  Need to set the document number
        //and recalculate the next line number for the new record that is created after adding the current one.
        if (!GlobalVariables.getMessageMap().hasErrors()) {
            newExpenseLine.setDocumentNumber(dvDocument.getDocumentNumber());
            dvDocument.getDvNonEmployeeTravel().addDvPrePaidEmployeeExpenseLine(newExpenseLine);
            final DisbursementVoucherNonEmployeeExpense newNewNonEmployeeExpenseLine = new DisbursementVoucherNonEmployeeExpense();
            newNewNonEmployeeExpenseLine.setFinancialDocumentLineNumber(newExpenseLine.getFinancialDocumentLineNumber() + 1);
            dvForm.setNewPrePaidNonEmployeeExpenseLine(newNewNonEmployeeExpenseLine);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Deletes a non employee travel expense line.
     */
    public ActionForward deleteNonEmployeeExpenseLine(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final DisbursementVoucherForm dvForm = (DisbursementVoucherForm) form;
        final DisbursementVoucherDocument dvDocument = (DisbursementVoucherDocument) dvForm.getDocument();

        final int deleteIndex = getLineToDelete(request);
        dvDocument.getDvNonEmployeeTravel().getDvNonEmployeeExpenses().remove(deleteIndex);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Deletes a pre paid travel expense line.
     */
    public ActionForward deletePrePaidEmployeeExpenseLine(
            final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final DisbursementVoucherForm dvForm = (DisbursementVoucherForm) form;
        final DisbursementVoucherDocument dvDocument = (DisbursementVoucherDocument) dvForm.getDocument();

        final int deleteIndex = getLineToDelete(request);
        dvDocument.getDvNonEmployeeTravel().getDvPrePaidEmployeeExpenses().remove(deleteIndex);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Adds a new pre conference registrant line.
     */
    public ActionForward addPreConfRegistrantLine(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final DisbursementVoucherForm dvForm = (DisbursementVoucherForm) form;
        final DisbursementVoucherDocument dvDocument = (DisbursementVoucherDocument) dvForm.getDocument();

        final DisbursementVoucherPreConferenceRegistrant newRegistrantLine = dvForm.getNewPreConferenceRegistrantLine();

        // validate line
        GlobalVariables.getMessageMap().addToErrorPath(KFSPropertyConstants.NEW_PRECONF_REGISTRANT_LINE);
        SpringContext.getBean(DictionaryValidationService.class).validateBusinessObject(newRegistrantLine);
        GlobalVariables.getMessageMap().removeFromErrorPath(KFSPropertyConstants.NEW_PRECONF_REGISTRANT_LINE);

        if (!GlobalVariables.getMessageMap().hasErrors()) {
            dvDocument.addDvPrePaidRegistrantLine(newRegistrantLine);
            dvForm.setNewPreConferenceRegistrantLine(new DisbursementVoucherPreConferenceRegistrant());
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Deletes a pre conference registrant line.
     */
    public ActionForward deletePreConfRegistrantLine(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final DisbursementVoucherForm dvForm = (DisbursementVoucherForm) form;
        final DisbursementVoucherDocument dvDocument = (DisbursementVoucherDocument) dvForm.getDocument();

        final int deleteIndex = getLineToDelete(request);
        dvDocument.getDvPreConferenceDetail().getDvPreConferenceRegistrants().remove(deleteIndex);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Calls service to generate tax accounting lines and updates nonresident tax line string in action form.
     */
    public ActionForward generateNonresidentTaxLines(
            final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final DisbursementVoucherForm dvForm = (DisbursementVoucherForm) form;
        final DisbursementVoucherDocument document = (DisbursementVoucherDocument) dvForm.getDocument();

        final DisbursementVoucherTaxService taxService = SpringContext.getBean(DisbursementVoucherTaxService.class);

        /* call service to generate new tax lines */
        GlobalVariables.getMessageMap().addToErrorPath("document");
        taxService.processNonresidentTax(document);
        GlobalVariables.getMessageMap().removeFromErrorPath("document");

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Calls service to clear tax accounting lines and updates nonresident tax line string in action form.
     */
    public ActionForward clearNonresidentTaxLines(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final DisbursementVoucherForm dvForm = (DisbursementVoucherForm) form;
        final DisbursementVoucherDocument document = (DisbursementVoucherDocument) dvForm.getDocument();

        final DisbursementVoucherTaxService taxService = SpringContext.getBean(DisbursementVoucherTaxService.class);

        /* call service to clear previous lines */
        taxService.clearNonresidentTaxLines(document);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Calls service to clear tax info.
     */
    public ActionForward clearNonresidentTaxInfo(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final DisbursementVoucherForm dvForm = (DisbursementVoucherForm) form;
        final DisbursementVoucherDocument document = (DisbursementVoucherDocument) dvForm.getDocument();

        final DisbursementVoucherTaxService taxService = SpringContext.getBean(DisbursementVoucherTaxService.class);

        /* call service to clear previous lines */
        taxService.clearNonresidentTaxInfo(document);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    @Override
    public ActionForward refresh(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final DisbursementVoucherForm dvForm = (DisbursementVoucherForm) form;

        final ActionForward actionAfterPayeeLookup = refreshAfterPayeeSelection(mapping, dvForm, request);
        if (actionAfterPayeeLookup != null) {
            return actionAfterPayeeLookup;
        }

        return super.refresh(mapping, form, request, response);
    }

    // do refresh after a payee is selected
    protected ActionForward refreshAfterPayeeSelection(
            final ActionMapping mapping, final DisbursementVoucherForm dvForm,
            final HttpServletRequest request) {
        final String refreshCaller = dvForm.getRefreshCaller();

        final DisbursementVoucherDocument document = (DisbursementVoucherDocument) dvForm.getDocument();

        final boolean isPayeeLookupable = getIsPayeeLookupable(refreshCaller);
        final boolean isAddressLookupable = KFSConstants.KUALI_VENDOR_ADDRESS_LOOKUPABLE_IMPL.equals(refreshCaller);
        final boolean isKualiLookupable = KFSConstants.KUALI_LOOKUPABLE_IMPL.equals(refreshCaller);

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
        final String payeeIdNumber = document.getDvPayeeDetail().getDisbVchrPayeeIdNumber();
        if (payeeIdNumber == null) {
            return null;
        }

        dvForm.setPayeeIdNumber(payeeIdNumber);
        dvForm.setHasMultipleAddresses(false);

        // determine whether the selected vendor has multiple addresses. If so, redirect to the address selection screen
        if (isPayeeLookupable && dvForm.isVendor()) {
            VendorDetail refreshVendorDetail = new VendorDetail();
            refreshVendorDetail.setVendorNumber(payeeIdNumber);
            refreshVendorDetail = (VendorDetail) SpringContext.getBean(BusinessObjectService.class).retrieve(refreshVendorDetail);

            VendorAddress defaultVendorAddress = null;
            if (refreshVendorDetail != null) {
                final List<VendorAddress> vendorAddresses = refreshVendorDetail.getVendorAddresses();
                final boolean hasMultipleAddresses = vendorAddresses != null && vendorAddresses.size() > 1;
                dvForm.setHasMultipleAddresses(hasMultipleAddresses);

                if (vendorAddresses != null) {
                    defaultVendorAddress = vendorAddresses.get(0);
                }
            }

            if (dvForm.hasMultipleAddresses()) {
                return renderVendorAddressSelection(mapping, request, dvForm);
            } else if (defaultVendorAddress != null) {
                setupPayeeAsVendor(dvForm, payeeIdNumber, defaultVendorAddress.getVendorAddressGeneratedIdentifier().toString());
            }

            return null;
        }

        final String payeeAddressIdentifier = request.getParameter(KFSPropertyConstants.VENDOR_ADDRESS_GENERATED_ID);
        if (isAddressLookupable && StringUtils.isNotBlank(payeeAddressIdentifier)) {
            setupPayeeAsVendor(dvForm, payeeIdNumber, payeeAddressIdentifier);
        }

        if (isPayeeLookupable && dvForm.isEmployee()) {
            setupPayeeAsEmployee(dvForm, payeeIdNumber);
        }

        // check for multiple custom addresses
        if (isPayeeLookupable && dvForm.isCustomer()) {
            final Customer customer = SpringContext.getBean(BusinessObjectService.class)
                    .findBySinglePrimaryKey(Customer.class, payeeIdNumber);

            CustomerAddress defaultCustomerAddress = null;
            if (customer != null) {
                defaultCustomerAddress = customer.getPrimaryAddress();

                final Map<String, String> addressSearch = new HashMap<>();
                addressSearch.put(KFSPropertyConstants.CUSTOMER_NUMBER, payeeIdNumber);

                final List<CustomerAddress> customerAddresses =
                        (List<CustomerAddress>) getBusinessObjectService().findMatching(CustomerAddress.class,
                                addressSearch
                        );
                
                if (customerAddresses != null && !customerAddresses.isEmpty()) {
                    if (customerAddresses.size() > 1) {
                        dvForm.setHasMultipleAddresses(true);
                    } else if (defaultCustomerAddress == null) {
                        defaultCustomerAddress = customerAddresses.get(0);
                    }
                }
            }

            if (dvForm.hasMultipleAddresses()) {
                return renderCustomerAddressSelection(mapping, request, dvForm);
            } else if (defaultCustomerAddress != null) {
                setupPayeeAsCustomer(dvForm, payeeIdNumber, defaultCustomerAddress.getCustomerAddressIdentifier().toString());
            }
        }

        final String customerAddressIdentifier = request.getParameter(KFSPropertyConstants.CUSTOMER_ADDRESS_IDENTIFIER);
        if (isKualiLookupable && StringUtils.isNotBlank(customerAddressIdentifier)) {
            setupPayeeAsCustomer(dvForm, payeeIdNumber, customerAddressIdentifier);
        }

        final String paymentReasonCode = document.getDvPayeeDetail().getDisbVchrPaymentReasonCode();
        addPaymentCodeWarningMessage(dvForm, paymentReasonCode);

        return null;
    }

    protected boolean getIsPayeeLookupable(final String refreshCaller) {
        return KFSConstants.KUALI_DISBURSEMENT_PAYEE_LOOKUPABLE_IMPL.equals(refreshCaller);
    }

    /**
     * Determines if the current user has full edit permissions on the document, which would allow them to repopulate the payee
     *
     * @param document the document to check for full edit permissions on
     * @return true if full edit is allowed on the document, false otherwise
     */
    protected boolean hasFullEdit(final DisbursementVoucherDocument document) {
        final Person user = GlobalVariables.getUserSession().getPerson();
        final TransactionalDocumentPresentationController documentPresentationController = (TransactionalDocumentPresentationController) getDocumentHelperService()
                .getDocumentPresentationController(document);
        final TransactionalDocumentAuthorizer documentAuthorizer = (TransactionalDocumentAuthorizer) getDocumentHelperService()
                .getDocumentAuthorizer(document);
        Set<String> documentActions = documentPresentationController.getDocumentActions(document);
        documentActions = documentAuthorizer.getDocumentActions(document, user, documentActions);

        Set<String> editModes = documentPresentationController.getEditModes(document);
        editModes = documentAuthorizer.getEditModes(document, user, editModes);

        return documentActions.contains(KRADConstants.KUALI_ACTION_CAN_EDIT) && editModes.contains("fullEntry");
    }

    /**
     * Hook into performLookup to switch the payee lookup based on the payee type selected.
     */
    @Override
    public ActionForward performLookup(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        return super.performLookup(mapping, form, request, response);
    }

    /**
     * render the vendor address lookup results if there are multiple addresses for the selected vendor
     */
    protected ActionForward renderVendorAddressSelection(
            final ActionMapping mapping, final HttpServletRequest request,
            final DisbursementVoucherForm dvForm) {
        final Map<String, String> props = new HashMap<>();

        props.put(KRADConstants.SUPPRESS_ACTIONS, Boolean.toString(true));
        props.put(KRADConstants.BUSINESS_OBJECT_CLASS_ATTRIBUTE, VendorAddress.class.getName());
        props.put(KRADConstants.LOOKUP_ANCHOR, KRADConstants.ANCHOR_TOP_OF_FORM);
        props.put(KRADConstants.LOOKED_UP_COLLECTION_NAME, KFSPropertyConstants.VENDOR_ADDRESSES);

        final String conversionPattern = "{0}" + KFSConstants.FIELD_CONVERSION_PAIR_SEPERATOR + "{0}";
        final String filedConversion = MessageFormat.format(conversionPattern,
                KFSPropertyConstants.VENDOR_ADDRESS_GENERATED_ID) +
                KFSConstants.FIELD_CONVERSIONS_SEPERATOR +
                MessageFormat.format(conversionPattern, KFSPropertyConstants.VENDOR_HEADER_GENERATED_ID) +
                KFSConstants.FIELD_CONVERSIONS_SEPERATOR +
                MessageFormat.format(conversionPattern, KFSPropertyConstants.VENDOR_DETAIL_ASSIGNED_ID);
        props.put(KRADConstants.CONVERSION_FIELDS_PARAMETER, filedConversion);

        props.put(KFSPropertyConstants.VENDOR_HEADER_GENERATED_ID, dvForm.getVendorHeaderGeneratedIdentifier());
        props.put(KFSPropertyConstants.VENDOR_DETAIL_ASSIGNED_ID, dvForm.getVendorDetailAssignedIdentifier());
        props.put(KFSPropertyConstants.ACTIVE, KFSConstants.ACTIVE_INDICATOR);

        props.put(KRADConstants.RETURN_LOCATION_PARAMETER, getReturnLocation(request, mapping));
        props.put(KRADConstants.BACK_LOCATION, getReturnLocation(request, mapping));

        props.put(KRADConstants.LOOKUP_AUTO_SEARCH, "Yes");
        props.put(KRADConstants.DISPATCH_REQUEST_PARAMETER, KFSConstants.SEARCH_METHOD);

        props.put(KRADConstants.DOC_FORM_KEY, GlobalVariables.getUserSession().addObjectWithGeneratedKey(dvForm));
        props.put(KRADConstants.DOC_NUM, dvForm.getDocument().getDocumentNumber());

        // TODO: how should this forward be handled
        final String url = UrlFactory.parameterizeUrl(getApplicationBaseUrl() + "/" + KRADConstants.LOOKUP_ACTION, props);

        dvForm.registerEditableProperty("methodToCall");

        return new ActionForward(url, true);
    }

    /**
     * setup the payee as an employee with the given id number
     */
    protected void setupPayeeAsEmployee(final DisbursementVoucherForm dvForm, final String payeeIdNumber) {
        final Person person = SpringContext.getBean(PersonService.class).getPersonByEmployeeId(payeeIdNumber);
        if (person != null) {
            ((DisbursementVoucherDocument) dvForm.getDocument()).templateEmployee(person);
            dvForm.setTempPayeeIdNumber(payeeIdNumber);
            dvForm.setOldPayeeType(KFSConstants.PaymentPayeeTypes.EMPLOYEE);

        } else {
            LOG.error("Exception while attempting to retrieve universal user by universal user id {}", payeeIdNumber);
        }
    }

    /**
     * setup the payee as a vendor with the given id number and address id
     */
    protected void setupPayeeAsVendor(final DisbursementVoucherForm dvForm, final String payeeIdNumber, final String payeeAddressIdentifier) {
        VendorDetail vendorDetail = new VendorDetail();
        vendorDetail.setVendorNumber(payeeIdNumber);
        vendorDetail = (VendorDetail) SpringContext.getBean(BusinessObjectService.class).retrieve(vendorDetail);

        VendorAddress vendorAddress = new VendorAddress();
        if (StringUtils.isNotBlank(payeeAddressIdentifier)) {
            try {
                vendorAddress.setVendorAddressGeneratedIdentifier(Integer.valueOf(payeeAddressIdentifier));
                vendorAddress = (VendorAddress) SpringContext.getBean(BusinessObjectService.class).retrieve(vendorAddress);
                dvForm.setTempPayeeIdNumber(payeeIdNumber);
                dvForm.setOldPayeeType(KFSConstants.PaymentPayeeTypes.VENDOR);

            } catch (final Exception e) {
                LOG.error(
                        "Exception while attempting to retrieve vendor address for vendor address id {}: {}",
                        payeeAddressIdentifier,
                        e
                );
            }
        }

        ((DisbursementVoucherDocument) dvForm.getDocument()).templateVendor(vendorDetail, vendorAddress);
    }

    /**
     * render the customer address lookup results if there are multiple addresses for the selected customer
     */
    protected ActionForward renderCustomerAddressSelection(
            final ActionMapping mapping, final HttpServletRequest request,
            final DisbursementVoucherForm dvForm) {
        final Map<String, String> props = new HashMap<>();

        props.put(KRADConstants.SUPPRESS_ACTIONS, Boolean.toString(true));
        props.put(KRADConstants.BUSINESS_OBJECT_CLASS_ATTRIBUTE, CustomerAddress.class.getName());
        props.put(KRADConstants.LOOKUP_ANCHOR, KRADConstants.ANCHOR_TOP_OF_FORM);
        props.put(KRADConstants.LOOKED_UP_COLLECTION_NAME, KFSPropertyConstants.VENDOR_ADDRESSES);

        final String conversionPattern = "{0}" + KFSConstants.FIELD_CONVERSION_PAIR_SEPERATOR + "{0}";
        final String filedConversion = MessageFormat.format(conversionPattern, KFSPropertyConstants.CUSTOMER_NUMBER) +
                KFSConstants.FIELD_CONVERSIONS_SEPERATOR + MessageFormat.format(conversionPattern,
                KFSPropertyConstants.CUSTOMER_ADDRESS_IDENTIFIER);
        props.put(KRADConstants.CONVERSION_FIELDS_PARAMETER, filedConversion);

        props.put(KFSPropertyConstants.CUSTOMER_NUMBER, dvForm.getPayeeIdNumber());
        props.put(KFSPropertyConstants.ACTIVE, KFSConstants.ACTIVE_INDICATOR);

        props.put(KRADConstants.RETURN_LOCATION_PARAMETER, getReturnLocation(request, mapping));
        props.put(KRADConstants.BACK_LOCATION, getReturnLocation(request, mapping));

        props.put(KRADConstants.LOOKUP_AUTO_SEARCH, "Yes");
        props.put(KRADConstants.DISPATCH_REQUEST_PARAMETER, KFSConstants.SEARCH_METHOD);

        props.put(KRADConstants.DOC_FORM_KEY, GlobalVariables.getUserSession().addObjectWithGeneratedKey(dvForm));
        props.put(KRADConstants.DOC_NUM, dvForm.getDocument().getDocumentNumber());

        // TODO: how should this forward be handled
        final String url = UrlFactory.parameterizeUrl(getApplicationBaseUrl() + "/" + KRADConstants.LOOKUP_ACTION, props);

        dvForm.registerEditableProperty("methodToCall");

        return new ActionForward(url, true);
    }

    /**
     * setup the payee as a customer with the given id number and address id
     */
    protected void setupPayeeAsCustomer(
            final DisbursementVoucherForm dvForm, final String payeeIdNumber,
            final String payeeAddressIdentifier) {
        final Customer customer = SpringContext.getBean(BusinessObjectService.class)
                .findBySinglePrimaryKey(Customer.class, payeeIdNumber);

        CustomerAddress customerAddress = null;
        if (StringUtils.isNotBlank(payeeAddressIdentifier)) {
            customerAddress = getBusinessObjectService().findByPrimaryKey(
                    CustomerAddress.class,
                    Map.of(payeeIdNumber, payeeAddressIdentifier)
            );
        }

        dvForm.setTempPayeeIdNumber(payeeIdNumber);
        dvForm.setOldPayeeType(KFSConstants.PaymentPayeeTypes.CUSTOMER);

        ((DisbursementVoucherDocument) dvForm.getDocument()).templateCustomer(customer, customerAddress);
    }

    /**
     * add warning message based on the given reason code
     */
    protected void addPaymentCodeWarningMessage(final DisbursementVoucherForm dvForm, final String paymentReasonCode) {
        // clear up the warning message and tab state carried from previous screen
        for (final String tabKey : TabByReasonCode.getAllTabKeys()) {
            dvForm.getTabStates().remove(tabKey);
        }

        for (final String propertyKey : TabByReasonCode.getAllDocumentPropertyKeys()) {
            GlobalVariables.getMessageMap().removeAllWarningMessagesForProperty(propertyKey);
        }

        final String reasonCodeProperty = KFSPropertyConstants.DOCUMENT + "." + KFSPropertyConstants.DV_PAYEE_DETAIL + "." +
                KFSPropertyConstants.DISB_VCHR_PAYMENT_REASON_CODE;
        GlobalVariables.getMessageMap().removeAllWarningMessagesForProperty(reasonCodeProperty);

        // add warning message and reset tab state as open if any
        final TabByReasonCode tab = TabByReasonCode.getTabByReasonCode(paymentReasonCode);
        if (tab != null) {
            dvForm.getTabStates().put(tab.tabKey, "OPEN");
            GlobalVariables.getMessageMap().putWarning(reasonCodeProperty, tab.messageKey);
            GlobalVariables.getMessageMap().putWarning(tab.getDocumentPropertyKey(), tab.messageKey);
        }
    } 

    /**
     * Extracts the DV as immediate payment upon user's request after it routes to FINAL.
     */
    public ActionForward extractNow(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final DisbursementVoucherForm dvForm = (DisbursementVoucherForm) form;
        final DisbursementVoucherDocument dvDocument = (DisbursementVoucherDocument) dvForm.getDocument();

        final PaymentSourceExtractionService disbursementVoucherExtractService = DisbursementVoucherDocument.getDisbursementVoucherExtractService();
        dvDocument.setImmediatePaymentIndicator(true);
        disbursementVoucherExtractService.extractSingleImmediatePayment(dvDocument);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    protected DisbursementVoucherPayeeService getDisbursementVoucherPayeeService() {
        if (disbursementVoucherPayeeService == null) {
            disbursementVoucherPayeeService = SpringContext.getBean(DisbursementVoucherPayeeService.class);
        }
        
        return disbursementVoucherPayeeService;
    }
    
    protected DisbursementVoucherValidationService getDisbursementVoucherValidationService() {
        if (disbursementVoucherValidationService == null) {
            disbursementVoucherValidationService = SpringContext.getBean(DisbursementVoucherValidationService.class);
        }

        return disbursementVoucherValidationService;
    }
    
    private BankService getBankService() {
        if (bankService == null) {
            bankService = SpringContext.getBean(BankService.class);
        }
        return bankService;
    }

    public PaymentSourceHelperService getPaymentSourceHelperService() {
        if (paymentSourceHelperService == null) {
            paymentSourceHelperService = SpringContext.getBean(PaymentSourceHelperService.class);
        }
        return paymentSourceHelperService;
    }
}
