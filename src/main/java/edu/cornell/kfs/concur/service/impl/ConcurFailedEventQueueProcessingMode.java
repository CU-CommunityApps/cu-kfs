package edu.cornell.kfs.concur.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.concur.ConcurConstants;

public enum ConcurFailedEventQueueProcessingMode {
    OFF(false, false, false),
    READONLY(true, true, false),
    READWRITE(true, false, true);
    
    public final boolean queryQueue;
    public final boolean logQueue;
    public final boolean persistQueue;
    
    private ConcurFailedEventQueueProcessingMode (boolean queryQueue, boolean logQueue, boolean persistQueue) {
        this.queryQueue = queryQueue;
        this.logQueue = logQueue;
        this.persistQueue = persistQueue;
    }
    
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
    }
    
    public static ConcurFailedEventQueueProcessingMode getConcurFailedEventQueueProcessingModeFromString(String processFailedEventQueue) {
        if (StringUtils.equalsIgnoreCase(processFailedEventQueue, KFSConstants.ParameterValues.YES)) {
            return READWRITE;
        } else if (StringUtils.equalsIgnoreCase(processFailedEventQueue, ConcurConstants.CONCUR_FAILED_EVENT_QUEUE_READONLY)) {
            return READONLY;
        } else {
            return OFF;
        }
    }

}
