package edu.cornell.kfs.concur.businessobjects;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

public class ValidationResult {
    private boolean valid;
    private List<String> errorMessages;
    private List<String> accountDetailMessages;
    
    public ValidationResult(){
        this.valid = true;
        this.errorMessages = new ArrayList<String>();
        this.accountDetailMessages = new ArrayList<String>();
    }

    public ValidationResult(boolean valid, List<String> messages) {
        this.valid = valid;
        this.errorMessages = messages;
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
        StringBuffer result = new StringBuffer();

        if (errorMessages != null && !errorMessages.isEmpty()) {
            for (String message : errorMessages) {
                result.append(message);
                result.append(KFSConstants.NEWLINE);
            }
        }
        
        return result.toString();
    }
    
    public List<String> getAccountDetailMessages() {
        return accountDetailMessages;
    }
    
    public void addAccountDetailMessage(String message) {
        if (accountDetailMessages == null) {
            accountDetailMessages = new ArrayList<String>();
        }
        if (StringUtils.isNotBlank(message) && isNotDuplicateMessage(accountDetailMessages, message)) {
            accountDetailMessages.add(message);
        }
    }
    
    public void addAccountDetailMessages(List<String> accountDetailMessagesToAdd) {
        if (CollectionUtils.isNotEmpty(accountDetailMessagesToAdd)) {
            for (String messageToAdd : accountDetailMessagesToAdd) {
                addAccountDetailMessage(messageToAdd);
            }
        }
    }
    
    public String getAccountDetailMessagesAsOneFormattedString() {
        StringBuffer result = new StringBuffer();

        if (accountDetailMessages != null && !accountDetailMessages.isEmpty()) {
            for (String message : accountDetailMessages) {
                result.append(message);
                result.append(KFSConstants.NEWLINE);
            }
        }
        
        return result.toString();
    }

    public void add(ValidationResult validationResult){
        this.valid &= validationResult.isValid();
        this.addErrorMessages(validationResult.getErrorMessages());
        this.addAccountDetailMessages(validationResult.getAccountDetailMessages());
    }

}
