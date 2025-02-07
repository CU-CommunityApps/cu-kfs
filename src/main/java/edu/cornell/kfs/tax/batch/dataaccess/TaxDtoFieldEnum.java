package edu.cornell.kfs.tax.batch.dataaccess;

import org.kuali.kfs.krad.bo.BusinessObject;

/**
 * Helper interface that is meant to be implemented by enum classes whose constants represent DB-mapped
 * tax DTO fields. By default, the enum constant's name will be used as the field name on the
 * associated business object.
 */
public interface TaxDtoFieldEnum {

    String name();

    default String getFieldName() {
        return name();
    }

    Class<? extends BusinessObject> getMappedBusinessObjectClass();

}
