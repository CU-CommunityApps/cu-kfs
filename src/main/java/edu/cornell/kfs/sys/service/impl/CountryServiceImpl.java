package edu.cornell.kfs.sys.service.impl;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.businessobject.Country;

import edu.cornell.kfs.sys.CUKFSPropertyConstants;
import edu.cornell.kfs.sys.service.CountryService;

/**
 * CU Generic ISO-FIPS Country modification
 */
public class CountryServiceImpl implements CountryService {

    private static final Logger LOG = LogManager.getLogger(CountryServiceImpl.class);

    private static final String NO_COUNTRY_FOUND_FOR_CODE = "No Country found for code : {0}";
    private static final String COUNTRY_CODE_INDICATOR_MESSAGE = "Country code : {0} has status of {1}";
 
    protected BusinessObjectService businessObjectService;
    
    public boolean isCountryActive(String countryCode) {
        Country countryFound = getByPrimaryId(countryCode);

        if (ObjectUtils.isNotNull(countryFound)) {
            LOG.info("isISOCountryActive: " +
                    MessageFormat.format(COUNTRY_CODE_INDICATOR_MESSAGE, countryCode, (countryFound.isActive() ? "Active" : "Inactive")));
            return countryFound.isActive();
        } else {
            LOG.error("isISOCountryActive: " + MessageFormat.format(NO_COUNTRY_FOUND_FOR_CODE, countryCode));
            return false;
        }
    }    
    
    public Country getByPrimaryId(String countryCode) {
        return getBusinessObjectService().findByPrimaryKey(Country.class, mapPrimaryKeys(countryCode));
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

}
