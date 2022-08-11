package edu.cornell.kfs.sys.service;

import java.util.List;

import edu.cornell.kfs.sys.businessobject.ISOFIPSCountryMap;

public interface ISOFIPSConversionService {

    public String convertISOCountryCodeToActiveFIPSCountryCode(String isoCountryCode);

    public String convertFIPSCountryCodeToActiveISOCountryCode(String fipsCountryCode);

    public List<ISOFIPSCountryMap> findManyActiveISOCountryCodesForFIPSCode(String fipsCountryCode);

    public List<ISOFIPSCountryMap> findManyActiveFIPSCountryCodesForISOCode(String isoCountryCode);

}