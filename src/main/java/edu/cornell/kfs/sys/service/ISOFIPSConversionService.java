package edu.cornell.kfs.sys.service;

/**
 * CU Generic ISO-FIPS Country modification
 */
public interface ISOFIPSConversionService {

    public String convertISOCountryCodeToActiveFIPSCountryCode(String isoCountryCode);

    public String convertFIPSCountryCodeToActiveISOCountryCode(String fipsCountryCode);

}
