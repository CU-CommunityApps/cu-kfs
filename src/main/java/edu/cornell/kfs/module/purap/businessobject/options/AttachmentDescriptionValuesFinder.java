package edu.cornell.kfs.module.purap.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.rice.core.util.KeyLabelPair;
import org.kuali.rice.kns.lookup.keyvalues.KeyValuesBase;

public class AttachmentDescriptionValuesFinder extends KeyValuesBase {

    /**
     * 
     * @see org.kuali.rice.kns.lookup.keyvalues.KeyValuesFinder#getKeyValues()
     */
    public List<KeyLabelPair> getKeyValues() {

        List<KeyLabelPair> keyValues = new ArrayList();

        keyValues.add(new KeyLabelPair("Note", "Note"));
        keyValues.add(new KeyLabelPair("Quote", "Quote"));
        keyValues.add(new KeyLabelPair("Invoice", "Invoice"));
        keyValues.add(new KeyLabelPair("Contract/Agreement", "Contract/Agreement"));
        keyValues.add(new KeyLabelPair("Sole Source", "Sole Source"));
        keyValues.add(new KeyLabelPair("Specifications", "Specifications"));
        keyValues.add(new KeyLabelPair("Other", "Other"));

        return keyValues;
    }

}
