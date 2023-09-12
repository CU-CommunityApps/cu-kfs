package edu.cornell.kfs.sys.util;

import java.util.List;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;

public class LogTestingUtils {
    private static final Logger LOG = LogManager.getLogger();

    public static boolean doesLogEntryExist(final List<LoggingEvent> loggingEventList, String searchMessage) {
        for (final LoggingEvent logEntry : loggingEventList) {
            if (logEntry.getMessage() instanceof ParameterizedMessage) {
                ParameterizedMessage message = (ParameterizedMessage) logEntry.getMessage();
                if (StringUtils.equals(message.getFormattedMessage(), searchMessage)) {
                    return true;
                }
            } else {
                LOG.warn("doesLogEntryExist, unexpected message class of {}, a new if condition should be added",
                        logEntry.getMessage().getClass());
            }
        }
        return false;
    }

}
