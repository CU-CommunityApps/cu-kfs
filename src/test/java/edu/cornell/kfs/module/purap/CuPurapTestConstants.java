package edu.cornell.kfs.module.purap;

public final class CuPurapTestConstants {

    public static final String COST_SOURCE_ESTIMATE = "EST";
    public static final String COST_SOURCE_PRICING_AGREEMENT = "CON";
    public static final String COST_SOURCE_EDU_AND_INST_COOP = "EI";
    public static final String COST_SOURCE_INVOICE = "INV";
    public static final String COST_SOURCE_PREFERRED = "PREF";
    public static final String COST_SOURCE_CONTRACT = "CNTR";

    public static final Integer TEST_CONTRACT_ID_1357 = Integer.valueOf(1357);
    public static final Integer TEST_CONTRACT_ID_6666 = Integer.valueOf(6666);
    public static final String TEST_CONTRACT_CHART = "JX";
    public static final String TEST_CONTRACT_ORG = "5555";
    public static final String TEST_PARM_CHART = "RR";
    public static final String TEST_PARM_ORG = "8642";

    public static final String PAYLOAD_ID_ATTRIBUTE = "payloadID";
    public static final String TIMESTAMP_ATTRIBUTE = "timestamp";
    public static final String VERSION_ATTRIBUTE = "version";
    public static final String XML_LANG_ATTRIBUTE = "xml:lang";
    public static final String XMLNS_ATTRIBUTE = "xmlns";
    public static final String XMLNS_XSI_ATTRIBUTE = "xmlns:xsi";
    public static final String EINVOICE_NAMESPACE_URL = "http://www.kuali.org/kfs/purap/electronicInvoice";
    public static final String XSI_NAMESPACE_URL = "http://www.w3.org/2001/XMLSchema-instance";

    public static final String TEST_INTERNAL_SUPPLIER_ID = "2468";

    public static final class TestB2BInformation {
        public static final String PUNCHOUT_URL = "https://mocktest.sciquest.com/apps/Router/ExternalAuth/cXML/Mock";
        public static final String PUNCHBACK_URL = "http://localhost:8080/kfs/b2b.do?methodToCall=returnFromShopping";
        public static final String ENVIRONMENT = "test";
        public static final String USER_AGENT = "MockTest";
        public static final String SHOPPING_IDENTITY = "shopper1";
        public static final String SHOPPING_PASSWORD = "z9y8x7w6";
    }

}
