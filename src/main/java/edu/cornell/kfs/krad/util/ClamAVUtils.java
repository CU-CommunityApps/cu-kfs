package edu.cornell.kfs.krad.util;

import edu.cornell.kfs.krad.CUKRADConstants.ClamAVDelimiters;

public final class ClamAVUtils {

    public static int indexOfNullCharDelimiter(byte[] content, int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
            if (content[i] == ClamAVDelimiters.NULL_SUFFIX_BYTE) {
                return i;
            }
        }
        return -1;
    }

}
