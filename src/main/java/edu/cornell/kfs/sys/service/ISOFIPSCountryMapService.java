package edu.cornell.kfs.sys.service;

import java.util.List;

import edu.cornell.kfs.sys.businessobject.ISOFIPSCountryMap;

/**
 * CU Generic ISO-FIPS Country modification
 */
public interface ISOFIPSCountryMapService {

    public List<ISOFIPSCountryMap> findActiveMapsByISOCountryId(String isoCountryCode);

    public List<ISOFIPSCountryMap> findActiveMapsByFIPSCountryId(String fipsCountryCode);

}
