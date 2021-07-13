package edu.cornell.kfs.krad.util;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.krad.CUKRADConstants.ClamAVDelimiters;
import edu.cornell.kfs.krad.CUKRADConstants.ClamAVResponses;

public final class ClamAVUtils {

    public static int indexOfNullCharDelimiter(byte[] content, int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
            if (content[i] == ClamAVDelimiters.NULL_SUFFIX_BYTE) {
                return i;
            }
        }
        return -1;
    }

    public static String getSignatureFromScanResultMessage(String result) {
        if (StringUtils.isBlank(result)) {
            return KFSConstants.EMPTY_STRING;
        }
        String signature = StringUtils.substringAfter(result, ClamAVResponses.STREAM_PREFIX);
        signature = StringUtils.substringBeforeLast(signature, ClamAVResponses.FOUND_SUFFIX);
        return StringUtils.trimToEmpty(signature);
    }

}
