package edu.cornell.kfs.pdp;

import org.kuali.kfs.pdp.PdpConstants;

public class CUPdpConstants extends PdpConstants {
	
    public static class PdpDocumentTypes {
    	public static String DISBURSEMENT_VOUCHER = "DVCA";
    	public static String PAYMENT_REQUEST = "PREQ";
    	public static String CAMPUS_LIFE = "APCL";
    	public static String CORNELL_STORE = "APCS";
    	public static String LIBRARY = "APLB";
    	public static String HOTEL = "APHT";
    	public static String CREDIT_MEMO = "CM";
    }
    
    public static class DivisionCodes {
        public static final int US_MAIL = 1;
        public static final int CU_MAIL_SERVICES = 16;
    }

    public static class PaymentDistributions {
        public static final String PROCESS_ACH_ONLY = "achOnly";
        public static final String PROCESS_CHECK_ONLY = "checkOnly";
        public static final String PROCESS_ALL = "all";
    }
    
    public static class CustomerProfilePrimaryKeyTags {
        public static final String CHART_OPEN = "<chart>";
        public static final String CHART_CLOSE = "</chart>";
        public static final String UNIT_OPEN = "<unit>";
        public static final String UNIT_CLOSE = "</unit>";
        public static final String SUBUNIT_OPEN = "<sub_unit>";
        public static final String SUBUNIT_CLOSE = "</sub_unit>";
    }
}
