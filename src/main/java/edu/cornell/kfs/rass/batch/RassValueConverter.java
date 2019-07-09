package edu.cornell.kfs.rass.batch;

import org.kuali.kfs.krad.bo.PersistableBusinessObject;

public interface RassValueConverter {
	 Object convert(Class<? extends PersistableBusinessObject> businessObjectClass, String propertyName, Object propertyValue);
}
