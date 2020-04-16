package edu.cornell.kfs.concur.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.rass.RassConstants;

public enum ConcurFailedEventQueueProcessingController {
    OFF("off", false, false, false),
    READONLY("read - only", true, true, false),
    READWRITE("read - write", true, false, true);
    
    public final String name;
    public final boolean queryFailedEventQueue;
    public final boolean logFailedEventQueue;
    public final boolean writeFailedEventQueue;
    
    private ConcurFailedEventQueueProcessingController (String name, boolean queryFailedEventQueue, boolean logFailedEventQueue, boolean writeFailedEventQueue) {
        this.name = name;
        this.queryFailedEventQueue = queryFailedEventQueue;
        this.logFailedEventQueue = logFailedEventQueue;
        this.writeFailedEventQueue = writeFailedEventQueue;
    }
    
    public static ConcurFailedEventQueueProcessingController getConcurFailedEventQueueProcessingControllerFromString(String processFailedEventQueue) {
        if (StringUtils.equalsIgnoreCase(processFailedEventQueue, KFSConstants.ParameterValues.YES)) {
            return READWRITE;
        } else if (StringUtils.equalsIgnoreCase(processFailedEventQueue, RassConstants.RASS_FAILED_EVENT_QUEUE_READONLY)) {
            return READONLY;
        } else {
            return OFF;
        }
    }

}
