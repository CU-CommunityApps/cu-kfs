package edu.cornell.kfs.concur.businessobjects;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNoticationVersion2EventType;
import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNotificationVersion2ProcessingResults;

public class ConcurEventNotificationProcessingResultsDTO {
    private ConcurEventNoticationVersion2EventType eventType;
    private ConcurEventNotificationVersion2ProcessingResults processingResults;
    private String reportNumber;
    private String reportName;
    private String reportStatus;
    private String travelerName;
    private String travelerEmail;
    private List<String> messages;
    
    public ConcurEventNotificationProcessingResultsDTO(ConcurEventNoticationVersion2EventType eventType, 
            ConcurEventNotificationVersion2ProcessingResults processingResults, String reportNumber, String reportName, String reportStatus,
            String travelerName, String travelerEmail) {
        this(eventType, processingResults, reportNumber, reportName, reportStatus, travelerName, travelerEmail, new ArrayList<String>());
    }
    
    public ConcurEventNotificationProcessingResultsDTO(ConcurEventNotificationProcessingResultsDTO dtoToCopy) {
        this(dtoToCopy.getEventType(), dtoToCopy.getProcessingResults(), dtoToCopy.getReportNumber(),
                dtoToCopy.getTravelerName(), dtoToCopy.getTravelerEmail(), new ArrayList<>(dtoToCopy.getMessages()));
    }
    
    public ConcurEventNotificationProcessingResultsDTO(ConcurEventNoticationVersion2EventType eventType, 
            ConcurEventNotificationVersion2ProcessingResults processingResults, String reportNumber, String reportName, String reportStatus,
            String travelerName, String travelerEmail, List<String> messages) {
        this.eventType = eventType;
        this.processingResults = processingResults;
        this.reportNumber = reportNumber;
        this.reportName = reportName;
        this.reportStatus = reportStatus;
        this.travelerName = travelerName;
        this.travelerEmail = travelerEmail;
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

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getReportStatus() {
        return reportStatus;
    }

    public void setReportStatus(String reportStatus) {
        this.reportStatus = reportStatus;
    }

    public String getTravelerName() {
        return travelerName;
    }

    public void setTravelerName(String travelerName) {
        this.travelerName = travelerName;
    }

    public String getTravelerEmail() {
        return travelerEmail;
    }

    public void setTravelerEmail(String travelerEmail) {
        this.travelerEmail = travelerEmail;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
    
    public String getFlattenedMessages() {
        return messages.stream().collect(Collectors.joining(KFSConstants.NEWLINE));
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
