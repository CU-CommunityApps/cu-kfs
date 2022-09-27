package edu.cornell.kfs.fp.document.web.struts;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.kew.api.document.DocumentStatus;
import org.kuali.kfs.krad.util.GlobalVariables;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.businessobject.RecurringDisbursementVoucherPDPStatus;
import edu.cornell.kfs.fp.document.RecurringDisbursementVoucherDocument;
import edu.cornell.kfs.fp.service.RecurringDisbursementVoucherDocumentService;
import edu.cornell.kfs.fp.service.RecurringDisbursementVoucherPaymentMaintenanceService;

public class RecurringDisbursementVoucherForm extends CuDisbursementVoucherForm {

    private static final long serialVersionUID = 7035540080454973823L;

    private transient List<RecurringDisbursementVoucherPDPStatus> pdpStatuses;
    private transient RecurringDisbursementVoucherDocumentService recurringDisbursementVoucherDocumentService;
    private transient RecurringDisbursementVoucherPaymentMaintenanceService recurringDisbursementVoucherPaymentMaintenanceService;

    @Override
    public String getDocTypeName() {
        return CuFPConstants.RecurringDisbursementVoucherDocumentConstants.RECURRING_DV_DOCUMENT_TYPE_NAME;
    }

    @Override
    protected String getDefaultDocumentTypeName() {
        return CuFPConstants.RecurringDisbursementVoucherDocumentConstants.RECURRING_DV_DOCUMENT_TYPE_NAME;
    }

    public boolean isCancelPDPPaymentsActionAvailable() {
        if (getRecurringDisbursementVoucherPaymentMaintenanceService().hasCancelPermission(GlobalVariables.getUserSession().getPerson())) {
            return areAnyPaymentDetailsCancelable() || areAnyDVsCancelable();
        }
        return false;
    }
    
    private boolean areAnyPaymentDetailsCancelable() {
        Collection<PaymentDetail> details = getRecurringDisbursementVoucherDocumentService().findPaymentDetailsFromRecurringDisbursementVoucher((RecurringDisbursementVoucherDocument)getDocument());
        for (PaymentDetail detail : details) {
            String paymentDetailStatus = detail.getPaymentGroup().getPaymentStatusCode();
            
            if (getRecurringDisbursementVoucherDocumentService().isPaymentCancelableByLoggedInUser(paymentDetailStatus)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean areAnyDVsCancelable() {
        for (RecurringDisbursementVoucherPDPStatus pdpSatus : getPdpStatuses()) {
            if(isDvInSavedStatus(pdpSatus) || (isPaymentPreExtracted(pdpSatus) && isDvNotInCanceledStatus(pdpSatus))) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isDvInSavedStatus(RecurringDisbursementVoucherPDPStatus pdpSatus) {
        return StringUtils.equalsIgnoreCase(pdpSatus.getDvStatus(), DocumentStatus.SAVED.getLabel());
    }

    private boolean isPaymentPreExtracted(RecurringDisbursementVoucherPDPStatus pdpSatus) {
        return StringUtils.equals(pdpSatus.getPdpStatus(), CuFPConstants.RecurringDisbursementVoucherDocumentConstants.PDP_PRE_EXTRACTION_STATUS);
    }

    private boolean isDvNotInCanceledStatus(RecurringDisbursementVoucherPDPStatus pdpSatus) {
        return !StringUtils.equalsIgnoreCase(pdpSatus.getDvStatus(), DocumentStatus.CANCELED.getLabel());
    }

    public boolean isRecurringDVDetailsDefaultOpen() {
        return !((RecurringDisbursementVoucherDocument)this.getDocument()).getRecurringDisbursementVoucherDetails().isEmpty();
    }

    public int getRecurringDVDetailsSize() {
        return ((RecurringDisbursementVoucherDocument)this.getDocument()).getRecurringDisbursementVoucherDetails().size();
    }

    public boolean isPreDisbursementProcessorTabDefaultOpen() {
        return !getPdpStatuses().isEmpty();
    }

    public List<RecurringDisbursementVoucherPDPStatus> getPdpStatuses() {
        if(pdpStatuses == null) {
            pdpStatuses = getRecurringDisbursementVoucherDocumentService().findPdpStatuses((RecurringDisbursementVoucherDocument)getDocument());
        }
        return pdpStatuses;
    }

    public void setPdpStatuses(List<RecurringDisbursementVoucherPDPStatus> pdpStatuses) {
        this.pdpStatuses = pdpStatuses;
    }

    public RecurringDisbursementVoucherDocumentService getRecurringDisbursementVoucherDocumentService() {
        if (recurringDisbursementVoucherDocumentService == null) {
            recurringDisbursementVoucherDocumentService = SpringContext.getBean(RecurringDisbursementVoucherDocumentService.class);
        }
        return recurringDisbursementVoucherDocumentService;
    }

    public void setRecurringDisbursementVoucherDocumentService(
            RecurringDisbursementVoucherDocumentService recurringDisbursementVoucherDocumentService) {
        this.recurringDisbursementVoucherDocumentService = recurringDisbursementVoucherDocumentService;
    }

    public RecurringDisbursementVoucherPaymentMaintenanceService getRecurringDisbursementVoucherPaymentMaintenanceService() {
        if (recurringDisbursementVoucherPaymentMaintenanceService == null) {
            recurringDisbursementVoucherPaymentMaintenanceService = SpringContext.getBean(RecurringDisbursementVoucherPaymentMaintenanceService.class);
        }
        return recurringDisbursementVoucherPaymentMaintenanceService;
    }

    public void setRecurringDisbursementVoucherPaymentMaintenanceService(RecurringDisbursementVoucherPaymentMaintenanceService paymentMaintenanceService) {
        this.recurringDisbursementVoucherPaymentMaintenanceService = paymentMaintenanceService;
    }

}
