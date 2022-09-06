package edu.cornell.kfs.sys.service.impl;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.sys.CUKFSPropertyConstants;
import edu.cornell.kfs.sys.businessobject.ISOCountry;
import edu.cornell.kfs.sys.service.ISOCountryService;

/**
 * CU Generic ISO-FIPS Country modification
 */
public class ISOCountryServiceImpl implements ISOCountryService {

    private static final Logger LOG = LogManager.getLogger(ISOCountryServiceImpl.class);

    private static final String NO_ISO_COUNTRY_FOUND_FOR_CODE = "No ISOCountry found for code : {0}";
    private static final String ISO_COUNTRY_CODE_INDICATOR_MESSAGE = "ISOCountry code : {0} has active status of {1}";
    
    protected BusinessObjectService businessObjectService;
    
    public boolean isISOCountryActive(String isoCountryCode) {
        ISOCountry isoCountryFound = getByPrimaryId(isoCountryCode);

        if (ObjectUtils.isNotNull(isoCountryFound)) {
            LOG.info("isISOCountryActive: " + MessageFormat.format(ISO_COUNTRY_CODE_INDICATOR_MESSAGE, isoCountryCode, isoCountryFound.isActive()));
            return isoCountryFound.isActive();
        } else {
            LOG.error("isISOCountryActive: " + MessageFormat.format(NO_ISO_COUNTRY_FOUND_FOR_CODE, isoCountryCode));
            return false;
        }
    }

    public boolean isISOCountryInactive(String isoCountryCode) {
        ISOCountry isoCountryFound = getByPrimaryId(isoCountryCode);

        if (ObjectUtils.isNotNull(isoCountryFound)) {
            LOG.info("isISOCountryInactive: " + MessageFormat.format(ISO_COUNTRY_CODE_INDICATOR_MESSAGE, isoCountryCode, isoCountryFound.isActive()));
            return !isoCountryFound.isActive();
        } else {
            LOG.error("isISOCountryInactive: " + MessageFormat.format(NO_ISO_COUNTRY_FOUND_FOR_CODE, isoCountryCode));
            return false;
        }
    }
    
    public ISOCountry getByPrimaryId(String isoCountryCode) {
        return getBusinessObjectService().findByPrimaryKey(ISOCountry.class, mapPrimaryKeys(isoCountryCode));
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

}
