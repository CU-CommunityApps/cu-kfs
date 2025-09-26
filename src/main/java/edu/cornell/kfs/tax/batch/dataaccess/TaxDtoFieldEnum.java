package edu.cornell.kfs.tax.batch.dataaccess;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.bo.BusinessObject;

/**
 * Helper interface that is meant to be implemented by enum classes whose constants represent DB-mapped
 * tax DTO fields. By default, the enum constant's name will be used as the field name on the
 * associated business object and on the associated DTO.
 */
public interface TaxDtoFieldEnum {

    String name();

    default String getBusinessObjectFieldName() {
        return name();
    }

    default String getDtoFieldName() {
        return name();
    }

    default boolean needsExplicitAlias() {
        return !StringUtils.equals(getBusinessObjectFieldName(), getDtoFieldName());
    }

    default boolean needsEncryptedStorage() {
        return false;
    }

    Class<? extends BusinessObject> getMappedBusinessObjectClass();

}
