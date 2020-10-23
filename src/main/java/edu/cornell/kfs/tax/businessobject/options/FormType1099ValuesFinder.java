package edu.cornell.kfs.tax.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;

import edu.cornell.kfs.tax.FormTypes1099;

public class FormType1099ValuesFinder extends KeyValuesBase {
  private static final long serialVersionUID = 2880415540151030542L;

  @Override
  public List<KeyValue> getKeyValues() {
    List<KeyValue> keyValues = new ArrayList<KeyValue>();
    keyValues.add(new ConcreteKeyValue(StringUtils.EMPTY, StringUtils.EMPTY));

    for (FormTypes1099 type : FormTypes1099.values()) {
      keyValues.add(new ConcreteKeyValue(type.formCode, type.formDescription));
    }

    return keyValues;
  }

}
