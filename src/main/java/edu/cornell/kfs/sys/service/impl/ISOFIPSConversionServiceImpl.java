package edu.cornell.kfs.sys.service.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.cornell.kfs.sys.businessobject.ISOFIPSCountryMap;
import edu.cornell.kfs.sys.exception.ManyFIPStoISOMappingException;
import edu.cornell.kfs.sys.exception.ManyISOtoFIPSMappingException;
import edu.cornell.kfs.sys.exception.NoFIPStoISOMappingException;
import edu.cornell.kfs.sys.exception.NoISOtoFIPSMappingException;
import edu.cornell.kfs.sys.service.CountryService;
import edu.cornell.kfs.sys.service.ISOCountryService;
import edu.cornell.kfs.sys.service.ISOFIPSConversionService;
import edu.cornell.kfs.sys.service.ISOFIPSCountryMapService;

/*****************************************************************************
 * CU Generic ISO-FIPS Country modification
 *
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
    
    private static final String NO_ISO_TO_FIPS_MAPPINGS_ERROR_MESSAGE = "No Active ISO-to-FIPS Country generic mapping found for ISO Country code : {0}";
    private static final String MANY_ISO_TO_FIPS_MAPPINGS_ERROR_MESSAGE = "More than one Active ISO-to-FIPS Country generic mapping found for ISO Country code : {0}";
    private static final String ONE_TO_ONE_ISO_TO_FIPS_MAPPING_MESSAGE = "One Active ISO-to-FIPS Country generic mapping found: ISO Country code : {0} mapped to FIPS Country code : {1}";
    private static final String NO_FIPS_TO_ISO_MAPPINGS_ERROR_MESSAGE = "No Active FIPS-to-ISO Country generic mapping found for FIPS Country code : {0}";
    private static final String MANY_FIPS_TO_ISO_MAPPINGS_ERROR_MESSAGE = "More than one Active FIPS-to-ISO Country generic mapping found for FIPS Country code : {0}";
    private static final String ONE_TO_ONE_FIPS_TO_ISO_MAPPING_MESSAGE = "One Active FIPS-to-ISO Country generic mapping found FIPS Country code : {0} mapped to ISO Country code : {1}";

    private ISOCountryService isoCountryService;
    private CountryService countryService;
    private ISOFIPSCountryMapService isoFipsCountryMapService;
    
    /**
     * Return the single ACTIVE FIPS country code mapped to the ISO country code input parameter when the
     * ISO Country is Active, the FIPS Country is Active, and a single Active ISO-FIPS Country Mapping exists;
     *
     * NOTE: Active status IS USED for all three items in this data retrieval
     */ 
    @Override
    public String convertISOCountryCodeToActiveFIPSCountryCode(String isoCountryCode) {
        if (getIsoCountryService().isISOCountryInactive(isoCountryCode)) {
            LOG.error("convertISOCountryCodeToActiveFIPSCountryCode: " + MessageFormat.format(NO_ISO_TO_FIPS_MAPPINGS_ERROR_MESSAGE, isoCountryCode));
            throw new NoISOtoFIPSMappingException(MessageFormat.format(NO_ISO_TO_FIPS_MAPPINGS_ERROR_MESSAGE, isoCountryCode));
        }
        
        List<ISOFIPSCountryMap> mappingsFound = findManyActiveFIPSCountryMapsForISOCode(isoCountryCode);
        
        mappingsFound = mappingsFound
                .stream()
                .filter(fipsCountryMapping -> fipsCountryMapping.getFipsCountry().isActive())
                .collect(Collectors.toList());

        if (mappingsFound.isEmpty()) {
            LOG.error("convertISOCountryCodeToActiveFIPSCountryCode: " + MessageFormat.format(NO_ISO_TO_FIPS_MAPPINGS_ERROR_MESSAGE, isoCountryCode));
            throw new NoISOtoFIPSMappingException(MessageFormat.format(NO_ISO_TO_FIPS_MAPPINGS_ERROR_MESSAGE, isoCountryCode));
        } else if (mappingsFound.size() > 1) {
            LOG.error("convertISOCountryCodeToActiveFIPSCountryCode: " + MessageFormat.format(MANY_ISO_TO_FIPS_MAPPINGS_ERROR_MESSAGE, isoCountryCode));
            throw new ManyISOtoFIPSMappingException(MessageFormat.format(MANY_ISO_TO_FIPS_MAPPINGS_ERROR_MESSAGE, isoCountryCode));
        } 
        String fipsCountryCodeFound = (mappingsFound.get(0)).getFipsCountryCode();
        LOG.info("convertISOCountryCodeToActiveFIPSCountryCode: " + MessageFormat.format(ONE_TO_ONE_ISO_TO_FIPS_MAPPING_MESSAGE, isoCountryCode, fipsCountryCodeFound));
        return fipsCountryCodeFound;
    }

    /**
     * Return the single ACTIVE ISO country code mapped to the FIPS country code input parameter when the
     * ISO Country is Active, the FIPS Country is Active, and a single Active ISO-FIPS Country Mapping exists;
     *
     * NOTE: Active status IS USED for all three items in this data retrieval.
     */
    @Override
    public String convertFIPSCountryCodeToActiveISOCountryCode(String fipsCountryCode) {
        if (getCountryService().isCountryInactive(fipsCountryCode)) {
            LOG.error("convertFIPSCountryCodeToISOCountryCode: " + MessageFormat.format(NO_FIPS_TO_ISO_MAPPINGS_ERROR_MESSAGE, fipsCountryCode));
            throw new NoFIPStoISOMappingException(MessageFormat.format(NO_FIPS_TO_ISO_MAPPINGS_ERROR_MESSAGE, fipsCountryCode));
        }
        
        List<ISOFIPSCountryMap> mappingsFound = findManyActiveISOCountryMapsForFIPSCode(fipsCountryCode);
        
        mappingsFound = mappingsFound
                .stream()
                .filter(isoCountryMapping -> isoCountryMapping.getIsoCountry().isActive())
                .collect(Collectors.toList());

        if (mappingsFound.isEmpty()) {
            LOG.error("convertFIPSCountryCodeToActiveISOCountryCode: " + MessageFormat.format(NO_FIPS_TO_ISO_MAPPINGS_ERROR_MESSAGE, fipsCountryCode));
            throw new NoFIPStoISOMappingException(MessageFormat.format(NO_FIPS_TO_ISO_MAPPINGS_ERROR_MESSAGE, fipsCountryCode));
        } else if (mappingsFound.size() > 1) {
            LOG.error("convertFIPSCountryCodeToActiveISOCountryCode: " + MessageFormat.format(MANY_FIPS_TO_ISO_MAPPINGS_ERROR_MESSAGE, fipsCountryCode));
            throw new ManyFIPStoISOMappingException(MessageFormat.format(MANY_FIPS_TO_ISO_MAPPINGS_ERROR_MESSAGE, fipsCountryCode));
        } 
        String isoCountryCodeFound = (mappingsFound.get(0)).getIsoCountryCode();
        LOG.info("convertFIPSCountryCodeToActiveISOCountryCode: " + MessageFormat.format(ONE_TO_ONE_FIPS_TO_ISO_MAPPING_MESSAGE, fipsCountryCode, isoCountryCodeFound));
        return isoCountryCodeFound;
    }

    /**
     * NOTE: Active status IS USED ONLY for the MAPPING in this data retrieval.
     * Ensuring proper status of ISO Country and FIPS Country are NOT taken into 
     * account and is the responsibility of the caller.
     */
    private List<ISOFIPSCountryMap> findManyActiveISOCountryMapsForFIPSCode(String fipsCountryCode) {
        return getIsoFipsCountryMapService().findActiveMapsByFIPSCountryId(fipsCountryCode);
    }

    /**
     * NOTE: Active status IS USED ONLY for the MAPPING in this data retrieval.
     * Ensuring proper status of ISO Country and FIPS Country are NOT taken into 
     * account and is the responsibility of the caller.
     */
    private List<ISOFIPSCountryMap> findManyActiveFIPSCountryMapsForISOCode(String isoCountryCode) {
        return getIsoFipsCountryMapService().findActiveMapsByISOCountryId(isoCountryCode);
    }

    public ISOCountryService getIsoCountryService() {
        return isoCountryService;
    }

    public void setIsoCountryService(ISOCountryService isoCountryService) {
        this.isoCountryService = isoCountryService;
    }

    public CountryService getCountryService() {
        return countryService;
    }

    public void setCountryService(CountryService countryService) {
        this.countryService = countryService;
    }

    public ISOFIPSCountryMapService getIsoFipsCountryMapService() {
        return isoFipsCountryMapService;
    }

    public void setIsoFipsCountryMapService(ISOFIPSCountryMapService isoFipsCountryMapService) {
        this.isoFipsCountryMapService = isoFipsCountryMapService;
    }
   
}
