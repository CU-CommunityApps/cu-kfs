package edu.cornell.kfs.vnd.fixture;

public enum VendorDefaultAddressFixture {

    campusBL("BL", false),
    campusKO("KO", false),
    campusIN("IN", true),
    campusSB("SB", true),;

    public final String vendorCampusCode;
    public final boolean active;

    VendorDefaultAddressFixture(final String vendorCampusCode, final boolean active) {
        this.vendorCampusCode = vendorCampusCode;
        this.active = active;
    }
}
