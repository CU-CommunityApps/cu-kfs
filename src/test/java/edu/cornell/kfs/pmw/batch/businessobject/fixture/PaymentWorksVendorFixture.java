package edu.cornell.kfs.pmw.batch.businessobject.fixture;

import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;

public enum PaymentWorksVendorFixture {
    JOHN_DOE("aabbccddeeff", 1050, 0, "John Doe", "102 Main St.", KFSConstants.EMPTY_STRING,
            "SomeTown", "NY", "US", "11111", "111223333", "johndoe@somewhere.com");

    public final String vendorRequestId;
    public final Integer vendorHeaderGeneratedIdentifier;
    public final Integer vendorDetailAssignedIdentifier;
    public final String requestingCompanyName;
    public final String remittanceAddressStreet1;
    public final String remittanceAddressStreet2;
    public final String remittanceAddressCity;
    public final String remittanceAddressState;
    public final String remittanceAddressCountry;
    public final String remittanceAddressZipCode;
    public final String requestingCompanyTin;
    public final String vendorInformationEmail;

    private PaymentWorksVendorFixture(String vendorRequestId, int vendorHeaderGeneratedIdentifier, int vendorDetailAssignedIdentifier,
            String requestingCompanyName, String remittanceAddressStreet1, String remittanceAddressStreet2,
            String remittanceAddressCity, String remittanceAddressState, String remittanceAddressCountry,
            String remittanceAddressZipCode, String requestingCompanyTin, String vendorInformationEmail) {
        this.vendorRequestId = vendorRequestId;
        this.vendorHeaderGeneratedIdentifier = Integer.valueOf(vendorHeaderGeneratedIdentifier);
        this.vendorDetailAssignedIdentifier = Integer.valueOf(vendorDetailAssignedIdentifier);
        this.requestingCompanyName = requestingCompanyName;
        this.remittanceAddressStreet1 = remittanceAddressStreet1;
        this.remittanceAddressStreet2 = remittanceAddressStreet2;
        this.remittanceAddressCity = remittanceAddressCity;
        this.remittanceAddressState = remittanceAddressState;
        this.remittanceAddressCountry = remittanceAddressCountry;
        this.remittanceAddressZipCode = remittanceAddressZipCode;
        this.requestingCompanyTin = requestingCompanyTin;
        this.vendorInformationEmail = vendorInformationEmail;
    }

    public PaymentWorksVendor toPaymentWorksVendor() {
        PaymentWorksVendor vendor = new PaymentWorksVendor();
        vendor.setPmwVendorRequestId(vendorRequestId);
        vendor.setKfsVendorHeaderGeneratedIdentifier(vendorHeaderGeneratedIdentifier);
        vendor.setKfsVendorDetailAssignedIdentifier(vendorDetailAssignedIdentifier);
        vendor.setRequestingCompanyName(requestingCompanyName);
        vendor.setRemittanceAddressStreet1(remittanceAddressStreet1);
        vendor.setRemittanceAddressStreet2(remittanceAddressStreet2);
        vendor.setRemittanceAddressCity(remittanceAddressCity);
        vendor.setRemittanceAddressState(remittanceAddressState);
        vendor.setRemittanceAddressCountry(remittanceAddressCountry);
        vendor.setRemittanceAddressZipCode(remittanceAddressZipCode);
        vendor.setRequestingCompanyTin(requestingCompanyTin);
        vendor.setVendorInformationEmail(vendorInformationEmail);
        return vendor;
    }

}
