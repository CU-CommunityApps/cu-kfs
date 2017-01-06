package edu.cornell.kfs.concur.businessobjects;

import java.util.ArrayList;
import java.util.List;

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

    public String getErrorMessagesAsOneFormattedString() {
        StringBuffer result = new StringBuffer();

        if (messages != null && !messages.isEmpty()) {
            for (String message : messages) {
                result.append(message);
                result.append("\n");
            }
        }
        return result.toString();
    }

}
