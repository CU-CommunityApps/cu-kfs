package edu.cornell.kfs.module.purap.businessobject.options;

import org.kuali.rice.kns.lookup.keyvalues.KeyValuesBase;
import org.kuali.rice.core.util.KeyLabelPair;
import java.util.ArrayList;
import java.util.List;
import org.kuali.kfs.module.purap.CUPurapConstants;

public class RequisitionAttachmentTypeValuesFinder extends KeyValuesBase {

    public List getKeyValues() {
        List keyValues = new ArrayList();

        keyValues.add(new KeyLabelPair(CUPurapConstants.AttachemntToVendorIndicators.DONT_SEND_TO_VENDOR, "No"));
        keyValues.add(new KeyLabelPair(CUPurapConstants.AttachemntToVendorIndicators.SEND_TO_VENDOR, "Yes"));       

        return keyValues;
    }

}
