package edu.cornell.kfs.vnd.businessobject.options;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;

import edu.cornell.kfs.vnd.CUVendorConstants;

public class CuLocaleValuesFinder extends KeyValuesBase {

    private static final long serialVersionUID = 600417262632569839L;

    /**
     * @see org.kuali.rice.krad.keyvalues.KeyValuesFinder#getKeyValues()
     */
    public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        
        keyValues.add(new ConcreteKeyValue(KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING));
        // In this case, the source map is a "linked" one, so we can rely on consistent iteration order.
        for (Map.Entry<String, String> locale : CUVendorConstants.LOCALES_LABEL_MAP.entrySet()) {
            keyValues.add(new ConcreteKeyValue(locale.getKey(), locale.getValue()));
        }
        
        return keyValues;
    }

}
