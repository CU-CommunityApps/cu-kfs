package edu.cornell.kfs.sys.service;

public interface ISOFIPSConversionService {

    /*
     * ISO-to-FIPS mappings are many-ISO-to-one-FIPS.
     */
	public String convertISOCountryCodeToFIPSCountryCode(String isoCountryCode);

	/*
	 * FIPS-to-ISO mappings are one-FIPS-to-many-ISO
	 */
	public String convertFIPSCountryCodeToISOCountryCode(String fipsCountryCode);

}
