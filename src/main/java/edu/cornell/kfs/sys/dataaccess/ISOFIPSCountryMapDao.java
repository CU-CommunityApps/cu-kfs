package edu.cornell.kfs.sys.dataaccess;

import java.util.List;

import edu.cornell.kfs.sys.businessobject.ISOFIPSCountryMap;

/**
 * CU Generic ISO-FIPS Country modification
 */
public interface ISOFIPSCountryMapDao {

    public List<ISOFIPSCountryMap> findActiveFipsCountryCodes(String isoCountryCode);

    public List<ISOFIPSCountryMap> findActiveIsoCountryCodes(String fipsCountryCode);

}
