package edu.cornell.kfs.module.purap.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;

public class OrderCompletedValuesFinder extends KeyValuesBase {
	
	private static final long serialVersionUID = 1L;

	public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        
        keyValues.add(new ConcreteKeyValue("N", "I have <b>NOT</b> placed the order. (Click <b>approve</b> to submit to the FTC/BSC for processing) &nbsp; &nbsp; &nbsp;"));
        keyValues.add(new ConcreteKeyValue("Y", "I <b>HAVE</b> placed the order via pcard/eshop etc. (Click <b>approve</b> to finalize the order)"));
         
        return keyValues;
    }

}
