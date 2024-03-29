package edu.cornell.kfs.sys.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSKeyConstants;
import edu.cornell.kfs.sys.businessobject.ISOFIPSCountryMap;
import edu.cornell.kfs.sys.exception.ManyFIPSCountriesForNameException;
import edu.cornell.kfs.sys.exception.ManyFIPStoISOMappingException;
import edu.cornell.kfs.sys.exception.ManyISOCountriesForNameException;
import edu.cornell.kfs.sys.exception.ManyISOtoFIPSMappingException;
import edu.cornell.kfs.sys.exception.NoFIPSCountriesForNameException;
import edu.cornell.kfs.sys.exception.NoFIPStoISOMappingException;
import edu.cornell.kfs.sys.exception.NoISOCountriesForNameException;
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
    
    private static final Logger LOG = LogManager.getLogger();
    
    private ISOCountryService isoCountryService;
    private CountryService countryService;
    private ISOFIPSCountryMapService isoFipsCountryMapService;
    private ConfigurationService configurationService;
    
    /**
     * Return the single ACTIVE FIPS country code mapped to the ISO country code input parameter when the
     * ISO Country is Active, the FIPS Country is Active, and a single Active ISO-FIPS Country Mapping exists;
     *
     * NOTE: Active status IS USED for all three items in this data retrieval
     */ 
    @Override
    public String convertISOCountryCodeToActiveFIPSCountryCode(String isoCountryCode) {
        if (getIsoCountryService().isISOCountryInactive(isoCountryCode)) {
            LOG.error("convertISOCountryCodeToActiveFIPSCountryCode: " + MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.ERROR_NO_ISO_TO_FIPS_MAPPINGS), isoCountryCode));
            throw new NoISOtoFIPSMappingException(MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.ERROR_NO_ISO_TO_FIPS_MAPPINGS), isoCountryCode));
        }
        
        List<ISOFIPSCountryMap> mappingsFound = findManyActiveFIPSCountryMapsForISOCode(isoCountryCode);
        if (CollectionUtils.isEmpty(mappingsFound)) {
            LOG.error("convertISOCountryCodeToActiveFIPSCountryCode: " + MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.ERROR_NO_ISO_TO_FIPS_MAPPINGS), isoCountryCode));
            throw new NoISOtoFIPSMappingException(MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.ERROR_NO_ISO_TO_FIPS_MAPPINGS), isoCountryCode));
        }
        
        List<ISOFIPSCountryMap> activeMappingsFound = new ArrayList<ISOFIPSCountryMap>();
        for (ISOFIPSCountryMap fipsCountryMapping : mappingsFound) {
            if (ObjectUtils.isNotNull(fipsCountryMapping.getFipsCountry()) && fipsCountryMapping.getFipsCountry().isActive()) {
                activeMappingsFound.add(fipsCountryMapping);
            }
        }

        if (CollectionUtils.isEmpty(activeMappingsFound)) {
            LOG.error("convertISOCountryCodeToActiveFIPSCountryCode: " + MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.ERROR_NO_ISO_TO_FIPS_MAPPINGS), isoCountryCode));
            throw new NoISOtoFIPSMappingException(MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.ERROR_NO_ISO_TO_FIPS_MAPPINGS), isoCountryCode));
        } else if (activeMappingsFound.size() > 1) {
            LOG.error("convertISOCountryCodeToActiveFIPSCountryCode: " + MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.ERROR_MANY_ISO_TO_FIPS_MAPPINGS), isoCountryCode));
            throw new ManyISOtoFIPSMappingException(MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.ERROR_MANY_ISO_TO_FIPS_MAPPINGS), isoCountryCode));
        } 
        String fipsCountryCodeFound = (activeMappingsFound.get(0)).getFipsCountryCode();
        LOG.debug("convertISOCountryCodeToActiveFIPSCountryCode: " + MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.MESSAGE_ONE_TO_ONE_ISO_TO_FIPS_MAPPING), isoCountryCode, fipsCountryCodeFound));
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
            LOG.error("convertFIPSCountryCodeToISOCountryCode: " + MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.ERROR_NO_FIPS_TO_ISO_MAPPINGS), fipsCountryCode));
            throw new NoFIPStoISOMappingException(MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.ERROR_NO_FIPS_TO_ISO_MAPPINGS), fipsCountryCode));
        }
        
        List<ISOFIPSCountryMap> mappingsFound = findManyActiveISOCountryMapsForFIPSCode(fipsCountryCode);
        if (CollectionUtils.isEmpty(mappingsFound)) {
            LOG.error("convertFIPSCountryCodeToActiveISOCountryCode: " + MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.ERROR_NO_FIPS_TO_ISO_MAPPINGS), fipsCountryCode));
            throw new NoFIPStoISOMappingException(MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.ERROR_NO_FIPS_TO_ISO_MAPPINGS), fipsCountryCode));
        }
        
        List<ISOFIPSCountryMap> activeMappingsFound = new ArrayList<ISOFIPSCountryMap>();
        for (ISOFIPSCountryMap isoCountryMapping : mappingsFound) {
            if (ObjectUtils.isNotNull(isoCountryMapping.getIsoCountry()) && isoCountryMapping.getIsoCountry().isActive()) {
                activeMappingsFound.add(isoCountryMapping);
            }
        }

        if (CollectionUtils.isEmpty(activeMappingsFound)) {
            LOG.error("convertFIPSCountryCodeToActiveISOCountryCode: " + MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.ERROR_NO_FIPS_TO_ISO_MAPPINGS), fipsCountryCode));
            throw new NoFIPStoISOMappingException(MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.ERROR_NO_FIPS_TO_ISO_MAPPINGS), fipsCountryCode));
        } else if (activeMappingsFound.size() > 1) {
            LOG.error("convertFIPSCountryCodeToActiveISOCountryCode: " + MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.ERROR_MANY_FIPS_TO_ISO_MAPPINGS), fipsCountryCode));
            throw new ManyFIPStoISOMappingException(MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.ERROR_MANY_FIPS_TO_ISO_MAPPINGS), fipsCountryCode));
        } 
        String isoCountryCodeFound = (activeMappingsFound.get(0)).getIsoCountryCode();
        LOG.debug("convertFIPSCountryCodeToActiveISOCountryCode: " + MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.MESSAGE_ONE_TO_ONE_FIPS_TO_ISO_MAPPING), fipsCountryCode, isoCountryCodeFound));
        return isoCountryCodeFound;
    }

    @Override
    public String findSingleActiveISOCountryCodeByISOCountryName(String isoCountryName) {
        List<String> isoCountryCodes = isoCountryService.findISOCountryCodesByCountryName(isoCountryName);
        if (CollectionUtils.size(isoCountryCodes) > 1) {
            String manyCodesMessage = formatMessage(CUKFSKeyConstants.ERROR_MANY_COUNTRIES_FOR_NAME, CUKFSConstants.ISO, isoCountryName);
            LOG.error("findSingleActiveISOCountryCodeByISOCountryName: {}", manyCodesMessage);
            throw new ManyISOCountriesForNameException(manyCodesMessage);
        }
        
        String isoCountryCode = CollectionUtils.isNotEmpty(isoCountryCodes) ? isoCountryCodes.get(0) : null;
        if (StringUtils.isBlank(isoCountryCode) || isoCountryService.isISOCountryInactive(isoCountryCode)) {
            String noCodesMessage = formatMessage(CUKFSKeyConstants.ERROR_NO_COUNTRY_FOUND_FOR_NAME, CUKFSConstants.ISO, isoCountryName);
            LOG.error("findSingleActiveISOCountryCodeByISOCountryName: {}", noCodesMessage);
            throw new NoISOCountriesForNameException(noCodesMessage);
        }
        
        LOG.debug("findSingleActiveISOCountryCodeByISOCountryName: {}",
                () -> formatMessage(CUKFSKeyConstants.MESSAGE_ONE_COUNTRY_FOR_NAME, CUKFSConstants.ISO, isoCountryName, isoCountryCode));
        return isoCountryCode;
    }

    @Override
    public String findSingleActiveFIPSCountryCodeByFIPSCountryName(String fipsCountryName) {
        List<String> fipsCountryCodes = countryService.findCountryCodesByCountryName(fipsCountryName);
        if (CollectionUtils.size(fipsCountryCodes) > 1) {
            String manyCodesMessage = formatMessage(CUKFSKeyConstants.ERROR_MANY_COUNTRIES_FOR_NAME, CUKFSConstants.FIPS, fipsCountryName);
            LOG.error("findSingleActiveFIPSCountryCodeByFIPSCountryName: {}", manyCodesMessage);
            throw new ManyFIPSCountriesForNameException(manyCodesMessage);
        }
        
        String fipsCountryCode = CollectionUtils.isNotEmpty(fipsCountryCodes) ? fipsCountryCodes.get(0) : null;
        if (StringUtils.isBlank(fipsCountryCode) || countryService.isCountryInactive(fipsCountryCode)) {
            String noCodesMessage = formatMessage(CUKFSKeyConstants.ERROR_NO_COUNTRY_FOUND_FOR_NAME, CUKFSConstants.FIPS, fipsCountryName);
            LOG.error("findSingleActiveFIPSCountryCodeByFIPSCountryName: {}", noCodesMessage);
            throw new NoFIPSCountriesForNameException(noCodesMessage);
        }
        
        LOG.debug("findSingleActiveFIPSCountryCodeByFIPSCountryName: {}",
                () -> formatMessage(CUKFSKeyConstants.MESSAGE_ONE_COUNTRY_FOR_NAME, CUKFSConstants.FIPS, fipsCountryName, fipsCountryCode));
        return fipsCountryCode;
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

    private String formatMessage(String messageKey, Object... arguments) {
        String messagePattern = configurationService.getPropertyValueAsString(messageKey);
        return MessageFormat.format(messagePattern, arguments);
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

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
   
}
