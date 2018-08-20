package edu.cornell.kfs.fp.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;

import edu.cornell.kfs.fp.CuFPConstants;

public class CuCurrencyTypeValuesFinder extends KeyValuesBase {

    public List getKeyValues() {
        List keyValues = new ArrayList();
        keyValues.add(new ConcreteKeyValue(CuFPConstants.CURRENCY_CODE_U, CuFPConstants.CURRENCY_US_DOLLAR));
        keyValues.add(new ConcreteKeyValue(CuFPConstants.CURRENCY_CODE_C, CuFPConstants.CURRENCY_US_DOLLAR_TO_FOREIGN));
        keyValues.add(new ConcreteKeyValue(CuFPConstants.CURRENCY_CODE_F, CuFPConstants.CURRENCY_FOREIGN));
        return keyValues;
    }

}
