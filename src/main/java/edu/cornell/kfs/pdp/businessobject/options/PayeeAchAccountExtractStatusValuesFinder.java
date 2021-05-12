package edu.cornell.kfs.pdp.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.krad.keyvalues.KeyValuesBase;
import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;

import edu.cornell.kfs.pdp.CUPdpConstants;

public class PayeeAchAccountExtractStatusValuesFinder extends KeyValuesBase {

    public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<>();
        keyValues.add(new ConcreteKeyValue(CUPdpConstants.PayeeAchAccountExtractStatuses.OPEN, CUPdpConstants.PayeeAchAccountExtractStatuses.OPEN));
        keyValues.add(new ConcreteKeyValue(CUPdpConstants.PayeeAchAccountExtractStatuses.CANCELED, CUPdpConstants.PayeeAchAccountExtractStatuses.CANCELED));
        keyValues.add(new ConcreteKeyValue(CUPdpConstants.PayeeAchAccountExtractStatuses.PROCESSED, CUPdpConstants.PayeeAchAccountExtractStatuses.PROCESSED));
        return keyValues;
    }

}
