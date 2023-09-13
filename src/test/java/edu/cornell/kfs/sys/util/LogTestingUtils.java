package edu.cornell.kfs.sys.util;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.Message;

public class LogTestingUtils {
    private static final Logger LOG = LogManager.getLogger();

    public static boolean doesLogEntryExist(final List<LoggingEvent> loggingEventList, String searchMessage) {
        for (final LoggingEvent logEntry : loggingEventList) {
            if (logEntry.getMessage() instanceof Message) {
                Message message = (Message) logEntry.getMessage();
                if (StringUtils.contains(message.getFormattedMessage(), searchMessage)) {
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
