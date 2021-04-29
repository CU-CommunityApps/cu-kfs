package edu.cornell.kfs.module.purap.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;

public class AttachmentDescriptionValuesFinder extends KeyValuesBase {

	private static final long serialVersionUID = 1L;

	/**
     * 
     * @see org.kuali.kfs.kns.lookup.keyvalues.KeyValuesFinder#getKeyValues()
     */
    public List<KeyValue> getKeyValues() {

        List<KeyValue> keyValues = new ArrayList<KeyValue>();

        keyValues.add(new ConcreteKeyValue("Note", "Note"));
        keyValues.add(new ConcreteKeyValue("Quote", "Quote"));
        keyValues.add(new ConcreteKeyValue("Invoice", "Invoice"));
        keyValues.add(new ConcreteKeyValue("Contract/Agreement", "Contract/Agreement"));
        keyValues.add(new ConcreteKeyValue("Sole Source", "Sole Source"));
        keyValues.add(new ConcreteKeyValue("Specifications", "Specifications"));
        keyValues.add(new ConcreteKeyValue("Other", "Other"));

        return keyValues;
    }

}
