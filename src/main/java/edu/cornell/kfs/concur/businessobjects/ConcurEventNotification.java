package edu.cornell.kfs.concur.businessobjects;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.concur.ConcurPropertyConstants;
import edu.cornell.kfs.sys.CUKFSConstants;

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

    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat(KFSConstants.MONTH_DAY_YEAR_DATE_FORMAT);
        StringBuilder sb = new StringBuilder();
        sb.append(ConcurPropertyConstants.ConcurEventNotification.CONCUR_EVENT_NOTIFICATION_ID).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(concurEventNotificationId).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(ConcurPropertyConstants.ConcurEventNotification.CONTEXT).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(context).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(ConcurPropertyConstants.ConcurEventNotification.EVENT_DATE_TIME).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(sdf.format(eventDateTime)).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(ConcurPropertyConstants.ConcurEventNotification.EVENT_TYPE).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(eventType).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(ConcurPropertyConstants.ConcurEventNotification.OBJECT_TYPE).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(objectType).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(ConcurPropertyConstants.ConcurEventNotification.OBJECT_URI).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(objectURI).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(ConcurPropertyConstants.ConcurEventNotification.NOTIFICATION_URI).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(notificationURI).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(ConcurPropertyConstants.ConcurEventNotification.IN_PROCESS).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(inProcess).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(ConcurPropertyConstants.ConcurEventNotification.PROCESSED).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(processed).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(ConcurPropertyConstants.ConcurEventNotification.VALIDATION_RESULT).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(validationResult).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(ConcurPropertyConstants.ConcurEventNotification.VALIDATION_RESULT_MESSAGE).append(CUKFSConstants.COLON).append(KFSConstants.BLANK_SPACE).append(validationResultMessage).append(KFSConstants.COMMA);
        return sb.toString();
    }
    
}
