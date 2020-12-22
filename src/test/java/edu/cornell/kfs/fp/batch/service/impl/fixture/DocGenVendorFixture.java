package edu.cornell.kfs.fp.batch.service.impl.fixture;

import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;

public enum DocGenVendorFixture {
    XYZ_INDUSTRIES(2100, 0, "XYZ Industries",
            VendorConstants.VendorTypes.DISBURSEMENT_VOUCHER, KFSConstants.PaymentPayeeTypes.VENDOR),
    REE_PHUND(5555, 0, "Phund, Ree",
            VendorConstants.VendorTypes.REFUND_PAYMENT, KFSConstants.PaymentPayeeTypes.REFUND_VENDOR);

    public final int vendorHeaderId;
    public final int vendorDetailId;
    public final String vendorName;
    public final String vendorTypeCode;
    public final String payeeTypeCode;

    private DocGenVendorFixture(int vendorHeaderId, int vendorDetailId, String vendorName, String vendorTypeCode,
            String payeeTypeCode) {
        this.vendorHeaderId = vendorHeaderId;
        this.vendorDetailId = vendorDetailId;
        this.vendorName = vendorName;
        this.vendorTypeCode = vendorTypeCode;
        this.payeeTypeCode = payeeTypeCode;
    }

    public String getVendorNumber() {
        return vendorHeaderId + "-" + vendorDetailId;
    }

    public VendorHeader createVendorHeader() {
        VendorHeader vendorHeader = new VendorHeader();
        vendorHeader.setVendorHeaderGeneratedIdentifier(vendorHeaderId);
        vendorHeader.setVendorTypeCode(vendorTypeCode);
        return vendorHeader;
    }

    public VendorDetail createVendorDetail() {
        VendorDetail vendorDetail = new VendorDetail();
        vendorDetail.setVendorHeader(createVendorHeader());
        vendorDetail.setVendorHeaderGeneratedIdentifier(vendorHeaderId);
        vendorDetail.setVendorDetailAssignedIdentifier(vendorDetailId);
        vendorDetail.setVendorName(vendorName);
        return vendorDetail;
    }

}
