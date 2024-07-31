package edu.cornell.kfs.pmw.document.validation.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.document.validation.impl.GlobalDocumentRuleBase;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants.PaymentWorksVendorGlobalAction;
import edu.cornell.kfs.pmw.batch.PaymentWorksConstants.SupplierUploadStatus;
import edu.cornell.kfs.pmw.batch.PaymentWorksKeyConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksPropertiesConstants;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.businessobject.PaymentWorksVendorGlobal;
import edu.cornell.kfs.pmw.businessobject.PaymentWorksVendorGlobalDetail;

@SuppressWarnings("deprecation")
public class PaymentWorksVendorGlobalRule extends GlobalDocumentRuleBase {

    private PaymentWorksVendorGlobal paymentWorksVendorGlobal;

    @Override
    public void setupConvenienceObjects() {
        paymentWorksVendorGlobal = (PaymentWorksVendorGlobal) super.getNewBo();
        for (final PaymentWorksVendorGlobalDetail vendorDetail : paymentWorksVendorGlobal.getVendorDetails()) {
            vendorDetail.refreshNonUpdateableReferences();
        }
    }

    @Override
    protected boolean processCustomRouteDocumentBusinessRules(final MaintenanceDocument document) {
        setupConvenienceObjects();
        boolean success = super.processCustomRouteDocumentBusinessRules(document);
        success &= checkActionType();
        success &= checkPaymentWorksVendorChanges();
        return success;
    }

    @Override
    protected boolean processCustomApproveDocumentBusinessRules(final MaintenanceDocument document) {
        setupConvenienceObjects();
        boolean success = super.processCustomApproveDocumentBusinessRules(document);
        success &= checkActionType();
        success &= checkPaymentWorksVendorChanges();
        return success;
    }

    private boolean checkActionType() {
        final PaymentWorksVendorGlobalAction actionType = paymentWorksVendorGlobal.getActionType();
        if (actionType == null || actionType != PaymentWorksVendorGlobalAction.RESTAGE_FOR_UPLOAD) {
            GlobalVariables.getMessageMap().putError(PaymentWorksPropertiesConstants.ACTION_TYPE,
                    PaymentWorksKeyConstants.ERROR_PAYMENTWORKS_VENDOR_GLOBAL_ACTION_INVALID,
                    PaymentWorksVendorGlobalAction.RESTAGE_FOR_UPLOAD.actionLabel);
            return false;
        }
        return true;
    }

    private boolean checkPaymentWorksVendorChanges() {
        final List<PaymentWorksVendorGlobalDetail> vendorDetails = paymentWorksVendorGlobal.getVendorDetails();
        if (CollectionUtils.isEmpty(vendorDetails)) {
            GlobalVariables.getMessageMap().putError(PaymentWorksPropertiesConstants.VENDOR_DETAILS,
                    PaymentWorksKeyConstants.ERROR_PAYMENTWORKS_VENDOR_GLOBAL_DETAILS_EMPTY);
            return false;
        }

        boolean success = true;
        int i = -1;
        final Set<Integer> encounteredPmwVendors = new HashSet<>();

        for (final PaymentWorksVendorGlobalDetail vendorDetail : vendorDetails) {
            i++;
            final String errorPathPrefix = buildVendorDetailsErrorPathPrefix(i);
            final String vendorDetailsIdErrorPath = errorPathPrefix + KRADPropertyConstants.ID;
            if (!encounteredPmwVendors.add(vendorDetail.getId())) {
                GlobalVariables.getMessageMap().putError(vendorDetailsIdErrorPath,
                        PaymentWorksKeyConstants.ERROR_PAYMENTWORKS_VENDOR_GLOBAL_DETAILS_DUPLICATE,
                        String.valueOf(vendorDetail.getId()));
                success = false;
            }

            final PaymentWorksVendor pmwVendor = vendorDetail.getPmwVendor();
            if (ObjectUtils.isNull(pmwVendor) || vendorDetail.paymentWorksVendorWasDeletedOrPurged()) {
                GlobalVariables.getMessageMap().putError(vendorDetailsIdErrorPath,
                        PaymentWorksKeyConstants.ERROR_PAYMENTWORKS_VENDOR_GLOBAL_DETAILS_INVALID,
                        String.valueOf(vendorDetail.getId()));
                success = false;
            } else if (paymentWorksVendorGlobal.getActionType() == PaymentWorksVendorGlobalAction.RESTAGE_FOR_UPLOAD) {
                success &= checkPaymentWorksVendorChangeForRestaging(pmwVendor, errorPathPrefix);
            }
        }

        return success;
    }

    private boolean checkPaymentWorksVendorChangeForRestaging(
            final PaymentWorksVendor pmwVendor, final String errorPathPrefix) {
        final String uploadStatusErrorPath = StringUtils.join(errorPathPrefix,
                PaymentWorksPropertiesConstants.PMW_VENDOR, KFSConstants.DELIMITER,
                PaymentWorksPropertiesConstants.PaymentWorksVendor.SUPPLIER_UPLOAD_STATUS);

        switch (StringUtils.defaultString(pmwVendor.getSupplierUploadStatus())) {
            case SupplierUploadStatus.VENDOR_UPLOADED :
            case SupplierUploadStatus.UPLOAD_FAILED :
                return true;

            case SupplierUploadStatus.READY_FOR_UPLOAD :
                GlobalVariables.getMessageMap().putError(uploadStatusErrorPath,
                        PaymentWorksKeyConstants.ERROR_PAYMENTWORKS_VENDOR_GLOBAL_DETAILS_UPLOAD_STATUS_MATCH,
                        pmwVendor.getSupplierUploadStatus());
                return false;

            default :
                GlobalVariables.getMessageMap().putError(uploadStatusErrorPath,
                        PaymentWorksKeyConstants.ERROR_PAYMENTWORKS_VENDOR_GLOBAL_DETAILS_UPLOAD_STATUS_INELIGIBLE,
                        pmwVendor.getSupplierUploadStatus());
                return false;
        }
    }

    private String buildVendorDetailsErrorPathPrefix(final int index) {
        return StringUtils.join(PaymentWorksPropertiesConstants.VENDOR_DETAILS,
                KFSConstants.SQUARE_BRACKET_LEFT, index, KFSConstants.SQUARE_BRACKET_RIGHT, KFSConstants.DELIMITER);
    }

}
