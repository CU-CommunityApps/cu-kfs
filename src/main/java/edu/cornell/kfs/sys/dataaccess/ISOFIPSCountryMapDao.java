package edu.cornell.kfs.sys.dataaccess;

import java.util.List;

import edu.cornell.kfs.sys.businessobject.ISOFIPSCountryMap;

public interface ISOFIPSCountryMapDao {

    public List<ISOFIPSCountryMap> findActiveFipsCountryCodes(String isoCountryCode);

    public List<ISOFIPSCountryMap> findActiveIsoCountryCodes(String fipsCountryCode);

}
