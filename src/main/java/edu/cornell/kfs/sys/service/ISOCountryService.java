package edu.cornell.kfs.sys.service;

import edu.cornell.kfs.sys.businessobject.ISOCountry;

/**
 * CU Generic ISO-FIPS Country modification
 */
public interface ISOCountryService {

    public boolean isISOCountryActive(String isoCountryCode);

    public ISOCountry getByPrimaryId(String isoCountryCode);

}
