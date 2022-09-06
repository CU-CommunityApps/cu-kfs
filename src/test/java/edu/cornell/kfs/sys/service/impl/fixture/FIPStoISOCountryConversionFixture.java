package edu.cornell.kfs.sys.service.impl.fixture;

/**
 * CU Generic ISO-FIPS Country modification
 */
public enum  FIPStoISOCountryConversionFixture {
    
    ALL_ACTIVE_TEST("ALL_ACTIVE_TEST", "PASS", ISOFIPSCountryMapFixture.ALL_ACTIVE.fipsCountryCode, ISOFIPSCountryMapFixture.ALL_ACTIVE),
    FIPS_INACTIVE_ISO_ACTIVE_MAP_ACTIVE_TEST("FIPS_INACTIVE_ISO_ACTIVE_MAP_ACTIVE_TEST", "THROWS", ISOFIPSCountryMapFixture.FIPS_INACTIVE_ISO_ACTIVE_MAP_ACTIVE.fipsCountryCode, ISOFIPSCountryMapFixture.FIPS_INACTIVE_ISO_ACTIVE_MAP_ACTIVE),
    FIPS_ACTIVE_ISO_INACTIVE_MAP_ACTIVE_TEST("FIPS_ACTIVE_ISO_INACTIVE_MAP_ACTIVE_TEST", "FAIL", ISOFIPSCountryMapFixture.FIPS_ACTIVE_ISO_INACTIVE_MAP_ACTIVE.fipsCountryCode, ISOFIPSCountryMapFixture.FIPS_ACTIVE_ISO_INACTIVE_MAP_ACTIVE),
    FIPS_INACTIVE_ISO_ACTIVE_MAP_INACTIVE_TEST("FIPS_INACTIVE_ISO_ACTIVE_MAP_INACTIVE_TEST", "THROWS", ISOFIPSCountryMapFixture.FIPS_INACTIVE_ISO_ACTIVE_MAP_INACTIVE.fipsCountryCode, ISOFIPSCountryMapFixture.FIPS_INACTIVE_ISO_ACTIVE_MAP_INACTIVE),
    FIPS_ACTIVE_ISO_INACTIVE_MAP_INACTIVE_TEST("FIPS_ACTIVE_ISO_INACTIVE_MAP_INACTIVE_TEST", "FAIL", ISOFIPSCountryMapFixture.FIPS_ACTIVE_ISO_INACTIVE_MAP_INACTIVE.fipsCountryCode, ISOFIPSCountryMapFixture.FIPS_ACTIVE_ISO_INACTIVE_MAP_INACTIVE);
    
    public final String typeOfTest;
    public final String testShouldPass;
    public final String fipsCountryCodeToSearchFor;
    public final ISOFIPSCountryMapFixture isoFipsCountryMapFixture;
        
    private FIPStoISOCountryConversionFixture(String typeOfTest, String testShouldPass,
            String fipsCountryCodeToSearchFor, ISOFIPSCountryMapFixture isoFipsCountryMapFixture) { 
        this.typeOfTest = typeOfTest;
        this.testShouldPass = testShouldPass;
        this.fipsCountryCodeToSearchFor = fipsCountryCodeToSearchFor;
        this.isoFipsCountryMapFixture = isoFipsCountryMapFixture;
    }
    
}
