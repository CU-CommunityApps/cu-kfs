package edu.cornell.kfs.sys.service;

/*
 * This is the generic solution create by Cornell to deal with converting between
 * many ISO country codes mapping to a single FIPS country code. 
 * Details of this implmentation are documented in KFSPTS-25260.
 */
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
