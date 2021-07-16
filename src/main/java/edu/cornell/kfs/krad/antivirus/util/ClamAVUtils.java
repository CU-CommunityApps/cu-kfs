package edu.cornell.kfs.krad.antivirus.util;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.krad.CUKRADConstants.ClamAVResponses;
import edu.cornell.kfs.krad.antivirus.ScanResult.Status;

public final class ClamAVUtils {

    public static Status determineScanStatusFromScanResultMessage(String result) {
        switch (StringUtils.defaultString(result)) {
            case ClamAVResponses.RESPONSE_OK :
                return Status.PASSED;
            
            case ClamAVResponses.RESPONSE_SIZE_EXCEEDED :
                return Status.ERROR;
            
            case ClamAVResponses.RESPONSE_ERROR_WRITING_FILE :
                return Status.ERROR;
            
            default :
                return StringUtils.endsWith(result, ClamAVResponses.FOUND_SUFFIX) ? Status.FAILED : Status.ERROR;
        }
    }

    public static String extractSignatureFromScanResultMessage(String result) {
        if (!StringUtils.startsWith(result, ClamAVResponses.STREAM_PREFIX)
                || !StringUtils.endsWith(result, ClamAVResponses.FOUND_SUFFIX)) {
            return KFSConstants.EMPTY_STRING;
        }
        String signature = StringUtils.substringAfter(result, ClamAVResponses.STREAM_PREFIX);
        signature = StringUtils.substringBeforeLast(signature, ClamAVResponses.FOUND_SUFFIX);
        return StringUtils.trimToEmpty(signature);
    }

}
