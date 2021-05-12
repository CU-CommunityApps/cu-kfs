package edu.cornell.kfs.concur.batch.businessobject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.sys.batch.FlatFileData;
import org.kuali.kfs.sys.batch.FlatFileTransactionInformation;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants;

public class ConcurRequestExtractFile implements FlatFileData {
    
    private String fileName;
    private Date batchDate;
    private Integer recordCount;
    private KualiDecimal totalApprovedAmount;
    private List<ConcurRequestExtractRequestDetailFileLine> requestDetails;
    private FlatFileTransactionInformation fileTransactionInformation;
    private String fullyQualifiedPdpFileName;
    
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

    public String getFullyQualifiedPdpFileName() {
        return fullyQualifiedPdpFileName;
    }

    public void setFullyQualifiedPdpFileName(String fullyQualifiedPdpFileName) {
        this.fullyQualifiedPdpFileName = fullyQualifiedPdpFileName;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(KFSConstants.NEWLINE).append("ConcurRequestExtractFile::   fileName: ").append(fileName).append(KFSConstants.NEWLINE);
        sb.append("batchDate: ").append(batchDate).append(ConcurConstants.SPACING_STRING_FOR_OUTPUT);
        sb.append("recordCount: ").append(recordCount).append(ConcurConstants.SPACING_STRING_FOR_OUTPUT);
        sb.append("totalApprovedAmount: ").append(totalApprovedAmount).append(KFSConstants.NEWLINE);
        sb.append("fullyQualifiedPdpFileName: ").append(fullyQualifiedPdpFileName).append(KFSConstants.NEWLINE);
        sb.append("requestDetails:   ").append(KFSConstants.NEWLINE).append(KFSConstants.NEWLINE);
        for (ConcurRequestExtractRequestDetailFileLine detailLines : requestDetails) {
            sb.append(detailLines.toString()).append(KFSConstants.NEWLINE).append(KFSConstants.NEWLINE);
        }
        return sb.toString();
    }

}
