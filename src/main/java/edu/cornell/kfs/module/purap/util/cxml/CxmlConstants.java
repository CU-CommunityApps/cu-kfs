package edu.cornell.kfs.module.purap.util.cxml;

public final class CxmlConstants {

    public static final String DOCTYPE_URL = "https://xml.cXML.org/schemas/cXML/1.2.019/cXML.dtd";
    public static final String PAYLOAD_ID_IRRELEVANT = "irrelevant";
    public static final String XML_LANG_EN = "en";
    public static final String XML_LANG_EN_US = "en-US";
    public static final String CONTACT_ROLE_END_USER = "endUser";
    public static final String PUNCHOUT_OPERATION_CREATE = "create";

    public static final class CxmlDefaults {
        public static final String CXML_VERSION_1_2_019 = "1.2.019";
        public static final String SIGNATURE_PK7_SELF_CONTAINED = "PK7 self-contained";
        public static final String ENCODING_BASE64 = "Base64";
    }

    public static final class CredentialDomains {
        public static final String NETWORK_ID = "NetworkId";
        public static final String DUNS = "DUNS";
        public static final String INTERNAL_SUPPLIER_ID = "internalsupplierid";
        public static final String TOPS_NETWORK_USER_ID = "TOPSNetworkUserId";
    }

    public static final class ExtrinsicFields {
        public static final String USER_EMAIL = "UserEmail";
        public static final String UNIQUE_NAME = "UniqueName";
        public static final String PHONE_NUMBER = "PhoneNumber";
        public static final String DEPARTMENT = "Department";
        public static final String CAMPUS = "Campus";
        public static final String FIRST_NAME = "FirstName";
        public static final String LAST_NAME = "LastName";
        public static final String ROLE = "Role";
    }

}
