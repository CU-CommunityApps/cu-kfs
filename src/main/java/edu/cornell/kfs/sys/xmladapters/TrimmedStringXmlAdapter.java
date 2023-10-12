package edu.cornell.kfs.sys.xmladapters;

import org.apache.commons.lang3.StringUtils;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class TrimmedStringXmlAdapter extends XmlAdapter<String, String> {

    @Override
    public String unmarshal(String v) throws Exception {
        return cleanString(v);
    }

    @Override
    public String marshal(String v) throws Exception {
        return cleanString(v);
    }
    
    protected String cleanString(String stringToClean) {
        return StringUtils.trim(stringToClean);
    }

}
