package edu.cornell.kfs.ksr.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;

public class SecurityGroupTabValuesFinder extends KeyValuesBase {

    private static final long serialVersionUID = 1825873598309848494L;

    @Override
    public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();

        keyValues.add(new ConcreteKeyValue("", ""));

        // TODO figure out how to populate values

        return keyValues;
    }

}
