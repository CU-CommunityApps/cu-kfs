package edu.cornell.kfs.pmw.businessobject;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.bo.GlobalBusinessObjectDetailBase;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.vnd.util.CuVendorUtils;

public class PaymentWorksVendorGlobalDetail extends GlobalBusinessObjectDetailBase {

    private static final long serialVersionUID = 1L;

    private Integer pmwVendorId;
    private String pmwVendorRequestId;
    private Integer kfsVendorHeaderGeneratedIdentifier;
    private Integer kfsVendorDetailAssignedIdentifier;
    private String supplierUploadStatus;
    private String requestingCompanyLegalName;

    private PaymentWorksVendor pmwVendor;

    @Override
    public void refreshNonUpdateableReferences() {
        if (representsUnsavedLine()) {
            refreshNonKeyFieldsFromPmwVendorReference();
        } else {
            super.refreshNonUpdateableReferences();
        }
    }

    private boolean representsUnsavedLine() {
        return StringUtils.isBlank(getDocumentNumber());
    }

    public void refreshNonKeyFieldsFromPmwVendorReference() {
        final PaymentWorksVendor oldPmwVendor = pmwVendor;
        super.refreshNonUpdateableReferences();
        if (ObjectUtils.isNotNull(pmwVendor)) {
            pmwVendorRequestId = pmwVendor.getPmwVendorRequestId();
            kfsVendorHeaderGeneratedIdentifier = pmwVendor.getKfsVendorHeaderGeneratedIdentifier();
            kfsVendorDetailAssignedIdentifier = pmwVendor.getKfsVendorDetailAssignedIdentifier();
            supplierUploadStatus = pmwVendor.getSupplierUploadStatus();
            requestingCompanyLegalName = pmwVendor.getRequestingCompanyLegalName();
        } else if (pmwVendorId == null
                || (ObjectUtils.isNotNull(oldPmwVendor) && !pmwVendorId.equals(oldPmwVendor.getId()))) {
            pmwVendorRequestId = null;
            kfsVendorHeaderGeneratedIdentifier = null;
            kfsVendorDetailAssignedIdentifier = null;
            supplierUploadStatus = null;
            requestingCompanyLegalName = null;
        }
    }

    public Integer getPmwVendorId() {
        return pmwVendorId;
    }

    public void setPmwVendorId(final Integer pmwVendorId) {
        this.pmwVendorId = pmwVendorId;
    }

    public String getPmwVendorRequestId() {
        return pmwVendorRequestId;
    }

    public void setPmwVendorRequestId(String pmwVendorRequestId) {
        this.pmwVendorRequestId = pmwVendorRequestId;
    }

    public Integer getKfsVendorHeaderGeneratedIdentifier() {
        return kfsVendorHeaderGeneratedIdentifier;
    }

    public void setKfsVendorHeaderGeneratedIdentifier(Integer kfsVendorHeaderGeneratedIdentifier) {
        this.kfsVendorHeaderGeneratedIdentifier = kfsVendorHeaderGeneratedIdentifier;
    }

    public Integer getKfsVendorDetailAssignedIdentifier() {
        return kfsVendorDetailAssignedIdentifier;
    }

    public void setKfsVendorDetailAssignedIdentifier(Integer kfsVendorDetailAssignedIdentifier) {
        this.kfsVendorDetailAssignedIdentifier = kfsVendorDetailAssignedIdentifier;
    }

    public String getKfsVendorNumber() {
        return CuVendorUtils.formatVendorNumber(kfsVendorHeaderGeneratedIdentifier, kfsVendorDetailAssignedIdentifier);
    }

    public void setKfsVendorNumber(final String kfsVendorNumber) {
        // Ignore
    }

    public String getSupplierUploadStatus() {
        return supplierUploadStatus;
    }

    public void setSupplierUploadStatus(String supplierUploadStatus) {
        this.supplierUploadStatus = supplierUploadStatus;
    }

    public String getRequestingCompanyLegalName() {
        return requestingCompanyLegalName;
    }

    public void setRequestingCompanyLegalName(String requestingCompanyLegalName) {
        this.requestingCompanyLegalName = requestingCompanyLegalName;
    }

    public PaymentWorksVendor getPmwVendor() {
        return pmwVendor;
    }

    public void setPmwVendor(final PaymentWorksVendor pmwVendor) {
        this.pmwVendor = pmwVendor;
    }

}
