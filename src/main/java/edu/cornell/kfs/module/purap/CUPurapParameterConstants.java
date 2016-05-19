package  edu.cornell.kfs.module.purap;

import org.kuali.kfs.module.purap.PurapParameterConstants;

public class CUPurapParameterConstants extends PurapParameterConstants {
    public static final String PURAP_PREQ_PAY_DATE_VARIANCE = "NUMBER_OF_DAYS_TO_DECREASE_PAY_DATE_BY";
	
	public static final String B2B_TOTAL_AMOUNT_FOR_AUTO_PO = "B2B_TOTAL_AMOUNT_FOR_AUTO_PO";
	
	public static final String APO_CONTRACT_MANAGER_EMAIL = "APO_CONTRACT_MANAGER_EMAIL";
	
	public static final String AUTO_CLOSE_PO_RESULTS_LIMIT = "AUTO_CLOSE_PO_RESULTS_LIMIT";
	
	public static final String MANUAL_DISTRIBUTION_EMAIL = "MANUAL_DISTRIBUTION_EMAIL";
	
    // KFSPTS-1625
	public static final String B2B_TOTAL_AMOUNT_FOR_SUPER_USER_AUTO_PO = "B2B_TOTAL_AMOUNT_FOR_SUPER_USER_AUTO_PO";

    //KFSUPGRADE-377
    public static final String PURAP_CR_PREQ_CANCEL_NOTE = "CR_CANCEL_NOTE";
    public static final String PURAP_CR_CM_CANCEL_NOTE = "CR_CANCEL_NOTE";

 // KFSPTS-1705
    public static final String PO_NOTIFY_EXCLUSIONS = "NOTIFY_REQUISITION_SOURCES";
    
    public static class ElectronicInvoiceParameters {
        public static final String SUPPRESS_REJECT_REASON_CODES_ON_EIRT_APPROVAL = "SUPPRESS_REJECT_REASON_CODES_ON_EIRT_APPROVAL";
        public static final String DEFAULT_PROCESSING_CAMPUS = "DEFAULT_PROCESSING_CAMPUS";
    }
    
    public static final String ROUTE_REQS_WITH_EXPIRED_CONTRACT_TO_CM = "ROUTE_REQS_WITH_EXPIRED_CONTRACT_TO_CM";

}
