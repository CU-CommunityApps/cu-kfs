package edu.cornell.kfs.pmw.batch.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.util.AutoPopulatingList;
import org.kuali.rice.kew.api.exception.WorkflowException;

import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.rules.rule.event.RouteDocumentEvent;
import org.kuali.kfs.krad.rules.rule.event.SaveDocumentEvent;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.SupplierDiversity;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

import edu.cornell.kfs.pdp.CUPdpConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.businessobject.KfsVendorDataWrapper;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksIsoFipsCountryItem;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksBatchReportRawDataItem;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksNewVendorRequestsBatchReportData;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksBatchUtilityService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksNewVendorRequestsReportService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksVendorDataProcessingIntoKfsService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksVendorToKfsVendorDetailConversionService;
import edu.cornell.kfs.vnd.CUVendorConstants;

public class PaymentWorksVendorDataProcessingIntoKfsServiceImpl implements PaymentWorksVendorDataProcessingIntoKfsService {
	private static final Logger LOG = LogManager.getLogger(PaymentWorksVendorDataProcessingIntoKfsServiceImpl.class);

    protected PaymentWorksVendorToKfsVendorDetailConversionService paymentWorksVendorToKfsVendorDetailConversionService;
    protected DocumentService documentService;
    protected PaymentWorksNewVendorRequestsReportService paymentWorksNewVendorRequestsReportService;
    protected PaymentWorksBatchUtilityService paymentWorksBatchUtilityService;
    
    @Override
    public boolean createValidateAndRouteOrSaveKFSVendor(PaymentWorksVendor pmwVendor, Map<String, List<PaymentWorksIsoFipsCountryItem>> paymentWorksIsoToFipsCountryMap,
                                                   Map<String, SupplierDiversity> paymentWorksToKfsDiversityMap, PaymentWorksNewVendorRequestsBatchReportData reportData) {
        boolean processingSuccessful = false;
        MaintenanceDocument vendorMaintenceDoc = createKfsVendorMaintenaceDocument(pmwVendor, paymentWorksIsoToFipsCountryMap, paymentWorksToKfsDiversityMap,
                reportData);
        if (ObjectUtils.isNotNull(vendorMaintenceDoc)) {
            if (kfsVendorMaintenanceDocumentValidated(vendorMaintenceDoc, reportData, pmwVendor)) {
                if (kfsVendorMaintenceDocumentRouted(vendorMaintenceDoc, reportData, pmwVendor)) {
                    pmwVendor.setKfsVendorDocumentNumber(vendorMaintenceDoc.getDocumentNumber());
                    processingSuccessful = true;
                }
            }
            else {
                if ( saveVendorMaintenanceDocument(vendorMaintenceDoc, reportData, pmwVendor)) {
                    pmwVendor.setKfsVendorDocumentNumber(vendorMaintenceDoc.getDocumentNumber());
                    processingSuccessful = true;
                }                  
            }
        }
        return processingSuccessful;
    }
    
    private MaintenanceDocument createKfsVendorMaintenaceDocument(PaymentWorksVendor pmwVendor, Map<String, List<PaymentWorksIsoFipsCountryItem>> paymentWorksIsoToFipsCountryMap, 
                                                                  Map<String, SupplierDiversity> paymentWorksToKfsDiversityMap, PaymentWorksNewVendorRequestsBatchReportData reportData) {
        MaintenanceDocument vendorMaintenceDoc = null;
        try {
            KfsVendorDataWrapper kfsVendorDataWrapper = getPaymentWorksVendorToKfsVendorDetailConversionService().createKfsVendorDetailFromPmwVendor(pmwVendor, paymentWorksIsoToFipsCountryMap, paymentWorksToKfsDiversityMap);
            if (kfsVendorDataWrapper.noProcessingErrorsGenerated()) {
                vendorMaintenceDoc = buildVendorMaintenanceDocument(pmwVendor, kfsVendorDataWrapper);
                LOG.info("createKfsVendorMaintenaceDocument: vendorMaintenceDoc created for pmwVendorId = " + pmwVendor.getPmwVendorRequestId());
            }
            else {
                reportData.getRecordsThatCouldNotBeProcessedSummary().incrementRecordCount();
                reportData.addPmwVendorThatCouldNotBeProcessed(new PaymentWorksBatchReportRawDataItem(pmwVendor.toString(), kfsVendorDataWrapper.getErrorMessages()));
                LOG.info("createKfsVendorMaintenaceDocument: vendorMaintenceDoc not created due to pmw-to-kfs data conversion error(s): " + kfsVendorDataWrapper.getErrorMessages().toString());
            }
        } catch (WorkflowException we) {
            List<String> edocCreateErrors = getPaymentWorksBatchUtilityService().convertReportDataValidationErrors(GlobalVariables.getMessageMap().getErrorMessages());
            captureKfsProcessingErrorsForVendor(pmwVendor, reportData, edocCreateErrors);
            LOG.error("createKfsVendorMaintenaceDocument: eDoc creation error(s): " + edocCreateErrors.toString());
            LOG.error("createKfsVendorMaintenaceDocument: eDoc creation exception caught: " + we.getMessage());
            vendorMaintenceDoc = null;
        } finally {
            GlobalVariables.getMessageMap().clearErrorMessages();
        }
        return vendorMaintenceDoc;
    }

