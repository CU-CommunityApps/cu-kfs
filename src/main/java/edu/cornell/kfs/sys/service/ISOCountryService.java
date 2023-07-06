package edu.cornell.kfs.sys.service;

import java.util.List;

/**
 * CU Generic ISO-FIPS Country modification
 */
public interface ISOCountryService {

    public boolean isISOCountryActive(String isoCountryCode);

    public boolean isISOCountryInactive(String isoCountryCode);

    public String findISOCountryNameByCountryCode(String isoCountryCode);

    public List<String> findISOCountryCodesByCountryName(String isoCountryName);

    public boolean isoCountryExists(String isoCountryCode);
}
