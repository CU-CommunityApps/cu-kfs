package edu.cornell.kfs.module.purap;

import org.springframework.http.HttpStatus;

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

    public static final int REQUISITION_ITEM_DESCRIPTION_MAX_LENGTH = 254;
    
    public static final String JAGGAER_UPLOAD_SUPPLIERS_TEST_VERSION_TAG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    public static final String JAGGAER_UPLOAD_SUPPLIERS_TEST_DTD_TAG = "<!DOCTYPE SupplierSyncMessage SYSTEM \"https://usertest-messages.sciquest.com/app_docs/dtd/supplier/TSMSupplier.dtd\">";
    
    
    private static final String STATUS_CODE_CHECK_FORMAT = "<StatusCode>%s</StatusCode>";
    private static final String FILE_PROCESSED_MESSAGE_FORMAT = "fileProcessedByJaggaer=%s";
    private static final String NUMBER_OF_ATTEMPTS_MESSAGE_FORMAT = "processUnsuccessfulResponse, attempt number %s, had an unsuccessful webservice call";
    private static final String UPLOAD_TURNED_OFF_MESSAGE = "uploadSupplierXMLFiles. uploading to Jaggaer is turned off, just remove the DONE file for test/jaggaer/xml/jaggaerTestFile.xml";
    
    public enum JaggaerMockServerConfiguration {
        DO_NOT_RUN(false, 606, "Condition to not run", new String[]{UPLOAD_TURNED_OFF_MESSAGE}),
        OK(true, HttpStatus.OK.value(), "Success (Counts:  Total documents attempted=1, Total documents completed=1.  Documents successful without warnings=1)",
                new String[]{buildStatusCodeCheck(200), buildFileProcessedCheck(true)}),
        ACCEPTED(true, HttpStatus.ACCEPTED.value(), "Success (Counts:  Total documents attempted=1, Total documents completed=1.  Documents successful without warnings=1)",
                new String[]{buildStatusCodeCheck(202), buildFileProcessedCheck(true)}),
        SERVER_ERROR(true, HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error while parsing the input / All documents failed. (Counts:  Total documents attempted=1, Total documents completed=0.  Documents attempted but unable to parse=1 - if there were any documents beyond the point of this parsing error, they were not able to be read and are not included in these counts.)",
                new String[]{buildStatusCodeCheck(500), buildFileProcessedCheck(false), buildAttemptCheck(1), buildAttemptCheck(2)}),
        BAD_REQUEST(true, HttpStatus.BAD_REQUEST.value(), "Error processing XML", new String[]{buildStatusCodeCheck(400), buildFileProcessedCheck(false), 
                buildAttemptCheck(1), buildAttemptCheck(2)});
        
        public final boolean shouldUploadFiles;
        public final int statusCode;
        public final String responseMessage;
        public final String[] logSearchStrings;
        
        private JaggaerMockServerConfiguration(boolean shouldUploadFiles, int statusCode, String responseMessage, String[] logSearchStrings) {
            this.shouldUploadFiles = shouldUploadFiles;
            this.statusCode = statusCode;
            this.responseMessage = responseMessage;
            this.logSearchStrings = logSearchStrings;
        }
        
        private static String buildStatusCodeCheck(int statusCode) {
            return String.format(STATUS_CODE_CHECK_FORMAT, String.valueOf(statusCode));
        }
        
        private static String buildFileProcessedCheck(boolean fileProcessed) {
            return String.format(FILE_PROCESSED_MESSAGE_FORMAT, String.valueOf(fileProcessed));
        }
        
        private static String buildAttemptCheck(int attemptNumber) {
            return String.format(NUMBER_OF_ATTEMPTS_MESSAGE_FORMAT, String.valueOf(attemptNumber));
        }
    }
}
