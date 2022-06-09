package edu.cornell.kfs.pmw.batch.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kew.api.exception.WorkflowException;

import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.bo.DocumentHeader;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.rules.rule.event.RouteDocumentEvent;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.PdpConstants.PayeeIdTypeCodes;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.pdp.document.PayeeACHAccountMaintainableImpl;

import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.pdp.CUPdpConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.businessobject.KfsAchDataWrapper;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksNewVendorPayeeAchBatchReportData;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksBatchUtilityService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksNewVendorPayeeAchReportService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksNewVendorRequestsReportService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksVendorAchDataProcessingIntoKfsService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksVendorToKfsPayeeAchAccountConversionService;
import edu.cornell.kfs.vnd.CUVendorConstants;

public class PaymentWorksVendorAchDataProcessingIntoKfsServiceImpl implements PaymentWorksVendorAchDataProcessingIntoKfsService {
	private static final Logger LOG = LogManager.getLogger(PaymentWorksVendorAchDataProcessingIntoKfsServiceImpl.class);
    
    protected DocumentService documentService;
    protected PaymentWorksBatchUtilityService paymentWorksBatchUtilityService;
    protected PaymentWorksNewVendorPayeeAchReportService paymentWorksNewVendorPayeeAchReportService;
    protected PaymentWorksVendorToKfsPayeeAchAccountConversionService paymentWorksVendorToKfsPayeeAchAccountConversionService;

    @Transactional
    @Override
    public boolean createValidateAndRouteKfsPayeeAch(PaymentWorksVendor pmwVendor, PaymentWorksNewVendorPayeeAchBatchReportData reportData) {
        boolean processingSuccessful = false;
        MaintenanceDocument paatMaintenceDoc = createKfsPayeeAchMaintenaceDocument(pmwVendor, reportData);
        if (ObjectUtils.isNotNull(paatMaintenceDoc)
            && kfsPayeeAchMaintenanceDocumentValidated(paatMaintenceDoc, reportData, pmwVendor)) {
        	kfsPayeeAchMaintenceDocumentRouted(paatMaintenceDoc, reportData, pmwVendor);
            pmwVendor.setKfsAchDocumentNumber(paatMaintenceDoc.getDocumentNumber());
            processingSuccessful = true;
        }
        return processingSuccessful;
    }

    private MaintenanceDocument createKfsPayeeAchMaintenaceDocument(PaymentWorksVendor pmwVendor, PaymentWorksNewVendorPayeeAchBatchReportData reportData) {
        MaintenanceDocument paatMaintenceDoc = null;
        try {
            KfsAchDataWrapper kfsAchDataWrapper = getPaymentWorksVendorToKfsPayeeAchAccountConversionService().createKfsPayeeAchFromPmwVendor(pmwVendor);
            if (kfsAchDataWrapper.noProcessingErrorsGenerated()) {
                paatMaintenceDoc = buildPayeeAchMaintenanceDocument(kfsAchDataWrapper);
                LOG.info("createKfsPayeeAchMaintenaceDocument: paatMaintenceDoc created for pmwVendorId = " + pmwVendor.getPmwVendorRequestId()
                         + "  KFS vendor id = " + pmwVendor.getKfsVendorHeaderGeneratedIdentifier() + "-" + pmwVendor.getKfsVendorDetailAssignedIdentifier());
            }
            else {
                reportData.getRecordsThatCouldNotBeProcessedSummary().incrementRecordCount();
                reportData.addPmwVendorAchThatCouldNotBeProcessed(getPaymentWorksNewVendorPayeeAchReportService().createBatchReportVendorItem(pmwVendor, kfsAchDataWrapper.getErrorMessages()));
                LOG.info("createKfsPayeeAchMaintenaceDocument: paatMaintenceDoc not created due to pmw-to-kfs data conversion error(s): " + kfsAchDataWrapper.getErrorMessages().toString());
            }
        } catch (WorkflowException we) {
            List<String> edocCreateErrors = getPaymentWorksBatchUtilityService().convertReportDataValidationErrors(GlobalVariables.getMessageMap().getErrorMessages());
            captureKfsPayeeAchProcessingErrorsForVendor(pmwVendor, reportData, edocCreateErrors);
            LOG.error("createKfsPayeeAchMaintenaceDocument: eDoc creation error(s): " + edocCreateErrors.toString());
            LOG.error("createKfsPayeeAchMaintenaceDocument: eDoc creation exception caught: " + we.getMessage());
            paatMaintenceDoc = null;
        } finally {
            GlobalVariables.getMessageMap().clearErrorMessages();
        }
        return paatMaintenceDoc;
    }
    
