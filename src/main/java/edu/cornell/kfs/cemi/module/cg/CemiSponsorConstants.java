package edu.cornell.kfs.cemi.module.cg;

public final class CemiSponsorConstants {
    
    public static final String SPONSOR_ID_FORMAT = "SPN-{0}";
    public static final String SPONSOR_REFERENCE_ID_FORMAT = "SPN-{0}";
    public static final String SPONSOR_ADDRESS_ID_FORMAT = "ITH_{}_{}";
    public static final String SPONSOR_DATA_PHONE_DATA_ID = "{SponsorId}_Phone";
    public static final String SPONSOR_DATA_EMAIL_DATA_ID = "{SponsorId}_Email";
    
    //    Examples of how those constants would be used:
    //        MessageFormat.format(CemiAwardScheduleConstants.SPREADSHEET_KEY_FORMAT, awardProposalNumber);
    //        MessageFormat.format(CemiAwardScheduleConstants.AWARD_SCHEDULE_REFERENCE_ID_FORMAT, awardProposalNumber);

    public static final String NET_30_DAYS = "Net_30_Days";
    public static final String NUMERIC_ONE = "1";
    public static final String USE_FOR_SHIPPING = "SHIPPING";
    public static final String USE_FOR_BILLING = "BILLING";
    public static final String USE_FOR_REMIT = "REMIT";
    public static final String USE_FOR_TENANTED_SHIPPING = "Shipping";
    public static final String USE_FOR_TENANTED_BILLING = "Billing";
    public static final String USE_FOR_TENANTED_REMIT = "Remit_to";
    
    
    public static final String SPONSOR_OUTPUT_DEFINITION_PATH_SUFFIX = "module/cg/batch/CemiSponsorExtractFileOutputDefinition.xml";
    
    public static final String SPONSOR_TEMPLATE_WORKBOOK_FILE_PATH_SUFFIX = "module/cg/batch/Put_Sponsor.xlsx";
    
    public static final String SPONSOR_EXTRACT_FILENAME_PREFIX = "Put_Sponsor_ITH_";
    public static final String SPONSOR_EXTRACT_PLAIN_FILENAME = "Put_Sponsor.xlsx";
    
    public static final class SponsorExtractSheets {
        public static final String SPONSOR = "Sponsor";
    }
  
}
