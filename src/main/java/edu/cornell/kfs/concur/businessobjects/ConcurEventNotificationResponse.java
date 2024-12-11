package edu.cornell.kfs.concur.businessobjects;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNotificationType;
import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNotificationStatus;

public class ConcurEventNotificationResponse {
    private ConcurEventNotificationType eventType;
    private ConcurEventNotificationStatus eventNotificationStatus;
    private String reportNumber;
    private String reportName;
    private String reportStatus;
    private String travelerName;
    private String travelerEmail;
    private List<String> errorMessages;
    private List<String> detailMessages;
    
    public ConcurEventNotificationResponse(ConcurEventNotificationType eventType,
                                           ConcurEventNotificationStatus eventNotificationStatus, String reportNumber, String reportName, String reportStatus,
                                           String travelerName, String travelerEmail) {
        this(eventType, eventNotificationStatus, reportNumber, reportName, reportStatus, travelerName, travelerEmail, new ArrayList<String>(), new ArrayList<String>());
    }
    
    public ConcurEventNotificationResponse(ConcurEventNotificationResponse responseToCopy) {
        this(responseToCopy.getEventType(), responseToCopy.getEventNotificationStatus(), responseToCopy.getReportNumber(),
                responseToCopy.getReportName(), responseToCopy.getReportStatus(),
                responseToCopy.getTravelerName(), responseToCopy.getTravelerEmail(), new ArrayList<>(responseToCopy.getErrorMessages()),
                new ArrayList<>(responseToCopy.getDetailMessages()));
    }
    
    public ConcurEventNotificationResponse(ConcurEventNotificationType eventType,
                                           ConcurEventNotificationStatus eventNotificationStatus, String reportNumber, String reportName, String reportStatus,
                                           String travelerName, String travelerEmail, List<String> errorMessages, List<String> detailMessages) {
        this.eventType = eventType;
        this.eventNotificationStatus = eventNotificationStatus;
        this.reportNumber = reportNumber;
        this.reportName = reportName;
        this.reportStatus = reportStatus;
        this.travelerName = travelerName;
        this.travelerEmail = travelerEmail;
        this.errorMessages = errorMessages;
        this.detailMessages = detailMessages;
    }

    public ConcurEventNotificationType getEventType() {
        return eventType;
    }

    public void setEventType(ConcurEventNotificationType eventType) {
        this.eventType = eventType;
    }

    public ConcurEventNotificationStatus getEventNotificationStatus() {
        return eventNotificationStatus;
    }

    public void setEventNotificationStatus(ConcurEventNotificationStatus eventNotificationStatus) {
        this.eventNotificationStatus = eventNotificationStatus;
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

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }
    
    public String getFlattenedErrorMessages() {
        return buildFlattenedMessageList(errorMessages);
    }
    
    public List<String> getDetailMessages() {
        return detailMessages;
    }

    public void setDetailMessages(List<String> detailMessages) {
        this.detailMessages = detailMessages;
    }
    
    public String getFlattenedDetailMessages() {
        return buildFlattenedMessageList(detailMessages);
    }
    
    private String buildFlattenedMessageList(List<String> messageList) {
        return messageList.stream().collect(Collectors.joining(KFSConstants.NEWLINE));
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
