package edu.cornell.kfs.vnd.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;

public class EinvoiceIndicatorValuesFinder extends KeyValuesBase {
    private static final long serialVersionUID = 6328041347235067740L;
    private static final Logger LOG = LogManager.getLogger();

    @Override
    public List<KeyValue> getKeyValues() {
        LOG.debug("getKeyValues, entering");
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
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
            for (EinvoiceIndicator indicator : EinvoiceIndicator.values()) {
                if (StringUtils.equalsIgnoreCase(code, indicator.code) ) {
                    return indicator;
                }
            }
            return EinvoiceIndicator.NONE;
        }
        
    }
    
}
