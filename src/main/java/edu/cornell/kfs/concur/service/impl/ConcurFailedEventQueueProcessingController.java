package edu.cornell.kfs.concur.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.rass.RassConstants;

public enum ConcurFailedEventQueueProcessingController {
    OFF(false, false, false),
    READONLY(true, true, false),
    READWRITE(true, false, true);
    
    public final boolean queryFailedEventQueue;
    public final boolean logFailedEventQueue;
    public final boolean writeFailedEventQueue;
    
    private ConcurFailedEventQueueProcessingController (boolean queryFailedEventQueue, boolean logFailedEventQueue, boolean writeFailedEventQueue) {
        this.queryFailedEventQueue = queryFailedEventQueue;
        this.logFailedEventQueue = logFailedEventQueue;
        this.writeFailedEventQueue = writeFailedEventQueue;
    }
    
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
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
