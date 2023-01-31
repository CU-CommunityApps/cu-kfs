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
        keyValues.add(new ConcreteKeyValue("N", "College/Unit <b>approved</b> for processing by service center. (Click <b>Approve</b> to automatically route to service center for processing.) &nbsp; &nbsp; &nbsp;"));
        keyValues.add(new ConcreteKeyValue("Y", "College/Unit <b>has</b> processed this request. (Do <b>not</b> route to service center. Click <b>Approve<b>.)")); 
        return keyValues;
    }

}
