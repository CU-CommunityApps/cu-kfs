package edu.cornell.kfs.sys.xmladapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.kuali.rice.core.api.util.type.KualiDecimal;

public class KualiDecimalXmlAdapter extends XmlAdapter<String, KualiDecimal> {

    @Override
    public KualiDecimal unmarshal(String v) throws Exception {
        return new KualiDecimal(v);
    }

    @Override
    public String marshal(KualiDecimal v) throws Exception {
        return v.toString();
    }

}
