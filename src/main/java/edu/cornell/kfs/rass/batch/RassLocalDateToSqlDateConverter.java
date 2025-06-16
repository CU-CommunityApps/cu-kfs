package edu.cornell.kfs.rass.batch;

import java.time.LocalDate;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;

public class RassLocalDateToSqlDateConverter extends RassValueConverterBase {

    @Override
    public Object convert(final Class<? extends PersistableBusinessObject> businessObjectClass,
            final RassPropertyDefinition propertyMapping, final Object propertyValue) {
        return cleanDateValue(businessObjectClass, propertyMapping.getBoPropertyName(), (LocalDate) propertyValue);
    }

}
