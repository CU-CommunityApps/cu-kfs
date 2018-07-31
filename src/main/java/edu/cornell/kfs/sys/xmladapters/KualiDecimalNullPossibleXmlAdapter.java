package edu.cornell.kfs.sys.xmladapters;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.util.type.KualiDecimal;

public class KualiDecimalNullPossibleXmlAdapter extends KualiDecimalXmlAdapter {
    
    @Override
    public KualiDecimal unmarshal(String v) throws Exception {
        if (StringUtils.isBlank(v)) {
            return null;
        } else {
            return super.unmarshal(v);
        }
    }

}
