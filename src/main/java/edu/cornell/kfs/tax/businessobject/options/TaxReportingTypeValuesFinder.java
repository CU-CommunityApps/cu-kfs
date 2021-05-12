package edu.cornell.kfs.tax.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;

import edu.cornell.kfs.tax.CUTaxConstants;

/**
 * Custom values finder that allows for selecting
 * a particular type of tax reporting (1099, 1042S, etc.).
 */
public class TaxReportingTypeValuesFinder extends KeyValuesBase {
    private static final long serialVersionUID = -7820959853151286197L;

    @Override
    public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        
        keyValues.add(new ConcreteKeyValue(KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING));
        keyValues.add(new ConcreteKeyValue(CUTaxConstants.TAX_TYPE_1099, CUTaxConstants.TAX_TYPE_1099));
        keyValues.add(new ConcreteKeyValue(CUTaxConstants.TAX_TYPE_1042S, CUTaxConstants.TAX_TYPE_1042S));
        
        return keyValues;
    }

}
