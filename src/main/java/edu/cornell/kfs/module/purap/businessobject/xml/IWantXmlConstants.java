package edu.cornell.kfs.module.purap.businessobject.xml;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

public class IWantXmlConstants {

    public static final String IWANT_DOCUMENT_NAMESPACE = "http://www.kuali.org/kfs/purap/iWantDocument";

    public static final String IWANT_DOCUMENT_SCHEMA_LOCATION = "http://www.kuali.org/kfs/purap/iWantDocument iWantDocument.xsd";
    public static final String IWANT_XML_WRAPPER_XML_PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    @XmlType(name = "amountOrPercentType", namespace = "http://www.kuali.org/kfs/purap/types")
    @XmlEnum
    public enum IWantAmountOrPercentTypeXml {

        A, P;

        public String value() {
            return name();
        }

        public static IWantAmountOrPercentTypeXml fromValue(String v) {
            return valueOf(v);
        }

    }

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

}
