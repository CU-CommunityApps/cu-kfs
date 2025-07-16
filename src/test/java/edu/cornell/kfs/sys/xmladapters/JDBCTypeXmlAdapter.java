package edu.cornell.kfs.sys.xmladapters;

import java.sql.JDBCType;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class JDBCTypeXmlAdapter extends XmlAdapter<String, JDBCType> {

    @Override
    public String marshal(JDBCType value) throws Exception {
        return value != null ? value.getName() : null;
    }

    @Override
    public JDBCType unmarshal(String value) throws Exception {
        return StringUtils.isNotBlank(value) ? JDBCType.valueOf(value.toUpperCase(Locale.US)) : null;
    }

}
