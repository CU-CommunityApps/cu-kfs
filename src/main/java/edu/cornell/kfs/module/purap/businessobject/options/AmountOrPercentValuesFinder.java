package edu.cornell.kfs.module.purap.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;

public class AmountOrPercentValuesFinder extends KeyValuesBase {

	private static final long serialVersionUID = 1L;

	/**
     * @see org.kuali.kfs.kns.lookup.keyvalues.KeyValuesFinder#getKeyValues()
     */
    public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        
        keyValues.add(new ConcreteKeyValue(CUPurapConstants.AMOUNT, "Amount"));
        keyValues.add(new ConcreteKeyValue(CUPurapConstants.PERCENT, "Percent"));
        
        return keyValues;
    }

}
