package edu.cornell.kfs.pmw.batch.businessobject;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;

public class KfsAchDataWrapper {

    private PayeeACHAccount payeeAchAccount;
    private String payeeAchAccountExplanation;
    private List<Note> payeeAchAccountNotes;
    private List<String> errorMessages;
    
    public KfsAchDataWrapper() {
        this.payeeAchAccount = new PayeeACHAccount();
        this.payeeAchAccountExplanation = new String();
        this.payeeAchAccountNotes = new ArrayList<Note>();
        this.errorMessages = new ArrayList<String>();
    }
    
    public KfsAchDataWrapper(PayeeACHAccount payeeAchAccount, String payeeAchAccountExplanation, List<Note> payeeAchAccountNotes, List<String> errorMessages) {
        this.payeeAchAccount = payeeAchAccount;
        this.payeeAchAccountExplanation = payeeAchAccountExplanation;
        this.payeeAchAccountNotes = payeeAchAccountNotes;
        this.errorMessages = errorMessages;
    }

    public PayeeACHAccount getPayeeAchAccount() {
        return payeeAchAccount;
    }

    public void setPayeeAchAccount(PayeeACHAccount payeeAchAccount) {
        this.payeeAchAccount = payeeAchAccount;
    }

    public String getPayeeAchAccountExplanation() {
        return payeeAchAccountExplanation;
    }

    public void setPayeeAchAccountExplanation(String payeeAchAccountExplanation) {
        this.payeeAchAccountExplanation = payeeAchAccountExplanation;
    }

    public List<Note> getPayeeAchAccountNotes() {
        return payeeAchAccountNotes;
    }

    public void setPayeeAchAccountNotes(List<Note> payeeAchAccountNotes) {
        this.payeeAchAccountNotes = payeeAchAccountNotes;
    }
    
    public List<String> getErrorMessages() {
        return errorMessages;
    }
    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }

    public boolean noProcessingErrorsGenerated() {
         return errorMessages.isEmpty();
    }
    
}
