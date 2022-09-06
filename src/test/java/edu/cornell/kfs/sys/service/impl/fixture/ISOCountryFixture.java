package edu.cornell.kfs.sys.service.impl.fixture;

import java.util.stream.Stream;

/**
 * CU Generic ISO-FIPS Country modification
 */
public enum ISOCountryFixture {
    
    ISO_ACTIVE("ISO Active", "IA", "", true),
    ISO_INACTIVE("ISO Inactive", "II", "", false),
    ISO_MISSING("", "", "", false);
    
    public final String name;
    public final String code;
    public final String alternateCode;
    public final boolean active;
    
    private ISOCountryFixture (String name, String code, String alternateCode, boolean active) {
        this.name = name;
        this.code = code;
        this.alternateCode = alternateCode;
        this.active = active;
    }
    
    public static Stream<ISOCountryFixture> mockISOCountryTable() {
        return Stream.of(ISOCountryFixture.values());
    }
    
    public boolean isISOCountryInactive() {
        return !active;
    }
}
