package edu.cornell.kfs.vnd.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;

public class EinvoiceIndicatorValuesFinder extends KeyValuesBase {

    @Override
    public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        for (EinvoiceIndicator indicator : EinvoiceIndicator.values()) {
            keyValues.add(new ConcreteKeyValue(indicator.code, indicator.description));
        }
        
        return keyValues;
    }
    
    public enum EinvoiceIndicator {
        NONE ("N", "None"),
        SFTP ("S", "SFTP"),
        WEB ("W", "Web");
        
        public final String code;
        public final String description;
        
        private EinvoiceIndicator(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public static EinvoiceIndicator getEinvoiceIndicatorFromCode(String code) {
            for (EinvoiceIndicator indicator : EinvoiceIndicator.values()) {
                if (StringUtils.equalsIgnoreCase(code, indicator.code) ) {
                    return indicator;
                }
            }
            return EinvoiceIndicator.NONE;
        }
        
    }

}
