package edu.cornell.kfs.sys.util;

import java.util.Objects;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.sys.KFSPropertyConstants;

public final class WebApiProperty {

    private final Class<? extends BusinessObject> businessObjectClass;
    private final String propertyName;
    private final String value;
    private final boolean required;

    public WebApiProperty(final Class<? extends BusinessObject> businessObjectClass,
            final String propertyName, final String value, final boolean required) {
        Objects.requireNonNull(businessObjectClass, "businessObjectClass cannot be null");
        Validate.notBlank(propertyName, "propertyName cannot be blank");
        this.businessObjectClass = businessObjectClass;
        this.propertyName = propertyName;
        this.value = value;
        this.required = required;
    }

    public Class<? extends BusinessObject> getBusinessObjectClass() {
        return businessObjectClass;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getValue() {
        return value;
    }

    public boolean isRequired() {
        return required;
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .setExcludeFieldNames(KFSPropertyConstants.VALUE)
                .build();
    }

    public static WebApiProperty of(final Class<? extends BusinessObject> businessObjectClass,
            final String propertyName, final String value, final boolean required) {
        return new WebApiProperty(businessObjectClass, propertyName, value, required);
    }

    public static WebApiProperty required(final Class<? extends BusinessObject> businessObjectClass,
            final String propertyName, final String value) {
        return new WebApiProperty(businessObjectClass, propertyName, value, true);
    }

    public static WebApiProperty optional(final Class<? extends BusinessObject> businessObjectClass,
            final String propertyName, final String value) {
        return new WebApiProperty(businessObjectClass, propertyName, value, false);
    }

}
