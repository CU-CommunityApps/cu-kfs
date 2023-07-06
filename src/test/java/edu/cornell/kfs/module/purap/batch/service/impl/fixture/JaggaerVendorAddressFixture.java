package edu.cornell.kfs.module.purap.batch.service.impl.fixture;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.vnd.businessobject.VendorAddress;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerAddressTypeForXml;

public enum JaggaerVendorAddressFixture {
    
    ITHACA(true, 101, JaggaerAddressTypeForXml.FULFILLMENT.kfsAddressType, "US", "120 Maple Ave", "Room 666", "Ithaca", "NY", StringUtils.EMPTY, "14850"),
    PLAYACAR(true, 102, JaggaerAddressTypeForXml.FULFILLMENT.kfsAddressType, "MX", "AV Xaman-Ha Mza 9 Y", "10 Lote 1 Fase II,", "Playacar", StringUtils.EMPTY, "QR", "77710"),
    INACTIVE(false, 103, StringUtils.EMPTY, StringUtils.EMPTY,  StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY);
    
    public final boolean active;
    public final Integer addressId;
    public final String addressType;
    public final String countryCode;
    public final String addressLine1;
    public final String addressLine2;
    public final String city;
    public final String state;
    public final String internationalState;
    public final String zip;
    
    private JaggaerVendorAddressFixture(boolean active, Integer addressId, String addressType, String countryCode,
            String addressLine1, String addressLine2, String city, String state, String internationalState,
            String zip) {
        this.active = active;
        this.addressId = addressId;
        this.addressType = addressType;
        this.countryCode = countryCode;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.state = state;
        this.internationalState = internationalState;
        this.zip = zip;
    }
    
    public VendorAddress toVendorAddress() {
        VendorAddress address = new VendorAddress();
        address.setActive(active);
        address.setVendorAddressGeneratedIdentifier(addressId);
        address.setVendorAddressTypeCode(addressType);
        address.setVendorCountryCode(countryCode);
        address.setVendorLine1Address(addressLine1);
        address.setVendorLine2Address(addressLine2);
        address.setVendorCityName(city);
        address.setVendorStateCode(state);
        address.setVendorAddressInternationalProvinceName(internationalState);
        address.setVendorZipCode(zip);
        return address;
    }
    
    
}
