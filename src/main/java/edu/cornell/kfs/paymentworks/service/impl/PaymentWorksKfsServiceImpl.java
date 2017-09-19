/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2014 The Kuali Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cornell.kfs.paymentworks.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.pdp.businessobject.ACHBank;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.pdp.document.PayeeACHAccountMaintainableImpl;
import org.kuali.kfs.pdp.service.AchBankService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.mail.BodyMailMessage;
import org.kuali.kfs.sys.service.EmailService;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.rice.core.api.util.type.KualiInteger;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.bo.AdHocRouteRecipient;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.maintenance.Maintainable;
import org.kuali.kfs.krad.rules.rule.event.RouteDocumentEvent;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.MaintenanceDocumentService;
import org.kuali.kfs.krad.service.SequenceAccessorService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.coa.businessobject.options.CUCheckingSavingsValuesFinder;
import edu.cornell.kfs.paymentworks.PaymentWorksConstants;
import edu.cornell.kfs.paymentworks.batch.PaymentWorksRetrieveNewVendorStep;
import edu.cornell.kfs.paymentworks.batch.PaymentWorksUploadSuppliersStep;
import edu.cornell.kfs.paymentworks.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.paymentworks.service.PaymentWorksAchService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksKfsService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksNewVendorConversionService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksUtilityService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksVendorUpdateConversionService;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksFieldChangeDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksVendorUpdatesDTO;
import edu.cornell.kfs.pdp.CUPdpConstants;
import edu.cornell.kfs.vnd.CUVendorConstants;

