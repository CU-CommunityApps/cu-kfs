package edu.cornell.kfs.module.purap.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;

public class YesNoValuesFinder extends KeyValuesBase {

	private static final long serialVersionUID = 1L;

    public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        
        keyValues.add(new ConcreteKeyValue("N", "No"));
        keyValues.add(new ConcreteKeyValue("Y", "Yes"));
        
        return keyValues;
    }
}
