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

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSKeyConstants;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;
import edu.cornell.kfs.sys.businessobject.ISOCountry;
import edu.cornell.kfs.sys.service.ISOCountryService;

/**
 * CU Generic ISO-FIPS Country modification
 */
public class ISOCountryServiceImpl implements ISOCountryService {

    private static final Logger LOG = LogManager.getLogger(ISOCountryServiceImpl.class);
    
    protected BusinessObjectService businessObjectService;
    protected ConfigurationService configurationService;
    
    @Override
    public boolean isISOCountryActive(String isoCountryCode) {
        if (ObjectUtils.isNull(isoCountryCode)) {
            return false;
        }
        ISOCountry isoCountryFound = getByPrimaryId(isoCountryCode);
        if (ObjectUtils.isNotNull(isoCountryFound)) {
            LOG.debug("isISOCountryActive: " + MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.MESSAGE_ISO_COUNTRY_CODE_INDICATOR), isoCountryCode, isoCountryFound.isActive()));
            return isoCountryFound.isActive();
        } else {
            LOG.error("isISOCountryActive: " + MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.ERROR_NO_ISO_COUNTRY_FOUND_FOR_CODE), isoCountryCode));
            return false;
        }
    }

    @Override
    public boolean isISOCountryInactive(String isoCountryCode) {
        if (ObjectUtils.isNull(isoCountryCode)) {
            return false;
        }
        ISOCountry isoCountryFound = getByPrimaryId(isoCountryCode);
        if (ObjectUtils.isNotNull(isoCountryFound)) {
            LOG.debug("isISOCountryInactive: " + MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.MESSAGE_ISO_COUNTRY_CODE_INDICATOR), isoCountryCode, isoCountryFound.isActive()));
            return !isoCountryFound.isActive();
        } else {
            LOG.error("isISOCountryInactive: " + MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.ERROR_NO_ISO_COUNTRY_FOUND_FOR_CODE), isoCountryCode));
            return false;
        }
    }
    
    protected ISOCountry getByPrimaryId(String isoCountryCode) {
        if (ObjectUtils.isNull(isoCountryCode)) {
            return null;
        }
        return getBusinessObjectService().findByPrimaryKey(ISOCountry.class, mapPrimaryKeys(isoCountryCode));
    }
    
    @Override
    public String findISOCountryNameByCountryCode(String isoCountryCode) {
        if (ObjectUtils.isNull(isoCountryCode)) {
            return KFSConstants.EMPTY_STRING;
        }
        ISOCountry isoCountryFound = getBusinessObjectService().findByPrimaryKey(ISOCountry.class, mapPrimaryKeys(isoCountryCode.toUpperCase()));
        if (ObjectUtils.isNotNull(isoCountryFound)) {
            return isoCountryFound.getName();
        } else {
            return MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.NAME_NOT_FOUND_FOR_COUNTRY_CODE), CUKFSConstants.ISO, isoCountryCode);
        }
    }

    @Override
    public boolean isoCountryExists(String isoCountryCode) {
        if (ObjectUtils.isNull(isoCountryCode)) {
            return false;
        }
        ISOCountry isoCountryFound = getBusinessObjectService().findByPrimaryKey(ISOCountry.class, mapPrimaryKeys(isoCountryCode.toUpperCase()));
        return ObjectUtils.isNotNull(isoCountryFound);
    }
    
    protected Map<String, Object> mapPrimaryKeys(String isoCountryCode) {
        Map<String, Object> primaryKeys = new HashMap<>();
        primaryKeys.put(CUKFSPropertyConstants.ISOCountry.CODE, isoCountryCode);
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
