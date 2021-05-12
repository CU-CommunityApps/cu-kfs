package edu.cornell.kfs.sys.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSConstants.ConfidentialAttachmentTypeCodes;

/**
 * Values finder for denoting attachments as confidential.
 */
public class CuConfidentialAttachmentTypeValuesFinder extends KeyValuesBase {

    private static final long serialVersionUID = 1441269817871470558L;

    @Override
    public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        
        keyValues.add(new ConcreteKeyValue(
                ConfidentialAttachmentTypeCodes.NON_CONFIDENTIAL_ATTACHMENT_TYPE, CUKFSConstants.NON_CONFIDENTIAL_ATTACHMENT_TYPE_LABEL));
        keyValues.add(new ConcreteKeyValue(
                ConfidentialAttachmentTypeCodes.CONFIDENTIAL_ATTACHMENT_TYPE, ConfidentialAttachmentTypeCodes.CONFIDENTIAL_ATTACHMENT_TYPE));
        
        return keyValues;
    }

}
