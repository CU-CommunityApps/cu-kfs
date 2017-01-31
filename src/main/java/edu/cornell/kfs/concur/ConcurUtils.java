package edu.cornell.kfs.concur;

import org.apache.commons.lang.StringUtils;

import edu.cornell.kfs.sys.CUKFSConstants;

public class ConcurUtils {

    public static boolean isExpenseReportURI(String URI) {
        if (StringUtils.isNotBlank(URI) && URI.contains(ConcurConstants.EXPENSE_REPORT_URI_INDICATOR)) {
            return true;
        }
        return false;
    }

    public static boolean isTravelRequestURI(String URI) {
        if (StringUtils.isNotBlank(URI) && URI.contains(ConcurConstants.TRAVEL_REQUEST_URI_INDICATOR)) {
            return true;
        }
        return false;
    }

    public static String extractKFSInfoFromConcurString(String concurString) {
        String kfsInfo = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(concurString) && concurString.contains(CUKFSConstants.LEFT_PARENTHESIS) && concurString.contains(CUKFSConstants.RIGHT_PARENTHESIS) && concurString.lastIndexOf(CUKFSConstants.LEFT_PARENTHESIS) < concurString.length()) {
            kfsInfo = concurString.substring(concurString.lastIndexOf(CUKFSConstants.LEFT_PARENTHESIS) + 1, concurString.lastIndexOf(CUKFSConstants.RIGHT_PARENTHESIS));
        }
        return kfsInfo;
    }

}
