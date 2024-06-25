package edu.cornell.kfs.module.purap.businessobject.xml;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "debitCreditCodeType", namespace = "http://www.kuali.org/kfs/sys/types")
@XmlEnum
public enum DebitCreditCodeType {

    D, C;

    public String value() {
        return name();
    }

    public static DebitCreditCodeType fromValue(String v) {
        return valueOf(v);
    }

}
