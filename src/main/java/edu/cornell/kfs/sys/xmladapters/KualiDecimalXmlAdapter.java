package edu.cornell.kfs.sys.xmladapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.kuali.rice.core.api.util.type.KualiDecimal;

public class KualiDecimalXmlAdapter extends XmlAdapter<String, KualiDecimal> {

    @Override
    public KualiDecimal unmarshal(String v) throws Exception {
        Double doubleValue = new Double(v);
        return new KualiDecimal(doubleValue);
    }

    @Override
    public String marshal(KualiDecimal v) throws Exception {
        return v.toString();
    }

}
