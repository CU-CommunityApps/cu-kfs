package edu.cornell.kfs.concur.businessobjects;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.concur.ConcurConstants;

public class ValidationResult {
    protected boolean valid;
    protected List<String> messages;

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
        messages.add(message);
    }

    public void addMessages(List<String> messagesToAdd) {
        if (messages == null) {
            messages = new ArrayList<String>();
        }
        if (messagesToAdd != null) {
            messages.addAll(messagesToAdd);
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

        return addMessageHeaderAndTruncate(result.toString(), ConcurConstants.VALIDATION_RESULT_MESSAGE_MAX_LENGTH);
    }

    private String addMessageHeaderAndTruncate(String message, int maxLength) {
        String errorMessagesString = addMessageHeader(message);
        errorMessagesString = truncateMessageLength(errorMessagesString, maxLength);
        return errorMessagesString;
    }

    private String addMessageHeader(String message) {
        if (message.length() > 0) {
            message = ConcurConstants.ERROR_MESSAGE_HEADER + message;
        }
        return message;
    }

    private String truncateMessageLength(String message, int maxLength) {
        if (message.length() > maxLength) {
            message = message.substring(0, maxLength + 1);
        }
        return message;
    }

}
