package edu.cornell.kfs.sys.xmladapters;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.util.type.KualiInteger;

public class KualiIntegerXmlAdapter extends XmlAdapter<String, KualiInteger> {

    @Override
    public KualiInteger unmarshal(String v) throws Exception {
        if (StringUtils.isNotBlank(v)) {
            return new KualiInteger(v);
        } else {
            return KualiInteger.ZERO;
        }
    }

    @Override
    public String marshal(KualiInteger v) throws Exception {
        return v.toString();
    }

}
