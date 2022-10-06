package edu.cornell.kfs.sys.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.krad.service.BusinessObjectService;

import edu.cornell.kfs.sys.CUKFSKeyConstants;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;
import edu.cornell.kfs.sys.businessobject.ISOFIPSCountryMap;
import edu.cornell.kfs.sys.service.ISOFIPSCountryMapService;

/**
 * CU Generic ISO-FIPS Country modification
 */
public class ISOFIPSCountryMapServiceImpl implements ISOFIPSCountryMapService {

    private static final Logger LOG = LogManager.getLogger(ISOFIPSCountryMapServiceImpl.class);
    
    protected BusinessObjectService businessObjectService;
    protected ConfigurationService configurationService;
           
    public List<ISOFIPSCountryMap> findActiveMapsByISOCountryId(String isoCountryCode) {
        if (isBlank(isoCountryCode, "isoCountryCode")) {
            return new ArrayList<ISOFIPSCountryMap>(); 
        }
        return performMappingConversion(CUKFSPropertyConstants.ISOFIPSCountryMap.ISO_COUNTRY_CODE, isoCountryCode);
    }
    
    public List<ISOFIPSCountryMap> findActiveMapsByFIPSCountryId(String fipsCountryCode) {
        if (isBlank(fipsCountryCode, "fipsCountryCode")) {
            return new ArrayList<ISOFIPSCountryMap>(); 
        } 
        return performMappingConversion(CUKFSPropertyConstants.ISOFIPSCountryMap.FIPS_COUNTRY_CODE, fipsCountryCode);
    }

    private List<ISOFIPSCountryMap> performMappingConversion(String key, String value) {
        Collection<ISOFIPSCountryMap> mappingsFound = getBusinessObjectService().findMatching(ISOFIPSCountryMap.class, mapPartialPrimaryKeysAndActiveStatus(key, value));
        if (CollectionUtils.isNotEmpty(mappingsFound)) {
            mappingsFound = mappingsFound
                    .stream()
                    .filter(mapping -> mapping.isActive())
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(mappingsFound)) {
            return new ArrayList<ISOFIPSCountryMap>();
        } else {
            return new ArrayList<ISOFIPSCountryMap>(mappingsFound);
        }
    }
    
    protected Map<String, Object> mapPartialPrimaryKeysAndActiveStatus(String key, String value) {
        Map<String, Object> partialKeys = new HashMap<>();
        partialKeys.put(key, value);
        return partialKeys;
    }
    
    private boolean isBlank(String countryCodeToCheck, String codeTypeForDebugMessage) {
        if (StringUtils.isBlank(countryCodeToCheck)) {
            LOG.debug("isBlank: " + MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.NULL_OR_BLANK_CODE_PARAMETER), codeTypeForDebugMessage));
            return true;
        }
        return false;
    }
    
    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
