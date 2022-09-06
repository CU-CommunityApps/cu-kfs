package edu.cornell.kfs.sys.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;

import edu.cornell.kfs.sys.CUKFSPropertyConstants;
import edu.cornell.kfs.sys.businessobject.ISOFIPSCountryMap;
import edu.cornell.kfs.sys.service.ISOFIPSCountryMapService;

/**
 * CU Generic ISO-FIPS Country modification
 */
public class ISOFIPSCountryMapServiceImpl implements ISOFIPSCountryMapService {

    private static final Logger LOG = LogManager.getLogger(ISOFIPSCountryMapServiceImpl.class);
    
    protected BusinessObjectService businessObjectService;
           
    public List<ISOFIPSCountryMap> findActiveMapsByISOCountryId(String isoCountryCode) {
        return performMappingConversion(CUKFSPropertyConstants.ISOFIPSCountryMap.ISO_COUNTRY_CODE, isoCountryCode);
    }
    
    public List<ISOFIPSCountryMap> findActiveMapsByFIPSCountryId(String fipsCountryCode) {
        return performMappingConversion(CUKFSPropertyConstants.ISOFIPSCountryMap.FIPS_COUNTRY_CODE, fipsCountryCode);
    }

    private List<ISOFIPSCountryMap> performMappingConversion(String key, String value) {
        Collection<ISOFIPSCountryMap> mappingsFoundCollection = getBusinessObjectService().findMatching(ISOFIPSCountryMap.class, mapPartialPrimaryKeysAndActiveStatus(key, value));
        List<ISOFIPSCountryMap> mappingsFound = new ArrayList<ISOFIPSCountryMap>(mappingsFoundCollection);
        if (!mappingsFound.isEmpty()) {
            mappingsFound = mappingsFound
                    .stream()
                    .filter(mapping -> mapping.isActive())
                    .collect(Collectors.toList());
        }
        if (mappingsFound.isEmpty()) {
            return new ArrayList<ISOFIPSCountryMap>();
        } else {
            return mappingsFound;
        }
    }
    
    protected Map<String, Object> mapPartialPrimaryKeysAndActiveStatus(String key, String value) {
        Map<String, Object> partialKeys = new HashMap<>();
        partialKeys.put(key, value);
        return partialKeys;
    }
    
    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
