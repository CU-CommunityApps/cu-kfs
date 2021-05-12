package edu.cornell.kfs.pdp.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.krad.keyvalues.KeyValuesBase;
import org.kuali.kfs.pdp.PdpConstants.PayeeIdTypeCodes;
import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;

import edu.cornell.kfs.pdp.CUPdpTestConstants.TestPayeeIdTypeLabels;

public class TestPayeeAchIdTypeValuesFinder extends KeyValuesBase {
    private static final long serialVersionUID = 1L;

    @Override
    public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<>();
        keyValues.add(new ConcreteKeyValue(PayeeIdTypeCodes.EMPLOYEE, TestPayeeIdTypeLabels.EMPLOYEE));
        keyValues.add(new ConcreteKeyValue(PayeeIdTypeCodes.FEIN, TestPayeeIdTypeLabels.FEIN));
        keyValues.add(new ConcreteKeyValue(PayeeIdTypeCodes.SSN, TestPayeeIdTypeLabels.SSN));
        keyValues.add(new ConcreteKeyValue(PayeeIdTypeCodes.ENTITY, TestPayeeIdTypeLabels.ENTITY));
        keyValues.add(new ConcreteKeyValue(PayeeIdTypeCodes.VENDOR_ID, TestPayeeIdTypeLabels.VENDOR_ID));
        keyValues.add(new ConcreteKeyValue(PayeeIdTypeCodes.OTHER, TestPayeeIdTypeLabels.OTHER));
        return keyValues;
    }

}
