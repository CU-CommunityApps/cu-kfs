package edu.cornell.kfs.vnd.businessobject.options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;
import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;

public class EinvoiceIndicatorValuesFinder extends KeyValuesBase {
    private static final long serialVersionUID = 6328041347235067740L;

    @Override
    public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<>();
        keyValues.add(new ConcreteKeyValue(EinvoiceIndicator.NONE.code, EinvoiceIndicator.NONE.description));
        keyValues.add(new ConcreteKeyValue(EinvoiceIndicator.SFTP.code, EinvoiceIndicator.SFTP.description));
        keyValues.add(new ConcreteKeyValue(EinvoiceIndicator.WEB.code, EinvoiceIndicator.WEB.description));
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
            return Arrays.stream(EinvoiceIndicator.values())
                    .filter(indicator -> StringUtils.equalsIgnoreCase(code, indicator.code))
                    .findFirst()
                    .orElse(EinvoiceIndicator.NONE);
        }
        
    }
    
}
