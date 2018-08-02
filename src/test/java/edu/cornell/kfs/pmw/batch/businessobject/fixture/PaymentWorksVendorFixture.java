package edu.cornell.kfs.pmw.batch.businessobject.fixture;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants.PaymentWorksUploadFileColumn;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;

public enum PaymentWorksVendorFixture {
    JOHN_DOE("13579999", 1050, 0, "Doe, John", "102 Main St.", KFSConstants.EMPTY_STRING,
            "SomeTown", "NY", "US", "11111", "111223333", "johndoe@somewhere.com"),
    MARY_SMITH("12345678", 2333, 0, "Smith, Mary", "333 Rocky Rd.", KFSConstants.EMPTY_STRING,
            "Ithaca", "NY", "US", "24680", "666666666", "marysmith@unknownsite.net"),
    WIDGET_MAKERS("55555555", 4160, 0, "Widget Makers", "Division of Financial Affairs", "1200 Long Dr.",
            "Miami", "FL", "US", "17171", "123443211", "findept@widgetmakers.org");

    public final String pmwVendorRequestId;
    public final Integer kfsVendorHeaderGeneratedIdentifier;
    public final Integer kfsVendorDetailAssignedIdentifier;
    public final String requestingCompanyLegalName;
    public final String remittanceAddressStreet1;
    public final String remittanceAddressStreet2;
    public final String remittanceAddressCity;
    public final String remittanceAddressState;
    public final String remittanceAddressCountry;
    public final String remittanceAddressZipCode;
    public final String requestingCompanyTin;
    public final String vendorInformationEmail;

    private PaymentWorksVendorFixture(String pmwVendorRequestId, int kfsVendorHeaderGeneratedIdentifier, int kfsVendorDetailAssignedIdentifier,
            String requestingCompanyLegalName, String remittanceAddressStreet1, String remittanceAddressStreet2,
            String remittanceAddressCity, String remittanceAddressState, String remittanceAddressCountry,
            String remittanceAddressZipCode, String requestingCompanyTin, String vendorInformationEmail) {
        this.pmwVendorRequestId = pmwVendorRequestId;
        this.kfsVendorHeaderGeneratedIdentifier = Integer.valueOf(kfsVendorHeaderGeneratedIdentifier);
        this.kfsVendorDetailAssignedIdentifier = Integer.valueOf(kfsVendorDetailAssignedIdentifier);
        this.requestingCompanyLegalName = requestingCompanyLegalName;
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
        vendor.setPmwVendorRequestId(pmwVendorRequestId);
        vendor.setKfsVendorHeaderGeneratedIdentifier(kfsVendorHeaderGeneratedIdentifier);
        vendor.setKfsVendorDetailAssignedIdentifier(kfsVendorDetailAssignedIdentifier);
        vendor.setRequestingCompanyLegalName(requestingCompanyLegalName);
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

    public String[] toParsedCsvFieldArray() {
        return Arrays.stream(PaymentWorksUploadFileColumn.values())
                .map(PaymentWorksUploadFileColumn::getPmwVendorPropertyName)
                .map(this::getFixtureFieldAsString)
                .toArray(String[]::new);
    }

    private String getFixtureFieldAsString(String fieldName) {
        try {
            Field fixtureField = PaymentWorksVendorFixture.class.getField(fieldName);
            Object fieldValue = fixtureField.get(this);
            String stringValue = (fieldValue != null) ? fieldValue.toString() : null;
            return StringUtils.defaultIfBlank(stringValue, KFSConstants.EMPTY_STRING);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