@Transactional
public class PaymentWorksKfsServiceImpl implements PaymentWorksKfsService {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksKfsServiceImpl.class);

    protected DocumentService documentService;
    protected EmailService emailService;
    protected AchBankService achBankService;
    protected ParameterService parameterService;
    protected VendorService vendorService;
    protected BusinessObjectService businessObjectService;
    protected MaintenanceDocumentService maintenanceDocumentService;
    protected PaymentWorksNewVendorConversionService paymentWorksNewVendorConversionService;
    protected PaymentWorksUtilityService paymentWorksUtilityService;
    protected PaymentWorksAchService paymentWorksAchService;
    protected PaymentWorksVendorUpdateConversionService paymentWorksVendorUpdateConversionService;
    protected SequenceAccessorService sequenceAccessorService;

    @Override
    public boolean routeNewVendor(PaymentWorksVendor paymentWorksVendor) {
        boolean routed = false;
        VendorDetail vendorDetail = getPaymentWorksNewVendorConversionService().createVendorDetail(paymentWorksVendor);

        try {
            MaintenanceDocument vendorMaintDoc = buildVendorMaintenanceDocument(paymentWorksVendor, vendorDetail);
            boolean valid = true;
            try {
                vendorMaintDoc.validateBusinessRules(new RouteDocumentEvent(vendorMaintDoc));
            } catch (ValidationException ve) {
                LOG.error("Failed to route Vendor document due to business rule error(s): "
                        + getPaymentWorksUtilityService().getAutoPopulatingErrorMessages(GlobalVariables.getMessageMap().getErrorMessages()), ve);
                valid = false;
            }

            if (valid) {
                documentService.routeDocument(vendorMaintDoc, "", new ArrayList());
                routed = true;
                paymentWorksVendor.setDocumentNumber(vendorMaintDoc.getDocumentNumber());
            }
        } catch (Exception e) {
            LOG.error("Failed to route Vendor document due to error(s): " + e.getMessage());
        } finally {
            GlobalVariables.getMessageMap().clearErrorMessages();
        }
        return routed;
    }

    @Override
    public boolean routeVendorEdit(PaymentWorksVendor paymentWorksVendor) {
        boolean routed = false;
        String vendorNumberList = paymentWorksVendor.getVendorNumberList();
        List<String> vendorNumbers = Arrays.asList(vendorNumberList.split("\\s*,\\s*"));
        StringBuffer documentNumbers = new StringBuffer("");

        for (String vendorNumber : vendorNumbers) {
            VendorDetail oldVendor = vendorService.getVendorDetail(vendorNumber);
            VendorDetail newVendor = (VendorDetail) ObjectUtils.deepCopy(oldVendor);

            if (!getPaymentWorksVendorUpdateConversionService().duplicateFieldsOnVendor(newVendor, paymentWorksVendor)) {
                routed = processNonDuplicateVendorDetail(paymentWorksVendor, documentNumbers, oldVendor, newVendor);
            } else {
                GlobalVariables.getMessageMap().putError("vendorDetail", KFSKeyConstants.ERROR_CUSTOM, "Duplicate vendor update detected");
                routed = false;
            }
        }

        if (documentNumbers.length() > 0) {
            paymentWorksVendor.setDocumentNumberList(documentNumbers.toString().substring(0, documentNumbers.length() - 1));
        }
        return routed;
    }

    protected boolean processNonDuplicateVendorDetail(PaymentWorksVendor paymentWorksVendor, StringBuffer documentNumbers, VendorDetail oldVendor, VendorDetail newVendor) {
        boolean routed = false;
        VendorDetail vendorDetail = getPaymentWorksVendorUpdateConversionService().createVendorDetailForEdit(newVendor, oldVendor, paymentWorksVendor);
        try {
            MaintenanceDocument vendorMaintDoc = buildVendorMaintenanceDocument(paymentWorksVendor, oldVendor, vendorDetail);

            if (!checkForLockingDocument(vendorMaintDoc)) {
                routed = processUnlockedVendorMaintenanceDocument(documentNumbers, vendorMaintDoc);
            } else {
                LOG.error("Failed to route Vendor Edit document due to lock");
                GlobalVariables.getMessageMap().putError("vendorDetail", KFSKeyConstants.ERROR_CUSTOM, "Failed to route Vendor Edit document due to lock");
            }
        } catch (Exception e) {
            LOG.error("Failed to route Vendor document due to error(s): " + e.getMessage());
        }
        return routed;
    }

    protected MaintenanceDocument buildVendorMaintenanceDocument(PaymentWorksVendor paymentWorksVendor, VendorDetail vendorDetail) throws WorkflowException {
        MaintenanceDocument vendorMaintDoc = buildVendorMaintenanceDocumentBase(vendorDetail, new VendorDetail(), paymentWorksVendor.getRequestingCompanyName(), KFSConstants.MAINTENANCE_NEW_ACTION);
        vendorMaintDoc.getDocumentHeader().setExplanation(paymentWorksVendor.getRequestingCompanyDesc());
        return vendorMaintDoc;
    }

    protected MaintenanceDocument buildVendorMaintenanceDocument(PaymentWorksVendor paymentWorksVendor, VendorDetail oldVendor, VendorDetail vendorDetail) throws WorkflowException {
        MaintenanceDocument vendorMaintDoc = buildVendorMaintenanceDocumentBase(vendorDetail, oldVendor, paymentWorksVendor.getVendorName(), KFSConstants.MAINTENANCE_EDIT_ACTION);
        vendorMaintDoc.getNewMaintainableObject().setDocumentNumber(vendorMaintDoc.getDocumentNumber());
        return vendorMaintDoc;
    }

    protected MaintenanceDocument buildVendorMaintenanceDocumentBase(VendorDetail newVendorDetail, VendorDetail oldVendorDetail, String documentDescription, String doucmentAction) throws WorkflowException {
        MaintenanceDocument vendorMaintDoc = (MaintenanceDocument) documentService.getNewDocument(CUVendorConstants.VENDOR_DOCUMENT_TYPE_NAME);
        vendorMaintDoc.getNewMaintainableObject().setBusinessObject(newVendorDetail);
        vendorMaintDoc.getOldMaintainableObject().setBusinessObject(oldVendorDetail);
        vendorMaintDoc.getDocumentHeader().setDocumentDescription(StringUtils.defaultIfBlank(documentDescription, StringUtils.EMPTY));
        vendorMaintDoc.getNewMaintainableObject().setMaintenanceAction(doucmentAction);
        return vendorMaintDoc;
    }

    protected boolean processUnlockedVendorMaintenanceDocument(StringBuffer documentNumbers, MaintenanceDocument vendorMaintDoc) throws WorkflowException {
        boolean routed = false;
        try {
            vendorMaintDoc.validateBusinessRules(new RouteDocumentEvent(vendorMaintDoc));
            documentService.routeDocument(vendorMaintDoc, null, null);
            routed = true;
            documentNumbers.append(vendorMaintDoc.getDocumentNumber() + ",");
        } catch (ValidationException ve) {
            LOG.error("Failed to route Vendor Edit document due to business rule error(s): "
                    + getPaymentWorksUtilityService().getAutoPopulatingErrorMessages(GlobalVariables.getMessageMap().getErrorMessages()));
        }
        return routed;
    }

    @Override
    public boolean directVendorEdit(PaymentWorksVendor paymentWorksVendor) {
        boolean success = true;
        String vendorNumberList = paymentWorksVendor.getVendorNumberList();
        List<String> vendorNumbers = Arrays.asList(vendorNumberList.split("\\s*,\\s*"));

        for (String vendorNumber : vendorNumbers) {
            VendorDetail vendorDetail = vendorService.getVendorDetail(vendorNumber);
            if (ObjectUtils.isNotNull(vendorDetail)) {
                if (!getPaymentWorksVendorUpdateConversionService().duplicateFieldsOnVendor(vendorDetail, paymentWorksVendor)) {
                    vendorDetail = getPaymentWorksVendorUpdateConversionService().createVendorDetailForEdit(vendorDetail, null, paymentWorksVendor);
                    try {
                        businessObjectService.save(vendorDetail);
                    } catch (Exception e) {
                        GlobalVariables.getMessageMap().putError("vendorDetail", KFSKeyConstants.ERROR_CUSTOM, e.getCause().getMessage());
                        success = false;
                        break;
                    }
                } else {
                    GlobalVariables.getMessageMap().putError("vendorDetail", KFSKeyConstants.ERROR_CUSTOM, "Duplicate vendor update detected");
                    success = false;
                }
            }
        }
        return success;
    }

    @Override
    public boolean directAchEdit(PaymentWorksVendorUpdatesDTO vendorUpdate, String vendorNumberList) {
        boolean successful = false;
        List<String> vendorNumbers = Arrays.asList(vendorNumberList.split("\\s*,\\s*"));
        successful = directAchEditByVendorNumbers(vendorUpdate, vendorNumbers);
        LOG.debug("directAchEdit, Was direct ach edit sucessful? " + successful);
        return successful;
    }

    protected boolean directAchEditByVendorNumbers(PaymentWorksVendorUpdatesDTO vendorUpdate, List<String> vendorNumbers) {
        boolean successful = false;
        for (String vendorNumber : vendorNumbers) {
            vendorNumber = findVendorNumber(vendorNumber);

            PayeeACHAccount payeeAchAccount = getPayeeAchAccount(PdpConstants.PayeeIdTypeCodes.VENDOR_ID, vendorNumber,
                    PaymentWorksConstants.PAYEE_ACH_ACCOUNT_DEFAULT_TRANSACTION_TYPE);

            if (ObjectUtils.isNotNull(payeeAchAccount)) {
                processEditAction(vendorUpdate, payeeAchAccount);
            } else {
                processNewAction(vendorUpdate, vendorNumber);
            }
        }
        return successful;
    }

    protected void processNewAction(PaymentWorksVendorUpdatesDTO vendorUpdate, String vendorNumber) {
        MaintenanceDocument paatDocument = buildPayeeACHAccountMaintenanceDocument("New request from PaymentWorks",
                KFSConstants.MAINTENANCE_NEW_ACTION);
        PayeeACHAccount payeeACHAccount = (PayeeACHAccount) paatDocument.getNewMaintainableObject().getDataObject();

        payeeACHAccount.setPayeeIdNumber(vendorNumber);
        payeeACHAccount.setPayeeIdentifierTypeCode(PdpConstants.PayeeIdTypeCodes.VENDOR_ID);
        payeeACHAccount.setAchAccountGeneratedIdentifier(new KualiInteger(getSequenceAccessorService()
                .getNextAvailableSequenceNumber(PdpConstants.ACH_ACCOUNT_IDENTIFIER_SEQUENCE_NAME)));
        payeeACHAccount.setAchTransactionType(PaymentWorksConstants.PAYEE_ACH_ACCOUNT_DEFAULT_TRANSACTION_TYPE);

        setValuesFromPaymentWorks(vendorUpdate, payeeACHAccount);

        /**
         * @todo, eventualy we will be able to set this value from PaymentWorks,
         * to move forward with testing, we are setting this to corporate
         * checking This will be done in KFSPTS-7364\ This should be moved down
         * to setValuesFromPaymentWorks when PaymentWorks has the value
         */
        payeeACHAccount.setBankAccountTypeCode(CUCheckingSavingsValuesFinder.BankAccountTypes.CORPORATE_CHECKING);

        addNote(paatDocument, "New ACH account automatically generated by PaymentWorks");
        List<AdHocRouteRecipient> adHocRoutingRecipients = null;
        try {
            getDocumentService().routeDocument(paatDocument, StringUtils.EMPTY, adHocRoutingRecipients);
        } catch (WorkflowException e) {
            LOG.error("directAchEditByVendorNumbers, unable to route new document.", e);
            throw new RuntimeException(e);
        }
    }

    protected void setValuesFromPaymentWorks(PaymentWorksVendorUpdatesDTO vendorUpdate, PayeeACHAccount payeeACHAccount) {
        List<PaymentWorksFieldChangeDTO> fieldChanges = vendorUpdate.getField_changes().getField_changes();
        payeeACHAccount.setBankRoutingNumber(findStringValueFromFieldChanges(fieldChanges, PaymentWorksConstants.FieldNames.ROUTING_NUMBER));
        payeeACHAccount.setBankAccountNumber(findStringValueFromFieldChanges(fieldChanges, PaymentWorksConstants.FieldNames.ACCOUNT_NUMBER));
        payeeACHAccount.setPayeeEmailAddress(findStringValueFromFieldChanges(fieldChanges, PaymentWorksConstants.FieldNames.ACH_EMAIL));
        payeeACHAccount.setActive(true);
    }

    protected void processEditAction(PaymentWorksVendorUpdatesDTO vendorUpdate, PayeeACHAccount payeeAchAccount) {
        MaintenanceDocument paatDocument = buildPayeeACHAccountMaintenanceDocument("Edit request from PaymentWorks",
                KFSConstants.MAINTENANCE_EDIT_ACTION);

        paatDocument.getOldMaintainableObject().setBusinessObject(payeeAchAccount);

        PayeeACHAccount payeeAccountToUpdate = (PayeeACHAccount) ObjectUtils.deepCopy(payeeAchAccount);

        paatDocument.getNewMaintainableObject().setBusinessObject(payeeAccountToUpdate);

        setValuesFromPaymentWorks(vendorUpdate, payeeAccountToUpdate);

        addNote(paatDocument, "ACH Edit automatically generated by PaymentWorks");
        List<AdHocRouteRecipient> adHocRoutingRecipients = null;
        try {
            getDocumentService().routeDocument(paatDocument, StringUtils.EMPTY, adHocRoutingRecipients);
        } catch (WorkflowException e) {
            LOG.error("directAchEditByVendorNumbers, unable to route edit document.", e);
            throw new RuntimeException(e);
        }
    }

    protected MaintenanceDocument buildPayeeACHAccountMaintenanceDocument(String documentDescription, String documentAction) {
        try {
            MaintenanceDocument paatDocument = (MaintenanceDocument) getDocumentService().getNewDocument(CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_MAINT_DOC_TYPE);
            paatDocument.getDocumentHeader().setDocumentDescription(documentDescription);

            PayeeACHAccountMaintainableImpl maintainable = (PayeeACHAccountMaintainableImpl) paatDocument.getNewMaintainableObject();
            maintainable.setMaintenanceAction(documentAction);
            return paatDocument;
        } catch (WorkflowException e) {
            LOG.error("buildPayeeACHAccountMaintenanceDocument, Unable to create a PAAT Maintenance Document.", e);
            throw new RuntimeException(e);
        }
    }

    protected String findVendorNumber(String vendorNumber) {
        LOG.info("findVendorNumber, Starting vendorNumber: " + vendorNumber);
        if (StringUtils.contains(vendorNumber, "-")) {
            String newVendNumber = StringUtils.split(vendorNumber, "-")[0];
            LOG.info("findVendorNumber, resetting vendor number to " + newVendNumber);
            vendorNumber = newVendNumber;
        }
        return vendorNumber;
    }

    protected void addNote(Document document, String noteText) {
        Note note = new Note();
        note.setNoteText(noteText);
        note.setRemoteObjectIdentifier(document.getObjectId());
        note.setAuthorUniversalIdentifier(GlobalVariables.getUserSession().getPerson().getPrincipalId());
        note.setNoteTypeCode(KFSConstants.NoteTypeEnum.DOCUMENT_HEADER_NOTE_TYPE.getCode());
        note.setNotePostedTimestampToCurrent();
        document.addNote(note);
    }

    @Override
    public void sendVendorInitiatedEmail(String documentNumber, String vendorName, String contactEmail) {
        String fromAddress = parameterService.getParameterValueAsString(PaymentWorksRetrieveNewVendorStep.class,
                PaymentWorksConstants.PaymentWorksParameters.VENDOR_INITIATED_EMAIL_FROM_ADDRESS);
        String subject = parameterService.getParameterValueAsString(PaymentWorksRetrieveNewVendorStep.class,
                PaymentWorksConstants.PaymentWorksParameters.VENDOR_INITIATED_EMAIL_SUBJECT);
        String body = parameterService.getParameterValueAsString(PaymentWorksRetrieveNewVendorStep.class,
                PaymentWorksConstants.PaymentWorksParameters.VENDOR_INITIATED_EMAIL_BODY);

        BodyMailMessage message = new BodyMailMessage();
        message.setFromAddress(fromAddress);
        message.getToAddresses().add(contactEmail);
        message.setSubject(subject);
        message.setMessage(body);

        try {
            emailService.sendMessage(message, false);
        } catch (Exception e) {
            LOG.error("Failed to send vendor initiated email: " + e);
        }
    }

    @Override
    public void sendVendorApprovedEmail(String vendorNumber, String contactEmail, String vendorName) {
        String fromAddress = parameterService.getParameterValueAsString(PaymentWorksUploadSuppliersStep.class,
                PaymentWorksConstants.PaymentWorksParameters.VENDOR_APPROVED_EMAIL_FROM_ADRESS);
        String subject = parameterService.getParameterValueAsString(PaymentWorksUploadSuppliersStep.class,
                PaymentWorksConstants.PaymentWorksParameters.VENDOR_APPROVED_EMAIL_SUBJECT);
        String body = parameterService.getParameterValueAsString(PaymentWorksUploadSuppliersStep.class,
                PaymentWorksConstants.PaymentWorksParameters.VENDOR_APPROVED_EMAIL_BODY);
        body = StringUtils.replace(body, "[VENDOR_NUMBER]", vendorNumber);
        body = StringUtils.replace(body, "[VENDOR_NAME]", vendorName);

        BodyMailMessage message = new BodyMailMessage();
        message.setFromAddress(fromAddress);
        message.getToAddresses().add(contactEmail);
        message.setSubject(subject);
        message.setMessage(body);

        try {
            emailService.sendMessage(message, false);
        } catch (Exception e) {
            LOG.error("Failed to send vendor approved email: " + e);
        }

    }

    protected PayeeACHAccount getPayeeAchAccount(String payeeIdentifierTypeCode, String payeeIdNumber, String achTransactionType) {
        Map<String, Object> fieldValues = new HashMap<String, Object>();
        fieldValues.put(PdpPropertyConstants.PAYEE_IDENTIFIER_TYPE_CODE, payeeIdentifierTypeCode);
        fieldValues.put(PdpPropertyConstants.PAYEE_ID_NUMBER, payeeIdNumber);
        fieldValues.put(PdpPropertyConstants.ACH_TRANSACTION_TYPE, achTransactionType);

        if (LOG.isInfoEnabled()) {
            LOG.info(new StringBuilder("getActivePayeeAchAccount, search parameters: payeeIdentifierTypeCode ")
                    .append(payeeIdentifierTypeCode).append("  payeeIdNumber ").append(payeeIdNumber)
                    .append("  achTransactionType ").append(achTransactionType).toString());
        }

        Collection<PayeeACHAccount> payeeAchAccounts = getBusinessObjectService().findMatching(PayeeACHAccount.class, fieldValues);

        PayeeACHAccount payeeAchAccount = null;

        if (ObjectUtils.isNotNull(payeeAchAccounts) && !payeeAchAccounts.isEmpty()) {
            payeeAchAccount = payeeAchAccounts.iterator().next();
        }

        return payeeAchAccount;
    }

    protected String findStringValueFromFieldChanges(List<PaymentWorksFieldChangeDTO> fieldChanges, String fieldName) {
        String fieldValue = null;
        boolean foundValue = false;
        for (PaymentWorksFieldChangeDTO fieldChange : fieldChanges) {
            if (fieldChange.getField_name().equals(fieldName)) {
                fieldValue = fieldChange.getTo_value();
                foundValue = true;
                break;
            }
        }
        if (!foundValue) {
            LOG.error("findStringValueFromFieldChanges, unable to find a value for " + fieldName);
        }
        return fieldValue;
    }

    protected boolean checkForLockingDocument(MaintenanceDocument document) {
        String blockingDocId = getMaintenanceDocumentService().getLockingDocumentId(document);
        if (StringUtils.isBlank(blockingDocId)) {
            return false;
        } else {
            return true;
        }
    }

    public DocumentService getDocumentService() {
        return documentService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public AchBankService getAchBankService() {
        return achBankService;
    }

    public void setAchBankService(AchBankService achBankService) {
        this.achBankService = achBankService;
    }

    public EmailService getEmailService() {
        return emailService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public VendorService getVendorService() {
        return vendorService;
    }

    public void setVendorService(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public MaintenanceDocumentService getMaintenanceDocumentService() {
        return maintenanceDocumentService;
    }

    public void setMaintenanceDocumentService(MaintenanceDocumentService maintenanceDocumentService) {
        this.maintenanceDocumentService = maintenanceDocumentService;
    }

    public PaymentWorksNewVendorConversionService getPaymentWorksNewVendorConversionService() {
        return paymentWorksNewVendorConversionService;
    }

    public void setPaymentWorksNewVendorConversionService(
            PaymentWorksNewVendorConversionService paymentWorksNewVendorConversionService) {
        this.paymentWorksNewVendorConversionService = paymentWorksNewVendorConversionService;
    }

    public PaymentWorksUtilityService getPaymentWorksUtilityService() {
        return paymentWorksUtilityService;
    }

    public void setPaymentWorksUtilityService(PaymentWorksUtilityService paymentWorksUtilityService) {
        this.paymentWorksUtilityService = paymentWorksUtilityService;
    }

    public PaymentWorksAchService getPaymentWorksAchService() {
        return paymentWorksAchService;
    }

    public void setPaymentWorksAchService(PaymentWorksAchService paymentWorksAchService) {
        this.paymentWorksAchService = paymentWorksAchService;
    }

    public PaymentWorksVendorUpdateConversionService getPaymentWorksVendorUpdateConversionService() {
        return paymentWorksVendorUpdateConversionService;
    }

    public void setPaymentWorksVendorUpdateConversionService(
            PaymentWorksVendorUpdateConversionService paymentWorksVendorUpdateConversionService) {
        this.paymentWorksVendorUpdateConversionService = paymentWorksVendorUpdateConversionService;
    }

    public SequenceAccessorService getSequenceAccessorService() {
        return sequenceAccessorService;
    }

    public void setSequenceAccessorService(SequenceAccessorService sequenceAccessorService) {
        this.sequenceAccessorService = sequenceAccessorService;
    }

}
