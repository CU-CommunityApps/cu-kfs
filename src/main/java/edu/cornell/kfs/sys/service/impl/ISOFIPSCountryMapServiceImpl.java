package edu.cornell.kfs.sys.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return (List<ISOFIPSCountryMap>) getBusinessObjectService().findMatching(ISOFIPSCountryMap.class, mapPartialPrimaryKeysAndActiveStatus(CUKFSPropertyConstants.ISOFIPSCountryMap.ISO_COUNTRY_CODE, isoCountryCode));
    }
    
    public List<ISOFIPSCountryMap> findActiveMapsByFIPSCountryId(String fipsCountryCode) {
        return (List<ISOFIPSCountryMap>) getBusinessObjectService().findMatching(ISOFIPSCountryMap.class, mapPartialPrimaryKeysAndActiveStatus(CUKFSPropertyConstants.ISOFIPSCountryMap.FIPS_COUNTRY_CODE, fipsCountryCode));
    }
    
    protected Map<String, Object> mapPartialPrimaryKeysAndActiveStatus(String key, String value) {
        Map<String, Object> partialKeys = new HashMap<>();
        partialKeys.put(key, value);
        partialKeys.put(CUKFSPropertyConstants.ISOFIPSCountryMap.ACTIVE, true);
        return partialKeys;
    }
    
    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
