package edu.cornell.kfs.krad.antivirus.util;

import org.apache.commons.lang3.StringUtils;

import edu.cornell.kfs.krad.antivirus.service.ScanResult.Status;

public final class DummyAVUtils {

    public static Status determineScanStatusFromScanResult(String result) {
        switch (StringUtils.defaultString(result)) {
            case "PASSED" :
                return Status.PASSED;
            case "FAILED" :
                return Status.FAILED;
            case "ERROR" :
                return Status.ERROR;
            default : 
                return null;
        }
    }

}
