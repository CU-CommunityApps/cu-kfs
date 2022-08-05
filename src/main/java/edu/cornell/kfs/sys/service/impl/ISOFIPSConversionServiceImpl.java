package edu.cornell.kfs.sys.service.impl;

import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.cornell.kfs.sys.businessobject.ISOFIPSCountryMap;
import edu.cornell.kfs.sys.dataaccess.ISOFIPSCountryMapDao;
import edu.cornell.kfs.sys.service.ISOFIPSConversionService;

/*****************************************************************************
 * This is the GENERIC solution created by Cornell to address converting
 * between ISO standard country codes and FIPS country codes currently
 * used by the rest of the KFS system.
 *
 *    *******NOTE*******
 * The FIPS country codes are stored in KFS table SH_CNTRY_T which is manually
 * maintained by the Country maintenace document.
 * Due to the manual nature of maintaining those FIPS country code values,
 * there are some INACTIVE ISO country codes also existing in that FIPS table.
 *    *******NOTE*******
 *
 * When mapping between the two different standards, in some cases a country
 * will have the same country code in both the FIPS and ISO standard.
 *
 * In other cases, the same country will be represented by different country
 * codes in the FIPS and ISO standards.
 *
 * There are also cases where multiple distinct FIPS country codes map to the
 * same single ISO country code.
 *
 * Specific examples for each of the three data conditions stated above are listed below.
 *
 *   ============                             ===========
 *   FIPS-Country                             ISO Country
 *   ============                             ===========
 *   US = United States                       US = United States of America (the)
 *
 *   JA = Japan                               JP = Japan
 *   AJ = Azerbaijan                          AZ = Azerbaijan
 *   No FIPS code exists                      AS = American Samoa
 *   GB = Gabon                               GA = Gabon
 *   UK = United Kingdom                      GB = United Kingdom of Great Britain and Northern Ireland (the)
 *
 *   BQ = NA VASSA ISLAND                     UM = United States Minor Outlying Islands (the)
 *   DQ = JARVIS ISALND                       UM = United States Minor Outlying Islands (the)
 *   FQ = BAKER ISLAND                        UM = United States Minor Outlying Islands (the)
 *   HQ = HOWLAND ISLAND                      UM = United States Minor Outlying Islands (the)
 *   JQ = JOHNSTON ATOLL                      UM = United States Minor Outlying Islands (the)
 *   KQ = KINGMAN REEF                        UM = United States Minor Outlying Islands (the)
 *   LQ = PALMYRA ATOLL                       UM = United States Minor Outlying Islands (the)
 *   MQ = MIDWAY ISLANDS                      UM = United States Minor Outlying Islands (the)
 *   WQ = WAKE ISLAND                         UM = United States Minor Outlying Islands (the)
 *
 *   AS = AUSTRALIA                           AU = Australia
 *   AT = ASHMORE AND CARTIER ISLANDS         AU = Australia
 *   CR = CORAL SEA ISLANDS TERRITORY         AU = Australia
 *
 *****************************************************************************/

public class ISOFIPSConversionServiceImpl implements ISOFIPSConversionService {
    
    private static final Logger LOG = LogManager.getLogger(ISOFIPSConversionServiceImpl.class);
    
    private ISOFIPSCountryMapDao isoFipsCountryMapDao;
    
    /*
     * ISO-to-FIPS mappings are many-ISO-to-one-FIPS.
     */
    public String convertISOCountryCodeToFIPSCountryCode(String isoCountryCode) {
        List<ISOFIPSCountryMap> mappingsFound = isoFipsCountryMapDao.findFipsCountries(isoCountryCode);

        if (mappingsFound.isEmpty()) {
            LOG.error("convertISOCountryCodeToFIPSCountryCode: No ISO-to-FIPS Country mapping found for ISO Country code : " + isoCountryCode);
            throw new RuntimeException("No ISO-to-FIPS Country mapping found for ISO Country code : " + isoCountryCode);
        } else if (mappingsFound.size() > 1) {
            LOG.error("convertISOCountryCodeToFIPSCountryCode: More than one ISO-to-FIPS Country mapping found for ISO Country code : " + isoCountryCode);
            throw new RuntimeException("More than one ISO-to-FIPS Country mapping found for ISO Country code : " + isoCountryCode);
        } 
        String fipsCountryCodeFound = (mappingsFound.get(0)).getFipsCountryCode();
        LOG.info("convertISOCountryCodeToFIPSCountryCode: One ISO-to-FIPS Country generic mapping found: ISO Country code : " + isoCountryCode + " mapped to FIPS Country code : " + fipsCountryCodeFound);
        return fipsCountryCodeFound;
    }

    public String convertFIPSCountryCodeToISOCountryCode(String fipsCountryCode) { 
        List<ISOFIPSCountryMap> mappingsFound = isoFipsCountryMapDao.findIsoCountries(fipsCountryCode);

        if (mappingsFound.isEmpty()) {
            LOG.error("convertFIPSCountryCodeToISOCountryCode: No FIPS-to-ISO Country mapping found for FIPS Country code : " + fipsCountryCode);
            throw new RuntimeException("No FIPS-to-ISO Country mapping found for FIPS Country code : " + fipsCountryCode);
        } else if (mappingsFound.size() > 1) {
            LOG.error("convertFIPSCountryCodeToISOCountryCode: More than one FIPS-to-ISO Country mapping found for FIPS Country code : " + fipsCountryCode);
            throw new RuntimeException("More than one FIPS-to-ISO Country mapping found for FIPS Country code : " + fipsCountryCode);
        } 
        String isoCountryCodeFound = (mappingsFound.get(0)).getIsoCountryCode();
        LOG.info("convertFIPSCountryCodeToISOCountryCode: One FIPS-to-ISO Country generic mapping found FIPS Country code : " + fipsCountryCode + " mapped to ISO Country code : " + isoCountryCodeFound);
        return isoCountryCodeFound;
    }

    public ISOFIPSCountryMapDao getIsoFipsCountryMapDao() {
        return isoFipsCountryMapDao;
    }

    public void setIsoFipsCountryMapDao(ISOFIPSCountryMapDao isoFipsCountryMapDao) {
        this.isoFipsCountryMapDao = isoFipsCountryMapDao;
    }
    
}
