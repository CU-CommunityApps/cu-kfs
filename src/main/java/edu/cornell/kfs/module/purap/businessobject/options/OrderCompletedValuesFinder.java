package edu.cornell.kfs.module.purap.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.rice.core.util.KeyLabelPair;
import org.kuali.rice.kns.lookup.keyvalues.KeyValuesBase;

public class OrderCompletedValuesFinder extends KeyValuesBase {

    public List getKeyValues() {
        List keyValues = new ArrayList();
        
        keyValues.add(new KeyLabelPair("N", "I have <b>NOT</b> placed the order. (Click <b>approve</b> to submit to the FTC/BSC for processing)"));
        keyValues.add(new KeyLabelPair("Y", "I have placed the order via pcard/eshop etc. (Click <b>approve</b> to finalize the order)"));
         
        return keyValues;
    }

}
