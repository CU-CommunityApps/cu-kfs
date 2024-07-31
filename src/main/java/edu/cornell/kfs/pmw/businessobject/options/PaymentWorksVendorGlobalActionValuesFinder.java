package edu.cornell.kfs.pmw.businessobject.options;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants.PaymentWorksVendorGlobalAction;

public class PaymentWorksVendorGlobalActionValuesFinder extends KeyValuesBase {

    private static final long serialVersionUID = 1L;

    private static final String EMPTY_OPTION_LABEL = "(Select...)";

    @Override
    public List<KeyValue> getKeyValues() {
        final Stream.Builder<KeyValue> keyValues = Stream.builder();
        keyValues.add(new ConcreteKeyValue(KFSConstants.EMPTY_STRING, EMPTY_OPTION_LABEL));
        for (final PaymentWorksVendorGlobalAction actionType : PaymentWorksVendorGlobalAction.values()) {
            keyValues.add(new ConcreteKeyValue(actionType.name(), actionType.actionLabel));
        }
        return keyValues.build().collect(Collectors.toUnmodifiableList());
    }

}
