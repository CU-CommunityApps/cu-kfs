package edu.cornell.kfs.module.purap.batch.service.impl.fixture;

import java.util.List;

import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerLegalStructure;
import edu.cornell.kfs.sys.fixture.XmlDocumentFixtureUtils;

public enum JaggaerVendorDetailFixture {
    ACME_ITHACA (true, 1234, 0, "Acme primary", "US", JaggaerLegalStructure.C_CORPORATION.kfsOwnerShipTypeCode, "http://www.google.com",
            buildAddressList(JaggaerVendorAddressFixture.ITHACA, JaggaerVendorAddressFixture.INACTIVE),
            buildStringArray(JaggaerVendorAddressFixture.ITHACA.addressId.toString(), JaggaerVendorAddressFixture.INACTIVE.addressId.toString())),
    ACME_TRANSNATIONAL (true, 1234, 1, "Acme Secondary", "US", JaggaerLegalStructure.C_CORPORATION.kfsOwnerShipTypeCode, "https://www.yahoo.com",
            buildAddressList(JaggaerVendorAddressFixture.ITHACA, JaggaerVendorAddressFixture.PLAYACAR),
            buildStringArray(JaggaerVendorAddressFixture.ITHACA.addressId.toString(), JaggaerVendorAddressFixture.PLAYACAR.addressId.toString())),
    ACME_NO_ADDRESS (true, 456, 0, "Acme no address", "US", JaggaerLegalStructure.C_CORPORATION.kfsOwnerShipTypeCode, "https://www.google.com",
            buildAddressList(),
            buildStringArray());
    
    public final boolean active;
    public final Integer generatedIdentifier;
    public final Integer assignedIdentifier;
    public final String name;
    public final String countryOfOrigin;
    public final String ownershipCode;
    public final String url;
    public final List<JaggaerVendorAddressFixture> addresses;
    public final List<String> expectedVendorAdressIds;
    
    private JaggaerVendorDetailFixture(boolean active, Integer generatedIdentifier, Integer assignedIdentifier,
            String name, String countryOfOrigin, String ownershipCode, String url, JaggaerVendorAddressFixture[] addresses, String[] expectedVendorAdressIds) {
        this.active = active;
        this.generatedIdentifier = generatedIdentifier;
        this.assignedIdentifier = assignedIdentifier;
        this.name = name;
        this.countryOfOrigin = countryOfOrigin;
        this.ownershipCode = ownershipCode;
        this.url = url;
        this.addresses = XmlDocumentFixtureUtils.toImmutableList(addresses);
        this.expectedVendorAdressIds = XmlDocumentFixtureUtils.toImmutableList(expectedVendorAdressIds);
    }
    
    private static JaggaerVendorAddressFixture[] buildAddressList(JaggaerVendorAddressFixture... fixtures) {
        return fixtures;
    }
    
    private static String[] buildStringArray(String... values) {
        return values;
    }
    
    public VendorDetail toVendorDetail() {
        VendorDetail detail = new VendorDetail();
        detail.setActiveIndicator(active);
        detail.setVendorHeaderGeneratedIdentifier(generatedIdentifier);
        detail.setVendorDetailAssignedIdentifier(assignedIdentifier);
        detail.setVendorName(name);
        
        VendorHeader header = new VendorHeader();
        header.setVendorCorpCitizenCode(countryOfOrigin);
        header.setVendorOwnershipCode(ownershipCode);
        detail.setVendorHeader(header);
        detail.setVendorUrlAddress(url);
        
        for (JaggaerVendorAddressFixture address : addresses) {
            detail.getVendorAddresses().add(address.toVendorAddress());
        }
        
        return detail;
    }
    
}
