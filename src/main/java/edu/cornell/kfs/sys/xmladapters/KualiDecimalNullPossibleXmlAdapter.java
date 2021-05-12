package edu.cornell.kfs.sys.xmladapters;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

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
