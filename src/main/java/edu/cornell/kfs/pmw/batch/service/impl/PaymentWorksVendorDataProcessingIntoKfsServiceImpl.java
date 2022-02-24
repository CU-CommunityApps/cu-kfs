package edu.cornell.kfs.pmw.batch.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.kuali.kfs.kew.api.exception.WorkflowException;

import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.rules.rule.event.KualiDocumentEvent;
import org.kuali.kfs.krad.rules.rule.event.RouteDocumentEvent;
import org.kuali.kfs.krad.rules.rule.event.SaveDocumentEvent;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.SupplierDiversity;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

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
    
    private enum DocumentAction {
        SAVE, ROUTE
    };
    
    @Override
    public boolean createValidateAndRouteOrSaveKFSVendor(PaymentWorksVendor pmwVendor, Map<String, List<PaymentWorksIsoFipsCountryItem>> paymentWorksIsoToFipsCountryMap,
                                                   Map<String, SupplierDiversity> paymentWorksToKfsDiversityMap, PaymentWorksNewVendorRequestsBatchReportData reportData) {
        boolean processingSuccessful = false;
        MaintenanceDocument vendorMaintenceDoc = createKfsVendorMaintenaceDocument(pmwVendor, paymentWorksIsoToFipsCountryMap, paymentWorksToKfsDiversityMap,
                reportData);
        if (ObjectUtils.isNotNull(vendorMaintenceDoc)) {
            if (kfsVendorMaintenanceDocumentValidatedForRoute(vendorMaintenceDoc, reportData, pmwVendor)) {
                if (kfsVendorMaintenceDocumentRouted(vendorMaintenceDoc, reportData, pmwVendor)) {
                    pmwVendor.setKfsVendorDocumentNumber(vendorMaintenceDoc.getDocumentNumber());
                    processingSuccessful = true;
                }
            } else {
                if (kfsVendorMaintenanceDocumentValidatedForSave(vendorMaintenceDoc, reportData, pmwVendor)) {
                    if (saveVendorMaintenanceDocument(vendorMaintenceDoc, reportData, pmwVendor)) {
                        pmwVendor.setKfsVendorDocumentNumber(vendorMaintenceDoc.getDocumentNumber());
                        processingSuccessful = true;
                    }
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

    private boolean kfsVendorMaintenanceDocumentValidatedForRoute(MaintenanceDocument vendorMaintenceDoc,
            PaymentWorksNewVendorRequestsBatchReportData reportData, PaymentWorksVendor pmwVendor) {
        return kfsVendorMaintenanceDocumentValidatedForEvent(vendorMaintenceDoc, reportData, pmwVendor, new RouteDocumentEvent(vendorMaintenceDoc));
    }

    private boolean kfsVendorMaintenanceDocumentValidatedForSave(MaintenanceDocument vendorMaintenceDoc,
            PaymentWorksNewVendorRequestsBatchReportData reportData, PaymentWorksVendor pmwVendor) {
        return kfsVendorMaintenanceDocumentValidatedForEvent(vendorMaintenceDoc, reportData, pmwVendor, new SaveDocumentEvent(vendorMaintenceDoc));
    }
    
    private boolean kfsVendorMaintenanceDocumentValidatedForEvent(MaintenanceDocument vendorMaintenceDoc,
            PaymentWorksNewVendorRequestsBatchReportData reportData, PaymentWorksVendor pmwVendor, KualiDocumentEvent documentEvent) {
        boolean documentValidated = false;
        try {
            LOG.info("kfsVendorMaintenanceDocumentValidatedForEvent: vendorMaintenceDoc validate.");
            vendorMaintenceDoc.validateBusinessRules(documentEvent);           
            documentValidated = true;
        } catch (ValidationException ve) {
            List<String> validationErrors = getPaymentWorksBatchUtilityService().convertReportDataValidationErrors(GlobalVariables.getMessageMap().getErrorMessages());
            captureKfsProcessingErrorsForVendor(pmwVendor, reportData, validationErrors);
            LOG.error("kfsVendorMaintenanceDocumentValidatedForEvent: eDoc validation error(s): " + validationErrors.toString());
            LOG.error("kfsVendorMaintenanceDocumentValidatedForEvent: eDoc validation exception caught: " + ve.getMessage());
        } finally {
            GlobalVariables.getMessageMap().clearErrorMessages();
        }
        return documentValidated;
    }
    
    private boolean saveVendorMaintenanceDocument(MaintenanceDocument vendorMaintenceDoc, PaymentWorksNewVendorRequestsBatchReportData reportData,
            PaymentWorksVendor pmwVendor) {
        return kfsVendorMaintenceDocumentActionProcessed(vendorMaintenceDoc, reportData, pmwVendor, DocumentAction.SAVE);
    }

    private boolean kfsVendorMaintenceDocumentRouted(MaintenanceDocument vendorMaintenceDoc, PaymentWorksNewVendorRequestsBatchReportData reportData,
            PaymentWorksVendor pmwVendor) {
        return kfsVendorMaintenceDocumentActionProcessed(vendorMaintenceDoc, reportData, pmwVendor, DocumentAction.ROUTE);
    }

    private boolean kfsVendorMaintenceDocumentActionProcessed(MaintenanceDocument vendorMaintenceDoc, PaymentWorksNewVendorRequestsBatchReportData reportData,
            PaymentWorksVendor pmwVendor, DocumentAction documentAction) {
        boolean documentActionProcessed = false;
        
        if (DocumentAction.SAVE.equals(documentAction)) {
            getDocumentService().saveDocument(vendorMaintenceDoc);
            LOG.info("kfsVendorMaintenceDocumentActionProcessed: vendorMaintenceDoc saved.");
        } else if (DocumentAction.ROUTE.equals(documentAction)) {
            String annotationMessage = PaymentWorksConstants.KFSVendorMaintenaceDocumentConstants.PAYMENTWORKS_NEW_VENDOR_CREATE_ROUTE_ANNOTATION + 
                    pmwVendor.getPmwVendorRequestId();
            getDocumentService().routeDocument(vendorMaintenceDoc, annotationMessage, null);
            LOG.info("kfsVendorMaintenceDocumentActionProcessed: vendorMaintenceDoc routed.");
        } else {
            LOG.info("kfsVendorMaintenceDocumentActionProcessed: document action not supported.");
            return false;
        }

        documentActionProcessed = true;
        GlobalVariables.getMessageMap().clearErrorMessages();
            
        return documentActionProcessed;
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
