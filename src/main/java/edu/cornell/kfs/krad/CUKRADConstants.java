package edu.cornell.kfs.krad;

public final class CUKRADConstants {

    public static final class ClamAVDelimiters {
        public static final String Z_PREFIX = "z";
        public static final String NULL_SUFFIX = "\0";
        public static final byte Z_PREFIX_BYTE = 0x7A;
        public static final byte NULL_SUFFIX_BYTE = 0;
    }

    public static final class ClamAVResponses {
        public static final String STREAM_PREFIX = "stream:";
        public static final String FOUND_SUFFIX = "FOUND";
        public static final String ERROR_SUFFIX = "ERROR";
        public static final String RESPONSE_OK = STREAM_PREFIX + " OK";
        public static final String RESPONSE_PING_SUCCESS = "PONG";
        public static final String RESPONSE_SIZE_EXCEEDED = "INSTREAM size limit exceeded. " + ERROR_SUFFIX;
        public static final String RESPONSE_ERROR_WRITING_FILE = "Error writing to temporary file. " + ERROR_SUFFIX;
    }

    public static final class ClamAVCommands {
        public static final String INSTREAM = "INSTREAM";
        public static final String PING = "PING";
        public static final String STATS = "STATS";
    }
}
