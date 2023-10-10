package edu.cornell.kfs.sys.xmladapters;

import com.amazonaws.util.StringUtils;

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
    
    protected String cleanString(String streamToClean) {
        return StringUtils.trim(streamToClean);
    }

}