    private boolean kfsPayeeAchMaintenanceDocumentValidated(MaintenanceDocument paatMaintenceDoc, PaymentWorksNewVendorPayeeAchBatchReportData reportData, PaymentWorksVendor pmwVendor) {
        boolean documentValidated = false;
        try {
            paatMaintenceDoc.validateBusinessRules(new RouteDocumentEvent(paatMaintenceDoc));
            LOG.info("kfsPayeeAchMaintenanceDocumentValidated: paatMaintenceDoc validate.");
            documentValidated = true;
        } catch (ValidationException ve) {
            List<String> validationErrors = getPaymentWorksBatchUtilityService().convertReportDataValidationErrors(GlobalVariables.getMessageMap().getErrorMessages());
            captureKfsPayeeAchProcessingErrorsForVendor(pmwVendor, reportData, validationErrors);
            LOG.error("kfsPayeeAchMaintenanceDocumentValidated: eDoc validation error(s): " + validationErrors.toString());
            LOG.error("kfsPayeeAchMaintenanceDocumentValidated: eDoc validation exception caught: " + ve.getMessage());
        } finally {
            GlobalVariables.getMessageMap().clearErrorMessages();
        }
        return documentValidated;
    }

    private void kfsPayeeAchMaintenceDocumentRouted(MaintenanceDocument paatMaintenceDoc, PaymentWorksNewVendorPayeeAchBatchReportData reportData, PaymentWorksVendor pmwVendor) {
        String annotationMessage = PaymentWorksConstants.KFSPayeeAchMaintenanceDocumentConstants.PAYMENTWORKS_NEW_VENDOR_ACH_CREATE_ROUTE_ANNOTATION + 
                pmwVendor.getPmwVendorRequestId();
        getDocumentService().routeDocument(paatMaintenceDoc, annotationMessage, null);
        LOG.info("kfsPayeeAchMaintenceDocumentRouted: paatMaintenceDoc routed.");
        GlobalVariables.getMessageMap().clearErrorMessages();
    }
    
    private void captureKfsPayeeAchProcessingErrorsForVendor(PaymentWorksVendor pmwVendorWithFailure, PaymentWorksNewVendorPayeeAchBatchReportData reportData, List<String> validationErrors) {
        reportData.getRecordsWithProcessingErrorsSummary().incrementRecordCount();
        reportData.addRecordWithProcessingErrors(getPaymentWorksNewVendorPayeeAchReportService().createBatchReportVendorItem(pmwVendorWithFailure, validationErrors));
    }

    private MaintenanceDocument buildPayeeAchMaintenanceDocument(KfsAchDataWrapper kfsAchDataWrapper) throws WorkflowException {
        MaintenanceDocument paatMaintenceDoc = buildPayeeAchMaintenanceDocumentBase(kfsAchDataWrapper.getPayeeAchAccount(), kfsAchDataWrapper.getPayeeAchAccountExplanation(), 
                                                                                    KFSConstants.MAINTENANCE_NEW_ACTION, kfsAchDataWrapper.getPayeeAchAccountNotes());
        return paatMaintenceDoc;
    }
    
