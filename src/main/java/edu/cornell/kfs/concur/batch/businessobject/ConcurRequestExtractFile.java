package edu.cornell.kfs.concur.batch.businessobject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.sys.batch.FlatFileData;
import org.kuali.kfs.sys.batch.FlatFileTransactionInformation;
import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;
import org.kuali.rice.core.api.util.type.KualiDecimal;

public class ConcurRequestExtractFile extends TransientBusinessObjectBase implements FlatFileData {
    
    private String fileName;
    private Date batchDate;
    private Integer recordCount;
    private KualiDecimal totalApprovedAmount;
    private List<ConcurRequestExtractRequestDetailFileLine> requestDetails;
    private FlatFileTransactionInformation fileTransactionInformation;
    
    public ConcurRequestExtractFile() {
        this.requestDetails = new ArrayList<ConcurRequestExtractRequestDetailFileLine>();
    }
    
    public FlatFileTransactionInformation getFlatFileTransactionInformation() {
        return this.fileTransactionInformation = new FlatFileTransactionInformation(this.getFileName());
    }
    
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getBatchDate() {
        return batchDate;
    }

    public void setBatchDate(Date batchDate) {
        this.batchDate = batchDate;
    }

    public Integer getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Integer recordCount) {
        this.recordCount = recordCount;
    }

    public KualiDecimal getTotalApprovedAmount() {
        return totalApprovedAmount;
    }
    
    public void setTotalApprovedAmount(KualiDecimal totalApprovedAmount) {
        this.totalApprovedAmount = totalApprovedAmount;
    }
    
    public List<ConcurRequestExtractRequestDetailFileLine> getRequestDetails() {
        return requestDetails;
    }

    public void setRequestDetails(List<ConcurRequestExtractRequestDetailFileLine> requestDetails) {
        this.requestDetails = requestDetails;
    }
}
