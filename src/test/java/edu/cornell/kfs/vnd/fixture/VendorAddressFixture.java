package edu.cornell.kfs.vnd.fixture;


import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDefaultAddress;

import java.util.ArrayList;
import java.util.List;

import static edu.cornell.kfs.vnd.fixture.VendorDefaultAddressFixture.campusIN;
import static edu.cornell.kfs.vnd.fixture.VendorDefaultAddressFixture.campusKO;
import static edu.cornell.kfs.vnd.fixture.VendorDefaultAddressFixture.campusSB;

public enum VendorAddressFixture {

    address1(1, "PO", "line1", "line2", "thisCity", "IN", "44444", "US", "attentionTo", "", "knoreceipt-l@indiana.edu",
            "", "555-555-5555", true, true, null),
    address2(2, "PO", "line1", "line2", "thisCity", "IN", "44444", "US", "attentionTo", "", "knoreceipt-l@indiana.edu",
            "", "555-555-5555", false, true, new VendorDefaultAddressFixture[]{campusKO, campusIN}),
    address3(3, "RM", "line1", "line2", "thisCity", "IN", "44444", "US", "attentionTo", "", "knoreceipt-l@indiana.edu",
            "", "555-555-5555", true, true, null),
    address4(4, "RM", "line1", "line2", "thisCity", "IN", "44444", "US", "attentionTo", "", "knoreceipt-l@indiana.edu",
            "", "555-555-5555", false, false, new VendorDefaultAddressFixture[]{campusSB}),
    address5(5, "PO", "line1", "line2", "thisCity", "IN", "44444", "US", "attentionTo", "", "knoreceipt-l@indiana.edu",
            "", "555-555-5555", false, true, null),
    address6(6, "PO", "line1", "line2", "thisCity", "IN", "44444", "US", "attentionTo", "", "knoreceipt-l@indiana.edu",
            "", "555-555-5555", true, true, new VendorDefaultAddressFixture[]{campusKO, campusIN}),
    address7(7, "RM", "line1", "line2", "thisCity", "IN", "44444", "US", "attentionTo", "", "knoreceipt-l@indiana.edu",
            "", "555-555-5555", true, true, new VendorDefaultAddressFixture[]{campusSB});

    public final Integer vendorAddressGeneratedIdentifier;
    public final String vendorAddressTypeCode;
    public final String vendorLine1Address;
    public final String vendorLine2Address;
    public final String vendorCityName;
    public final String vendorStateCode;
    public final String vendorZipCode;
    public final String vendorCountryCode;
    public final String vendorAttentionName;
    public final String vendorAddressInternationalProvinceName;
    public final String vendorAddressEmailAddress;
    public final String vendorBusinessToBusinessUrlAddress;
    public final String vendorFaxNumber;
    public final boolean vendorDefaultAddressIndicator;
    public final boolean active;
    public final List<VendorDefaultAddress> defaultAddresses = new ArrayList<>();

    VendorAddressFixture(
            final Integer vendorAddressGeneratedIdentifier, final String vendorAddressTypeCode,
            final String vendorLine1Address, final String vendorLine2Address, final String vendorCityName, final String vendorStateCode,
            final String vendorZipCode, final String vendorCountryCode, final String vendorAttentionName,
            final String vendorAddressInternationalProvinceName, final String vendorAddressEmailAddress,
            final String vendorBusinessToBusinessUrlAddress, final String vendorFaxNumber, final boolean vendorDefaultAddressIndicator,
            final boolean active, final VendorDefaultAddressFixture[] campuses) {
        this.vendorAddressGeneratedIdentifier = vendorAddressGeneratedIdentifier;
        this.vendorAddressTypeCode = vendorAddressTypeCode;
        this.vendorLine1Address = vendorLine1Address;
        this.vendorLine2Address = vendorLine2Address;
        this.vendorCityName = vendorCityName;
        this.vendorStateCode = vendorStateCode;
        this.vendorZipCode = vendorZipCode;
        this.vendorCountryCode = vendorCountryCode;
        this.vendorAttentionName = vendorAttentionName;
        this.vendorAddressInternationalProvinceName = vendorAddressInternationalProvinceName;
        this.vendorAddressEmailAddress = vendorAddressEmailAddress;
        this.vendorBusinessToBusinessUrlAddress = vendorBusinessToBusinessUrlAddress;
        this.vendorFaxNumber = vendorFaxNumber;
        this.vendorDefaultAddressIndicator = vendorDefaultAddressIndicator;
        this.active = active;
        if (campuses != null) {
            for (final VendorDefaultAddressFixture campus : campuses) {
                final VendorDefaultAddress vda = new VendorDefaultAddress();
                vda.setVendorCampusCode(campus.vendorCampusCode);
                vda.setActive(campus.active);
                defaultAddresses.add(vda);
            }
        }
    }

    public VendorAddress createAddress() {
        final VendorAddress address = new VendorAddress();
        address.setVendorAddressGeneratedIdentifier(vendorAddressGeneratedIdentifier);
        address.setVendorAddressTypeCode(vendorAddressTypeCode);
        address.setVendorLine1Address(vendorLine1Address);
        address.setVendorLine2Address(vendorLine2Address);
        address.setVendorCityName(vendorCityName);
        address.setVendorStateCode(vendorStateCode);
        address.setVendorZipCode(vendorZipCode);
        address.setVendorCountryCode(vendorCountryCode);
        address.setVendorAttentionName(vendorAttentionName);
        address.setVendorAddressInternationalProvinceName(vendorAddressInternationalProvinceName);
        address.setVendorAddressEmailAddress(vendorAddressEmailAddress);
        address.setVendorBusinessToBusinessUrlAddress(vendorBusinessToBusinessUrlAddress);
        address.setVendorFaxNumber(vendorFaxNumber);
        address.setVendorDefaultAddressIndicator(vendorDefaultAddressIndicator);
        address.setVendorDefaultAddresses(defaultAddresses);
        address.setActive(active);
        return address;
    }
}
