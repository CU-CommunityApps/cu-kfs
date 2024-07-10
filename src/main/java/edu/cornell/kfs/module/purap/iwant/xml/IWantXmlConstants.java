package edu.cornell.kfs.module.purap.iwant.xml;

public class IWantXmlConstants {

    public static final String IWANT_DOCUMENT_NAMESPACE = "http://www.kuali.org/kfs/purap/iWantDocument";

    public static final String IWANT_DOCUMENT_SCHEMA_LOCATION = "http://www.kuali.org/kfs/purap/iWantDocument iWantDocument.xsd";
    public static final String IWANT_XML_WRAPPER_XML_PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    
    /*
     * @todo remove this, I think
     */
    public enum IWantIndicatorTypeXml {
        Y, N;

        public String value() {
            return name();
        }
        
        public boolean toBoolean() {
            return this == Y;
        }

        public static IWantIndicatorTypeXml fromValue(String v) {
            return valueOf(v);
        }
        
        public static String fromBoolean(boolean booleanVal) {
            return booleanVal ? Y.toString() : N.toString();
        }
    }

}
