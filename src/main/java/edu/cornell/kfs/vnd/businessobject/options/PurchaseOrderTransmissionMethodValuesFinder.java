package edu.cornell.kfs.vnd.businessobject.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kuali.kfs.module.purap.businessobject.PurchaseOrderTransmissionMethod;
import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;
import org.kuali.kfs.krad.service.KeyValuesService;

public class PurchaseOrderTransmissionMethodValuesFinder extends KeyValuesBase {
	private static final long serialVersionUID = 1L;
	protected KeyValuesService keyValuesService;

	@Override
    public List<KeyValue> getKeyValues() {
        Collection<PurchaseOrderTransmissionMethod> methods = keyValuesService.findAll(PurchaseOrderTransmissionMethod.class);
        List<KeyValue> labels = new ArrayList<>();
        labels.add(new ConcreteKeyValue("", ""));
        for (PurchaseOrderTransmissionMethod method : methods) {
            labels.add(new ConcreteKeyValue(method.getPurchaseOrderTransmissionMethodCode(),
                    method.getPurchaseOrderTransmissionMethodDescription()));
        }        
        return labels;
    }

    public void setKeyValuesService(KeyValuesService keyValuesService) {
        this.keyValuesService = keyValuesService;
    }
	
}
