package edu.cornell.kfs.sys.xmladapters;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

public class BooleanNullPossibleXmlAdapter extends XmlAdapter<String, Boolean> {

    @Override
    public Boolean unmarshal(String v) throws Exception {
        if (StringUtils.isNotBlank(v)) {
            return StringUtils.equalsIgnoreCase(v, KFSConstants.ParameterValues.YES);
        } else {
            return null;
        }
    }

    @Override
    public String marshal(Boolean v) throws Exception {
        if (v == null) {
            return null;
        } else {
            return v ? KFSConstants.ParameterValues.YES : KFSConstants.ParameterValues.NO;
        }
    }

}
