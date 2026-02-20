package edu.cornell.kfs.vnd.batch.dto;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;

import edu.cornell.kfs.sys.util.CemiUtils;
import edu.cornell.kfs.vnd.CUVendorConstants.VendorOwnershipCodes;
import edu.cornell.kfs.vnd.CemiVendorConstants;
import edu.cornell.kfs.vnd.CemiVendorConstants.TaxAuthorityFormTypes;

@SuppressWarnings("deprecation")
public class CemiSupplier {

    private final VendorDetail vendorDetail;
    private final String supplierId;
    private final String supplierReferenceId;
    private final String taxAuthorityFormType;

    public CemiSupplier(final VendorDetail vendorDetail, final String supplierId) {
        this.vendorDetail = vendorDetail;
        this.supplierId = supplierId;
        this.supplierReferenceId = buildSupplierReferenceId(vendorDetail);
        this.taxAuthorityFormType = determineTaxAuthorityFormType(vendorDetail);
    }

    private static String buildSupplierReferenceId(final VendorDetail vendor) {
        return MessageFormat.format(CemiVendorConstants.SUPPLIER_REFERENCE_ID_FORMAT,
                vendor.getVendorHeaderGeneratedIdentifier(),
                vendor.getVendorDetailAssignedIdentifier());
    }

    private static String determineTaxAuthorityFormType(final VendorDetail vendor) {
        if (StringUtils.isNotBlank(vendor.getVendorHeader().getVendorW8TypeCode())) {
            return TaxAuthorityFormTypes.FORM_1042S;
        } else if (StringUtils.equals(vendor.getVendorHeader().getVendorOwnershipCode(),
                VendorOwnershipCodes.INDIVIDUAL_OR_SOLE_PROPRIETOR_OR_SMLLC)) {
            return TaxAuthorityFormTypes.FORM_1099_MISC;
        } else {
            return KFSConstants.EMPTY_STRING;
        }
    }

    public VendorDetail getVendorDetail() {
        return vendorDetail;
    }

    public VendorHeader getVendorHeader() {
        return vendorDetail.getVendorHeader();
    }

    public String getSupplierId() {
        return supplierId;
    }

    public String getSupplierReferenceId() {
        return supplierReferenceId;
    }

    public String getTaxAuthorityFormType() {
        return taxAuthorityFormType;
    }

    public String getIrs1099SupplierFlag() {
        return CemiUtils.convertToBooleanValueForFileExtract(
                StringUtils.equals(taxAuthorityFormType, TaxAuthorityFormTypes.FORM_1099_MISC));
    }

}
