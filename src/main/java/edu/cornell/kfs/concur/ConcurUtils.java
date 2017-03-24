package edu.cornell.kfs.concur;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.sys.CUKFSConstants;

public class ConcurUtils {

    public static boolean isExpenseReportURI(String URI) {
        return StringUtils.isNotBlank(URI) && URI.contains(ConcurConstants.EXPENSE_REPORT_URI_INDICATOR);
    }

    public static boolean isTravelRequestURI(String URI) {
        return StringUtils.isNotBlank(URI) && URI.contains(ConcurConstants.TRAVEL_REQUEST_URI_INDICATOR);
    }

    public static String extractKFSInfoFromConcurString(String concurString) {
        String kfsInfo = concurString;
        if (doesStringContainOpenCloseParenthesis(concurString)) {
            kfsInfo = concurString.substring(concurString.lastIndexOf(CUKFSConstants.LEFT_PARENTHESIS) + 1, concurString.lastIndexOf(CUKFSConstants.RIGHT_PARENTHESIS));
        }
        return kfsInfo;
    }
    
    public static boolean doesStringContainOpenCloseParenthesis(String input) {
        return StringUtils.isNotBlank(input)
                && input.contains(CUKFSConstants.LEFT_PARENTHESIS)
                && input.contains(CUKFSConstants.RIGHT_PARENTHESIS)
                && input.lastIndexOf(CUKFSConstants.LEFT_PARENTHESIS) < input.length();
    }   
    
    public static String formatStringForErrorMessage(String label, String... values) {
        String formattedString = StringUtils.EMPTY;
        if (values != null && values.length > 0) {
            formattedString += label + CUKFSConstants.COLON;
            for (String value : values) {
                formattedString += value + KFSConstants.COMMA;
            }
            formattedString = StringUtils.removeEnd(formattedString,
                    KFSConstants.COMMA);
        }

        return formattedString;
    }
    
    public static boolean isConcurReportStatusAwaitingExternalValidation(String statusCode){
        return ConcurConstants.EXPENSE_AWAITING_EXTERNAL_VALIDATION_STATUS_CODE.equalsIgnoreCase(statusCode) || ConcurConstants.REQUEST_AWAITING_EXTERNAL_VALIDATION_STATUS_CODE.equalsIgnoreCase(statusCode);
    }

}
