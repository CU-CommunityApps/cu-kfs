package edu.cornell.kfs.module.purap.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.rice.core.util.KeyLabelPair;
import org.kuali.rice.kns.lookup.keyvalues.KeyValuesBase;

public class DepartmentLevelOrganizationValuesFinder extends KeyValuesBase {

    public List getKeyValues() {

        List keyValues = new ArrayList();

        keyValues.add(new KeyLabelPair("", ""));

        return keyValues;
    }

}
