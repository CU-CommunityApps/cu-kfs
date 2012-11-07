package edu.cornell.kfs.module.purap.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.rice.core.util.KeyLabelPair;
import org.kuali.rice.kns.lookup.keyvalues.KeyValuesBase;

public class OrderCompletedValuesFinder extends KeyValuesBase {

    public List getKeyValues() {
        List keyValues = new ArrayList();
        
        keyValues.add(new KeyLabelPair("Y", "I have placed the order through eShop or other means"));
        keyValues.add(new KeyLabelPair("N", "I have not placed an order"));
        
        return keyValues;
    }

}