    private boolean kfsVendorMaintenanceDocumentValidated(MaintenanceDocument vendorMaintenceDoc, PaymentWorksNewVendorRequestsBatchReportData reportData, PaymentWorksVendor pmwVendor) {
        boolean documentValidated = false;
        try {
            vendorMaintenceDoc.validateBusinessRules(new RouteDocumentEvent(vendorMaintenceDoc));
            LOG.info("kfsVendorMaintenanceDocumentValidated: vendorMaintenceDoc validate.");
            documentValidated = true;
        } catch (ValidationException ve) {
            List<String> validationErrors = getPaymentWorksBatchUtilityService().convertReportDataValidationErrors(GlobalVariables.getMessageMap().getErrorMessages());
            captureKfsProcessingErrorsForVendor(pmwVendor, reportData, validationErrors);
            LOG.error("kfsVendorMaintenanceDocumentValidated: eDoc validation error(s): " + validationErrors.toString());
            LOG.error("kfsVendorMaintenanceDocumentValidated: eDoc validation exception caught: " + ve.getMessage());       
        } finally {
            GlobalVariables.getMessageMap().clearErrorMessages();
        }
        return documentValidated;
    }
    
    private boolean saveVendorMaintenanceDocument(MaintenanceDocument vendorMaintenceDoc, PaymentWorksNewVendorRequestsBatchReportData reportData, PaymentWorksVendor pmwVendor) {
       // vendorMaintenceDoc.validateBusinessRules(new SaveDocumentEvent(vendorMaintenceDoc));
        GlobalVariables.getMessageMap().clearErrorMessages();
        LOG.info("saveVendorMaintenanceDocument: vendorMaintenceDoc validate save business rules.");
        boolean documentSaved = false;
        try {
            getDocumentService().saveDocument(vendorMaintenceDoc);
            LOG.info("kfsVendorMaintenceDocumentRouted: vendorMaintenceDoc routed.");
            documentSaved = true;
        } catch (WorkflowException we) {
            List<String> edocCreateErrors = getPaymentWorksBatchUtilityService().convertReportDataValidationErrors(GlobalVariables.getMessageMap().getErrorMessages());
            captureKfsProcessingErrorsForVendor(pmwVendor, reportData, edocCreateErrors);
            LOG.error("saveVendorMaintenanceDocument: eDoc saving error(s): " + edocCreateErrors.toString());
            LOG.error("saveVendorMaintenanceDocument: eDoc saving exception caught: " + we.getMessage());
        } finally {
            GlobalVariables.getMessageMap().clearErrorMessages();
        }
        return documentSaved;
    }

    private boolean kfsVendorMaintenceDocumentRouted(MaintenanceDocument vendorMaintenceDoc, PaymentWorksNewVendorRequestsBatchReportData reportData, PaymentWorksVendor pmwVendor) {
        boolean documentRouted = false;
        try {
            getDocumentService().routeDocument(vendorMaintenceDoc, PaymentWorksConstants.KFSVendorMaintenaceDocumentConstants.PAYMENTWORKS_NEW_VENDOR_CREATE_ROUTE_ANNOTATION, null);
            LOG.info("kfsVendorMaintenceDocumentRouted: vendorMaintenceDoc routed.");
            documentRouted = true;
        } catch (WorkflowException we) {
            List<String> edocCreateErrors = getPaymentWorksBatchUtilityService().convertReportDataValidationErrors(GlobalVariables.getMessageMap().getErrorMessages());
            captureKfsProcessingErrorsForVendor(pmwVendor, reportData, edocCreateErrors);
            LOG.error("kfsVendorMaintenceDocumentRouted: eDoc routing error(s): " + edocCreateErrors.toString());
            LOG.error("kfsVendorMaintenceDocumentRouted: eDoc routing exception caught: " + we.getMessage());
        } finally {
            GlobalVariables.getMessageMap().clearErrorMessages();
        }
        return documentRouted;
    }
    
