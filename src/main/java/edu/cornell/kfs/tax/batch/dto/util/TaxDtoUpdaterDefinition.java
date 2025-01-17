package edu.cornell.kfs.tax.batch.dto.util;

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.kuali.kfs.krad.bo.BusinessObject;

public class TaxDtoUpdaterDefinition<T> {

    private final Class<T> dtoClass;
    private final Class<? extends BusinessObject> targetBusinessObject;
    private final List<TaxDtoFieldUpdater<T, ?>> fieldUpdaters;

    public TaxDtoUpdaterDefinition(final Class<T> dtoClass, final Class<? extends BusinessObject> targetBusinessObject,
            final List<TaxDtoFieldUpdater<T, ?>> fieldUpdaters) {
        Validate.notNull(dtoClass, "dtoClass cannot be null");
        Validate.notNull(targetBusinessObject, "targetBusinessObject cannot be null");
        Validate.notNull(fieldUpdaters, "fieldUpdaters cannot be null");

        this.dtoClass = dtoClass;
        this.targetBusinessObject = targetBusinessObject;
        this.fieldUpdaters = List.copyOf(fieldUpdaters);
    }

    public Class<T> getDtoClass() {
        return dtoClass;
    }

    public Class<? extends BusinessObject> getTargetBusinessObject() {
        return targetBusinessObject;
    }

    public List<TaxDtoFieldUpdater<T, ?>> getFieldUpdaters() {
        return fieldUpdaters;
    }

}
