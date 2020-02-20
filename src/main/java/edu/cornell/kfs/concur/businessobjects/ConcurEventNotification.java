package edu.cornell.kfs.concur.businessobjects;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.sys.KFSConstants;

public class ConcurEventNotification extends PersistableBusinessObjectBase{   
    protected int concurEventNotificationId;
    protected String context;
    protected Date eventDateTime;
    protected String eventType;
    protected String objectType;
    protected String objectURI;
    protected String notificationURI;
    protected boolean inProcess;
    protected boolean processed;
    protected boolean validationResult;
    protected String validationResultMessage;
    
    public int getConcurEventNotificationId() {
        return concurEventNotificationId;
    }

    public void setConcurEventNotificationId(int concurEventNotificationId) {
        this.concurEventNotificationId = concurEventNotificationId;
    }
    
    public String getContext() {
        return context;
    }
    
    public void setContext(String context) {
        this.context = context;
    }
    
    public Date getEventDateTime() {
        return eventDateTime;
    }
    
    public void setEventDateTime(Date eventDateTime) {
        this.eventDateTime = eventDateTime;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public String getObjectType() {
        return objectType;
    }
    
    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }
    
    public String getObjectURI() {
        return objectURI;
    }
    
    public void setObjectURI(String objectURI) {
        this.objectURI = objectURI;
    }

    public boolean isInProcess() {
        return inProcess;
    }

    public void setInProcess(boolean inProcess) {
        this.inProcess = inProcess;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public boolean isValidationResult() {
        return validationResult;
    }

    public void setValidationResult(boolean validationResult) {
        this.validationResult = validationResult;
    }

    public String getValidationResultMessage() {
        return validationResultMessage;
    }

    public void setValidationResultMessage(String validationResultMessage) {
        this.validationResultMessage = validationResultMessage;
    }

    public String getNotificationURI() {
        return notificationURI;
    }

    public void setNotificationURI(String notificationURI) {
        this.notificationURI = notificationURI;
    }

    public String toPurgeRecordingString() {
        SimpleDateFormat sdf = new SimpleDateFormat(KFSConstants.MONTH_DAY_YEAR_DATE_FORMAT, Locale.US);
        StringBuilder sb = new StringBuilder();
        sb.append("concurEventNotificationId: ").append(concurEventNotificationId).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append("context: ").append(context).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append("eventDateTime: ").append(sdf.format(eventDateTime)).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append("eventType: ").append(eventType).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append("objectType: ").append(objectType).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append("objectURI: ").append(objectURI).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append("notificationURI: ").append(notificationURI).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append("inProcess: ").append(inProcess).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append("processed: ").append(processed).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append("validationResult: ").append(validationResult).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append("validationResultMessage: ").append(validationResultMessage).append(KFSConstants.COMMA);
        return sb.toString();
    }
    
}
