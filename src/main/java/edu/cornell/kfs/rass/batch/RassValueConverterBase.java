package edu.cornell.kfs.rass.batch;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;

import edu.cornell.kfs.sys.CUKFSConstants;

public class RassValueConverterBase implements RassValueConverter {

    private static final Logger LOG = LogManager.getLogger(RassValueConverterBase.class);

    private DataDictionaryService dataDictionaryService;

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    @Override
    public Object convert(Class<? extends PersistableBusinessObject> businessObjectClass, RassPropertyDefinition propertyMapping, Object propertyValue) {
        return cleanSimplePropertyValue(businessObjectClass, propertyMapping, propertyValue);
    }

    protected Object cleanSimplePropertyValue(
            Class<? extends PersistableBusinessObject> businessObjectClass, RassPropertyDefinition propertyMapping, Object propertyValue) {
        Object cleanedValue = propertyValue;
        if (propertyValue instanceof String) {
            cleanedValue = cleanStringValue(businessObjectClass, propertyMapping, (String) propertyValue);
        } else if (propertyValue instanceof Date) {
            cleanedValue = cleanDateValue(businessObjectClass, propertyMapping.getBoPropertyName(), (Date) propertyValue);
        } else if (propertyValue instanceof Boolean) {
            cleanedValue =  cleanBooleanValue(businessObjectClass, propertyMapping.getBoPropertyName(), (Boolean) propertyValue);
        } else if (LOG.isDebugEnabled()) {
            String propertyNameForLogging = StringUtils.defaultIfBlank(propertyMapping.getBoPropertyName(), "[Blank]");
            if (ObjectUtils.isNull(propertyValue)) {
                LOG.debug("cleanSimplePropertyValue, property " +  propertyNameForLogging + " is null on business object " 
                        + businessObjectClass.getName());
            } else {
                LOG.debug("cleanSimplePropertyValue, no cleaning for property " +  propertyNameForLogging 
                        + "  on business object " + businessObjectClass.getName() + " (value: " + propertyValue.toString() + ")");
            }
        }
        return cleanedValue;
    }

    protected String cleanStringValue(
            Class<? extends PersistableBusinessObject> businessObjectClass, RassPropertyDefinition propertyMapping, String propertyValue) {
        String propertyName = propertyMapping.getBoPropertyName();
        String cleanedValue = StringUtils.defaultIfBlank(propertyValue, null);
        if (ObjectUtils.isNotNull(cleanedValue)) {
            cleanedValue = cleanedValue.trim();
        }
        Integer maxLength = dataDictionaryService.getAttributeMaxLength(businessObjectClass, propertyName);
        if (LOG.isDebugEnabled()) {
            LOG.debug("cleanStringValue, businessObjectClass: " + businessObjectClass + " propertyName: " + propertyName + " has a maximum size of " + maxLength);
        }
        if (maxLength != null && maxLength > 0 && StringUtils.length(cleanedValue) > maxLength) {
            if (propertyMapping.isTruncateWithEllipsis()) {
                cleanedValue = StringUtils.left(cleanedValue, maxLength - CUKFSConstants.ELLIPSIS.length()) + CUKFSConstants.ELLIPSIS;
            } else {
                cleanedValue = StringUtils.left(cleanedValue, maxLength);
            }
            LOG.info("cleanStringValue, Truncating value for business object " + businessObjectClass.getName() 
                + " and property " + propertyName + " to a value of '" + cleanedValue + "'");
        }
        return cleanedValue;
    }

    protected java.sql.Date cleanDateValue(
            Class<? extends PersistableBusinessObject> businessObjectClass, String propertyName, Date propertyValue) {
        return new java.sql.Date(propertyValue.getTime());
    }

    protected Boolean cleanBooleanValue(
            Class<? extends PersistableBusinessObject> businessObjectClass, String propertyName, Boolean propertyValue) {
        if (propertyValue == null) {
            return Boolean.FALSE;
        }
        return propertyValue;
    }

}
