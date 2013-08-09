package edu.cornell.kfs.vnd.businessobject.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.kuali.kfs.module.purap.businessobject.PurchaseOrderTransmissionMethod;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import org.kuali.rice.krad.service.KeyValuesService;

/**
 * Values Finder for <code>PurchaseOrderTransmissionMethod</code>.
 */

public class PurchaseOrderTransmissionMethodValuesFinder extends KeyValuesBase {

	private static final long serialVersionUID = 1L;

	/*
     * @see org.kuali.keyvalues.KeyValuesFinder#getKeyValues()
     */
    public List getKeyValues() {

        KeyValuesService boService = SpringContext.getBean(KeyValuesService.class);
        Collection codes = boService.findAll(PurchaseOrderTransmissionMethod.class);
        List labels = new ArrayList();
        labels.add(new ConcreteKeyValue("", ""));
        for (Iterator iter = codes.iterator(); iter.hasNext();) {
        	PurchaseOrderTransmissionMethod poTransmissionMethod = (PurchaseOrderTransmissionMethod) iter.next();
            labels.add(new ConcreteKeyValue(poTransmissionMethod.getPurchaseOrderTransmissionMethodCode(), poTransmissionMethod.getPurchaseOrderTransmissionMethodDescription()));
        }        
        return labels;
    }
	
}
