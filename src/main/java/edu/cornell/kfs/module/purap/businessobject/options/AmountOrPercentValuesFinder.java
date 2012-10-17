package edu.cornell.kfs.module.purap.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.module.purap.CUPurapConstants;
import org.kuali.rice.core.util.KeyLabelPair;
import org.kuali.rice.kns.lookup.keyvalues.KeyValuesBase;

public class AmountOrPercentValuesFinder extends KeyValuesBase {

    /**
     * @see org.kuali.rice.kns.lookup.keyvalues.KeyValuesFinder#getKeyValues()
     */
    public List getKeyValues() {
        List keyValues = new ArrayList();
        
        keyValues.add(new KeyLabelPair(CUPurapConstants.AMOUNT, "Amount"));
        keyValues.add(new KeyLabelPair(CUPurapConstants.PERCENT, "Percent"));
        
        return keyValues;
    }

}
