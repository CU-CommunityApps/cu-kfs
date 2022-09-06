package edu.cornell.kfs.sys.service;

/**
 * CU Generic ISO-FIPS Country modification
 */
public interface ISOCountryService {

    public boolean isISOCountryActive(String isoCountryCode);

    public boolean isISOCountryInactive(String isoCountryCode);

}
