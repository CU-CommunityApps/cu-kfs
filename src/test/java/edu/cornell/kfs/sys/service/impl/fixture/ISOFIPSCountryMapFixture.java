package edu.cornell.kfs.sys.service.impl.fixture;

import java.util.stream.Stream;

/**
 * CU Generic ISO-FIPS Country modification
 */
public enum  ISOFIPSCountryMapFixture {
    
    ALL_ACTIVE(ISOCountryFixture.ISO_ACTIVE.code, CountryFixture.FIPS_ACTIVE.code, true, CountryFixture.FIPS_ACTIVE, ISOCountryFixture.ISO_ACTIVE),
    FIPS_INACTIVE_ISO_ACTIVE_MAP_ACTIVE(ISOCountryFixture.ISO_ACTIVE.code, CountryFixture.FIPS_INACTIVE.code, true, CountryFixture.FIPS_INACTIVE, ISOCountryFixture.ISO_ACTIVE),
    FIPS_ACTIVE_ISO_INACTIVE_MAP_ACTIVE(ISOCountryFixture.ISO_INACTIVE.code, CountryFixture.FIPS_ACTIVE.code, true, CountryFixture.FIPS_ACTIVE, ISOCountryFixture.ISO_INACTIVE),
    FIPS_INACTIVE_ISO_ACTIVE_MAP_INACTIVE(ISOCountryFixture.ISO_ACTIVE.code, CountryFixture.FIPS_INACTIVE.code, false, CountryFixture.FIPS_INACTIVE, ISOCountryFixture.ISO_ACTIVE),
    FIPS_ACTIVE_ISO_INACTIVE_MAP_INACTIVE(ISOCountryFixture.ISO_INACTIVE.code, CountryFixture.FIPS_ACTIVE.code, false, CountryFixture.FIPS_ACTIVE, ISOCountryFixture.ISO_INACTIVE);
    
    public final String isoCountryCode;
    public final String fipsCountryCode;
    public final boolean active;
    public final CountryFixture fipsCountryFixture;
    public final ISOCountryFixture isoCountryFixture;
    
    private ISOFIPSCountryMapFixture(String isoCountryCode, String fipsCountryCode, boolean active, 
            CountryFixture fipsCountryFixture, ISOCountryFixture isoCountryFixture) {
        this.isoCountryCode = isoCountryCode;
        this.fipsCountryCode = fipsCountryCode;
        this.active = active;
        this.fipsCountryFixture = fipsCountryFixture;
        this.isoCountryFixture = isoCountryFixture;
    }
    
    public static Stream<ISOFIPSCountryMapFixture> mockISOFIPSCountryMapTable() {
        return Stream.of(ISOFIPSCountryMapFixture.values());
    }
    
    public boolean isActive() {
        return active;
    }

}
