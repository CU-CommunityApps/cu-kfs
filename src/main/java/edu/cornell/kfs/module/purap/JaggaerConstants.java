package edu.cornell.kfs.module.purap;

public class JaggaerConstants {
    
    public static final String JAGGAER_ROLE_NAME_VIEW_ONLY = "View Only";
    
    public static final String JAGGAER_NAMESPACE = "JAGGAER";
    public static final String JAGGAER_PERMISSION_TEMPLATE_NAME = "Jaggaer Permission Template";
    public static final String JAGGAER_ATTRIBUTE_VALUE_KEY = "jaggaerRole";
    
    public static final String DEFAULT_XML_NS_XOP = "http://www.w3.org/2004/08/xop/include/";
    
    public static final String SUPPLIER_SYNCH_MESSAGE_XML_VERSION = "1.0";
    
    public static final String YES = "Yes";
    public static final String NO = "No";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String XML_VERSION = "1.0";
    
    public enum JaggaerBooleanToStringTyoe {
        SUPPLIER_ACTUVE(YES, NO),
        ADDRESS_ACTIVE(TRUE, FALSE),
        LOCATION_ACTIVE(YES, NO),
        LOCATION_PRIMARY(YES, NO);
        
        public final String true_string;
        public final String false_string;
        
        private JaggaerBooleanToStringTyoe(String true_string, String false_string) {
            this.true_string = true_string;
            this.false_string = false_string;
        }
    }
    
}
