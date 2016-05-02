package edu.cornell.kfs.vnd.businessobject.options;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;

import edu.cornell.kfs.vnd.CUVendorConstants;

public class CuProcurementMethodValuesFinder extends KeyValuesBase {

    private static final long serialVersionUID = -9206370884410077180L;

    /**
     * @see org.kuali.rice.krad.keyvalues.KeyValuesFinder#getKeyValues()
     */
    public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>(CUVendorConstants.PROC_METHODS_LABEL_MAP.size());
        
        // In this case, the source map is a "linked" one, so we can rely on consistent iteration order.
        for (Map.Entry<String, String> procMethod : CUVendorConstants.PROC_METHODS_LABEL_MAP.entrySet()) {
            keyValues.add(new ConcreteKeyValue(procMethod.getKey(), procMethod.getValue()));
        }
        
        return keyValues;
    }

}
