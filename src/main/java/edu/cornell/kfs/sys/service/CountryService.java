package edu.cornell.kfs.sys.service;

import org.kuali.kfs.sys.businessobject.Country;

/**
 * CU Generic ISO-FIPS Country modification
 */
public interface CountryService {

    public boolean isCountryActive(String countryCode);

    public Country getByPrimaryId(String countryCode);

}
