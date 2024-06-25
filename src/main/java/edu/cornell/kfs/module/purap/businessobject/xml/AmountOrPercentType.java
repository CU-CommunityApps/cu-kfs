package edu.cornell.kfs.module.purap.businessobject.xml;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "amountOrPercentType", namespace = "http://www.kuali.org/kfs/purap/types")
@XmlEnum
public enum AmountOrPercentType {

    A, P;

    public String value() {
        return name();
    }

    public static AmountOrPercentType fromValue(String v) {
        return valueOf(v);
    }

}
