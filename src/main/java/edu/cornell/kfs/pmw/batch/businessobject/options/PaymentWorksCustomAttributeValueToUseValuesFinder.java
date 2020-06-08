package edu.cornell.kfs.pmw.batch.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.krad.keyvalues.KeyValuesBase;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;

public class PaymentWorksCustomAttributeValueToUseValuesFinder extends KeyValuesBase {
    private static final long serialVersionUID = 2933785376056086534L;

    @Override
    public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValueList = new ArrayList<KeyValue>();
        keyValueList.add(new ConcreteKeyValue(PaymentWorksConstants.CustomAttributeValueToUse.FIELD_VALUE, 
                PaymentWorksConstants.CustomAttributeValueToUse.FIELD_VALUE));
        keyValueList.add(new ConcreteKeyValue(PaymentWorksConstants.CustomAttributeValueToUse.FILE, 
                PaymentWorksConstants.CustomAttributeValueToUse.FILE));
        return keyValueList;
    }
    
}
