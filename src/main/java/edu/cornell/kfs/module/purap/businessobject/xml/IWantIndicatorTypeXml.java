package edu.cornell.kfs.module.purap.businessobject.xml;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "indicatorType", namespace = "http://www.kuali.org/kfs/sys/types")
@XmlEnum
public enum IWantIndicatorTypeXml {

    Y, N;

    public String value() {
        return name();
    }

    public static IWantIndicatorTypeXml fromValue(String v) {
        return valueOf(v);
    }

}
