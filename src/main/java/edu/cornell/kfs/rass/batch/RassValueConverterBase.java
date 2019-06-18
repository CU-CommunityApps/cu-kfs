package edu.cornell.kfs.rass.batch;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.service.DataDictionaryService;
import org.kuali.kfs.krad.util.ObjectUtils;

public class RassValueConverterBase implements RassValueConverter {

    private static final Logger LOG = LogManager.getLogger(RassValueConverterBase.class);

    private DataDictionaryService dataDictionaryService;

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    @Override
    public Object convert(Class<? extends PersistableBusinessObject> businessObjectClass, String propertyName, Object propertyValue) {
        return cleanSimplePropertyValue(businessObjectClass, propertyName, propertyValue);
    }

    protected Object cleanSimplePropertyValue(
            Class<? extends PersistableBusinessObject> businessObjectClass, String propertyName, Object propertyValue) {
        if (propertyValue instanceof String) {
            return cleanStringValue(businessObjectClass, propertyName, (String) propertyValue);
        } else if (propertyValue instanceof Date) {
            return cleanDateValue(businessObjectClass, propertyName, (Date) propertyValue);
        } else if (propertyValue instanceof Boolean) {
            return cleanBooleanValue(businessObjectClass, propertyName, (Boolean) propertyValue);
        } else {
            return propertyValue;
        }
    }

    protected String cleanStringValue(
            Class<? extends PersistableBusinessObject> businessObjectClass, String propertyName, String propertyValue) {
        String cleanedValue = StringUtils.defaultIfBlank(propertyValue, null);
        Integer maxLength = dataDictionaryService.getAttributeMaxLength(businessObjectClass, propertyName);
        if (maxLength != null && maxLength > 0 && StringUtils.length(cleanedValue) > maxLength) {
            LOG.info("cleanStringValue, Truncating value for business object " + businessObjectClass.getName()
                    + " and property " + propertyName);
            cleanedValue = StringUtils.left(cleanedValue, maxLength);
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
