package edu.cornell.kfs.sys.businessobject.options;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;
import org.kuali.kfs.krad.service.KeyValuesService;

import edu.cornell.kfs.sys.businessobject.KualiAddressType;

public class KualiAddressTypeValuesFinder extends KeyValuesBase {

    public List<KeyValue> getKeyValues() {
        List<KeyValue> addressTypeKeyLabels = new ArrayList<KeyValue>();
        addressTypeKeyLabels.add(new ConcreteKeyValue("", ""));

        Collection<KualiAddressType> addressTypes = SpringContext.getBean(KeyValuesService.class)
                .findAll(KualiAddressType.class);
        for (KualiAddressType addressType : addressTypes) {
            if (addressType.isActive()) {
                addressTypeKeyLabels.add(new ConcreteKeyValue(addressType.getAddressTypeCode(),
                        addressType.getAddressTypeCode() + " - " + addressType.getAddressTypeName()));
            }
        }

        return addressTypeKeyLabels;
    }

}
