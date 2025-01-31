package edu.cornell.kfs.sys.xmladapters;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class UppercasedStringXmlAdapter extends XmlAdapter<String, String> {

    @Override
    public String marshal(String value) throws Exception {
        return StringUtils.upperCase(value, Locale.US);
    }

    @Override
    public String unmarshal(String value) throws Exception {
        return StringUtils.upperCase(value, Locale.US);
    }

}
