package edu.cornell.kfs.sys.service.impl;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.criteria.CriteriaLookupService;
import org.kuali.kfs.core.api.criteria.GenericQueryResults;
import org.kuali.kfs.core.api.criteria.PredicateFactory;
import org.kuali.kfs.core.api.criteria.QueryByCriteria;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
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

    private static final Logger LOG = LogManager.getLogger();
 
    protected BusinessObjectService businessObjectService;
    protected ConfigurationService configurationService;
    protected CriteriaLookupService criteriaLookupService;
    
    @Override
    public boolean isCountryActive(String countryCode) {
        if (isBlankCountryCode(countryCode)) {
            return false;
        }
        Country countryFound = getByPrimaryId(countryCode);
        if (ObjectUtils.isNotNull(countryFound)) {
            LOG.debug("isCountryActive: " +
                    MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.MESSAGE_COUNTRY_CODE_INDICATOR),
                            CUKFSConstants.FIPS, countryCode, (countryFound.isActive() ? "Active" : "Inactive")));
            return countryFound.isActive();
        } else {
            LOG.error("isCountryActive: " + MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.ERROR_NO_COUNTRY_FOUND_FOR_CODE), CUKFSConstants.FIPS, countryCode));
            return false;
        }
    }
    
    @Override
    public boolean isCountryInactive(String countryCode) {
        if (isBlankCountryCode(countryCode)) {
            return false;
        }
        Country countryFound = getByPrimaryId(countryCode);
        if (ObjectUtils.isNotNull(countryFound)) {
            LOG.debug("isCountryInactive: " +
                    MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.MESSAGE_COUNTRY_CODE_INDICATOR), 
                            CUKFSConstants.FIPS, countryCode, (countryFound.isActive() ? "Active" : "Inactive")));
            return !countryFound.isActive();
        } else {
            LOG.error("isCountryInactive: " + MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.ERROR_NO_COUNTRY_FOUND_FOR_CODE), CUKFSConstants.FIPS, countryCode));
            return false;
        }
    }
    
    private boolean isBlankCountryCode(String countryCode) {
        return isBlank(countryCode, CUKFSPropertyConstants.Location.COUNTRY_CODE);
    }
    
    private boolean isBlank(String countryValue, String propertyName) {
        if (StringUtils.isBlank(countryValue)) {
            LOG.debug("isBlank: {}",
                    () -> MessageFormat.format(getConfigurationService().getPropertyValueAsString(
                            CUKFSKeyConstants.NULL_OR_BLANK_CODE_PARAMETER), propertyName));
            return true;
        }
        return false;
    }
    
    protected Country getByPrimaryId(String countryCode) {
        if (isBlankCountryCode(countryCode)) {
            return null;
        }
        String uppercasedCode = countryCode.toUpperCase(Locale.US);
        return getBusinessObjectService().findByPrimaryKey(Country.class, mapPrimaryKeys(uppercasedCode));
    }
    
    @Override
    public String findCountryNameByCountryCode(String countryCode) {
        if (isBlankCountryCode(countryCode)) {
            return KFSConstants.EMPTY_STRING;
        }
        Country countryFound = getByPrimaryId(countryCode);
        if (ObjectUtils.isNotNull(countryFound)) {
            return countryFound.getName();
        } else {
            return MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.NAME_NOT_FOUND_FOR_COUNTRY_CODE), CUKFSConstants.FIPS, countryCode);
        }
    }
    
    @Override
    public List<String> findCountryCodesByCountryName(String countryName) {
        if (isBlank(countryName, CUKFSPropertyConstants.Location.COUNTRY_NAME)) {
            return List.of();
        }
        String trimmedName = StringUtils.trim(countryName);
        QueryByCriteria criteria = QueryByCriteria.Builder.fromPredicates(
                PredicateFactory.equalIgnoreCase(CUKFSPropertyConstants.Country.NAME, trimmedName),
                PredicateFactory.equal(KRADPropertyConstants.ACTIVE, KRADConstants.YES_INDICATOR_VALUE));
        GenericQueryResults<Country> results = criteriaLookupService.lookup(Country.class, criteria);
        List<Country> countries = results.getResults();
        return countries.stream()
                .map(Country::getCode)
                .collect(Collectors.toUnmodifiableList());
    }
    
    @Override
    public boolean countryExists(String countryCode) {
        if (isBlankCountryCode(countryCode)) {
            return false;
        }
        Country countryFound = getByPrimaryId(countryCode);
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

    public void setCriteriaLookupService(CriteriaLookupService criteriaLookupService) {
        this.criteriaLookupService = criteriaLookupService;
    }

}
