package edu.cornell.kfs.sys.dataaccess;

import java.util.List;

import edu.cornell.kfs.sys.businessobject.ISOFIPSCountryMap;


public interface ISOFIPSCountryMapDao {
    
    List<ISOFIPSCountryMap> findFipsCountries(String isoCountryCode);
    
    List<ISOFIPSCountryMap> findIsoCountries(String fipsCountryCode);
    
}