    private MaintenanceDocument buildPayeeAchMaintenanceDocumentBase(PayeeACHAccount newPayeeAchAccount, String documentExplanation, String documentAction, List<Note> notesToPersist) throws WorkflowException{
        MaintenanceDocument paatMaintenceDoc = (MaintenanceDocument) getDocumentService().getNewDocument(CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_MAINT_DOC_TYPE);
        paatMaintenceDoc.getDocumentHeader().setDocumentDescription(buildPayeeAchMaintenanceDocumentDescription(newPayeeAchAccount));
        paatMaintenceDoc.getDocumentHeader().setExplanation(documentExplanation);
        
        PayeeACHAccountMaintainableImpl maintainable = (PayeeACHAccountMaintainableImpl) paatMaintenceDoc.getNewMaintainableObject();
        maintainable.setMaintenanceAction(KFSConstants.MAINTENANCE_NEW_ACTION);
        PayeeACHAccount achAccount = (PayeeACHAccount) maintainable.getDataObject();
        
        achAccount.setPayeeIdentifierTypeCode(newPayeeAchAccount.getPayeeIdentifierTypeCode());
        achAccount.setPayeeIdNumber(newPayeeAchAccount.getPayeeIdNumber());
        achAccount.setBankRoutingNumber(newPayeeAchAccount.getBankRoutingNumber());
        achAccount.setBankAccountNumber(newPayeeAchAccount.getBankAccountNumber());
        achAccount.setBankAccountTypeCode(newPayeeAchAccount.getBankAccountTypeCode());
        achAccount.setPayeeName(newPayeeAchAccount.getPayeeName());
        achAccount.setPayeeEmailAddress(newPayeeAchAccount.getPayeeEmailAddress());
        achAccount.setAchTransactionType(newPayeeAchAccount.getAchTransactionType());
        achAccount.setActive(newPayeeAchAccount.isActive());
        
        for (Note note : notesToPersist) {
            paatMaintenceDoc.addNote(note);
        }
        return paatMaintenceDoc;
    }
    
    private String buildPayeeAchMaintenanceDocumentDescription(PayeeACHAccount newPayeeAchAccount) {
        StringBuilder sb = new StringBuilder(PaymentWorksConstants.KFSPayeeAchMaintenanceDocumentConstants.DESCRIPTION_PREFIX_FOR_NEW_PAYEE_ACH).append(KFSConstants.BLANK_SPACE);
        sb.append(newPayeeAchAccount.getPayeeIdNumber()).append(KFSConstants.BLANK_SPACE).append(newPayeeAchAccount.getPayeeName());
        if (sb.toString().length() > PaymentWorksConstants.KFSPayeeAchMaintenanceDocumentConstants.DESCRIPTION_MAX_LENGTH) {
            return sb.toString().substring(0, PaymentWorksConstants.KFSPayeeAchMaintenanceDocumentConstants.DESCRIPTION_MAX_LENGTH);
        } else {
            return sb.toString();
        }
    }

    public DocumentService getDocumentService() {
        return documentService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public PaymentWorksBatchUtilityService getPaymentWorksBatchUtilityService() {
        return paymentWorksBatchUtilityService;
    }

    public void setPaymentWorksBatchUtilityService(PaymentWorksBatchUtilityService paymentWorksBatchUtilityService) {
        this.paymentWorksBatchUtilityService = paymentWorksBatchUtilityService;
    }

    public PaymentWorksVendorToKfsPayeeAchAccountConversionService getPaymentWorksVendorToKfsPayeeAchAccountConversionService() {
        return paymentWorksVendorToKfsPayeeAchAccountConversionService;
    }

    public void setPaymentWorksVendorToKfsPayeeAchAccountConversionService(PaymentWorksVendorToKfsPayeeAchAccountConversionService paymentWorksVendorToKfsPayeeAchAccountConversionService) {
        this.paymentWorksVendorToKfsPayeeAchAccountConversionService = paymentWorksVendorToKfsPayeeAchAccountConversionService;
    }

    public PaymentWorksNewVendorPayeeAchReportService getPaymentWorksNewVendorPayeeAchReportService() {
        return paymentWorksNewVendorPayeeAchReportService;
    }

    public void setPaymentWorksNewVendorPayeeAchReportService(
            PaymentWorksNewVendorPayeeAchReportService paymentWorksNewVendorPayeeAchReportService) {
        this.paymentWorksNewVendorPayeeAchReportService = paymentWorksNewVendorPayeeAchReportService;
    }

}
