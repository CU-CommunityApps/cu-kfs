package edu.cornell.kfs.rass.batch;

import org.kuali.kfs.krad.bo.PersistableBusinessObject;

public class RassBooleanConverter extends RassValueConverterBase {

    @Override
    public Object convert(final Class<? extends PersistableBusinessObject> businessObjectClass,
            final RassPropertyDefinition propertyMapping, final Object propertyValue) {
        return cleanBooleanValue(businessObjectClass, propertyMapping.getBoPropertyName(), (Boolean) propertyValue);
    }

}
