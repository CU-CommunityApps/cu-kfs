package edu.cornell.kfs.module.purap.batch.service.impl.fixcture;

import org.apache.commons.lang.StringUtils;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerAddressType;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractPartyUploadRowType;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractAddressUploadDto;

public enum VendorAddressFixture {
    BASIC_VENDOR_ADDRESS_1(VendorFixture.BASIC_VENDOR.ERPNumber, "12342", JaggaerAddressType.FULFILLMENT, "123 Main Street",
            "Apartment 666", "Freeville", "NY", "13068"),
    BASIC_VENDOR_ADDRESS_2(VendorFixture.BASIC_VENDOR.ERPNumber, "98765", JaggaerAddressType.REMIT, "45 Palm Street",
            "Apartment 1F", "Ithaca", "NY", "14850"),
    FULL_VENDOR_FULL_ADDRSS(JaggaerContractPartyUploadRowType.ADDRESS, StringUtils.EMPTY, "Active", VendorFixture.FULL_VENOOR.ERPNumber,
            "addressId", "ABCD", JaggaerAddressType.FULFILLMENT, "primary type", "US", "street line 1", "street line 2", "street line 3",
            "city", "NY", "14850", "6072559900", "18002809900", "fax", StringUtils.EMPTY),
    FULL_VENDOR_FULL_ADDRSS_FOR_CSV(JaggaerContractPartyUploadRowType.ADDRESS, "vendor address sciquest id", "Active", VendorFixture.FULL_VENOOR_FOR_CSV.ERPNumber,
            "666666666", "Yankees Fan Company", JaggaerAddressType.FULFILLMENT, "primary type", "US", "street line 1", "street line 2", "street line 3",
            "city", "NY", "14850", "6072559900", "18002809900", "fax", "super awesome cool note");

    public final JaggaerContractPartyUploadRowType rowType;
    public final String sciQuestID;
    public final String active;
    public final String ERPNumber;
    public final String addressID;
    public final String name;
    public final JaggaerAddressType addressType;
    public final String primaryType;
    public final String country;
    public final String streetLine1;
    public final String streetLine2;
    public final String streetLine3;
    public final String city;
    public final String state;
    public final String postalCode;
    public final String phone;
    public final String tollFreeNumber;
    public final String fax;
    public final String notes;

    private VendorAddressFixture(String eRPNumber, String addressID, JaggaerAddressType addressType, String streetLine1,
            String streetLine2, String city, String state, String postalCode) {
        this(JaggaerContractPartyUploadRowType.ADDRESS, StringUtils.EMPTY, StringUtils.EMPTY, eRPNumber, addressID,
                StringUtils.EMPTY, addressType, StringUtils.EMPTY, "US", streetLine1, streetLine2, StringUtils.EMPTY,
                city, state, postalCode, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY);

    }

    private VendorAddressFixture(JaggaerContractPartyUploadRowType rowType, String sciQuestID, String active,
            String eRPNumber, String addressID, String name, JaggaerAddressType addressType, String primaryType,
            String country, String streetLine1, String streetLine2, String streetLine3, String city, String state,
            String postalCode, String phone, String tollFreeNumber, String fax, String notes) {
        this.rowType = rowType;
        this.sciQuestID = sciQuestID;
        this.active = active;
        ERPNumber = eRPNumber;
        this.addressID = addressID;
        this.name = name;
        this.addressType = addressType;
        this.primaryType = primaryType;
        this.country = country;
        this.streetLine1 = streetLine1;
        this.streetLine2 = streetLine2;
        this.streetLine3 = streetLine3;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.phone = phone;
        this.tollFreeNumber = tollFreeNumber;
        this.fax = fax;
        this.notes = notes;
    }

    public JaggaerContractAddressUploadDto toJaggaerContractAddressUploadDto() {
        JaggaerContractAddressUploadDto dto = new JaggaerContractAddressUploadDto();
        dto.setRowType(rowType);
        dto.setSciQuestID(sciQuestID);
        dto.setActive(active);
        dto.setERPNumber(ERPNumber);
        dto.setAddressID(addressID);
        dto.setName(name);
        dto.setAddressType(addressType);
        dto.setPrimaryType(primaryType);
        dto.setCountry(country);
        dto.setStreetLine1(streetLine1);
        dto.setStreetLine2(streetLine2);
        dto.setStreetLine3(streetLine3);
        dto.setCity(city);
        dto.setState(state);
        dto.setPostalCode(postalCode);
        dto.setPhone(phone);
        dto.setTollFreeNumber(tollFreeNumber);
        dto.setFax(fax);
        dto.setNotes(notes);
        return dto;
    }
}
