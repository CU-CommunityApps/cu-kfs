package edu.cornell.kfs.sys.service.impl.fixture;

import java.util.stream.Stream;

/**
 * CU Generic ISO-FIPS Country modification
 */
public enum CountryFixture {
    
    FIPS_ACTIVE("FIPS Active", "FA", "", true),
    FIPS_INACTIVE("FIPS Inactive", "FI", "", false),
    FIPS_MISSING("", "", "", false);

    public final String name;
    public final String code;
    public final String alternateCode;
    public final boolean active;
    
    private CountryFixture(String name, String code, String alternateCode, boolean active) {
        this.name = name;
        this.code = code;
        this.alternateCode = alternateCode;
        this.active = active;
    }
    
    public static Stream<CountryFixture> mockCountryTable() {
        return Stream.of(CountryFixture.values());
    }
    
    public boolean isCountryInactive() {
        return !active;
    }
}
