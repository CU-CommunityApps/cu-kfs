package edu.cornell.kfs.sys.service;

import java.util.List;

/**
 * CU Generic ISO-FIPS Country modification
 */
public interface CountryService {

    public boolean isCountryActive(String countryCode);

    public boolean isCountryInactive(String countryCode);

    public String findCountryNameByCountryCode(String countryCode);

    public List<String> findCountryCodesByCountryName(String countryName);

    public boolean countryExists(String countryCode);
}
