package edu.cornell.kfs.module.purap.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;

public class RequisitionAttachmentTypeValuesFinder extends KeyValuesBase {

	private static final long serialVersionUID = 1L;

	public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();

        keyValues.add(new ConcreteKeyValue(CUPurapConstants.AttachemntToVendorIndicators.DONT_SEND_TO_VENDOR, "No"));
        keyValues.add(new ConcreteKeyValue(CUPurapConstants.AttachemntToVendorIndicators.SEND_TO_VENDOR, "Yes"));       

        return keyValues;
    }

}
