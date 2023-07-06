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

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSKeyConstants;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;
import edu.cornell.kfs.sys.businessobject.ISOCountry;
import edu.cornell.kfs.sys.service.ISOCountryService;

/**
 * CU Generic ISO-FIPS Country modification
 */
public class ISOCountryServiceImpl implements ISOCountryService {

    private static final Logger LOG = LogManager.getLogger();
    
    protected BusinessObjectService businessObjectService;
    protected ConfigurationService configurationService;
    protected CriteriaLookupService criteriaLookupService;
    
    @Override
    public boolean isISOCountryActive(String isoCountryCode) {
        if (isBlankCountryCode(isoCountryCode)) {
            return false;
        }
        ISOCountry isoCountryFound = getByPrimaryId(isoCountryCode);
        if (ObjectUtils.isNotNull(isoCountryFound)) {
            LOG.debug("isISOCountryActive: " + MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.MESSAGE_COUNTRY_CODE_INDICATOR),
                    CUKFSConstants.ISO, isoCountryCode, (isoCountryFound.isActive() ? "Active" : "Inactive")));
            return isoCountryFound.isActive();
        } else {
            LOG.error("isISOCountryActive: " + MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.ERROR_NO_COUNTRY_FOUND_FOR_CODE), CUKFSConstants.ISO, isoCountryCode));
            return false;
        }
    }

    @Override
    public boolean isISOCountryInactive(String isoCountryCode) {
        if (isBlankCountryCode(isoCountryCode)) {
            return false;
        }
        ISOCountry isoCountryFound = getByPrimaryId(isoCountryCode);
        if (ObjectUtils.isNotNull(isoCountryFound)) {
            LOG.debug("isISOCountryInactive: " + MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.MESSAGE_COUNTRY_CODE_INDICATOR),
                    CUKFSConstants.ISO, isoCountryCode, (isoCountryFound.isActive() ? "Active" : "Inactive")));
            return !isoCountryFound.isActive();
        } else {
            LOG.error("isISOCountryInactive: " + MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.ERROR_NO_COUNTRY_FOUND_FOR_CODE), CUKFSConstants.ISO, isoCountryCode));
            return false;
        }
    }
    
    private boolean isBlankCountryCode(String isoCountryCode) {
        return isBlank(isoCountryCode, CUKFSPropertyConstants.Location.ISO_COUNTRY_CODE);
    }
    
    private boolean isBlank(String isoCountryValue, String propertyName) {
        if (StringUtils.isBlank(isoCountryValue)) {
            LOG.debug("isBlank: {}",
                    () -> MessageFormat.format(getConfigurationService().getPropertyValueAsString(
                            CUKFSKeyConstants.NULL_OR_BLANK_CODE_PARAMETER), propertyName));
            return true;
        }
        return false;
    }
    
    protected ISOCountry getByPrimaryId(String isoCountryCode) {
        if (isBlankCountryCode(isoCountryCode)) {
            return null;
        }
        String uppercasedCode = isoCountryCode.toUpperCase(Locale.US);
        return getBusinessObjectService().findByPrimaryKey(ISOCountry.class, mapPrimaryKeys(uppercasedCode));
    }
    
    @Override
    public String findISOCountryNameByCountryCode(String isoCountryCode) {
        if (isBlankCountryCode(isoCountryCode)) {
            return KFSConstants.EMPTY_STRING;
        }
        ISOCountry isoCountryFound = getByPrimaryId(isoCountryCode);
        if (ObjectUtils.isNotNull(isoCountryFound)) {
            return isoCountryFound.getName();
        } else {
            return MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.NAME_NOT_FOUND_FOR_COUNTRY_CODE), CUKFSConstants.ISO, isoCountryCode);
        }
    }

    @Override
    public List<String> findISOCountryCodesByCountryName(String isoCountryName) {
        if (isBlank(isoCountryName, CUKFSPropertyConstants.Location.ISO_COUNTRY_NAME)) {
            return List.of();
        }
        String trimmedName = StringUtils.trim(isoCountryName);
        QueryByCriteria criteria = QueryByCriteria.Builder.fromPredicates(
                PredicateFactory.equalIgnoreCase(CUKFSPropertyConstants.ISOCountry.NAME, trimmedName),
                PredicateFactory.equal(KRADPropertyConstants.ACTIVE, KRADConstants.YES_INDICATOR_VALUE));
        GenericQueryResults<ISOCountry> results = criteriaLookupService.lookup(ISOCountry.class, criteria);
        List<ISOCountry> countries = results.getResults();
        return countries.stream()
                .map(ISOCountry::getCode)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public boolean isoCountryExists(String isoCountryCode) {
        if (isBlankCountryCode(isoCountryCode)) {
            return false;
        }
        ISOCountry isoCountryFound = getByPrimaryId(isoCountryCode);
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

    public void setCriteriaLookupService(CriteriaLookupService criteriaLookupService) {
        this.criteriaLookupService = criteriaLookupService;
    }

}
