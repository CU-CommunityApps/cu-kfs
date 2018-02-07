package edu.cornell.kfs.pmw.batch.report;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.sys.KFSConstants;

public class PaymentWorksBatchReportVendorItem {
    
    private String pmwVendorId;
    private Timestamp pmwSubmissionTimeStamp;
    private String pmwVendorType;
    private String pmwVendorLegelName;
    private String pmwVendorLegelFirstName;
    private String pmwVendorLegelLastName;
    private String pmwTaxIdType;
    private String pmwSubmitterEmailAddress;
    private String pmwInitiatorNetId;
    private List<String> errorMessages;
    
    public PaymentWorksBatchReportVendorItem() {
        this.pmwVendorId = KFSConstants.EMPTY_STRING;
        this.pmwSubmissionTimeStamp = null;
        this.pmwVendorType = KFSConstants.EMPTY_STRING;
        this.pmwVendorLegelName = KFSConstants.EMPTY_STRING;
        this.pmwVendorLegelFirstName = KFSConstants.EMPTY_STRING;
        this.pmwVendorLegelLastName = KFSConstants.EMPTY_STRING;
        this.pmwTaxIdType = KFSConstants.EMPTY_STRING;
        this.pmwSubmitterEmailAddress = KFSConstants.EMPTY_STRING;
        this.pmwInitiatorNetId = KFSConstants.EMPTY_STRING;
        this.errorMessages = new ArrayList<String>();
    }
    
    public PaymentWorksBatchReportVendorItem(String pmwVendorId,
           Timestamp pmwSubmissionTimeStamp,
           String pmwVendorType,
           String pmwVendorLegelName,
           String pmwVendorLegelFirstName,
           String pmwVendorLegelLastName,
           String pmwTaxIdType,
           String pmwSubmitterEmailAddress,
           String pmwInitiatorNetId, 
           List<String> errorMessages) {
       
        this.pmwVendorId = pmwVendorId;
        this.pmwSubmissionTimeStamp = pmwSubmissionTimeStamp;
        this.pmwVendorType = pmwVendorType;
        this.pmwVendorLegelName = pmwVendorLegelName;
        this.pmwVendorLegelFirstName = pmwVendorLegelFirstName;
        this.pmwVendorLegelLastName = pmwVendorLegelLastName;
        this.pmwTaxIdType = pmwTaxIdType;
        this.pmwSubmitterEmailAddress = pmwSubmitterEmailAddress;
        this.pmwInitiatorNetId = pmwInitiatorNetId;
        this.errorMessages = errorMessages;
    }

    public String getPmwVendorId() {
        return pmwVendorId;
    }

    public void setPmwVendorId(String pmwVendorId) {
        this.pmwVendorId = pmwVendorId;
    }

    public Timestamp getPmwSubmissionTimeStamp() {
        return pmwSubmissionTimeStamp;
    }

    public void setPmwSubmissionTimeStamp(Timestamp pmwSubmissionTimeStamp) {
        this.pmwSubmissionTimeStamp = pmwSubmissionTimeStamp;
    }

    public String getPmwVendorType() {
        return pmwVendorType;
    }

    public void setPmwVendorType(String pmwVendorType) {
        this.pmwVendorType = pmwVendorType;
    }

    public String getPmwVendorLegelName() {
        return pmwVendorLegelName;
    }

    public void setPmwVendorLegelName(String pmwVendorLegelName) {
        this.pmwVendorLegelName = pmwVendorLegelName;
    }

    public String getPmwVendorLegelFirstName() {
        return pmwVendorLegelFirstName;
    }

    public void setPmwVendorLegelFirstName(String pmwVendorLegelFirstName) {
        this.pmwVendorLegelFirstName = pmwVendorLegelFirstName;
    }

    public String getPmwVendorLegelLastName() {
        return pmwVendorLegelLastName;
    }

    public void setPmwVendorLegelLastName(String pmwVendorLegelLastName) {
        this.pmwVendorLegelLastName = pmwVendorLegelLastName;
    }

    public String getPmwTaxIdType() {
        return pmwTaxIdType;
    }

    public void setPmwTaxIdType(String pmwTaxIdType) {
        this.pmwTaxIdType = pmwTaxIdType;
    }

    public String getPmwSubmitterEmailAddress() {
        return pmwSubmitterEmailAddress;
    }

    public void setPmwSubmitterEmailAddress(String pmwSubmitterEmailAddress) {
        this.pmwSubmitterEmailAddress = pmwSubmitterEmailAddress;
    }

    public String getPmwInitiatorNetId() {
        return pmwInitiatorNetId;
    }

    public void setPmwInitiatorNetId(String pmwInitiatorNetId) {
        this.pmwInitiatorNetId = pmwInitiatorNetId;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }
    
    public void addErrorMessage(String errorMessage) {
        if (this.errorMessages == null) {
            this.errorMessages = new ArrayList<String>();
        }
        this.errorMessages.add(errorMessage);
    }
}
