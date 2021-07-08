package edu.cornell.kfs.krad;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class CUKRADConstants {

    public static final class ClamAVResponses {
        public static final String STREAM_PREFIX = "stream: ";
        public static final String FOUND_SUFFIX = "FOUND";
        public static final String ERROR_SUFFIX = "ERROR";
        public static final String RESPONSE_OK = "stream: OK";
        public static final String PING_RESPONSE_OK = "PONG\0";
        public static final String RESPONSE_SIZE_EXCEEDED = "INSTREAM size limit exceeded. ERROR";
        public static final String RESPONSE_ERROR_WRITING_FILE = "Error writing to temporary file. ERROR";
    }

    public static final class ClamAVCommands {
        public static final String INSTREAM = "zINSTREAM\0";
        public static final String PING = "zPING\0";
        public static final String STATS = "nSTATS\n";
    }

    public enum ClamAVCommand {
        INSTREAM("zINSTREAM\0"),
        PING("zPING\0"),
        STATS("nSTATS\n");
        
        private final String commandString;
        private final byte[] commandAsBytes;
        
        private ClamAVCommand(String commandString) {
            this.commandString = commandString;
            this.commandAsBytes = commandString.getBytes(StandardCharsets.UTF_8);
        }
        
        public String getCommandString() {
            return commandString;
        }
        
        public byte[] asBytes() {
            return Arrays.copyOf(commandAsBytes, commandAsBytes.length);
        }
    }

}
