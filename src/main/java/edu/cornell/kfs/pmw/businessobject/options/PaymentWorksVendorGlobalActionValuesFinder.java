package edu.cornell.kfs.pmw.businessobject.options;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants.PaymentWorksVendorGlobalAction;

public class PaymentWorksVendorGlobalActionValuesFinder extends KeyValuesBase {

    private static final long serialVersionUID = 1L;

    @Override
    public List<KeyValue> getKeyValues() {
        return Arrays.stream(PaymentWorksVendorGlobalAction.values())
                .map(action -> new ConcreteKeyValue(action.getActionTypeCode(), action.getActionLabel()))
                .collect(Collectors.toUnmodifiableList());
    }

}
