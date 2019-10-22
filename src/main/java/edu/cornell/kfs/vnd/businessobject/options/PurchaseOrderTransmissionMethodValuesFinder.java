package edu.cornell.kfs.vnd.businessobject.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.kuali.kfs.module.purap.businessobject.PurchaseOrderTransmissionMethod;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;
import org.kuali.kfs.krad.service.KeyValuesService;

/**
 * Values Finder for <code>PurchaseOrderTransmissionMethod</code>.
 */

public class PurchaseOrderTransmissionMethodValuesFinder extends KeyValuesBase {

	private static final long serialVersionUID = 1L;

    public List<KeyValue> getKeyValues() {
        KeyValuesService boService = SpringContext.getBean(KeyValuesService.class);
        Collection<PurchaseOrderTransmissionMethod> methods = boService.findAll(PurchaseOrderTransmissionMethod.class);
        List<KeyValue> labels = new ArrayList<>();
        labels.add(new ConcreteKeyValue("", ""));
        for (PurchaseOrderTransmissionMethod method : methods) {
            labels.add(new ConcreteKeyValue(method.getPurchaseOrderTransmissionMethodCode(),
                    method.getPurchaseOrderTransmissionMethodDescription()));
        }        
        return labels;
    }
	
}
