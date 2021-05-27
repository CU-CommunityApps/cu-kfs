package edu.cornell.kfs.fp.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;

/**
 * This is a modified copy of the financials BankAccountTypeValuesFinder class,
 * which was removed in KualiCo's 2021-01-28 patch. Since we still rely on it,
 * we have added this customized version that is compatible with the migration
 * of KEW to KFS.
 */
public class CuBankAccountTypeValuesFinder extends KeyValuesBase {

    @Override
    public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<>();
        keyValues.add(new ConcreteKeyValue("C", "Checking"));
        keyValues.add(new ConcreteKeyValue("S", "Savings"));
        return keyValues;
    }

}
