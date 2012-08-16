package edu.cornell.kfs.vnd.businessobject.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.kuali.kfs.module.purap.businessobject.PurchaseOrderTransmissionMethod;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kns.lookup.keyvalues.KeyValuesBase;
import org.kuali.rice.kns.service.KeyValuesService;
import org.kuali.rice.core.util.KeyLabelPair;

/**
 * Values Finder for <code>PurchaseOrderTransmissionMethod</code>.
 */

public class PurchaseOrderTransmissionMethodValuesFinder extends KeyValuesBase {

    /*
     * @see org.kuali.keyvalues.KeyValuesFinder#getKeyValues()
     */
    public List getKeyValues() {

        KeyValuesService boService = SpringContext.getBean(KeyValuesService.class);
        Collection codes = boService.findAll(PurchaseOrderTransmissionMethod.class);
        List labels = new ArrayList();
        labels.add(new KeyLabelPair("", ""));
        for (Iterator iter = codes.iterator(); iter.hasNext();) {
        	PurchaseOrderTransmissionMethod poTransmissionMethod = (PurchaseOrderTransmissionMethod) iter.next();
            labels.add(new KeyLabelPair(poTransmissionMethod.getPurchaseOrderTransmissionMethodCode(), poTransmissionMethod.getPurchaseOrderTransmissionMethodDescription()));
        }        
        return labels;
    }
	
}
