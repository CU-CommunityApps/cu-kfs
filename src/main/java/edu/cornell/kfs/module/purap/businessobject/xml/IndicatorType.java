package edu.cornell.kfs.module.purap.businessobject.xml;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "indicatorType", namespace = "http://www.kuali.org/kfs/sys/types")
@XmlEnum
public enum IndicatorType {

    Y, N;

    public String value() {
        return name();
    }

    public static IndicatorType fromValue(String v) {
        return valueOf(v);
    }

}
