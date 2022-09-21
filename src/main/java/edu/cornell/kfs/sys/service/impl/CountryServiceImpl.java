package edu.cornell.kfs.sys.service.impl;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.Country;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSKeyConstants;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;
import edu.cornell.kfs.sys.service.CountryService;

/**
 * CU Generic ISO-FIPS Country modification
 */
public class CountryServiceImpl implements CountryService {

    private static final Logger LOG = LogManager.getLogger(CountryServiceImpl.class);
 
    protected BusinessObjectService businessObjectService;
    protected ConfigurationService configurationService;
    
    @Override
    public boolean isCountryActive(String countryCode) {
        if (ObjectUtils.isNull(countryCode)) {
            return false;
        }
        Country countryFound = getByPrimaryId(countryCode);
        if (ObjectUtils.isNotNull(countryFound)) {
            LOG.debug("isCountryActive: " +
                    MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.MESSAGE_COUNTRY_CODE_INDICATOR), countryCode, (countryFound.isActive() ? "Active" : "Inactive")));
            return countryFound.isActive();
        } else {
            LOG.error("isCountryActive: " + MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.ERROR_NO_COUNTRY_FOUND_FOR_CODE), countryCode));
            return false;
        }
    }
    
    @Override
    public boolean isCountryInactive(String countryCode) {
        if (ObjectUtils.isNull(countryCode)) {
            return false;
        }
        Country countryFound = getByPrimaryId(countryCode);
        if (ObjectUtils.isNotNull(countryFound)) {
            LOG.debug("isCountryInactive: " +
                    MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.MESSAGE_COUNTRY_CODE_INDICATOR), countryCode, (countryFound.isActive() ? "Active" : "Inactive")));
            return !countryFound.isActive();
        } else {
            LOG.error("isCountryInactive: " + MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.ERROR_NO_COUNTRY_FOUND_FOR_CODE), countryCode));
            return false;
        }
    }
    
    protected Country getByPrimaryId(String countryCode) {
        if (ObjectUtils.isNull(countryCode)) {
            return null;
        }
        return getBusinessObjectService().findByPrimaryKey(Country.class, mapPrimaryKeys(countryCode));
    }
    
    @Override
    public String findCountryNameByCountryCode(String countryCode) {
        if (ObjectUtils.isNull(countryCode)) {
            return KFSConstants.EMPTY_STRING;
        }
        Country countryFound = getBusinessObjectService().findByPrimaryKey(Country.class, mapPrimaryKeys(countryCode.toUpperCase()));
        if (ObjectUtils.isNotNull(countryFound)) {
            return countryFound.getName();
        } else {
            return MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.NAME_NOT_FOUND_FOR_COUNTRY_CODE), CUKFSConstants.FIPS, countryCode);
        }
    }
    
    @Override
    public boolean countryExists(String countryCode) {
        if (ObjectUtils.isNull(countryCode)) {
            return false;
        }
        Country countryFound = getBusinessObjectService().findByPrimaryKey(Country.class, mapPrimaryKeys(countryCode.toUpperCase()));
        return ObjectUtils.isNotNull(countryFound);
    }
    
    protected Map<String, Object> mapPrimaryKeys(String countryCode) {
        Map<String, Object> primaryKeys = new HashMap<>();
        primaryKeys.put(CUKFSPropertyConstants.Country.CODE, countryCode);
        return primaryKeys;
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
