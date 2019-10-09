package edu.cornell.kfs.rass.batch;

import org.apache.commons.beanutils.ConvertUtils;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.springframework.beans.factory.InitializingBean;

/**
 * Custom converter that allows for properly handling values that need to map to a primitive field on the BO.
 * For example, null values will be automatically converted to a wrapped non-null default value (false, 0, etc.).
 */
public class RassPrimitiveValueConverter implements RassValueConverter, InitializingBean {

    private Class<?> primitiveType;

    public void setPrimitiveType(Class<?> primitiveType) {
        this.primitiveType = primitiveType;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (primitiveType == null) {
            throw new IllegalStateException("primitiveType cannot be null");
        } else if (!primitiveType.isPrimitive()) {
            throw new IllegalStateException("primitiveType does not represent a primitive type");
        }
    }

    @Override
    public Object convert(Class<? extends PersistableBusinessObject> businessObjectClass,
            RassPropertyDefinition propertyMapping, Object propertyValue) {
        return ConvertUtils.convert(propertyValue, primitiveType);
    }

}
