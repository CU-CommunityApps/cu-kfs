package edu.cornell.kfs.sys.xmladapters;

import org.apache.commons.lang3.StringUtils;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class IWantAmountOrPercentXmlAdapter extends XmlAdapter<String, String> {
    public static final String AMOUNT_CODE = "A";
    public static final String PERCENT_CODE = "P";

    @Override
    public String unmarshal(String v) throws Exception {
        if (StringUtils.equalsIgnoreCase(AMOUNT_CODE, v)) {
            return CUPurapConstants.AMOUNT;
        } else if (StringUtils.equalsIgnoreCase(PERCENT_CODE, v)) {
            return CUPurapConstants.PERCENT;
        } else {
            return StringUtils.EMPTY;
        }
    }

    @Override
    public String marshal(String v) throws Exception {
        if (StringUtils.equalsIgnoreCase(CUPurapConstants.AMOUNT, v)) {
            return AMOUNT_CODE;
        } else if (StringUtils.equalsIgnoreCase(CUPurapConstants.PERCENT, v)) {
            return PERCENT_CODE;
        } else {
            return StringUtils.EMPTY;
        }
    }

}
