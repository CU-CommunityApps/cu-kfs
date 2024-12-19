package edu.cornell.kfs.concur.businessobjects;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

public class ValidationResult {
    private boolean valid;
    private List<String> errorMessages;
    private List<String> detailMessages;
    
    public ValidationResult(){
        this.valid = true;
        this.errorMessages = new ArrayList<String>();
        this.detailMessages = new ArrayList<String>();
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isNotValid() {
        return !valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void addErrorMessage(String message) {
        if (errorMessages == null) {
            errorMessages = new ArrayList<String>();
        }
        if (StringUtils.isNotBlank(message) && isNotDuplicateMessage(errorMessages, message)) {
            errorMessages.add(message);
        }
    }
    
    private boolean isNotDuplicateMessage(List<String> messages, String message) {
        for (String currentMessage : messages) {
            if (StringUtils.equalsIgnoreCase(currentMessage, message)) {
                return false;
            }
        }
        return true;
    }

    public void addErrorMessages(List<String> errorMessagesToAdd) {
        if (CollectionUtils.isNotEmpty(errorMessagesToAdd)) {
            for (String messageToAdd : errorMessagesToAdd) {
                addErrorMessage(messageToAdd);
            }
        }
    }

    public String getErrorMessagesAsOneFormattedString() {
        return buildOneFormattedString(errorMessages);
    }
    
    private String buildOneFormattedString(List<String> messageList) {
        StringBuffer result = new StringBuffer();
        if (messageList != null && !messageList.isEmpty()) {
            for (String message : messageList) {
                result.append(message);
                result.append(KFSConstants.NEWLINE);
            }
        }
        return result.toString();
    }
    
    public List<String> getDetailMessages() {
        return detailMessages;
    }
    
    public void addDetailMessage(String message) {
        if (detailMessages == null) {
            detailMessages = new ArrayList<String>();
        }
        if (StringUtils.isNotBlank(message) && isNotDuplicateMessage(detailMessages, message)) {
            detailMessages.add(message);
        }
    }
    
    public void addDetailMessages(List<String> accountDetailMessagesToAdd) {
        if (CollectionUtils.isNotEmpty(accountDetailMessagesToAdd)) {
            for (String messageToAdd : accountDetailMessagesToAdd) {
                addDetailMessage(messageToAdd);
            }
        }
    }
    
    public String getDetailMessagesAsOneFormattedString() {
        return buildOneFormattedString(detailMessages);
    }

    public void add(ValidationResult validationResult){
        this.valid &= validationResult.isValid();
        this.addErrorMessages(validationResult.getErrorMessages());
        this.addDetailMessages(validationResult.getDetailMessages());
    }

}