    private void captureKfsProcessingErrorsForVendor(PaymentWorksVendor pmwVendorWithFailure, PaymentWorksNewVendorRequestsBatchReportData reportData, List<String> validationErrors) {
        reportData.getRecordsWithProcessingErrorsSummary().incrementRecordCount();
        reportData.addRecordWithProcessingErrors(getPaymentWorksNewVendorRequestsReportService().createBatchReportVendorItem(pmwVendorWithFailure, validationErrors));
    }
    
    private MaintenanceDocument buildVendorMaintenanceDocument(PaymentWorksVendor paymentWorksVendor, KfsVendorDataWrapper kfsVendorDataWrapper) throws WorkflowException {
        boolean isNewVendor = true;
        MaintenanceDocument vendorMaintDoc = buildVendorMaintenanceDocumentBase(kfsVendorDataWrapper.getVendorDetail(), new VendorDetail(), KFSConstants.MAINTENANCE_NEW_ACTION, isNewVendor, kfsVendorDataWrapper.getVendorNotes());
        return vendorMaintDoc;
    }
    
    private MaintenanceDocument buildVendorMaintenanceDocumentBase(VendorDetail newVendorDetail, VendorDetail oldVendorDetail, String documentAction, boolean isNewVendor, List<Note> notesToPersist) throws WorkflowException{
        MaintenanceDocument vendorMaintDoc = (MaintenanceDocument) getDocumentService().getNewDocument(CUVendorConstants.VENDOR_DOCUMENT_TYPE_NAME);
        vendorMaintDoc.getNewMaintainableObject().setBusinessObject(newVendorDetail);
        vendorMaintDoc.getOldMaintainableObject().setBusinessObject(oldVendorDetail);
        vendorMaintDoc.getDocumentHeader().setDocumentDescription(buildVendorMainenanceDocumentDescription(newVendorDetail, isNewVendor));
        vendorMaintDoc.getNewMaintainableObject().setMaintenanceAction(documentAction);
        for (Note note : notesToPersist) {
            vendorMaintDoc.addNote(note);
        }
        return vendorMaintDoc;
    }
    
    private String buildVendorMainenanceDocumentDescription(VendorDetail vendorDetail, boolean isNewVendor) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(vendorDetail.getVendorName())) {
            sb.append(vendorDetail.getVendorName());
        }
        else {
            sb.append(vendorDetail.getVendorLastName()).append(", ").append(vendorDetail.getVendorFirstName());
        }
        if (isNewVendor) {
            sb.append(PaymentWorksConstants.KFSVendorMaintenaceDocumentConstants.DESCRIPTION_SUFFIX_FOR_NEW_VENDOR);
        }
        if (sb.toString().length() > PaymentWorksConstants.KFSVendorMaintenaceDocumentConstants.DESCRIPTION_MAX_LENGTH) {
            return sb.toString().substring(0, PaymentWorksConstants.KFSVendorMaintenaceDocumentConstants.DESCRIPTION_MAX_LENGTH);
        } else {
            return sb.toString();
        }
    }

    public PaymentWorksVendorToKfsVendorDetailConversionService getPaymentWorksVendorToKfsVendorDetailConversionService() {
        return paymentWorksVendorToKfsVendorDetailConversionService;
    }

    public void setPaymentWorksVendorToKfsVendorDetailConversionService(
            PaymentWorksVendorToKfsVendorDetailConversionService paymentWorksVendorToKfsVendorDetailConversionService) {
        this.paymentWorksVendorToKfsVendorDetailConversionService = paymentWorksVendorToKfsVendorDetailConversionService;
    }

    public DocumentService getDocumentService() {
        return documentService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }
    public PaymentWorksNewVendorRequestsReportService getPaymentWorksNewVendorRequestsReportService() {
        return paymentWorksNewVendorRequestsReportService;
    }
    public void setPaymentWorksNewVendorRequestsReportService(
            PaymentWorksNewVendorRequestsReportService paymentWorksNewVendorRequestsReportService) {
        this.paymentWorksNewVendorRequestsReportService = paymentWorksNewVendorRequestsReportService;
    }

    public PaymentWorksBatchUtilityService getPaymentWorksBatchUtilityService() {
        return paymentWorksBatchUtilityService;
    }

    public void setPaymentWorksBatchUtilityService(PaymentWorksBatchUtilityService paymentWorksBatchUtilityService) {
        this.paymentWorksBatchUtilityService = paymentWorksBatchUtilityService;
    }

}
