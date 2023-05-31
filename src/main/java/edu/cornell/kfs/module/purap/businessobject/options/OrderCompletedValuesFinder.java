package edu.cornell.kfs.module.purap.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;

import edu.cornell.kfs.fp.CuFPConstants;

public class OrderCompletedValuesFinder extends KeyValuesBase {
	
	private static final long serialVersionUID = 1L;

	public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        keyValues.add(new ConcreteKeyValue(CuFPConstants.NO, "College/Unit approved for <b>service center processing.</b> (Select and click Approve to send to service center for processing.)"));
        keyValues.add(new ConcreteKeyValue(CuFPConstants.YES, "College/Unit has processed this request. (Select and click Approve to finalize this request.)"));
        return keyValues;
    }

}
