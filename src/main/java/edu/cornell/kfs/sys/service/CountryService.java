package edu.cornell.kfs.sys.service;

/**
 * CU Generic ISO-FIPS Country modification
 */
public interface CountryService {

    public boolean isCountryActive(String countryCode);

    public boolean isCountryInactive(String countryCode);

}
