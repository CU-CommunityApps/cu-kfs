package edu.cornell.kfs.concur.businessobjects;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNoticationVersion2EventType;
import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNotificationVersion2ProcessingResults;

public class ConcurEventNotificationProcessingResultsDTO {
    private ConcurEventNoticationVersion2EventType eventType;
    private ConcurEventNotificationVersion2ProcessingResults processingResults;
    private String reportNumber;
    private List<String> messages;
    
    public ConcurEventNotificationProcessingResultsDTO(ConcurEventNoticationVersion2EventType eventType, 
            ConcurEventNotificationVersion2ProcessingResults processingResults, String reportNumber) {
        this(eventType, processingResults, reportNumber, new ArrayList<String>());
    }
    
    public ConcurEventNotificationProcessingResultsDTO(ConcurEventNoticationVersion2EventType eventType, 
            ConcurEventNotificationVersion2ProcessingResults processingResults, String reportNumber, List<String> messages) {
        this.eventType = eventType;
        this.processingResults = processingResults;
        this.reportNumber = reportNumber;
        this.messages = messages;
    }

    public ConcurEventNoticationVersion2EventType getEventType() {
        return eventType;
    }

    public void setEventType(ConcurEventNoticationVersion2EventType eventType) {
        this.eventType = eventType;
    }

    public ConcurEventNotificationVersion2ProcessingResults getProcessingResults() {
        return processingResults;
    }

    public void setProcessingResults(ConcurEventNotificationVersion2ProcessingResults processingResults) {
        this.processingResults = processingResults;
    }

    public String getReportNumber() {
        return reportNumber;
    }

    public void setReportNumber(String reportNumber) {
        this.reportNumber = reportNumber;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
