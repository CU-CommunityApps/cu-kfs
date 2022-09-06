package edu.cornell.kfs.sys.service.impl.fixture;

/**
 * CU Generic ISO-FIPS Country modification
 */
public enum  ISOtoFIPSCountryConversionFixture {
    
    ALL_ACTIVE_TEST("ALL_ACTIVE_TEST", "PASS", ISOFIPSCountryMapFixture.ALL_ACTIVE.isoCountryCode, ISOFIPSCountryMapFixture.ALL_ACTIVE),
    FIPS_INACTIVE_ISO_ACTIVE_MAP_ACTIVE_TEST("FIPS_INACTIVE_ISO_ACTIVE_MAP_ACTIVE_TEST", "FAIL", ISOFIPSCountryMapFixture.FIPS_INACTIVE_ISO_ACTIVE_MAP_ACTIVE.isoCountryCode, ISOFIPSCountryMapFixture.FIPS_INACTIVE_ISO_ACTIVE_MAP_ACTIVE),
    FIPS_ACTIVE_ISO_INACTIVE_MAP_ACTIVE_TEST("FIPS_ACTIVE_ISO_INACTIVE_MAP_ACTIVE_TEST", "THROWS", ISOFIPSCountryMapFixture.FIPS_ACTIVE_ISO_INACTIVE_MAP_ACTIVE.isoCountryCode, ISOFIPSCountryMapFixture.FIPS_ACTIVE_ISO_INACTIVE_MAP_ACTIVE),
    FIPS_INACTIVE_ISO_ACTIVE_MAP_INACTIVE_TEST("FIPS_INACTIVE_ISO_ACTIVE_MAP_INACTIVE_TEST", "FAIL", ISOFIPSCountryMapFixture.FIPS_INACTIVE_ISO_ACTIVE_MAP_INACTIVE.isoCountryCode, ISOFIPSCountryMapFixture.FIPS_INACTIVE_ISO_ACTIVE_MAP_INACTIVE),
    FIPS_ACTIVE_ISO_INACTIVE_MAP_INACTIVE_TEST("FIPS_ACTIVE_ISO_INACTIVE_MAP_INACTIVE_TEST", "THROWS", ISOFIPSCountryMapFixture.FIPS_ACTIVE_ISO_INACTIVE_MAP_INACTIVE.isoCountryCode, ISOFIPSCountryMapFixture.FIPS_ACTIVE_ISO_INACTIVE_MAP_INACTIVE);
    
    public final String typeOfTest;
    public final String testShouldPass;
    public final String isoCountryCodeToSearchFor;
    public final ISOFIPSCountryMapFixture isoFipsCountryMapFixture;
        
    private ISOtoFIPSCountryConversionFixture(String typeOfTest, String testShouldPass,
            String isoCountryCodeToSearchFor, ISOFIPSCountryMapFixture isoFipsCountryMapFixture) { 
        this.typeOfTest = typeOfTest;
        this.testShouldPass = testShouldPass;
        this.isoCountryCodeToSearchFor = isoCountryCodeToSearchFor;
        this.isoFipsCountryMapFixture = isoFipsCountryMapFixture;
    }
    
}
