package edu.cornell.kfs.module.purap.businessobject.xml;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "debitCreditCodeType", namespace = "http://www.kuali.org/kfs/sys/types")
@XmlEnum
public enum IWantDebitCreditCodeTypeXml {

    D, C;

    public String value() {
        return name();
    }

    public static IWantDebitCreditCodeTypeXml fromValue(String v) {
        return valueOf(v);
    }

}
