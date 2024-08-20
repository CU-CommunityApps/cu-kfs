package edu.cornell.kfs.pmw.document.validation.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.document.validation.impl.GlobalDocumentRuleBase;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.util.GlobalVariables;
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
            vendorDetail.refreshNonKeyFieldsFromPmwVendorReference();
        }
    }

    @Override
    public boolean processCustomAddCollectionLineBusinessRules(
            final MaintenanceDocument document, final String collectionName,
            final PersistableBusinessObject bo) {
        final PaymentWorksVendorGlobalDetail vendorDetail = (PaymentWorksVendorGlobalDetail) bo;
        vendorDetail.refreshNonKeyFieldsFromPmwVendorReference();

        boolean success = true;
        final String errorPropertyPath = buildVendorDetailsAddLinePropertyPath(
                PaymentWorksPropertiesConstants.PMW_VENDOR_ID);
        final PaymentWorksVendor pmwVendor = vendorDetail.getPmwVendor();
        if (ObjectUtils.isNull(pmwVendor)) {
            putFieldError(errorPropertyPath,
                    PaymentWorksKeyConstants.ERROR_PAYMENTWORKS_VENDOR_GLOBAL_DETAILS_ADDLINE_NOT_FOUND,
                    String.valueOf(vendorDetail.getPmwVendorId()));
            success = false;
        }

        return success;
    }

    private String buildVendorDetailsAddLinePropertyPath(final String propertyName) {
        return StringUtils.join(KFSConstants.ADD_PREFIX, KFSConstants.DELIMITER,
                PaymentWorksPropertiesConstants.VENDOR_DETAILS, KFSConstants.DELIMITER, propertyName);
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
        boolean success = true;
        final PaymentWorksVendorGlobalAction actionType = paymentWorksVendorGlobal.getActionType();
        if (actionType == null || actionType == PaymentWorksVendorGlobalAction.NO_ACTION) {
            putFieldError(PaymentWorksPropertiesConstants.ACTION_TYPE_CODE,
                    PaymentWorksKeyConstants.ERROR_PAYMENTWORKS_VENDOR_GLOBAL_ACTION_EMPTY,
                    PaymentWorksVendorGlobalAction.RESTAGE_FOR_UPLOAD.getActionLabel());
            success = false;
        } else if (actionType != PaymentWorksVendorGlobalAction.RESTAGE_FOR_UPLOAD) {
            putFieldError(PaymentWorksPropertiesConstants.ACTION_TYPE_CODE,
                    PaymentWorksKeyConstants.ERROR_PAYMENTWORKS_VENDOR_GLOBAL_ACTION_INVALID,
                    new String[] {
                            actionType.getActionLabel(),
                            PaymentWorksVendorGlobalAction.RESTAGE_FOR_UPLOAD.getActionLabel()
                    });
            success = false;
        }
        return success;
    }

    private boolean checkPaymentWorksVendorChanges() {
        final List<PaymentWorksVendorGlobalDetail> vendorDetails = paymentWorksVendorGlobal.getVendorDetails();
        if (CollectionUtils.isEmpty(vendorDetails)) {
            putFieldError(PaymentWorksPropertiesConstants.VENDOR_DETAILS,
                    PaymentWorksKeyConstants.ERROR_PAYMENTWORKS_VENDOR_GLOBAL_DETAILS_EMPTY);
            return false;
        }

        boolean success = true;
        int i = -1;
        final Set<Integer> encounteredPmwVendors = new HashSet<>();

        for (final PaymentWorksVendorGlobalDetail vendorDetail : vendorDetails) {
            i++;
            final String errorPathPrefix = buildVendorDetailsErrorPathPrefix(i);
            final String pmwVendorIdErrorPath = errorPathPrefix + PaymentWorksPropertiesConstants.PMW_VENDOR_ID;

            final boolean isDuplicate = !encounteredPmwVendors.add(vendorDetail.getPmwVendorId());
            if (isDuplicate) {
                putFieldError(pmwVendorIdErrorPath,
                        PaymentWorksKeyConstants.ERROR_PAYMENTWORKS_VENDOR_GLOBAL_DETAILS_DUPLICATE,
                        String.valueOf(vendorDetail.getPmwVendorId()));
                success = false;
            }

            final PaymentWorksVendor pmwVendor = vendorDetail.getPmwVendor();
            if (ObjectUtils.isNull(pmwVendor)) {
                putFieldError(pmwVendorIdErrorPath,
                        PaymentWorksKeyConstants.ERROR_PAYMENTWORKS_VENDOR_GLOBAL_DETAILS_NOT_FOUND,
                        String.valueOf(vendorDetail.getPmwVendorId()));
                success = false;
            } else if (paymentWorksVendorGlobal.getActionType() == PaymentWorksVendorGlobalAction.RESTAGE_FOR_UPLOAD) {
                success &= checkPaymentWorksVendorChangeForRestaging(pmwVendor, errorPathPrefix);
            }
        }

        return success;
    }

    private boolean checkPaymentWorksVendorChangeForRestaging(
            final PaymentWorksVendor pmwVendor, final String errorPathPrefix) {
        final String uploadStatusErrorPath = errorPathPrefix
                + PaymentWorksPropertiesConstants.PaymentWorksVendor.SUPPLIER_UPLOAD_STATUS;

        switch (StringUtils.defaultString(pmwVendor.getSupplierUploadStatus())) {
            case SupplierUploadStatus.VENDOR_UPLOADED :
            case SupplierUploadStatus.UPLOAD_FAILED :
                return true;

            case SupplierUploadStatus.READY_FOR_UPLOAD :
                GlobalVariables.getMessageMap().putWarningWithoutFullErrorPath(
                        MAINTAINABLE_ERROR_PREFIX + uploadStatusErrorPath,
                        PaymentWorksKeyConstants.WARNING_PAYMENTWORKS_VENDOR_GLOBAL_DETAILS_UPLOAD_STATUS_MATCH,
                        String.valueOf(pmwVendor.getId()), pmwVendor.getSupplierUploadStatus());
                return true;

            default :
                putFieldError(uploadStatusErrorPath,
                        PaymentWorksKeyConstants.ERROR_PAYMENTWORKS_VENDOR_GLOBAL_DETAILS_UPLOAD_STATUS_INELIGIBLE,
                        new String[] { String.valueOf(pmwVendor.getId()), pmwVendor.getSupplierUploadStatus() });
                return false;
        }
    }

    private String buildVendorDetailsErrorPathPrefix(final int index) {
        return StringUtils.join(PaymentWorksPropertiesConstants.VENDOR_DETAILS,
                KFSConstants.SQUARE_BRACKET_LEFT, index, KFSConstants.SQUARE_BRACKET_RIGHT, KFSConstants.DELIMITER);
    }

}
