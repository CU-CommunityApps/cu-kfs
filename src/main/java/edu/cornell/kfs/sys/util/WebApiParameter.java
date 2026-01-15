package edu.cornell.kfs.sys.util;

import java.util.Objects;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.sys.KFSPropertyConstants;

/**
 * Helper class for mapping a web service data input value to an Attribute Definition in the Data Dictionary.
 */
public final class WebApiParameter {

    private final String entryName;
    private final String attributeName;
    private final String value;
    private final boolean required;

    public WebApiParameter(final String entryName, final String attributeName, final String value,
            final boolean required) {
        Validate.notBlank(entryName, "entryName cannot be blank");
        Validate.notBlank(attributeName, "attributeName cannot be blank");
        this.entryName = entryName;
        this.attributeName = attributeName;
        this.value = value;
        this.required = required;
    }

    public String getEntryName() {
        return entryName;
    }

    public String getAttributeName() {
        return attributeName;
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

    public static WebApiParameter of(final Class<? extends BusinessObject> businessObjectClass,
            final String attributeName, final String value, final boolean required) {
        return new WebApiParameter(getDataDictionaryEntryName(businessObjectClass), attributeName, value, required);
    }

    public static WebApiParameter required(final Class<? extends BusinessObject> businessObjectClass,
            final String attributeName, final String value) {
        return new WebApiParameter(getDataDictionaryEntryName(businessObjectClass), attributeName, value, true);
    }

    public static WebApiParameter optional(final Class<? extends BusinessObject> businessObjectClass,
            final String attributeName, final String value) {
        return new WebApiParameter(getDataDictionaryEntryName(businessObjectClass), attributeName, value, false);
    }

    private static String getDataDictionaryEntryName(final Class<? extends BusinessObject> boClass) {
        Objects.requireNonNull(boClass, "boClass cannot be null");
        return boClass.getSimpleName();
    };

}
