package edu.cornell.kfs.sys.xmladapters;

import org.apache.commons.lang3.StringUtils;
import org.kuali.rice.core.api.util.type.KualiDecimal;

public class NullableKualiDecimalXmlAdapter extends KualiDecimalXmlAdapter {
    
    @Override
    public KualiDecimal unmarshal(String v) throws Exception {
        if (StringUtils.isBlank(v)) {
            return null;
        } else {
            return super.unmarshal(v);
        }
    }
    
    @Override
    public String marshal(KualiDecimal v) throws Exception {
        if (v == null) {
            return StringUtils.EMPTY;
        } else {
            return super.marshal(v);
        }
        
    }

}
