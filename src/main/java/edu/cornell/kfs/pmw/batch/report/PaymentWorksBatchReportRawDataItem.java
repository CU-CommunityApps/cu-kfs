package edu.cornell.kfs.pmw.batch.report;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.sys.KFSConstants;

public class PaymentWorksBatchReportRawDataItem {
    
    private String pmwFlatDdto;
    private List<String> errorMessages;
    
    public PaymentWorksBatchReportRawDataItem() {
        this.pmwFlatDdto = KFSConstants.EMPTY_STRING;
        this.errorMessages = new ArrayList<String>();
    }
    
    public PaymentWorksBatchReportRawDataItem(String pmwFlatDdto, String errorMessage) {
        this.pmwFlatDdto = pmwFlatDdto;
        this.addErrorMessage(errorMessage);
    }
    
    public PaymentWorksBatchReportRawDataItem(String pmwFlatDdto, List<String> errorMessages) {
        this.pmwFlatDdto = pmwFlatDdto;
        this.errorMessages = errorMessages;
    }

    public String getpmwFlatDdto() {
        return pmwFlatDdto;
    }

    public void setPmwFlatDdto(String pmwFlatDdto) {
        this.pmwFlatDdto = pmwFlatDdto;
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
