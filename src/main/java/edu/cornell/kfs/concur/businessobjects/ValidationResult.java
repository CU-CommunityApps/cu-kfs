package edu.cornell.kfs.concur.businessobjects;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

public class ValidationResult {
    protected boolean valid;
    protected List<String> messages;
    
    public ValidationResult(){
        this.valid = true;
        this.messages = new ArrayList<String>();
    }

    public ValidationResult(boolean valid, List<String> messages) {
        this.valid = valid;
        this.messages = messages;
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

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public void addMessage(String message) {
        if (messages == null) {
            messages = new ArrayList<String>();
        }
        if (StringUtils.isNotBlank(message) && isNotDuplicateMessage(message)) {
            messages.add(message);
        }
    }
    
    private boolean isNotDuplicateMessage(String message) {
        for (String currentMessage : getMessages()) {
            if (StringUtils.equalsIgnoreCase(currentMessage, message)) {
                return false;
            }
        }
        return true;
    }

    public void addMessages(List<String> messagesToAdd) {
        if (CollectionUtils.isNotEmpty(messagesToAdd)) {
            for (String messageToAdd : messagesToAdd) {
                addMessage(messageToAdd);
            }
        }
    }

    public String getErrorMessagesAsOneFormattedString() {
        StringBuffer result = new StringBuffer();

        if (messages != null && !messages.isEmpty()) {
            for (String message : messages) {
                result.append(message);
                result.append(KFSConstants.NEWLINE);
            }
        }
        
        return result.toString();
    }   
    
    public void add(ValidationResult validationResult){
        this.valid &= validationResult.isValid();
        this.addMessages(validationResult.getMessages());
    }

}
